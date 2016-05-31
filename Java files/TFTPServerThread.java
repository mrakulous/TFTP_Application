import java.io.*;
import java.net.*;
import java.util.*;

//import TFTPServer.Request;

public class TFTPServerThread implements Runnable
{
	private static enum Request { READ, WRITE, ERROR};
	private static final int DATA_SIZE = 512;
	private static final int TOTAL_SIZE = DATA_SIZE+4;  
	private static final int DATA_PACKET = 3;
	private static final int ACK_PACKET = 4;
	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket Socket;
	private String mode;
	private Request req;
	private String errMsg;
	private int port;

	private boolean firstTime = true;
	private Byte leftByte;
	private Byte rightByte;
	
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
	
	/*public void  formErrPacket (String msg){
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
	    
	}*/
	
	   
	public void identifyReq()
	{
		String filename = "";
		byte[] data = receivedPacket.getData();
		int len = receivedPacket.getLength();
		int fileNameSize=0;//size of file nname
		int modecount=0;//size of mode name
	             
		if (data[0]!=0) req = Request.ERROR; // bad
		if (data[1]==1) req = Request.READ; // could be read
		else if (data[1]==2) req = Request.WRITE; // could be write
		else req = Request.ERROR; // bad
		
		if (req!=Request.ERROR) { // check for filename
			for(fileNameSize=2; fileNameSize<len; fileNameSize++) {
				if (data[fileNameSize] == 0) break;
			}    
			if (fileNameSize==len) req=Request.ERROR; // didn't find a 0 byte
			if (fileNameSize==2) req=Request.ERROR; // filename is 0 bytes long
			filename = new String(data,2,fileNameSize-2);
		}
		 
		if(req!=Request.ERROR) { // check for mode
			for(modecount=fileNameSize+1;modecount<len;modecount++) { 
				if (data[modecount] == 0) break;
			}
			if (modecount==len) req=Request.ERROR; // didn't find a 0 byte
			if (modecount==fileNameSize+1) req=Request.ERROR; // mode is 0 bytes long
			mode = new String(data,fileNameSize,modecount-fileNameSize-1);
		}
		         
		if(modecount!=len-1) req=Request.ERROR; // other stuff at end of packet        

		//System.out.println("Type of request: " + req);
		
		// Create a response.
		
		if(req==Request.READ) {
			read(filename);
		} else if(req==Request.WRITE) {
			write(filename);
		}
	}

	/**
	 * Retrieve text file from directory, put it into packet and send to client
	 * @throws SocketException 
	 */
	 public void read(String fn) {
		 
		String filename = fn; 
		Byte blocknum1= new Byte((byte) 0);
		Byte blocknum2= new Byte((byte) 1);
		Byte ackByte1;
		Byte ackByte2;
		int dataCheck, len;
		DatagramPacket receivePacket;
		
		try {
			//read in file
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
			do {
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				int i = 0;
				
				dataCheck = in.read(data);
				if(dataCheck==-1) {
					len = 0;
				} else {
					len = dataCheck;
				}

				msg[0] = 0;
				msg[1] = DATA_PACKET;
				msg[2] = blocknum1;
				msg[3] = blocknum2;
				
			  //System.arraycopy(src, srcLoc, dest, destLoc, len)
				if(len != 0) {
					System.arraycopy(data, 0, msg, 4, len);
				}
				
				
				/*if(this.port == 0){
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
					sendPacket = new DatagramPacket(msg, len+4, receivedPacket.getAddress(), receivedPacket.getPort());
					try {
						Socket.send(sendPacket);
					} catch (IOException e){
						e.printStackTrace();
						System.exit(1);
					}
				//}// end port check 
				
				//  re-send the data if it has the wrong port 
				/*while(receivedPacket.getData()[1] == 5){
					if(receivedPacket.getData()[3] == 4){
						System.exit(1);
					} else {
						Socket.send(sendPacket);
					}
				}*/
				
				sendPacket = new DatagramPacket(msg, len+4, receivedPacket.getAddress(), receivedPacket.getPort());
				
				System.out.println("Server: Sending DATA packet to simulator.");
		        System.out.println("To host: " + sendPacket.getAddress());
		        System.out.println("Destination host port: " + sendPacket.getPort());
		        int packetLength = sendPacket.getLength();
		        System.out.println("Length: " + packetLength);
		        System.out.println("Block Number: " + blocknum1.toString() + blocknum2.toString());
		        System.out.println("Contents(bytes): " + msg);
		        String contents = new String(msg, 4, DATA_SIZE);
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
		        
				if(blocknum1 < 256) {
					if(blocknum2 == 255){
						blocknum1++;
						blocknum2 = 0;
					} else {
						blocknum2++;
					}
				} else {
					System.out.println("You have reached the maximum memory limit.  Aborting...");
					System.exit(1);
				}
		        
				// check for error 4
				/*if(receivedPacket.getData()[0] != 0 || receivedPacket.getData()[1] != 3 ||
						receivedPacket.getData()[2] != blocknum1  || receivedPacket.getData()[3] !=blocknum2) {
					String error4 = "Format Mistake";
					formErrPacket(error4);
				}*/
				
			    receivedPacket = new DatagramPacket(msg, msg.length);
        		
				try {
					Socket.receive(receivedPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				//get block number of ack packet
				ackByte1 = new Byte(receivedPacket.getData()[2]);
				ackByte2 = new Byte(receivedPacket.getData()[3]);
				
		        System.out.println("Server: ACK Packet received from simulator.");
		        System.out.println("From host: " + receivedPacket.getAddress());
		        System.out.println("Host port: " + receivedPacket.getPort());
		        packetLength = receivedPacket.getLength();
		        System.out.println("Packet Length: " + packetLength);
		        System.out.println("Block Number: " + ackByte1.toString() + ackByte2.toString());
		        System.out.println("Contents(bytes): " + msg);
		        System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
			    
		        try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
		        
			} while (len==DATA_SIZE);
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
	public void write(String fn) {
		
		String filename = fn;
		Byte blocknum1= new Byte((byte) 0);
		Byte blocknum2= new Byte((byte) 0);
		String contents;
   		
		try {
			// The file to get the data from.
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("new"+filename));
   			
			for(;;) {
				int len;
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				byte[] ack = new byte[4];
                
				ack[0] = 0;
				ack[1] = ACK_PACKET;
				ack[2] = blocknum1;
				ack[3] = blocknum2;
                
				// check for error port 
				/*if(this.port == 0){
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
				} else {*/
				
					sendPacket = new DatagramPacket(ack, ack.length, receivedPacket.getAddress(), receivedPacket.getPort());
					
					System.out.println("Server: Sending ACK packet to simulator.");
			        System.out.println("To host: " + sendPacket.getAddress());
			        System.out.println("Destination host port: " + sendPacket.getPort());
			        len = sendPacket.getLength();
			        System.out.println("Packet Length: " + len);
			        if(firstTime) {
						// Do nothing
			        	firstTime = false;
			        }
				    else {
				    	System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
				    }
			        
				    System.out.println("Contents(bytes): " + ack);
				    
				    if(len > 4) {
				    	// It is not an ACK packet
				    	contents = new String(ack, 4, DATA_SIZE);
				    	System.out.println("Contents(string): \n" + contents + "\n");
				    }
				    else {
				    	// It is an ACK packet
				    	System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
				    }
			        
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
				//} end check port
                
					if(blocknum1 < 256) {
						if(blocknum2 == 255){
							blocknum1++;
							blocknum2 = 0;
						} else {
							blocknum2++;
						}
					} else {
						System.out.println("You have reached the maximum memory limit.  Aborting...");
						System.exit(1);
					}
				
				receivedPacket = new DatagramPacket(msg, msg.length);
				
				try {
					Socket.receive(receivedPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				leftByte = new Byte(receivedPacket.getData()[2]);
				rightByte = new Byte(receivedPacket.getData()[3]);
				
				/*
				//  re-send the data if it has the wrong port 
				while(receivedPacket.getData()[1] == 5) {
					if(receivedPacket.getData()[3] == 4) {
						System.exit(1);
					} else {
						Socket.send(sendPacket);
					}
				}
				*/
				// check for error 4
				/*if(receivedPacket.getData()[0] != 0 || receivedPacket.getData()[1] != 3 ||
						receivedPacket.getData()[2] != blocknum1  || receivedPacket.getData()[3] !=blocknum2) {
					String error4 = "Format Mistake";
					formErrPacket (error4 ); 
				}*/
				
				System.out.println("Server: DATA Packet received from simulator.");
		        System.out.println("From host: " + receivedPacket.getAddress());
		        System.out.println("Host port: " + receivedPacket.getPort());
		        len = receivedPacket.getLength();
		        System.out.println("Length: " + len);
			    System.out.println("Contents(bytes): " + msg);
			    if(firstTime) {
			    	// filename and mode
			    	contents = new String(msg, 0, msg.length);
			    	System.out.println("Contents(string): \n" + contents + "\n");
			    }
			    else {
			    	if(len > 4) {
			    		// It is not an ACK packet
			    		contents = new String(msg, 4, DATA_SIZE);
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
                
				System.arraycopy(receivedPacket.getData(), 4, data, 0, receivedPacket.getLength()-4);
                
				for(len = 4; len < data.length; len++) {
					if (data[len] == 0) break;
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
	
	private void testString(String n) {
		System.out.println(n);
	}
}
