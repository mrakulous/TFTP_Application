//TFTPSim.java
//This class is the beginnings of an error simulator for a simple TFTP server 
//based on UDP/IP. The simulator receives a read or write packet from a client and
//passes it on to the server.  Upon receiving a response, it passes it on to the 
//client.
//One socket (23) is used to receive from the client, and another to send/receive
//from the server.  A new socket is used for each communication back to the client.   

import java.io.*;
import java.net.*;
//import java.util.*;

public class Sim {

// UDP datagram packets and sockets used to send / receive
private DatagramPacket sendPacket, receivePacket;
private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;

public Sim()
{
   try {
      // Construct a datagram socket and bind it to port 23
      // on the local host machine. This socket will be used to
      // receive UDP Datagram packets from clients.
      receiveSocket = new DatagramSocket(23);
      // Construct a datagram socket and bind it to any available
      // port on the local host machine. This socket will be used to
      // send and receive UDP Datagram packets from the server.
      sendReceiveSocket = new DatagramSocket();
   } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
   }
}

public void passOnTFTP()
{
   byte[] data;
   int clientPort, len;

   for(;;) {
      data = new byte[100];
      receivePacket = new DatagramPacket(data, data.length);

      System.out.println("Simulator: Waiting for packet from client............");
      System.out.println();
      
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
      System.out.println("Contents: " + contents);
      System.out.println();

      sendPacket = new DatagramPacket(data, len,
                                     receivePacket.getAddress(), 69);
     
      System.out.println("Simulator: Sending packet to server.");
      System.out.println("To host: " + sendPacket.getAddress());
      System.out.println("Destination host port: " + sendPacket.getPort());
      len = sendPacket.getLength();
      System.out.println("Length: " + len);
      contents = new String(data,0,len);
      System.out.println("Contents: " + contents);
      System.out.println();

      try {
         sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      data = new byte[100];
      receivePacket = new DatagramPacket(data, data.length);

      System.out.println("Simulator: Waiting for packet from server............");
      System.out.println();
      
      try {
         sendReceiveSocket.receive(receivePacket);
      } catch(IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Simulator: Packet received from server.");
      System.out.println("From host: " + receivePacket.getAddress());
      System.out.println("Host port: " + receivePacket.getPort());
      len = receivePacket.getLength();
      System.out.println("Length: " + len);
      contents = new String(data,0,len);
      System.out.println("Contents: " + contents);
      System.out.println();

      sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                            receivePacket.getAddress(), clientPort);

      System.out.println( "Simulator: Sending packet to client.");
      System.out.println("To host: " + sendPacket.getAddress());
      System.out.println("Destination host port: " + sendPacket.getPort());
      len = sendPacket.getLength();
      System.out.println("Length: " + len);
      contents = new String(data,0,len);
      System.out.println("Contents: " + contents);
      System.out.println();

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
