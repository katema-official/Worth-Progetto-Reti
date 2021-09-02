package worth.lagreca.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import worth.lagreca.client.rmi.UsersInformationsInterface;

public interface CallbackInterface extends Remote{
	//interfaccia contenente i metodi astratti usati per il meccanismo delle callback RMI.
	//con questa interfaccia, i client potranno registrarsi alle callback e cancellarsi.
	//nell'implementazione di questa interfaccia (@see CallbackImplementation) vi è anche
	//il metodo che fa le callback, aggiornando i vari client sullo stato di un utente che
	//si è appena registrato/ha fatto il login/ha fatto il logout
	
	//metodo per registrarsi alle callback e ottenere la lista iniziale degli utenti di worth, ovvero
	//la lista degli utenti che sono registrati a Worth nel momento in cui questo metodo viene invocato.
	public String registerForCallback(UsersInformationsInterface client, String name) throws RemoteException;
	
	//metodo per cancellarsi dalle callback
	public void unregisterForCallback(UsersInformationsInterface client, String name) throws RemoteException;

}

