package worth.lagreca.guipopup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import worth.lagreca.guicomponents.WorthPanel;

public class WorthPPanelUsernameTaken extends WorthPanel{

	public WorthPPanelUsernameTaken(WorthPopup popup) {
		panel.setBounds(100, 100, 500, 300);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Errore: questo nome utente \u00E8 gi\u00E0 stato preso");
		lblNewLabel.setBounds(63, 11, 268, 27);
		panel.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Che paura!");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.disposePopup();
			}
		});
		btnNewButton.setBounds(122, 74, 133, 44);
		panel.add(btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Magari si tratta del tuo gemello malvagio");
		lblNewLabel_1.setBounds(73, 36, 300, 27);
		panel.add(lblNewLabel_1);
		
		
		
	}
}
