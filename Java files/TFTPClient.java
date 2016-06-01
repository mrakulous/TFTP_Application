// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPClient {
	
	//SIM PORT
	private static final int simPort = 23;
	
	//MAX DATA PER PACKET
	public static final int DATA_SIZE = 512;
	
	//msg array size
	public static final int TOTAL_SIZE = DATA_SIZE+4;
	
	private static final int DATA_PACKET = 3;
	private static final int ACK_PACKET = 4;
	
	private static final int TIMEOUT = 50000;
	private static final int RETRANSMIT_TIME = 25000;
	
	private static final char QUIT = 'q';
	
	//REQUEST AND MODE TYPES
	public static enum Request { READ, WRITE, ERROR};
	public static enum Mode { NORMAL, TEST};
	
	//packets and sockets
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private String contents;

	//ACK counter
	private Byte ackCntL, ackCntR;//starting byte

	
   	public TFTPClient()
   	{
   		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
   	
   	public void runClient(Scanner readInput) {
   		Scanner re = readInput;
   		//String filepath, workingDir; -- To implement
   		String filename;
   		String mode;
   		Request req;
   		String checkInput;
   		int cmd = 0;
   		
   		while(true) {				
				System.out.print("[1]Read   [2]Write  [5]Shutdown, to Quit press \"q\" (not implemented) : ");
				checkInput = re.nextLine();
				if(checkInput.equals(QUIT)) {
					shutDown();
				} else {
					try {
				
						cmd = Integer.parseInt(checkInput);
						if(cmd == 1) {
							req = Request.READ;
							setAckCounter(req);
							break;
						} else if(cmd == 2) {
							req = Request.WRITE;
							setAckCounter(req);
							break;
						} else if (cmd == 5) {
							System.exit(1);
						}
					} catch(NumberFormatException e) {
						System.out.println("Please enter a valid option");
					}
				}
		}
   		
   		//read in filename
   		while(true) {
			try {
				System.out.print("Enter the file you would like to " + req.toString() +" (\"q\" to Quit): ");
				filename = re.nextLine();
				if(filename.equals(QUIT)) {
					shutDown();
				}
				//workingDir = System.getProperty("user.dir");
				//filepath = workingDir + "\\" + filename;
				File input = new File(filename);
				Scanner read = new Scanner(input);//used to verify if file is valid
				break;
			} catch(FileNotFoundException e) {
				System.out.println("File does not exist.  Please enter a valid file.");
			}
		}
   		
   		//*****************TO FIX******************
		while(true) {
			try {
				System.out.print("[1]netascii  [2]octet (\"q\" to Quit): ");
				checkInput = re.nextLine();
				if(checkInput.equals(QUIT))
					shutDown();
				cmd = Integer.parseInt(checkInput);
				if(cmd == 1) {
					mode = "netascii";
					break;
				} else if(cmd == 2) {
					mode = "octet";
					break;
				}
			} catch(NumberFormatException e) {
				System.out.println("Please enter a valid option");
			}
		}
   		//for(;;) {
   		run(filename, mode, req);
   		System.out.println("***Transfer Complete***");
   		System.out.println("");
   		//}
   	}

   	public void run(String fp, String filemode, Request r)
   	{
   			
   			String filepath = fp;
   			String mode = filemode;
   			Request req = r;
			
			byte[] msg = new byte[TOTAL_SIZE];
			
			msg[0] = 0;
			if(req==Request.READ) {
				msg[1] = 1;
				
			} else if(req==Request.WRITE) {
				msg[1] = 2;
			}
			
			
			int index = 2;
			//put file name into bytes
			byte[] fn = filepath.getBytes();
			System.arraycopy(fn,0,msg,index,fn.length);
			index = index + fn.length;
			msg[index] = 0;
			index++;
			
			byte[] md = mode.getBytes();
			System.arraycopy(md,0,msg,index,md.length);
			index = index + md.length;
			msg[index] = 0;
			index++;

			try {
				sendPacket = new DatagramPacket(msg, index, InetAddress.getLocalHost(), simPort);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("\nClient: Sending packet to simulator.");
	        System.out.println("To host: " + sendPacket.getAddress());
	        System.out.println("Destination host port: " + sendPacket.getPort());
	        int packetLength = sendPacket.getLength();
	        System.out.println("Packet Length: " + packetLength);
	        System.out.println("Contents(bytes): " + msg);
	        String contents = new String(msg,2,packetLength);
	        System.out.println("Contents(string): " + contents + "\n");
			
	        try {
	             Thread.sleep(500);
	        } catch (InterruptedException e) {
	        	 e.printStackTrace();
	        }
	        
	        System.out.println("Client: Waiting for packet from simulator............" + "\n");
	        
	        try {
	             Thread.sleep(500);
	         } catch (InterruptedException e) {
	        	 e.printStackTrace();
	         }
	        
	        //send request
			try{
            	sendReceiveSocket.send(sendPacket);
            } catch (IOException e) {
            	e.printStackTrace();
                System.exit(1);
            }
			
			if(req==Request.READ) {
				read(filepath);
			} else if(req==Request.WRITE) {
				write(filepath);
			}
	}
   	
   	public void read(String fp)
   	{
   		String filepath = fp;
   		Byte blocknum1=0;
		Byte blocknum2=1;
		//ackCntL = 0;
		//ackCntR = 1;//we are starting our read ack counter at 1
		
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("new"+filepath));
			for(;;) {
				int len;
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];

				receivePacket = new DatagramPacket(msg, msg.length);

				try {
					/*
					 * Network Error Handling 
					 * ACK retransmission is not needed as duplicate ack packets are disabled
					 * Only have check for initial timeout
					 */
					for(;;) {
						//set timeout time
						
						
						
						
						//*******ADD MULTIPLE SENDS BEFORE TIME OUT*********************************************
						sendReceiveSocket.setSoTimeout(TIMEOUT);
						
						//block socket, wait for packet
						sendReceiveSocket.receive(receivePacket);

						//****ERROR HANDLING: DUPLICATE DATA****
						
						//if incoming block number != ack counter + 1, keep waiting, 
						//check for duplicate
						//byte byteCheck1 = (byte) (ackCntR+1);
						//case where right ack byte counter is at max
						
						//get received packet number
						/*
						Byte leftByte = new Byte(receivePacket.getData()[2]);
						Byte rightByte = new Byte(receivePacket.getData()[3]);
											
						//compare block number to our counter
						
						/* if data block is 1 higher, send ack back and inc ack counter, else send same data block number*/
						/*
						if(leftByte.compareTo(ackCntL) == 0 && rightByte.compareTo(ackCntR) == 0) {
							incAckCounter();
							break;
						} else {
							System.out.println("Packet not as expected - error cannot be handled this iteration");
							System.exit(1);
						}
						
					 	*/
						break;
					}
				} catch (IOException e) {//CHANGE TO SEND DATA MORE THAN 5 TIMES
					System.out.println("No data received: Data lost.");
					System.out.println("Shutting down.");
					System.exit(1);
				}
				
				try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
				
				len = receivePacket.getLength();
				printDataPacket(receivePacket, msg);
		        
				try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
				
				Byte leftByte = new Byte(receivePacket.getData()[2]);
				Byte rightByte = new Byte(receivePacket.getData()[3]);
		        
		      //System.arraycopy(src, srcLoc, dest, destLoc, len)
				System.arraycopy(receivePacket.getData(), 4, data, 0, receivePacket.getLength()-4);

				
				if(!(ackCntL == 255 && ackCntR == 255)) {
					if(ackCntR == 255){
						
						// if the data packet is correct (1 block number higher)
						if((leftByte) == ackCntL.byteValue()+1 && (rightByte)== 0) {
							
							for(len = 4; len < data.length; len++) {
								if (data[len] == 0) break;
							}
	
							out.write(data,0,len);
							
						} 
						    
					} else {
						
						if (leftByte == ackCntL.byteValue() && (rightByte)== ackCntR.byteValue()+1){
							for(len = 4; len < data.length; len++) {
								if (data[len] == 0) break;
							}
	
							out.write(data,0,len);
							}
						
					}
				} else {
					System.out.println("Memory limit reached. Aborting...");
					System.exit(1);
				}
					

				byte[] ack = new byte[4];
				ack[0] = 0;
				ack[1] = ACK_PACKET;
				ack[2] = leftByte;
				ack[3] = rightByte;

				sendPacket = new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), simPort);
				
				printAckPacket(sendPacket, leftByte, rightByte);
		        
		        if(sendPacket.getData().length > 4) {
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
		        
		        System.out.println("Client: Waiting for packet from simulator............" + "\n");
		        
		        try {
		             Thread.sleep(500);
		        } catch (InterruptedException e) {
		        	 e.printStackTrace();
		        }
		        
		        //send 
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				//if this is he last data packet, end
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
			
				incAckCounter(leftByte, rightByte);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
   	
public void write(String fp) {
		
		String filepath = fp;
		Byte blocknum1= new Byte((byte)0);
		Byte blocknum2= new Byte((byte)0);//first block sent
		//ackCntL = 0;
		//ackCntR = 0;//start ack counter at 0
		int len, dataCheck;
		boolean firstRun = true;
		
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(filepath));
			do {
				byte[] msg = new byte[TOTAL_SIZE]; // msg has size 516
				byte[] data = new byte[DATA_SIZE]; // data has size 512
				int i = 0;//DATA DELAY TIMER

				receivePacket = new DatagramPacket(msg, msg.length);
				
				try {
					// Network Error Handling 
					
					
					for(;;) {
						sendReceiveSocket.receive(receivePacket);
						/*
						while(true) {
							try {
								//sendReceiveSocket.setSoTimeout(TIMEOUT);
								
								break;
							} catch (SocketTimeoutException e) {
								sendReceiveSocket.send(sendPacket);
							}
						}
						*/
						//set timeout time

						//****ERROR HANDLING: DATA LOSS****
						
						
						//block socket, wait for packet
						
						//****ERROR HANDLING: DUPLICATE DATA****
						
						//if incoming block number != ack counter + 1, keep waiting, 
						//check for duplicate
						/*
						byte byteCheck1 = (byte) (ackCntR+1);
						//case where right ack byte counter is at max
						if(ackCntR==255) {
							byte leftCount = (byte) (ackCntL+1);
							if(receivePacket.getData()[2]==leftCount && receivePacket.getData()[3]==byteCheck1) {
								break;
							}
						}else if(receivePacket.getData()[2]==ackCntL && receivePacket.getData()[3]==byteCheck1) {
							break;
						} else {
							System.out.println("Packet not as expected - error cannot be handled this iteration");
							System.exit(1);
						}
						*/
						
						//Get block number from received packet to compare
						Byte leftByte = new Byte(receivePacket.getData()[2]);
						Byte rightByte = new Byte(receivePacket.getData()[3]);
						
						/*
						 * BUG IS HERE - LOGIC IS OFF
						 */
						
						//If ack counter matches packet block number, continue, else break
						if(firstRun) {
							if(leftByte.compareTo(getAckCntL()) == 0 && rightByte.compareTo(getAckCntR()) == 0) {
								//increment ack counter if correct block number received
								incAckCounter(leftByte, rightByte);	
								break;
							}
						} else {
							//if duplicate ACK packet, ignore and wait
							if(!(leftByte.intValue() == getAckCntL() && rightByte.intValue() == getAckCntR())) {
								if(leftByte.compareTo(getAckCntL()) == 0 && rightByte.compareTo(getAckCntR()) == 0) {
									//increment ack counter if correct block number received
									incAckCounter(leftByte, rightByte);	
									break;
								} 
							}
						}
						firstRun = false;
					}//end for
					
				} catch (IOException e) {
					System.out.println("No data received: Data lost.");
					System.out.println("Shutting down.");
					System.exit(1);
				}

				
				Byte leftByte = new Byte(receivePacket.getData()[2]);
				Byte rightByte = new Byte(receivePacket.getData()[3]);
				String contents;
				
				System.out.println("Client: Packet received from simulator.");
		        System.out.println("From host: " + receivePacket.getAddress());
		        System.out.println("Host port: " + receivePacket.getPort());
		        int packetLength = receivePacket.getLength();
		        System.out.println("Length: " + packetLength);
		        System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
		        System.out.println("Contents(bytes): " + msg);
		        if(packetLength > 4) {
		        	// It is not an ACK packet
		        	contents = new String(msg, 4, DATA_SIZE);
		        	System.out.println("Contents(string): \n" + contents + "\n");
		        }
		        else {
		        	// It is an ACK packet
		        	System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
		        }

		        
		        if (blocknum1 < 256) {
					if(blocknum2 == 255) {
						blocknum1++;
						blocknum2 = 0;
					} else {
						blocknum2++;
					}
		        } else {
		        	System.out.println("Maximum memory reached.  Aborting...");
		        	System.exit(1);
		        }
		        
		        //
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
					
				sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), simPort);
				
				System.out.println("Client: Sending packet to simulator.");
		        System.out.println("To host: " + sendPacket.getAddress());
		        System.out.println("Destination host port: " + sendPacket.getPort());
		        packetLength = sendPacket.getLength();
		        System.out.println("Packet Length: " + packetLength);
		        System.out.println("Block Number: " + blocknum1.toString() + blocknum2.toString());
		        System.out.println("Contents(bytes): " + msg);
		        if(packetLength > 4) {
		        	// It is not an ACK packet
		        	contents = new String(msg, 4, DATA_SIZE);
		        	System.out.println("Contents(string): \n" + contents + "\n");
		        }
		        else {
		        	// It is an ACK packet
		        	System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
		        }

		        
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				}
				
				testString("THIS IS TEST FOR LEN: " + len);
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

	public static void main(String args[])
	{
		TFTPClient c = new TFTPClient();
		Scanner re = new Scanner(System.in);
		for(;;) {
			c.runClient(re);
		}
	}
	
	private void testString(String n) {
		System.out.println(n);
	}
	
	private void shutDown() {
		System.out.println("Shutting Down...");
		System.out.println("Done.");
		System.exit(1);
	}
	
	/*
	private boolean checkAckPacket(DatagramPacket p) {
		boolean isAck = false;
		DatagramPacket receivePacket = p;
		
		Byte leftByte = new Byte(receivePacket.getData()[2]);
		Byte rightByte = new Byte(receivePacket.getData()[3]);
		
		//If ack counter matches packet block number, continue, else break
		if(leftByte.compareTo(ackCntL) == 0 && rightByte.compareTo(ackCntR) == 0) {
			//increment ack counter if correct block number received
			
			
			
		
		}
	}
	*/
	private void setAckCounter(Request r) {
		Request req = r;
		if(req == Request.READ) {
			ackCntL = 0;
			ackCntR = 1;
		} else if (req == Request.WRITE) {
			ackCntL = 0;
			ackCntR = 0;
		}
	}
		
	private void incAckCounter(Byte l, Byte r) {
		
		Byte leftByte = l;
		Byte rightByte = r;
		/*if(ackCntL.intValue()<256) {
			if(ackCntR.intValue() == 255) {
				ackCntL++;
				ackCntR=0;
			} else {
				ackCntR++;
			}
		} else {
			System.out.println("File too big, exiting program.");
			System.exit(1);
		}*/
		
		if(!(ackCntL == 255 && ackCntR == 255)) {
			if(ackCntR == 255){
				
				// if the data packet is correct (1 block number higher)
				if((leftByte) == ackCntL.byteValue()+1 && (rightByte)== 0) {
					ackCntL++;
					ackCntR=0;
				} 
				    
			} else {
				
				if (leftByte == ackCntL.byteValue() && (rightByte)== ackCntR.byteValue()+1){
					ackCntR++;
				}
			}
		} else {
			System.out.println("Memory limit reached. Aborting...");
			System.exit(1);
		}
	}
	
	private void printDataPacket(DatagramPacket p, byte[] m) {
		DatagramPacket packet = p;
		byte[] msg = m;
		int len = 0;
		
		Byte leftByte = new Byte(packet.getData()[2]);
		Byte rightByte = new Byte(packet.getData()[3]);
		
		System.out.println("Client: DATA Packet received from simulator.");
        System.out.println("From host: " + packet.getAddress());
        System.out.println("Host port: " + packet.getPort());
        len = packet.getLength();
        System.out.println("Packet Length: " + len);
        System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
        System.out.println("Contents(bytes): " + msg);
        String contents = new String(msg,4,len-4);
        System.out.println("Contents(string): \n" + contents + "\n");
	}
	
	private void printAckPacket(DatagramPacket p, Byte l, Byte r) {
		DatagramPacket packet = p;
		Byte leftByte = l;
		Byte rightByte = r;
		int packetLength;
		
		System.out.println("Client: Sending ACK packet to simulator.");
        System.out.println("To host: " + packet.getAddress());
        System.out.println("Destination host port: " + packet.getPort());
        packetLength = packet.getData().length;
        System.out.println("Packet Length: " + packetLength);
        System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
        System.out.println("Contents(bytes): " + packet.getData());
	}
	
	private Byte getAckCntL() {
		return ackCntL;
	}
	
	private Byte getAckCntR() {
		return ackCntR;
	}
}
