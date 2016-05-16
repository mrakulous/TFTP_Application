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
      System.out.println("Server: Waiting for packet.");
         try {
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         TFTPServerThread st = new TFTPServerThread(receivePacket);
         se = new Thread(st);
         se.start();

   }

   public static void main( String args[] ) throws Exception
   {
      TFTPServer c = new TFTPServer();
      for(;;){
    	  /*Scanner sc = new Scanner(System.in);
    	  String s = sc.nextLine();
    	  if (s.equals("shutdown")){
    		  receiveSocket.close();
    	  }
    	  if(!se.isAlive()){
    		  break;
    	  }*/
    	  c.receiveAndSendTFTP();
      }
   }
}