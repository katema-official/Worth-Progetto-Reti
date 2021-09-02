package worth.lagreca.users;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Random;

public class UtenteRegistratoConPassword {
	//oggetto che va a rappresentate un utente come lo intende il server. Sono infatti presenti anche
	//informazioni riservate: la sua password (cifrata con lo sha), il seed della password (che introduce
	//un certo grado di aleatorietà nella generazione delle password cifrate) e una lista di progetti
	//di cui l'utente fa parte, oltre all'oggetto UtenteRegistrato in cui si memorizza l'username
	//dell'utente e il suo stato.
	
	private UtenteRegistrato user;
	private String password_sha;
	private String seed;
	
	//uso un HashSet in quanto non ci possono essere membri doppioni in un progetto, e con un set
	//ho modo di controllare in tempo costante se sto aggiungendo qualcuno al progetto che già ne fa parte
	private HashSet<String> listaProgettiDiCuiSonoMembro;
	
	public UtenteRegistratoConPassword(String username, String password) {
		//come prima cosa, creo l'oggetto UtenteRegistrato relativo a questo nuovo utente
		user = new UtenteRegistrato(username);
		
		//poi vado a concatenare la password del nuovo utente con un valore casuale (convertito in stringa)
		Random random = new Random();
		int s = random.nextInt(100000);
		seed = String.valueOf(s);
		String password_to_digest = seed + password;
		
		//ora "digerisco" la password, usando la funzione hash SHA-256
		password_sha = digestPasswordSHA_256(password_to_digest);
		
		//creo la lista di progetti di cui faccio parte, che inizialmente sarà vuota
		listaProgettiDiCuiSonoMembro = new HashSet<String>();
		
	}
	
	//con questo metodo vado a cifrare la password inserita dall'utente usando la funzione hash one-way SHA-256
	//source: https://www.geeksforgeeks.org/sha-256-hash-in-java/#:~:text=In%20Cryptography%2C%20SHA%20is%20cryptographic,used%2C%20under%20the%20package%20java.
	private String digestPasswordSHA_256(String password_to_digest){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
        byte[] hash = md.digest(password_to_digest.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);  
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32){  
            hexString.insert(0, '0');  
        }  
        return hexString.toString();
	}
	
	//metodo per confrontare il nome inserito con quello di questo utente
	public boolean confrontUsername(String username) {
		if(username.equals(user.getUtenteRegistratoName())) {
			return true;
		}else {
			return false;
		}
	}
	
	//metodo per verificare se la password immessa coincide con quella di questo utente
	public boolean confrontPassword(String password_in_chiaro) {
		String password_to_digest = seed + password_in_chiaro;
		String password_hashed = digestPasswordSHA_256(password_to_digest);
		if(password_hashed.equals(password_sha)) {
			return true;
		}else {
			return false;
		}
	}
	
	//getter dell'oggetto UtenteRegistrato
	public UtenteRegistrato getUtenteRegistrato() {
		return user;
	}
	
	//metodo usato per aggiungere alla lista di progetti di cui l'utente fa parte un nuovo progetto
	public void addProjectToListOfUsersProjects(String project_name) {
		listaProgettiDiCuiSonoMembro.add(project_name);
	}
	
	//metodo per rimuovere un progetto dalla lista di progetti di cui l'utente fa parte
	public void removeProjectFromListOfUsersProjects(String project_name) {
		listaProgettiDiCuiSonoMembro.remove(project_name);
	}
	
	//getter della lista progetti di cui l'utente fa parte
	public HashSet<String> getProjectsOfUser(){
		return listaProgettiDiCuiSonoMembro;
	}
	
}
