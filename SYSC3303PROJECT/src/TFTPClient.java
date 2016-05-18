// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPClient {
	private static final int DATA_SIZE = 512;
	private static final int TOTAL_SIZE = DATA_SIZE +4;
	private static final int TIMEOUT = 1000;
	private static final int RETRANSMIT_TIME = 500;
	private static final String EXIT_CMD = "q";
	public static enum Request { READ, WRITE, ERROR};
	public static enum Mode { NORMAL, TEST};
	public Request req;
	private String pathName;
	private String mode;
	private String workingDir;
	private byte ackCntL=0;
	private byte ackCntR=0;//starting byte

	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;

   	public TFTPClient()
   	{
   		try {
			sendReceiveSocket = new DatagramSocket();
			workingDir = System.getProperty("user.dir") + "\\clientSide\\";//get working directory
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

   	public void run()
   	{
		Scanner re = new Scanner(System.in);
		int cmd = 0;
		for(;;) {
			
			//Get Request Type
			while(true) {
				try {
					System.out.print("[1]Read   [2]Write  [5]Shutdown : ");
					cmd = Integer.parseInt(re.nextLine());
					if(cmd == 1) {
						req = Request.READ;
						break;
					} else if(cmd == 2) {
						req = Request.WRITE;
						break;
					} else if (cmd == 5) {
						System.exit(1);
					}
				} catch(NumberFormatException e) {
					System.out.println("Please enter a valid option");
				}
			}

			//Get File Name
			while(true) {
				try {
					if(req==Request.READ) {
						System.out.print("Enter file to read (enter 'q' to quit) : ");
					} else if (req==Request.WRITE) {
						System.out.print("Enter file to write (enter 'q' to quit) : ");
					}
						String fileName = re.nextLine();
						if(fileName.length()==EXIT_CMD.length() && fileName.toLowerCase().equals(EXIT_CMD)){
							System.out.println("PeAcE");
							System.exit(1);
						}
						pathName = workingDir + fileName;//add file name to file path
						File input = new File(pathName);
						Scanner read = new Scanner(input);//if a file, break, else throw exception
						break;
					
				} catch(FileNotFoundException e) {
					System.out.println("Please enter a valid filename");
				}
			}
			
			//Get mode
			while(true) {
				try {
					System.out.print("[1]netascii  [2]octet : ");
					cmd = Integer.parseInt(re.nextLine());
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
			mode = "octet";
			byte[] msg = new byte[TOTAL_SIZE];
			
			msg[0] = 0;
			if(req==Request.READ) {
				msg[1] = 1;
				
			} else if(req==Request.WRITE) {
				msg[1] = 2;
			}
			int index = 2;
			
			byte[] fn = pathName.getBytes();
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
				sendPacket = new DatagramPacket(msg, index, InetAddress.getLocalHost(), 23);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			try{
            	sendPackets(sendPacket);
            } catch (IOException e) {
            	e.printStackTrace();
                System.exit(1);
            }
			
			if(req==Request.READ) {
				read();
			} else if(req==Request.WRITE) {
				write();
			}
		} 
	}
   	
   	private void sendPackets(DatagramPacket sendPacket2) throws IOException {
		// TODO Auto-generated method stub
   		sendReceiveSocket.send(sendPacket);
	}

	public void read()
   	{
		try {
			//Put file into clientSide folder with same name as server
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(pathName));
			for(;;) {
				int len;
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				
				//create receive Datagram Packet
				receivePacket = new DatagramPacket(msg, msg.length);
				
				try {
					
					/*
					 * Network Error Handling 
					 * 
					 * ACK retransmission is not needed as duplicate ack packets are disabled
					 * 
					 * Only have check for initial timeout
					 */
					
								
					for(;;) {
										
						//set timeout time
						

						//****ERROR HANDLING: DATA LOSS****
						sendReceiveSocket.setSoTimeout(TIMEOUT);
						
						//block socket, wait for packet
						sendReceiveSocket.receive(receivePacket);

							
						//****ERROR HANDLING: DUPLICATE DATA****
						
						//if incoming block number != ack counter + 1, keep waiting, 
						//check for duplicate
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
							System.out.println("Error cannot be handled this iteration");
							System.exit(1);
						}
					}
				} catch (IOException e) {
					System.out.println("No data received: Data lost.");
					System.out.println("Shutting down.");
					System.exit(1);
				}
						

				
				//get data from receivePacket using offset, copy to byte array
				System.arraycopy(receivePacket.getData(), 4, data, 0, receivePacket.getLength()-4);
				
				//check if this packet is last packet (<512 bytes) is so, break
				for(len = 4; len < data.length; len++) {
					if (data[len] == 0) break;
				}

				//write byte array to file
				out.write(data,0,len);
				System.out.println("Sending ack");

				//create ack packet
				byte[] ack = new byte[4];
				ack[0] = 0;
				ack[1] = 4;
				ack[2] = ackCntL;
				ack[3] = ackCntR;
				//check for duplicate data

				sendPacket = new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 23);

				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
		                 
				if(len+1<=TOTAL_SIZE) {
					out.close();
					break;
				}

				if(ackCntR == 255) {
					ackCntL++;
				}
				ackCntR++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
   	
	public void write() {
		byte blocknum1=0;
		byte blocknum2=1;
		int len;
		try {
			//Take file from clientSide folder
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(pathName));
			do {
				byte[] msg = new byte[TOTAL_SIZE];
				byte[] data = new byte[DATA_SIZE];
				int i = 0;//DATA DELAY TIMER
				
				//receive packet for ack
				receivePacket = new DatagramPacket(msg, msg.length);

				try {
					
					/*
					 * Network Error Handling 
					 * 
					 * 
					 * 
					 * 
					 */
					
								
					for(;;) {
										
						
						//set packet delay time to .5s
						while (i<2) {
						
							try {
								
								//****ERROR HANDLING: DATA LOSS****
								sendReceiveSocket.setSoTimeout(RETRANSMIT_TIME);//set timeout time
								
								//block socket, wait for packet
								sendReceiveSocket.receive(receivePacket);
							} catch (SocketTimeoutException e){
								//if ack packet hasn't been received after .5s
								//resend
								if(i!=1){ 
									sendReceiveSocket.send(sendPacket);
									i++;
								}
								System.out.println("ACK not received, timing out." );
								System.exit(1);
							}
						}	
							//****ERROR HANDLING: DUPLICATE DATA****
							
							//if incoming block number != ack counter + 1, keep waiting, 
							//check for duplicate
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
						
					}
				} catch (IOException e) {
					System.out.println("No data received: Data lost.");
					System.out.println("Shutting down.");
					System.exit(1);
				}
				
				
				try {
					//block socket until ack received
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				//ack counter to account for
				if(blocknum2 == 255) {
					blocknum1++;
				}
				blocknum2++;
				
				//parse file into a byte array[len]
				len = in.read(data);
				System.out.println(len);
				
				//include block number with datagram
				msg[0] = 0;
				msg[1] = 3;
				msg[2] = blocknum1;
				msg[3] = blocknum2;

				//
				int i1 = 0; 
				for(;;) {
					System.arraycopy(data, i1, msg, i1+4, len-1);
					if(data[i1]==0) {
						break;
					} else {
						i1++;
					}
				}
				data[i1] = 0;
				i1++;
				
				sendPacket = new DatagramPacket(msg, i1+4, InetAddress.getLocalHost(), 23);
				
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				}
		
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

	public static void main(String args[])
	{
		TFTPClient c = new TFTPClient();
		for(;;) {
			c.run();
		}
	}
	
	//tester method for output
	private void test(String n) {
		System.out.println(n);
	}
	
}
