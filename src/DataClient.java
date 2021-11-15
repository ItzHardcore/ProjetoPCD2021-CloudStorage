import java.awt.BorderLayout;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.awt.FlowLayout;

import javax.swing.*; 
public class DataClient {
	static JFrame f=new JFrame("Cliente");
	public static void main(String[] args) {  
		createWindow();
	}  
	
	public static void createWindow() {
		f.setSize(700, 200);
		f.setLayout(new BorderLayout());
		f.setName("Client");
		JPanel top=new JPanel();
		JLabel labelPosição= new JLabel("Posição a consultar");
		JTextField textFieldPosição=new JTextField();
		textFieldPosição.setColumns(12);
		JLabel labelComprimento= new JLabel("Comprimento");
		JTextField textFieldComprimento=new JTextField();
		JTextArea textArea=new JTextArea();
		textFieldComprimento.setColumns(12);
		textArea.setColumns(30);
		textArea.setText("As respostas aparecerão aqui...");
		JButton b = new JButton("Consultar");//creating instance of JButton
		b.setBounds(130,100,100, 40);//x axis, y axis, width, height  
		top.add(labelPosição);
		top.add(textFieldPosição);
		top.add(labelComprimento);
		top.add(textFieldComprimento);
		top.add(b);
		f.add(top,BorderLayout.NORTH);
		f.add(textArea,BorderLayout.CENTER);
		f.setVisible(true);//making the frame visible 
		
		
		b.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int posicao=0,comprimento = 0;
				String text1 = textFieldPosição.getText();
				String text2= textFieldComprimento.getText();
				if(text1.isBlank() || text2.isBlank()) {
					textArea.setText("Inserir valores");
					return;
				}
				try {
					posicao=Integer.parseInt(text1);
					comprimento= Integer.parseInt(text2);
				}catch (NumberFormatException err){
					textArea.setText("Inserir valor numérico");
					return;
				}	
				String str= "Posição: " + posicao + " Comprimento: "+ comprimento;
				textArea.setText(str);
				
			}
		});
	}
	

	
	
}
