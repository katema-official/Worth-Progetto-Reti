package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelEmptyUsername extends WorthPanel{

	public WorthPPanelEmptyUsername(WorthPopup popup) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: l'username non pu\u00F2 essere vuoto");
		lblNewLabel.setBounds(83, 36, 268, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Volevo essere Mr. invisible :(");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(70, 74, 250, 44);
		panel.add(btnNewButton);
	}
}
