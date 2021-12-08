import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
		import java.io.PrintWriter;
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

public class StorageNode {

	public static final int COMPRIMENTO_DADOS = 1000000;
	private CloudByte[] storedData = new CloudByte[COMPRIMENTO_DADOS];
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private String porto;
	private String portoDiretorio;
	private String ficheiro;
	private InetAddress ip;
	private ServerSocket serverSocket;

	public StorageNode(String ip,String portoDiretorio,String porto,String ficheiro) throws UnknownHostException,IOException {
		this.ip=InetAddress.getByName(ip);
		this.porto = porto;
		this.portoDiretorio = "8080";
		this.ficheiro=ficheiro;
		System.out.println("Sending to diretory:"+" INSC "+ip+" "+porto);
		try {
			byte[] conteudoFicheiro = Files.readAllBytes(new File(ficheiro).toPath());
			for(int i=0;i<conteudoFicheiro.length;i++)  storedData[i]= new CloudByte(conteudoFicheiro[i]);
			System.out.println("Loaded data from file:1000000");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.serverSocket = new ServerSocket(Integer.parseInt(porto));
		new DataInjectionErrorThread().start();
		serve();
	}
	public StorageNode(String ip, String portoDiretorio, String porto) throws UnknownHostException,IOException {
		this.porto = porto;
		this.portoDiretorio = portoDiretorio;
		this.ip = InetAddress.getByName(ip);

		try {
			List<Node> nodes= getNodes();
			// creating object of List<String>
			List<ByteBlockRequest> list = new ArrayList<ByteBlockRequest>();

			// populate the list
			for(int i=0;i<10000;i++){
				list.add(new ByteBlockRequest(i*100,100));
			}

			// create a synchronized list
			List<ByteBlockRequest> synlist = Collections.synchronizedList(list);


		} catch (IOException e) {e.printStackTrace();}
		this.serverSocket = new ServerSocket(Integer.parseInt(porto));
		new DataInjectionErrorThread().start();
		serve();
	}

	public class Node{
		InetAddress ip;
		String porto;

		public Node(InetAddress ip, String porto) {
			super();
			this.ip = ip;
			this.porto = porto;
		}
	}
	public void MergetoFile(ByteBlockRequest request, CloudByte[] array){
		int y=0;
		for(int i=request.getStartIndex();i<100;i++) {
			storedData[i] = array[y];
			y++;
		}
	}

	public List<Node> getNodes() throws IOException {
		ArrayList<Node> nodesList=new ArrayList<>();		
		System.out.println("getnodes");
		out.println("nodes");
		while(true) {
			String msg =in.readLine();
			if(msg.equals("end"))
				break;
			InetAddress ipNode=null;
			String portoNode=null;
			String[] componentes = msg.split(" ");
			System.out.println("corri");
			ipNode= InetAddress.getByName(componentes[1].substring(1));
			portoNode= componentes[2];
			System.out.println("Ip: "+ipNode+"  Porto: "+portoNode);
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
		out.println("INSC "+ip+" "+porto);
		//System.out.println(in.read());
	}

	public void serve() {
		System.err.println("Servia iniciar...");
		while (true) {
		  try {
			while (true) {
			  Socket s = this.serverSocket.accept();
			  (new ResponderNodes(s)).start();
			} 
		  } catch (IOException e) {
			System.err.println("Erro ao aceitar ligade cliente no diret");
		  } 
		} 
	  }

	public void runClient() {
		try {
			connectToServer();
			registerInServer();
			getNodes();
			new DownloadThread(InetAddress.getByName("127.0.0.1"), "8081").start();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {

			}
		}
	}

	public static void main(String[] args) throws UnknownHostException,IOException {
		if(args.length==4) new StorageNode(args[0],args[1],args[2],args[3]).runClient();
		else if(args.length==3) new StorageNode(args[0],args[1],args[2]).runClient();
		else System.err.println("Invalid arguments");
	}

	public class DataInjectionErrorThread extends Thread{
		public void run() {
			Scanner s= new Scanner(System.in);
			while(true) {
				String error = s.nextLine();
				String[] str=error.split(" ");
				if(str[0].equals("ERROR") && str.length==2) {
					int index= Integer.parseInt(str[1]);
					storedData[index].makeByteCorrupt();
					System.out.println("Error injected: "+storedData[index]+" Parity NOK");
				}
			}
		}
	}

	public class DownloadThread extends Thread{
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private InetAddress ip;
		private String porto;

		public void run() {
			try {
				System.out.println("comecei contacto");
				connectToNode();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		public DownloadThread(InetAddress ip, String porto) {
			this.ip = ip;
			this.porto = porto;
		}

		void connectToNode() throws IOException {
			socket = new Socket(ip, Integer.parseInt(porto));
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		}		
	}

	public class ResponderNodes extends Thread {
		private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ResponderNodes(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
				out = new ObjectOutputStream(clientSocket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
			  	System.out.println("conectado");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}           
    	}
	}


}

