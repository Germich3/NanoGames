package es.um.redes.nanoGames.client.application;

import java.io.IOException;
import java.util.ArrayList;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.client.comm.NGGameClient;
import es.um.redes.nanoGames.client.shell.NGCommands;
import es.um.redes.nanoGames.client.shell.NGShell;
import es.um.redes.nanoGames.message.NGAlertMessage;
import es.um.redes.nanoGames.message.NGGameMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGRulesMessage;
import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;

public class NGController {
	//Number of attempts to get a token
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;
	//Different states of the client (according to the automata)
	private static final byte PRE_TOKEN = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OFF_ROOM = 3;
	private static final byte IN_ROOM = 4;
	//TODO Add additional states if necessary
	//The client for the broker
	private BrokerClient brokerClient;
	//The client for the game server
	private NGGameClient ngClient;
	//The shell for user commands from the standard input
	private NGShell shell;
	//Last command provided by the user
	private byte currentCommand;
	//Nickname of the user
	private String nickname;
	//Temporal Nickname of the user
	private String tempnick;
	//Current room of the user (if any)
	private String room;
	//if is allowed to leave the current room
	private boolean exitAllowed;
	//Current answer of the user (if any)
	private String answer;
	//if is allowed the answer
	private boolean answerAllowed;
	//Rules of the game
	private String rules;
	//Current status of the game
	private String gameStatus;
	//Token obtained from the broker
	private long token;
	//server
	private String serverHostname;

	public NGController(String brokerHostname, String serverHostname) throws IOException {
		this.brokerClient = new BrokerClient(brokerHostname);
		this.serverHostname = serverHostname;
		this.shell = new NGShell();
		this.ngClient = null;
		this.rules = null;
		this.token = 0;
		this.gameStatus = null;
		this.nickname = null;
		this.tempnick = null;
		this.currentCommand = 0;
		this.room = null;
		this.exitAllowed = false;
		this.answer = null;
		this.answerAllowed = false;
	}
	
	public byte getCurrentCommand() {
		return this.currentCommand;
	}

	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}
	
	public void setCurrentCommandArguments(String[] args) {
		//According to the command we register the related parameters
		//We also check if the command is valid in the current state
		switch (currentCommand) {
		case NGCommands.COM_CHANGENICK:
			tempnick = args[0];
			break;
		case NGCommands.COM_ENTER:
			room = args[0];
			break;
		case NGCommands.COM_ANSWER:
			answer = args[0];
			break;
		default:
		}
	}

	//Process commands provided by the users when they are not in a room 
	public void processCommand() throws IOException {
		switch (currentCommand) {
		case NGCommands.COM_TOKEN:
			getTokenAndDeliver();
			break;
		case NGCommands.COM_NICK:
			printNick();
			break;
		case NGCommands.COM_CHANGENICK:
			registerNickName();
			break;
		case NGCommands.COM_ROOMLIST:
			getAndShowRooms();
			break;
		case NGCommands.COM_ENTER:
			this.enterTheGame();
			break;
		case NGCommands.COM_QUIT:
			ngClient.disconnect();			
			brokerClient.close();
			break;
		default:
		}
	}
	
	private void getAndShowRooms() {
		//We obtain the rooms from the server and we display them
		try {
			ArrayList<NGRoomDescription> rooms = ngClient.getRoomsList();
			if (rooms.isEmpty()) {
				System.out.println("There is no game available");
			}
			else {
				System.out.println("----------------------------------------------------");
				System.out.println("Rooms List: Type \"enter <RoomID>\" to access a room.");
				for (NGRoomDescription room : rooms) {
					System.out.println("----------------------------------------------------");
					System.out.println("RoomID: " + room.getRoomID());
					System.out.println("Game: " + room.getRoomName());
					System.out.println("Available space: " + (room.getMaxPlayers() - room.getPlayers().size()));
					for (int i = 1; i <= room.getPlayers().size() ; i++) {
						System.out.println("Player " + i + ": " + room.getPlayers().get(i-1));
					}
				}
				System.out.println("----------------------------------------------------");
			}
		}
		catch (IOException e) {
			System.out.println("An unexpected error occurred while processing the request " + e);
		}
	}

	private void registerNickName() {
		//We try to register the nick in the server (it will check for duplicates)
		try {
			if (!tempnick.equals("null") &&
				tempnick.length() >= 4) {
				String responseNick = ngClient.registerNickname(this.tempnick);
				if (responseNick.equals(this.nickname)) {
					System.out.println("The nickname has been already taken.");
				}
				else {
					this.nickname = responseNick;
					this.tempnick = null;
				}
				System.out.println("The server has set " + this.nickname + " as your nick");
			}
			else {
				System.out.println("Error: it's not a valid nickname.");
			}
		} catch (IOException e) {
			System.out.println("An unexpected error occurred while processing the request " + e);
		}
	}

	private void enterTheGame() {
		//The users request to enter in the room
		//TODO
		try {
			if (ngClient.processRequestToEnterRoom(room)) {
				this.exitAllowed = false;
				//If success, we change the state in order to accept new commands
				do {
					//We will only accept commands related to a room
					readGameCommandFromShell();
					processGameCommand();
				} while (!this.exitAllowed);
			}
			else {
				System.out.println("The selected room does not exist or is currently full.");
			}
		} catch (IOException e) {
			System.out.println("Error: The specified field is not a valid room." + e);
		}
	}

	private void printNick() {
		System.out.println("Your nickname is: " + this.nickname);
	}
	
	private void processGameCommand() {
		switch (currentCommand) {
		case NGCommands.COM_RULES:
			printRules();
			break;
		case NGCommands.COM_STATUS:
			//TODO
			break;
		case NGCommands.COM_ANSWER:
			this.sendAnswer();
			break;
		case NGCommands.COM_SOCKET_IN:
			//In this case the user did not provide a command but an incoming message was received from the server
			processGameMessage();
			break;
		case NGCommands.COM_EXIT:
			this.exitTheGame();
			break;
		}		
	}

	private void exitTheGame() {
		//We notify the server that the user is leaving the room
		try {
			if (ngClient.processRequestToExitRoom()) {
				System.out.println("You has left the room.");
				this.exitAllowed = true;
				//Restarting attributes
				this.answerAllowed = false;
				this.answer = null;
				this.room = null;
				this.rules = null;
				this.gameStatus = null;
			}
			else {
				System.out.println("You are not allowed to leave the room now.");
				this.exitAllowed = false;
			}
		}
		catch (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	private void sendAnswer() {
		//In case we have to send an answer we will wait for the response to display it
		if (this.answerAllowed) {
			try {
				ngClient.sendAnswer(answer);
				this.answerAllowed = false;
			} catch (IOException e) {
				System.out.println("Unexpected error: " + e);
			}
		}
		else {
			System.out.println("You are not allowed to do an answer right now.");
		}
	}

	private void printRules() {
		if (this.rules != null) {
			System.out.println("Rules: " + this.rules);
		}
		else {
			System.out.println("There is no rules for this game.");
		}
	}
	
	private void processGameMessage() {		
		//This method processes the incoming message received when the shell was waiting for a user command
		try {
			NGMessage response  = ngClient.processResponseFromDataInput();
			if (response.getOpcode().equals(NGMessage.OP_RULES)) {
				this.rules = ((NGRulesMessage)response).getRules();
				this.printRules();
			}
			else if (response.getOpcode().equals(NGMessage.OP_QUESTION)) {
				if (((NGGameMessage)response).getGameRepresentation().getPlayer().equals(this.nickname)
				      || ((NGGameMessage)response).getGameRepresentation().getPlayer().equals("all")) {
					this.answerAllowed = true;
				}
				System.out.println(((NGGameMessage)response).getGameRepresentation().representInString());
			}
			else if (response.getOpcode().equals(NGMessage.OP_STATUS)) {
				System.out.println(((NGGameMessage)response).getGameRepresentation().representInString());
				System.out.println("Turn: " + ((NGGameMessage)response).getGameRepresentation().getPlayer());
				System.out.println("Time left: " + ((NGGameMessage)response).getTimeout());
				System.out.println("Scores:");
				for (String s : ((NGGameMessage)response).getScore().keySet()) {
					System.out.println(s + ": " + ((NGGameMessage)response).getScore().get(s));
				}
			}
			else if (response.getOpcode().equals(NGMessage.OP_ALERT)) {
				System.out.println(((NGAlertMessage)response).getAlert());
			}
			else if (response.getOpcode().equals(NGMessage.OP_ENDGAME)) {
				System.out.println("Game ended: " + ((NGGameMessage)response).getReason());
				System.out.println("You has left the room.");
				this.exitAllowed = true;
				//Restarting attributes
				this.answerAllowed = false;
				this.answer = null;
				this.room = null;
				this.rules = null;
				this.gameStatus = null;
			}
		} catch (IOException e) {
			System.out.println("Error: " + e);
		}		
	}

	//Method to obtain the token from the Broker
	private void getTokenAndDeliver() {
		//There will be a max number of attempts
		int attempts = MAX_NUMBER_OF_ATTEMPTS;
		//We try to obtain a token from the broker
		try {
			while (attempts > 0 && this.token == 0) {
				this.token = brokerClient.getToken();
				attempts--;
			}
		} catch (IOException e) {
			System.out.println("Error: no response has been received from the broker.");
		}
		//If we have a token then we will send it to the game server
		if (token != 0) {
			try {
				//We initialize the game client to be used to connect with the name server
				ngClient = new NGGameClient(serverHostname);
				this.nickname = ngClient.verifyToken(token);
				//We send the token in order to verify it
				if (this.nickname == null) {
					System.out.println("* The token is not valid.");	
					token = 0;
				}
			} catch (IOException e) {
				System.out.println("* Check your connection, the game server is not available.");
				//System.out.println(e);
				token = 0;
			}
		}
	}
	
	public void readGameCommandFromShell() {
		//We ask for a new game command to the Shell (and parameters if any)
		shell.readGameCommand(ngClient);
		setCurrentCommand(shell.getCommand());
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	public void readGeneralCommandFromShell() {
		//We ask for a general command to the Shell (and parameters if any)
		shell.readGeneralCommand();
		setCurrentCommand(shell.getCommand());
		setCurrentCommandArguments(shell.getCommandArguments());
	}
	
	public void forceThisCommand(byte command, String[] args) {
		setCurrentCommand(command);
		setCurrentCommandArguments(args);
		try {
			if (this.room != null) {
				processGameCommand();
			}
			else {
				processCommand();
			}
		} catch (IOException e) {
			System.out.println("Error: Invalid command.");
		}
	}
	
	public boolean sendToken() throws IOException {
		//We simulate that the Token is a command provided by the user in order to reuse the existing code
		System.out.println("* Obtaining the token...");
		setCurrentCommand(NGCommands.COM_TOKEN);
		processCommand();
		if (token != 0) {
			System.out.println("* Token is "+ this.token + " and it was validated by the server.");
			System.out.println("* Your nickname assigned by the server is \""+ this.nickname + "\", but you can change it in any time.");
		}
		return (token != 0);
	}

	public boolean shouldQuit() {
		return currentCommand == NGCommands.COM_QUIT;
	}

}
