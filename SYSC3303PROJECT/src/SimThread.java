//This class is the beginnings of an error simulator for a simple TFTP server 
//based on UDP/IP. The simulator receives a read or write packet from a client and
//passes it on to the server.  Upon receiving a response, it passes it on to the 
//client.
//One socket (23) is used to receive from the client, and another to send/receive
//from the server.  A new socket is used for each communication back to the client.   

import java.io.*;
import java.net.*;

public class SimThread implements Runnable{
	// UDP datagram packets and sockets used to send / receive
	public static final int DATA_SIZE = 512;
	public static final int TOTAL_SIZE = DATA_SIZE + 4;  
	
	private DatagramPacket sendPacket, receivedPacket;
	private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;

	public SimThread(DatagramPacket received)
	{
		this.receivedPacket = received;
		
		try {
			receiveSocket = new DatagramSocket();
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
	      data = new byte[516];
	      receivedPacket = new DatagramPacket(data, data.length);

	      System.out.println("Simulator: Waiting for packet from client............");
	      System.out.println();
	      
	      // Wait for packet from client
	      
	      try {
	         receiveSocket.receive(receivedPacket);
	      } catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
	      
	      // Packet received from client
	      
	      System.out.println("Simulator: Packet received from client.");
	      System.out.println("From host: " + receivedPacket.getAddress());
	      clientPort = receivedPacket.getPort();
	      System.out.println("Host port: " + clientPort);
	      len = receivedPacket.getLength();
	      System.out.println("Length: " + len);
	      String contents = new String(data,0,len);
	      System.out.println("Contents: " + contents);
	      System.out.println();
	
	      sendPacket = new DatagramPacket(data, len, receivedPacket.getAddress(), 69);
	     
	      System.out.println("Simulator: Sending packet to server.");
	      System.out.println("To host: " + sendPacket.getAddress());
	      System.out.println("Destination host port: " + sendPacket.getPort());
	      len = sendPacket.getLength();
	      System.out.println("Length: " + len);
	      contents = new String(data,0,len);
	      System.out.println("Contents: " + contents);
	      System.out.println();
	      
	      // Send packet to server
	
	      try {
	         sendReceiveSocket.send(sendPacket);
	      } catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
	
	      data = new byte[100];
	      receivedPacket = new DatagramPacket(data, data.length);
	
	      System.out.println("Simulator: Waiting for packet from server............");
	      System.out.println();
	      
	      // Wait for packet from server
	      
	      try {
	         sendReceiveSocket.receive(receivedPacket);
	      } catch(IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
	      
	      // Packet received from server
	
	      System.out.println("Simulator: Packet received from server.");
	      System.out.println("From host: " + receivedPacket.getAddress());
	      System.out.println("Host port: " + receivedPacket.getPort());
	      len = receivedPacket.getLength();
	      System.out.println("Length: " + len);
	      contents = new String(data,0,len);
	      System.out.println("Contents: " + contents);
	      System.out.println();
	
	      sendPacket = new DatagramPacket(data, receivedPacket.getLength(),
	                            receivedPacket.getAddress(), clientPort);
	
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
	      
	      // Send packet to client
	
	      try {
	         sendSocket.send(sendPacket);
	      } catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
	      
	      sendSocket.close();
	   }
	}
	
	public void run(){
		passOnTFTP();
	}
}
