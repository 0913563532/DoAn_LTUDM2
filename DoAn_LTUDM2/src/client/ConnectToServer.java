package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

public class ConnectToServer {


    private Socket socket;


    public ConnectToServer(String host, int port, String i) throws IOException {
        this.socket = new Socket(host, port);
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        output.println("ClientName:" +i); // Gửi tên của client lên server
     
    }
 

 
   

    public void closeConnection() {
        try {
        	if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
           
        }
        
    }
    boolean checkConnection() {
    	 if (socket.isConnected()) 
            return true;
          else 
             return false;
         
    }
    public Socket getSocket() {
    	return socket;
    }
    public void close(ObjectInputStream oos, ObjectOutputStream ois) throws IOException {
        if (oos != null) {
            oos.close();
        }
        if (ois != null) {
            ois.close();
        }
        socket.close();
    }
    public PublicKey receivePublicKey(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois = new ObjectInputStream(socket.getInputStream());
            return (PublicKey) ois.readObject();
      
    }

    public void sendEncryptedAesKey(byte[] encryptedAesKey) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(encryptedAesKey);
            oos.flush();
        }
    }
}
