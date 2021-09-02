package worth.lagreca.server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

import worth.lagreca.client.rmi.UsersInformationsInterface;
import worth.lagreca.constants.Constants;
import worth.lagreca.server.ServerInfo;
import worth.lagreca.users.UtenteRegistrato;
import worth.lagreca.users.UtenteRegistratoConPassword;

public class CallbackImplementation extends RemoteObject implements CallbackInterface{
	
	private boolean DEBUG = true;
	
	private static final long serialVersionUID = -7392040451719470075L;

	//struttura dati usata per memorizzare i client registrati alle callback. 
	private HashMap<String, UsersInformationsInterface> registeredClientsForCallback;
	
	public CallbackImplementation() {
		registeredClientsForCallback = new HashMap<String, UsersInformationsInterface>();
	}
	
	//con questo metodo, non solo si registra un client alle notifiche callback, ma si segna anche (nella struttura dati 
	//server e ai client) che è online.
	//per questo viene chiamato updateAllUsers, perché bisogna avvertire tutti i client che un nuovo utente
	//è ora online su WORTH
	//viene inoltre restituita una hashmap serializzata contenente gli utenti attualmente registrati a Worth, con associato
	//il loro stato (online/offline)
	public synchronized String registerForCallback(UsersInformationsInterface client, String name) throws RemoteException {
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Setto lo stato dell'utente " + name + " a online");
		ServerInfo.setUserState(name, Constants.ONLINE);
		updateAllUsers(name);
		if(!registeredClientsForCallback.containsKey(name)) {
			registeredClientsForCallback.put(name, client);
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Ho aggiunto all'elenco di client registrati alle callback un nuovo client");
		}else {
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Non è stato possibile aggiungere il nuovo client all'elenco di registrati alle callback");
		}
		
		//siccome l'utente ha fatto il login e si è registrato al meccanismo di callback, devo aumentare di uno il numero
		//di utenti che sta usando i suoi progetti (proprio perché almeno lui adesso li sta usando). Per farlo, devo
		//vedere di che progetti fa parte e dire al client di lanciare un thread per ogni progetto di cui fa parte
		UtenteRegistratoConPassword this_user = ServerInfo.getUtenteRegistratoConPassword(name);
		HashSet<String> his_projects = this_user.getProjectsOfUser();
		Iterator<String> iterator = his_projects.iterator();
		
		//itero i progetti dell'utente, in quanto per ognuno di essi devo segnare che un nuovo utente li sta usando e ottenere il
		//loro indirizzo di multicast, così da trasmetterlo al client
		while(iterator.hasNext()) {
			String projectName = iterator.next();
			
			//così mi segno che un utente in più sta usando questo progetto
			ServerInfo.progettiWorth.get(projectName).incrementNumberOfUsersUsingThisProject();
			
			String ma = ServerInfo.progettiWorth.get(projectName).getMulticastAddressOfThisProject();
			
			ArrayList<String> loggedUser = new ArrayList<String>();
			loggedUser.add(name);
			
			//avverto l'utente che deve gestire la chat di questo progetto
			updateSomeUsersChat(loggedUser, projectName, ma, Constants.CHAT_ADD);
		}
		
		return getInitialUsers();
		
	}
	
	//con questo metodo, non solo si cancella un client dalle notifiche callback, ma si segna anche (nella struttura dati 
	//server e ai client) che è offline.
	//per questo alla fine viene chiamato updateAllUsers, perché bisogna avvertire tutti i client che un nuovo utente
	//è ora offline su WORTH
	public synchronized void unregisterForCallback(UsersInformationsInterface client, String name) throws RemoteException {
		
		//tuttavia, prima di cancellare l'utente dal meccanismo di RMI callback, devo dirgli di "ammazzare" tutti i thread
		//che gestiscono le chat dei suoi progetti
		UtenteRegistratoConPassword this_user = ServerInfo.getUtenteRegistratoConPassword(name);
		HashSet<String> his_projects = this_user.getProjectsOfUser();
		Iterator<String> iterator = his_projects.iterator();
		while(iterator.hasNext()) {
			String projectName = iterator.next();
			ServerInfo.progettiWorth.get(projectName).decrementNumberOfUsersUsingThisProject();
			ArrayList<String> loggingOutUser = new ArrayList<String>();
			loggingOutUser.add(name);
			updateSomeUsersChat(loggingOutUser, projectName, null, Constants.CHAT_REMOVE);
		}
		
		if(registeredClientsForCallback.remove(name, client)) {
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Rimosso correttamente un client dall'elenco di client registrati per le callback");
		}else {
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Non è stato possibile rimuove un client dall'elenco di client registrati per le callback");
		}
		ServerInfo.setUserState(name, Constants.OFFLINE);
		updateAllUsers(name);
		
	}
	
	//con questo metodo, vado ad accedere alla struttura dati contenente tutti gli utenti di
	//WORTH (@see ServerInfo), così da mandare una copia della struttura dati al client. 
	public synchronized String getInitialUsers() throws RemoteException {
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("server getInitialUsers ok");
		
		Gson gson = new Gson();
		String list = gson.toJson(ServerInfo.getAllUtentiRegistrati());
		
		//ciò che restituisco è una HashMap<String, UtenteRegistrato> in formato json, quindi il client
		//dovrà deserializzarla prima di poterla usare
		
		return list;
	}
	
	//metodo usato dal server per inviare notifiche asincrone ai vari client quando un utente
	//fa il login o il logout
	public synchronized void updateAllUsers(String name) throws RemoteException{
		UtenteRegistrato ur = ServerInfo.getUtenteRegistrato(name);
		Gson gson = new Gson();
		String utenteRegistratoJson = gson.toJson(ur);
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Faccio le callback in quanto un utente è ora online o offline");
		Iterator<Entry<String, UsersInformationsInterface>> iterator = registeredClientsForCallback.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, UsersInformationsInterface> pair = iterator.next();
			UsersInformationsInterface client = pair.getValue();
			client.UpdateUserState(utenteRegistratoJson);
		}
		
	}
	
	

	public synchronized void updateSomeUsersChat(ArrayList<String> names, String project_name, String indirizzo_di_multicast, boolean action) throws RemoteException{
		Iterator<String> iterator = names.iterator();
		while(iterator.hasNext()) {
			String name = iterator.next();
			UsersInformationsInterface client = registeredClientsForCallback.get(name);
			client.UpdateUserChat(project_name, indirizzo_di_multicast, action);
		}	
	}
	
	//metodi usati dal server per mandare una notifica mediante RMI riguardante la chat di un progetto (In ServerTcpOperations,
	//createProject, addMember, cancelProject).
	
	public void updateChatCreateProject(String creator, String project_name, String multicast_address) throws RemoteException{
		ArrayList<String> name = new ArrayList<String>();
		name.add(creator);
		updateSomeUsersChat(name, project_name, multicast_address, Constants.CHAT_ADD);
	}
	
	public void updateChatAddMember(String new_member, String project_name, String multicast_address) throws RemoteException{
		ArrayList<String> name = new ArrayList<String>();
		name.add(new_member);
		updateSomeUsersChat(name, project_name, multicast_address, Constants.CHAT_ADD);
	}
	
	public void updateChatCancelProject(ArrayList<String> members, String project_name) throws RemoteException{
		updateSomeUsersChat(members, project_name, null, Constants.CHAT_REMOVE);
	}
	
	//metodo usato per fare le callback quando un utente crasha. Chiaramente non devo dire nulla all'utente crashato, ma devo segnare, lato
	//server, l'assenza di questo utente (ovvero, c'è un utente in meno che utilizza i progetti di cui l'utente crashato fa parte), e dire
	//agli altri client che ora questo utente crashato è offline. In poche parole è come un unregisterForCallback, con la differenza che
	//non devo preoccuparmi dell'utente crashato
	public void updateCrash(String crashed_user) throws RemoteException{
		UsersInformationsInterface client = registeredClientsForCallback.get(crashed_user);
		UtenteRegistratoConPassword this_user = ServerInfo.getUtenteRegistratoConPassword(crashed_user);
		HashSet<String> his_projects = this_user.getProjectsOfUser();
		Iterator<String> iterator = his_projects.iterator();
		while(iterator.hasNext()) {
			String projectName = iterator.next();
			ServerInfo.progettiWorth.get(projectName).decrementNumberOfUsersUsingThisProject();
		}
		
		if(registeredClientsForCallback.remove(crashed_user, client)) {
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Rimosso correttamente un client CRASHATO dall'elenco di client registrati per le callback");
		}else {
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Non è stato possibile rimuove un client CRASHATO dall'elenco di client registrati per le callback");
		}
		ServerInfo.setUserState(crashed_user, Constants.OFFLINE);
		updateAllUsers(crashed_user);
		
	}
	
}
