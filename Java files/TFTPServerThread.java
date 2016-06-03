package iter3;

import java.io.*;
import java.net.*;
import java.util.*;

import iter3.TFTPClient.Request;

//import TFTPServer.Request;

public class TFTPServerThread implements Runnable
{
	private static enum Request { READ, WRITE, ERROR};
	private static final int DATA_SIZE = 512;
	private static final int TOTAL_SIZE = DATA_SIZE+4;  
	private static final int DATA_PACKET = 3;
	private static final int ACK_PACKET = 4;
	
	private static final int RETRANSMIT_TIME = 10000;
	
	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket Socket;
	private String mode;
	private Request req;
	private String errMsg;
	private int port;

	private boolean firstTime = true;
	//ACK counter
	private Byte ackCntL, ackCntR;//starting byte
	
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
			setAckCounter(req);
			read(filename);
		} else if(req==Request.WRITE) {
			setAckCounter(req);
			write(filename);
		}
	}

	/**
	 * Retrieve text file from directory, put it into packet and send to client
	 * @throws SocketException 
	 */
	 public void read(String fn) {
		 
		String filename = fn; 
		Byte leftByte = null;
		Byte rightByte = null;
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
				msg[2] = getAckCntL();
				msg[3] = getAckCntR();
				
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
				
				sendPacket = new DatagramPacket(msg, msg.length, receivedPacket.getAddress(), receivedPacket.getPort());
				int packetLength = sendPacket.getLength();
				
				System.out.println("Server: Sending DATA packet to simulator.");
		        System.out.println("To host: " + sendPacket.getAddress());
		        System.out.println("Destination host port: " + sendPacket.getPort());
		        System.out.println("Length: " + (len+4));
		        System.out.println("Block Number: " + getAckCntL().toString() + getAckCntR().toString());
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
		        
				receivePacket = new DatagramPacket(msg, msg.length);
				try { 
					// Network Error Handling 
					for(;;) {
						//****ERROR HANDLING: DATA LOSS****
						while(i<=5) {
							try {
								Socket.setSoTimeout(RETRANSMIT_TIME);
								Socket.receive(receivePacket);
								break;
							} catch (SocketTimeoutException e) {
								System.out.println("try"+i);
								if(i == 5){
									System.out.println("end");
									System.exit(1);
								}
								Socket.send(sendPacket);
								i++;
							}
						}
						
						//****ERROR HANDLING: DUPLICATE ACK****
						
						//Get block number from received packet to compare
						leftByte = new Byte(receivePacket.getData()[2]);
						rightByte = new Byte(receivePacket.getData()[3]);
						
						//If incoming ack packet is correct ack packet, inc and break
						if(leftByte.compareTo(getAckCntL()) == 0 && rightByte.compareTo(getAckCntR()) == 0) {
							//increment ack counter if correct block number received
							incReadAckCounter(leftByte, rightByte);	
							break;
						} else {
							//if duplicate ACK packet, ignore and wait
							System.out.println("\n*****DUPLICATE ACK RECEIVED - IGNORING PACKET*****\n");
						}
					}//end for
					
				} catch (IOException e) {
					System.out.println("No data received: Data lost.");
					System.out.println("Shutting down.");
					System.exit(1);
				}

				
		        System.out.println("Server: ACK Packet received from simulator.");
		        System.out.println("From host: " + receivedPacket.getAddress());
		        System.out.println("Host port: " + receivedPacket.getPort());
		        packetLength = receivedPacket.getLength();
		        System.out.println("Packet Length: " + packetLength);
		        System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
		        System.out.println("Contents(bytes): " + msg);
		        System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
			    
		        try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
		        
		        if(in.read() == -1){
					break;
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
		String contents;
		int len;
		Byte leftByte = 0;
        Byte rightByte = 0;	
   		
		try {
			// The file to get the data from.
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("new"+filename));
   			
			for(;;) {
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				byte[] ack = new byte[4];			
				
				ack[0] = 0;
				ack[1] = ACK_PACKET;
				ack[2] = leftByte;
				ack[3] = rightByte;
                
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
				
				receivedPacket = new DatagramPacket(msg, msg.length);
				
				try {
					Socket.receive(receivedPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				leftByte = new Byte(receivedPacket.getData()[2]);
				rightByte = new Byte(receivedPacket.getData()[3]);
				
				System.arraycopy(receivedPacket.getData(), 4, data, 0, receivedPacket.getLength()-4);
				
				if(!(ackCntL == 127 && ackCntR == 127)) {
					if(ackCntR == 127){
						// if the data packet is correct (1 block number higher)
						if(leftByte == ackCntL.byteValue()+1 && rightByte == 0) {
							out.write(data,0,receivedPacket.getLength()-4);
						}
					} else {
						if (leftByte == ackCntL.byteValue() && rightByte== ackCntR.byteValue()+1){	
							System.out.println(""+receivedPacket.getLength());
							out.write(data,0,receivedPacket.getLength()-4);
							}
					}
				} else {
					System.out.println("Memory limit reached. Aborting...");
					System.exit(1);
				}
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
				incWriteAckCounter(leftByte, rightByte);
				
				if(len<DATA_SIZE) {
					out.close();
					try {
			             Thread.sleep(500);
			        } catch (InterruptedException e) {
			        	 e.printStackTrace();
			        }
					
					System.out.println("#####  OPERATION COMPLETED.  #####" + "\n");
					/*
					 * IMPLEMENT RE-PROMPT FOR NEW FILE TRANSFER
					 */
					//System.exit(1);
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
	
	private void incReadAckCounter(Byte left, Byte right) {
		// increment the ack count for write
		if(!(ackCntL == 127 && ackCntR == 127)) {
			// if it is the correct block coming in then increment the ack count or ignore counting up the ack
			if(ackCntL.compareTo(left) == 0 && ackCntR.compareTo(right) == 0) {
				if(ackCntR == 127){
					ackCntL++;
					ackCntR = 0;
				} else {
					ackCntR++;
				}
			}
		} else {
			System.out.println("Memory limit reached. Aborting...");
			System.exit(1);
		}		
	}
	
	private void incWriteAckCounter(Byte left, Byte right) {
		// increment the ack count for read
		if(!(ackCntL == 127 && ackCntR == 127)) {
			// if it is the correct block coming in then increment the ack count or ignore counting up the ack
			if(ackCntR == 127){
				// if the data packet is correct (1 block number higher)
				if(left == ackCntL.byteValue()+1 && right == 0) { // 
					ackCntL++;
					ackCntR = 0;
				}
			} else {
				if (left == ackCntL.byteValue() && right== ackCntR.byteValue()+1){
					ackCntR++;
				}
			}
		} else {
			System.out.println("Memory limit reached. Aborting...");
			System.exit(1);
		}
	}
	
	private void setAckCounter(Request req) {
		// set the ack counter every new operation
		if(req == Request.READ) {
			ackCntL = 0;
			ackCntR = 1;
		} else if (req == Request.WRITE) {
			ackCntL = 0;
			ackCntR = 0;
		}
	}
	
	private Byte getAckCntL() {
		// returns block left
		return ackCntL;
	}
	
	private Byte getAckCntR() {
		// returns block right
		return ackCntR;
	}

	public void run(){
		identifyReq();
	}
}
