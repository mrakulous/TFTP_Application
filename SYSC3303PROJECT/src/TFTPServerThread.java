import java.io.*;
import java.net.*;
import java.util.*;

//import TFTPServer.Request;

public class TFTPServerThread implements Runnable{
	// types of requests we can receive
	public static enum Request { READ, WRITE, ERROR};
	// responses for valid requests
	public static final byte[] readResp = {0, 3, 0, 1};
	public static final byte[] writeResp = {0, 4, 0, 0};
	   
	private DatagramPacket received, sendPacket, ack;
	private DatagramSocket socket, sendSocket;
	private InetAddress ip;
	private int port;
	private String filename;
	private String mode;
	private Request req;
	private int j, len;
	private String threadTest;
	
	public TFTPServerThread(DatagramPacket received){
		this.received = received;
		this.ip = received.getAddress();
		this.port = received.getPort();
		System.out.println("TEST1");
	}
	   
	public void identifyReq(){
		byte[] data = received.getData();
		//byte[] response = new byte[4];
		int len = received.getLength();
		int filecount=0;
		int modecount=0;
	             
		//if (data[0]!=0) //req = Request.ERROR; // bad
		if (data[1]==1) req = Request.READ; // could be read
		else if (data[1]==2) req = Request.WRITE; // could be write
		//else //req = Request.ERROR; // bad

		//if (req!=Request.ERROR) { // check for filename
			// search for next all 0 byte
			for(filecount=4;filecount<len;filecount++) {
			if (data[filecount] == 0) break;
				}
		//if (filecount==len)// req=Request.ERROR; // didn't find a 0 byte
		//if (filecount==2) //req=Request.ERROR; // filename is 0 bytes long
		// otherwise, extract filename
		filename = new String(data,4,filecount-4);
		//}
	 
		//if(req!=Request.ERROR) { // check for mode
		// search for next all 0 byte
		for(modecount=filecount+1;modecount<len;modecount++) { 
			if (data[modecount] == 0) break;
		}
		//if (modecount==len)// req=Request.ERROR; // didn't find a 0 byte
		//if (modecount==filecount+1) req=Request.ERROR; // mode is 0 bytes long
		mode = new String(data,filecount,modecount-filecount-1);
		//}
	         
		//if(modecount!=len-1) //req=Request.ERROR; // other stuff at end of packet        

		try {
			socket = new DatagramSocket();
		} catch (SocketException e){
			e.printStackTrace();
			System.exit(1);
		}
	    
		System.out.println("Test: " + req);
		
		// Create a response.
		if (req==Request.READ) { // for Read it's 0301
			try {
				Read(filename);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (req==Request.WRITE) { // for Write it's 0400
			try {
				Write();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else { // it was invalid, just quit
			// throw new Exception("Not yet implemented");
		}
	}

	/**
	 * Retrieve text file from directory, put it into packet and send to client
	 */
	 public void Read(String filename) throws FileNotFoundException, IOException {
	   
		 System.out.println("READTEST");

		//prepare file for transfer to client.
		BufferedInputStream in;
		in = new BufferedInputStream(new FileInputStream(filename));

		int n;
		byte[] data = new byte[516];  
		byte[] ac = new byte[4];
		//construct datagram packet that is to 
		//be sent to a specified port on a specified host.
		   
		sendSocket = new DatagramSocket();
		   
		for(int i=0;i<4;i++) //initiliaze opcode+block number
		data[i] = readResp[i];
		   
		while ((n = in.read(data)) != 1) { //if data.length()<512, last packet
			//ack = new DatagramPacket(ackByte, ackByte.length);
			sendPacket = new DatagramPacket(data, data.length, received.getAddress(), received.getPort());
			ack = new DatagramPacket(ac, ac.length, received.getAddress(), received.getPort());        
			sendSocket.send(sendPacket);
			sendSocket.receive(ack);
			       
			//if data[3] is at max, reset to zero and increase data[2] by 1 (block number counter)
			if((data[3]++)==256) {
				data[2]++;
				data[3]=0;
			}
		}  
		in.close(); 
		socket.close();
	}

        /*
	* Write the received datagram packet to an output file.
	*/
	public void Write() throws FileNotFoundException, IOException {
		
		
		System.out.println("writeTEST");
		threadTest = "TESTSERVERTHREAD.dat";
		int offset = 4; // Offset for the opcode
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(threadTest)); // Create the output file
		byte[] ACK_array = new byte[4]; // Create ACK byte array
		ACK_array[0] = 0; // Acknowledge byte
		ACK_array[1] = 3; // Acknowledge byte
		for(;;) {
			byte[] receivedPacket = received.getData(); // Convert the received datagram packet into a byte array
		        int receivedDataLength = receivedPacket.length; // Get the length of the received packet

			out.write(receivedPacket, offset, receivedDataLength); // Write to output file starting at offset 0

			ACK_array[2] = receivedPacket[2]; // Block number
			ACK_array[3] = receivedPacket[3]; // Block number
	       
			DatagramPacket ACK_packet = new DatagramPacket(ACK_array, 4, ip, port); // Create a packet to send back to client
			DatagramSocket sendSocket = new DatagramSocket(); // Create a new socket to use for sending
		       
			// Send the ACK packet
			try {
				sendSocket.send(ACK_packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			sendSocket.close(); // Close the newly made socket
		       
			// Check if it's the last set of bytes
			if(receivedDataLength-offset < 512) {
				break;
			}
		}
		out.close(); // Close the BufferedOutputStream
	}

	public void run(){
		System.out.println("RUNTEST1");
		identifyReq();
	}
}