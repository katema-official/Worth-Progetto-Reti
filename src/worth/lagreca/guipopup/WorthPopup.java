package worth.lagreca.guipopup;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import worth.lagreca.guicomponents.WorthPanel;


public class WorthPopup {
	//classe del tutto simile a @see WorthFrame, ma usata per i popup, ovvero frame aggiuntivi
	//che forniscono un feedback all'utente circa le operazioni effettuate e il loro esito
	private JFrame frame;
	
	public WorthPopup() {
		frame = new JFrame();
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 400, 200);
		frame.setResizable(false);	
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
		
	public void setPanel(WorthPanel panel) {
		frame.setContentPane(panel.getActualPanel());
		SwingUtilities.updateComponentTreeUI(frame);
		frame.setLocationRelativeTo(null);
	}
	
	public void disposePopup() {
		frame.dispose();
	}
	
	
}
