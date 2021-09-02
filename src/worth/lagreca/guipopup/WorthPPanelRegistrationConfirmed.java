package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelRegistrationConfirmed extends WorthPanel{
	
	public WorthPPanelRegistrationConfirmed(WorthPopup popup) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Registrazione effettuata con successo");
		lblNewLabel.setBounds(98, 11, 253, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Let's go");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 133, 44);
		panel.add(btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Benvenuto! Speriamo avrai un'esperienza piacevole su Worth");
		lblNewLabel_1.setBounds(20, 36, 380, 27);
		panel.add(lblNewLabel_1);
	}
}
