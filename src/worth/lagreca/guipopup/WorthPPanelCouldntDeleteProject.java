package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelCouldntDeleteProject extends WorthPanel{

	public WorthPPanelCouldntDeleteProject(WorthPopup popup) {
		panel.setBounds(100, 100, 400, 200);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: non è stato possibile cancellare questo progetto");
		lblNewLabel.setBounds(43, 36, 400, 27);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("in quanto non tutte le sue card sono nella lista \"Done\"");
		lblNewLabel_1.setBounds(43, 70, 400, 27);
		panel.add(lblNewLabel_1);
		
		JButton btnNewButton = new JButton("Mo ce le metto");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 100, 133, 44);
		panel.add(btnNewButton);
	}
	
	
}
