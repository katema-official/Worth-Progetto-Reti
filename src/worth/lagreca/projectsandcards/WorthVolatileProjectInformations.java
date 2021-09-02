package worth.lagreca.projectsandcards;

import worth.lagreca.constants.Constants;
import worth.lagreca.server.ServerInfo;

public class WorthVolatileProjectInformations {

	private boolean DEBUG = true;
	
	//oggetto che mantiene le informazioni volatili di un progetto, ovvero quelle che hanno senso di esistere
	//solo a tempo di esecuzione.
	//per ogni progetto, quando il server è attivo, mi interessa sapere due cose:
	//-quale indirizzo di multicast è associato a questo progetto
	//-quanti sono gli utenti che lo stanno usando
	
	String multicast_address_of_this_project;
	
	//variabile usata per indicare quanti utenti stanno usando questo progetto (ovvero, quanti utenti che fanno
	//parte di questo progetto sono online). Quando il server parte questo valore sarà 0, ma potrà aumentare
	//o diminuire nel tempo. Dato che è molto importante gestire correttamente questo valore, in quanto quando
	//è uguale a 0 vuol dire che non c'è bisogno di associare un indirizzo di multicast a questo progetto, mentre
	//quando è maggiore di 0 bisogna associargli un indirizzo di multicast, le operazione che lo andranno a modificare
	//(login di un utente, logout di un utente...) dovranno essere eseguite in mutua esclusione
	int number_of_users_using_this_project;
	
	public WorthVolatileProjectInformations(String multicast_address) {
		this.multicast_address_of_this_project = multicast_address;
		this.number_of_users_using_this_project = 0;
	}
	
	//metodi per aumentare, decrementare e ottenere, IN MUTUA ESCLUSIONE, il numero di utenti che stanno usando
	//questo progetto (che sono online e che partecipano a questo progetto)
	public synchronized void incrementNumberOfUsersUsingThisProject() {
		number_of_users_using_this_project++;
		//ora che sicuramente almeno una persona sta usando il progetto (perché dopo la riga di codice qui sopra
		//il numero di utenti connessi che partecipano a questo progetto è >=1) devo sincerarmi che questo progetto
		//abbia una chat di multicast associata
		if(multicast_address_of_this_project == null) {
			String ma = ServerInfo.getMulticastAddressToAssociateToAProject();
			multicast_address_of_this_project = ma;
		}
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("(++) Utenti che stanno usando il progetto con indirizzo " + multicast_address_of_this_project + ": " + number_of_users_using_this_project);
	}
	
	public synchronized void decrementNumberOfUsersUsingThisProject() {
		number_of_users_using_this_project--;
		//quando vado a decrementere il numero di utenti che stanno usando questo progetto, potrei arrivare alla situazione
		//in cui nessuno sta più usando questo progetto. Se questo è il caso, devo "riciclare" l'indirizzo di multicast
		//associato a questo progetto
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("(--) Utenti che stanno usando il progetto con indirizzo " + multicast_address_of_this_project + ": " + number_of_users_using_this_project);
		if(number_of_users_using_this_project == 0) {
			ServerInfo.putRecycledMulticastAddress(multicast_address_of_this_project);
			multicast_address_of_this_project = null;
		}
	}
	
	public synchronized int getNumberOfUsersUsingThisProject() {
		return number_of_users_using_this_project;
	}
	
	public synchronized String getMulticastAddressOfThisProject() {
		return multicast_address_of_this_project;
	}
	
	
	//metodo con cui vado a liberare l'indirizzo di multicast usato da questo progetto nel momento in cui il progetto in questione viene cancellato
	public synchronized void freeMulticastAddressBecauseProjectIsBeingCancelled() {
		ServerInfo.putRecycledMulticastAddress(multicast_address_of_this_project);
		number_of_users_using_this_project = 0;
		multicast_address_of_this_project = null;
	}
	
	
	
	
}
