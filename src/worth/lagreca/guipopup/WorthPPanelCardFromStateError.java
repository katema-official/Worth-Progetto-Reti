package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.client.ClientInfo;
import worth.lagreca.client.ClientTcpOperations;
import worth.lagreca.guicomponents.WorthFrame;
import worth.lagreca.guicomponents.WorthPanel;
import worth.lagreca.guicomponents.WorthPanelShowCard_alt;

public class WorthPPanelCardFromStateError extends WorthPanel{

	public WorthPPanelCardFromStateError(WorthPopup popup, String card_name, String to_list, WorthFrame frame) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: la lista di partenza che hai specificato");
		lblNewLabel.setBounds(33, 36, 367, 27);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("per questa card non è quella corretta");
		lblNewLabel_1.setBounds(33, 66, 367, 27);
		panel.add(lblNewLabel_1);
		
		JButton btnNewButton = new JButton("Ok");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(30, 100, 100, 44);
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Visualizza questa card");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				ArrayList<String> al = ClientTcpOperations.clientShowCard(ClientInfo.nome_progetto_selezionato, card_name);
				frame.setPanel(new WorthPanelShowCard_alt(frame, al, to_list));
				popup.disposePopup();
				
			}
		});
		btnNewButton_1.setBounds(160, 100, 210, 44);
		panel.add(btnNewButton_1);
		
	}
}
