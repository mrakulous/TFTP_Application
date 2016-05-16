import java.io.*;
import java.net.*;
import java.util.*;

//import TFTPServer.Request;

public class TFTPServerThread implements Runnable{
	public static enum Request { READ, WRITE, ERROR};
	public static final int DATA_SIZE = 512;
	public static final int TOTAL_SIZE = DATA_SIZE +4;  
	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket receiveSocket, sendSocket;
	private String filename;
	private String mode;
	private Request req;
	
	public TFTPServerThread(DatagramPacket received){
		this.receivedPacket = received;
		
		try {
			receiveSocket = new DatagramSocket();
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {   // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}
	   
	public void identifyReq(){
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
	 */
	 public void read() {
		 
		byte blocknum1=0;
   		byte blocknum2=1;
   		int len;
   		
   		try{
   			System.out.print(filename);
   			BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
   			do{
		                byte[] msg = new byte[TOTAL_SIZE];
		                byte[] data = new byte[DATA_SIZE];
		                len = in.read(data);
		                
		                msg[0] = 0;
		                msg[1] = 3;
		                msg[2] = blocknum1;
		                msg[3] = blocknum2;
		                
		                int i = 0; 
		                for(;;){
		                	System.arraycopy(data, i, msg, i+4, len);
		                	if(data[i]==0){
		                		break;
		                	} else {
		                		i++;
		                	}
		                }
		                data[i] = 0;
		                i++;
		                
		                sendPacket = new DatagramPacket(msg, i+4, receivedPacket.getAddress(), receivedPacket.getPort());
		                try{
		                	receiveSocket.send(sendPacket);
		                } catch (IOException e){
		                	e.printStackTrace();
		                    System.exit(1);
		                }
		                
		                if(blocknum2 == 255){
		                	blocknum1++;
		                }
		                blocknum2++;  
		                
			} while (len>=DATA_SIZE);
   			in.close();
   			
   		} catch (FileNotFoundException e){
   			e.printStackTrace();
   			System.exit(1);
   		} catch (IOException e){
   			e.printStackTrace();
   			System.exit(1);
   		}
	}

        /*
	* Write the received datagram packet to an output file.
	*/
	public void write() {
		
		byte blocknum1=0;
   		byte blocknum2=1;
   		
   		try{
   			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
   			System.out.print(filename);
   			//for(;;){
   				int len;
		                byte[] msg = new byte[TOTAL_SIZE];
		                byte[] data = new byte[DATA_SIZE];
		                byte[] ack = new byte[4];
		                
		                ack[0] = 0;
		                ack[1] = 4;
		                ack[2] = blocknum1;
		                ack[3] = blocknum2;
		                
		                sendPacket = new DatagramPacket(ack, ack.length,
		                		receivedPacket.getAddress(), receivedPacket.getPort());
		                try{
		                	sendSocket.send(sendPacket);
		                } catch (IOException e){
		                	e.printStackTrace();
		                    System.exit(1);
		                }
		                
		                if(blocknum2 == 255){
		                	blocknum1++;
		                }
		                blocknum2++;
		                
		                receivedPacket = new DatagramPacket(msg, msg.length);
		                try{
		                	receiveSocket.receive(receivedPacket);
		                } catch (IOException e){
		                	e.printStackTrace();
		                    System.exit(1);
		                }
		                
		                System.arraycopy(receivedPacket.getData(), 4, data, 0, receivedPacket.getLength()-4);
		                
		                for(len = 4; len < data.length; len++) {
		                    if (data[len] == 0) break;
		                }
		                
		                System.out.print("test1");
		                
		                out.write(data,0,len); 
		                System.out.print("test2");
		                
		                if(len+1<=TOTAL_SIZE) {
		                    out.close();
		                    //break;
		                }
			//}
   			
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
}
