package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelNoCardsToShow extends WorthPanel{

	public WorthPPanelNoCardsToShow(WorthPopup popup) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Questo progetto non ha ancora nessuna card");
		lblNewLabel.setBounds(83, 36, 350, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Non per molto...");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 153, 44);
		panel.add(btnNewButton);
	}
}
