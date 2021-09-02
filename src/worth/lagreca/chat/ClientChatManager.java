package worth.lagreca.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.swing.JTextArea;

import worth.lagreca.client.ClientInfo;
import worth.lagreca.constants.Constants;

public class ClientChatManager implements Runnable{

	private boolean DEBUG = true;
	public ArrayList<String> list_of_messages;
	public String project_name;
	public String multicast_address;
	public JTextArea text_area;
	
	public ClientChatManager(String project_name, String multicast_address) {
		list_of_messages = new ArrayList<String>();
		this.project_name = project_name;
		this.multicast_address = multicast_address;
	}
	
	
	
	
	@Override
	public void run() {
		
		//Eseguo il thread solo se la chat è disponibile (c'è un indirizzo di multicast), altrimenti fermo subito il thread.
		if(multicast_address != null){
				
			InetAddress chatgroup = null;
			
			try {
				chatgroup = InetAddress.getByName(multicast_address);
			} catch (UnknownHostException e) {
				System.out.println("Errore: host sconosciuto");
			}
			
			if(chatgroup.isMulticastAddress() == false) {
				System.out.println("Errore: Questo non è un indirizzo di multicast");
			}
			
			try {
	
				//mi unisco al gruppo di multicast di questa chat
				MulticastSocket ms = new MulticastSocket(4321);
				ms.joinGroup(chatgroup);
				byte[] data = new byte[512];
				
				//mi metto a ricevere messaggi finché non vengo interrotto (perché faccio il logout o il
				//progetto non esiste più)
				while(!Thread.currentThread().isInterrupted()) {
					
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Thread con indirizzo di multicast " + multicast_address + " in esecuzione e in attesa di messaggi");
					DatagramPacket dp = new DatagramPacket(data, data.length);
					ms.receive(dp);
					
					String msg = new String(dp.getData(), 0, dp.getLength(), StandardCharsets.UTF_8);
					
					//il messaggio vuoto viene mandato quando il thread deve morire (ogni altro messaggio contiene al suo interno almeno
					//il nome del mittente, che non può mai essere vuoto)
					if(!msg.equals("")) {
					
						list_of_messages.add(msg);
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Ricevuto un messaggio sulla chat multicast " + multicast_address); 
						//se l'utente sta guardando la chat associata a questo indirizzo di multicast (e quindi a questo progetto), devo
						//aggiornare la chat grafica in tempo reale.
						if(ClientInfo.nome_progetto_selezionato!=null && ClientInfo.nome_progetto_selezionato.equals(project_name) && ClientChatInfo.chat_selected == true) {
							
							try {
							//@see ClientRealTimeChatReceiver
								text_area.append(msg);
							}catch(NullPointerException e) {
								if(Constants.GLOBALDEBUG && DEBUG) System.out.println("msg ricevuto (e che vado ad appendere alla textarea): " + msg);
								
							}
							
						}
					}else {
						//Ricevuto il messaggio stringa vuota ("") (@see UsersInformationsInterface), che viene inviato solo quando si vuole
						//che il thread termini, il thread setta a true il suo flag di interruzione, così quando riparte il ciclo while la
						//guardia sarà false
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Io, thread dell'indirizzo di multicast "+ multicast_address + ", ho ricevuto il messaggio \"\", quindi devo terminare");
						Thread.currentThread().interrupt();
					}
				}
				
				ms.leaveGroup(chatgroup);
				ms.close();
				System.out.println("Sono il thread relativo all'indirizzo di multicast " + multicast_address + " e ho terminato la mia esecuzione");
			
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	//metodo per ottenere i messaggi ricevuti finora
	public ArrayList<String> getInitialMessages() {
		return list_of_messages;
	}
	
	public void setTextAreaForClientChatManager(JTextArea text_area) {
		this.text_area = text_area;
	}
	
	
}
