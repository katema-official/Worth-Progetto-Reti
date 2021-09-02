package worth.lagreca.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.google.gson.Gson;

import worth.lagreca.constants.Constants;
import worth.lagreca.projectsandcards.WorthCard;
import worth.lagreca.projectsandcards.WorthProjectManager;
import worth.lagreca.projectsandcards.WorthVolatileProjectInformations;
import worth.lagreca.server.rmi.CallbackImplementation;
import worth.lagreca.users.UtenteRegistratoConPassword;

public class ServerTcpOperations {
	
	private static boolean DEBUG = true;
	
	//classe contenente tutti i metodi che il server deve eseguire quando
	//riceve una richiesta tcp da un client.
	
	private static CallbackImplementation ci = null;
	//metodo per ottenere l'oggetto @see CallbackImplementation che mi serve al corretto funzionamento di alcune operazioni
	public static void initializeServerTcpOperations(CallbackImplementation callback_impl) {
		ci = callback_impl;
	}
	
	public static int serverLogin(String username, String password) {
		//metodo per loggare l'utente. Restituisce un codice di errore e interrompe la connessione
		//se l'username non esiste o se la password è sbagliata, o se l'utente che sta cercando di
		//loggarsi è già loggato. Altrimenti restituisce un messaggio di successo e continua la connessione
		
		synchronized(ServerInfo.ALL_USERS_MONITOR) {
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("ServerTcpOperations.serverLogin: ci sono entrato");
			
			if(!ServerInfo.doesUserExist(username)) {
				return Constants.RES_LOGIN_UNKNOWN_USER;
			}
			if(!ServerInfo.userAuthentication(username, password)) {
				return Constants.RES_LOGIN_WRONG_PASSWORD;
			}
			if(ServerInfo.getUserState(username) == Constants.ONLINE) {
				return Constants.RES_LOGIN_USER_ALREADY_LOGGED;
			}
			
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il client può effettuare il login");
			
			return Constants.RES_LOGIN_SUCCESS;
		}
		
	}
	
	public static int serverLogout(String username) {
		//metodo per fare il logout dell'utente. Siccome l'utente deve aver fatto il login prima
		//di poter fare il logout, questo metodo avrà sempre successo
		return Constants.RES_LOGOUT_SUCCESS;
		
	}
	

	
	public static int serverCreateProject(String project_name, String creator) {
		//Metodo usato per creare un nuovo progetto. Il progetto viene creato solo se non esiste
		//già un progetto con quel nome.
		synchronized(ServerInfo.ALL_PROJECTS_MONITOR){
			//devo controllare la struttura dati che mantiene i nomi dei progetti già esistenti.
			if(ServerInfo.progettiWorth.containsKey(project_name)) {
				//se il progetto esiste già, mi fermo e restituisco un codice di errore al client
				return Constants.RES_CREATEPROJECT_PROJECT_ALREADY_EXISTS;
			}else {
				//se invece posso creare il nuovo progetto, creo la directory ad esso dedicata,
				//un oggetto @see WorthProjectManager che lo rappresenta, includendo l'utente
				//creatore tra i membri del progetto, e restituisco al client un codice di successo
				
				//creo la directory
				String newProjectDirectory = ServerInfo.projectsDir + File.separator + project_name;
				File newProjectDir = new File(newProjectDirectory);
				boolean created = newProjectDir.mkdir();
				if(created && Constants.GLOBALDEBUG && DEBUG) System.out.println("Directory per il progetto " + project_name + " creata");
				
				//creo l'oggetto @see WorthProjectManager, aggiungendo il creatore del progetto tra i membri
				WorthProjectManager wpm = new WorthProjectManager(project_name, creator);
				
				//lo scrivo nella directory appena creata, serializzandolo
				serializeWorthProjectManager(wpm);
				
				//infine, aggiungo il nuovo progetto alla struttura dati
				ServerInfo.progettiWorth.put(project_name, new WorthVolatileProjectInformations(null));
				
				//gli associo un indirizzo di multicast, che sicuramente avrà in quanto un utente online ha appena creato
				//un nuovo progetto
				ServerInfo.progettiWorth.get(project_name).incrementNumberOfUsersUsingThisProject();
				String ma = ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject();
				
				//il progetto è stato creato, ma nell'oggetto @see UtenteRegistratoConPassword devo aggiornare la lista
				//di progetti di cui l'utente fa parte, non solo nella memoria del processo server, ma anche sull'oggetto serializzato
				UtenteRegistratoConPassword urcp = ServerInfo.getUtenteRegistratoConPassword(creator);
				urcp.addProjectToListOfUsersProjects(project_name);
				
				//qui avviene la serializzazione
				ServerInfo.updateExistingUserInformations(urcp);
				
				//aggiorno il creatore con il meccanismo di RMI callback per dirgli di avviare il thread relativo alla chat di questo progetto
				try {
					ci.updateChatCreateProject(creator, project_name, ma);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
				//e restituisco un codice di successo
				return Constants.RES_CREATEPROJECT_PROJECT_CREATED;
				
			}
			
		}
		
	}
	
	public static int serverAddMember(String project_name, String new_member) {
		//metodo lato server per aggiungere un membro ad un progetto
		
		//siccome questo metodo potrebbe essere eseguito in maniera concorrente da due utenti che già fanno parte del progetto project_name
		//e che vogliono aggiungere contemporaneamente, nel peggiore dei casi, lo stesso new_member, assicuro l'atomicità di questa operazione
		//sincronizzandomi sul progetto
		try {
			synchronized(ServerInfo.progettiWorth.get(project_name)) {
				if(ServerInfo.progettiWorth.containsKey(project_name)) {
				
					//innanzitutto devo sincerarmi che questo utente esista, altrimenti non ha senso proseguire
					if(!ServerInfo.doesUserExist(new_member)) {
						return Constants.RES_ADDMEMBER_UNKNOWN_USER;
					}
					
					//se l'utente esiste ma appartiene già a questo progetto, restituisco un codice di errore diverso
					if(ServerInfo.getUtenteRegistratoConPassword(new_member).getProjectsOfUser().contains(project_name)) {
						return Constants.RES_ADDMEMBER_MEMBER_ALREADY_IN_PROJECT;
					}
					
					//se nessuna delle due situazioni si è verificata, vuol dire che devo aggiungere il nuovo membro al progetto. In questo caso,
					//ci sono delle operazioni che vanno sempre effettuate e altre che solo a volte vanno effettuate. In particolare:
					//OBBLIGATORIO: prima di dire al client che l'utente è stato davvero aggiunto al progetto, devo rendere persistente il
					//cambiamento avvenuto sul server. Per farlo, devo aggiornare il file json che rappresenta il nuovo membro, aggiornando
					//la sua lista di progetti a cui partecipa (stesso dicasi per l'oggetto presente in memoria che lo rappresenta), ma devo
					//aggiornare anche il @see WorthProjectManager di questo progetto affinché si salvi sulla lista di partecipanti al progetto
					//il nome di questo nuovo membro.
					//OPZIONALE: se per puro caso il nuovo membro che il client sta aggiungendo al progetto è online, devo segnarmi che quindi
					//c'è un utente in più che sta usando questo progetto (quello appena aggiunto per l'appunto, @see WorthVolatileProjectInformations),
					//e avvertirlo mediante RMI callback di generare un nuovo thread che si metta in attesa dei messaggi della chat di questo progetto.
					
					//dunque: cominciamo col cambiare lo stato degli oggetti relativi all'utente
					UtenteRegistratoConPassword urcp = ServerInfo.getUtenteRegistratoConPassword(new_member);
					urcp.addProjectToListOfUsersProjects(project_name);
					ServerInfo.updateExistingUserInformations(urcp);
					
					//cambiamo adesso le informazioni contenute nel @see WorthProjectManager di questo progetto, deserializzandolo, modificando lo stato,
					//e riserializzandolo
					WorthProjectManager wpm = deserializeWorthProjectManager(project_name);
		
					//aggiorno il contenuto del file e lo riscrivo sul disco
					wpm.listMembersName.add(new_member);
					serializeWorthProjectManager(wpm);
		
					//a questo punto, devo capire se l'utente che ho aggiunto è online in questo momento. Perché se non lo è ho vita facile,
					//e posso finire qui, tanto ci penserà il meccanismo di RMI callback al momento del login a dirgli "guarda, ora fai parte
					//anche di questo progetto, crea un thread che ne gestisca la chat".
					//Se però è online, devo notificarlo subito del fatto che ora fa parte di un altro progetto. Quindi devo usare subito
					//il meccanismo di RMI callback
					if(ServerInfo.getUtenteRegistrato(new_member).getUtenteRegistratoState() == Constants.ONLINE) {
						ServerInfo.progettiWorth.get(project_name).incrementNumberOfUsersUsingThisProject();
						String ma = ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject();
						
						//aggiorno il creatore con il meccanismo di RMI callback per dirgli di avviare il thread relativo alla chat di questo progetto
						try {
							ci.updateChatAddMember(new_member, project_name, ma);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					
					//l'operazione ha avuto successo, quindi posso avvertire gli utenti del progetto sulla chat con un messaggio (se la chat esiste)
					if(ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject() != null) {
						InetAddress chatgroup;
						DatagramSocket ds;
						try {
							ds = new DatagramSocket();
							chatgroup = InetAddress.getByName(ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject());
							String message = "Sistema (notifica generata automaticamente): " + new_member + " e' stato aggiunto al progetto\n";
							DatagramPacket dp = new DatagramPacket(message.getBytes(), 0, message.getBytes().length, chatgroup, 4321);
							ds.send(dp);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (SocketException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					
					}
					
					return Constants.RES_ADDMEMBER_SUCCESS;
				}else {
					throw new NullPointerException();
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente
			return Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE;
		}
		
	}
	
	public static String serverShowMembers(String project_name){
		//metodo per restituire a un utente la lista dei membri di un progetto
		
		//leggo il project manager di questo progetto per ottenere la lista dei partecipanti
		try {
			synchronized(ServerInfo.progettiWorth.get(project_name)) {
				if(ServerInfo.progettiWorth.containsKey(project_name)) {
					
					WorthProjectManager wpm = deserializeWorthProjectManager(project_name);
					
					//poi la serializzo, poiché voglio mandare una stringa sulla connessione TCP
					HashSet<String> hs = wpm.listMembersName;
					Gson gson = new Gson();
					String res = gson.toJson(hs);
					return res;
				}else {
					throw new NullPointerException();
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente sottoforma di stringa (che andrà deserializzata e analizzato il primo elemento)
			
			//in questo caso non mi basta restituire un valore speciale: se un utente si chiama "9" e restituisco come elemento in cima al set la costante "9"
			//in caso di errore, il client potrebbe pensare che si tratti, appunto, di un errore. Per risolvere il problema restituisco una stringa che sicuramente
			//non è il nome di un utente: una stringa più lunga di 20 caratteri
			HashSet<String> hs = new HashSet<String>();
			String s = "";
			for(int i=0; i<21; i++) {
				s += String.valueOf(Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE);
			}
			hs.add(s);
			Gson gson = new Gson();
			String res = gson.toJson(hs);
			return res;
		}
		
		
	}
	
	public static String serverShowCards(String project_name){
		//metodo per restituire a un utente la lista delle card di questo progetto
		
		//leggo il project manager di questo progetto per ottenere le quattro liste di card, costruirne una e restituirla (serializzata)
		try {
			synchronized(ServerInfo.progettiWorth.get(project_name)) {
				if(ServerInfo.progettiWorth.containsKey(project_name)) {
					Gson gson = new Gson();
					WorthProjectManager wpm = deserializeWorthProjectManager(project_name);
					
					//costruisco un'unica lista "fondendo" le quattro liste di card e la serializzo, per poi mandarla al client
					ArrayList<String> al = new ArrayList<String>();
					al.addAll(wpm.toDo);
					al.addAll(wpm.inProgress);
					al.addAll(wpm.toBeRevised);
					al.addAll(wpm.done);
					
					//se la lista è vuota, restituisco una stringa di errore al client
					if(al.isEmpty()) {
						al.add(Constants.RES_SHOWCARDS_NO_CARD);
					}
					
					String res = gson.toJson(al);
					return res;
				}else {
					throw new NullPointerException();
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente sottoforma di stringa (che andrà deserializzata e analizzato il primo elemento)
				
			//anche in questo caso non mi basta restituire un valore speciale: potrebbero esserci card chiamate "9". Risolvo il problema in modo analogo a prima,
			//restituendo una stringa che sicuramente non è il nome di una card: una che supera i 50 caratteri
			ArrayList<String> al = new ArrayList<String>();
			String s = "";
			for(int i=0; i<51; i++) {
				s += String.valueOf(Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE);
			}
			al.add(s);
			Gson gson = new Gson();
			String res = gson.toJson(al);
			return res;
		}
	}
	
	public static String serverShowCard(String project_name, String card_name){
		//metodo per mostrare le informazioni di una card all'utente che vuole visualizzarla. Per ottenere le informazioni
		//di una card, devo deserializzarla e inserire in una lista il suo nome, la sua descrizione e il suo stato. Fatto ciò,
		//devo serializzare questa lista così da spedirla al client sulla connessione TCP
		try {
			synchronized(ServerInfo.progettiWorth.get(project_name)) {
				if(ServerInfo.progettiWorth.containsKey(project_name)) {
					WorthCard wc = deserializeWorthCard(project_name, card_name);
					
					//costruisco un'unica lista "fondendo" le informazioni della card e la serializzo, per poi mandarla al client
					ArrayList<String> al = new ArrayList<String>();
					al.add(wc.name);
					al.add(wc.description);
					al.add(wc.currentState);
		
					Gson gson = new Gson();
					String res = gson.toJson(al);
					return res;
				}else {
					throw new NullPointerException();
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente sottoforma di stringa (che andrà deserializzata e analizzato il primo elemento)
			ArrayList<String> al = new ArrayList<String>();
			String s = "";
			for(int i=0; i<51; i++) {
				s += String.valueOf(Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE);
			}
			al.add(s);
			Gson gson = new Gson();
			String res = gson.toJson(al);
			return res;
		}
		
	}
	
	public static int serverAddCard(String project_name, String card_name, String description) {
		//Metodo per aggiungere una card ad un progetto. L'operazione consiste nel deserializzare il @see WorthProjectManager
		//relativo a questo progetto, capire se contiene già una card con il nome della nuova card, e nel caso restituire un
		//codice di errore al client. Altrimenti, viene modificato lo stato di quest'oggetto, e viene creato un nuovo file
		//che altro non è che la serializzazione di un oggetto @see WorthCard appena creato.
		try {
			synchronized(ServerInfo.progettiWorth.get(project_name)) {
				if(ServerInfo.progettiWorth.containsKey(project_name)) {
					//non accetto che una card abbia lo stesso nome del progetto. Quindi, se questo è il caso, restituisco
					//subito un errore al client
					if(project_name.equals(card_name)) {
						return Constants.RES_ADDCARD_CARD_NAME_EQUALS_PROJECT_NAME;
					}
					
					//innanzitutto, leggo il WorthProjectManager di questo progetto
					WorthProjectManager wpm = deserializeWorthProjectManager(project_name);
					
					//vado a vedere se in almeno una delle quattro liste di card del progetto c'è una card col nome della nuova card
					if(wpm.toDo.contains(card_name) || wpm.inProgress.contains(card_name) || wpm.toBeRevised.contains(card_name) || wpm.done.contains(card_name)) {
						return Constants.RES_ADDCARD_CARD_ALREADY_EXISTS;
					}
					
					//se il nome della card è troppo lungo, restituisco un errore
					if(card_name.length() > 50) {
						return Constants.RES_ADDCARD_CARD_NAME_TOO_LONG;
					}
					
					//altrimenti, aggiungo la card al progetto (nella lista todo, che è quella di partenza)
					wpm.toDo.add(card_name);
					
					//riscrivo l'oggetto modificato
					serializeWorthProjectManager(wpm);
					
					//creo la card vera e propria e la serializzo
					WorthCard new_card = new WorthCard(card_name, description);
					serializeWorthCard(new_card, project_name);
					
					//l'operazione ha avuto successo, quindi posso avvertire gli utenti del progetto sulla chat con un messaggio (se la chat esiste)
					if(ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject() != null) {
						InetAddress chatgroup;
						DatagramSocket ds;
						try {
							ds = new DatagramSocket();
							chatgroup = InetAddress.getByName(ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject());
							String message = "Sistema (notifica generata automaticamente): la card "+ card_name + " e' stata aggiunta al progetto\n";
							DatagramPacket dp = new DatagramPacket(message.getBytes(), 0, message.getBytes().length, chatgroup, 4321);
							ds.send(dp);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (SocketException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					//ora posso dire di aver creato la card
					return Constants.RES_ADDCARD_SUCCESS;
				}else {
					throw new NullPointerException();
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente
			return Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE;
		}
	}
	
	public static int serverMoveCard(String project_name, String card_name, String starting_list, String destination_list) {
		//Metodo per spostare una card da una lista all'altra di un progetto. Si possono presentare varie situazioni di errore:
		//0: La card non esiste
		//1: La lista di partenza specificata dal client non è quella dove si trova la card
		//2: La lista di partenza è corretta, ma lo spostamento non è valido (ad esempio, il client sta tentando si spostare
		//una card da "To do" a "Done")
		//se nessuna delle condizioni sopra si verifica, lo spostamento ha successo. Per capire se l'operazione richiesta
		//dal client può essere soddisfatta o no, devo controllare il @see WorthProjectManager del progetto in questione. Nel
		//caso si presenti una situazione di errore, mi fermo e restituisco un codice di errore al client. Altrimenti rendo
		//consistente il cambiamento voluto dal client, andando a modificare il WorthProjectManager del progetto e la
		//@see WorthCard della card selezionata, andando a deserializzarla, applicarle i cambiamenti voluti dall'utente, e
		//riserializzarla
		
		try {
			synchronized(ServerInfo.progettiWorth.get(project_name)) {
				if(ServerInfo.progettiWorth.containsKey(project_name)) {
					//parto col deserializzare il WorthProjectManager di questo progetto
					WorthProjectManager wpm = deserializeWorthProjectManager(project_name);
		
					//Controllo se la card esiste
					if((wpm.toDo.contains(card_name) || wpm.inProgress.contains(card_name) || wpm.toBeRevised.contains(card_name) || wpm.done.contains(card_name)) == false) {
						return Constants.RES_MOVECARD_UNKNOWN_CARD;
					}
					
					//Se esiste, controllo se la lista di partenza specificata dal client coincide con quella dove si trova adesso la card
					switch(starting_list) {
						case "To do":
							if(!wpm.toDo.contains(card_name)) {
								return Constants.RES_MOVECARD_FROM_STATE_ERROR;
							}
						break;
						case "In progress":
							if(!wpm.inProgress.contains(card_name)) {
								return Constants.RES_MOVECARD_FROM_STATE_ERROR;
							}
						break;
						case "To be revised":
							if(!wpm.toBeRevised.contains(card_name)) {
								return Constants.RES_MOVECARD_FROM_STATE_ERROR;
							}
						break;
						default:
							if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il client mi ha mandato una staring_list per la card " + card_name + " che non corrisponde a nessuna delle quattro possibili");
						break;
					}
					
					//Se la card esiste e si trova nella lista di partenza giusta, devo capire se lo spostamento che il client mi ha richiesto
					//è legittimo o no (seguendo le specifiche del progetto, gli spostamenti possibili sono:
					//To do -> In progress
					//In progress -> To be revised || done
					//To be revised -> In progress || done)
					switch(starting_list) {
						case "To do":
							if(!destination_list.equals("In progress")) {
								return Constants.RES_MOVECARD_INVALID_DISPLACEMENT;
							}
						break;
						case "In progress":
							if(Constants.GLOBALDEBUG && DEBUG) System.out.println("La card sul server è in progress, sto capendo se lo spostamento è legittimo");
							if(!destination_list.equals("To be revised") && !destination_list.equals("Done")) {
								if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Lo spostamento no è legittimo");
								return Constants.RES_MOVECARD_INVALID_DISPLACEMENT;
							}
						break;
						case "To be revised":
							if(!destination_list.equals("In progress") && !destination_list.equals("Done")) {
								return Constants.RES_MOVECARD_INVALID_DISPLACEMENT;
							}
						break;
						default:
						break;
					}
					
					//se nessuna delle condizioni sopra indicate si è verificata, lo spostamento può essere effettuato. Va quindi modificato
					//lo stato del WorthProjectManager di questa card, ma va anche modificato l'oggetto WorthCard che rappresenta questa card.
					
					//per spostare la card da una lista all'altra nel WorthProjectManager, la rimuovo dalla lista in cui si trova al momento
					//e la aggiungo a quella di destinazione
					
					//rimuovo la card dalla lista di partenza
					switch(starting_list) {
						case "To do":
							wpm.toDo.remove(card_name);
						break;
						case "In progress":
							wpm.inProgress.remove(card_name);
						break;
						case "To be revised":
							wpm.toBeRevised.remove(card_name);
						break;
						default:
						break;
					}
					
					//aggiungo la card alla lista di destinazione
					switch(destination_list) {
						case "In progress":
							wpm.inProgress.add(card_name);
						break;
						case "To be revised":
							wpm.toBeRevised.add(card_name);
						break;
						case "Done":
							wpm.done.add(card_name);
						break;
						default:
						break;
					}
					
					//riscrivo l'oggetto WorthProjectManager modificato
					serializeWorthProjectManager(wpm);
					
					//adesso vado a deserializzare il file serializzato che rappresenta questa carta (@ee WorthCard), modificarne lo stato,
					//e riserializzarlo
					
					//deserializzo il file relativo a questa card
					
					WorthCard wc = deserializeWorthCard(project_name, card_name);
					
					//Modifico lo stato di questa card. Per farlo, non solo devo cambiare lo stato della card, ma prima, visto che esiste anche
					//il metodo (client/server)getCardHistory, devo salvarmi lo stato attuale nella lista history della card
					wc.history.add(wc.currentState);
					wc.currentState = destination_list;
					
					//modificato lo stato, vado a riserializzare il file, così da rendere tutti i cambiamenti persistenti
					serializeWorthCard(wc, project_name);
					
					//l'operazione ha avuto successo, quindi posso avvertire gli utenti del progetto sulla chat con un messaggio (se la chat esiste)
					if(ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject() != null) {
						InetAddress chatgroup;
						DatagramSocket ds;
						try {
							ds = new DatagramSocket();
							chatgroup = InetAddress.getByName(ServerInfo.progettiWorth.get(project_name).getMulticastAddressOfThisProject());
							String message = "Sistema (notifica generata automaticamente): la card " + card_name + " e' stata spostata dalla lista " + starting_list +  " alla lista " + destination_list + "\n";
							DatagramPacket dp = new DatagramPacket(message.getBytes(), 0, message.getBytes().length, chatgroup, 4321);
							ds.send(dp);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (SocketException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					return Constants.RES_MOVECARD_SUCCESS;
				}else {
					throw new NullPointerException();
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente
			return Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE;
		}
	}
	
	public static String serverGetCardHistory(String project_name, String card_name){
		//Con questo metodo vado a restituire al client la history di una card del progetto project_name. Per farlo, devo deserializzare
		//l'oggetto relativo a questa card, copiarne la history, e restituirla al client
		//può verificarsi una situazione di errore qualora la card non esistesse: per sapere se esiste, devo prima controllare il
		//@see WorthProjectManager e verificare che una carta col nome card_name esista. Se non esisto, restituisco come risultato
		//una stringa che altro non è che un arraylist serializzato che contiene un solo elemento: il carattere "0". Dato che, se
		//il risultato è corretto, la lista contiene solo le stringhe "To do", "In progress", "To be revised" e "Done", il client,
		//leggendo il primo elemento della lista, sarà in grado di capire se la lista ricevuta è un messaggio di errore oppuere è
		//il risultato corretto.
		try {
			synchronized(ServerInfo.progettiWorth.get(project_name)) {
				if(ServerInfo.progettiWorth.containsKey(project_name)) {
					Gson gson = new Gson();
					
					//deserializzo il WorthProjectManager di questo progetto
					WorthProjectManager wpm = deserializeWorthProjectManager(project_name);
					
					//controllo se esiste una card col nome card_name in questo progetto. Se non esiste, restituisco l'arraylist di errore al client
					if((wpm.toDo.contains(card_name) || wpm.inProgress.contains(card_name) || wpm.toBeRevised.contains(card_name) || wpm.done.contains(card_name)) == false) {
						ArrayList<String> al = new ArrayList<String>();
						String s = "";
						for(int i=0; i<51; i++) {
							s += String.valueOf(0);
						}
						al.add(s);
						String res = gson.toJson(al);
						return res;
					}
					
					//se la card esiste, la deserializzo
					WorthCard wc = deserializeWorthCard(project_name, card_name);
				
					//mi creo una copia della history della card, e ci aggiungo anche lo stato attuale
					ArrayList<String> al = new ArrayList<String>();
					al.addAll(wc.history);
					al.add(wc.currentState);
					
					//serializzo la lista copiata, e la mando al client
					String res = gson.toJson(al);
					return res;
				}else {
					throw new NullPointerException();
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente sottoforma di stringa (che andrà deserializzata e analizzato il primo elemento)
			Gson gson = new Gson();
			ArrayList<String> al = new ArrayList<String>();
			String s = "";
			for(int i=0; i<51; i++) {
				s += String.valueOf(Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE);
			}
			al.add(s);
			String res = gson.toJson(al);
			return res;
		}
	}
	
	public static int serverCancelProject(String project_name) {
		//Metodo per cancellare un progetto da WORTH. La cancellazione di un progetto richiede una serie di operazioni importanti:
		//-capire se nel progetto tutte le card sono nello stato "Done"
		//-mandare, tramite RMI callback, una notifica a tutti i partecipanti online di questo progetto, così che fermino il thread
		//che si occupa della sua chat
		//-recuperare l'indirizzo di multicast della chat di questo progetto.
		
		//Inoltre, siccome questa operazione va a modificare pesantemente lo stato del server (viene cancellato un intero progetto),
		//devo avere una lock a grana grossa per poter essere sicuro di mantenere uno stato consistente (altrimenti, potrebbe ad esempio verificarsi
		//una race condition in cui l'utente "a" vuole creare il progetto "prog", e per farlo controlla, con la lock a grana grossa ALL_PROJECTS_MONITOR
		//(@see ServerInfo), se esiste già un progetto con quel nome. Il controllo risulta negativo. Nello stesso istante, l'utente "b", che partecipa
		//al progetto "prog", lo cancella, lavorando in mutua esclusione solo sulla lock relativa al progetto "prog". In questo caso l'utente "a"
		//sarebbe riuscito a creare il progetto "prog" se avesse ritardato di qualche istante. Mettendo una lock a grana grossa ci assicuriamo che
		//la richiesta di creazione e quella di cancellazione del progetto vengano effettuate in sequenza).
		
		//allo stesso tempo però, siccome l'operazione è anche locale al progetto project_name, bisogna acquisire la lock di questo progetto,
		//altrimenti rischiamo grosso: se un membro del progetto crea una card mentre un altro membro dello stesso progetto lo sta
		//cancellando, nel migliore dei casi il progetto viene cancellato con una card nello stato "To do", nel peggiore il primo utente
		//chiede al server di scrivere un file (quello della nuova card) in una directory che non esiste! (quella del progetto appena rimosso).
		
		//purtroppo non è finita: ogni utente ha, nell'oggetto lato server che lo rappresenta (@see UtenteRegistratoConPassword) una lista
		//contenente i progetti di cui fa parte. Se modifico in due punti separati (e quindi in modo NON atomico) la struttura dati contenente
		//i membri del progetto nel WorthProjectManager del progetto da cancellare, e la struttura dati contenente i progetti di cui un utente
		//fa parte nel suo oggetto rappresentativo UtenteRegistratoConPassword, rischio di incontrare altre race conditions: l'utente "a"
		//potrebbe cancellare il progetto "prog" mentre l'utente "b", che ne fa parte, accede a WORTH. Il risultato che è che "prog" non
		//esiste più, ma per "b" quel progetto esiste ancora!
		
		//Per questi motivi, abbiamo bisogno di acquisire ben tre lock: quella globale dei progetti, quella locale a questo e quella relativa
		//agli utenti registrati a WORTH. Sì, è decisamente un'operazione impegnativa
		try {
			synchronized(ServerInfo.ALL_PROJECTS_MONITOR) {
				synchronized(ServerInfo.progettiWorth.get(project_name)) {
					synchronized(ServerInfo.ALL_USERS_MONITOR) {
						if(ServerInfo.progettiWorth.containsKey(project_name)) {
							//prima cosa da fare: deserializziamo il @see WorthProjectManager di questo progetto e vediamo
							//se tutte le card sono nella lista "Done", altrimenti non ha senso andare avanti (che è la
							//stessa cosa che dire, controlliamo che le altre tre liste siano vuote)
							
							WorthProjectManager wpm = deserializeWorthProjectManager(project_name);
							
							if(!wpm.toDo.isEmpty() || !wpm.inProgress.isEmpty() || !wpm.toBeRevised.isEmpty()) {
								return Constants.RES_CANCELPROJECT_ERROR;
							}
							
							//se invece tutte le card (se ce ne sono, potrebbero anche non essercene se ad esempio l'utente ha creato il progetto
							//per sbaglio e vuole subito cancellarlo) sono nella lista "Done", posso cancellare il progetto. Questo implica tre cose:
							//1) Gli utenti online che fanno parte di questo progetto vanno avvertiti del fatto che questo progetto è stato cancellato.
							//uso il meccanismo di RMI callback per farlo
							//2) Devo recuperare l'indirizzo di multicast relativo a questo progetto
							//3) Va cancellata ogni traccia di questo progetto dal server: per farlo, basta rimuovere la directory che la rappresenta
							//e tutti i suoi contenuti
							
							//1) Cominciamo col prendere la lista degli utenti di questo progetto
							HashSet<String> all_members = wpm.listMembersName;
							
							//Cancello, dalla struttura dati che mantiene i progetti di cui l'utente fa parte, il progetto project_name, per ogni
							//utente che ne fa parte, e salvo i cambiamenti effettuati sul file relativo a quell'utente
							Iterator<String> iterator = all_members.iterator();
							while(iterator.hasNext()) {
								String current_member = iterator.next();
								UtenteRegistratoConPassword urcp = ServerInfo.getUtenteRegistratoConPassword(current_member);
								urcp.removeProjectFromListOfUsersProjects(project_name);
								ServerInfo.updateExistingUserInformations(urcp);
							}
							
							//A questo punto, tra gli utenti che partecipavano al progetto, vanno avvisati quelli online del fatto che possono ammazzare
							//il thread relativo alla chat di quel progetto, dato che il progetto non esiste più. Potevo scrivere le righe di codice
							//relative a questa operazione anche nel while(iterator.hasNext()) qua sopra, ma ho preferito tenerli separati per leggibilità
							Iterator<String> iterator_2 = all_members.iterator();
							while(iterator_2.hasNext()) {
								String current_member = iterator_2.next();
								if(ServerInfo.getUserState(current_member) == Constants.OFFLINE) {
									iterator_2.remove();
								}
							}
							
							//Vado dunque ad aggiornare gli utenti online che partecivano a questo progetto col meccanismo di RMI callback
							ArrayList<String> online_members = new ArrayList<String>();
							online_members.addAll(all_members);
							try {
								ci.updateChatCancelProject(online_members, project_name);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
							
							//2) riciclo l'indirizzo di multicast di questo progetto
							ServerInfo.progettiWorth.get(project_name).freeMulticastAddressBecauseProjectIsBeingCancelled();
							
							//3) cancello questo progetto dalla struttura dati del server che ne tiene traccia E cancello tutti i file inerenti a questo progetto,
							//directory compresa
							ServerInfo.progettiWorth.remove(project_name);
							
							File dir_to_cancel = new File(ServerInfo.projectsDir + File.separator + project_name);
							for (File file : dir_to_cancel.listFiles()) {
							    file.delete();
							}
							dir_to_cancel.delete();
							
							return Constants.RES_CANCELPROJECT_SUCCESS;
						}else {
							throw new NullPointerException();
						}
					}
				}
			}
		}catch(NullPointerException e) {
			//se il progetto non esiste, restituisco un codice di errore all'utente
			return Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE;
		}
		
	}
	
	//metodo per deserializzare un @see WorthProjectManager
	private static WorthProjectManager deserializeWorthProjectManager(String project_name) {
		//uso Gson per deserializzare
		Gson gson = new Gson();
		WorthProjectManager wpm = null;
		
		//leggo, dal file, prima la lunghezza dell'oggetto serializzato, poi l'oggetto serializzato
		File file = new File(ServerInfo.projectsDir + File.separator + project_name + File.separator + project_name + ".json");
		try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
			int length = dis.readInt();
			
			byte[] objToDeserialize = new byte[length];
			dis.read(objToDeserialize);
			String obj = new String(objToDeserialize);
			wpm = gson.fromJson(obj, WorthProjectManager.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wpm;
	}
	
	//metodo per serializzare un WorthProjectManager, praticamente lo speculare di quello di sopra
	private static void serializeWorthProjectManager(WorthProjectManager wpm) {
		//serializzo usando Gson
		Gson gson = new Gson();
		String s = gson.toJson(wpm);
		int len = s.getBytes().length;
		
		//scrivo, nel file, prima la lunghezza dell'oggetto serializzato, poi l'oggetto serializzato stesso
		File file = new File(ServerInfo.projectsDir + File.separator + wpm.projectName + File.separator + wpm.projectName + ".json");
		try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
			dos.writeInt(len);
			dos.writeBytes(s);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//di seguito due metodi che serializzano e deserializzano una @see WorthCard di un progetto, del tutto analoghi a quelli poco sopra sui WorthProjectManager
	private static WorthCard deserializeWorthCard(String project_name, String card_name) {
		Gson gson = new Gson();
		WorthCard wc = null;
		File file = new File(ServerInfo.projectsDir + File.separator + project_name + File.separator + card_name + ".json");
		try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
			int length = dis.readInt();
			
			byte[] objToDeserialize = new byte[length];
			dis.read(objToDeserialize);
			String obj = new String(objToDeserialize);
			wc = gson.fromJson(obj, WorthCard.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wc;
	}
	
	private static void serializeWorthCard(WorthCard wc, String project_name) {
		Gson gson = new Gson();
		String s = gson.toJson(wc);
		int len = s.getBytes().length;
		File file = new File(ServerInfo.projectsDir + File.separator + project_name + File.separator + wc.name + ".json");
		try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
			dos.writeInt(len);
			dos.writeBytes(s);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
