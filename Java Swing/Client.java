package gahh;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JFrame{

    // 为了简单起见，所有的异常都直接往外抛
    String host = "127.0.0.1"; // 要连接的服务端IP地址
    int port = 8899; // 要连接的服务端对应的监听端口
    mythread thread  = null;
    Socket client = null;
    DataOutputStream writer = null;

    private Tools tool = new Tools();
    private String password;
    
    private JTextArea msg = new JTextArea();
    private JTextArea input = new JTextArea();
    private JButton cliCon = new JButton("Connect");
    private JButton msgSend = new JButton("Send");
    private JButton sendFile = new JButton("Send File");
    private JTextField fileOut = new JTextField();
    private JButton selectFile = new JButton("..");
    private JPanel panel = new JPanel();
    
    Client() {
        // TODO Auto-generated constructor stub
        this.setVisible(true);
        this.setSize(750, 750);
        this.setTitle("Client");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                // TODO Auto-generated method stub
                super.windowClosing(arg0);
                try {
                    if(client != null){
                        client.close();
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if(thread != null){
                	thread.stopMe();
                }

                System.exit(0);
            }
        });
        
        panel.setLayout(null);
        
        msg.setBounds(20, 20, 690,400);

        input.setBounds(20, 440, 450, 140);
         
        fileOut.setBounds(490,440,160,30);
        fileOut.setEditable(false);
        
        selectFile.setBounds(670,440,30,30);          
        selectFile.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e){
        		fileOut.setText("");
        		String fileName = "";
                JFileChooser fileChooser = new JFileChooser();
                int iOpen = fileChooser.showOpenDialog(getContentPane());
                if(iOpen == JFileChooser.APPROVE_OPTION){
                	File selectedFile = fileChooser.getSelectedFile();
                	if(selectedFile.exists()) {
                		fileName = selectedFile.getAbsolutePath(); 
                		fileOut.setText(fileName);
                	}
                }
        	}
		});

        cliCon.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		initSocket();
        	}
        });
        cliCon.setBounds(20, 600, 150, 30);
        
        msgSend.addActionListener(new ActionListener(){
        	@Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
        		if(thread!=null){
	                String temp = null;
	                try {
	                    if((temp = input.getText()) != null){
	                        appendMsg("I ("+client.getLocalPort()+") say : "+temp);
	                        byte[] tempByte = temp.getBytes("UTF-8");
	                    	try{
	                    		tempByte = tool.desEncrypt(tempByte,password);
	                    		System.out.println(tempByte);
	                    	}catch(Exception e1){
	                    		e1.printStackTrace();
	                    	}
	                    	System.out.println("ok: " + tempByte);
	                        writer.write(tempByte);
	                        writer.flush();
	                        input.setText("");
	                    }
	                } catch (IOException e1) {
	                    // TODO Auto-generated catch block
	                    //e1.printStackTrace();
	                }
        		}
            }
        });
        msgSend.setBounds(180, 600, 150, 30);
        
        sendFile.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		String fileName = fileOut.getText();
        		if(thread!=null){
        			try{
        				input.setEditable(false);
        				byte[] temp = tool.desEncrypt(new String("FILE//::").getBytes("UTF-8"), password);
        				writer.write(temp);
        				writer.flush();
        				appendMsg("I send FILE : "+ fileName);
        				thread.sendFile(fileName);
        				input.setEditable(true);
        			}catch(Exception e1){
        				//
        			}
        		}
        		fileOut.setText("");
        	}
        });
        sendFile.setBounds(350,600,150,30);
        
        panel.add(msg);
        panel.add(input);
        panel.add(msgSend);
        panel.add(cliCon);
        panel.add(fileOut);
        panel.add(selectFile);
        panel.add(sendFile);
        this.add(panel);
    }
    
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
    	//new Client();
        new Client();
    }

    public void initSocket(){
        try {
            client = new Socket(this.host, this.port);
            writer = new DataOutputStream(client.getOutputStream());
            // start a connection
            thread = new mythread(client, this);
            thread.start();
            password = thread.passWord;
            
            this.appendMsg("Connected");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            this.appendMsg("Disconnected");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            this.appendMsg("Disconnected");
        }
    }

    public void appendMsg(String msg){
        this.msg.append(msg+"\n");
    }
    
}

class mythread extends Thread {
    private Socket socket = null;
    private DataInputStream reader = null;
    private int len = 0;
    private int number;
    char chars[] = new char[64];
    private Client client = null;
    private String temp = "";
    private boolean stopMe = true;
    private Tools tool = null;
    public String passWord;
    
    private FileInputStream fis;
    private FileOutputStream fos;
    private DataOutputStream dos;  
    private DataInputStream dis;
    
    private String fileState = "FILE//::";
    private static DecimalFormat df = null;
    
    public mythread(Socket oldSocket, Client oldClient) {
        // TODO Auto-generated constructor stub
        socket = oldSocket;
        client = oldClient;
        number = 0;
        passWord = null;
        tool = new Tools();
        try {
            reader = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            // TODO: handle exception
        }
        
    }

    public void encryptFile(String file, String destFile) throws Exception{
		FileInputStream is = new FileInputStream(file);
		FileOutputStream out = new FileOutputStream(destFile);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer,0,buffer.length)) > 0) {
			byte[] tempBytes = tool.desEncrypt(buffer, passWord);
			System.out.print(tempBytes);
			out.write(tempBytes, 0, tempBytes.length);
		}
		is.close();
		out.close();
	}

    public void decryptFile(String file,String destFile) throws Exception {
    	FileInputStream is = new FileInputStream(file);
		FileOutputStream out = new FileOutputStream(destFile);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer,0,buffer.length)) > 0) {
			byte[] tempBytes = tool.desDecrypt(buffer, passWord);
			out.write(tempBytes, 0, tempBytes.length);
		}
		is.close();
		out.close();
	}

    public void sendFile(String filePath){  
    	System.out.println("Sending file");
        try{  
            File file = new File(filePath);
            if(file.exists()) {
            	try{
            		encryptFile(filePath,filePath+"encrypt");
            	}catch(Exception e){}
            	filePath+="encrypt";
            }
            file = new File(filePath);
            if(file.exists()){
            	String fileName = file.getName();
                fis = new FileInputStream(file);  
                dos = new DataOutputStream(socket.getOutputStream());  
                
                // fileName and length
                System.out.println("fileName and length");
                dos.writeUTF(fileName);  
                dos.flush();  
                dos.writeLong(file.length());  
                dos.flush();  
  
                // start Transmission  
                System.out.println("======== start Transmission ========");  
                byte[] bytes = new byte[1024];  
                int length = 0;  
                long progress = 0;  
                while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);  
                    dos.flush();  
                    progress += length;  
                    System.out.println("| " + (100*progress/file.length()) + "% |");  
                }   
                System.out.println("======== Success ========");  
            }  /*
            try{
                if(fis != null)  
                    fis.close();  
                if(dos != null)  
                    dos.close();  
            }catch(Exception e){
            	
            }*/
        }catch(Exception e){  
            //e.printStackTrace();  
        } 
        System.out.println("======== Over ========");  
        
    }  
    
    public void reveiveFile(){
    	System.out.println("Receiving file");
    	try{
	    	dis = new DataInputStream(socket.getInputStream());  
	    	  
	        // file name and length
	        String fileName = dis.readUTF();  
	        long fileLength = dis.readLong();  
	        File directory = new File("D:\\JavaTestCache");  
	        if(!directory.exists()) {  
	            directory.mkdir();  
	        }  
	        File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);  
	        fos = new FileOutputStream(file);  
	
	        // start receiving  
	        int nums = 0;
		    byte[] bytes = new byte[1024];  
	        String temp;
		    int length = 0;
	        long progress = 0;
	        while((length = dis.read(bytes, 0, bytes.length)) != -1) {
	        	
	        	fos.write(bytes, 0, length);  
	            fos.flush();
	            progress += length;
	            if(progress==fileLength) break;
    			System.out.println("blocks : " + (nums++));  
	        }  
	        if(fileName!=null)
	        	System.out.println(
	        		"======== receive [File Name：" + fileName + "] [Size：" + getFormatFileSize(fileLength) + "] ========");  
	        String filePath = directory.getAbsolutePath() + File.separatorChar + fileName;
	        decryptFile(filePath,filePath.substring(0, filePath.length() - 7));
	        file.deleteOnExit();
	        /*try{  
	    		if(fos != null)  
	    			fos.close();  
	    		if(dis != null)  
	                dis.close();  
	        }catch(Exception e){
	        	//
	        }   */
    	}catch (Exception e){  
	        e.printStackTrace();  
	    }
    	
    }
    
    private String getFormatFileSize(long length) {  
        double size = ((double) length) / (1 << 30);  
        if(df!=null && size >= 1) {  
            return df.format(size) + "GB";  
        }  
        size = ((double) length) / (1 << 20);  
        if(df!=null && size >= 1) {  
            return df.format(size) + "MB";  
        }  
        size = ((double) length) / (1 << 10);  
        if(df!=null && size >= 1) {  
            return df.format(size) + "KB";  
        }  
        return length + "B";  
    }  
    
    public void stopMe(){
    	stopMe = false;
    	try{
    		reader.close();
    		socket.close();
			reader = null;
	    	socket = null;
    	}catch(Exception e){
    		
    	}
    }
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        System.out.println("Client "+this.getId()+" Start");
        
        while (stopMe) {
            try {
                if (socket!=null && socket.isClosed() == false) {
                    if (socket.isInputShutdown() == false) {
                    	System.out.println("Client get message" );
                    	/*
                    	if(passWord == null){
                    		passWord = reader.readUTF();
                    		System.out.println("Client get " + passWord);
                        	//number = 1;
                    	}*/
                    	byte[] tempByte = new byte[4096];
                    	int length = 1024;
                        while ((len =  reader.read(tempByte,0,length)) != -1) {
                        	System.out.println(tempByte + "\n" + number);
                        	if(number==0){
                        		passWord = new String(tempByte);
                        		System.out.println("Client get " + passWord);
                        		number = 1;
                            	continue;
                        	}
                    		try{
                    			temp = new String(tool.desDecrypt(tempByte, passWord));
                    		}catch(Exception e1){
                    			e1.printStackTrace();
                    		}
                			System.out.println(temp);
//                          System.out.println(temp.compareTo(fileState));
                    		if(temp.compareTo(fileState)==0){
                    			client.appendMsg("Receiving file...");
                    			try{
                    				reveiveFile();
                    			}catch(Exception e1){
                    				//e1.print
                    			}
                    			client.appendMsg("Success");
                    			continue;
                    		}else{
                    			System.out.println(temp);
                    			client.appendMsg(temp);
                    			continue;
                    		}
                        	//System.out.println();
                        }
                        //System.out.print("Client get byte length " + len);
                    }
                    this.yield();
                } else {
                    if (socket!=null && reader!=null && socket.getKeepAlive() == false) {
                        reader.close();
                        socket.close();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }
        System.exit(0);
    }
}
