package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelTooLongUsername extends WorthPanel{

	public WorthPPanelTooLongUsername(WorthPopup popup) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: nome utente più lungo di 20 caratteri");
		lblNewLabel.setBounds(63, 36, 268, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Andrò dall'anagrafe");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(100, 74, 173, 44);
		panel.add(btnNewButton);
	
	}
	
}
