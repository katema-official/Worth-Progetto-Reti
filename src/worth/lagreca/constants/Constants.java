package worth.lagreca.constants;

public class Constants {
	
	public static final boolean GLOBALDEBUG = false;
	
	//Classe che contiene delle costanti valide o per il client, o per il server, o per tutti e due.
	//Le utilizzo in quanto delle stringhe sono sicuramente più autoesplicative di alcuni numeri
	
	//costanti usate per indicare se un utente è online o meno
	public static final boolean ONLINE = true;
	public static final boolean OFFLINE = false;
	
	//costanti usate nel meccanismo di RMI callback per segnalare al client se il progetto e
	//il relativo indirizzo di multicast inviati sono da considerarsi come nuovi o come da eliminare
	public static final boolean CHAT_ADD = true;
	public static final boolean CHAT_REMOVE = false;
	
	//costante che rappresenta la porta su cui il server va ad esportare gli oggetti remoti
	public static final int PORT_REGISTRY = 1099;
	
	//costante che rappresenta la porta su il il server si mette in ascolto
	public static final int PORT_TCP = 50000;
	
	//costante per indicare quando tutti i byte di un channel sono stati letti
	public static final int END_OF_BYTES = -1;
	
	//costanti usate nell'oggetto @see ServerWorthObjectAttached per capire a che fase della lettura
	//dati sono (leggendo i dati inviati dal client al server)
	public static final int ATT_STATE_OPERATION_IDENTIFICATION = 0;
	public static final int ATT_STATE_DATA_LENGTH = 1;
	public static final int ATT_STATE_DATA_READING = 2;
	public static final int ATT_STATE_ALL_DATA_READ = 3;
	
	//costanti usate nell'oggetto @see ServerWorthObjectAttached per capire il tipo di operazione
	//che il client sta effettuando
	public static final int OP_LOGIN = 0;
	public static final int OP_LOGOUT = 1;
	public static final int OP_LISTPROJECTS = 2;
	public static final int OP_CREATEPROJECT = 3;
	public static final int OP_ADDMEMBER = 4;
	public static final int OP_SHOWMEMBERS = 5;
	public static final int	OP_SHOWCARDS = 6;
	public static final int OP_SHOWCARD = 7;
	public static final int OP_ADDCARD = 8;
	public static final int OP_MOVECARD = 9;
	public static final int	OP_GETCARDHISTORY = 10;
	public static final int OP_CANCELPROJECT = 11;
	
	//costanti usate per rendere più chiaro l'esito di alcune operazioni che il client esegue
	//sulla connessine TCP instaurata col server.
	
	//costanti usate come risposta nella fase di login
	public static final int RES_LOGIN_UNKNOWN_USER = 0;
	public static final int RES_LOGIN_WRONG_PASSWORD = 1;
	public static final int RES_LOGIN_SUCCESS = 2;
	public static final int RES_LOGIN_USER_ALREADY_LOGGED = 3;
	
	//costanti usate come risposta nella fase di logout
	public static final int RES_LOGOUT_SUCCESS = 0;
	
	//costanti usate come risposta durante l'operazione di creazione di un nuovo progetto
	public static final int RES_CREATEPROJECT_PROJECT_ALREADY_EXISTS = 0;
	public static final int RES_CREATEPROJECT_PROJECT_CREATED = 1;
	
	//costanti usate come risposta durante l'operazione di aggiunta di un membro ad un progetto
	public static final int RES_ADDMEMBER_UNKNOWN_USER = 0;
	public static final int RES_ADDMEMBER_MEMBER_ALREADY_IN_PROJECT = 1;
	public static final int RES_ADDMEMBER_SUCCESS = 2;
	
	
	
	//costanti usate come risposta durante l'operazione di aggiunta di una card
	public static final int RES_ADDCARD_CARD_ALREADY_EXISTS = 0;
	public static final int RES_ADDCARD_CARD_NAME_EQUALS_PROJECT_NAME = 1;
	public static final int RES_ADDCARD_CARD_NAME_TOO_LONG = 2;
	public static final int RES_ADDCARD_SUCCESS = 3;
	
	//costanti usate come risposta durante l'operazione di movimento di una card
	public static final int RES_MOVECARD_UNKNOWN_CARD = 0;
	public static final int RES_MOVECARD_FROM_STATE_ERROR = 1;
	public static final int RES_MOVECARD_INVALID_DISPLACEMENT = 2;
	public static final int RES_MOVECARD_SUCCESS = 3;
	
	//costanti usate come risposta durante l'operazione di cancellazione di un progetto
	public static final int RES_CANCELPROJECT_ERROR = 0;
	public static final int RES_CANCELPROJECT_SUCCESS = 1;
	
	
	//costante che vale per più operazioni, restituita quando il progetto che si vuole andare
	//in qualche modo a modificare (aggiungere membri, modificare card, etc..) non esiste più
	public static final int RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE = 9;
	
	//costante usata come risposta durante l'operazione di showcards per dire al client
	//che non ci sono card da mostrare (che ha lunghezza > 50, il limite massimo di caratteri
	//che può avere il nome di una card)
	public static final String RES_SHOWCARDS_NO_CARD = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	
}
