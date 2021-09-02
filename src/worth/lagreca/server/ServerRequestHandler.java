package worth.lagreca.server;

import java.nio.channels.SelectionKey;

import worth.lagreca.constants.Constants;

public class ServerRequestHandler implements Runnable{
	
	private boolean DEBUG = true;
	
	private SelectionKey key;
	private int resInt;
	private String resString;
	
	public ServerRequestHandler(SelectionKey key) {
		this.key=key;
	}

	@Override
	public void run() {
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Runnato il thread che si occuperà di soddisfare la richiesta del client");
		
		//prendo l'attachment della chiave, che mi serve per capire che operazione eseguire
		ServerWorthObjectAttached key_att = (ServerWorthObjectAttached) key.attachment();

		//TODO: prendere il valore di ritorno corretto dalle varie funzioni e salvarlo su key_att
		switch(key_att.id_operation) {
			case Constants.OP_LOGIN:
				
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Riconosciuta la richiesta di login");
				
				resInt = ServerTcpOperations.serverLogin(key_att.username, key_att.password);
				key_att.resInt = resInt;
			break;
			case Constants.OP_LOGOUT:
				resInt = ServerTcpOperations.serverLogout(key_att.username);
				key_att.resInt = resInt;
			break;
			
			case Constants.OP_CREATEPROJECT:
				resInt = ServerTcpOperations.serverCreateProject(key_att.project_name, key_att.username);
				key_att.resInt = resInt;
			break;
			case Constants.OP_ADDMEMBER:
				resInt = ServerTcpOperations.serverAddMember(key_att.project_name, key_att.new_member);
				key_att.resInt = resInt;
			break;
			case Constants.OP_SHOWMEMBERS:
				resString = ServerTcpOperations.serverShowMembers(key_att.project_name);
				key_att.resString = resString;
			break;
			case Constants.OP_SHOWCARDS:
				resString = ServerTcpOperations.serverShowCards(key_att.project_name);
				key_att.resString = resString;
			break;
			case Constants.OP_SHOWCARD:
				resString = ServerTcpOperations.serverShowCard(key_att.project_name, key_att.card_name);
				key_att.resString = resString;
			break;
			case Constants.OP_ADDCARD:
				resInt = ServerTcpOperations.serverAddCard(key_att.project_name, key_att.card_name, key_att.description);
				key_att.resInt = resInt;
			break;
			case Constants.OP_MOVECARD:
				resInt = ServerTcpOperations.serverMoveCard(key_att.project_name, key_att.card_name, key_att.starting_list, key_att.destination_list);
				key_att.resInt = resInt;
			break;
			case Constants.OP_GETCARDHISTORY:
				resString = ServerTcpOperations.serverGetCardHistory(key_att.project_name, key_att.card_name);
				key_att.resString = resString;
			break;
			case Constants.OP_CANCELPROJECT:
				resInt = ServerTcpOperations.serverCancelProject(key_att.project_name);
				key_att.resInt = resInt;
			break;
			default:
				System.out.println("Operazione richiesta dal client non riconosciuta... what?");
			break;
		}
		
		key.attach(key_att);
		
		//aggiorno l'interestOps della chiave
		key.interestOps(SelectionKey.OP_WRITE);
		
		//Vado ad avvertire la select del @see ServerWorthMultiplexerNio del fatto che c'è una nuova
		//chiave pronta
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Sveglio la select (ServerRequestHandler)");
		ServerWorthMultiplexerNio.selector.wakeup();
		
	}
	
	
	
	
	
}
