package project;

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
   
   public static final int DATA_SIZE = 512;
   public static final int TOTAL_SIZE = DATA_SIZE+4;
   private boolean firstTime = true;
   private String contents;
   private Byte leftByte;
   private Byte rightByte;
   
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
	   	 byte[] data = new byte[TOTAL_SIZE];
      
	   	 receivePacket = new DatagramPacket(data, data.length);
         // Block until a datagram packet is received from receiveSocket.
         try {
        	
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }
         
         leftByte = new Byte(receivePacket.getData()[2]);
         rightByte = new Byte(receivePacket.getData()[3]);
 		
         System.out.println("Server: Packet received from simulator.");
	     System.out.println("From host: " + receivePacket.getAddress());
	     System.out.println("Host port: " + receivePacket.getPort());
	     int len = receivePacket.getLength();
	     System.out.println("Length: " + len);
	     if(firstTime) {
				// Do nothing
		 }
	     else {
	    	 System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
	     }
	     System.out.println("Contents(bytes): " + data);
	     if(firstTime) {
	    	 // filename and mode
	    	 contents = new String(data, 2, 17);
	    	 System.out.println("Contents(string): \n" + contents + "\n");
	    	 firstTime = false;
	     }
	     else {
	    	 if(len > 4) {
	    		 // It is not an ACK packet
	    		 contents = new String(data, 4, DATA_SIZE);
	    		 System.out.println("Contents(string): \n" + contents + "\n");
	    	 }
	    	 else {
	    		 // It is an ACK packet
	    		 System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
	    	 }
	     }
	     
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
