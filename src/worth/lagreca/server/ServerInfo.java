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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

import worth.lagreca.constants.Constants;
import worth.lagreca.projectsandcards.WorthVolatileProjectInformations;
import worth.lagreca.users.UtenteRegistrato;
import worth.lagreca.users.UtenteRegistratoConPassword;

public class ServerInfo {
	
	private static boolean DEBUG = true;
	
	//classe utilizzata dal server per mantenere alcune informazioni generali,
	
	//HashMap di utenti registrati con password che il server mantiene sempre in memoria quando viene avviato.
	//uso una HashMap in quanto il nickname di un utente è univoco in tutto WORTH, e voglio poter accedere subito
	//al nome di un utente per controllare se esiste, trovare le informazioni ad esso associato, etc..
	//per poter persistere lo stato di questi oggetti, il server li serializza quando vengono creati/modificati
	//e li legge quando viene avviato.
	private static HashMap<String, UtenteRegistratoConPassword> utentiRegistratiWorth = new HashMap<String, UtenteRegistratoConPassword>();
	
	//hashMap di tutti i progetti presenti sul server WORTH. Poiché un progetto in WORTH è definito in modo
	//univoco dal suo nome, mi basta tenere traccia dei nomi dei progetti in una hashmap, così da associare
	//univocamente a ciascuno di loro un @see WorthVolatileProjectInformations (indirizzo di multicast e utenti
	//connessi a WORTH che partecipano a questo progetto)
	public static HashMap<String, WorthVolatileProjectInformations> progettiWorth = new HashMap<String, WorthVolatileProjectInformations>();
	
	//siccome in questa classe vado a mantenere informazioni importanti che verranno accedute in maniera concorrente,
	//ho bisogno di dichiarare degli oggetti che fungeranno da monitor (verranno usati nei blocchi synchronized)
	public static Object ALL_USERS_MONITOR = new Object();
	
	//un altro monitor di cui ho bisogno è uno che mi assicuri di accedere in mutua esclusione alla cartella contenente
	//tutti i progetti di WORTH. Desidero questo livello di mutua esclusività in operazioni come quella di creazione
	//di un nuovo progetto, dove voglio avere la certezza di creare questo progetto una e una sola volta
	public static Object ALL_PROJECTS_MONITOR = new Object();
	
	//monitor che uso per effettuare in mutua esclusione le operazioni che vanno a modificare lo stato degli indirizzi
	//multicast (assegnamento, rimozione, etc...), altrimenti correremmo il rischio di assegnare lo stesso indirizzo
	//a due chat diverse.
	public static Object ALL_MULTICAST_ADDRESSES_MONITOR = new Object();
	
	
	//stringhe che mantengono il nome delle directory utili al server per accedere alle risorse persistenti, ovvero
	//gli utenti con i loro attributi e i progetti
	public static String usersDir;
	public static String projectsDir;
	
	//lista di indirizzi multicast "riciclati" da progetti cancellati
	private static ArrayList<String> recycledMulticastAddressesList = new ArrayList<String>();
	
	//valore iniziale e finale degli indirizzi di multicast utilizzabili per la chat
	private static String initialMulticastAddress = "224.0.1.0";
	private static String finalMulticastAddress = "239.255.255.255";
	private static String currentMulticastAddress = initialMulticastAddress;
	
	public static void InitializeServerInfo(String usersDirr, String projectsDirr) {
		usersDir=usersDirr;
		projectsDir=projectsDirr;
		
		//metodo per inizializzare la struttura dati contenente gli utenti registrati
		initializeUsers();
		
		//metodo per inizializzare la struttura dati contenente i nomi dei progetti presenti su WORTH
		initializeProjects();
	}
	
	private static void initializeUsers() {
		//in questo metodo vado ad inizializzare la struttura dati degli utenti registrati a WORTH
		//quando il server viene acceso. In pratica accedo alla cartella degli utenti, guardo i file
		//uno ad uno, e aggiungo un nuovo elemento alla hashMap, con:
		//chiave = nome del file (ovvero dell'utente)
		//valore = oggetto UtenteRegistratoConPassword contenente tutte le informazioni su quell'utente
		File dir = new File(usersDir);
		File[] userFiles = dir.listFiles();
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("InitializeUsers lanciato");
		
		//nel caso in cui il server WORTH sia stato lanciato per la prima volta, non ci saranno file
		//da leggere, e questo if verrà saltato
		if(userFiles.length != 0) {
			Gson gson = new Gson();
			for(File currentUserFile : userFiles) {
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("deserializzo un utente: " + currentUserFile.getName());
				
				//prendo il nome del file, che però sarà in estensione .json
				String name = currentUserFile.getName();
				
				//rimuovo l'estensione dal nome
				String key = name.substring(0, name.length() - ".json".length());
				
				//per deserializzare il file, devo innanzitutto leggerne la lunghezza. Per farlo, devo
				//leggere i primi 4 byte del file, che rappresentano un intero che indica la lunghezza,
				//in byte, del file. Uso un DataInputStream per leggere queste informazioni dal file
				try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(currentUserFile)))){
					
					//innanzitutto leggo la lunghezza del file, andando a leggere i primi 4 byte
					int length = dis.readInt();
					
					//poi mi dichiaro un array di byte che sarà lungo esattamente length, dove vado ad inserire l'oggetto
					//da deserializzare
					byte[] objToDeserialize = new byte[length];
					dis.read(objToDeserialize);
					String obj = new String(objToDeserialize);
					UtenteRegistratoConPassword user = gson.fromJson(obj, UtenteRegistratoConPassword.class);
					
					//quando il server viene avviato, tutti gli utenti sono offline. Con questa riga di codice rendo vera questa condizione
					user.getUtenteRegistrato().setUtenteRegistratoState(false);
					
					//ora posso aggiungere un elemento alla HashMap
					utentiRegistratiWorth.put(key, user);
					
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
					
			}
		}
		
	}
	
	private static void initializeProjects(){
		//in questo metodo vado ad inizializzare la struttura dati contenente i nomi di tutti i progetti esistenti su WORTH.
		//per farlo, scorro i nomi di tutte le directory presenti nella cartella delle directory, e aggiungo questi nomi
		//al set
		File dir = new File(projectsDir);
		File[] projectDirectories = dir.listFiles();
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("InitializeProjects lanciato");
		
		//di nuovo, se è la prima volta che lancio WORTH, non ci saranno cartelle da controllare, quindi questo
		//if viene saltato
		if(projectDirectories.length != 0) {
			for(File currentProjectDirectory : projectDirectories) {
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Leggo il nome di un progetto: " + currentProjectDirectory.getName());
				
				//prendo il nome della directory (che coincide col nome del progetto)
				String name = currentProjectDirectory.getName();
				
				//pensavo di associare immediatamente un indirizzo di multicast ad ogni progetto, ma non lo farò. Inizialmente,
				//a nessun progetto è associato un indirizzo. Solo quando quel progetto è in utilizzo (ovvero c'è un utente loggato
				//che partecipa a quel progetto) glielo assegno. Parimenti, se nessun utente usa un certo progetto, lo libero
				//del suo indirizzo di multicast
				
				//aggiungo il progetto alla struttura dati
				progettiWorth.put(name, new WorthVolatileProjectInformations(null));
			}
		}	
	}
	
	
	
	//metodo che restituisce il minimo indirizzo di multicast disponibile, usato quando si va a istanziare un nuovo progetto
	//(sia perché si sta lanciando il server che deve leggere i progetti già esistenti, sia perché un utente sta creando un
	//nuovo progetto)
	public static String getMulticastAddressToAssociateToAProject() {
		
		synchronized(ALL_MULTICAST_ADDRESSES_MONITOR) {
		
			//prima di tutto, controllo se ho davvero bisogno di creare un nuovo indirizzo di multicast, o se posso riciclarne uno
			String multicast_address = null;
			if((multicast_address = pollRecycledMulticastAddress()) != null) {
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il server ha riciclato un indirizzo di multicast, ovvero: " +  multicast_address);
				return multicast_address;
			}else {
				//se devo creare un nuovo indirizzo per un progetto, devo prima sincerarmi che ce ne sia uno libero
				if(currentMulticastAddress.equals(finalMulticastAddress)) {
					return null;		//quindi gli indirizzi sono finiti (Come è possibile!?)
				}
				
				//altrimenti, vado a "creare" il successivo
				String[] valuesOfMAString = currentMulticastAddress.split("\\.", 4);
				int[] valuesOfMAInt = new int[4];
				for(int i = 0; i < 4; i++) {
					valuesOfMAInt[i] = Integer.parseInt(valuesOfMAString[i]);
				}
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Indirizzo di multicast corrente come interi: " + valuesOfMAInt[0]+valuesOfMAInt[1]+valuesOfMAInt[2]+valuesOfMAInt[3]);
				
				if(valuesOfMAInt[3] < 255) {
					valuesOfMAInt[3]++;
				}else {
					valuesOfMAInt[3] = 0;
					if(valuesOfMAInt[2] < 255) {
						valuesOfMAInt[2]++;
					}else {
						valuesOfMAInt[2] = 0;
						if(valuesOfMAInt[1] < 255) {
							valuesOfMAInt[1]++;
						}else {
							valuesOfMAInt[1] = 0;
							if(valuesOfMAInt[0] < 239) {
								valuesOfMAInt[0]++;
							}
						}
					}
				}
				
				//prendo il primo indirizzo libero
				multicast_address = currentMulticastAddress;
				
				//poi però lo devo aggiornare
				currentMulticastAddress = valuesOfMAInt[0]+"."+valuesOfMAInt[1]+"."+valuesOfMAInt[2]+"."+valuesOfMAInt[3];
				
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("il server ha creato un indirizzo di multicast da zero, ovvero: " + multicast_address);
				
				return multicast_address;
			}
		}
		
	}
	
	//metodo con cui vado a "riciclare" indirizzi di multicast che si sono liberati. In pratica, con questo metodo, nel momento
	//in cui ho bisogno di un nuovo indirizzo di multicast da associare ad un progetto, vado a vedere se, al posto di crearne
	//uno nuovo, posso recuperarne uno che è stato usato da un progetto che ora non lo sta più usando.
	private static String pollRecycledMulticastAddress() {
		
		//se la lista di indirizzi da riciclare non è vuota, prendo il primo elemento
		if(recycledMulticastAddressesList.size() > 0) {
			return recycledMulticastAddressesList.remove(0);
		}else {
			return null;
		}
	}
	
	//metodo per aggiungere un indirizzo di multicast alla lista di indirizzi "riciclati"
	public static void putRecycledMulticastAddress(String multicast_address) {
		synchronized(ALL_MULTICAST_ADDRESSES_MONITOR) {
			//mi interessa aggiungere solo i veri indirizzi di multicast
			if(multicast_address != null) {
				recycledMulticastAddressesList.add(multicast_address);
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Il server ha aggiunto alla coda di indirizzi di multicast da riciclare l'indirizzo " + multicast_address);
			}
		}
	}
	
	
	
	//metodo usato per reperire le informazioni PUBBLICHE di tutti gli utenti registrati a WORTH. Viene usato
	//ad esempio dal metodo di registrazione alle callback della classe @see CallbackImplementation
	public static HashMap<String, UtenteRegistrato> getAllUtentiRegistrati(){
		
		//dichiaro la hashmap in cui metterò gli utenti registrati
		HashMap<String, UtenteRegistrato> result = new HashMap<String, UtenteRegistrato>();
		
		synchronized(ALL_USERS_MONITOR) {
			//itero gli utenti presenti nel sistema, andando a prendere, per ciascuno di essi, solo le informazioni
			//pubbliche, ed aggiungendole al risultato
			Iterator<Entry<String, UtenteRegistratoConPassword>> iterator = utentiRegistratiWorth.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<String, UtenteRegistratoConPassword> entry = iterator.next();
				UtenteRegistratoConPassword urcp = entry.getValue();
				UtenteRegistrato ur = urcp.getUtenteRegistrato();
				
				result.put(ur.getUtenteRegistratoName(), ur);
			}
		}
		
		return result;
		
		
	}
	
	public static boolean doesUserExist(String username) {
		//metodo che permette di accedere in mutua esclusione alla struttura dati contenente gli utenti, e di controllare
		//se un nome utente esiste oppure no
		
		synchronized(ALL_USERS_MONITOR) {
			if(utentiRegistratiWorth.containsKey(username)) {
				return true;
			}else {
				return false;
			}
		}
		
	}
	
	public static boolean addUser(String username, String password) {
		//metodo usato per aggiungere un utente al server WORTH. Verrà chiamato in fase di registrazione dal client
		//mediante RMI (@see RegistrationImplementation), e restituirà false se l'utente col nome specificato
		//esiste già, quindi non aggiungendolo, true se invece il nuovo utente è stato creato correttamente.
		//il tutto è sincronizzato su un monitor, in quanto voglio, ATOMICAMENTE, controllare se un certo utente
		//esiste già e, nel caso non esistesse, aggiungerlo
		
		synchronized(ALL_USERS_MONITOR) {
			if(utentiRegistratiWorth.containsKey(username)) {
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("L'utente " + username + " esiste già, non posso ricrearlo");
				return false;
			}else {
				//vado a creare il nuovo utente. La creazione dell'utente si divide in due parti: prima creo l'oggetto
				//UtenteRegistratoConPassword e lo serializzo, poi aggiungo una nuova entry alla struttura dati che
				//mantiene gli utenti registrati
				UtenteRegistratoConPassword u = new UtenteRegistratoConPassword(username, password);
				Gson gson = new Gson();
				String s = gson.toJson(u);
				int len = s.getBytes().length;
				
				//scrivo la lunghezza dell'oggetto serializzato, poi l'oggetto serializzato
				File newUserFile = new File(usersDir + File.separator + username + ".json");
				try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newUserFile)))){
					dos.writeInt(len);
					dos.writeBytes(s);
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//infine, aggiungo il nuovo utente alla struttura dati
				utentiRegistratiWorth.put(username, u);
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Utente " + username + " creato correttamente");
				
				
				return true;
			}
		}
	}
	
	//metodo usato per cambiare lo stato (online, offline) di un utente registrato a WORTH. Verrà chiamato
	//in fase di login e logout dai metodi della classe @see CallbackImplementation
	public static void setUserState(String name, boolean newState) {
		synchronized(ALL_USERS_MONITOR) {
			UtenteRegistratoConPassword urcp = utentiRegistratiWorth.get(name);
			urcp.getUtenteRegistrato().setUtenteRegistratoState(newState);
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Lo stato dell'utente " + name + " è ora a " + urcp.getUtenteRegistrato().getUtenteRegistratoStateAsString());
			utentiRegistratiWorth.put(name, urcp);
		}
	}
	
	//metodo getter per sapere lo stato corrente dell'utente
	public static boolean getUserState(String name) {
		synchronized(ALL_USERS_MONITOR) {
			UtenteRegistratoConPassword urcp = utentiRegistratiWorth.get(name);
			return urcp.getUtenteRegistrato().getUtenteRegistratoState();
			
		}
	}
	
	//metodo getter per ottenere le informazioni PUBBLICHE di un utente registrato
	public static UtenteRegistrato getUtenteRegistrato(String name) {
		synchronized(ALL_USERS_MONITOR) {
			return utentiRegistratiWorth.get(name).getUtenteRegistrato();
		}
	}
	
	//metodo per verificare se la password inserita da un utente esistente
	//è corretta
	public static boolean userAuthentication(String username, String password) {
		UtenteRegistratoConPassword urcp = utentiRegistratiWorth.get(username);
		if(urcp.confrontPassword(password) == true) {
			return true;
		}else {
			return false;
		}
	}
	
	//metodo per ottenere tutte le informazioni di un utente
	public static UtenteRegistratoConPassword getUtenteRegistratoConPassword(String username) {
		return utentiRegistratiWorth.get(username);
	}
	
	//metodo per aggiornare le informazioni di un utente che esiste già
	public static void updateExistingUserInformations(UtenteRegistratoConPassword urcp) {
		
		synchronized(ALL_USERS_MONITOR) {
			Gson gson = new Gson();
			String s = gson.toJson(urcp);
			int len = s.getBytes().length;
			
			//scrivo la lunghezza dell'oggetto serializzato, poi l'oggetto serializzato (vado a sostituire quello che già esisteva)
			File updatedUserFile = new File(usersDir + File.separator + urcp.getUtenteRegistrato().getUtenteRegistratoName() + ".json");
			try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(updatedUserFile)))){
				dos.writeInt(len);
				dos.writeBytes(s);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//infine, aggiorno la struttura dati degli utenti, sovrascrivendo il vecchio valore dell'utente con quello nuovo
			utentiRegistratiWorth.put(urcp.getUtenteRegistrato().getUtenteRegistratoName(), urcp);
			
		}
	}
	
}
