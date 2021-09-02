package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelCardNameTooLong extends WorthPanel{

	public WorthPPanelCardNameTooLong(WorthPopup popup) {
		
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: il nome di una card non può superare i 50 caratteri");
		lblNewLabel.setBounds(13, 36, 400, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Sintetizzo");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 133, 44);
		panel.add(btnNewButton);
		
		
		
		
		
		
	}
}
