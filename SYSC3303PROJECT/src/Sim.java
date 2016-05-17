import java.io.*;
import java.net.*;
import java.util.*;

public class Sim {
   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
   
   public Sim()
   {
      try {
         receiveSocket = new DatagramSocket(23);
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void passOnTFTP()
   {
      byte[] data;
      int clientPort, j=0, len;
      int serverPort = 0;

      for(;;) {
         data = new byte[516];
         receivePacket = new DatagramPacket(data, data.length);

         System.out.println("Simulator: Waiting for packet from client............" + "\n");
         
         try {
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         System.out.println("Simulator: Packet received from client.");
         System.out.println("From host: " + receivePacket.getAddress());
         clientPort = receivePacket.getPort();
         System.out.println("Host port: " + clientPort);
         len = receivePacket.getLength();
         System.out.println("Length: " + len);
         String contents = new String(data,0,len);
         System.out.println("Contents: " + contents + "\n");
         
         int sport = 69;
         if(data[1] == 1|| data[1] == 2){
        	 sport = 69;
         } else {
        	 sport = serverPort;
         }
         
         sendPacket = new DatagramPacket(data, len,
                                        receivePacket.getAddress(), sport);
        
         System.out.println("Simulator: Sending packet to server.");
         System.out.println("To host: " + sendPacket.getAddress());
         System.out.println("Destination host port: " + sendPacket.getPort());
         len = sendPacket.getLength();
         System.out.println("Length: " + len);
         contents = new String(data,0,len);
         System.out.println("Contents: " + contents + "\n");

         try {
            sendReceiveSocket.send(sendPacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         data = new byte[100];
         receivePacket = new DatagramPacket(data, data.length);

         System.out.println("Simulator: Waiting for packet from server............" + "\n");
         try {
            sendReceiveSocket.receive(receivePacket);
         } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         System.out.println("Simulator: Packet received from server.");
         System.out.println("From host: " + receivePacket.getAddress());
         serverPort = receivePacket.getPort();
         System.out.println("Host port: " + serverPort);
         len = receivePacket.getLength();
         System.out.println("Length: " + len);
         contents = new String(data,0,len);
         System.out.println("Contents: " + contents + "\n");

         sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                               receivePacket.getAddress(), clientPort);

         System.out.println( "Simulator: Sending packet to client.");
         System.out.println("To host: " + sendPacket.getAddress());
         System.out.println("Destination host port: " + sendPacket.getPort());
         len = sendPacket.getLength();
         System.out.println("Length: " + len);
         contents = new String(data,0,len);
         System.out.println("Contents: " + contents + "\n");

         try {
            sendSocket = new DatagramSocket();
         } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
         }

         try {
            sendSocket.send(sendPacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }
         sendSocket.close();
      }
   }

   public static void main( String args[] )
   {
      Sim s = new Sim();
      s.passOnTFTP();
   }
}
