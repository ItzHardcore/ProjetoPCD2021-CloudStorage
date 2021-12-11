import java.awt.BorderLayout;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.awt.FlowLayout;

import javax.swing.*;

public class DataClient {
	static JFrame f = new JFrame("Cliente");
	InetAddress ip;
	String porto;

	
	public DataClient(InetAddress ip, String porto) {
		this.ip = ip;
		this.porto = porto;
		createWindow();
	}

	public static void main(String[] args) throws UnknownHostException {
		new DataClient(InetAddress.getByName(args[0]), args[1]);
	}

	public void createWindow() {
		f.setSize(700, 200);
		f.setLayout(new BorderLayout());
		f.setName("Client");
		JPanel top = new JPanel();
		JLabel labelPosition = new JLabel("Posição a consultar");
		JTextField textFieldPosition = new JTextField();
		textFieldPosition.setColumns(12);
		JLabel labelComprimento = new JLabel("Comprimento");
		JTextField textFieldComprimento = new JTextField();
		JTextArea textArea = new JTextArea();
		textFieldComprimento.setColumns(12);
		textArea.setColumns(30);
		textArea.setText("As respostas aparecerão aqui...");
		JButton b = new JButton("Consultar");// creating instance of JButton
		b.setBounds(130, 100, 100, 40);// x axis, y axis, width, height
		top.add(labelPosition);
		top.add(textFieldPosition);
		top.add(labelComprimento);
		top.add(textFieldComprimento);
		top.add(b);
		f.add(top, BorderLayout.NORTH);
		f.add(textArea, BorderLayout.CENTER);
		f.setVisible(true);// making the frame visible

		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int posicao = 0, comprimento = 0;
				String text1 = textFieldPosition.getText();
				String text2 = textFieldComprimento.getText();
				if (text1.isBlank() || text2.isBlank()) {
					textArea.setText("Inserir valores");
					return;
				}
				try {
					posicao = Integer.parseInt(text1);
					comprimento = Integer.parseInt(text2);
				} catch (NumberFormatException err) {
					textArea.setText("Inserir valor numérico");
					return;
				}
				if(posicao+comprimento>1000000){
					textArea.setText("Excede o tamanho máximo");
					return;
				}

				CloudByte[] valores= new DownloadThread(ip, porto, new ByteBlockRequest(posicao, comprimento)).getData();
				String str="";
				for (CloudByte cloudByte : valores) {
					str+=cloudByte.getValue();
				}
				textArea.setText(str);

			}
		});
	}

	public class DownloadThread {
		private ObjectInputStream in;
		private ObjectOutputStream out;
		// private BufferedReader in;
		// private PrintWriter out;
		private InetAddress ip;
		private String porto;
		private ByteBlockRequest request;
		private Socket socketNode;
		private int count;

		public CloudByte[] getData() {
			try {
				connectToNode();
				out.writeObject(request);
				CloudByte[] bytes = (CloudByte[]) in.readObject();
				count++;
				System.out.println("Retirei " + count + " blocos do node IP: " + socketNode.getInetAddress()
						+ " Porto: " + socketNode.getPort());
				socketNode.close();
				return bytes;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		public DownloadThread(InetAddress ip, String porto, ByteBlockRequest request) {
			this.ip = ip;
			this.porto = porto;
			this.request = request;
		}

		void connectToNode() throws IOException {
			socketNode = new Socket(this.ip, Integer.parseInt(this.porto));
			this.in = new ObjectInputStream(socketNode.getInputStream());
			this.out = new ObjectOutputStream(socketNode.getOutputStream());
		}
	}

}
