//This class represents the Error Simulator.
import java.io.*;
import java.net.*;
import java.util.*;

public class Sim {
   // UDP datagram packets and sockets used to send / receive
   private Scanner re;
   private DatagramSocket sendReceiveSocket, receiveSocket, sendSocket;
   private DatagramPacket sendPacket, temp, temp2, receivePacket;
   private int cmd;
   private int packetType;
   private byte blockNum;
   private int[] data;
   private byte[] dataSer;
   
   public Sim()
   {
      try {
    	  receiveSocket = new DatagramSocket(23);
    	  sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void receiveAndSendTFTP() throws Exception
   {
      byte[] data = new byte[516];
      cmd = 0;
      
      receivePacket = new DatagramPacket(data, data.length);
      System.out.println("Simulator: Waiting for packet.");
      try {
          receiveSocket.receive(receivePacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
      
      // read in packet
      re = new Scanner(System.in);
      
      byte blockNum = -1;
      
      //choose error
      while(true) {
    	  try {
    		  System.out.println("[0] Normal, [1] Duplicate [2] Delay [3] Lost: ");
    		  cmd = Integer.parseInt(re.nextLine());
    		  if (cmd >= 0 && cmd <= 3) break ;//If valid command, move on
    	  } catch (NumberFormatException e) {
    		  System.out.println("Please enter a valid option.");
    	  }
      }
      
      //parse error type
      packetType = parsePacketType();
      
      //if data or ack, enter which block number you'd like to interrupt
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
		   
	   }//end if 
      
      //action error to simulate
      parseError(cmd, packetType, blockNum);
      
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
         System.out.println("Contents(bytes): " + data);
         String contents = new String(data,0,len);
         System.out.println("Contents(string): " + contents + "\n");
         
         //if RRQ or WRQ, server Port = 69, else 0
         int servPort = 69;
         if(data[1] == 1|| data[1] == 2){
        	 servPort = 69;
         } else {
        	 servPort = serverPort;
         }
         
         //create packet to send to server port 69
         sendPacket = new DatagramPacket(data, len,
                                        receivePacket.getAddress(), servPort);
         temp = sendPacket; 
         
         System.out.println("Simulator: Sending packet to server.");
         System.out.println("To host: " + sendPacket.getAddress());
         System.out.println("Destination host port: " + sendPacket.getPort());
         len = sendPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Contents(bytes): " + data);
         contents = new String(data,0,len);
         System.out.println("Contents(string): " + contents + "\n");
         
         //send packet
         try {
            sendReceiveSocket.send(sendPacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         //receive server data
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
         System.out.println("Contents(bytes): " + dataSer);
         contents = new String(dataSer,0,len);
         System.out.println("Contents(string): " + contents + "\n");
         
         
         //prep received data from server to send to client
         sendPacket = new DatagramPacket(dataSer, receivePacket.getLength(),
                               receivePacket.getAddress(), clientPort);
         temp2 =   sendPacket;
          
         System.out.println( "Simulator: Sending packet to client.");
         System.out.println("To host: " + sendPacket.getAddress());
         System.out.println("Destination host port: " + sendPacket.getPort());
         len = sendPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Contents(bytes): " + dataSer);
         contents = new String(dataSer,0,len);
         System.out.println("Contents(string): " + contents + "\n");
         
         //prep socket to send packet to client
         try {
            sendSocket = new DatagramSocket();
         } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
         }

         //send packet to client
         try {
            sendSocket.send(sendPacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }
         sendSocket.close();
      }//end for loop
   }//END FUNCTION
  
   //Receive packet type from user
   private int parsePacketType() {
	   int pType = 0;
	   re = new Scanner(System.in);
	   
	   while (true) {
		   try {
			   System.out.println("Choose which packet you would like to cause an error: ");
			   System.out.println("[1] Data, [2] Ack, [3] RRQ [4] WRQ: ");
			   packetType = Integer.parseInt(re.nextLine());//parse user input
			   
			   //if not valid option, give error msg and give options again
			   if(pType > 0 && pType <= 4) 
				   break;
			   else
				   System.out.println("Please enter a valid option");
			   
		   } catch (NumberFormatException e) {
			   System.out.println("Please enter a valid option.");
		   }
	   }
	   
	   return pType;
   }
   
   private void parseError(int command, int packetType2, byte blockNum2) throws SocketException, UnknownHostException {
	   
	   int packetType = packetType2;
	   byte blockNum = blockNum2;
	   int cmd = command;
	   int time;
	   
	   //DUPLICATE ERROR
	   if(cmd == 1) {
	    	 while(true) {
					try {
						System.out.print("Enter the time in seconds between the first and second packets ");
						time = Integer.parseInt(re.nextLine());
						time = time *1000;
						//if (cmd >= 0 && cad <= length)
						break; 
						
						//if not valid number, throw exception
					} catch(NumberFormatException e) {
						System.out.println("Please enter a valid option");
					}
				}// end while 
			duplicate(packetType,blockNum,time); 
			
		 //DELAY ERROR	
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
			
		 //LOST PACKET   
		 } else if (cmd == 3) {
			 lost(packetType,blockNum);
		 }
   }
   
   private void lost(int packetType2, byte blockNum2) throws UnknownHostException {
	   
	   byte[] ipAddr = new byte[] { 127, 0, 0, 1 };
       InetAddress addr = InetAddress.getByAddress(ipAddr);
       DatagramPacket  losePacket;
       if (packetType ==1){
    	   if (data [1]== 3) { 
    		   	if (data [3]==  blockNum ){ 
    		   		// sendReceiveSocket.setSoTimeout();
    		   		losePacket =  new DatagramPacket (temp.getData(), data.length, addr, temp.getPort());
		    	    try {
		    	    	sendReceiveSocket.send(losePacket);
		    	    } catch (IOException e) {
		    	    	e.printStackTrace();
		    	    	System.exit(1);
		    	    }
		    	    	 
		    	    }
    		} else if (dataSer [1]== 3){
    			if (dataSer [3]==  blockNum ){
    			// sendSocket .setSoTimeout(time);
    			losePacket =  new DatagramPacket (temp2.getData(), dataSer.length, addr, temp2.getPort());
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
		    	  } else if (dataSer [1]== 4){
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

   private void delay(int packetType2, byte blockNum2, int time2) throws SocketException {
	   
	   int packetType = packetType2;
	   byte blockNum = blockNum2;
	   int time = time2;
	   if (packetType == 1) {
			  // delay data  
		   if (data [1]== 3) { // this a data packet
			   if (data [3] == blockNum ) {
		    	    	 
				   sendReceiveSocket.setSoTimeout(time);
		    	       
		    	   try {
		    		   sendReceiveSocket.send(temp);
		    	   } catch (IOException e) {
		    	       e.printStackTrace();
		    	       System.exit(1);
		    	   }//end try catch
		    	    	 
			   }//end if
		    		  
		   }else if (dataSer [1]== 3) {
			   
			   if (dataSer [3]==  blockNum ){
				   sendReceiveSocket.setSoTimeout(time);
			    	       
				   try {
					   sendSocket .send(temp2);
				   } catch (IOException e) {
			    	   e.printStackTrace();
			    	   System.exit(1);
				   }//end try-catch	    	 
			   }//end if
		   }//else if
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
		    	   }//end try-catch
		    	    	 
			   }//end if
		    		  
		   } else if (dataSer [1]== 4) {
			   if (dataSer [3]==  blockNum ){
				   sendSocket .setSoTimeout(time);
				   try {
					   sendSocket .send(temp2);
				   } catch (IOException e) {
			    	   e.printStackTrace();
			    	   System.exit(1);
				   }//end try-catch
			    	    	 
			   }//end if
		   } //end elseif
	   } else if (packetType == 3) {  
			  //rrq 
		   if (data [1]== 1) { 
			   sendReceiveSocket.setSoTimeout(time);
			   try {
				   sendReceiveSocket.send(temp);
			   } catch (IOException e) {
				   e.printStackTrace();
		    	   System.exit(1);
			   }//end try-catch
		    	    	 
		   	}//end if  
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

   public void duplicate (int packetType, byte blockNum, int time ) throws SocketException { 
	   if (packetType ==1) {
		  // duplicate data 
		   if (data [1]== 3) { // this a data packet
			   if (data [3]==  blockNum ) {
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
	    	    	 
			   }//end if  
		   } else if (dataSer [1]== 3) {
			   if (dataSer [3]==  blockNum ){
				   try {
					   sendSocket.send( temp2);
		    	   } catch (IOException e) {
		    	       e.printStackTrace();
		    	       System.exit(1);
		    	   }//end try-catch
	    			
				   sendSocket .setSoTimeout(time);
		    	       
		    	   try {
		    		   sendSocket .send(temp2);
		    	   } catch (IOException e) {
		    	       e.printStackTrace();
		    	       System.exit(1);
		    	   }//end try-catch
		    	    	 
			   }//end if
		   }//end else if
	     
	    	
	    	
       	} else if(packetType == 2) {
		   // duplicate ack 
       		if (data [1]== 4) { 
       			if (data [3] ==  blockNum ){
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
	    	    	 
	    	    }//end if
	    	} else if (dataSer [1]== 4){
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
		    	    	 
		    	}//end if
	    	}//end else if
        	
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
	    	}//end if  

       } else if (packetType == 4) {
   	     // WRQ 
    	   if (data [1]== 4) {
    		   
    		   try {
    			   sendReceiveSocket.send(temp);
  	           } catch (IOException e) {
  	        	   e.printStackTrace();
  	               System.exit(1);
  	           }//end try
  	           sendReceiveSocket.setSoTimeout(time);
  	       
  	           try {
  	        	   sendReceiveSocket.send(temp);
  	           } catch (IOException e) {
  	               e.printStackTrace();
  	               System.exit(1);
  	           }//end try
    	   }//end if
       }//end else if 
   } // end duplicate
   
   /*
   public static void testReceiveDatagramPacket() throws UnknownHostException {
	   byte[] testByte = new byte[4];
	   int testLen = testByte.length;
	   DatagramPacket receiveTester = new DatagramPacket(testByte, testLen, InetAddress.getLocalHost(), MAC_SOCKET);
   }
   */

   public static void main(String args[]) throws Exception {
	   Sim s = new Sim();
	   for(;;){
	 	  s.receiveAndSendTFTP();
	 	  //testReceiveDatagramPacket();
	   }//end for
	}
}