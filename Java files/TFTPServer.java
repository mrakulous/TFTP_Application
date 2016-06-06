import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPServer {

   // UDP datagram packets and sockets used to send / receive
   private DatagramPacket receivePacket;
   private static DatagramSocket receiveSocket;
   private static Thread se1;
   public static boolean toPrint;
   private static Scanner re;
   
   public static final int DATA_SIZE = 512;
   public static final int TOTAL_SIZE = DATA_SIZE+4;
   private static final int PORT = 69;
   private boolean firstTime = true;
   private String contents;
   private Byte leftByte;
   private Byte rightByte;
   
   public TFTPServer()
   {
	   re = new Scanner(System.in);
	   int cmd = 0;
	   while (true) {
		   try {
				System.out.print("[1]Quiet  [2]Verbose : ");
				cmd = Integer.parseInt(re.nextLine());
				if (cmd == 1) {
					toPrint = false;
					break;
				} else {
					toPrint = true;
					break;
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid input.");
			}
		}
	   if (toPrint == true) {
		   System.out.println("Server: Waiting for packet from simulator............" + "\n");
	   }
	   try {
		   // Construct a datagram socket and bind it to port 69
		   // on the local host machine. This socket will be used to
		   // receive UDP Datagram packets.
		   receiveSocket = new DatagramSocket(PORT);
	   } catch (SocketException se) {
		   se.printStackTrace();
		   System.exit(1);
	   }
   }
   public void receiveAndSendTFTP() throws Exception
   {
	   	 byte[] data = new byte[TOTAL_SIZE];
      
	   	 receivePacket = new DatagramPacket(data, data.length);
         // Block until a datagram packet is received from receiveSocket.
         try {
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }
         
         leftByte = new Byte(receivePacket.getData()[2]);
         rightByte = new Byte(receivePacket.getData()[3]);
 		
         if (toPrint == true) {
	         System.out.println("Server: Packet received from simulator.");
		     System.out.println("From host: " + receivePacket.getAddress());
		     System.out.println("Host port: " + receivePacket.getPort());
         }
         
	     int len = receivePacket.getLength();
	     
	     if (toPrint == true) {
	    	 System.out.println("Length: " + len);
         }
	     if(firstTime) {
	    	 // Do nothing
		 }
	     else {
	    	 if (toPrint == true) {
	    		 System.out.println("Block Number: " + leftByte.toString() + rightByte.toString());
	    	 }
	     }
	     if (toPrint == true) {
	    	 System.out.println("Contents(bytes): " + data);
	     }
	     if(firstTime) {
	    	 // filename and mode
	    	 contents = new String(data, 2, 17);
	    	 if (toPrint == true) {
	    		 System.out.println("Contents(string): \n" + contents + "\n");
	    	 }
	    	 firstTime = false;
	     }
	     else {
	    	 if(len > 4) {
	    		 // It is not an ACK packet
	    		 contents = new String(data, 4, DATA_SIZE);
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
	     
	     try {
	    	 Thread.sleep(500);
         } catch (InterruptedException e) {
        	 e.printStackTrace();
         }
	     
         TFTPServerThread st = new TFTPServerThread(receivePacket, TFTPServer.toPrint);
         se1 = new Thread(st);
         se1.start();
   }

   public static void main( String args[] ) throws Exception
   {
      TFTPServer c = new TFTPServer();
      for(;;){
    	  c.receiveAndSendTFTP();
      }
   }
}
