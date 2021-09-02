package worth.lagreca.guicomponents;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import worth.lagreca.chat.ClientChatInfo;
import worth.lagreca.client.ClientInfo;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.WorthPPanelProjectDoesntExist;
import worth.lagreca.guipopup.WorthPopup;

public class WorthPanelListProjects extends WorthPanel{

	private boolean DEBUG = true;
	
	public WorthPanelListProjects(WorthFrame frame) {
		
		ClientInfo.nome_progetto_selezionato = null;
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Fai doppio click su un progetto per selezionarlo");
		lblNewLabel.setBounds(42, 0, 500, 64);
		panel.add(lblNewLabel);
		
		JPanel panell = new JPanel();
		panell.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(42, 50, 795, 371);
		panel.add(scrollPane);
		
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		Iterator<String> iterator = ClientChatInfo.mapChatManagers.keySet().iterator();
		while(iterator.hasNext()) {
			String project_current = iterator.next();
			dlm.addElement(project_current);
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
						//quando clicco su un progetto per selezionarlo, quel progetto potrebbe non esistere più (magari nel frattempo
						//qualcuno lo ha eliminato). Nel caso, restituisco un messaggio d'errore e riporto l'utente alla schermata iniziale.
						if(ClientChatInfo.mapChatManagers.containsKey(o.toString())) {
							ClientInfo.nome_progetto_selezionato = o.toString();
							frame.setPanel(new WorthPanelProjectSelected(frame));
							if(Constants.GLOBALDEBUG && DEBUG) System.out.println(o.toString());
						}else {
							WorthPopup popup9 = new WorthPopup();
							popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
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
				frame.setPanel(new WorthPanelLogged(frame));
			}
		});
		btnTornaAlla.setBounds(30, 430, 300, 100);
		panel.add(btnTornaAlla);
		
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pulsante per aggiornare la visualizzazione gli utenti online e offline
				frame.setPanel(new WorthPanelListProjects(frame));
			}
		});
		btnRefresh.setBounds(570, 430, 300, 100);
		panel.add(btnRefresh);
	
	}
	
}
