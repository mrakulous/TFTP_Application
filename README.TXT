Group 8
Iteration 1

README

Meeting #1: CB5109, 3:30pm-7:00pm, Sunday May 8, 2016 
Meeting #2: CB5109, 9:00am-11:00am, 2:00pm-9:00pm, Monday May 9, 2016
Meeting #3: CB5109, 4:00pm-12:00am, Tuesday May 10, 2016


The following list are the files required to run the program, with a short description of the files' purpose:

1) TFTPClient.java:
	* This is the client class
	* Here it will prompt the user to select a read or write request
	* Then it will ask the user for the particular file to either write to or to read from.
	* When the class is run (by the main method), the client will continously be run until prompted to shut down.
	* Make sure you run the Sim and the TFTPServer classes before the TFTPClient ckass is run

2) Sim.java:
	* This is the error simulator
	* At this moment in time, the simulator will directly send the packets that it recieves from the client and the server side to each respectiable reciever (packet from the server goes to the client, and the packet from the client goes to the server)
	* When this class is run (by the main method), the simulator will have it create and instance and invoke a method on the simulator to take in and send out each packet
	* Make sure you run the TFTPServer class before the simulator class.

3) TFTPServer.java:
	* This is the server class
	* Here it will either send data to the client (via the simulator) or receive data from the client (via the simulator). 
	* Once it recieved the first packet and for each packet, another thread will be invoked (TFTPServerThread class) to respectively deal with the request.
	* Please have this as the first class being run in the program.

4) TFTPServerThread.java:
	* This class is the server thread which handles the steady-state file transfer of files between the client and the server.  This class is instantiated by the �Server.java� class (as a thread) when a request is received from the �Sim.java� class.  This class then parses through the request to determine whether it is a �read� or a �write� request.  Based on the request, the corresponding action is done (�Read()� or �Write()�).
	* This class allows for the server to have multiple requests handled by having multiple threads dealing with each request.

Test files:
Client Read: "input.dat"
client Write: "input.dat"


Set up Instructions:

1) Please ensure the 4 java files are located together in the same folder along with any test files.
2) Open Eclipse, with all three files loaded into a project.
3) Open 3 conslole windows.
4) Run the TFTPServer class (main)
5) Pin one of the console windows
6) Run the Sim class (main)
7) Pin one of the remaining two console windows
8) Run the TFTPClient class (main)
9) Pin the last remaining console window
10) Choose a 1 for a read request, a 2 for a write request or a 5 to shutdown.
11) Allow for the terminal window to display each successful packet transfer to either read or write the file.


Responsibilities
Ricky:	TFTPServerThread.java
Abobakr: Client.java
Viraj: Client.java
Alagu: UI, shutdown functionality
Mark: TFTPServerThread.java
Everyone has contributed equally throughout the coding process, as well as the other files required for this iteration (i.e. UML class diagrams, UCMs)
