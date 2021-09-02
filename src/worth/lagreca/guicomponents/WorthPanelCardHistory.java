package worth.lagreca.guicomponents;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import worth.lagreca.client.ClientInfo;
import worth.lagreca.client.ClientTcpOperations;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.WorthPPanelProjectDoesntExist;
import worth.lagreca.guipopup.WorthPPanelUnknownCard;
import worth.lagreca.guipopup.WorthPopup;

public class WorthPanelCardHistory extends WorthPanel{
	
	public WorthPanelCardHistory(WorthFrame frame, ArrayList<String> al, String card_name) {
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JPanel panell = new JPanel();
		panell.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(42, 34, 795, 371);
		panel.add(scrollPane);
		
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		Iterator<String> iterator = al.iterator();
		while(iterator.hasNext()) {
			String current = iterator.next();
			dlm.addElement(current);
		}
		
		JList<String> list = new JList<String>(dlm);
		scrollPane.setViewportView(list);
		
		panel.add(panell, BorderLayout.CENTER);
		
		JButton btnTornaAlla = new JButton("Torna alla schermata precedente");
		btnTornaAlla.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.setPanel(new WorthPanelProjectSelected(frame));
			}
		});
		btnTornaAlla.setBounds(30, 430, 300, 100);
		panel.add(btnTornaAlla);
		
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pulsante per aggiornare la visualizzazione gli utenti online e offline
				ArrayList<String> al = ClientTcpOperations.clientGetCardHistory(ClientInfo.nome_progetto_selezionato, card_name);
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
						frame.setPanel(new WorthPanelCardHistory(frame, al, card_name));
					}
				}
			}
		});
		btnRefresh.setBounds(570, 430, 300, 100);
		panel.add(btnRefresh);
	}
	
	
	
	
}
