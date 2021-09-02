package worth.lagreca.guicomponents;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
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
import worth.lagreca.guipopup.WorthPopup;

public class WorthPanelShowMembersOfProject extends WorthPanel{
	
	public WorthPanelShowMembersOfProject(WorthFrame frame, HashSet<String> hs) {
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		JPanel panell = new JPanel();
		panell.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(42, 34, 795, 371);
		panel.add(scrollPane);
		
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		Iterator<String> iterator = hs.iterator();
		while(iterator.hasNext()) {
			String user = iterator.next();
			dlm.addElement(user);
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
		btnRefresh.setBounds(570, 430, 300, 100);
		panel.add(btnRefresh);
	}

}
