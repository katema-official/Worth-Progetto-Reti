package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelUserAlreadyParticipatesToThisProject extends WorthPanel{

	public WorthPPanelUserAlreadyParticipatesToThisProject(WorthPopup popup){
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: questo utente fa già parte di questo progetto");
		lblNewLabel.setBounds(33, 36, 390, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Ah ma davvero?");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 133, 44);
		panel.add(btnNewButton);
	}
	
	
}
