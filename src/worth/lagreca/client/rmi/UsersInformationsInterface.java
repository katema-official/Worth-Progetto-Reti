package worth.lagreca.client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface UsersInformationsInterface extends Remote{
	//interfaccia creata dal client e usata dal server che prevede un metodo remoto per aggiornare lo stato
	//di un utente su worth. In pratica, invocando questo metodo, si va ad aggiornare la lista di utenti
	//locale al client, che contiene tutti gli utenti registrati a worth con il loro stato (online/offline).
	//verrà chiamato quando un nuovo utente ha effettuato il login o il logout
	public void UpdateUserState(String utenteRegistratoJson) throws RemoteException;
	
	
	
	//come sopra, ma con questo metodo va ad aggiornare la situazione di una chat. L'esito può essere la
	//creazione di un nuovo thread che deve occuparsi della gestione di una chat o l'eliminazione di un
	//thread, in quanto il progetto relativo è stato cancellato
	public void UpdateUserChat(String project_name, String ind_di_multicast, boolean action) throws RemoteException;
	
}
