package worth.lagreca.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import worth.lagreca.client.rmi.UsersInformationsImplementation;
import worth.lagreca.client.rmi.UsersInformationsInterface;
import worth.lagreca.constants.Constants;
import worth.lagreca.server.rmi.CallbackInterface;
import worth.lagreca.users.UtenteRegistrato;

public class ClientTcpOperations {
	
	private static boolean DEBUG = true;
	
	//classe che contiene tutti i metodi che il client può utilizzare
	//per interagire, mediante connessione TCP, col server. Non solo, questa
	//classe contiene anche le variabili utili al client per comunicare
	//in TCP col server (buffer, channel...)
	
	private static ByteBuffer buffer = null;
	private static SocketChannel client = null;
	private static int resultInt = -1;
	private static String resultString = null;
	private static SocketAddress address = null;
	
	//variabili usate nel meccanismo di iscrizione/disiscrizione alle callback RMI
	private static Registry registry = null;
	private static CallbackInterface ci = null;
	private static UsersInformationsInterface uii  = null;
	private static UsersInformationsInterface stub = null;
	
	//preparo il registry e lo stub per il meccanismo di RMI callback
	public static void setUpClient() {
		try {
			registry = LocateRegistry.getRegistry(Constants.PORT_REGISTRY);
			ci = (CallbackInterface) registry.lookup("CALLBACK");
			uii = new UsersInformationsImplementation();
			stub = (UsersInformationsInterface) UnicastRemoteObject.exportObject(uii, 0);
		}catch(IOException e){
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("setUpClient finito");
	}
	
	public static int clientLogin(String username, String password) {
		//Quando il client effettua il login, deve aprire la connessione col server
		//e registrarsi per le callback RMI riguardanti lo stato degli utenti di WORTH
		try {
			//apro la connessione TCP col server
			address = new InetSocketAddress("localhost", Constants.PORT_TCP);
			client = SocketChannel.open(address);
			
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Aperta la connessione col server");
			
			//gli mando l'identificatore dell'operazione di login
			sendId(Constants.OP_LOGIN);
			
			//gli mando l'username
			sendData(username);
			
			//gli mando la password
			sendData(password);
		
			//infine gli mando l'intero -1 in modo da segnalargli che i dati inviati sono finiti
			sendDataEnd();
			
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Mandati i dati al server");
		
			//ora devo leggere la risposta del server, che avrà sempre la stessa lunghezza di 4 byte,
			//dato che è un intero
			resultInt = receiveInt();
			
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("mandati i dati al server e ricevuta la risposta");
			
			if(resultInt == Constants.RES_LOGIN_UNKNOWN_USER) {
				//chiudo la connessione in quanto il login è fallito
				client.close();
				return resultInt;
			}
			if(resultInt == Constants.RES_LOGIN_WRONG_PASSWORD) {
				client.close();
				return resultInt;
			}
			if(resultInt == Constants.RES_LOGIN_USER_ALREADY_LOGGED) {
				client.close();
				return resultInt;
			}
			
			if(resultInt == Constants.RES_LOGIN_SUCCESS) {
				
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Login lato client ha avuto successo");
				
				//se il sistema mi ha riconosciuto, prima di loggarmi definitivamente,
				//devo sottoscrivermi al meccanismo di RMI callback
				String list = ci.registerForCallback(stub, username);
				
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("client registerForCallback ok");
				
				Gson gson = new Gson();
				
				//deserializzo la hashmap che ho ricevuto in formato json usando gson
				//source: https://howtodoinjava.com/gson/gson-serialize-deserialize-hashmap/
				Type type = new TypeToken<HashMap<String, UtenteRegistrato>>(){}.getType();
				HashMap<String, UtenteRegistrato> initialMap = gson.fromJson(list, type);
				
				ClientInfo.worthUsersList = initialMap;
				
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("client getInitialUsers ok");
				
				ClientInfo.nome_utente_loggato = username;
				
				return resultInt;
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("Errore lato client nella fase di login");
		return -1;
	}
	
	public static int clientLogout(String username) {
		//quando il client effettua il logout, deve prima disiscriversi dal meccanismo
		//di callback RMI, e poi chiudere la connessione col server
		
		sendId(Constants.OP_LOGOUT);
		sendData(username);
		sendDataEnd();
		resultInt = receiveInt();
		try {
			if(resultInt == Constants.RES_LOGOUT_SUCCESS) {
				ci.unregisterForCallback(stub, username);
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("======================================== SONO L'UTENTE " + ClientInfo.nome_utente_loggato + " E HO FATTO LOGOUT ========================================");
				ClientInfo.nome_utente_loggato = null;
				client.close();
				return resultInt;
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("Errore nella fase di logout lato client");
		return -1;
		
	}


	
	public static int clientCreateProject(String project_name) {
		//metodo con cui il client tenta di far creare al server un nuovo progetto.
		sendId(Constants.OP_CREATEPROJECT);
		sendData(project_name);
		sendDataEnd();
		resultInt = receiveInt();
		return resultInt;
	}
	
	public static int clientAddMember(String project_name, String new_member) {
		//metodo con cui il client tenta di aggiungere un membro al progetto che sta visualizzando
		sendId(Constants.OP_ADDMEMBER);
		sendData(project_name);
		sendData(new_member);
		sendDataEnd();
		resultInt = receiveInt();
		return resultInt;
		
	}
	
	public static HashSet<String> clientShowMembers(String project_name){
		//metodo con cui il client richiede al server la lista dei membri del progetto selezionato
		sendId(Constants.OP_SHOWMEMBERS);
		sendData(project_name);
		sendDataEnd();
		resultString = receiveString();
		
		Gson gson = new Gson();
		Type hashmap = new TypeToken<HashSet<String>>(){}.getType();
		HashSet<String> resultSet = gson.fromJson(resultString, hashmap);
		return resultSet;
		
	}
	
	public static ArrayList<String> clientShowCards(String project_name){
		//metodo con cui il client richiede al server la lista di card del progetto selezionato
		sendId(Constants.OP_SHOWCARDS);
		sendData(project_name);
		sendDataEnd();
		resultString = receiveString();
		
		Gson gson = new Gson();
		Type arraylist = new TypeToken<ArrayList<String>>(){}.getType();
		ArrayList<String> resultList = gson.fromJson(resultString, arraylist);
		return resultList;
		
	}
	
	public static ArrayList<String> clientShowCard(String project_name, String card_name){
		//metodo con cui il client richiede al server le informazioni di una card di un progetto
		sendId(Constants.OP_SHOWCARD);
		sendData(project_name);
		sendData(card_name);
		sendDataEnd();
		resultString = receiveString();
		
		Gson gson = new Gson();
		Type arraylist = new TypeToken<ArrayList<String>>(){}.getType();
		ArrayList<String> resultList = gson.fromJson(resultString, arraylist);
		return resultList;
		
	}
	
	public static int clientAddCard(String project_name, String card_name, String description) {
		//metodo per aggiungere una card ad un progetto
		sendId(Constants.OP_ADDCARD);
		sendData(project_name);
		sendData(card_name);
		sendData(description);
		sendDataEnd();
		resultInt = receiveInt();
		return resultInt;
	}
	
	public static int clientMoveCard(String project_name, String card_name, String starting_list, String destination_list) {
		//metodo per spostare una card da una lista all'altra del progetto a cui appartiene
		sendId(Constants.OP_MOVECARD);
		sendData(project_name);
		sendData(card_name);
		sendData(starting_list);
		sendData(destination_list);
		sendDataEnd();
		resultInt = receiveInt();
		return resultInt;
	}
	
	public static ArrayList<String> clientGetCardHistory(String project_name, String card_name){
		//metodo per richiedere al server l'history di una card del progetto selezionato
		sendId(Constants.OP_GETCARDHISTORY);
		sendData(project_name);
		sendData(card_name);
		sendDataEnd();
		resultString = receiveString();
		
		Gson gson = new Gson();
		Type arraylist = new TypeToken<ArrayList<String>>(){}.getType();
		ArrayList<String> resultList = gson.fromJson(resultString, arraylist);
		return resultList;
	}
	
	public static int clientCancelProject(String project_name) {
		//metodo per chiedere al server di cancellare il progetto corrente. La richiesta
		//verrà approvata solo se tutte le card sono nella lista done
		sendId(Constants.OP_CANCELPROJECT);
		sendData(project_name);
		sendDataEnd();
		resultInt = receiveInt();
		return resultInt;
	}
		
	
	
	//metodo che ogni metodo tra quelli sopra (ossia quelli per la comunicazione col server)
	//deve invocare subito per dire al server che tipo di operazione il client richiede
	private static void sendId(int id) {
		buffer = ByteBuffer.allocate(4);
		buffer.putInt(id);
		buffer.flip();
		try {
			client.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//metodo usato dai metodi sopra per indicare al server quanti byte sarà lungo il prossimo
	//dato che verrà inviato dal client, e allo stesso tempo per inviare il prossimo dato
	//che ha quella dimensione
	private static void sendData(String message) {
		//invio la lunghezza del dato
		buffer = ByteBuffer.allocate(4);
		int l = message.getBytes().length;
		buffer.putInt(l);
		buffer.flip();
		try {
			client.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//invio il dato
		buffer = ByteBuffer.allocate(l);
		buffer.put(message.getBytes());
		buffer.flip();
		try {
			client.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//metodo usato dai metodi sopra per indicare al server che tutti i dati sono stati inviati
	private static void sendDataEnd() {
		buffer = ByteBuffer.allocate(4);
		buffer.putInt(Constants.END_OF_BYTES);
		buffer.flip();
		try {
			client.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//metodo usato da alcuni dei metodi sopra per leggere l'intero di risposta
	private static int receiveInt() {
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Entrato in receiveInt");
		
		buffer = ByteBuffer.allocate(4);
		int read_bytes = 0;
		while(read_bytes < 4) {
			try {
				read_bytes += client.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il client ha letto i 4 byte (un intero)");
		buffer.flip();
		int res = buffer.getInt();
		return res;
	}
	
	//metodo usato da alcuni dei metodi sopra per leggere una stringa (solitamente è un json di una lista o comunque una struttura dati)
	private static String receiveString() {
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Entrato in receiveString");
		
		//prima leggo quanto è lunga
		buffer = ByteBuffer.allocate(4);
		int read_bytes = 0;
		while(read_bytes < 4) {
			try {
				read_bytes += client.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il client ha letto i 4 byte (la lunghezza della stringa)");
		buffer.flip();
		int length = buffer.getInt();
		
		//poi leggo la stringa
		String res = null;
		buffer = ByteBuffer.allocate(length);
		read_bytes = 0;
		while(read_bytes < length) {
			try {
				read_bytes += client.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il client ha letto la stringa");
		buffer.flip();
		byte[] receivedBytes = new byte[length];
		buffer.get(receivedBytes);
		res = new String(receivedBytes);	//, StandardCharsets.UTF_8);
		
		return res;
	}
	
}
