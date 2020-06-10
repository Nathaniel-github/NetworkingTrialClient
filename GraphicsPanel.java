import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class GraphicsPanel extends JFrame {
	
	private Player player;
	private Socket client;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private ObjectInputStream objectIn;
	private boolean serverConnection = false;
	private final int MOVELENGTH = 3;
	
	private FileWriter fileWriter;
	
	private JPanel mainPanel = new JPanel() {
		
		@Override
		public void paintComponent(Graphics g) {
			
			g.setColor(Color.WHITE);
			
			if (notConnectedToServer()) {
				
				g.drawString("Connecting to server...", 680, 400);
				
			} else if (serverConnection) {
				
				try {
					
					int numberOfPlayers = dataIn.readInt();
					
					fileWriter.write("Data received(Number of Players): " + numberOfPlayers);
					
					for (int i = 0; i < numberOfPlayers; i ++) {
						
						int x = dataIn.readInt();
						fileWriter.write("Data received(X coordinate), Iteration " + i + ": " + x);
						int y = dataIn.readInt();
						fileWriter.write("Data received(Y coordinate), Iteration " + i + ": " + y);

						g.fillOval(x - 5, y - 5, 10, 10);
						
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} else {
				
				g.fillOval(player.getX() - 5, player.getY() - 5, 10, 10);
				
			}
			
		}
		
	};
	
	private Timer refresh = new Timer(1000/60, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			try {
				if (!notConnectedToServer()) {
					if(dataIn.available() > 0 || !serverConnection) {
						mainPanel.repaint();
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		
	});
	
	private Timer leftMove = new Timer(1000/60, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (player != null) {
				player.shiftX(-MOVELENGTH);
			}
			
		}
		
	});
	
	private Timer rightMove = new Timer(1000/60, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (player != null) {
				player.shiftX(MOVELENGTH);
			}
			
		}
		
	});
	
	private Timer upMove = new Timer(1000/60, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (player != null) {
				player.shiftY(-MOVELENGTH);
			}
			
		}
		
	});
	
	private Timer downMove = new Timer(1000/60, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (player != null) {
				player.shiftY(MOVELENGTH);
			}
			
		}
		
	});

	public GraphicsPanel(String name) {
		
		super(name);
		
		try {
			fileWriter = new FileWriter(new File("ClientLog.txt"), true) {
				
				@Override
				public void write(String str) {
					
					StringBuilder sb = new StringBuilder();
					
					sb.append(str);
					sb.append(" (");
					sb.append(LocalDateTime.now());
					sb.append(")\n");
					
					try {
						super.write(sb.toString());
						this.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			};
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					
					client = new Socket("tcp.ngrok.io", 19611);
					fileWriter.write("Connection to server established. Server ip: " + client.getInetAddress());
					dataOut = new DataOutputStream(client.getOutputStream());
					objectIn = new ObjectInputStream(client.getInputStream());
					dataIn = new DataInputStream(new BufferedInputStream(client.getInputStream()));
				
					player = (Player)objectIn.readObject();
							
					fileWriter.write("Data received: " + player);
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}).start();

		mainPanel.setBackground(Color.BLACK);
		this.setBackground(Color.BLACK);

		setKeyBindings();

		Container c = getContentPane();

		c.add(mainPanel);
		
		refresh.start();
			
	}
	
	private void setKeyBindings() {
		// The action map for the main panel (theoretically this could be for any object
		// but I chose this
		// one)
		ActionMap actionMap = mainPanel.getActionMap();

		// The input map
		InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		// Assigns the key strokes with a "key"
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left_pressed");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right_pressed");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "left_released");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "right_released");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up_pressed");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "up_released");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down_pressed");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "down_released");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "s_pressed");

		// Assigns the "key" to an action
		actionMap.put("left_pressed", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("left");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				leftMove.start();

			}
		});

		actionMap.put("left_released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("left_released");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				leftMove.stop();
				
			}
		});

		actionMap.put("right_pressed", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("right");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				rightMove.start();

			}
		});

		actionMap.put("right_released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("right_released");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				rightMove.stop();
				
			}
		});

		actionMap.put("up_pressed", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("up");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				upMove.start();

			}
		});

		actionMap.put("up_released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("up_released");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				upMove.stop();

			}
		});
		
		actionMap.put("down_pressed", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("down");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				downMove.start();

			}
		});

		actionMap.put("down_released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				if (serverConnection) {
					try {
						dataOut.writeUTF("down_released");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				downMove.stop();

			}
		});
		
		actionMap.put("s_pressed", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent actionEvt) {

				serverConnection = !serverConnection;
				try {
					dataOut.writeUTF(Arrays.toString(player.getData()));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

	}
	
	private boolean notConnectedToServer() {
		
		return player == null || client == null || dataIn == null || dataOut == null || objectIn == null;
		
	}
	
}
