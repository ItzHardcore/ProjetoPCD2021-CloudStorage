import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
<<<<<<< HEAD
import java.net.UnknownHostException;
>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.awt.FlowLayout;

import javax.swing.*;
<<<<<<< HEAD
import javax.xml.crypto.Data;

public class DataClient {
	static JFrame f=new JFrame("Cliente");
	private InetAddress IP;
	String porto;

	public DataClient(String ip, String porto) throws IOException {
		IP = InetAddress.getByName(ip);
		this.porto = porto;

		createWindow();
	}

	public static void main(String[] args) throws IOException  { 
		new DataClient(args[0], args[1]);
	}  
	
>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549
	public void createWindow() {
		f.setSize(700, 200);
		f.setLayout(new BorderLayout());
		f.setName("Client");
<<<<<<< HEAD
		JPanel top = new JPanel();
		JLabel labelPosition = new JLabel("Posição a consultar");
		JTextField textFieldPosition = new JTextField();
>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549
		textFieldPosition.setColumns(12);
		JLabel labelComprimento = new JLabel("Comprimento");
		JTextField textFieldComprimento = new JTextField();
		JTextArea textArea = new JTextArea();
		textFieldComprimento.setColumns(12);
		textArea.setColumns(30);
<<<<<<< HEAD
		textArea.setText("As respostas aparecerão aqui...");
		JButton b = new JButton("Consultar");// creating instance of JButton
		b.setBounds(130, 100, 100, 40);// x axis, y axis, width, height
>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549
		top.add(labelPosition);
		top.add(textFieldPosition);
		top.add(labelComprimento);
		top.add(textFieldComprimento);
		top.add(b);
<<<<<<< HEAD
		f.add(top,BorderLayout.NORTH);
		f.add(textArea,BorderLayout.CENTER);
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		f.add(areaScrollPane,BorderLayout.CENTER);
		f.setVisible(true);//making the frame visible 
		
		
		
		

>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549

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
				if(Integer.parseInt(text1)<0 || Integer.parseInt(text1)>1000000){
					textArea.setText("Posicao fora dos limites");
					return;
				}
				if(Integer.parseInt(text2)<0 || Integer.parseInt(text2)>1000000){
					textArea.setText("Comprimento fora dos limites");
					return;
				}
				if(Integer.parseInt(text1)+Integer.parseInt(text2)>1000000){
					textArea.setText("Comprimento fora dos limites");
					return;
				}
				try {
					posicao=Integer.parseInt(text1);
					comprimento= Integer.parseInt(text2);
				}catch (NumberFormatException err){
					textArea.setText("Inserir valor numerico");
					return;
				}
				
				CloudByte[] storedData = new CloudByte[comprimento];
				ByteBlockRequest request = new ByteBlockRequest(posicao, comprimento);
				DownloadService downloadservice = new DownloadService(IP,porto,request);
				storedData = downloadservice.download();
				String str = "";
				for(int i = 0;i<request.getLength();i++){
					System.out.println("escrevendo...");
					str = str + storedData[i].getValue() + " ";
				}
					
>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549
				textArea.setText(str);

			}
		});
	}
	
	public class DownloadService {  //cliente
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
					System.out.println("entrei");
					out.writeObject(request);
					bytes = (CloudByte[]) in.readObject();
				socketNode.close();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bytes;
>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549
		}

		void connectToNode() throws IOException {
			socketNode = new Socket(this.ip, Integer.parseInt(this.porto));
			this.in = new ObjectInputStream(socketNode.getInputStream());
			this.out = new ObjectOutputStream(socketNode.getOutputStream());
<<<<<<< HEAD
			System.err.println("Conectado ao node com o IP: "+socketNode.getInetAddress()+" no porto: "+socketNode.getPort());
		}
	}
	
	
>>>>>>> 7326d950deada6bbc3a3e749a44dc39d7ceee549
}
