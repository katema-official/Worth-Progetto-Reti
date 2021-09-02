package worth.lagreca.client.rmi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

import com.google.gson.Gson;

import worth.lagreca.chat.ClientChatInfo;
import worth.lagreca.chat.ClientChatManager;
import worth.lagreca.client.ClientInfo;
import worth.lagreca.constants.Constants;
import worth.lagreca.users.UtenteRegistrato;

public class UsersInformationsImplementation extends RemoteObject implements UsersInformationsInterface{
	
	private static boolean DEBUG = false;
	
	private static final long serialVersionUID = -2183039055672459444L;

	//implementazione del metodo astratto dell'interfaccia @see UsersInformationsInterface
	public void UpdateUserState(String utenteRegistratoJson) throws RemoteException {
		//aggiungo un nuovo utente alla struttura dati del client, o aggiorno il suo stato corrente (@see ClientInfo)
		Gson gson = new Gson();
		UtenteRegistrato u = gson.fromJson(utenteRegistratoJson, UtenteRegistrato.class);
		ClientInfo.updateUsersList(u);
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("L'utente " + u.getUtenteRegistratoName() + " è segnato come " + u.getUtenteRegistratoStateAsString());
	}

	
	
	public void UpdateUserChat(String project_name, String ind_di_multicast, boolean action) throws RemoteException {
		//con questo metodo vado a creare o a eliminare un gestore di una chat multicast associata 
		//ad un progetto (@see ClientChatManager). Ovvero: tutto parte da action: se action mi dice
		//che devo creare un nuovo progetto-chat, aggiungo una entry alle strutture dati di @see ClientChatInfo,
		//e lancio un thread che si occupi di quella chat. Se invece mi dice che lo devo cancellare,
		//vado a prendere il thread che si occupa di quella chat, lo fermo e rimuovo ogni traccia di
		//quel progetto dalle struttura dati di ClientChatInfo
		if(action == Constants.CHAT_ADD) {
			//devo creare un nuovo gestore per la chat di questo progetto
			
			if(DEBUG && ClientChatInfo.mapChatManagers.containsKey(project_name)) {
				System.out.println("Errore in UsersInformationsImplementation: mi è stato chiesto di aggiungere una chat che esiste già");
			}
			
			//creo il manager della nuova chat e il relativo thread (lanciandolo), e salvandomi questi
			//due oggetti nelle strutture dati di @see ClientChatInfo
			ClientChatManager cci = new ClientChatManager(project_name, ind_di_multicast);
			ClientChatInfo.mapChatManagers.put(project_name, cci);
			Thread t = new Thread(cci);
			if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Sto per lanciare, sul client, il thread relativo al progetto " + project_name + ", con indirizzo di multicast " + ind_di_multicast);
			t.start();
			
		}else{
			
			if(DEBUG && ClientChatInfo.mapChatManagers.containsKey(project_name) == false) {
				System.out.println("Errore in UsersInformationsImplementation: mi è stato chiesto di rimuovere una chat che però non esiste");
			}
			
			//innanzitutto, interrompo il thread relativo alla chat del progetto. Per farlo, mando un messaggio speciale, una
			//stringa vuota (""), che il thread interpreterà come "ok, ho finito il mio lavoro e posso terminare". È praticamente
			//un messaggio fittizio che mando su questo indirizzo di multicast, altrimenti il thread rimarrebbe bloccato sulle receive
			InetAddress group = null;
			try {
				ind_di_multicast = ClientChatInfo.mapChatManagers.get(project_name).multicast_address;
				group = InetAddress.getByName(ind_di_multicast);
				DatagramSocket ds = new DatagramSocket();
				String ss = "";
				DatagramPacket dp = new DatagramPacket(ss.getBytes(), 0, ss.getBytes().length, group, 4321);
				ds.send(dp);
				ds.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ClientChatInfo.mapChatManagers.remove(project_name);
		}
	}
}
