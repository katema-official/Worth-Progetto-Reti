package worth.lagreca.guicomponents;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import worth.lagreca.client.ClientTcpOperations;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.*;
import worth.lagreca.server.rmi.RegistrationInterface;


public class WorthPanelLogin extends WorthPanel{
	
	private boolean DEBUG = true;
	
	//classe che rappresenta il panel che deve comparire nel frame WORTH all'utente quando
	//apre il client
	
	//dichiaro lo stub dell'oggetto remoto relativo alla registrazione
	private RegistrationInterface registrationObject = null;
	
	public WorthPanelLogin(WorthFrame frame) {
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Creato il frame di login");
		
		//prendo la Registry che si trova sulla porta indicata nella classe delle costanti (@see Constants),
		//così da prendere la stub dell'oggetto remoto
		Registry reg;
		try {
			reg = LocateRegistry.getRegistry(Constants.PORT_REGISTRY);
			registrationObject = (RegistrationInterface) reg.lookup("REGISTRATION");
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
		
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Benvenuto in WORTH");
		lblNewLabel.setForeground(Color.BLACK);
		lblNewLabel.setBackground(Color.ORANGE);
		lblNewLabel.setBounds(372, 61, 300, 41);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Nome utente");
		lblNewLabel_1.setBounds(150, 134, 200, 32);
		panel.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Password");
		lblNewLabel_2.setBounds(150, 287, 200, 32);
		panel.add(lblNewLabel_2);
		
		JTextField textField = new JTextField();
		textField.setBounds(297, 134, 276, 32);
		panel.add(textField);
		
		JPasswordField passwordField = new JPasswordField();
		passwordField.setBounds(299, 287, 276, 32);
		panel.add(passwordField);
		
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Sto per aggiungere il pulsante di registrazione");
		
		JButton btnNewButton = new JButton("Registrati");
		btnNewButton.setBounds(169, 449, 152, 52);
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//l'utente vuole registrarsi. Usiamo allora l'oggetto esportato dal server per registrarci.
				//in base al valore restituito, faremo spuntare un popup di errore o di conferma.
				String name = textField.getText();
				String password = new String(passwordField.getPassword());
				int outcome = -1;
				try {
					outcome = registrationObject.registerToWorth(name, password);
					WorthPopup popup = new WorthPopup();
					switch(outcome) {
						case 0:
							popup.setPanel(new WorthPPanelEmptyPassword(popup));
						break;
						case 1:
							popup.setPanel(new WorthPPanelUsernameTaken(popup));
						break;
						case 2:
							popup.setPanel(new WorthPPanelRegistrationConfirmed(popup));
						break;
						case 3:
							popup.setPanel(new WorthPPanelEmptyUsername(popup));
						break;
						case 4:
							popup.setPanel(new WorthPPanelTooLongUsername(popup));
						break;
					}
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
			
		});
		panel.add(btnNewButton);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(562, 449, 152, 52);
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Creato il bottone di login");
		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				//l'utente, in fase di login, apre una connessione TCP col server e si identifica.
				//se il login fallisce, il server restituisce un messaggio d'errore, la connessione
				//s'interrompe e il client fa spuntare un popup d'errore, altrimenti procede
				//alla pagina iniziale di WORTH.
				String name = textField.getText();
				String password = new String(passwordField.getPassword());
				int res = ClientTcpOperations.clientLogin(name, password);
				if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Mandata la richiesta di login e ricevuta la risposta");
				switch(res) {
					case Constants.RES_LOGIN_UNKNOWN_USER:
						WorthPopup popup0 = new WorthPopup();
						popup0.setPanel(new WorthPPanelUnknownUsername(popup0));
					break;
					case Constants.RES_LOGIN_WRONG_PASSWORD:
						WorthPopup popup1 = new WorthPopup();
						popup1.setPanel(new WorthPPanelWrongPassword(popup1));
					break;
					case Constants.RES_LOGIN_USER_ALREADY_LOGGED:
						WorthPopup popup2 = new WorthPopup();
						popup2.setPanel(new WorthPPanelUserAlreadyLogged(popup2));
					break;
					case Constants.RES_LOGIN_SUCCESS:
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("================================================== SONO L'UTENTE " + name + " E MI SONO LOGGATO ==================================================");
						frame.setPanel(new WorthPanelLogged(frame));
					break;
					default:	
					break;
				}
				
			}
			
		});
		panel.add(btnLogin);
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Aggiunto il bottone di login al panel");
		
		
	}

}
