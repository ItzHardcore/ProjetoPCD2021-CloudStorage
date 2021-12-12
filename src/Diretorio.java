

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Diretorio {
    private ServerSocket serverSocket;
    private ArrayList<Node> array = new ArrayList<Node>();

    public class Node {
		InetAddress ip;
		int porto;

		public Node(InetAddress ip, int porto) {
			super();
			this.ip = ip;
			this.porto = porto;
		}
	}

    public class Trata extends Thread {
        private Socket socketCliente;
        private InetAddress endereco;
        private int porto;

        public Trata(Socket socketCliente) {
            this.socketCliente = socketCliente;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        this.socketCliente.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(this.socketCliente.getOutputStream())), true);
                String msgInicial = in.readLine();
                String[] componentesMensagem = msgInicial.split(" ");
                if (componentesMensagem.length < 2 || !componentesMensagem[0].equals("INSC")) {
                    System.err.println("Não recebi incricao: " + msgInicial);
                    return;
                }
                endereco = InetAddress.getByName(componentesMensagem[1].substring(componentesMensagem[1].indexOf("/") + 1));
                porto = Integer.parseInt(componentesMensagem[2]);

                if(array.size()==0){
                    RegistaCliente(endereco, porto);
                    System.err.println("Cliente inscrito:" + this.socketCliente.getInetAddress().getHostAddress() + " "
                            + porto);
                }else{
                    for(int i=0;i<array.size();i++){
                        
                        if(array.get(i).ip.equals(endereco) && array.get(i).porto == porto){
                            System.err.println("Node ja existe, impossivel criar");
                            return;
                        }
                    }
                    RegistaCliente(endereco, porto);
                        System.err.println("Cliente inscrito:" + this.socketCliente.getInetAddress().getHostAddress() + " "
                            + porto);
                }
                
                while (true) {
                    String msg = in.readLine();
                    
                    System.err.println("Msg Recebida: " + msg);
                    switch (msg.hashCode()) {
                        case 104993457:
                            if (!msg.equals("nodes"))
                                break;
                            TrataCliente(out);
                            continue;
                    }
                    System.err.println("Msg " + msg);
                }
            } catch (IOException e) {
                //for(int i=0;i<array.size();i++)
                //    System.out.println(array.get(i));
                System.out.println("Node com o IP "+endereco+" e Porto "+porto+" removido");
                LogoutCliente(this.endereco, porto);
                //for(int i=0;i<array.size();i++)
                    //System.out.println(array.get(i));
                //System.err.println("Erro ao inicializar canais de comunicacao com o cliente.");
                return;
            }
        }

        private void TrataCliente(PrintWriter out) {
            for (int i = 0; i != array.size(); i++)
                out.println("node " + array.get(i).ip + " "+ array.get(i).porto);
            out.println("end");
        }

        private void RegistaCliente(InetAddress endereco, int porto) {
            array.add(new Node(endereco, porto));
        }

        private void LogoutCliente(InetAddress endereco, int porto) {
            for(int i=0;i<array.size();i++)
                if(array.get(i).ip.equals(endereco) && array.get(i).porto == porto){
                    array.remove(i);
                    try {
                        socketCliente.close();
                    } catch (IOException e) {
                        System.out.println("Não foi possivel encerrrar a ligação");
                    }
                }
        }
    }

    public void serve() {
        System.err.println("Diretorio a iniciar...");
        while (true) {
            try {
                while (true) {
                    Socket s = this.serverSocket.accept();
                    (new Trata(s)).start();
                }
            } catch (IOException e) {
                System.err.println("Erro");
            }
        }
    }

    public Diretorio(int porto) throws IOException {
        this.serverSocket = new ServerSocket(porto);
    }

    public static void main(String[] args) {

        if (args.length == 1)
            try {
                new Diretorio(Integer.parseInt(args[0])).serve();
            } catch (NumberFormatException e) {
                System.out.println("Insira um numero inteiro");
            } catch (IOException e) {
                System.out.println("Erro no numero do porto");
            }
        else
			System.err.println("Erro no numero de argumentos");
    }
}