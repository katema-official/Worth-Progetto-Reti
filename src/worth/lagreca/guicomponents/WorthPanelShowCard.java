package worth.lagreca.guicomponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import worth.lagreca.client.ClientInfo;
import worth.lagreca.client.ClientTcpOperations;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.WorthPPanelNoCardsToShow;
import worth.lagreca.guipopup.WorthPPanelProjectDoesntExist;
import worth.lagreca.guipopup.WorthPopup;

public class WorthPanelShowCard extends WorthPanel{
	
	public WorthPanelShowCard(WorthFrame frame, ArrayList<String> infos) {
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Nome della card:");
		lblNewLabel.setBounds(23, 11, 266, 27);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel(infos.get(0));
		lblNewLabel_1.setBounds(23, 33, 841, 36);
		panel.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Descrizione:");
		lblNewLabel_2.setBounds(23, 99, 128, 27);
		panel.add(lblNewLabel_2);
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setText(infos.get(1));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(23, 137, 829, 409);
		panel.add(scrollPane);
		
		JLabel lblNewLabel_3 = new JLabel("Stato attuale (lista in cui si trova adesso questa card):");
		lblNewLabel_3.setBounds(23, 584, 391, 27);
		panel.add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel(infos.get(2));
		lblNewLabel_4.setBounds(23, 609, 255, 31);
		panel.add(lblNewLabel_4);
		
		JButton btnNewButton = new JButton("Torna all'elenco delle card");
		btnNewButton.setBounds(637, 67, 215, 59);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//torno all'elenco delle card
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
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Refresh");
		btnNewButton_1.setBounds(466, 67, 149, 59);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//metodo per refreshare la visualizzazione della card (qualcuno potrebbe cambiarla mentre la sto guardando)
				ArrayList<String> al = ClientTcpOperations.clientShowCard(ClientInfo.nome_progetto_selezionato, infos.get(0));
				String s = "";
				for(int i=0; i<51; i++) {
					s += String.valueOf(Constants.RES_GLOBAL_PROJECT_DOESNT_EXIST_ANYMORE);
				}
				if(al.get(0).equals(s)) {
					WorthPopup popup9 = new WorthPopup();
					popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
				}else{
					frame.setPanel(new WorthPanelShowCard(frame, al));
				}
			}
		});
		panel.add(btnNewButton_1);
	}
}
