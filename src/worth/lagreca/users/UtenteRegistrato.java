package worth.lagreca.users;

import worth.lagreca.constants.Constants;

public class UtenteRegistrato {
	//classe che rappresenta un utente registrato a WORTH, e che mantiene solo le informazioni pubbliche
	//dell'utente, ovvero il suo nome utente e il suo stato (online o offline). Oggetti di questo tipo
	//verranno usati sia dal client che dal server
	
	//variabile per ricordare il nome dell'utente registrato a WORTH
	private String name;
	
	//variabile che mantiene lo stato dell'utente su WORTH, ovvero se è online (true) o offline (false)
	private boolean state;
	
	//metodo costruttore per quando viene creato un oggetto UtenteRegistrato
	public UtenteRegistrato(String name) {
		this.name = name;
		state = Constants.OFFLINE;
	}
	
	//getter del nome (una volta creato un utente, il suo nome non può essere modificato)
	
	public String getUtenteRegistratoName() {
		return name;
	}
	
	//getter e setter dello stato
	public boolean getUtenteRegistratoState() {
		return state;
	}
	
	public void setUtenteRegistratoState(boolean new_state) {
		state = new_state;
	}
	
	public String getUtenteRegistratoStateAsString() {
		if(state == Constants.ONLINE) {
			return "ONLINE";
		}else{
			return "OFFLINE";
		}
	}
	
	
}
