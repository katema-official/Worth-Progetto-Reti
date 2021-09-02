package worth.lagreca.guicomponents;

import javax.swing.JPanel;

public abstract class WorthPanel {
	//classe astratta che rappresenta un panel presente sul frame principale di WORTH. questa classe
	//contiene due metodi che dovranno essere comuni a tutte le classi che la estenderanno: uno costruttore
	//per istanziare il JPanel, e uno per prenderlo.
	
	protected final JPanel panel;
	
	public WorthPanel() {
		panel = new JPanel();
	}

	public JPanel getActualPanel() {
		return panel;
	}
}
