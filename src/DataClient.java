import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.*;


public class DataClient {
	static JFrame f = new JFrame("Cliente");
	private InetAddress IP;
	String porto;

	public DataClient(String ip, String porto) throws IOException {
		IP = InetAddress.getByName(ip);
		this.porto = porto;

		createWindow();
	}

	public static void main(String[] args) throws IOException {
		new DataClient(args[0], args[1]);
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
		JTextPane textArea = new JTextPane();
		textFieldComprimento.setColumns(12);
		// textArea.setColumns(30);

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
		// JTextPane areaScrollPane= new JTextPane();
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		f.add(areaScrollPane, BorderLayout.CENTER);
		f.setVisible(true);// making the frame visible
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int posicao = 0, comprimento = 0;
				String text1 = textFieldPosition.getText();
				String text2 = textFieldComprimento.getText();
				try {
					posicao = Integer.parseInt(text1);
					comprimento = Integer.parseInt(text2);
				} catch (NumberFormatException err) {
					textArea.setText("Inserir valor numerico");
					return;
				}
				if (text1.isBlank() || text2.isBlank()) {
					textArea.setText("Inserir valores");
					return;
				}
				if (Integer.parseInt(text1) < 0 || Integer.parseInt(text1) > 1000000) {
					textArea.setText("Posicao fora dos limites");
					return;
				}
				if (Integer.parseInt(text2) < 0 || Integer.parseInt(text2) > 1000000) {
					textArea.setText("Comprimento fora dos limites");
					return;
				}
				if (Integer.parseInt(text1) + Integer.parseInt(text2) > 1000000) {
					textArea.setText("Comprimento fora dos limites");
					return;
				}

				CloudByte[] storedData = new CloudByte[comprimento];
				ByteBlockRequest request = new ByteBlockRequest(posicao, comprimento);
				DownloadService downloadservice = new DownloadService(IP, porto, request);
				storedData = downloadservice.download();
				String str = "";
				for (int i = 0; i < request.getLength(); i++) {
					str = str + storedData[i].getValue() + " ";
					System.out.println(Math.round((float)i/(float)comprimento*100)+" %");
				}
				System.out.println("Terminei");
				textArea.setText(str);
			}
		});
	}

	public class DownloadService { // cliente
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private InetAddress ip;
		private String porto;
		private ByteBlockRequest request;
		private Socket socketNode;

		public DownloadService(InetAddress ip, String porto, ByteBlockRequest request) {
			this.ip = ip;
			this.porto = porto;
			this.request = request;
		}

		public CloudByte[] download() {

			CloudByte[] bytes = new CloudByte[request.getLength()];

			try {
				connectToNode();
				out.writeObject(request);
				bytes = (CloudByte[]) in.readObject();
				socketNode.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bytes;

		}

		void connectToNode() throws IOException {
			socketNode = new Socket(this.ip, Integer.parseInt(this.porto));
			this.in = new ObjectInputStream(socketNode.getInputStream());
			this.out = new ObjectOutputStream(socketNode.getOutputStream());
		}
	}

}
