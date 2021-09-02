package worth.lagreca.projectsandcards;

import java.util.HashSet;
import java.util.LinkedList;


public class WorthProjectManager {
	//un oggetto di questa classe contiene tutte le informazioni relative a uno specifico progetto,
	//ovvero il suo nome, la lista dei membri, e quattro liste per le card. Ci sono anche i metodi
	//relativi a questi attributi
	
	public String projectName;
	
	//uso un HashSet per tenere traccia dei membri del progetto in quanto non possono esserci doppioni,
	//e voglio accedere in tempo costante a questa struttura dati per controllare se un utente fa già
	//parte del progetto o meno
	public HashSet<String> listMembersName;
	
	//Per le quattro liste di card, uso una LinkedList perché voglio mantenere l'ordine di inserimento delle card,
	//ma siccome verranno modificate nel tempo dagli utenti, voglio poter spostare elementi da una lista all'altra
	//(quindi rimuovere e aggiungere) in tempo costante
	public LinkedList<String> toDo;
	public LinkedList<String> inProgress;
	public LinkedList<String> toBeRevised;
	public LinkedList<String> done;
	
	

	public WorthProjectManager(String projectName, String creator) {
		listMembersName = new HashSet<String>();
		this.projectName = projectName;
		listMembersName.add(creator);
		toDo = new LinkedList<String>();
		inProgress = new LinkedList<String>();
		toBeRevised = new LinkedList<String>();
		done = new LinkedList<String>();
	}
	
}
