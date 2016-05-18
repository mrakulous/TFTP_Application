// TFTPServer.java 
// This class is the server side of a simple TFTP server based on
// UDP/IP. The server receives a read or write packet from a client and
// sends back the appropriate response without any actual file transfer.
// One socket (69) is used to receive (it stays open) and another for each response. 

import java.io.*;
import java.net.*;
import java.util.*;

public class Sim {

   
   // UDP datagram packets and sockets used to send / receive
   private DatagramPacket receivePacket;
   private static DatagramSocket receiveSocket;
   private static Thread se;
   public Sim()
   {
      try {
         receiveSocket = new DatagramSocket(23);
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void receiveAndSendTFTP() throws Exception
   {
      byte[] data = new byte[516];
      
      receivePacket = new DatagramPacket(data, data.length);
      System.out.println("Simulator: Waiting for packet.");
         try {
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         SimThread st = new SimThread(receivePacket);
         se = new Thread(st);
         se.start();
   }

   public static void main( String args[] ) throws Exception
   {
      Sim s = new Sim();
      for(;;){
    	  s.receiveAndSendTFTP();
      }
   }
}
