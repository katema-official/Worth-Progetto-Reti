package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthFrame;
import worth.lagreca.guicomponents.WorthPanel;
import worth.lagreca.guicomponents.WorthPanelLogged;

public class WorthPPanelProjectDoesntExist extends WorthPanel{
	
	public WorthPPanelProjectDoesntExist(WorthPopup popup, WorthFrame frame) {
		
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: questo progetto non esiste più");
		lblNewLabel.setBounds(53, 36, 350, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("That hurts");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 200, 44);
		panel.add(btnNewButton);
		
		frame.setPanel(new WorthPanelLogged(frame));
		
	}
}





