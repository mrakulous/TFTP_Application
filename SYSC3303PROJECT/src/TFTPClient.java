// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPClient {
	public static final int DATA_SIZE = 512;
	public static final int TOTAL_SIZE = DATA_SIZE +4;
	public static enum Request { READ, WRITE, ERROR};
	public static enum Mode { NORMAL, TEST};
	public Request req;
	private String filename;
	private String mode;
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;

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

   	public void run(){
		Scanner re = new Scanner(System.in);
		int cmd = 0;
      
		for(;;){
			while(true){
				try{
					System.out.print("Please pick (1) read or (2) write or (5) shutdown: ");
					cmd = Integer.parseInt(re.nextLine());
					if(cmd == 1) {
						req = Request.READ;
						break;
					} else if(cmd == 2) {
						req = Request.WRITE;
						break;
					} else if (cmd == 5){
						System.exit(1);
					}
				} catch(NumberFormatException e) {
					System.out.println("Please enter a valid option");
				}
			}
			
			while(true){
				try{
					System.out.print("Please enter filename that you want to: ");
					filename = re.nextLine();
					File input = new File(filename);
					Scanner read = new Scanner(input);
					break;
				} catch(FileNotFoundException e) {
					System.out.println("Please enter a valid filename");
				}
			}
			/*/while(true){
				try{
					System.out.print("Please pick (1) netascii or (2) octet: ");
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
			}*/
			mode = "octet";
			byte[] msg = new byte[TOTAL_SIZE];
			
			msg[0] = 0;
			if(req==Request.READ) {
				msg[1] = 1;
				
			} else if(req==Request.WRITE) {
				msg[1] = 2;
			}
			int index = 2;
			
			byte[] fn = filename.getBytes();
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
				sendPacket = new DatagramPacket(msg, index,
								InetAddress.getLocalHost(), 23);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			try{
            	sendReceiveSocket.send(sendPacket);
            } catch (IOException e){
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
   	public void read() {
   		byte blocknum1=0;
   		byte blocknum2=1;
   		try{
   			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("read.txt"));
   			//for(;;){
                		int len;
                		byte[] msg = new byte[TOTAL_SIZE];
                		byte[] data = new byte[DATA_SIZE];
                
		                receivePacket = new DatagramPacket(msg, msg.length);
		                try{
		                	sendReceiveSocket.receive(receivePacket);
		                } catch (IOException e){
		                	e.printStackTrace();
		                    System.exit(1);
                		}
                
		                System.arraycopy(receivePacket.getData(), 4, data, 0, receivePacket.getLength()-4);
                System.out.print("test");
		                for(len = 4; len < data.length; len++) {
		                    if (data[len] == 0) break;
		                }
		                out.write(data,0,len);
		                System.out.println("Sending ack");
                
		                byte[] ack = new byte[4];
		                ack[0] = 0;
		                ack[1] = 4;
		                ack[2] = blocknum1;
		                ack[3] = blocknum2;
		                
		                sendPacket = new DatagramPacket(ack, ack.length,
								InetAddress.getLocalHost(), 23);
		                try{
		                	sendReceiveSocket.send(sendPacket);
		                } catch (IOException e){
		                	e.printStackTrace();
		                    System.exit(1);
		                }
		                 
		                if(len+1<=TOTAL_SIZE) {
		                    out.close();
		                    //break;
		                }
		                 
		                if(blocknum2 == 255){
		                	blocknum1++;
		                }
		                blocknum2++;
			//}
   		} catch (IOException e){
   			e.printStackTrace();
            System.exit(1);
   		}
	}
   	
   	public void write() {
   		byte blocknum1=0;
   		byte blocknum2=1;
   		int len;
   		try{
   			BufferedInputStream in = new BufferedInputStream(new FileInputStream("file.txt"));
   			do{
		                byte[] msg = new byte[TOTAL_SIZE];
		                byte[] data = new byte[DATA_SIZE];
		                
		                receivePacket = new DatagramPacket(msg, msg.length);
		                try{
		                	sendReceiveSocket.receive(receivePacket);
		                } catch (IOException e){
		                	e.printStackTrace();
		                    System.exit(1);
		                }
		                
		                if(blocknum2 == 255){
		                	blocknum1++;
		                }
		                blocknum2++;
		                
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
		                
		                sendPacket = new DatagramPacket(msg, i+4, InetAddress.getLocalHost(), 23);
		                try{
		                	sendReceiveSocket.send(sendPacket);
		                } catch (IOException e){
		                	e.printStackTrace();
		                    System.exit(1);
		                }
		              
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
	
	public static void main(String args[])
	{
		TFTPClient c = new TFTPClient();
		for(;;){
			c.run();
		}
	}
}
