package worth.lagreca.client;

import worth.lagreca.guicomponents.WorthFrame;
import worth.lagreca.guicomponents.WorthPanelLogin;

public class ClientWorthMain {

	public static void main(String[] args) {
		
		//preparo le componenti relative al meccanismo di RMI callback per questo client
		ClientTcpOperations.setUpClient();
		
		//creo il frame del client
		WorthFrame clientFrame = new WorthFrame();
		
		//imposto come panel quello iniziale, ovvero quello di accesso.
		clientFrame.setPanel(new WorthPanelLogin(clientFrame));

	}

}
