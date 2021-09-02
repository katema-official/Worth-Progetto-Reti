package worth.lagreca.guicomponents;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import worth.lagreca.chat.ClientChatInfo;
import worth.lagreca.chat.ClientChatManager;
import worth.lagreca.client.ClientInfo;
import worth.lagreca.constants.Constants;
import worth.lagreca.guipopup.WorthPPanelProjectDoesntExist;
import worth.lagreca.guipopup.WorthPopup;


public class WorthPanelChat extends WorthPanel{

	private Document source;
	private boolean DEBUG = true;
	private int written_chars;
	private JLabel lblNewLabel_3;
	private JTextArea textArea;
	private JTextArea textArea_1;
	private String project_selected;
	
	public WorthPanelChat(WorthFrame frame) {
		
		project_selected = ClientInfo.nome_progetto_selezionato;
		if(Constants.GLOBALDEBUG && DEBUG) System.out.println("ENTRO NELLA CHAT");
		written_chars = 0;
		
		panel.setBounds(0, 0, 800, 600);
		panel.setLayout(null);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(23, 45, 829, 420);
		panel.add(scrollPane);
		
		Iterator<Entry<String, ClientChatManager>> iterator = ClientChatInfo.mapChatManagers.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, ClientChatManager> current = iterator.next();
			current.getValue().text_area = textArea;
		}
		
		
		JLabel lblNewLabel = new JLabel("Chat del progetto " + ClientInfo.nome_progetto_selezionato);
		lblNewLabel.setBounds(23, 11, 829, 31);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Inserisci qui sotto il messaggio da inviare");
		lblNewLabel_1.setBounds(23, 530, 266, 31);
		panel.add(lblNewLabel_1);
		
		textArea_1 = new JTextArea();
		textArea_1.setEditable(true);
		textArea_1.setLineWrap(true);
		textArea_1.setHighlighter(null);
		textArea_1.addKeyListener((KeyListener) new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void keyPressed(KeyEvent e) {
				update_number_of_chars_inserted();
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		//source: http://www.java2s.com/Tutorial/Java/0260__Swing-Event/ListeningtoJTextFieldEventswithaDocumentListener.htm
	    DocumentListener documentListener = new DocumentListener() {
	      public void changedUpdate(DocumentEvent documentEvent) {
	        printIt(documentEvent);
	      }
	      public void insertUpdate(DocumentEvent documentEvent) {
	        printIt(documentEvent);
	      }
	      public void removeUpdate(DocumentEvent documentEvent) {
	        printIt(documentEvent);
	      }
	      private void printIt(DocumentEvent documentEvent) {
	        DocumentEvent.EventType type = documentEvent.getType();
	        String typeString = null;
	        if (type.equals(DocumentEvent.EventType.CHANGE)) {
	          typeString = "Change";
	        }  else if (type.equals(DocumentEvent.EventType.INSERT)) {
	          typeString = "Insert";
	        }  else if (type.equals(DocumentEvent.EventType.REMOVE)) {
	          typeString = "Remove";
	        }
	        //System.out.print("Type : " + typeString);
	        source = documentEvent.getDocument();
	        written_chars = source.getLength();
//	        if(Constants.GLOBALDEBUG && DEBUG)
//				try {
//					System.out.println("COSA C'E' NEL MIO DOCUMENTO?: " + source.getText(0, source.getLength()));
//				} catch (BadLocationException e) {
//					e.printStackTrace();
//				}
	        update_number_of_chars_inserted();
	      }
	    };
	    textArea_1.getDocument().addDocumentListener(documentListener);
				
		
		JScrollPane scrollPane_1 = new JScrollPane(textArea_1);
		scrollPane_1.setBounds(23, 560, 729, 70);
		panel.add(scrollPane_1);
		
		JButton btnNewButton = new JButton("Invia");
		btnNewButton.setBounds(771, 561, 81, 69);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pulsante per mandare un messaggio in chat (solo se ha lunghezza massimo di 490 caratteri)
				
				//se il progetto non esiste più, torna alla pagina iniziale
				if(!ClientChatInfo.mapChatManagers.containsKey(project_selected)) {
					WorthPopup popup9 = new WorthPopup();
					popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
				}else {
					if(written_chars <= 490 && written_chars > 0) {
						if(Constants.GLOBALDEBUG && DEBUG) System.out.println("Premuto il pulsante invia");
						String s = null;
						try {
							s = source.getText(0, source.getLength());
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
						String name = ClientInfo.nome_utente_loggato + ": ";
						String message = name + s + "\n";
						InetAddress chatgroup;
						DatagramSocket ds;
						try {
							ds = new DatagramSocket();
							chatgroup = InetAddress.getByName(ClientChatInfo.mapChatManagers.get(ClientInfo.nome_progetto_selezionato).multicast_address);
							DatagramPacket dp = new DatagramPacket(message.getBytes(), 0, message.getBytes().length, chatgroup, 4321);
							ds.send(dp);
							//@see ClientChatManager
							if(Constants.GLOBALDEBUG && DEBUG) System.out.println("messaggio inviato sull'indirizzo di multicast " + ClientChatInfo.mapChatManagers.get(ClientInfo.nome_progetto_selezionato).multicast_address);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (SocketException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						textArea_1.setText("");
					}
				}
			}
		});
		panel.add(btnNewButton);
		
		JLabel lblNewLabel_2 = new JLabel("Caratteri usati:");
		lblNewLabel_2.setBounds(580, 530, 200, 24);
		panel.add(lblNewLabel_2);
		
		lblNewLabel_3 = new JLabel("0/490");
		lblNewLabel_3.setBounds(710, 527, 130, 37);
		panel.add(lblNewLabel_3);
		
		JButton btnNewButton_1 = new JButton("Torna al progetto");
		btnNewButton_1.setBounds(630, 476, 221, 50);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//se il progetto non esiste più, torna alla pagina iniziale
				if(!ClientChatInfo.mapChatManagers.containsKey(project_selected)) {
					WorthPopup popup9 = new WorthPopup();
					popup9.setPanel(new WorthPPanelProjectDoesntExist(popup9, frame));
					ClientChatInfo.chat_selected = false;
				}else {
					if(Constants.GLOBALDEBUG && DEBUG) System.out.println("ESCO DALLA CHAT");
					ClientChatInfo.chat_selected = false;
					frame.setPanel(new WorthPanelProjectSelected(frame));
				}
			}
		});
		panel.add(btnNewButton_1);
		
		ArrayList<String> init_msgs = ClientChatInfo.mapChatManagers.get(ClientInfo.nome_progetto_selezionato).list_of_messages;
		Iterator<String> iterator_1 = init_msgs.iterator();
		while(iterator_1.hasNext()) {
			String current = iterator_1.next();
			textArea.append(current);
		}
		
	}
	
	//metodo per aggiornare il counter dei caratteri scritti
	public void update_number_of_chars_inserted() {
		if(written_chars > 490) {
			lblNewLabel_3.setForeground(Color.RED);
		}else {
			lblNewLabel_3.setForeground(Color.BLACK);
		}
		lblNewLabel_3.setText(written_chars + "/490");
	}
	
}
