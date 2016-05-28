import java.io.*;
import java.net.*;
import java.util.*;

public class Sim {
   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
   private int cmd;
   private int packetType;
   private byte blockNum;
   private byte[] data;
   private Scanner re;
   private int time;
   
   private byte[] dataSer;
   public static final int MAC_SOCKET = 2300;
   public static final int WIN_SOCKET = 23;
   
   public Sim()
   {	      
      try {
         receiveSocket = new DatagramSocket(WIN_SOCKET);
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void receiveAndSendTFTP(){
	   cmd = 0;
	   
	   re = new Scanner(System.in);
      
	   blockNum = -1;
      
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
      
      if (cmd!=0){
	      //parse error type
	      packetType = parsePacketType();
	      
	      //if data or ack or lost, enter which block number you'd like to interrupt
		   while(true) {
				try {
					System.out.print("Enter the block number of the data packet ");
					int block = Integer.parseInt(re.nextLine());
					blockNum = (byte) block;
					break; 
				} catch(NumberFormatException e) {
					System.out.println("Please enter a valid option");
				}
			}// end while 
		   
			if(cmd!=3){
			   while(true) {
					try {
						if(cmd == 1){
							System.out.print("Enter the time in seconds between the first and second packets ");
						} else {
							System.out.print("Enter the time in seconds to delay the packet ");
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
	  
      int clientPort, j=0, len;
      int serverPort = 0;
      
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
       
       try {
           Thread.sleep(500);
       } catch (InterruptedException e) {
      	 e.printStackTrace();
       }
       
       int sport = 69;
       if(data[1] == 1|| data[1] == 2){
      	 sport = 69;
       } else {
      	 sport = serverPort;
       }
       
       if(cmd==0){
	       sendPacket = new DatagramPacket(data, len,
	               receivePacket.getAddress(), sport);
       } else {
    	   byte[] currentBlock = null;
    	   System.arraycopy(data, 2, currentBlock, 0, 2);
    	   if(blockNum == java.nio.ByteBuffer.wrap(currentBlock).getInt()){
    		   if(cmd == 1){
    			   try {
					duplicate(packetType, blockNum, time);
					} catch (SocketException e) {
						e.printStackTrace();
					}
    		   } else if(cmd ==2){
    			   try {
					delay(packetType, blockNum, time);
					} catch (SocketException e) {
						e.printStackTrace();
					}
    		   } else {
    			   try {
					lost(packetType, blockNum);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
    		   }
    	   }
    	   sendPacket = new DatagramPacket(data, len,
	               receivePacket.getAddress(), sport);
       }
       
		System.out.println("Simulator: Sending packet to server.");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Contents(bytes): " + data);
		contents = new String(data,0,len);
		System.out.println("Contents(string): " + contents + "\n");
		
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
		
		System.out.println("Simulator: Packet received from server.");
		System.out.println("From host: " + receivePacket.getAddress());
		serverPort = receivePacket.getPort();
		System.out.println("Host port: " + serverPort);
		len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Contents(bytes): " + data);
		contents = new String(data,0,len);
		System.out.println("Contents(string): \n" + contents + "\n");
		
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
		System.out.println("Contents(bytes): " + data);
		contents = new String(data,0,len);
		System.out.println("Contents(string): \n" + contents + "\n");
		
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
		
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		sendSocket.close();
       
   }
   
   private int parsePacketType() {
	   int pType = 0;
	   re = new Scanner(System.in);
	   
	   while (true) {
		   try {
			   System.out.println("Choose which packet you would like to cause an error: ");
			   System.out.println("[1] Data, [2] Ack, [3] RRQ [4] WRQ: ");
			   packetType = Integer.parseInt(re.nextLine());//parse user input
			   
			   //if not valid option, give error msg and give options again
			   if(pType > 0 && pType <= 4) {
				   break;
			   } else {
				   System.out.println("Please enter a valid option");
			   }
		   } catch (NumberFormatException e) {
			   System.out.println("Please enter a valid option.");
		   }
	   }
	   
	   return pType;
   }
   
   private void lost(int packetType2, byte blockNum2) throws UnknownHostException {
	   
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
   
   public static void main( String args[] )
   {
      Sim s = new Sim();
      for(;;){
    	  s.receiveAndSendTFTP();
      }
   }
}