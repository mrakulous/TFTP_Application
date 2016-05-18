import java.io.*;
import java.net.*;
import java.util.*;

//import TFTPServer.Request;

public class TFTPServerThread implements Runnable
{
	public static enum Request { READ, WRITE, ERROR};
	public static final int DATA_SIZE = 512;
	public static final int TOTAL_SIZE = DATA_SIZE +4;  
	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket Socket;
	private String filename;
	private String mode;
	private Request req;
	private byte correctBlock1 = 0;
	private byte correctBlock2 = 0;
	
	public TFTPServerThread(DatagramPacket received)
	{
		this.receivedPacket = received;
		
		try {
			Socket = new DatagramSocket();
		} catch (SocketException se) {   // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}
	   
	public void identifyReq()
	{
		byte[] data = receivedPacket.getData();
		int len = receivedPacket.getLength();
		int filecount=0;
		int modecount=0;
	             
		if (data[0]!=0) req = Request.ERROR; // bad
		if (data[1]==1) req = Request.READ; // could be read
		else if (data[1]==2) req = Request.WRITE; // could be write
		else req = Request.ERROR; // bad
		
		if (req!=Request.ERROR) { // check for filename
			for(filecount=2; filecount<len; filecount++) {
				if (data[filecount] == 0) break;
			}
			if (filecount==len) req=Request.ERROR; // didn't find a 0 byte
			if (filecount==2) req=Request.ERROR; // filename is 0 bytes long
			filename = new String(data,2,filecount-2);
		}
		 
		if(req!=Request.ERROR) { // check for mode
			for(modecount=filecount+1;modecount<len;modecount++) { 
				if (data[modecount] == 0) break;
			}
			if (modecount==len) req=Request.ERROR; // didn't find a 0 byte
			if (modecount==filecount+1) req=Request.ERROR; // mode is 0 bytes long
			mode = new String(data,filecount,modecount-filecount-1);
		}
		         
		if(modecount!=len-1) req=Request.ERROR; // other stuff at end of packet        

		System.out.println("Type of request: " + req);
		
		// Create a response.
		
		if(req==Request.READ) {
			read();
		} else if(req==Request.WRITE) {
			write();
		}
	}

	/**
	 * Retrieve text file from directory, put it into packet and send to client
	 * @throws SocketException 
	 */
	public void read() {
		 
		byte blocknum1 = 0;
		byte blocknum2 = 1;
		int len;
		int counter = 0;
   		
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
			do {
				for(;;) {
					
					Socket.receive(receivedPacket);
					
					if(correctBlock1 == receivedPacket.getData()[2] && correctBlock2 == receivedPacket.getData()[3]) {
						// Ex, sent DATA 1 and received ACK 1, correct, so break out of forever loop.
						break;
					}
				}
				
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				len = in.read(data);
		                
				msg[0] = 0;
				msg[1] = 3;
				msg[2] = blocknum1;
				msg[3] = blocknum2;
		                
				int i = 0; 
				for(;;) {
					System.arraycopy(data, i, msg, i+4, len);
					if(data[i]==0) {
						break;
					} else {
						i++;
					}
				}
				data[i] = 0;
				i++;
				
				while (counter < 2) {
					sendPacket = new DatagramPacket(msg, i+4, receivedPacket.getAddress(), receivedPacket.getPort());
					try {
						Socket.setSoTimeout(500);
						Socket.send(sendPacket);
					} catch (IOException e){
						e.printStackTrace();
						retransmit(sendPacket);
						if(counter < 1) {
							// First time, so retransmit
							counter++;
						}
						else {
							System.out.println("Still no ACK received. Timing out.");
							System.exit(1);
						}
					}
				}
				
				if(blocknum2 == 255){
					blocknum1++;
				}
				blocknum2++;  
			} while (len>=DATA_SIZE);
				in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	 
	 public void retransmit(DatagramPacket thePacket) throws IOException {
		 Socket.send(thePacket);
		 System.out.println("Packet retransmitted.");
	 }
	 
        /*
	* Write the received datagram packet to an output file.
	*/
	public void write() {
		
		byte blocknum1=0;
		byte blocknum2=0;
   		
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
			System.out.print(filename);
   			
			for(;;) {
				int len;
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				byte[] ack = new byte[4];
                
				ack[0] = 0;
				ack[1] = 4;
				ack[2] = blocknum1;
				ack[3] = blocknum2;
                
				sendPacket = new DatagramPacket(ack, ack.length, receivedPacket.getAddress(), receivedPacket.getPort());
				try {
					Socket.send(sendPacket);
				} catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				}
				
				if(correctBlock2 == 255) {
					// We have 0 255
					correctBlock1++;
					// We have 1 255
				}
				
				correctBlock2++;
				// If it was 1 255, it is now 1 0
				
				receivedPacket = new DatagramPacket(msg, msg.length);

				try {
					int block1 = correctBlock1;
					int block2 = correctBlock2;
					
					for(;;) {
						Socket.setSoTimeout(1000);
						Socket.receive(receivedPacket);
						if(block2 == 255) {
							// We have 0 255
							if(block1+1 == receivedPacket.getData()[2] && block2+1 == receivedPacket.getData()[3]) {
								// We have 1 0, which is correct, so break out of forever loop
								break;
							}
						}
						if(block1 == receivedPacket.getData()[2] && block2+1 == receivedPacket.getData()[3]) {
							// Ex, had 0 1, it is now 0 2, correct, so break out of forever loop
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
                
				System.arraycopy(receivedPacket.getData(), 4, data, 0, receivedPacket.getLength()-4);
                
				for(len = 4; len < data.length; len++) {
					if (data[len] == 0) break;
				}
                
				out.write(data,0,len); 
               
				if(len+1<=TOTAL_SIZE) {
					out.close();
					break;
				}
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
			System.exit(1);
		} catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run(){
		identifyReq();
	}
	
	private void test(String n) {
		System.out.println(n);
	}
}
