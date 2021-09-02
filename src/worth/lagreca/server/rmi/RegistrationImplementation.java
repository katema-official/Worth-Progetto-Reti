package worth.lagreca.server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

import worth.lagreca.server.ServerInfo;

public class RegistrationImplementation extends RemoteObject implements RegistrationInterface{

	private CallbackImplementation ci = null;
	private static final long serialVersionUID = 2536293067496278730L;
	
	public void initializeRegistrationImplementation(CallbackImplementation ci) {
		this.ci = ci;
	}

	public int registerToWorth(String username, String password) throws RemoteException{
		//in questo metodo vado a registrare l'utente. Gli esiti di questo metodo possono essere cinque:
		//return 0: la password � vuota;
		//return 1: il nome utente esiste gi�
		//return 2: la registrazione � avvenuta con successo
		//return 3: il nome utente � vuoto
		//return 4: il nome utente � pi� lungo di 20 caratteri
		//sar� il client a capire l'esito della registrazione, interpretando correttamente il valore restituito
		
		synchronized(ServerInfo.ALL_USERS_MONITOR) {
		
			//controllo se la password � vuota
			if(password.equals("")) {
				return 0;	
			}
			//controllo se l'username � vuoto
			if(username.equals("")) {
				return 3;
			}
			if(username.length() > 20) {
				return 4;
			}
			
			//l'atomicit� dell'operazione � assicurata dal metodo nella guardia dell'if
			if(ServerInfo.addUser(username, password)) {
				//faccio le callback agli altri utenti per avvisarli che un nuovo utente si � registrato
				ci.updateAllUsers(username);
				
				return 2;	//utente registrato
			}else {
				return 1;	//il nome era gi� stato preso
			}
		}
		
	}
	
}
