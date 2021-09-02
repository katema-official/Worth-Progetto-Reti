package worth.lagreca.guicomponents;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import worth.lagreca.client.ClientInfo;
import worth.lagreca.users.UtenteRegistrato;

public class WorthPanelShowAllUsers extends WorthPanel{

	public WorthPanelShowAllUsers(WorthFrame frame) {
		
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JPanel panell = new JPanel();
		panell.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(42, 34, 795, 371);
		panel.add(scrollPane);
		
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		Iterator<Entry<String, UtenteRegistrato>> iterator = ClientInfo.worthUsersList.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, UtenteRegistrato> utenteCorrente = (Map.Entry<String, UtenteRegistrato>) iterator.next();
			UtenteRegistrato u = utenteCorrente.getValue();
			String s = u.getUtenteRegistratoStateAsString() + " - " + u.getUtenteRegistratoName();
			dlm.addElement(s);
		}
		
		JList<String> list = new JList<String>(dlm);
		scrollPane.setViewportView(list);
		
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
				frame.setPanel(new WorthPanelShowAllUsers(frame));
			}
		});
		btnRefresh.setBounds(570, 430, 300, 100);
		panel.add(btnRefresh);
		
	}
	
}
