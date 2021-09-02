package worth.lagreca.server;

import java.nio.ByteBuffer;

import worth.lagreca.constants.Constants;

public class ServerWorthObjectAttached {
	//attachment usato dal server per tenere traccia dei progressi fatti nella
	//comunicazione con un client. In questo attachment ci sono variabili per
	//conservare tutte le informazioni relative a ciò che
	//un client mi può spedire, così come variabili aggiuntive di supporto
	//al corretto funzionamento della comunicazione e variabili per conservare
	//le informazioni che il server può dover mandare al client
	
	//variabile utile ad identificare che tipo di operazione il client ha richiesto
	public int id_operation;
	
	//variabile che indica quanti sono i prossimi byte da leggere
	public int bytes_to_read;
	
	//buffer dove conservare i byte letti
	public ByteBuffer buffer;
	
	//variabile intera che mi dice a che punto sono della lettura dei dati
	//spediti dal client (@see Constants per i nomi delle costanti relative a 0, 1, 2 e 3)
	//0: devo identificare il tipo di operazione richiesta
	//1: leggo la length della prossima informazione
	//2: leggo la prossima informazione
	//3: ho letto tutto, setto infos_ready a true
	public int reading_state;
	
	//variabile che indica qual è il prossimo dato da leggere,
	//nell'ordine degli argomenti della funzione relativa
	public int counter_infos_read;
	
	//variabile per gestire il crash del client
	public int failed_to_read_counter;
	
	//variabili per conservare ciò che il client mi scrive.
	public String username;
	public String password;
	public String project_name;
	public String new_member;
	public String card_name;
	public String description;
	public String starting_list;
	public String destination_list;
	
	
	
	//variabile per conservare il valore di ritorno di tutti quei metodi che restituiscono un intero
	//come risultato, il quale indicherà l'esito dell'operazione
	public int resInt;
	
	//variabile per conservare il valore di ritorno di tutte quelle operazioni che hanno bisogno di restituire
	//una lista di oggetti, che io mando al client serializzati come stringhe json
	public String resString;
	
	//variabile usata in combo con quella sopra per capire se devo scrivere la lunghezza della stringa
	//o la stringa vera e propria
	//0 = devo scrivere la lunghezza della stringa
	//1 = devo scrivere la stringa
	public int stateResString;
	
	
	
	public ServerWorthObjectAttached() {
		id_operation = -1;
		bytes_to_read = 0;
		buffer = null;
		reading_state = Constants.ATT_STATE_OPERATION_IDENTIFICATION;
		counter_infos_read = 0;
		failed_to_read_counter = 0;
		
		username = null;
		password = null;
		project_name = null;
		new_member = null;
		card_name = null;
		description = null;
		starting_list = null;
		destination_list = null;
		
		resInt = -1;
		resString = null;
		stateResString = 0;
	}
	
	//metodo invocato una volta che si è portata a termine (sia con esito positivo che negativo)
	//un'operazione richiesta dal client, ovvero quando sono stati letti i dati da esso inviato
	//e gli si è mandata una risposta. Questo metodo assicura che le varie richieste siano completamente
	//separate, in quanto azzera il valore di tutte le variabili (ad eccezione dell'username) così
	//che il server non possa confondersi quando va ad interpretarle
	public void operationCompleted() {
		id_operation = -1;
		bytes_to_read = 0;
		buffer = null;
		reading_state = Constants.ATT_STATE_OPERATION_IDENTIFICATION;
		counter_infos_read = 0;
		failed_to_read_counter = 0;
		
		password = null;
		project_name = null;
		new_member = null;
		card_name = null;
		description = null;
		starting_list = null;
		destination_list = null;
		
		resInt = -1;
		resString = null;
		stateResString = 0;
	}
	
	//metodo invocato dalla classe @see ServerWorthMultiplexerNio per aggiungere il dato letto
	//nel campo appropriato. Questo campo viene deciso in base a due cose: il tipo di operazione
	//che il client sta effettuando (login, logout, listprojects...) e la posizione del dato
	//negli argomenti della funzione relativa (es: login(nickutente, password) associerà l'intero
	//0 a nickutente, 1 a password). Queste informazioni sono mantenute da quest'oggetto, quindi il
	//chiamante ha solo bisogno di specificare la stringa da aggiungere. siccome il metodo listProjects
	//specificato nel progetto WORTH non ha argomenti, non appare il suo caso in questa funzione, dato che
	//non ci sono dati da identificare (ci sarebbe il nome utente, che però è mantenuto da quest'oggetto
	//dopo che il client ha fatto il login)
	public void setNewReadData(String s) {
		switch(id_operation){
			case Constants.OP_LOGIN:
				switch(counter_infos_read) {
					case 0:
						username = s;
						counter_infos_read++;
					break;
					case 1:
						password = s;
					break;
				}
			break;
			case Constants.OP_LOGOUT:
				//in realtà non servirebbe ricevere il nickname in fase di logout, dato che
				//viene mantenuto in quest'oggetto fino al momento del logout, ma ormai preferisco
				//rispettare la specifica del progetto WORTH
				if(!username.equals(s)) {
					System.out.println("Problema con il logout");
				}
			break;
			case Constants.OP_CREATEPROJECT:
				project_name = s;
			break;
			case Constants.OP_ADDMEMBER:
				switch(counter_infos_read) {
					case 0:
						project_name = s;
						counter_infos_read++;
					break;
					case 1:
						new_member = s;
					break;
				}
			break;
			case Constants.OP_SHOWMEMBERS:
				project_name = s;
			break;
			case Constants.OP_SHOWCARDS:
				project_name = s;
			break;
			case Constants.OP_SHOWCARD:
				switch(counter_infos_read) {
					case 0:
						project_name = s;
						counter_infos_read++;
					break;
					case 1:
						card_name = s;
					break;
				}
			break;
			case Constants.OP_ADDCARD:
				switch(counter_infos_read) {
					case 0:
						project_name = s;
						counter_infos_read++;
					break;
					case 1:
						card_name = s;
						counter_infos_read++;
					break;
					case 2:
						description = s;
					break;
				}
			break;
			case Constants.OP_MOVECARD:
				switch(counter_infos_read) {
					case 0:
						project_name = s;
						counter_infos_read++;
					break;
					case 1:
						card_name = s;
						counter_infos_read++;
					break;
					case 2:
						starting_list = s;
						counter_infos_read++;
					break;
					case 3:
						destination_list = s;
					break;
				}
			break;
			case Constants.OP_GETCARDHISTORY:
				switch(counter_infos_read) {
					case 0:
						project_name = s;
						counter_infos_read++;
					break;
					case 1:
						card_name = s;
					break;
				}
			break;
			case Constants.OP_CANCELPROJECT:
				project_name = s;
			break;
			
			default:
			break;
		}
		
	}
	
	
	
}
