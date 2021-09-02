package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelCardEqualsProject extends WorthPanel{
	
	public WorthPPanelCardEqualsProject(WorthPopup popup) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: il nome della card deve essere diverso");
		lblNewLabel.setBounds(43, 36, 350, 27);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("da quello del progetto");
		lblNewLabel_1.setBounds(43, 66, 350, 27);
		panel.add(lblNewLabel_1);
		
		JButton btnNewButton = new JButton("Va bene");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 100, 133, 44);
		panel.add(btnNewButton);
	}
}
