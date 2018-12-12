package gahh;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server extends JFrame {
    
	private static String password; 
    
    private Tools tool = new Tools();
    private Map<Integer, Socket> clients = new HashMap<Integer, Socket>();
    private JTextArea msg = new JTextArea();
    private JTextArea pwd = new JTextArea();
    private JButton pwdSure = new JButton("Sure");
    private JButton msgSend = new JButton("Send");
    private JButton serverOff = new JButton("Server Off");
    
    private mythread1 serverTh = null;
    private JPanel panel = new JPanel();
    
    public Server() {
        // TODO Auto-generated constructor stub
        this.setVisible(true);
        this.setSize(590, 650);
        this.setTitle("Server");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                // TODO Auto-generated method stub
                super.windowClosing(arg0);
                if(serverTh!=null){
                	serverTh.stopMe();
                }
                System.exit(0);
            }
        });
        
        panel.setLayout(null);
        
        msg.setBounds(20, 20, 530, 470);
        msgSend.addActionListener(new ActionListener(){
    	    @Override
    	    public void actionPerformed(ActionEvent e) {
    	        // TODO Auto-generated method stub
    	        String temp = "";
    	        if("sendMsg".equals(e.getActionCommand())){
    	            Set<Integer> keset = clients.keySet();
    	            java.util.Iterator<Integer> iter = keset.iterator();
    	            while(iter.hasNext()){
    	                int key = iter.next();
    	                Socket socket = clients.get(key);
    	                try {
    	                    if(socket.isClosed() == false){
    	                        if(socket.isOutputShutdown() == false){
    	                            temp = "To Client "+socket.getPort()+" Send Message ";
    	                            System.out.println(temp);
    	                            apppendMsg(temp);
    	                            DataOutputStream writer = new DataOutputStream(
    	                            		socket.getOutputStream());
    	                            temp = new String("Server : Hello!");
    	                            try{

        	                            byte[] tempByte = tool.desEncrypt(temp.getBytes("UTF-8"), password);
    	                            	//temp = tool.desEncryptString(temp,password);
        	                            writer.write(tempByte);
    	                            }catch(Exception e1){
    	                            	e1.printStackTrace();
    	                            }
    	                            writer.flush();
    	                        }
    	
    	                    }
    	                } catch (SocketException e1) {
    	                    // TODO Auto-generated catch block
    	                    e1.printStackTrace();
    	                } catch (IOException e1) {
    	                    // TODO Auto-generated catch block
    	                    e1.printStackTrace();
    	                }
    	            }
    	        }
    	    }
        } );

        msgSend.setActionCommand("sendMsg");
        msgSend.setBounds(390, 520, 160, 30);
        
        pwd.setBounds(20, 520, 180, 30);
        pwd.setText("");
        
        pwdSure.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		password = pwd.getText();
        		if(password.length()==0){
        			JOptionPane.showMessageDialog(null, "Password is empty.", "Error",JOptionPane.ERROR_MESSAGE); 
        		}else{
    				JOptionPane.showMessageDialog(null, "Password is settled.");
        		}
        		sendMsgToAll(null,password.getBytes());
        	}
        });
        pwdSure.setBounds(220, 520, 150, 30);
        
        serverOff.addActionListener(new ActionListener(){
        	@Override
    	    public void actionPerformed(ActionEvent e) {
    	        // TODO Auto-generated method stub
    	        Set<Integer> keset = clients.keySet();
	            java.util.Iterator<Integer> iter = keset.iterator();
	            while(iter.hasNext()){
	                int key = iter.next();
	                Socket socket = clients.get(key);
                    String temp = "Client"+socket.getPort()+":Exit";
                    apppendMsg(temp);
                    try{
                    	serverTh.stopMe();
                    	socket.close();
                    	socket = null;
                    	System.exit(0);
                    }catch(Exception e1){
                    	
                    }
            		System.exit(0);
	            }
        	}
        });
        
        panel.add(msg);
        panel.add(msgSend);
        panel.add(pwd);
        panel.add(pwdSure);
        this.add(panel);
        //this.add(serverOff);
    }
    
    public static void main(String[] args){
        new Server().listenClient();
    }
    
    public void listenClient(){
        int port = 8899;
        String temp = "";
        password = "12345678";
        System.out.println("listen Client " + password);
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                clients.put(socket.getPort(), socket);
                temp = "Client"+socket.getPort()+":Connection";
                this.apppendMsg(temp);
                serverTh = new mythread1(socket, this, password);
                serverTh.start();
                
                sendMsg(socket,password);
            }
        } catch (Exception e) {
            // TODO: handle exception
            //e.printStackTrace();
        }
    }

    public void apppendMsg(String msg){
        this.msg.append(msg+"\r\n");
    }

    public void sendMsg(Socket socket, String msg) {
        try {
            if(socket.isClosed() == false){
                if(socket.isOutputShutdown() == false){

                    DataOutputStream writer = new DataOutputStream(
                            socket.getOutputStream());
                    System.out.println("SendMessage " + msg);
                    writer.write(msg.getBytes("UTF-8"));
                    writer.flush();
                }
            }
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            // e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            //e1.printStackTrace();
        }
    }
    
    public void sendMsgToAll(Socket fromSocket, byte[] msg) {
        Set<Integer> keset = this.clients.keySet();
        java.util.Iterator<Integer> iter = keset.iterator();
        System.out.println("Send msg to all : " + msg);
        while(iter.hasNext()){
            int key = iter.next();
            Socket socket = clients.get(key);
            if(socket != fromSocket){
                try {
                    if(socket.isClosed() == false){
                        if(socket.isOutputShutdown() == false){
                            DataOutputStream writer = new DataOutputStream(
                                    socket.getOutputStream());
                            writer.write(msg);
                            writer.flush();
                        }
                    }
                } catch (SocketException e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }
            }
        }
    }  

    public void forwarded(Socket fromSocket){
    	Set<Integer> keset = this.clients.keySet();
        java.util.Iterator<Integer> iter = keset.iterator();

		DataOutputStream dos;  
		DataInputStream dis;
		
		while(iter.hasNext()){
            int key = iter.next();
            Socket socket = clients.get(key);
            if(socket != fromSocket){
                try {
                	if(socket.isClosed() == false){
                        if(socket.isOutputShutdown() == false){
                        	dis = new DataInputStream(fromSocket.getInputStream()); 
                        	dos = new DataOutputStream(socket.getOutputStream());
                        	dos.writeUTF(dis.readUTF());  
                    		dos.flush();
                    		long fileLength = dis.readLong();
                    		dos.writeLong(fileLength);  
                    		dos.flush();
                    		
                    		// start forwarded  
                    		int nums = 0;
                    		byte[] bytes = new byte[1024];  
                    		int length = 0;  
                    		long progress = 0;
                    		while((length = dis.read(bytes, 0, bytes.length)) != -1) {  
                    			dos.write(bytes, 0, length);  
                    			dos.flush();
                    			progress += length;
                    			if(progress == fileLength) break;
                    			//System.out.println(nums++);
                    		}  
                        }
                    }
                } catch (SocketException e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }
            }
        }
    }
}

class mythread1 extends Thread{

    private Socket socket = null;
    private Server server = null;
    private DataInputStream reader = null;
    char chars[] = new char[64];
    int len;
    private String temp = null;
    private String passWord;
    private String fileState = "FILE//::";
    private boolean stopMe = true;
    private Tools tool = new Tools();
    
    public mythread1(Socket socket, Server server, String password) {
        // TODO Auto-generated constructor stub
        this.socket = socket;
        this.server = server;
        passWord = password;
        init();
    }

    private void init(){
        try {
            reader = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
    }

    public void stopMe(){
    	try{
    		reader.close();
    		socket.close();
    		socket = null;
    		server = null;
    		reader = null;
    	}catch(Exception e){
    		
    	}
    	stopMe = false;
    }
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        while(stopMe){
            try {
            	byte[] tempByte = new byte[4096];
            	int length = 1024;
                while ((len =  reader.read(tempByte,0,length)) != -1) {
                    temp = new String(tool.desDecrypt(tempByte, passWord));
                    if(temp.compareTo(fileState)==0){
                        //System.out.println("Client : " + temp + "  temp size : " + temp.length());
                    	server.sendMsgToAll(this.socket, tempByte);
                    	server.forwarded(this.socket);
                    	continue;
                    }
                    
                    System.out.println(tempByte + "\n" + temp);
                    try{
                    	temp = new String("Client "+socket.getPort()+" says : "+temp);
                    	tempByte = tool.desEncrypt(temp.getBytes("UTF-8"), passWord);
                    	server.sendMsgToAll(this.socket, tempByte);
                    }catch(Exception e1){
                    	e1.printStackTrace();
                    }
                    server.apppendMsg("From Client"+socket.getPort()+" :" +temp);
                    //System.out.println(temp);
                }
                if(socket.getKeepAlive() == false){
                    reader.close();
                    temp = "Client "+socket.getPort()+" : Exit";
                    server.apppendMsg(temp);
                    socket.close();
                }
            } catch (Exception e) {
                // TODO: handle exception
                //e.printStackTrace(); 
                stopMe();
            }
            this.yield();
        }
    }
}

