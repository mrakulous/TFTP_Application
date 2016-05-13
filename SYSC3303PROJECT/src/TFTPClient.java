
// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.net.*;
import java.util.*;


public class TFTPClient {

   private DatagramPacket sendPacket, receivePacket, ack;
   private DatagramSocket sendReceiveSocket;
   public static enum Request { READ, WRITE, ERROR};
   public static Request req;
   private static String filename;
   public static final byte[] readResp = {0, 3, 0, 1};
   public static final byte[] writeResp = {0, 4, 0, 0};
   
   // we can run in normal (send directly to server) or test
   // (send to simulator) mode
   public static enum Mode { NORMAL, TEST};

   public TFTPClient()
   {
      try {
         // Construct a datagram socket and bind it to any available
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets.
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {   // Can't create the socket.
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void sendAndReceive() throws FileNotFoundException, IOException
   {
	   int blockNum = 0;
		byte[] set = new byte[100];
		byte[] fn = new byte[filename.length()];
		
		if(req==Request.READ)

		{
			
			set[0] = 0;
	        set[1] = 1;
	        set[2] = 0;
	        set[3] = 0; //setting opcode
	        fn = filename.getBytes();
	        System.arraycopy(fn,0,set,4,fn.length);
	        int i = fn.length+4;
	        set[i] = 0;
	        String mode = "octet";
	        byte[] md = mode.getBytes();
	        // and copy into the msg
	        System.arraycopy(md,0,set,i+1,md.length);
	        set[i+md.length+1] = 0;
	        
	        for(int o = 0 ; o<i+md.length+2;o++){
	        	System.out.println(set[o]);
	        }
	        
	        try {
	            sendPacket = new DatagramPacket(set, i+md.length+2,
	                                InetAddress.getLocalHost(), 23);
	         } catch (UnknownHostException e) {
	            e.printStackTrace();
	            System.exit(1);
	         } //send opcode to server
	        
	        try {
		           // Block until a datagram is received via sendReceiveSocket.
		           sendReceiveSocket.send(sendPacket);
		        } catch(IOException e) {
		           e.printStackTrace();
		           System.exit(1);
		        } //get our data from the server
	        
			 	byte [] data = new byte[516];
		        receivePacket = new DatagramPacket(data, data.length);

		        System.out.println("Client: Waiting for packet.");
		        try {
		           // Block until a datagram is received via sendReceiveSocket.
		           sendReceiveSocket.receive(receivePacket);
		        } catch(IOException e) {
		           e.printStackTrace();
		           System.exit(1);
		        } //get our data from the server
		        
		        for(;;)
		        {
		            
		             // if the client reads, it will send an acknowledge
		            
		            // if the client writes, it will send data depends on ack
		            /* Read the file in 512 byte chunks. */

		            BufferedOutputStream out =
		                    new BufferedOutputStream(new FileOutputStream("TESTREADCLIENT.dat"));
		            
		          //  while ((n = in.read(data)) != -1) {
		                /* 
		                 * We just read "n" bytes into array data. 
		                 * Now write them to the output file. 
		                 */ //specify the name of file
		                   out.write(data, 4, data.length);
		        //}
		            
		            set[0] = 0;
	            	set[1] = 3; //get ack ready
	            	set[2] = data[2];
		            //ack sending
		            set[3]  = data[3];
		            
		            try {
		                sendPacket = new DatagramPacket(set, set.length,
		                                    InetAddress.getLocalHost(), 23);
		             } catch (UnknownHostException e) {
		                e.printStackTrace();
		                System.exit(1);
		             }
		            
		            if(data.length != 516)
		            {
		            	
		            	break;
		            }
		            
		            System.out.println("Client: Waiting for packet.");
			        try {
			           // Block until a datagram is received via sendReceiveSocket.
			           sendReceiveSocket.receive(receivePacket);
			        } catch(IOException e) {
			           e.printStackTrace();
			           System.exit(1);
			        }
		}
		
	}


		else if(req==Request.WRITE){
			set[0]=0;
			set[1]=2;
			set[2]=0;
			set[3]=0; // setting
																		// opcode
		fn=filename.getBytes();
		System.arraycopy(fn,0,set,4,fn.length);

		int i = fn.length + 4;
		set[i]=0;
		String mode = "octet";
		byte[] md = mode.getBytes();
		// and copy into the msg
		System.arraycopy(md,0,set,i+1,md.length);
		set[i+md.length+1]=0;

		try

		{
			sendPacket = new DatagramPacket(set, i+md.length+2, InetAddress.getLocalHost(), 23);
		} catch(UnknownHostException e)

		{
			e.printStackTrace();
			System.exit(1);
		} // send opcode to server

		try {
	           // Block until a datagram is received via sendReceiveSocket.
	           sendReceiveSocket.send(sendPacket);
	        } catch(IOException e) {
	           e.printStackTrace();
	           System.exit(1);
	        } //get our data from the server
     
		byte [] data = new byte[516];
        receivePacket = new DatagramPacket(data, data.length);

        System.out.println("Client: Waiting for packet.");
        try {
           // Block until a datagram is received via sendReceiveSocket.
           sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
           e.printStackTrace();
           System.exit(1);
        } //get our data from the server
		
		int j;
		int len = data.length;
		for(j=2;j<len;j++)

		{
			if (data[j] == 0)
				break;
		}
		// extract filename
		filename=new String(data,2,j-2);

		// prepare file for transfer to client.
		BufferedInputStream in;
		in=new BufferedInputStream(new FileInputStream("readTest.dat"));

		byte[] data1= new byte[516];
		int n;

		// construct datagram packet that is to
		// be sent to a specified port on a specified host.

		for(int k = 0;k<4;k++) // initiliaze opcode+block number
		data1[k]=readResp[k];

		while((n=in.read(data1))!=1)

		{// if data.length()<512, last packet

			// ack = new DatagramPacket(ackByte, ackByte.length);
			sendPacket = new DatagramPacket(data1, data1.length, receivePacket.getAddress(), receivePacket.getPort());

			sendReceiveSocket.send(sendPacket);
			sendReceiveSocket.receive(ack);

			// if data[3] is at max, reset to zero and increase data[2] by 1 (block
			// number counter)
			if ((data1[3]++) == 256) {
				data1[2]++;
				data1[3] = 0;
			}
		} 
		in.close();
		sendReceiveSocket.close();

	}
   }

   public static void main(String args[])
   {
      TFTPClient c = new TFTPClient();
      Scanner re = new Scanner(System.in);
      boolean error = true;
      int cmd = 0;
      
      for(;;){
    	  if (cmd != 5){
		      while(error){
			      try{
			    	  System.out.print("Please pick (1) read or (2) write or shutdown (5): ");
			    	  cmd = Integer.parseInt(re.nextLine());
			    	  if(cmd == 1){
			    		  req = Request.READ;
			    		  error = false;
			    	  } else if(cmd == 2){
			    		  req = Request.WRITE;
			    		  error = false;
			    	  } else if (cmd == 5){
			    		  error = false;
			    	  }
			      } catch(NumberFormatException e){
			    	  System.out.println("Error");
			      }
		      }
		      error = true;
		           
		      while(error){
			      try{
			    	  System.out.print("Please enter filename that you want to: ");
			    	  filename = re.nextLine();
			    	  File input = new File(filename);
			    	  Scanner read = new Scanner(input);
			    	  error = false;
			      } catch(FileNotFoundException e){
			    	  System.out.println("Error");
			      }
		      }
		      
		      try {
				c.sendAndReceive();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  } 
    	  break;
      }
   }
}