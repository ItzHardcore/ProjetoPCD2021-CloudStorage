import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

public class StorageNode implements Serializable {

	public static final int COMPRIMENTO_DADOS = 1000000;
	private CloudByte[] storedData = new CloudByte[COMPRIMENTO_DADOS];
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private String porto;
	private String portoDiretorio;
	private String ficheiro;
	private InetAddress ip;
	private Servico servico;
	private List<ByteBlockRequest> requests = Collections.synchronizedList(new ArrayList<>());

	public StorageNode(String ip, String portoDiretorio, String porto, String ficheiro)
			throws UnknownHostException, IOException {
		this.ip = InetAddress.getByName(ip);
		this.porto = porto;
		this.portoDiretorio = "8080";
		this.ficheiro = ficheiro;
		System.out.println("Sending to diretory:" + " INSC " + ip + " " + porto);
		connectToServer();
		registerInServer();
		try {
			byte[] conteudoFicheiro = Files.readAllBytes(new File(ficheiro).toPath());
			for (int i = 0; i < conteudoFicheiro.length; i++)
				storedData[i] = new CloudByte(conteudoFicheiro[i]);
			System.out.println("Loaded data from file:1000000");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Servico(new ServerSocket(Integer.parseInt(porto))).start();
		new DataInjectionErrorThread().start();
	}

	public StorageNode(String ip, String portoDiretorio, String porto) throws UnknownHostException, IOException {
		this.porto = porto;
		this.portoDiretorio = portoDiretorio;
		this.ip = InetAddress.getByName(ip);
		connectToServer();
		registerInServer();
		try {
			List<Node> nodes = getNodes();
			for (int i = 0; i < 10000; i++) {
				requests.add(new ByteBlockRequest(i * 100, 100));
			}
			for (Node node : nodes) {
				new DownloadThread(node.ip, node.porto).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Servico(new ServerSocket(Integer.parseInt(porto))).start();
		new DataInjectionErrorThread().start();

	}

	public class Node {
		InetAddress ip;
		String porto;

		public Node(InetAddress ip, String porto) {
			super();
			this.ip = ip;
			this.porto = porto;
		}
	}

	public synchronized void MergetoFile(ByteBlockRequest request, CloudByte[] array) {
		int y = 0;
		for (int i = request.getStartIndex(); i < request.getStartIndex()+request.getLength(); i++) {
			storedData[i] = array[y];
			y++;
		}
	}

	public List<Node> getNodes() throws IOException {
		ArrayList<Node> nodesList = new ArrayList<>();
		out.println("nodes");
		while (true) {
			String msg = in.readLine();
			if (msg.equals("end"))
				break;
			InetAddress ipNode = null;
			String portoNode = null;
			String[] componentes = msg.split(" ");
			System.out.println("corri");
			ipNode = InetAddress.getByName(componentes[1].substring(1));
			portoNode = componentes[2];
			System.out.println("Ip: " + ipNode + "  Porto: " + portoNode);
			if (!this.porto.contentEquals(portoNode)) {
				nodesList.add(new Node(ipNode, portoNode));
			}
		}
		return nodesList;
	}

	void connectToServer() throws IOException {
		socket = new Socket(ip, Integer.parseInt(portoDiretorio));
		in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream())),
				true);
	}

	void registerInServer() throws IOException {
		out.println("INSC " + ip + " " + porto);
	}

	// public void runClient() {
	// try {
	// connectToServer();
	// registerInServer();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public class DataInjectionErrorThread extends Thread {
		public void run() {
			Scanner s = new Scanner(System.in);
			while (true) {
				String error = s.nextLine();
				String[] str = error.split(" ");
				if (str[0].equals("ERROR") && str.length == 2) {
					int index = Integer.parseInt(str[1]);
					storedData[index].makeByteCorrupt();
					System.out.println("Error injected: " + storedData[index] + " Parity NOK");
				}
			}
		}
	}

	public class DownloadThread extends Thread {
		private ObjectInputStream in;
		private ObjectOutputStream out;
		// private BufferedReader in;
		// private PrintWriter out;
		private InetAddress ip;
		private String porto;
		private ByteBlockRequest request;
		private Socket socketNode;
		private int count;

		public void run() {
			try {
				connectToNode();
				while (!requests.isEmpty()) {
					request = requests.remove(0);
					//System.out.println("retirei request");
					out.writeObject(request);
					CloudByte[] bytes= (CloudByte[]) in.readObject();
					count++;
					MergetoFile(request, bytes);
				}
				System.out.println("Retirei "+count+" blocos do node IP: "+ socketNode.getInetAddress()+" Porto: "+socketNode.getPort());
				socketNode.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public DownloadThread(InetAddress ip, String porto, ByteBlockRequest request) {
			this.ip = ip;
			this.porto = porto;
			this.request = request;
		}

		public DownloadThread(InetAddress ip, String porto) {
			this.ip = ip;
			this.porto = porto;
		}

		void connectToNode() throws IOException {
			socketNode = new Socket(this.ip, Integer.parseInt(this.porto));
			this.in = new ObjectInputStream(socketNode.getInputStream());
			this.out = new ObjectOutputStream(socketNode.getOutputStream());
		}
	}

	public class ResponderNodes extends Thread {
		private Socket clientSocket;
		// private BufferedReader in;
		// private PrintWriter out;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		ByteBlockRequest request;

		public ResponderNodes(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				connectToNode();
				// out.println("ola");
				// System.out.println("mandei");
				while(true){
					request = (ByteBlockRequest) in.readObject();
					// if(request.getLength()==-1)
					// 	break;
					CloudByte[] lista = new CloudByte[request.getLength()];
					for (int i = request.getStartIndex(); i < request.getLength()+request.getStartIndex(); i++) {
						lista[i - request.getStartIndex()] = storedData[i];
					}
					out.writeObject(lista);
				}
				// do {
				// 	request = (ByteBlockRequest) in.readObject();
				// 	CloudByte[] lista = new CloudByte[request.getLength()];
				// 	for (int i = request.getStartIndex(); i < request.getLength()+request.getStartIndex(); i++) {
				// 		lista[i - request.getStartIndex()] = storedData[i];
				// 	}
				// 	out.writeObject(lista);
				// } while (request.getLength() != -1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Terminei de descarregar para o node IP:"+clientSocket.getInetAddress()+" Porto: "+clientSocket.getPort());
			}
		}

		void connectToNode() throws IOException {
			// this.in = new BufferedReader(new InputStreamReader(
			// clientSocket.getInputStream()));
			// this.out = new PrintWriter(new BufferedWriter(
			// new OutputStreamWriter(clientSocket.getOutputStream())),
			// true);
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
		}
	}

	public class Servico extends Thread {
		private ServerSocket serverSocket;

		public Servico(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {
			serve();
		}

		public void serve() {
			System.err.println("Servia iniciar...");
			while (true) {
				try {
					while (true) {
						Socket s = this.serverSocket.accept();
						new ResponderNodes(s).start();
					}
				} catch (IOException e) {
					System.err.println("Erro ao aceitar ligade cliente no diret");
				}
			}
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length == 4)
			new StorageNode(args[0], args[1], args[2], args[3]);
		else if (args.length == 3)
			new StorageNode(args[0], args[1], args[2]);
		else
			System.err.println("Invalid arguments");
	}
}
