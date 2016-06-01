package project;

import java.io.*;
import java.net.*;
import java.util.*;

public class Sim {
	private DatagramPacket sendPacket, receivePacket, losePacket;
	private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
	private static int cmd;
	private static int packetType;
	private static Byte blockNum;
	private byte[] data;
	private static Scanner re;
	private static int time;
	private static Byte leftByte, rightByte;

	private byte[] dataSer;
	public static final int MAC_SOCKET = 2300;
	public static final int WIN_SOCKET = 23;
	private int serverPort = 0;

	public static final int DATA_SIZE = 512;
	public static final int TOTAL_SIZE = DATA_SIZE+4;
	public boolean firstTime = true;

	public Sim() {
		try {
			receiveSocket = new DatagramSocket(WIN_SOCKET);
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
   }

	public void receiveAndSendTFTP(){

		int clientPort, j=0, len;
		String contents;
		

		data = new byte[TOTAL_SIZE];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Simulator: Waiting for packet from client............" + "\n");
		try {
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		leftByte = new Byte(receivePacket.getData()[2]);
		rightByte = new Byte(receivePacket.getData()[3]);

		System.out.println("Simulator: Packet received from client.");
		System.out.println("From host: " + receivePacket.getAddress());
		clientPort = receivePacket.getPort();
		System.out.println("Host port: " + clientPort);
		len = receivePacket.getLength();
		System.out.println("Length: " + len);
		if(firstTime) {
			// Do nothing
		}
		else {
			System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
		}
        System.out.println("Contents(bytes): " + data);
        if(firstTime) {
        	// filename and mode
        	contents = new String(data, 2, DATA_SIZE);
        	System.out.println("Contents(string): \n" + contents + "\n");
        }
        else {
	        if(len > 4) {
	        	// It is not an ACK packet
	        	contents = new String(data, 4, DATA_SIZE);
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

		int Serport = 69;
		if(data[1] == 1|| data[1] == 2){
			Serport = 69;
		} else {
			Serport = serverPort;
		}

		sendPacket = new DatagramPacket(data, len,
				receivePacket.getAddress(), Serport);
		
		System.out.println("############### " + cmd);
		
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
				if(cmd==1){
					try {
						System.out.println("############### DUPLICATE_1 ###############");
						duplicate();
					} catch (SocketException e) {
						e.printStackTrace();
					}
				}
				if(cmd==2){
					try {
						System.out.println("############### DELAY!!!!!!!!!!! ########");
						delay();
					} catch (SocketException e) {
						e.printStackTrace();
					}
				} 
				if(cmd==3){
					try {
    				   lost();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
        }

		System.out.println("Simulator: Sending packet to server.");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		len = sendPacket.getLength();
		System.out.println("Length: " + len);
		if(firstTime) {
			// Do nothing
		}
		else {
			System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
		}
        System.out.println("Contents(bytes): " + data);
        if(firstTime) {
        	// filename and mode
        	contents = new String(data, 2, DATA_SIZE);
        	System.out.println("Contents(string): \n" + contents + "\n");
        	firstTime = false;
        }
        else {
	        if(len > 4) {
	        	// It is not an ACK packet
	        	contents = new String(data, 4, DATA_SIZE);
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

		System.out.println("Simulator: Waiting for packet from server............" + "\n");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		receivePacket = new DatagramPacket(data, data.length);

		try {
			sendReceiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		leftByte = new Byte(receivePacket.getData()[2]);
		rightByte = new Byte(receivePacket.getData()[3]);

		System.out.println("Simulator: Packet received from server.");
		System.out.println("From host: " + receivePacket.getAddress());
		serverPort = receivePacket.getPort();
		System.out.println("Host port: " + serverPort);
		len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
        System.out.println("Contents(bytes): " + data);
        if(len > 4) {
        	// It is not an ACK packet
        	contents = new String(data, 4, DATA_SIZE);
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

		sendPacket = new DatagramPacket(data, receivePacket.getLength(),
		      receivePacket.getAddress(), clientPort);

		System.out.println("Simulator: Sending packet to client.");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
        System.out.println("Contents(bytes): " + data);
        if(len > 4) {
        	// It is not an ACK packet
        	contents = new String(data, 4, DATA_SIZE);
        	System.out.println("Contents(string): \n" + contents + "\n");
        }
        else {
        	// It is an ACK packet
        	System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
        }

		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/*
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}*/
		
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
						System.out.println("############### DUPLICATE_2 ###############");
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
    				   lost();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
        }//end if
		
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		sendSocket.close();
	}

   public void duplicate() throws SocketException {
	   try {
		   	System.out.println("Simulator: Sending packet to client.");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			int len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
	        System.out.println("Contents(bytes): " + data);
	        if(len > 4) {
	        	// It is not an ACK packet
	        	String contents = new String(data, 4, DATA_SIZE);
	        	System.out.println("Contents(string): \n" + contents + "\n");
	        }
	        else {
	        	// It is an ACK packet
	        	System.out.println("Contents(string): \n" + "########## ACKPacket ##########\n");
	        }
	        try {
	        	System.out.println("Waiting " + (time/1000) + " seconds between first,second packets");
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        sendReceiveSocket.send(sendPacket);
	        System.out.println("~~~~~~~~~~~~~~~~~~~~~~ DUPLICATE SENT ~~~~~~~~~~~~~~~~~~~~~~\n");

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		sendReceiveSocket.setSoTimeout(time);

   } // end duplicate

   private void delay() throws SocketException {
	   try {
       	System.out.println("@@@@@@@@@@ WAITING " + time);
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	   System.out.println("@@@@@@@@@@ BAKRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
	}

   	private void lost() throws UnknownHostException {
	   byte[] ipAddr = new byte[] { 127, 0, 0, 1 };
       InetAddress addr = InetAddress.getByAddress(ipAddr);

       losePacket =  new DatagramPacket (sendPacket.getData(),
    		   				data.length, addr, sendPacket.getPort());
  	}// end lost

   	public static void function() {
   		cmd = 0;
		re = new Scanner(System.in);
		blockNum = -1;

		//choose error
		while(true) {
			try {
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
   	
   public static void main( String args[] )
   {
	  boolean doneTransfer = true;
	  int count=0;

      Sim s = new Sim();
      function();
      for(;;){
    	 s.receiveAndSendTFTP();
      }
   }
}
