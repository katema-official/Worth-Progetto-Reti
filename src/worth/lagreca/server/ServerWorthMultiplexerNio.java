package worth.lagreca.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import worth.lagreca.constants.Constants;
import worth.lagreca.server.rmi.CallbackImplementation;


public class ServerWorthMultiplexerNio implements Runnable{

	private boolean DEBUG = true;
	
	//classe che rappresenta l'oggetto server vero e proprio, dove cioè
	//il server si mette in attesa di connessioni da parte dei client
	//per poi gestirne le varie richieste in tcp. Un oggetto di questa
	//classe viene istanziato nel @see ServerWorthMain dopo che
	//le strutture dati del server sono state inizializzate.
	
	public static Selector selector;
	private ThreadPoolExecutor executor;
	
	private static CallbackImplementation ci = null;
	//metodo per ottenere l'oggetto @see CallbackImplementation che mi serve al corretto funzionamento di alcune operazioni
	public void initializeServerWorthMultiplexerNio(CallbackImplementation callback_impl) {
		ci = callback_impl;
	}
	
	@Override
	public void run(){
		
		//creo il ThreadPoolExecutor così da non sovraccaricare il server quando riceve richieste
		//da perte dei client (usato alla riga 497)
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		
		//per poter mettere in ascolto il server, devo prima creare il server.
		ServerSocketChannel ssc = null;
		try {
			ssc = ServerSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//prendo la ServerSocket della ServerSocketChannel
		ServerSocket ss = ssc.socket();
		
		//lego alla serverSocket l'indirizzo ip del localhost e un numero di porta
		try {
			ss.bind(new InetSocketAddress("localhost", Constants.PORT_TCP));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//dato che voglio usare il channel del server in modalità non bloccante, setto che il canale non deve essere bloccante
		try {
			ssc.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//creo un selector
		selector = null;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//registro su questo selector il ServerSocketChannel
		try {
			ssc.register(selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Inizializzazione Server in ascolto: ok");
		
		//avvio il server
		while(true) {
					
			//chiamo la select
			try {
				selector.select();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("La select si è sbloccata");
			
			//quando la select, che è bloccante, sarà terminata, potrò prendere il suo insieme di SelectedKeys, e decidere cosa
			//fare in base al tipo di operazione pronta
			Set<SelectionKey> rkeys= selector.selectedKeys();
			
			//costruisco un iteratore per l'insieme delle chiavi pronte. Una chiave mi rappresenta un canale registrato sul selector
			//che ha pronta un'operazione di un certo tipo. 
			Iterator<SelectionKey> iteratorrkeys = rkeys.iterator();
			
			//scorro tutte le chiavi pronte
			while(iteratorrkeys.hasNext()) {
				SelectionKey key = iteratorrkeys.next();
				iteratorrkeys.remove();
				
				//controllo che tipo di chiave è. Posso averne solo di tre tipi: connessione (il ServerSocketChannel),
				//lettura o scrittura (il SocketChannel legato al client)
				if(key.isAcceptable()) {
					
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Accetto una connessione da un client");
					
					//prendo il channel relativo al Listening Socket
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					
					//accetto la connessione, e ottengo il SocketChannel del client che si è connesso
					SocketChannel client = null;
					try {
						client = server.accept();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					//setto il client a non blocking, così da poterlo registrare sul selettore
					try {
						client.configureBlocking(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					//associo il client (channel) al selettore, indicando per il client interesse verso operazioni
					//di lettura (devo leggere il suo messaggio). Solo DOPO specificherò l'interesse alle operazioni di scrittura.
					SelectionKey clientKey = null;
					try {
						clientKey = client.register(selector, SelectionKey.OP_READ);
					} catch (ClosedChannelException e) {
						e.printStackTrace();
					}
					
					//associo a questo specifico collegamento client-server un oggetto che mantiene tutte le informazioni di cui
					//ho bisogno per gestire la connessione
					ServerWorthObjectAttached swoa = new ServerWorthObjectAttached();
					clientKey.attach(swoa);
					
				}
				if(key.isValid() && key.isReadable()) {
					
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Sto per leggere i dati del client");
					
					readDataFromClient(key);

				}
				if(key.isValid() && key.isWritable()) {
					
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("key in modalità scrittura");
					
					sendSomethingToClient(key);
					
				}
				
			}
			
		}
		
	}
	
	
	
	private void readDataFromClient(SelectionKey key) {
		//prendo l'attachment della connessione
		ServerWorthObjectAttached key_att = (ServerWorthObjectAttached) key.attachment();
		
		//prendo il channel client-server
		SocketChannel CSchannel = (SocketChannel) key.channel();
		
		//preparo il bytebuffer
		ByteBuffer bb = key_att.buffer;
		
		//vado a capire a che punto della lettura sono (se devo ancora capire che tipo
		//di operazione il client vuole effettuare, se sto leggendo la lunghezza dei dati,
		//se sto leggendo i dati o se ho finito)
		switch(key_att.reading_state) {
			case Constants.ATT_STATE_OPERATION_IDENTIFICATION:
				
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Stato lettura dati dal client: ATT_STATE_OPERATION_IDENTIFICATION");
				
				//in questo stato, devo ancora identificare il tipo di operazione da effettuare
				
				//Il buffer settato a null indica che non ho ancora letto niente. In tal caso,
				//procedo dunque alla creazione del buffer
				if(bb == null) {
					//alloco il bytebuffer per un int, in quanto devo leggere l'id dell'operazione
					bb = ByteBuffer.allocate(4);
				}
				
				//leggo il contenuto del channel
				try {
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Leggo i dati del client");
					CSchannel.read(bb);
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("A + valore key_Att.reading_state = " + key_att.reading_state);
					
					//per la gestione del crash
					if(bb.position() == 0) key_att.failed_to_read_counter++;
					if(key_att.failed_to_read_counter > 10) throw new IOException();
					
					if(key.isValid()) {
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("La key è valida per la lettura");
						//controllo quanti byte ho letto dal channel
						//se non sono riuscito a leggere tutti i dati dal channel...
						if(bb.position() != bb.capacity()) {
							//...salvo i progressi fatti del buffer sull'attachment...
							key_att.buffer = bb;
							
							//...e aggiorno l'attachment di questa chiave.
							key.attach(key_att);
							if(Constants.GLOBALDEBUG && DEBUG) System.out.println("AA + valore key_Att.reading_state = " + key_att.reading_state);
							
						}else {
							//se invece ho letto tutto quello che dovevo dal channel...
							//...mi preparo a leggere il contenuto del buffer
							bb.flip();
							
							//mi salvo l'id dell'operazione richiesta
							key_att.id_operation = bb.getInt();
							
							//cambio lo stato dell'automa di questa connessione
							key_att.reading_state = Constants.ATT_STATE_DATA_LENGTH;
							
							//cancello il buffer
							key_att.buffer = null;
							
							//aggiorno l'attachment della chiave
							key.attach(key_att);
							if(Constants.GLOBALDEBUG && DEBUG) System.out.println("AAA + valore key_Att.reading_state = " + key_att.reading_state);
							
						}
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("AAAA + bb = " + bb);
					}
					
				} catch (IOException e) {
					//gestione del crash del client
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("RESET DELLA CONNESSIONE");
					try {
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("update Crash");
						ci.updateCrash(key_att.username);
						
					} catch (RemoteException e1) {
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Remote exception durante l'update Crash");
						e1.printStackTrace();
					}finally {
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Cancello la key a seguito di un crash");
						//cancello la chiave
						key.cancel();
						
						//e chiudo il channel
						try {
							key.channel().close();
						} catch (IOException e2) {
							e2.printStackTrace();
						}
					}
				}
				
				
			break;
			case Constants.ATT_STATE_DATA_LENGTH:
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Stato lettura dati dal client: ATT_STATE_DATA_LENGTH");
				
				//in questo caso devo leggere la lunghezza del prossimo dato inviato dal client
				//(che può essere anche -1, a indicare che non ci sono ulteriori dati da leggere)
				//Siccome molte delle operazioni effettuate in questi altri casi sono simili
				//a quelle del primo caso, non commenterò nuovamente quelle operazioni. Saranno
				//commentate solo le azioni diverse
				
				if(bb == null) {
					bb = ByteBuffer.allocate(4);
				}
				try {
					CSchannel.read(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(bb.position() != bb.capacity()) {
					key_att.buffer = bb;
					key.attach(key_att);
				}else {
					bb.flip();
					key_att.bytes_to_read = bb.getInt();
					
					//devo distinguere due casi:
					//ho letto -1: vuol dire che ho letto tutti i dati mandati dal client
					//ho letto un valore diverso da -1: vuol dire che c'è un dato da leggere
					if(key_att.bytes_to_read == Constants.END_OF_BYTES) {
						key_att.reading_state = Constants.ATT_STATE_ALL_DATA_READ;
						//in questo caso ho finito di leggere i dati che il client mi ha inviato,
						//quindi posso soddisfare o declinare la sua richiesta e poi rispondergli
						
						//siccome alcune richieste potrebbero essere costose in termini di tempo,
						//in quanto a volte il server deve andare a leggere qualcosa da disco,
						//modificarlo e poi riscriverlo, per evitare che queste operazioni impediscano
						//al server di servire nel frattempo altri client, creo un thread che gestisce
						//questa specifica richiesta del client. Quando la richiesta sarà stata
						//gestita e i dati da mandare al client saranno pronti, imposterò l'interest set
						//della key di questo client come interessato all'operazione di scrittura
						ServerRequestHandler srh = new ServerRequestHandler(key);
						executor.execute(srh);
						
						//quando il thread avrà finito, l'interest set della chiave sarà cambiato
						//da lettura a scrittura (@see ServerRequestHandler)	
						
					}else {
						key_att.reading_state = Constants.ATT_STATE_DATA_READING;
					}
					
					key_att.buffer = null;
					key.attach(key_att);
				}
				
			break;
			case Constants.ATT_STATE_DATA_READING:
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Stato lettura dati dal client: ATT_STATE_DATA_READING");
				
				//in questo caso devo leggere i dati dal client e dargli un significato
				//in base a quanti dati ho già letto e il tipo di operazione richiesta dal client
				if(bb == null) {
					bb = ByteBuffer.allocate(key_att.bytes_to_read);
				}
				try {
					CSchannel.read(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(bb.position() != bb.capacity()) {
					key_att.buffer = bb;
					key.attach(key_att);
				}else {
					bb.flip();
					
					//leggo il dato
					byte[] messageReceivedBytes = new byte[key_att.bytes_to_read];
					bb.get(messageReceivedBytes);
					String s = new String(messageReceivedBytes, StandardCharsets.UTF_8);
					key_att.setNewReadData(s);
					key_att.reading_state = Constants.ATT_STATE_DATA_LENGTH;
					key_att.buffer = null;
					key.attach(key_att);
				}
				
			break;
			
		}
		
	}
	
	//metodo chiamato quando è il momento di scrivere qualcosa al client (perché
	//bisogna dargli una risposta in merito a un'operazione effettuata dal client al server)
	private void sendSomethingToClient(SelectionKey key) {
		
		//prendo l'attachment della connessione
		ServerWorthObjectAttached key_att = (ServerWorthObjectAttached) key.attachment();
		
		//caso particolare: se l'operazione che il client ha appena effettuato
		//era quella di login e questa non è andata a buon fine, devo scrivere
		//l'esito di questa operazione al client e chiudere la connessione con lui
		if(key_att.id_operation == Constants.OP_LOGIN && key_att.resInt != Constants.RES_LOGIN_SUCCESS) {
			writeIntToClient(key);
			
			//se il buffer è a null vuol dire che l'intero è stato inviato completamente al client
			if(key_att.buffer == null) {
			
				//il login è fallito, cancello la chiave
				key.cancel();
				
				//e chiudo il channel
				try {
					key.channel().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("ServerWorthMultiplexerNio: Cancellata una chiave a seguito di un login fallito");
			}
		}
		
		
		
		//in questo metodo devo rispondere al client circa l'esito dell'operazione che ha richiesto.
		//per poterlo fare, devo prima distinguere quale operazione ha effettuato, e in base a questa
		//rispondergli nel modo appropriato
		switch(key_att.id_operation) {
			case Constants.OP_LOGIN:
				writeIntToClient(key);
			break;
			case Constants.OP_LOGOUT:
				writeIntToClient(key);
				
				//se ho comunicato al client che il logout è avvenuto con successo
				if(key_att.buffer == null) {
					
					//cancello la chiave
					key.cancel();
					
					//e chiudo il channel
					try {
						key.channel().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("ServerWorthMultiplexerNio: Cancellata una chiave a seguito di un logout");
			break;

			case Constants.OP_CREATEPROJECT:
				writeIntToClient(key);
			break;
			case Constants.OP_ADDMEMBER:
				writeIntToClient(key);
			break;
			case Constants.OP_SHOWMEMBERS:
				writeStringToClient(key);
			break;
			case Constants.OP_SHOWCARDS:
				writeStringToClient(key);
			break;
			case Constants.OP_SHOWCARD:
				writeStringToClient(key);
			break;
			case Constants.OP_ADDCARD:
				writeIntToClient(key);
			break;
			case Constants.OP_MOVECARD:
				writeIntToClient(key);
			break;
			case Constants.OP_GETCARDHISTORY:
				writeStringToClient(key);
			break;
			case Constants.OP_CANCELPROJECT:
				writeIntToClient(key);
			break;
			
		}
		
		
		
	}
	
	//metodo usato per mandare un intero al client. Questo intero rappresenterà l'esito di una
	//certa operazione (login, logout, addmember...), e sarà poi compito del client capire cosa
	//quell'intero significa
	private void writeIntToClient(SelectionKey key) {
		//prendo l'attachment della connessione
		ServerWorthObjectAttached key_att = (ServerWorthObjectAttached) key.attachment();
		
		//prendo il channel client-server
		SocketChannel CSchannel = (SocketChannel) key.channel();
		
		//preparo il bytebuffer
		ByteBuffer bb = key_att.buffer;
		
		//se non ho ancora scritto niente, alloco il buffer per l'intero
		if(bb == null) {
		
			//alloco il bytebuffer per un int
			bb = ByteBuffer.allocate(4);
			
			//scrivo l'intero di risposta sul buffer
			bb.putInt(key_att.resInt);
			bb.flip();
		}
		
		//mando il buffer sul channel
		try {
			CSchannel.write(bb);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//controllo quanti byte sono riuscito a scrivere.
		//se non li ho scritti tutti...
		if(bb.position() != bb.capacity()) {
			
			//...aggiorno lo stato del buffer...
			key_att.buffer = bb;
			
			//...e salvo le modifiche fatte sull'attachment della chiave
			key.attach(key_att);
		}else {
			//se invece ho finito di scrivere i byte sul channel...
			
			//...cancello il buffer...
			key_att.buffer = null;
			
			//...ripristino il valore delle variabili dell'attachment...
			key_att.operationCompleted();
			
			//...aggiorno l'attachment della chiave...
			key.attach(key_att);
			
			//...e imposto la chiave come interessata ad operazioni di lettura...
			key.interestOps(SelectionKey.OP_READ);
			
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il server ha mandato l'intero di errore/conferma al client");
		}
	}
	
	
	//metodo analogo a quello per mandare un intero, con la differenza che mando al client una stringa
	//che altro non sarà che un oggetto (o meglio, una struttura dati contenente più oggetti) json.
	private void writeStringToClient(SelectionKey key) {
		ServerWorthObjectAttached key_att = (ServerWorthObjectAttached) key.attachment();
		SocketChannel CSchannel = (SocketChannel) key.channel();
		ByteBuffer bb = key_att.buffer;
		
		//devo mandare, in sequenza, la lunghezza della stringa e poi la stringa
		
		switch(key_att.stateResString) {
			case 0:
				//mando la lunghezza della stringa
				if(bb == null) {
					bb = ByteBuffer.allocate(4);
					bb.putInt(key_att.resString.length());
					bb.flip();
				}
				
				try {
					CSchannel.write(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(bb.position() != bb.capacity()) {
					key_att.buffer = bb;
					key.attach(key_att);
				}else {
					key_att.buffer = null;
					key_att.stateResString = 1;
					key.attach(key_att);
				}
			break;
			case 1:
				//mando la stringa
				if(bb == null) {
					bb = ByteBuffer.allocate(key_att.resString.length());
					bb.put(key_att.resString.getBytes());
					bb.flip();
				}
				
				try {
					CSchannel.write(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(bb.position() != bb.capacity()) {
					key_att.buffer = bb;
					key.attach(key_att);
				}else {
					key_att.buffer = null;
					key_att.operationCompleted();
					key.attach(key_att);
					key.interestOps(SelectionKey.OP_READ);
				}
			break;
			default:
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Errore nell'invio di una stringa al client: lo stato di stateResString è > 1");
			break;
		}
		
	}
	
}
