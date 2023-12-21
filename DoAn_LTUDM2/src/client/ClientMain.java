package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMain {
	

    private static final String SERVER_ADDRESS = "172.20.10.9";
    private static final int SERVER_PORT = 12345; // Cổng kết nối tới server
    private static final String NameClient = "client 2";
    public static ExecutorService executor;
    public static void main(String[] args) {
       
    	   try {
    		   
    		   
    		   ConnectToServer client = new ConnectToServer(SERVER_ADDRESS,SERVER_PORT,NameClient);
    		   
    		   executor = Executors.newFixedThreadPool(3); 
    		   ObjectOutputStream out = new ObjectOutputStream(client.getSocket().getOutputStream());
    		   ObjectInputStream in = new ObjectInputStream(client.getSocket().getInputStream());
    		   View view = new View(client,NameClient,out,in);
    		  
    		   executor.execute(view);
    		   
           } catch (Exception e) {
               e.printStackTrace();
           }
    	 
    }



 
}