package worth.lagreca.guicomponents;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import worth.lagreca.chat.ClientChatInfo;
import worth.lagreca.client.ClientInfo;
import worth.lagreca.client.ClientTcpOperations;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.WorthPPanelCardAlreadyExists;
import worth.lagreca.guipopup.WorthPPanelCardCreated;
import worth.lagreca.guipopup.WorthPPanelCardEqualsProject;
import worth.lagreca.guipopup.WorthPPanelCardFromStateError;
import worth.lagreca.guipopup.WorthPPanelCardInvalidDisplacement;
import worth.lagreca.guipopup.WorthPPanelCardMoved;
import worth.lagreca.guipopup.WorthPPanelCardNameTooLong;
import worth.lagreca.guipopup.WorthPPanelChatNotAvailable;
import worth.lagreca.guipopup.WorthPPanelCouldntAddMemberBecauseUserdDoesNotExist;
import worth.lagreca.guipopup.WorthPPanelCouldntDeleteProject;
import worth.lagreca.guipopup.WorthPPanelNoCardsToShow;
import worth.lagreca.guipopup.WorthPPanelProjectDeleted;
import worth.lagreca.guipopup.WorthPPanelProjectDoesntExist;
import worth.lagreca.guipopup.WorthPPanelUnknownCard;
import worth.lagreca.guipopup.WorthPPanelUserAddedToProject;
import worth.lagreca.guipopup.WorthPPanelUserAlreadyParticipatesToThisProject;
import worth.lagreca.guipopup.WorthPopup;

public class WorthPanelProjectSelected extends WorthPanel{

	private boolean DEBUG = true;
	
	private JTextField textField_2;
	private JComboBox comboBox;
	private JComboBox comboBox_1;
	
	public WorthPanelProjectSelected(WorthFrame frame) {
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Hai selezionato il progetto " + ClientInfo.nome_progetto_selezionato + ".");
		lblNewLabel.setBounds(10, 11, 854, 34);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Cosa vuoi fare?");
		lblNewLabel_1.setBounds(10, 38, 154, 25);
		panel.add(lblNewLabel_1);
		
		JButton btnNewButton = new JButton("Mostra membri");
		btnNewButton.setBounds(68, 77, 154, 80);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Pulsante per mostrare i membri di questo progetto. Per ottenere la lista dei membri, la chiedo mediante la connessione TCP al server
				HashSet<String> hs = ClientTcpOperations.clientShowMembers(ClientInfo.nome_progetto_selezionato);
				String s = "";
				for(int i=0; i<21; i++) {
					s += Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE;
				}
				if(hs.contains(s)) {
					WorthPopup popup9 = new WorthPopup();
					popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
				}else {
					frame.setPanel(new WorthPanelShowMembersOfProject(frame, hs));
				}
			}
		});	
		panel.add(btnNewButton);
		
		JButton btnMostraLeCard = new JButton("Mostra le card di questo progetto");
		btnMostraLeCard.setBounds(299, 56, 279, 120);
		btnMostraLeCard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Pulsante per mostrare le card di questo progetto. Per ottenere la lista delle card, la chiedo mediante la connessione TCP al server
				ArrayList<String> al = ClientTcpOperations.clientShowCards(ClientInfo.nome_progetto_selezionato);
				
				//se la lista di card è vuota, non devo fare niente, solo dire al client che non esistono card per questo progetto
				if(al.get(0).equals(Constants.RES_SHOWCARDS_NO_CARD)) {
					WorthPopup popup0 = new WorthPopup();
					popup0.setPanel(new WorthPPanelNoCardsToShow(popup0));
				}else {
					String s = "";
					for(int i=0; i<51; i++) {
						s += String.valueOf(Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE);
					}
					if(al.get(0).equals(s)) {
						WorthPopup popup9 = new WorthPopup();
						popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
					}else {
						frame.setPanel(new WorthPanelShowCards(frame, al));
					}
				}
			}
		});
		panel.add(btnMostraLeCard);
		
		JLabel lblNewLabel_2 = new JLabel("Inserisci qui sotto il nome del nuovo membro");
		lblNewLabel_2.setBounds(600, 7, 280, 25);
		panel.add(lblNewLabel_2);
		
		JTextField textField = new JTextField();
		textField.setBounds(618, 33, 223, 34);
		panel.add(textField);
		textField.setColumns(10);
		
		JButton btnAggiungiUnNuovo = new JButton("Aggiungi un nuovo membro");
		btnAggiungiUnNuovo.setBounds(618, 77, 223, 80);
		btnAggiungiUnNuovo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pulsante per l'aggiunta di un nuovo membro
				
				//la stringa vuota non è ammessa (non ci sono utenti con "" come nome)
				if(!textField.getText().equals("")) {
					String name_new_member = textField.getText();
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("nome nuovo membro: " + name_new_member);
					
					//mando al server il nome del progetto e l'utente da aggiungere
					int res = ClientTcpOperations.clientAddMember(ClientInfo.nome_progetto_selezionato, name_new_member);
					switch(res) {
						case Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE:
							WorthPopup popup9 = new WorthPopup();
							popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
						break;
						case Constants.RES_ADDMEMBER_UNKNOWN_USER:
							WorthPopup popup0 = new WorthPopup();
							popup0.setPanel(new WorthPPanelCouldntAddMemberBecauseUserdDoesNotExist(popup0));
						break;
						case Constants.RES_ADDMEMBER_MEMBER_ALREADY_IN_PROJECT:
							WorthPopup popup1 = new WorthPopup();
							popup1.setPanel(new WorthPPanelUserAlreadyParticipatesToThisProject(popup1));
						break;
						case Constants.RES_ADDMEMBER_SUCCESS:
							WorthPopup popup2 = new WorthPopup();
							popup2.setPanel(new WorthPPanelUserAddedToProject(popup2));
						break;
						default:
							if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Errore lato client durante l'aggiunta di un nuovo membro: codice di risposta non riconosciuto");
						break;
					}
					textField.setText("");
				}
			}
		});
		panel.add(btnAggiungiUnNuovo);
		
		JLabel lblNewLabel_3 = new JLabel("Inserisci qui sotto il nome della nuova card");
		lblNewLabel_3.setBounds(27, 197, 250, 25);
		panel.add(lblNewLabel_3);
		
		JTextField textField_1 = new JTextField();
		textField_1.setBounds(31, 233, 233, 34);
		panel.add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblNewLabel_3_1 = new JLabel("Inserisci qui sotto la descrizione della nuova card");
		lblNewLabel_3_1.setBounds(1, 278, 279, 25);
		panel.add(lblNewLabel_3_1);
		
		JTextField textField_3 = new JTextField();
		textField_3.setBounds(31, 307, 233, 34);
		panel.add(textField_3);
		textField_3.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("Crea una nuova card");
		btnNewButton_1.setBounds(31, 359, 233, 71);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//mando al server nome e descrizione della nuova card. Non ammetto l'esistenza di card senza nome,
				//ma ammetto che ci possano essere card senza descrizione (un membro del progetto potrebbe volerne
				//creare una come semplice promemoria)
				if(!textField_1.getText().equals("")) {
					String card_name = textField_1.getText();
					String card_description = textField_3.getText();
					int res = ClientTcpOperations.clientAddCard(ClientInfo.nome_progetto_selezionato, card_name, card_description);
					
					switch(res) {
						case Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE:
							WorthPopup popup9 = new WorthPopup();
							popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
						break;
						case Constants.RES_ADDCARD_CARD_NAME_EQUALS_PROJECT_NAME:
							WorthPopup popup0 = new WorthPopup();
							popup0.setPanel(new WorthPPanelCardEqualsProject(popup0));
						break;
						case Constants.RES_ADDCARD_CARD_ALREADY_EXISTS:
							WorthPopup popup1 = new WorthPopup();
							popup1.setPanel(new WorthPPanelCardAlreadyExists(popup1));
						break;
						case Constants.RES_ADDCARD_CARD_NAME_TOO_LONG:
							WorthPopup popup2 = new WorthPopup();
							popup2.setPanel(new WorthPPanelCardNameTooLong(popup2));
						break;
						case Constants.RES_ADDCARD_SUCCESS:
							WorthPopup popup3 = new WorthPopup();
							popup3.setPanel(new WorthPPanelCardCreated(popup3));
							textField_1.setText("");
							textField_3.setText("");
						break;
					}
				}
			}
		});
		panel.add(btnNewButton_1);
		
		JLabel lblNewLabel_4 = new JLabel("Scrivi qui sotto il nome della card che vuoi muovere");
		lblNewLabel_4.setBounds(290, 197, 290, 25);
		panel.add(lblNewLabel_4);
		
		textField_2 = new JTextField();
		textField_2.setBounds(299, 233, 279, 34);
		panel.add(textField_2);
		textField_2.setColumns(10);
		
		String[] options_lists_1 = {"To do", "In progress", "To be revised"};
		String[] options_lists_2 = {"In progress", "To be revised", "Done"};
		
		comboBox = new JComboBox(options_lists_1);
		comboBox.setBounds(299, 314, 279, 34);
		panel.add(comboBox);
		
		JLabel lblNewLabel_5 = new JLabel("Indicami la lista di partenza qua sotto");
		lblNewLabel_5.setBounds(345, 278, 233, 25);
		panel.add(lblNewLabel_5);
		
		comboBox_1 = new JComboBox(options_lists_2);
		comboBox_1.setBounds(299, 395, 279, 34);
		panel.add(comboBox_1);
		
		JLabel lblNewLabel_6 = new JLabel("Seleziona la lista dove spostare la card");
		lblNewLabel_6.setBounds(331, 359, 240, 25);
		panel.add(lblNewLabel_6);
		
		JButton btnNewButton_2 = new JButton("Muovi la card");
		btnNewButton_2.setBounds(299, 454, 279, 71);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pulsante che, se premuto, chiede al server di spostare una card da una lista all'altra. Non fa nulla se il testo
				//inserito è vuoto (non esistono card con "" come nome) o se la lista di partenza e quella di destinazione coincidono
				//(non ha senso chiedere al server di spostare una card nella lista in cui questa già si trova)
				if(!textField_2.getText().equals("")) {
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Voglio spostare la card " + textField_2.getText() + " Dalla lista " + comboBox.getSelectedItem() + " alla lista " + comboBox_1.getSelectedItem());
					String card_to_move = textField_2.getText();
					String from_list = comboBox.getSelectedItem().toString();
					String to_list = comboBox_1.getSelectedItem().toString();
					
					if(!from_list.equals(to_list)) {
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("L'operazione move card ha senso, le due liste non coincidono");
						int res = ClientTcpOperations.clientMoveCard(ClientInfo.nome_progetto_selezionato, card_to_move, from_list, to_list);
						switch(res) {
							case Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE:
								WorthPopup popup9 = new WorthPopup();
								popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
							break;
							case Constants.RES_MOVECARD_UNKNOWN_CARD:
								WorthPopup popup0 = new WorthPopup();
								popup0.setPanel(new WorthPPanelUnknownCard(popup0));
							break;
							case Constants.RES_MOVECARD_FROM_STATE_ERROR:
								WorthPopup popup1 = new WorthPopup();
								popup1.setPanel(new WorthPPanelCardFromStateError(popup1, card_to_move, to_list, frame));
							break;
							
							case Constants.RES_MOVECARD_INVALID_DISPLACEMENT:
								WorthPopup popup2 = new WorthPopup();
								popup2.setPanel(new WorthPPanelCardInvalidDisplacement(popup2, from_list));
							break;
							case Constants.RES_MOVECARD_SUCCESS:
								WorthPopup popup3 = new WorthPopup();
								popup3.setPanel(new WorthPPanelCardMoved(popup3));
								//se la card è stata spostata con successo nella lista done, pulisco il text field, tanto sicuramente quella card non si muoverà più
								if(to_list.equals("Done")) {
									textField_2.setText("");
								}
							break;
							default:
								if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Errore nel movimento di una card: risposta non riconosciuta");
							break;
						}
					}
					
				}
			}
		});
		panel.add(btnNewButton_2);
		
		JTextField textField_4 = new JTextField();
		textField_4.setBounds(622, 251, 219, 34);
		panel.add(textField_4);
		textField_4.setColumns(10);
		
		JLabel lblNewLabel_7 = new JLabel("Scrivi qui sotto il nome della card di cui vuoi");
		lblNewLabel_7.setBounds(610, 202, 263, 25);
		panel.add(lblNewLabel_7);
		
		JLabel lblNewLabel_7_1 = new JLabel("conoscere la history");
		lblNewLabel_7_1.setBounds(679, 221, 185, 25);
		panel.add(lblNewLabel_7_1);
		
		JButton btnNewButton_3 = new JButton("Visualizza history della card");
		btnNewButton_3.setBounds(623, 299, 218, 64);
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!textField_4.getText().equals("")) {
					String card_chosen = textField_4.getText();
					ArrayList<String> al = ClientTcpOperations.clientGetCardHistory(ClientInfo.nome_progetto_selezionato, card_chosen);
					String s = "";
					String ss = "";
					for(int i=0; i<51; i++) {
						s += String.valueOf(Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE);
						ss += String.valueOf(0);
					}
					//se il progetto non esiste
					if(al.get(0).equals(s)) {
						WorthPopup popup9 = new WorthPopup();
						popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
					}else {
						//se la card non esiste
						if(al.get(0).equals(ss)) {
							WorthPopup popup0 = new WorthPopup();
							popup0.setPanel(new WorthPPanelUnknownCard(popup0));
						}else {
							frame.setPanel(new WorthPanelCardHistory(frame, al, card_chosen));
						}
					}
				}
			}
		});
		panel.add(btnNewButton_3);
		
		JButton btnNewButton_4 = new JButton("(!) Cancella questo progetto (!)");
		btnNewButton_4.setBounds(299, 588, 279, 52);
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pulsante per chiedere al server di cancellare questo progetto
				int res = ClientTcpOperations.clientCancelProject(ClientInfo.nome_progetto_selezionato);
				switch(res) {
					case Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE:
						WorthPopup popup9 = new WorthPopup();
						popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
					break;
					case Constants.RES_CANCELPROJECT_ERROR:
						WorthPopup popup0 = new WorthPopup();
						popup0.setPanel(new WorthPPanelCouldntDeleteProject(popup0));
					break;
					case Constants.RES_CANCELPROJECT_SUCCESS:
						WorthPopup popup1 = new WorthPopup();
						popup1.setPanel(new WorthPPanelProjectDeleted(popup1));
						ClientInfo.nome_progetto_selezionato = null;
						frame.setPanel(new WorthPanelListProjects(frame));
					break;
				}
			}
		});
		panel.add(btnNewButton_4);
		
		JButton btnNewButton_5 = new JButton("Vai alla chat");
		btnNewButton_5.setBounds(68, 462, 165, 144);
		btnNewButton_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//controllo se il progetto esiste
				if(!ClientChatInfo.mapChatManagers.containsKey(ClientInfo.nome_progetto_selezionato)){
					WorthPopup popup9 = new WorthPopup();
					popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
				}else {
					//controllo se una chat c'è (potrebbe non avere un indirizzo di multicast
					//se questi sono finiti)
					if(ClientChatInfo.mapChatManagers.get(ClientInfo.nome_progetto_selezionato).multicast_address == null) {
						WorthPopup popup0 = new WorthPopup();
						popup0.setPanel(new WorthPPanelChatNotAvailable(popup0));
					}else {
						ClientChatInfo.chat_selected = true;
						frame.setPanel(new WorthPanelChat(frame));
					}
				}
			}
		});
		panel.add(btnNewButton_5);
		
		JButton btnNewButton_6 = new JButton("Torna all'elenco dei tuoi progetti");
		btnNewButton_6.setBounds(618, 512, 233, 98);
		btnNewButton_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClientInfo.nome_progetto_selezionato = null;
				frame.setPanel(new WorthPanelListProjects(frame));
			}
		});
		panel.add(btnNewButton_6);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBackground(Color.BLACK);
		separator.setForeground(Color.BLACK);
		separator.setBounds(282, 0, 12, 651);
		panel.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.BLACK);
		separator_1.setForeground(Color.BLACK);
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(595, 0, 12, 651);
		panel.add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBackground(Color.BLACK);
		separator_2.setForeground(Color.BLACK);
		separator_2.setBounds(0, 187, 874, 10);
		panel.add(separator_2);
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setForeground(Color.BLACK);
		separator_3.setBackground(Color.BLACK);
		separator_3.setBounds(0, 441, 284, 125);
		panel.add(separator_3);
		
		JSeparator separator_4 = new JSeparator();
		separator_4.setForeground(Color.BLACK);
		separator_4.setBackground(Color.BLACK);
		separator_4.setBounds(282, 550, 313, 16);
		panel.add(separator_4);
		
		JSeparator separator_5 = new JSeparator();
		separator_5.setForeground(Color.BLACK);
		separator_5.setBackground(Color.BLACK);
		separator_5.setBounds(595, 463, 279, 16);
		panel.add(separator_5);
		
	}
	
	
	
	
	public void set_values(String card_name, String from_list, String to_list) {
		textField_2.setText(card_name);
		comboBox.setSelectedItem(from_list);
		comboBox_1.setSelectedItem(to_list);
	}
	
}
