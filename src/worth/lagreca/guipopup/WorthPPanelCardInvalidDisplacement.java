package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelCardInvalidDisplacement extends WorthPanel{

	public WorthPPanelCardInvalidDisplacement(WorthPopup popup, String from_list) {
		
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: le card della lista " + from_list + " possono essere spostate");
		lblNewLabel.setBounds(3, 36, 400, 27);
		panel.add(lblNewLabel);
		
		String result_string = null;
		switch(from_list) {
			case "To do":
				result_string = "Solo nella lista \"In progress\".";
			break;
			case "In progress":
				result_string = "Solo nella lista \"To be revised\" o nella lista \"Done\".";
			break;
			case "To be revised":
				result_string = "Solo nella lista \"In progress\" o nella lista \"Done\".";
			break;
		}
		
		JLabel lblNewLabel_1 = new JLabel(result_string);
		lblNewLabel_1.setBounds(3, 59, 360, 27);
		panel.add(lblNewLabel_1);
		
		JButton btnNewButton = new JButton("O.o");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 100, 133, 44);
		panel.add(btnNewButton);
		
		
	}
	
}
