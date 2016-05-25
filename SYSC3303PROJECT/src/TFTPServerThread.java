import java.io.*;
import java.net.*;
import java.util.*;

//import TFTPServer.Request;

public class TFTPServerThread implements Runnable
{
	public static enum Request { READ, WRITE, ERROR};
	public static final int DATA_SIZE = 512;
	public static final int TOTAL_SIZE = DATA_SIZE+4;  
	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket Socket;
	private String filename;
	private String mode;
	private Request req;
	private String errMsg;
	private int port;
	
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
	
	public void  formErrPacket (String msg){
		DatagramPacket errorPacket ; 
		byte [] msgbytes = msg.getBytes(); 
		int msgLen = msgbytes.length; 
		msgLen += 5;
		byte[] error = new byte [msgLen]; 
		error[0] = 0 ; 
		error[1] = 5 ; 
		error[2] = 0 ; 
		error[3] = 4 ; 
		System.arraycopy(msgbytes, 0,error , 4,msgLen );
		error[msgLen-1] = 0 ; 
	    errorPacket  = new DatagramPacket (error,error.length, receivedPacket.getAddress(),  receivedPacket.getPort());
	    try{
			   Socket .send(errorPacket);
		     } catch (IOException e){
			   e.printStackTrace();
			   System.exit(1);
		     }
	    // terminate
	    System.exit(1);
	    
	}
	
	public void identifyReq()
	{
		byte[] data = receivedPacket.getData();
		int len = receivedPacket.getLength();
		int filecount=0;
		int modecount=0; 
	
		// Error handling for invalid opcode in RRQ and WRQ
		if (data[0]!=0 || data[1]!=1 || data[1]!=2) 
			req = Request.ERROR; // bad
		    errMsg = "Invalid Read Or Write Request";
		    formErrPacket (errMsg);
		  
		if (data[1]==1)
			req = Request.READ; // could be read
		else if (data[1]==2)
	       req = Request.WRITE; // could be write
	   
		
		//else req = Request.ERROR; // bad
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
		 
		byte blocknum1=0;
		byte blocknum2=1;
		int len;
		DatagramPacket receivePacket;
		
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
			do {
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
					if(msg[i]==0) {
						break;
					} else {
						i++;
					}
				}
				msg[i] = 0;
				i++;
				
				if(this.port == 0){
					this.port =  receivedPacket.getPort();
				}
				
				if(this.port !=  receivedPacket.getPort()){
					// create a error datagram with error 5
					byte[] err5 = new byte[TOTAL_SIZE];
					err5[0] = 0;
					err5[1] = 5;
					err5[2] = 0;
					err5[3] = 5;
					// the port is not the same
					String error = "Unknown Port";
					System.out.println("Error 5: unknown port");
					System.arraycopy(error.getBytes(), 0, err5, 4, error.getBytes().length);
					err5[error.getBytes().length+4] = 0;
					// create the datagram Packet
					DatagramPacket errorPacket5 = new DatagramPacket(err5, err5.length,  receivedPacket.getAddress(),  receivedPacket.getPort());
					// send the pakcet and wait for the new packet
					 try{
					   Socket .send(errorPacket5);
				     } catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				     }
					//sendReceiveSocket.receive(receivePacket);
				} else {
					sendPacket = new DatagramPacket(msg, i+4, receivedPacket.getAddress(), receivedPacket.getPort());
					try {
						Socket.send(sendPacket);
					} catch (IOException e){
						e.printStackTrace();
						System.exit(1);
					}
				}// end port check 
				
				//  re-send the data if it has the wrong port 
				while(receivedPacket.getData()[1] == 5){
					if(receivedPacket.getData()[3] == 4){
						System.exit(1);
					} else {
						Socket.send(sendPacket);
					}
				}
				
				sendPacket = new DatagramPacket(msg, len, receivedPacket.getAddress(), receivedPacket.getPort());
				
				System.out.println("Server: Sending packet to simulator.");
		        System.out.println("To host: " + sendPacket.getAddress());
		        System.out.println("Destination host port: " + sendPacket.getPort());
		        int packetLength = sendPacket.getLength();
		        System.out.println("Length: " + packetLength);
		        System.out.println("Contents(bytes): " + msg);
		        String contents = new String(msg,0,packetLength);
		        System.out.println("Contents(string): \n" + contents + "\n");
		        
		        try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
		        
		        System.out.println("Server: Waiting for packet from simulator............" + "\n");
		        
		        try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
		        
				try {
					Socket.send(sendPacket);
				} catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				}
		        
				if(blocknum2 == 255){
					blocknum1++;
				}
				blocknum2++;
			    
				// check for error 4
				if(receivedPacket.getData()[0] != 0 || receivedPacket.getData()[1] != 3 ||
						receivedPacket.getData()[2] != blocknum1  || receivedPacket.getData()[3] !=blocknum2) {
					String error4 = "Format Mistake";
					formErrPacket(error4);
				}
				
			    receivedPacket = new DatagramPacket(msg, msg.length);
        		
				try {
					Socket.receive(receivedPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

		        System.out.println("Server: Packet received from simulator.");
		        System.out.println("From host: " + receivedPacket.getAddress());
		        System.out.println("Host port: " + receivedPacket.getPort());
		        packetLength = receivedPacket.getLength();
		        System.out.println("Length: " + packetLength);
		        System.out.println("Contents(bytes): " + msg);
		        contents = new String(msg,0,packetLength);
		        System.out.println("Contents(string): \n" + contents + "\n");
		        
			} while (len>0);
				if(len<DATA_SIZE) {
					in.close();
				}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

        /*
	* Write the received datagram packet to an output file.
	*/
	public void write() {

		byte blocknum1=0;
		byte blocknum2=0;
   		
		try {
			// The file to get the data from.
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
   			
			for(;;) {
				int len;
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				byte[] ack = new byte[4];
                
				ack[0] = 0;
				ack[1] = 4;
				ack[2] = blocknum1;
				ack[3] = blocknum2;
                
				// check for error port 
				if(this.port == 0){
					this.port =  receivedPacket.getPort();
				}
				
				if(this.port !=  receivedPacket.getPort()){
					// create a error datagram with error 5
					byte[] err5 = new byte[TOTAL_SIZE];
					err5[0] = 0;
					err5[1] = 5;
					err5[2] = 0;
					err5[3] = 5;
					// the port is not the same
					String error = "Unknown Port";
					System.out.println("Error 5: unknown port");
					System.arraycopy(error.getBytes(), 0, err5, 4, error.getBytes().length);
					err5[error.getBytes().length+4] = 0;
					// create the datagram Packet
					DatagramPacket errorPacket5 = new DatagramPacket(err5, err5.length,  receivedPacket.getAddress(),  receivedPacket.getPort());
					// send the pakcet and wait for the new packet
					 try{
					   Socket .send(errorPacket5);
				     } catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				     }
					//sendReceiveSocket.receive(receivePacket);
				} else {
				
					sendPacket = new DatagramPacket(ack, ack.length, receivedPacket.getAddress(), receivedPacket.getPort());
					
					System.out.println("Server: Sending packet to simulator.");
			        System.out.println("To host: " + sendPacket.getAddress());
			        System.out.println("Destination host port: " + sendPacket.getPort());
			        len = sendPacket.getLength();
			        System.out.println("Length: " + len);
			        System.out.println("Contents(bytes): " + ack);
			        String contents = new String(ack,0,len);
			        System.out.println("Contents(string): " + contents + "\n");
					
			        try {
			             Thread.sleep(500);
			        } catch (InterruptedException e) {
			        	 e.printStackTrace();
			        }
			        
			        System.out.println("Server: Waiting for packet from simulator............" + "\n");
			        
			        try {
			             Thread.sleep(500);
			        } catch (InterruptedException e) {
			        	 e.printStackTrace();
			        }
			        
					try {
						Socket.send(sendPacket);
					} catch (IOException e){
						e.printStackTrace();
						System.exit(1);
					}
				}// end port checking 
					
				if(blocknum2 == 255) {
					blocknum1++;
				}
				
				blocknum2++;
				
				receivedPacket = new DatagramPacket(msg, msg.length);
				
				try {
					Socket.receive(receivedPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				//  re-send the data if it has the wrong port 
				while(receivedPacket.getData()[1] == 5) {
					if(receivedPacket.getData()[3] == 4) {
						System.exit(1);
					} else {
						Socket.send(sendPacket);
					}
				}
							
				// check for error 4
				if(receivedPacket.getData()[0] != 0 || receivedPacket.getData()[1] != 3 ||
						receivedPacket.getData()[2] != blocknum1  || receivedPacket.getData()[3] !=blocknum2) {
					String error4 = "Format Mistake";
					formErrPacket (error4 ); 
				}
				
				System.out.println("Server: Packet received from simulator.");
		        System.out.println("From host: " + receivedPacket.getAddress());
		        System.out.println("Host port: " + receivedPacket.getPort());
		        len = receivedPacket.getLength();
		        System.out.println("Length: " + len);
		        System.out.println("Contents(bytes): " + msg);
		        String contents = new String(msg,0,len);
		        System.out.println("Contents(string): " + contents + "\n");
		        
		        try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
                
				System.arraycopy(receivedPacket.getData(), 4, data, 0, receivedPacket.getLength()-4);
                
				for(len = 4; len<data.length; len++) {
					if(data[len] == 0) break;
				}
                
				out.write(data,0,len);
				
				if(len<DATA_SIZE) {
					System.out.println("#####  OPERATION COMPLETED.  #####" + "\n");
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
