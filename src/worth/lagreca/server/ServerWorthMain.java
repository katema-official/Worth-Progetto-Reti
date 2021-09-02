package worth.lagreca.server;


import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import worth.lagreca.constants.Constants;
import worth.lagreca.server.rmi.*;

public class ServerWorthMain {

	public static void main(String[] args) {
		
		boolean DEBUG = true;
		
		//creo la directory principale, ovvero quella in cui verranno conservati tutti i dati persistenti
		//del sistema, se questa non esiste. Questa cartella verrà creata solo e solamente
		//alla prima attivazione del server WORTH, in quanto, dopo averla creata, verrà usata per mantenere
		//le informazioni persistenti del sistema (sempre che non si vada ad eliminare manualmente
		//questa cartella, in tal caso verrà ricreata quando il server verrà rieseguito)
		String mainDirectory = "WorthServerDirectory";
		File rootDir = new File(mainDirectory);
		if(!rootDir.exists()) {
			boolean created = rootDir.mkdir();
			if(created && Constants.GLOBALDEBUG && DEBUG) System.out.println("Directory root creata");
		}
		
		//creo poi una cartella contenente gli oggetti UtenteRegistratoConPassword. In particolare avrò,
		//per ogni utente, un file ad esso relativo contenente:
		//4 byte che indicano la lunghezza, in byte, dell'oggetto utente serializzato;
		//i byte dell'oggetto serializzato;
		//tutte le volte che il server WORTH viene lanciato, si vanno a deserializzare questi file e a metterli
		//in una struttura dati presente in @see ServerInfo, ovvero utentiRegistratiWorth
		String usersDirectory = mainDirectory + File.separator + "UsersDirectory";
		File usersDir = new File(usersDirectory);
		if(!usersDir.exists()) {
			boolean created = usersDir.mkdir();
			if(created && Constants.GLOBALDEBUG && DEBUG) System.out.println("Directory utenti creata");
		}
		
		//allo stesso modo, creo una cartella radice per i progetti, in cui ci saranno altre cartelle,
		//una per ogni progetto (Con dentro i file delle card di quel progetto). Quando il server WORTH viene
		//lanciato, va a leggere i nomi di tutti i progetti e a metterli in una struttura dati presente in @see ServerInfo,
		//ovvero progettiWorth.
		String projectsDirectory = mainDirectory + File.separator + "ProjectsDirectory";
		File projectsDir = new File(projectsDirectory);
		if(!projectsDir.exists()) {
			boolean created = projectsDir.mkdir();
			if(created && Constants.GLOBALDEBUG && DEBUG) System.out.println("Directory progetti creata");
		}
		
		//preparate le cartelle di worth, vado ad inizializzare le strutture dati del server, presenti nella classe ServerInfo
		ServerInfo.InitializeServerInfo(usersDirectory, projectsDirectory);
		
		//siccome qui siamo ancora in fase di inizializzazione del server, tutte queste operazioni avverranno in maniera
		//sequenziale, pertanto non mi preoccupo della sincronizzazione con lock.
		
		
		
		
		
		//a questo punto, devo dar modo agli utenti di registrarsi e loggarsi. Comincio dunque con l'esportare
		//l'oggetto remoto @see RegistrationInterface per permettere agli utenti di registrarsi
		
		//creo l'oggetto da esportare
		RegistrationImplementation registerObject = new RegistrationImplementation();
		
		//esporto l'oggetto
		RegistrationInterface stubRegistration = null;
		try {
			stubRegistration = (RegistrationInterface) UnicastRemoteObject.exportObject(registerObject, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//creo la Registry sulla porta indicata nella classe delle costanti (@see Constants)
		Registry reg = null;
		try {
			LocateRegistry.createRegistry(Constants.PORT_REGISTRY);
			reg = LocateRegistry.getRegistry();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//pubblico lo stub nel registry
		try {
			reg.rebind("REGISTRATION", stubRegistration);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		//adesso devo esportare l'oggetto remoto che permetterà ai client di registrarsi e cancellarsi
		//per il meccanismo delle callback
		
		//creo l'oggetto da esportare
		CallbackImplementation callbackObject = new CallbackImplementation();
		
		//esporto l'oggetto
		CallbackInterface stubCallback = null;
		try {
			stubCallback = (CallbackInterface) UnicastRemoteObject.exportObject(callbackObject, 0);
		}catch(RemoteException e) {
			e.printStackTrace();
		}
		
		//pubblico lo stub nel registry che ho già creato in precedenza
		try {
			reg.rebind("CALLBACK", stubCallback);
		}catch(RemoteException e) {
			e.printStackTrace();
		}
		
		//passo all'oggetto @see ServerTcpOperations un riferimento all'oggetto delle callback
		//(lo faccio anche con @see ServerWorthMultiplexerNio poco più sotto)
		ServerTcpOperations.initializeServerTcpOperations(callbackObject);
		
		//faccio lo stesso con l'oggetto @see RegistrationImplementation
		registerObject.initializeRegistrationImplementation(callbackObject);
		
		//creo il server vero e proprio, cioè quello che deve gestire le connessioni,
		//e lo lancio
		ServerWorthMultiplexerNio swmn = new ServerWorthMultiplexerNio();
		swmn.initializeServerWorthMultiplexerNio(callbackObject);
		Thread t1 = new Thread(swmn);
		t1.start();
		
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Fine main");
		

	}

}
