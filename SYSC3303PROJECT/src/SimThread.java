//TFTPSim.java
//This class is the beginnings of an error simulator for a simple TFTP server 
//based on UDP/IP. The simulator receives a read or write packet from a client and
//passes it on to the server.  Upon receiving a response, it passes it on to the 
//client.
//One socket (23) is used to receive from the client, and another to send/receive
//from the server.  A new socket is used for each communication back to the client.   

import java.io.*;
import java.net.*;
//import java.util.*;
import java.util.Scanner;

public class SimThread implements Runnable{

// UDP datagram packets and sockets used to send / receive
	private DatagramPacket sendPacket,temp , temp2, receivePacket;
	   private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
	   private Scanner re  ; 
	   private byte[] data, dataSer, dataCli;
	   public SimThread(DatagramPacket received)
	   {
	      try {
	         receiveSocket = new DatagramSocket(23);
	         sendReceiveSocket = new DatagramSocket();
	      } catch (SocketException se) {
	         se.printStackTrace();
	         System.exit(1);
	      }
	   }

	   public void passOnTFTP()  throws Exception
	   
	   {  
		   re = new Scanner(System.in);
			int cmd = 0, packetType , time; 
			byte blockNum = -1;
	       // int length =  receivePacket.getLength();
	        
		   while(true) {
				try {
					System.out.print("[0] Normal , [1] duplicate  [2] delay  [3] lost: ");
					cmd = Integer.parseInt(re.nextLine());
					if (cmd >= 0 && cmd <=3 ) break ; 
					
				} catch(NumberFormatException e) {
					System.out.println("Please enter a valid option");
				}
			} 
		   
		   packetType = PickPacketType(); 
		   
		   if (packetType == 1 || packetType == 2){
			   while(true) {
					try {
						System.out.print("Enter the block number of the data packet ");
						int block = Integer.parseInt(re.nextLine());
						blockNum = (byte) block;
						//if (cmd >= 0 && cad <= length)
							break; 
						
					} catch(NumberFormatException e) {
						System.out.println("Please enter a valid option");
					}
				}// end while 
			   
		   } 
		     if(cmd == 1) {
		    	 while(true) {
						try {
							System.out.print("Enter the time in seconds between the first and second packets ");
							time = Integer.parseInt(re.nextLine());
							time = time *1000;
							//if (cmd >= 0 && cad <= length)
								break; 
							
						} catch(NumberFormatException e) {
							System.out.println("Please enter a valid option");
						}
					}// end while 
				duplicate(packetType,blockNum,time); 
				
			 } else if(cmd == 2) {
				 while(true) {
						try {
							System.out.print("Enter the time in seconds to delay the packet ");
							 time = Integer.parseInt(re.nextLine());
							time = time *1000;
							
								break; 
							
						} catch(NumberFormatException e) {
							System.out.println("Please enter a valid option");
						}
					}// end while 
			    delay(packetType,blockNum,time);
				
			 } else if (cmd == 3) {
				
			    lost (packetType,blockNum);
			 }
		    
		   
	  
		
		
	      
	      int clientPort, j=0, len;
	      int serverPort = 0;

	      for(;;) {
	         data = new byte[516];
	         receivePacket = new DatagramPacket(data, data.length);
	         
	         System.out.println("Simulator: Waiting for packet from client............" + "\n");
	         
	         try {
	            receiveSocket.receive(receivePacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         System.out.println("Simulator: Packet received from client.");
	         System.out.println("From host: " + receivePacket.getAddress());
	         clientPort = receivePacket.getPort();
	         System.out.println("Host port: " + clientPort);
	         len = receivePacket.getLength();
	         System.out.println("Length: " + len);
	         String contents = new String(data,0,len);
	         System.out.println("Contents: " + contents + "\n");
	         
	         int sport = 69;
	         if(data[1] == 1|| data[1] == 2){
	        	 sport = 69;
	         } else {
	        	 sport = serverPort;
	         }
	         
	        
	         sendPacket = new DatagramPacket(data, len,
	                                        receivePacket.getAddress(), sport);
	         temp = sendPacket ; 
	        
	         System.out.println("Simulator: Sending packet to server.");
	         System.out.println("To host: " + sendPacket.getAddress());
	         System.out.println("Destination host port: " + sendPacket.getPort());
	         len = sendPacket.getLength();
	         System.out.println("Length: " + len);
	         contents = new String(data,0,len);
	         System.out.println("Contents: " + contents + "\n");
	         
	         try {
	            sendReceiveSocket.send(sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         dataSer = new byte[516];
	         receivePacket = new DatagramPacket(dataSer, dataSer.length);
	        
	         System.out.println("Simulator: Waiting for packet from server............" + "\n");
	         try {
	            sendReceiveSocket.receive(receivePacket);
	         } catch(IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         System.out.println("Simulator: Packet received from server.");
	         System.out.println("From host: " + receivePacket.getAddress());
	         serverPort = receivePacket.getPort();
	         System.out.println("Host port: " + serverPort);
	         len = receivePacket.getLength();
	         System.out.println("Length: " + len);
	         contents = new String(dataSer,0,len);
	         System.out.println("Contents: " + contents + "\n");
	         
	         
	         sendPacket = new DatagramPacket(dataSer, receivePacket.getLength(),
	                               receivePacket.getAddress(), clientPort);
	         temp2 =   sendPacket;
	          
	         System.out.println( "Simulator: Sending packet to client.");
	         System.out.println("To host: " + sendPacket.getAddress());
	         System.out.println("Destination host port: " + sendPacket.getPort());
	         len = sendPacket.getLength();
	         System.out.println("Length: " + len);
	         contents = new String(dataSer,0,len);
	         System.out.println("Contents: " + contents + "\n");

	         try {
	            sendSocket = new DatagramSocket();
	         } catch (SocketException se) {
	            se.printStackTrace();
	            System.exit(1);
	         }

	         try {
	            sendSocket.send(sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }
	         sendSocket.close();
	      }
	   }
	   
	   public void duplicate (int packetType, byte blockNum, int time ) throws 
	   SocketException { 
		    
		    if (packetType ==1){
			  // duplicate data 
		    	  if (data [1]== 3) { // this a data packet
		    	     if (data [3]==  blockNum ){
		    	    	 try {
		    	             sendReceiveSocket.send(temp);
		    	          } catch (IOException e) {
		    	             e.printStackTrace();
		    	             System.exit(1);
		    	          }
		    	           sendReceiveSocket.setSoTimeout(time);
		    	       
		    	       try {
		    	             sendReceiveSocket.send(temp);
		    	          } catch (IOException e) {
		    	             e.printStackTrace();
		    	             System.exit(1);
		    	          }
		    	    	 
		    	     }
		    		  
		    	  }else if (dataSer [1]== 3){
		    		  if (dataSer [3]==  blockNum ){
		    			  try {
		    				  sendSocket.send( temp2);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
		    			  sendSocket .setSoTimeout(time);
			    	       
			    	       try {
			    	    	   sendSocket .send(temp2);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	     }
		    	  }
		     
		    	
		    	
	        } else if(packetType == 2) {
			   // duplicate ack 
	        	 if (data [1]== 4) { 
		    	     if (data [3]==  blockNum ){
		    	    	 try {
		    	             sendReceiveSocket.send(temp);
		    	          } catch (IOException e) {
		    	             e.printStackTrace();
		    	             System.exit(1);
		    	          }
		    	           sendReceiveSocket.setSoTimeout(time);
		    	       
		    	       try {
		    	             sendReceiveSocket.send(temp);
		    	          } catch (IOException e) {
		    	             e.printStackTrace();
		    	             System.exit(1);
		    	          }
		    	    	 
		    	     }
		    		  
		    	  }else if (dataSer [1]== 4){
		    		  if (dataSer [3]==  blockNum ){
		    			  try {
		    				  sendSocket.send( temp2);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
		    			  sendSocket .setSoTimeout(time);
			    	       
			    	       try {
			    	    	   sendSocket .send(temp2);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	     }
		    	  }
		    	     
	        	 
			
		    } else if (packetType == 3) {  
			  //rrq 
		    	if (data [1]== 3) { 
		    	   
		    	    	 try {
		    	             sendReceiveSocket.send(temp);
		    	          } catch (IOException e) {
		    	             e.printStackTrace();
		    	             System.exit(1);
		    	          }
		    	           sendReceiveSocket.setSoTimeout(time);
		    	       
		    	       try {
		    	             sendReceiveSocket.send(temp);
		    	          } catch (IOException e) {
		    	             e.printStackTrace();
		    	             System.exit(1);
		    	          }
		    	    	 
		    	}  
		    	
		   
		
	       } else if (packetType == 4) {
	   	     // wrq 
	    	   if (data [1]== 4) { 
		    	   
	  	    	 try {
	  	             sendReceiveSocket.send(temp);
	  	          } catch (IOException e) {
	  	             e.printStackTrace();
	  	             System.exit(1);
	  	          }
	  	           sendReceiveSocket.setSoTimeout(time);
	  	       
	  	       try {
	  	             sendReceiveSocket.send(temp);
	  	          } catch (IOException e) {
	  	             e.printStackTrace();
	  	             System.exit(1);
	  	          }
	    	   }
	    	 }//end if 
		   
		   
	   } // end duplicate 
	   
	   public void delay( int packetType, byte blockNum , int time ) throws 
	   SocketException {
		   
		   if (packetType ==1){
				  // delay data  
			    	  if (data [1]== 3) { // this a data packet
			    	     if (data [3]==  blockNum ){
			    	    	 
			    	           sendReceiveSocket.setSoTimeout(time);
			    	       
			    	       try {
			    	             sendReceiveSocket.send(temp);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	     }
			    		  
			    	  }else if (dataSer [1]== 3){
			    		  if (dataSer [3]==  blockNum ){
			    			  
			    			  sendSocket .setSoTimeout(time);
				    	       
				    	       try {
				    	    	   sendSocket .send(temp2);
				    	          } catch (IOException e) {
				    	             e.printStackTrace();
				    	             System.exit(1);
				    	          }
				    	    	 
				    	     }
			    	  }
			     
			    	
			    	
		        } else if(packetType == 2) {
				   // duplicate ack 
		        	 if (data [1]== 4) { 
			    	     if (data [3]==  blockNum ){
			    	    	 
			    	           sendReceiveSocket.setSoTimeout(time);
			    	       
			    	       try {
			    	             sendReceiveSocket.send(temp);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	     }
			    		  
			    	  }else if (dataSer [1]== 4){
			    		  if (dataSer [3]==  blockNum ){
			    			  
			    			  sendSocket .setSoTimeout(time);
				    	       
				    	       try {
				    	    	   sendSocket .send(temp2);
				    	          } catch (IOException e) {
				    	             e.printStackTrace();
				    	             System.exit(1);
				    	          }
				    	    	 
				    	     }
			    	  }
			    	     
		        	 
				
			    } else if (packetType == 3) {  
				  //rrq 
			    	if (data [1]== 1) { 
			    	   
			    	           sendReceiveSocket.setSoTimeout(time);
			    	       
			    	       try {
			    	             sendReceiveSocket.send(temp);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	}  
			    	
			   
			
		       } else if (packetType == 4) {
		   	     // wrq 
		    	   if (data [1]== 2) { 
			    	   
		  	           sendReceiveSocket.setSoTimeout(time);
		  	       
		  	       try {
		  	             sendReceiveSocket.send(temp);
		  	          } catch (IOException e) {
		  	             e.printStackTrace();
		  	             System.exit(1);
		  	          }
		    	   }
		    	 }//end if 
			   
		   
		   
	    }// end delay 
	  
	    public void lost(int packetType, byte blockNum ) throws Exception{ 
		  
	    	byte[] ipAddr = new byte[] { 127, 0, 0, 1 };
	        InetAddress addr = InetAddress.getByAddress(ipAddr);
	        DatagramPacket  losePacket;
	        if (packetType ==1){
				  
			    	  if (data [1]== 3) { 
			    	     if (data [3]==  blockNum ){
			    	    	 
			    	          // sendReceiveSocket.setSoTimeout();
			    	   losePacket =  new DatagramPacket (temp.getData(),
			   	    			  data.length, addr, temp.getPort());
			    	  
			    	       try {
			    	             sendReceiveSocket.send(losePacket);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	     }
			    		  
			    	  }else if (dataSer [1]== 3){
			    		  if (dataSer [3]==  blockNum ){
			    			  
			    			 // sendSocket .setSoTimeout(time);
			    		 losePacket =  new DatagramPacket (temp2.getData(),
				   	    			  dataSer.length, addr, temp2.getPort());
				    	       
				    	       try {
				    	    	   sendSocket .send( losePacket);
				    	          } catch (IOException e) {
				    	             e.printStackTrace();
				    	             System.exit(1);
				    	          }
				    	    	 
				    	     }
			    	  }
			     
			    	
			    	
		        } else if(packetType == 2) {
				  
		        	 if (data [1]== 4) { 
			    	     if (data [3]==  blockNum ){
			    	    	 
			    	          // sendReceiveSocket.setSoTimeout(time);
			    	    	losePacket =  new DatagramPacket (temp.getData(),
				   	    			  data.length, addr, temp.getPort());
			    	       try {
			    	             sendReceiveSocket.send( losePacket);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	     }
			    		  
			    	  }else if (dataSer [1]== 4){
			    		  if (dataSer [3]==  blockNum ){
			    			  losePacket =  new DatagramPacket (temp2.getData(),
				   	    			  dataSer.length, addr, temp2.getPort()); 
			    			 // sendSocket .setSoTimeout(time);
				    	       
				    	       try {
				    	    	   sendSocket .send( losePacket);
				    	          } catch (IOException e) {
				    	             e.printStackTrace();
				    	             System.exit(1);
				    	          }
				    	    	 
				    	     }
			    	  }
			    	     
		        	 
				
			    } else if (packetType == 3) {  
				  //rrq 
			    	if (data [1]== 1) { 
			    	   
			    	          // sendReceiveSocket.setSoTimeout(time);
			    		 losePacket =  new DatagramPacket (temp.getData(),
			   	    			  dataSer.length, addr, temp.getPort()); 
			    	       
			    	       try {
			    	             sendReceiveSocket.send( losePacket);
			    	          } catch (IOException e) {
			    	             e.printStackTrace();
			    	             System.exit(1);
			    	          }
			    	    	 
			    	}  
			    	
			   
			
		       } else if (packetType == 4) {
		   	     // wrq 
		    	   if (data [1]== 2) { 
			    	   
		  	           //sendReceiveSocket.setSoTimeout(time);
		    		losePacket =  new DatagramPacket (temp.getData(),
			   	    			  dataSer.length, addr, temp.getPort()); 
		  	       try {
		  	             sendReceiveSocket.send(losePacket);
		  	          } catch (IOException e) {
		  	             e.printStackTrace();
		  	             System.exit(1);
		  	          }
		    	   }
		    	 }//end if 
			   
		   
	   }// end lost
	  public int PickPacketType(){
		  int packetType = 0;
		  re = new Scanner(System.in);
		  while(true) {
				try {
					System.out.print("Choose the type of packet ");
					System.out.println(" [1] Data , [2] Ack , [3] RRQ  or [4] WRQ))");
					
					packetType = Integer.parseInt(re.nextLine());
					
					if(packetType == 1) {
						return 1; 
						
					} else if(packetType == 2) {
						return 2; 
						
					} else if (packetType == 3) {
						return 3; 
					   
					
				    } else if (packetType == 4) {
				    	return 4; 
				    
					}
				    
		            
				} catch(NumberFormatException e) {
					System.out.println("Please enter a valid option");
				}
			}
		  
	  }// end packetType
	  
	     public void duplicateHelper( byte blockNum, int time)throws 
	     SocketException{ 
		  
		  
		  
	    }
	     public void run (){
	    	 passOnTFTP();
	     }
}