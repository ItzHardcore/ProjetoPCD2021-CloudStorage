import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

public class StorageNode {
	
	public static final int MIN_NUMBER_ANSWER_TO_CORRECT = 2;
	public static final int STORED_DATA_LENGTH = 1000000;
	public static final int INITIAL_DOWNLOAD_BLOCK_LENGTH = 10000;
	private CloudByte[] storedData = new CloudByte[STORED_DATA_LENGTH];
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private String porto;
	private String portoDiretorio;
	private String ficheiro;
	private InetAddress ip;

	
	public StorageNode(String ip,String portoDiretorio,String porto,String ficheiro) throws UnknownHostException {
		this.ip=InetAddress.getByName(ip);
		this.porto = porto;
		this.portoDiretorio = "8080";
		this.ficheiro=ficheiro;
		
		System.out.println("Sending to diretory:"+" INSC "+ip+" "+porto);
		try {
			byte[] fileContents = Files.readAllBytes(new File(ficheiro).toPath());
			for(int i=0;i<fileContents.length;i++)  storedData[i]= new CloudByte(fileContents[i]); 
			System.out.println("Loaded data from file:1000000");
		} catch (IOException e) {e.printStackTrace();}
		
		new DataInjectionErrorThread().start();
	}
	
	void connectToServer() throws IOException {
		socket = new Socket(ip, Integer.parseInt(portoDiretorio));
		in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream())),
				true);
	}
	
	void sendMessages() throws IOException {
		out.println("INSC "+ip+" "+porto);
		System.out.println(in.read());
	}
	
	public void runClient() {
		try {
			connectToServer();
			sendMessages();
		} catch (IOException e) {
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		if(args.length==4) new StorageNode(args[0],args[1],args[2],args[3]).runClient();
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
					System.out.println("Error injected:"+storedData[index]+" Parity NOK");
					storedData[index].makeByteCorrupt();
				}
			}
		}
	}
}
