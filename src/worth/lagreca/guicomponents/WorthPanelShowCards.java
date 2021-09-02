package worth.lagreca.guicomponents;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import worth.lagreca.client.ClientInfo;
import worth.lagreca.client.ClientTcpOperations;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.WorthPPanelNoCardsToShow;
import worth.lagreca.guipopup.WorthPPanelProjectDoesntExist;
import worth.lagreca.guipopup.WorthPopup;

public class WorthPanelShowCards extends WorthPanel{
	
	public WorthPanelShowCards(WorthFrame frame, ArrayList<String> al) {
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Fai doppio click su una card per vederne il contenuto");
		lblNewLabel.setBounds(42, 0, 500, 64);
		panel.add(lblNewLabel);
		
		JPanel panell = new JPanel();
		panell.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(42, 50, 795, 371);
		panel.add(scrollPane);
		
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		Iterator<String> iterator = al.iterator();
		while(iterator.hasNext()) {
			String member_current = iterator.next();
			dlm.addElement(member_current);
		}
		
		JList<String> list = new JList<String>(dlm);
		scrollPane.setViewportView(list);
		
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				JList<String> theList = (JList<String>) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						Object o = theList.getModel().getElementAt(index);
						
						String selected_card = o.toString();
						ArrayList<String> al = ClientTcpOperations.clientShowCard(ClientInfo.nome_progetto_selezionato, selected_card);
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
		        }
			}
		};
		list.addMouseListener(mouseListener);
		
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
				//pulsante per aggiornare la visualizzazione delle card del progetto
				
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
		btnRefresh.setBounds(570, 430, 300, 100);
		panel.add(btnRefresh);
	
	}
}
