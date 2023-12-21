package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class ServerMain {
  
	 private static int SERVER_PORT = 12345; 
	 public static int numThread = 3;
	 private static ServerSocket serverSocket = null;
     public static void main(String[] args) throws IOException {
    	   ExecutorService executor = Executors.newFixedThreadPool(numThread);
        try {
       
 
             
        	serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is listening on port " + SERVER_PORT);
         
            
            while(true){
            	String nameClient="";
            	 Socket clientSocket = serverSocket.accept(); 
            	 try {
                     // Lấy địa chỉ IP của client
                     String clientIP = clientSocket.getInetAddress().getHostAddress();
                    

                     BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     
                     PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                     // Đọc tên của client từ dòng đầu tiên gửi lên từ client
                     String clientInfo = input.readLine();
                     String[] parts = clientInfo.split(":");
                     if (parts.length == 2 && parts[0].equals("ClientName")) {
                         nameClient = parts[1];
                         System.out.println("Client " + nameClient +" IP : "+clientIP+ " đã kết nối");
                         
                     } else {
                         System.out.println("Không thể xác định thông tin client");
                     }
                     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream (clientSocket.getInputStream());
                     
                     ServerHandler serverHandler = new ServerHandler(clientSocket, nameClient,out,in);
                     executor.execute(serverHandler);
           	}catch (Exception e) {
       			e.printStackTrace();
       		}
            	 
            	
            	 
  
            }
         
        }catch (Exception e) {
            e.printStackTrace();
            
        }
        finally {
            executor.shutdown();
        }
}
     
}
