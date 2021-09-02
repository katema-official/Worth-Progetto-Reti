package worth.lagreca.projectsandcards;

import java.util.ArrayList;

public class WorthCard {
	//classe i cui oggetti rappresentano le card di worth. Le informazioni relative ad una card
	//sono: il suo nome (univoco all'interno di un progetto), il suo stato attuale (todo, inprogress,
	//toberevised o done), la sua descrizione e la history, ovvero una lista contenente gli stati passati
	//della card (quello attuale NON è incluso)
	
	public String name;
	public String currentState;
	public String description;
	
	//uso un arrayList perché mi interessa solo aggiungere elementi ad una lista, e quando li accedo
	//lo faccio in maniera sequenziale
	public ArrayList<String> history;
	
	public WorthCard(String name, String description) {
		this.name = name;
		this.description = description;
		currentState = "To do";
		history = new ArrayList<String>();
	}
	
}
