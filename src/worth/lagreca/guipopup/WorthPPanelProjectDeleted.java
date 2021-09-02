package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelProjectDeleted extends WorthPanel{
	
	public WorthPPanelProjectDeleted(WorthPopup popup) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Progetto eliminato correttamente");
		lblNewLabel.setBounds(83, 36, 268, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Addio, progetto mio...");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 200, 44);
		panel.add(btnNewButton);
	}

}
