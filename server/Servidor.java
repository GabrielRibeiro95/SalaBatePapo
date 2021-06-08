package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Servidor extends Thread {

	private static int numCl=0;
	private static ArrayList<BufferedWriter> clientes;
    private static ServerSocket server;
    private String nome;
    private Socket con;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;
    private BufferedWriter bfw;
    private static JFrame sd=new JFrame("Server");
    private static JButton shut=new JButton("Shut Down");
    
    //Construtor
    public Servidor(Socket con){
        this.con = con;
        try {
            in  = con.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Inicio da thread
    public void run(){
        try {
          	String msg;
        	OutputStream ou =  this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            if (numCl<10) {
	            clientes.add(bfw);
	            numCl++;
	            nome = msg = bfr.readLine();  
	            warnConnect(bfw);
	            while(msg!=null) {
	                msg = bfr.readLine();
	                sendToAll(bfw, msg);
	                if (msg!=null) {
	                	if (!msg.isEmpty()) {
	                		System.out.println(msg);
	                	}
	                }
	            }
	            warnDisconnect(bfw);
        	} else {
        		warnFull(bfw);
        	}
        } catch (IndexOutOfBoundsException e) {
        	JOptionPane.showMessageDialog(null, "Sala cheia");
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
     
    //Envia mensagem para todos os usuários
    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException {
        BufferedWriter bwS;
        for(BufferedWriter bw : clientes){
            bwS = (BufferedWriter)bw;
            if (bw!=null) {
	            if(!(bwSaida == bwS)){
	            	if (msg!=null) {
	            		bw.write(nome+": "+msg+"\r\n");
	            		try {
	            			bw.flush();
	            		} catch(Exception e) {
	            			System.out.println("Erro");
	            		}
	            	}
	            }
            }
        }
    }
    
    //Avisa entrada
    public void warnConnect(BufferedWriter bwSaida) throws  IOException {
        BufferedWriter bwS;
        for(BufferedWriter bw : clientes){
            bwS = (BufferedWriter)bw;
            if(!(bwSaida == bwS)){
            	bw.write(nome+" entrou\r\n");
            	try {
            		bw.flush();
            	} catch (Exception e) {
            		
            	}
            }
        }
    }
    
    //Avisa saída
    public void warnDisconnect(BufferedWriter bwSaida) throws  IOException {
        BufferedWriter bwS;
        for(BufferedWriter bw : clientes){
            bwS = (BufferedWriter)bw;
            if(!(bwSaida == bwS)){
            	bw.write(nome+" saiu\r\n");
            	bw.flush();
            }
        }
    }
    
    //Avisa lotação
    public void warnFull(BufferedWriter bwSaida) throws  IOException {
        bwSaida.write("Sala cheia\r\n");      
        bwSaida.flush();
    }
    
    
    //Método principal
    public static void main(String []args) {
        try {
            //Cria os objetos necessário para instânciar o servidor
            JLabel lblMessage = new JLabel("Porta:");
            JTextField txtPorta = new JTextField("12345");
            Object[] texts = {lblMessage, txtPorta};
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            clientes=new ArrayList<BufferedWriter>();
            JOptionPane.showMessageDialog(null,"Servidor ativo na porta: "+txtPorta.getText());
            while(true){
                System.out.println("Aguardando conexão...");
                Socket con = server.accept();
                System.out.println("Cliente conectado...");
                Thread t = new Servidor(con);
                t.start();
            }
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(null, "Porta já está sendo usada");
            System.exit(0);
        } 
    }
} 

