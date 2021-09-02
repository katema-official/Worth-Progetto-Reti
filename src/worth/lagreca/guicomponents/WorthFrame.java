package worth.lagreca.guicomponents;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import worth.lagreca.constants.Constants;

public class WorthFrame {
	
	private boolean DEBUG = true;
	//classe che rappresenta il frame grafico di WORTH, dove andrò ad aggiungere le componenti grafiche necessarie.
	//quando viene creato, istanzia un JFrame, che deve essere riempito con un Panel con il metodo setPanel.
	
	private JFrame frame;
	
	public WorthFrame() {
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Frame utente creato correttamente");
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 900, 700);
		frame.setResizable(false);	
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
		
	public void setPanel(WorthPanel panel) {
		frame.setContentPane(panel.getActualPanel());
		SwingUtilities.updateComponentTreeUI(frame);
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	
	
	
}
