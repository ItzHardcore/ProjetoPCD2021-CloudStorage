import java.io.IOException;
		import java.io.InputStreamReader;
		import java.io.OutputStreamWriter;
		import java.io.PrintWriter;
		import java.net.InetAddress;
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
	private ArrayList<Node> nodesList;


	public StorageNode(String ip,String portoDiretorio,String porto,String ficheiro) throws UnknownHostException {
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

		new DataInjectionErrorThread().start();
	}
	public StorageNode(String porto, String portoDiretorio, InetAddress ip) {
		this.porto = porto;
		this.portoDiretorio = portoDiretorio;
		this.ip = ip;

		try {
			//getnodes
			//cria o array de pedidos

			// creating object of List<String>
			List<String> list = new ArrayList<String>();

			// populate the list
			list.add("A");
			list.add("B");
			list.add("C");
			list.add("D");
			list.add("E");

			// printing the Collection
			System.out.println("List : " + list);

			// create a synchronized list
			List<String> synlist = Collections
					.synchronizedList(list);

			// printing the Collection
			System.out.println("Synchronized list is : " + synlist);


			byte[] fileContents = Files.readAllBytes(new File(ficheiro).toPath());
			for(int i=0;i<fileContents.length;i++)  storedData[i]= new CloudByte(fileContents[i]);
			System.out.println("Loaded data from file:1000000");
		} catch (IOException e) {e.printStackTrace();}

		new DataInjectionErrorThread().start();
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

	public StorageNode(String ip,String portoDiretorio,String porto) throws UnknownHostException {
		this.ip=InetAddress.getByName(ip);
		this.porto = porto;
		this.portoDiretorio = "8080";
		//apanhar byte
		System.out.println("Sending to diretory:"+" INSC "+ip+" "+porto);
		try {
			byte[] conteudoFicheiro = Files.readAllBytes(new File(ficheiro).toPath());
			for(int i=0;i<conteudoFicheiro.length;i++)  storedData[i]= new CloudByte(conteudoFicheiro[i]);
			System.out.println("Loaded data from file:1000000");
		} catch (IOException e) {
			e.printStackTrace();
		}

		new DataInjectionErrorThread().start();
	}

	public void getNodes() throws IOException {
		System.out.println("getnodes");
		out.println("nodes");
		while(true) {
			String msg =in.readLine();
			if(msg.equals("END"))
				break;
			InetAddress ip;
			String porto;
			String[] componentes = msg.split(" ");
			ip= InetAddress.getByName(componentes[1].substring(1));
			porto= componentes[2];
			nodesList.add(new Node(ip, porto));
		}
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

	public void runClient() {
		try {
			connectToServer();
			registerInServer();
			getNodes();
			System.out.println("acabei getnodes");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {

			}
		}
	}

	public static void main(String[] args) throws UnknownHostException {
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
}

