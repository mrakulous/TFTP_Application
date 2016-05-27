// TFTPServer.java 
// This class is the server side of a simple TFTP server based on
// UDP/IP. The server receives a read or write packet from a client and
// sends back the appropriate response without any actual file transfer.
// One socket (69) is used to receive (it stays open) and another for each response. 

import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPServer {
   // UDP datagram packets and sockets used to send / receive
   private DatagramPacket receivePacket;
   private static DatagramSocket receiveSocket;
   private static Thread se;
   public TFTPServer()
   {
	   System.out.println("Server: Waiting for packet from simulator............" + "\n");
	   try {
		   // Construct a datagram socket and bind it to port 69
		   // on the local host machine. This socket will be used to
		   // receive UDP Datagram packets.
		   receiveSocket = new DatagramSocket(69);
	   } catch (SocketException se) {
		   se.printStackTrace();
		   System.exit(1);
	   }
   }

   public void receiveAndSendTFTP() throws Exception
   {
	   	 byte[] data = new byte[516];
	   	 
      
	   	 receivePacket = new DatagramPacket(data, data.length);
         // Block until a datagram packet is received from receiveSocket.
         try {
        	
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         System.out.println("Server: Packet received from simulator.");
	     System.out.println("From host: " + receivePacket.getAddress());
	     System.out.println("Host port: " + receivePacket.getPort());
	     int len = receivePacket.getLength();
	     System.out.println("Length: " + len);
	     System.out.println("Contents(bytes): " + data);
	     String contents = new String(data,0,len);
	     System.out.println("Contents(string): " + contents + "\n");
	     
	     try {
             Thread.sleep(500);
         } catch (InterruptedException e) {
        	 e.printStackTrace();
         }
	     
         TFTPServerThread st = new TFTPServerThread(receivePacket);
         se = new Thread(st);
         se.start();
   }

   public static void main( String args[] ) throws Exception
   {
      TFTPServer c = new TFTPServer();
      for(;;){
    	  c.receiveAndSendTFTP();
    	  
      }
   }
}
