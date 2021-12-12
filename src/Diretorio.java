

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLSyntaxErrorException;
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

    public class TrataCliente extends Thread {
        private Socket socketCliente;

        public TrataCliente(Socket socketCliente) {
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
                    System.err.println("Erro ao receber inscricao do cliente: mensagem inv" + msgInicial);
                    return;
                }
                InetAddress endereco = InetAddress.getByName(componentesMensagem[1].substring(componentesMensagem[1].indexOf("/") + 1));
                int portoCliente = Integer.parseInt(componentesMensagem[2]);

                if(array.size()==0){
                    registaCliente(endereco, portoCliente);
                    System.err.println("Cliente inscrito:" + this.socketCliente.getInetAddress().getHostAddress() + " "
                            + portoCliente);
                }else{
                    for(int i=0;i<array.size();i++){
                        
                        if(array.get(i).ip.equals(endereco) && array.get(i).porto == portoCliente){
                            System.err.println("Node ja existe, impossivel criar");
                            return;
                        }
                    }
                    registaCliente(endereco, portoCliente);
                        System.err.println("Cliente inscrito:" + this.socketCliente.getInetAddress().getHostAddress() + " "
                            + portoCliente);
                }
                
                while (true) {
                    String msg = in.readLine();
                    System.err.println("Mensagem recebida: " + msg);
                    String str1;
                    switch ((str1 = msg).hashCode()) {
                        case 104993457:
                            if (!str1.equals("nodes"))
                                break;
                            trataConsultaClientes(out);
                            continue;
                    }
                    System.err.println("Mensagem de cliente inv" + msg);
                }
                
            } catch (IOException e) {
                //logoutCliente(endereco, portoCliente);
                System.err.println("Erro ao inicializar canais de comunicacao com o cliente.");
                
            }
        }

        private void trataConsultaClientes(PrintWriter out) {
            for (int i = 0; i != array.size(); i++)
                out.println("node " + array.get(i).ip + " "+ array.get(i).porto);
            out.println("end");
        }

        private void registaCliente(InetAddress endereco, int portoCliente) {
                array.add(new Node(endereco, portoCliente));
        }

        private void logoutCliente(InetAddress endereco, int portoCliente) {
                array.remove(new Node(endereco, portoCliente));
        }
    }

    public void serve() {
        System.err.println("Diretorio a iniciar...");
        while (true) {
            try {
                while (true) {
                    Socket s = this.serverSocket.accept();
                    (new TrataCliente(s)).start();
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
        if (args.length != 1)
            throw new RuntimeException("Porto deve ser dado como argumento.");
        try {
            (new Diretorio(Integer.parseInt(args[0]))).serve();
        } catch (NumberFormatException e) {
            throw new RuntimeException("Insira um numero inteiro");
        } catch (IOException e) {
            throw new RuntimeException("Erro no numero do porto " + Integer.parseInt(args[0]));
        }
    }
}