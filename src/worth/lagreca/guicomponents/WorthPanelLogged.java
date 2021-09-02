package worth.lagreca.guicomponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import worth.lagreca.client.ClientInfo;
import worth.lagreca.client.ClientTcpOperations;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.*;

public class WorthPanelLogged extends WorthPanel{
	
	public WorthPanelLogged(WorthFrame frame) {
		
		ClientInfo.nome_progetto_selezionato = null;
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Bentornato su Worth " + ClientInfo.nome_utente_loggato + ". Cosa vuoi fare?");
		lblNewLabel.setBounds(100, 11, 800, 64);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Mostra utenti");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//mostro tutti gli utenti registrati a worth
				frame.setPanel(new WorthPanelShowAllUsers(frame));
			
			}
		});
		btnNewButton.setBounds(577, 92, 204, 84);
		panel.add(btnNewButton);
		
		JButton btnMostraUtentiOnline = new JButton("Mostra utenti online");
		btnMostraUtentiOnline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//mostro gli utenti di worth che sono online
				frame.setPanel(new WorthPanelShowOnlineUsers(frame));
			}
		});
		btnMostraUtentiOnline.setBounds(577, 316, 204, 92);
		panel.add(btnMostraUtentiOnline);
		
		JButton btnMostraIMiei = new JButton("Mostra i miei progetti");
		btnMostraIMiei.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//mostro i progetti di cui l'utente fa parte
				frame.setPanel(new WorthPanelListProjects(frame));
			}
		});
		btnMostraIMiei.setBounds(88, 92, 204, 84);
		panel.add(btnMostraIMiei);
		
		JLabel lblNewLabel_1 = new JLabel("Inserisci qui sotto il nome del nuovo progetto");
		lblNewLabel_1.setBounds(82, 244, 318, 29);
		panel.add(lblNewLabel_1);
		
		JTextField textField = new JTextField();
		textField.setBounds(54, 280, 277, 29);
		panel.add(textField);
		textField.setColumns(10);
		
		JButton btnCreaUnNuovo = new JButton("Crea un nuovo progetto");
		btnCreaUnNuovo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//per creare un nuovo progetto, l'utente deve mandare al server il nome di questo progetto.
				//Se in tutta WORTH non esiste ancora un progetto con questo nome, la richiesta verrà soddisfatta,
				//altrimenti il client verrà informato del fatto che deve scegliere un nome diverso per il suo progetto.
				if(!textField.getText().equals("")) {
					int res = ClientTcpOperations.clientCreateProject(textField.getText());
					
					switch(res) {
						case Constants.RES_CREATEPROJECT_PROJECT_ALREADY_EXISTS:
							WorthPopup popup0 = new WorthPopup();
							popup0.setPanel(new WorthPPanelProjectAlreadyExists(popup0));
						break;
						case Constants.RES_CREATEPROJECT_PROJECT_CREATED:
							WorthPopup popup1 = new WorthPopup();
							popup1.setPanel(new WorthPPanelNewProjectCreated(popup1));
						break;
						default:
						break;
					}
				}
				textField.setText("");
			}
		});
		btnCreaUnNuovo.setBounds(88, 320, 204, 84);
		panel.add(btnCreaUnNuovo);
		
		JButton btnLogout = new JButton("Logout");
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//quando l'utente vuole effettuare il logout, deve mandare il suo nome utente al server
				//e attendere una risposta
				int res = ClientTcpOperations.clientLogout(ClientInfo.nome_utente_loggato);
				
				switch(res) {
					case Constants.RES_LOGOUT_SUCCESS:
						frame.setPanel(new WorthPanelLogin(frame));
					break;
					default:
					break;
				}
				
			}
		});
		btnLogout.setBounds(341, 499, 204, 84);
		panel.add(btnLogout);
	}
	
	
}
