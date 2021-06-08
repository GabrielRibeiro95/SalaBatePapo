package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Cliente extends JFrame implements ActionListener, KeyListener {
	
	/* Variáveis necessárias, como caixas de texto, botões e o próprio socket de 
	   conexão. Possui também as classes de saíde de dados e escrita */
	private static final long serialVersionUID=1L;
    private JTextArea mensagens;
    private JTextField txtMsg;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel lblHistorico;
    private JLabel lblMsg;
    private JPanel pnlContent;
    private Socket socket;
    private OutputStream ou ;
    private Writer ouw;
    private BufferedWriter bfw;
    private JTextField txtServidor;
    private JTextField txtPorta;
    private JTextField txtNome;
	
    //Mostra tela de conversa
    public void showWindow() throws IOException {
        pnlContent = new JPanel();
        mensagens=new JTextArea(10,20);
        mensagens.setEditable(false);
        txtMsg=new JTextField(20);
        txtMsg.grabFocus();
        lblHistorico     = new JLabel("Histórico");
        lblMsg        = new JLabel("Digite sua mensagem:");
        btnSend                     = new JButton("Enviar");
        btnSend.setToolTipText("Enviar mensagem");
        btnSair           = new JButton("Sair");
        btnSair.setToolTipText("Sair do chat");
        btnSend.addActionListener(this);
        btnSair.addActionListener(this);
        btnSend.addKeyListener(this);
        txtMsg.addKeyListener(this);
        JScrollPane scroll = new JScrollPane(mensagens);
        mensagens.setLineWrap(true);        
        pnlContent.add(lblHistorico);
        pnlContent.add(scroll);
        pnlContent.add(lblMsg);
        pnlContent.add(txtMsg);
        pnlContent.add(btnSair);
        pnlContent.add(btnSend);
        setTitle(txtNome.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250,300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        txtMsg.grabFocus();
    }
    
    //Método construtor do cliente que chama uma tela de log-in
	public Cliente() throws IOException {
		JLabel lblNome=new JLabel("Nome: ");
		JLabel lblPorta=new JLabel("Porta: ");
		JLabel lblServidor=new JLabel("Servidor: ");
		txtServidor=new JTextField("localhost",10);
		lblServidor.setAlignmentX(10);
		txtPorta = new JTextField("12345",10);
        txtNome = new JTextField("Gabriel",10);
        Object[] texts = {lblNome, txtNome, lblPorta, txtPorta, lblServidor, txtServidor};
        JOptionPane.showMessageDialog(null, texts);
	}
	
	//Conecta ao socket no servidor e porta especificados na tela de log-in
	public void connect() throws IOException {
		try {
			socket = new Socket(txtServidor.getText(),Integer.parseInt(txtPorta.getText()));
			ou = socket.getOutputStream();
			ouw = new OutputStreamWriter(ou);
			bfw = new BufferedWriter(ouw);
			bfw.write(txtNome.getText()+"\r\n");
			bfw.flush();
			showWindow();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Favor digitar valores válidos!");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Servidor '"+txtServidor.getText()+"' não encontrado");
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nenhum servidor escutando na porta "+txtPorta.getText());
			e.printStackTrace();
		}
    }
	
	//Envia mensagem para o servidor
    public void sendMessage(String msg) throws IOException{
    	if (!msg.isEmpty()) {
    		bfw.write(msg+"\r\n");
    		mensagens.append(txtNome.getText()+": "+txtMsg.getText()+"\r\n");
    	} else {
    		JOptionPane.showMessageDialog(null, "Digite alguma coisa");
    		txtMsg.grabFocus();
    	}
    	bfw.flush();
        txtMsg.setText("");
    }
    
    //Sai da sala e desconecta
    public void disconnect() throws IOException {
    	sendMessage("Desconectando");
        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
        System.exit(0);
    }

    //Evento do clique do mouse, envia a mensagem ou sai
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals(btnSend.getActionCommand())) {
                sendMessage(txtMsg.getText());
            } else {
            	if(e.getActionCommand().equals(btnSair.getActionCommand())) {
            		disconnect();
            	}
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //Evento do botão 'Enter', envia a mensagem
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
        	try {
        		sendMessage(txtMsg.getText());
            } catch (IOException e1) {
               e1.printStackTrace();
            }
        }
    }
    
    public void listen() throws IOException{
        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";
        while(!"Sair".equalsIgnoreCase(msg))
            if(bfr.ready()){
                msg = bfr.readLine();
                if(msg.equals("Sair"))
                    mensagens.append("Servidor caiu! \r\n");
                else
                    mensagens.append(msg+"\r\n");
            }
    }

    //Não utilizado
    @Override
    public void keyReleased(KeyEvent arg0){}
    
    //Não utilizado
    @Override
    public void keyTyped(KeyEvent arg0){}
	
    //Método principal que instancia um cliente, o conecta e passa a escutar
	public static void main(String[] args) throws IOException {
		Cliente app=new Cliente();
		app.connect();
		app.listen();
	}
}
