package worth.lagreca.chat;

import java.util.HashMap;

public class ClientChatInfo {

	//Classe analoga a @see ClientInfo, con la differenza che in questa mantengo SOLO le informazioni
	//sulla chat (per rendere più leggibile il codice insomma)
	
	//struttura dati che contiene gli oggetti runnable @see ClientChatManager. È importante mantenere
	//da qualche parte questi oggetti in quanto ognuno di essi conterrà i vari messaggi relativi ad una
	//chat
	public static HashMap<String, ClientChatManager> mapChatManagers = new HashMap<String, ClientChatManager>();
	
	//variabile che mi indica se l'utente, in questo momento, sta guardando la chat. Mi serve per capire se ho
	//bisogno di aggiornare l'utente in tempo reale o no.
	public static boolean chat_selected = false;
}
