package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelUserAlreadyLogged extends WorthPanel{
	
	public WorthPPanelUserAlreadyLogged(WorthPopup popup){
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: questo utente risulta già loggato");
		lblNewLabel.setBounds(83, 36, 268, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Beccato!");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 133, 44);
		panel.add(btnNewButton);
	}
}
