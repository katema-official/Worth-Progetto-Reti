package worth.lagreca.client;

import java.util.HashMap;

import worth.lagreca.users.UtenteRegistrato;

public class ClientInfo {
	//classe che serve a tenere traccia di alcune informazioni lato client, una volta che l'utente ha
	//acceduto a WORTH
	
	//variabile che serve ad identificare l'utente che sta utilizzando questo client, una volta che si � loggato
	public static String nome_utente_loggato = null;
	
	//variabile che serve a tenere traccia di quale progetto l'utente ha aperto in questo momento
	public static String nome_progetto_selezionato = null;
	
	//HashMap degli utenti da aggiornare con RMI callback. Uso una HashMap per memorizzarli cos�
	//da permettere al meccanismo di RMI callback di aggiornare pi� rapidamente lo stato di un
	//utente gi� presente
	public static HashMap<String, UtenteRegistrato> worthUsersList = new HashMap<String, UtenteRegistrato>();
	
	
	
	//questo metodo va ad aggiornare lo stato di un utente presente nella struttura dati.
	//se l'utente non � presente, viene aggiunto
	public static void updateUsersList(UtenteRegistrato u) {
		String username = u.getUtenteRegistratoName();
		
		//se l'utente non � presente nella lista viene aggiunto un nuovo elemento, altrimenti
		//viene sovrascritto quello precedente.
		worthUsersList.put(username, u);
		
	}
	
	
}
