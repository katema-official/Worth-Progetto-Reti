package worth.lagreca.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrationInterface extends Remote{
	
	//metodo usato per la registrazione. Devono essergli passati, dal client, nome utente e password.
	//A seconda del valore intero restituito, l'operazione avrà avuto esiti diversi (@see RegistrazioneImplementation)
	public int registerToWorth(String username, String password) throws RemoteException;
	
	
}
