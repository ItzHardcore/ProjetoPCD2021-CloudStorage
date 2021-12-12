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
import java.util.concurrent.CountDownLatch;
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
	private InetAddress ip;
	private List<ByteBlockRequest> requests = Collections.synchronizedList(new ArrayList<>());

	public StorageNode(String ip, String portoDiretorio, String porto, String ficheiro)
			throws UnknownHostException, IOException {
		try {
			Integer.parseInt(porto);
			Integer.parseInt(portoDiretorio);
			InetAddress.getByName(ip);
		} catch (Exception e) {
			System.err.println("Valores inseridos errados");
			return;
		}
		this.ip = InetAddress.getByName(ip);
		this.porto = porto;
		this.portoDiretorio = "8080";
		System.out.println("Conectado ao diretório com IP " + ip + " e Porto " + porto);
		connectToServer();
		registerInServer();
		try {
			byte[] conteudoFicheiro = Files.readAllBytes(new File(ficheiro).toPath());
			for (int i = 0; i < conteudoFicheiro.length; i++)
				storedData[i] = new CloudByte(conteudoFicheiro[i]);
			System.out.println("O ficheiro local foi carregado com sucesso");
		} catch (IOException e) {
			System.err.println("Erro ao importar o Ficheiro");
			return;
		}
		try {
			new Servico(new ServerSocket(Integer.parseInt(porto))).start();
		} catch (Exception e) {
			System.err.println("Valores de IP e porto já utilizados, tente novamente");
			return;
		}
		new DataInjectionErrorThread().start();
		new DetetorDeErros().start();
	}

	public StorageNode(String ip, String portoDiretorio, String porto) throws UnknownHostException, IOException {
		try {
			Integer.parseInt(porto);
			Integer.parseInt(portoDiretorio);
			InetAddress.getByName(ip);
		} catch (Exception e) {
			System.err.println("Valores inseridos errados");
			return;
		}
		this.porto = porto;
		this.portoDiretorio = portoDiretorio;
		this.ip = InetAddress.getByName(ip);
		connectToServer();
		registerInServer();
		try {
			List<Node> nodes = getNodes();
			if (nodes.size() < 1) {
				System.err.println("Não existem nodes com ficheiro disponível");
				return;
			}
			CountDownLatch latch = new CountDownLatch(nodes.size());
			for (int i = 0; i < 10000; i++) {
				requests.add(new ByteBlockRequest(i * 100, 100));
			}
			for (Node node : nodes) {
				new DownloadThread(node.ip, node.porto, latch).start();
			}
			latch.await();
			System.out.println("O ficheiro foi carregado com sucesso");
		} catch (IOException e) {
			System.err.println("Erro ao importar o Ficheiro");
			return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.err.println("Erro nas thread de descarregar");
		}
		try {
			new Servico(new ServerSocket(Integer.parseInt(porto))).start();
		} catch (Exception e) {
			System.err.println("Valores de IP e porto já utilizados, tente novamente");
			return;
		}
		new DataInjectionErrorThread().start();
		new DetetorDeErros().start();
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

	public List<Node> getNodes() throws IOException {
		ArrayList<Node> nodesList = new ArrayList<>();
		out.println("nodes");
		while (true) {
			String msg = in.readLine();
			if (msg.equalsIgnoreCase("end"))
				break;
			InetAddress ipNode = null;
			String portoNode = null;
			String[] componentes = msg.split(" ");
			ipNode = InetAddress.getByName(componentes[1].substring(componentes[1].indexOf("/") + 1));
			portoNode = componentes[2];
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

	public class DataInjectionErrorThread extends Thread {
		public void run() {
			Scanner s = new Scanner(System.in);
			while (true) {
				String error = s.nextLine();
				String[] str = error.split(" ");
				if (str[0].equals("ERROR") && str.length == 2) {
					int index = 0;
					try {
						index = Integer.parseInt(str[1]);
						System.out.println(
								"Injetando erro no byte : " + storedData[index].value + " na posição: " + index);
						storedData[index].makeByteCorrupt();
						System.out.println(
								"Novo valor do byte corrompido: " + storedData[index].value + " na posição: " + index);
					} catch (Exception e) {
						System.err.println("Inserir valor numérico");
					}
				}
			}
		}
	}

	public class DetetorDeErros extends Thread {
		public void run() {
			while (true) {
				try {
					for (int i = 0; i < storedData.length; i++) {
						if (!storedData[i].isParityOk()) {
							System.out.println("Detetei erro na posicao " + i + " no " + storedData[i]);
							corrigirErro(i);
						}
					}
				} catch (Exception e) {
					System.err.println("A aguardar 10 sec por um novo node");
					try {
						sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public synchronized void corrigirErro(int posicao) throws Exception {
		CountDownLatch latch = new CountDownLatch(2);
		List<Node> nodes = getNodes();
		if (nodes.size() < 1)
			throw new Exception();
		if (nodes.size() < 2)
			latch = new CountDownLatch(1);
		ByteBlockRequest request = new ByteBlockRequest(posicao, 1);
		for (Node node : nodes) {
			new DownloadThread(node.ip, node.porto, request, latch).start();
		}
		latch.await();
		System.out.println("Erro corrigido na posição " + posicao + " para " + storedData[posicao]);
	}

	public class DownloadThread extends Thread {
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private InetAddress ip;
		private String porto;
		private ByteBlockRequest request;
		private Socket socketNode;
		private int count;
		private CountDownLatch latch;

		public DownloadThread(InetAddress ip, String porto, ByteBlockRequest request, CountDownLatch latch) {
			this.ip = ip;
			this.porto = porto;
			this.request = request;
			this.latch = latch;
		}

		public DownloadThread(InetAddress ip, String porto) {
			this.ip = ip;
			this.porto = porto;
			this.latch = new CountDownLatch(0);
		}

		public DownloadThread(InetAddress ip, String porto, CountDownLatch latch) {
			this.ip = ip;
			this.porto = porto;
			this.latch = latch;
		}

		public void run() {
			try {
				connectToNode();
				do {
					if (!requests.isEmpty())
						request = requests.remove(0);
					out.writeObject(request);
					CloudByte[] bytes = (CloudByte[]) in.readObject();
					count++;
					MergetoFile(request, bytes);
				} while (!requests.isEmpty());
				System.out.println("Retirei " + count + " blocos do node IP: " + socketNode.getInetAddress()
						+ " Porto: " + socketNode.getPort());
				socketNode.close();
				latch.countDown();
			} catch (Exception e) {
			}
		}

		void connectToNode() throws IOException {
			socketNode = new Socket(this.ip, Integer.parseInt(this.porto));
			this.in = new ObjectInputStream(socketNode.getInputStream());
			this.out = new ObjectOutputStream(socketNode.getOutputStream());
			System.err.println("Conectado ao node com o IP: " + socketNode.getInetAddress() + " no porto: "
					+ socketNode.getPort());
		}

		public synchronized void MergetoFile(ByteBlockRequest request, CloudByte[] array) {
			int y = 0;
			for (int i = request.getStartIndex(); i < request.getStartIndex() + request.getLength(); i++) {
				storedData[i] = array[y];
				y++;
			}
		}
	}

	public class ResponderNodes extends Thread { // SERVO
		private Socket clientSocket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		ByteBlockRequest request;

		public ResponderNodes(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				connectToNode();
				while (true) {
					request = (ByteBlockRequest) in.readObject();
					if (request.getLength() == -1) {
						break;
					}
					CloudByte[] lista = new CloudByte[request.getLength()];
					for (int i = request.getStartIndex(); i < request.getLength() + request.getStartIndex(); i++) {
						if (!storedData[i].isParityOk())
							corrigirErro(i);
						lista[i - request.getStartIndex()] = storedData[i];
					}
					out.writeObject(lista);
				}
			} catch (Exception e) {
			}
			System.out.println("Terminei de descarregar para o node IP:" + clientSocket.getInetAddress()
					+ " Porto: " + clientSocket.getPort());
		}

		void connectToNode() throws IOException {
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
		}
	}

	public class Servico extends Thread { // cria o numero de threash consoante o numero de nodes, para responder aos
											// pedidos
		private ServerSocket serverSocket;

		public Servico(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {
			serve();
		}

		public void serve() {
			while (true) {
				try {
					while (true) {
						Socket s = this.serverSocket.accept();
						new ResponderNodes(s).start();
					}
				} catch (IOException e) {
					System.err.println("Erro ao aceitar ligação de um node");
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
			System.err.println("Número de argumentos invalidos");
	}

}
