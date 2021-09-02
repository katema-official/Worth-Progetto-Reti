package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelChatNotAvailable extends WorthPanel{

	public WorthPPanelChatNotAvailable(WorthPopup popup) {
		
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("La chat di questo progetto non è al momento disponibile,");
		lblNewLabel.setBounds(13, 36, 400, 27);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("ci scusiamo per il disagio.");
		lblNewLabel_1.setBounds(13, 56, 400, 27);
		panel.add(lblNewLabel_1);
		
		JButton btnNewButton = new JButton(":(");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 80, 133, 44);
		panel.add(btnNewButton);
		
		
		
	}
	
}
