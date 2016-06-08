import java.io.*;
import java.net.*;
import java.util.*;

public class Sim {
	private DatagramPacket sendPacket, receivePacket, losePacket;
	private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
	private static int cmd;
	private static int cmd2;
	private static int packetType;
	private static Byte blockNum;
	private byte[] data;
	private static Scanner re;
	private static int time;
	private static Byte leftByte, rightByte;

	private byte[] dataSer;
	private static final int MAC_SOCKET = 2300;
	private static final int WIN_SOCKET = 23;
	private int clientPort, serverPort;

	private static final int DATA_SIZE = 512;
	private static final int TOTAL_SIZE = DATA_SIZE+4;
	private static final int WAITING_TIME = 10000;
	private boolean firstTime = true;
	private static boolean toPrint;
	private boolean firstReq = true;
	private static boolean read;
	
	private int ACKNumber = -1;
	private boolean backToBeginning = false;
	private static boolean firstWait = true;
	private static boolean restartSim = true;

	public Sim() {
		try {
			receiveSocket = new DatagramSocket(WIN_SOCKET);
			sendReceiveSocket = new DatagramSocket();
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void receiveAndSendTFTP(){

		int j=0, len;
		String contents;
		

		
		
		for(;;) {
			data = new byte[TOTAL_SIZE];
			receivePacket = new DatagramPacket(data, data.length);
			
			if (toPrint == true) {
				System.out.println("Simulator: Waiting for packet from client............" + "\n");
			}
			try {
				receiveSocket.setSoTimeout(WAITING_TIME);
				receiveSocket.receive(receivePacket);
				setClientPort(receivePacket.getPort());
			} catch (SocketTimeoutException e) {
				//if REQ is lost, restart SIM
				if(firstWait==true) {
					testString("Duplicate packet sent to Client - Returning to Server");
					firstWait=false;
					break;
				} else {
					testString("Time Out - Restarting Sim");
					restartSim = true;
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			if(firstReq){
				if(data[1] == 1){//1 for read
					read = true;
				} else {
					read = false;
				}
				firstReq = false;
			}
			
			leftByte = new Byte(receivePacket.getData()[2]);
			rightByte = new Byte(receivePacket.getData()[3]);
			
			
			
			String blockNumberString = leftByte.intValue() + rightByte.intValue() + "";
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX " + blockNumberString);
			blockNumberString = "";
			len = receivePacket.getLength();
			
			//RECEIVE PACKET FROM CLIENT
			/*************************************************/
			printReceivePacket(toPrint, firstTime, receivePacket, 1);
			/*************************************************/
			//CHECK IF REQUEST
			int Serport = 69;
			if(data[1] == 1|| data[1] == 2){
				Serport = 69;
			} else {
				Serport = serverPort;
			}
	
			//CREATE SENDPACKET TO SERVER
			sendPacket = new DatagramPacket(data, len, receivePacket.getAddress(), Serport);
	
			//IF NOT NORMAL
			
			if(cmd!=0) {
				byte[] currentBlock = new byte[2];
				System.arraycopy(data, 2, currentBlock, 0, 2);
				Byte firstBlock = new Byte (currentBlock[0]);
				Byte secondBlock = new Byte (currentBlock[1]);
				Byte correct = (byte) (firstBlock.intValue()*10 + secondBlock.intValue());
				if(packetType == 1 || packetType == 2)
				{
					blockNum = correct;
				}
				
				if(blockNum == correct && data[1] == (byte)packetType){
					if(cmd==1){
						try {
							if (toPrint == true) {
								System.out.println("############### DUPLICATE_1 ###############");
							}
							duplicate();
							break;
						} catch (SocketException e) {
							e.printStackTrace();
						}
					}
					if(cmd==2){
						try {
							if (toPrint == true) {
								System.out.println("############### DELAY!!!!!!!!!!! ########");
							}
							delay();
							break;
						} catch (SocketException e) {
							e.printStackTrace();
						}
					} 
					if(cmd==3){
						try {
							lost(sendPacket);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
					}
				}//END IF
				break;
	        } else {
	        	testString("WE'RE BREAKING OUT");
	        	break;
	        }//END ELSE
			
		}//END FOR LOOP FOR CLIENT RECEIVE
		
		/***********************************************/
		printSendPacket(toPrint, firstTime, sendPacket, 1);
		/***********************************************/
		
		firstTime = false;
		try {//SEND PACKET TO SERVER
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//System.out.println("4"+ Serport);
		receivePacket = new DatagramPacket(data, data.length);

		for(;;) {
			try {
				sendReceiveSocket.setSoTimeout(WAITING_TIME);
				sendReceiveSocket.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				if(firstWait==true) {
					testString("Duplicate packet sent to Server - Returning to Client");
					firstWait = false;
					break;
				} else {
					testString("Time Out - Restarting Sim");
					restartSim = true;
					return;
				}
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
	
			leftByte = new Byte(receivePacket.getData()[2]);
			rightByte = new Byte(receivePacket.getData()[3]);
	        
			/***********************************************/
			printReceivePacket(firstTime, toPrint, receivePacket, 2);
			/***********************************************/
			
			if (toPrint == true) {
				System.out.println("Simulator: Packet received from server.");
				System.out.println("From host: " + receivePacket.getAddress());
			}
			
			setServerPort(receivePacket.getPort());
			
			if (toPrint == true) {
				System.out.println("Host port: " + getServerPort());
			}
			
			len = receivePacket.getLength();
			
			if (toPrint == true) {
				System.out.println("Length: " + len);
				System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
		        System.out.println("Contents(bytes): " + data);
			}
	        if(len>4 && receivePacket.getLength()==516) {
	        	// It is not an ACK packet
	        	contents = new String(data, 4, receivePacket.getLength()-4);
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + contents + "\n");
	        	}
	        }
	        else if(len>4 && receivePacket.getLength()!=516) {
	        	// It is not an ACK packet
	        	contents = new String(data, 4, receivePacket.getLength());
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + contents + "\n");
	        	}
	        }
	        else {
	        	// It is an ACK packet
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
	        	}
	        }
	
			sendPacket = new DatagramPacket(data, receivePacket.getLength(), receivePacket.getAddress(), getClientPort());
	
			if (toPrint == true) {
				System.out.println("Simulator: Sending packet to client.");
				System.out.println("To host: " + sendPacket.getAddress());
				System.out.println("Destination host port: " + sendPacket.getPort());
			}
			
			len = sendPacket.getLength();
			
			if (toPrint == true) {
				System.out.println("Length: " + len);
				System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
		        System.out.println("Contents(bytes): " + data);
			}
			if(len>4 && receivePacket.getLength()==516) {
	        	// It is not an ACK packet
	        	contents = new String(data, 4, receivePacket.getLength()-4);
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + contents + "\n");
	        	}
	        }
	        else if(len>4 && receivePacket.getLength()!=516) {
	        	// It is not an ACK packet
	        	contents = new String(data, 4, receivePacket.getLength());
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + contents + "\n");
	        	}
	        }
	        else {
	        	if(receivePacket.getData()[1] == 4){
		        	// It is an ACK packet
		        	if (toPrint == true) {
		        		System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
		        	}
	        	} else {
	        		System.out.println("Contents(string): \n");
	        	}
	        }
			
			if(cmd!=0) {
				byte[] currentBlock = new byte[2];
				System.arraycopy(data, 2, currentBlock, 0, 2);
				Byte firstBlock = new Byte (currentBlock[0]);
				Byte secondBlock = new Byte (currentBlock[1]);
				Byte correct = (byte) (firstBlock.intValue()*10 + secondBlock.intValue());
				if(packetType == 1 || packetType ==2)
				{
					blockNum = correct;
				}
				
				if(blockNum == correct && data[1] == (byte)packetType){
					if(cmd == 1){
						try {
							if (toPrint == true) {
								System.out.println("############### DUPLICATE_2 ###############");
							}
							duplicate();
						} catch (SocketException e) {
							e.printStackTrace();
						}
					}
					if(cmd ==2){
						try {
							delay();
						} catch (SocketException e) {
							e.printStackTrace();
						}
					} 
					if(cmd==3){
						try {
	    				    lost(sendPacket);//lost packet 
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
					}
				}
	        }//end if
			
			if(!(cmd==3)){
				
				try {
					sendSocket.send(sendPacket);
					break;
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}//END FOR LOOP
	}

   public void duplicate() throws SocketException {
	   try {
		   	if (toPrint == true) {
			   System.out.println("Simulator: Sending packet to client.");
			   System.out.println("To host: " + sendPacket.getAddress());
			   System.out.println("Destination host port: " + sendPacket.getPort());
		   	}
		   	
			int len = sendPacket.getLength();
			
			if (toPrint == true) {
				System.out.println("Length: " + len);
				System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
				System.out.println("Contents(bytes): " + data);
			}
	        if(len > 4) {
	        	// It is not an ACK packet
	        	String contents = new String(data, 4, receivePacket.getLength()-4);
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + contents + "\n");
	        	}
	        }
	        else {
	        	// It is an ACK packet
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
	        	}
	        }
	        try {
	        	if (toPrint == true) {
	        		System.out.println("Waiting " + (time/1000) + " seconds between first,second packets");
	        	}
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        if((read && sendPacket.getData()[1] == 4) || (!read && sendPacket.getData()[1] == 3)){
	        	sendReceiveSocket.send(sendPacket);
	        } else {
	        	sendSocket.send(sendPacket);
	        }
	        
	        if (toPrint == true) {
	        	System.out.println("~~~~~~~~~~~~~~~~~~~~~~ DUPLICATE SENT ~~~~~~~~~~~~~~~~~~~~~~\n");
	        }
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//sendReceiveSocket.setSoTimeout(time);
   } // end duplicate

   private void delay() throws SocketException {
	   try {
		   if (toPrint == true) {
			   System.out.println("@@@@@@@@@@ WAITING " + time);
		   }	
       	Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	   	if (toPrint == true) {
		   System.out.println("@@@@@@@@@@ BAKRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
	   	}
	}

   	@SuppressWarnings("resource")
	private void lost(DatagramPacket sendPacket) throws UnknownHostException {
	   //byte[] ipAddr = new byte[] { 127, 0, 0, 1 };
       //InetAddress addr = InetAddress.getByAddress(ipAddr);

       try {
    	   System.out.println("\n PACKET WILL BE LOST \n");
    	   DatagramSocket lostSocket = new DatagramSocket();
    	   sendPacket.setPort(100);
    	   lostSocket.send(sendPacket);
       } catch (IOException e) {
		// TODO Auto-generated catch block
    	   e.printStackTrace();
       }
       
  	}// end lost

   	public static void function() {
   		cmd = 0;
   		cmd2 = 0;
		re = new Scanner(System.in);
		blockNum = -1;

		//choose error
		while(true) {
			try {
				System.out.print("***NEW SIM*** \n");
				System.out.print("[0]Normal  [1]Duplicate  [2]Delay  [3]Lost: ");
				cmd = Integer.parseInt(re.nextLine());
				if (cmd >= 0 && cmd <= 3) break ;//If valid command, move on
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid option.");
			}
		}

		if (cmd!=0){
			//parse error type
			packetType = parsePacketType();
			
			//if data or ack or lost, enter which block number you'd like to interrupt
			while(true) {
				try {
					if(packetType == 1 || packetType == 2){
						break;
					}
					System.out.print("Block number of the data packet: ");
					int block = Integer.parseInt(re.nextLine());
					blockNum = (byte) block;
					break;
				} catch(NumberFormatException e) {
					System.out.println("Please enter a valid option");
				}
			}// end while

		   while(true) {
				try {
					if(cmd==3){
						// User wants lost
						break;
					}
					if(cmd == 1){
						// User wants duplicate
						System.out.print("Delay between first,second packets (seconds): ");
					} else {
						// User wants delay
						System.out.print("Delay for the packet (seconds): ");
					}
					time = Integer.parseInt(re.nextLine());
					time = time *1000;
					break;
				} catch(NumberFormatException e) {
					System.out.println("Please enter a valid option");
				}
			}// end while
			
      	}
   		// Choose to have quiet or verbose
 		while (true) {
 			try {
 				System.out.print("[1]Quiet  [2]Verbose : ");
 				cmd2 = Integer.parseInt(re.nextLine());
 				if (cmd2 == 1) {
 					toPrint = false;
 					break;
 				} else {
 					toPrint = true;
 					break;
 				}
 			} catch (NumberFormatException e) {
 				System.out.println("Please enter a valid option");
 			}
 		}
   	}
   	
   	private static int parsePacketType() {
		re = new Scanner(System.in);

		while (true) {
			try {
				System.out.println("Pick a packet for the error.");
				System.out.print("[1]RRQ  [2]WRQ  [3]DATA  [4]ACK: ");
				packetType = Integer.parseInt(re.nextLine());//parse user input

				//if not valid option, give error msg and give options again
				if(packetType > 0 && packetType <= 4) {
					break;
				} else {
					System.out.println("Please enter a valid option");
				}
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid option.");
			}
		}
		return packetType;
   }
   	
   	private void printReceivePacket(boolean toPrint, boolean firstTime, DatagramPacket receivePacket, int n) {
   		String contents = "";
   		int len  = receivePacket.getLength();
   		
   		if (toPrint == true) {
   			if(n == 1) 
   				System.out.println("Simulator: Packet received from client.");
   			else
   				System.out.println("Simulator: Packet received from Server.");	
   			
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			System.out.println("Length: " + len);
		}

		if (firstTime) {
			// Do nothing
		} else {
			if (toPrint == true) {
				System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
			}
		}
		if (toPrint == true) {
			System.out.println("Contents(bytes): " + data);
		}
		if (firstTime) {
			// filename and mode
			contents = new String(data, 2, receivePacket.getLength() - 2);
			if (toPrint == true) {
				System.out.println("Contents(string): \n" + contents + "\n");
			}
		} else {
			if (len > 4) {
				// It is not an ACK packet
				contents = new String(data, 4, receivePacket.getLength() - 4);
				if (toPrint == true) {
					System.out.println("Contents(string): \n" + contents + "\n");
				}
			} else {
				// It is an ACK packet
				if (toPrint == true) {
					System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
				}
			}
		}
   	}
   	
   	private void printSendPacket (boolean toPrint, boolean firstTime, DatagramPacket sendPacket, int n) {
   		int len = sendPacket.getLength();
		String contents = "";
		
		if((cmd==3)){
			if(n==1)
				System.out.println("Simulator: Sending packet to Server.");
			else
				System.out.println("Simulator: Sending packet to Client.");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			System.out.println("Length: " + len);
			if(firstTime) {
				// Do nothing
			}
			else {
				if (toPrint == true) {
					System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
				}
			}
		} 
		if(toPrint == true) {
			//if sending packet to server, n==1
			if(n==1) {
				System.out.println("Simulator: Sending packet to Server.");
				contents = new String(data, 2, receivePacket.getLength()-2);
			} else {
				System.out.println("Simulator: Sending packet to Client.");
				contents = new String(data, 4, receivePacket.getLength()-4);
			}
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			System.out.println("Length: " + len);
			System.out.println("Contents(bytes): " + data);	        
		}
		if(firstTime) {
        	// filename and mode
        	contents = new String(data, 2, receivePacket.getLength());
        	if (toPrint == true) {
        		System.out.println("Contents(string): \n" + contents + "\n");
        	}
        }
        else {
	        if(len > 4) {
	        	// It is not an ACK packet
	        	contents = new String(data, 4, receivePacket.getLength()-4);
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + contents + "\n");
	        	}
	        }
	        else {
	        	// It is an ACK packet
	        	if (toPrint == true) {
	        		System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
	        	}
	        }
        }
		
		if (toPrint == true) {
			System.out.println("Simulator: Waiting for packet from server............" + "\n");
		}
   	}
   	public static void main( String args[] )
   {
      Sim s = new Sim();
      for(;;){
    	  if(restartSim == true) {
    		  function();
    		  firstWait = true;
    		  restartSim = false;
    	  }
    	  s.receiveAndSendTFTP();
      }
   }
   	
   	private void testString(String n) {
   		System.out.println(n);
   	}
   	
	private int getServerPort() {
		return serverPort;
	}

	private void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	private int getClientPort() {
		return clientPort;
	}

	private void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

   	
}
