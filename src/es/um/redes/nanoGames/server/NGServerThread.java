package es.um.redes.nanoGames.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.message.NGAlertMessage;
import es.um.redes.nanoGames.message.NGGameMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGNickNameMessage;
import es.um.redes.nanoGames.message.NGRoomMessage;
import es.um.redes.nanoGames.message.NGRulesMessage;
import es.um.redes.nanoGames.message.NGTokenMessage;
import es.um.redes.nanoGames.server.roomManager.NGChallenge;
import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;
import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;

/**
 * A new thread runs for each connected client
 */
public class NGServerThread extends Thread {
	
	//Possible states of the connected client
	private static final byte PRE_TOKEN = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OFF_ROOM = 3;
	private static final byte IN_ROOM = 4;
	
	//Time difference between the token provided by the client and the one obtained from the broker directly
	private static final long TOKEN_THRESHOLD = 1500; //15 seconds
	//Socket to exchange messages with the client
	private Socket socket;
	//Global and shared manager between the threads
	private NGServerManager serverManager;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Utility class to communicate with the Broker
	private BrokerClient brokerClient;
	//Current player
	private NGPlayerInfo player;
	//Current RoomManager (it depends on the room the user enters)
	private NGRoomManager roomManager;
	//TODO Add additional fields
	private NGRoomStatus lastStatus;
	private NGChallenge lastChallenge;

	public NGServerThread(NGServerManager manager, Socket socket, String brokerHostname) throws IOException {
		//Initialization of the thread
		//TODO
		this.socket = socket;
		this.serverManager = manager;
		this.roomManager = null;
		this.brokerClient = new BrokerClient(brokerHostname);
		this.lastStatus = new NGRoomStatus();
		this.lastChallenge = null;
		do {
			this.player = new NGPlayerInfo();
		} while (!serverManager.addPlayer(player));
	}

	//Main loop
	public void run() {
		try {
			//We obtain the streams from the socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//The first step is to receive and to verify the token
			receiveAndVerifyToken();
			//While the connection is alive...
			while (!socket.isClosed()) {
				//TODO Rest of messages according to the automata
				NGMessage message = NGMessage.readMessageFromSocket(dis);
				if (message.getOpcode().equals(NGMessage.OP_CHANGENICKNAME)) {
					receiveAndVerifyNickname(message);
				}
				else if (message.getOpcode().equals(NGMessage.OP_GETROOMLIST)) {
					sendRoomList(serverManager.getRoomList());
				}
				else if (message.getOpcode().equals(NGMessage.OP_ENTERROOM)) {
					processReplyToEnterRoom(message);
					processRoomMessages();
				}
				//else if
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			System.out.println("A connection has been closed");
			e.printStackTrace();
		} finally {
			//TODO Close the socket
			this.closeSocket();
		}
		
	}

	//Receive and verify Token
	private void receiveAndVerifyToken() throws IOException {
		boolean tokenVerified = false;
		while (!tokenVerified) {
				//We extract the token from the message
				//now we obtain a new token from the broker
				//We check the token and send an answer to the client
			NGTokenMessage message = (NGTokenMessage)NGMessage.readMessageFromSocket(dis);
			NGMessage response;
			if ((brokerClient.getToken() - 1000) < message.getToken() &&
				message.getToken() < brokerClient.getToken()) {
				response = NGTokenMessage.validTokenMessage(player.getNick());
			} else {
				response = NGTokenMessage.invalidTokenMessage();
			}
			dos.writeUTF(new String(((NGTokenMessage)response).toByteArray()));
			tokenVerified = true;
		}
	}

	//We obtain the nick and we request the server manager to verify if it is duplicated
	private void receiveAndVerifyNickname(NGMessage message) throws IOException {
				//We obtain the nick from the message
				//we try to add the player in the server manager
				//if success we send to the client the NICK_OK message
				//otherwise we send DUPLICATED_NICK
			NGMessage response;
			if (!this.serverManager.existThisPlayer(((NGNickNameMessage)message).getNewNickname())) {
				NGPlayerInfo replace = new NGPlayerInfo(player);
				replace.setNick(((NGNickNameMessage)message).getNewNickname());
				this.serverManager.replacePlayer(this.player, replace);
				this.player = replace;
				response = NGNickNameMessage.newNicknameMessage(this.player.getNick());
			}
			else {
				response = NGNickNameMessage.newNicknameMessage(this.player.getNick());
			}
			dos.writeUTF(new String(((NGNickNameMessage)response).toByteArray()));
	}

	//We send to the client the room list
	private void sendRoomList(LinkedList<NGRoomDescription> rooms) throws IOException {
		//The room list is obtained from the server manager
		//Then we build all the required data to send the message to the client
		NGMessage response = NGRoomMessage.roomListMessage(rooms);
		dos.writeUTF(new String(((NGRoomMessage)response).toByteArray()));
	}

	private void processReplyToEnterRoom(NGMessage message) throws IOException {
		this.roomManager = this.serverManager.enterRoom(this.player, Integer.valueOf(((NGRoomMessage) message).getId()));
		if (this.roomManager != null) {
			NGMessage response = NGRoomMessage.validRoomMessage(((NGRoomMessage)message).getId());
			dos.writeUTF(new String(((NGRoomMessage)response).toByteArray()));
			NGMessage rulesResponse = NGRulesMessage.rulesMessage(this.roomManager.getRules());
			dos.writeUTF(new String(((NGRulesMessage)rulesResponse).toByteArray()));
		}
		else {
			NGMessage response = NGRoomMessage.invalidRoomMessage(((NGRoomMessage)message).getId());
			dos.writeUTF(new String(((NGRoomMessage)response).toByteArray()));
		}
	}
	
	private boolean processReplyToExitRoom(NGMessage message) throws IOException {
		if (this.roomManager.allowExit(this.player)) {
			NGMessage response = NGRoomMessage.roomExitOKMessage();
			dos.writeUTF(new String(((NGRoomMessage)response).toByteArray()));
			this.serverManager.leaveRoom(player, this.serverManager.getRoomID(this.roomManager));
			this.roomManager = null;
			return true;
		}
		else {
			NGMessage response = NGRoomMessage.roomExitNOMessage();
			dos.writeUTF(new String(((NGRoomMessage)response).toByteArray()));
			return false;
		}
	}
	
	private String processAnswerGameMessage(NGMessage message) throws IOException {
		return ((NGGameMessage) message).getAnswer();
	}
	
	private boolean closeSocket() {
		try {
			this.socket.close();
			this.serverManager.removePlayer(player);
			if (this.roomManager != null) {
				this.roomManager.removePlayer(player);
				this.roomManager = null;
			}
			return true;
		} catch (IOException e) {
			System.out.println("Error: " + e);
		}
		return false;
	}
	
	//Method to process messages received when the player is in the room
	//TODO
	private void processRoomMessages() throws IOException {
		//Now we check for incoming messages, status updates and new challenges
		boolean exit = false;
		while (!exit) {
			//TODO
			if (dis.available() > 0) {
				NGMessage message = NGMessage.readMessageFromSocket(dis);
				
				if (message.getOpcode().equals(NGMessage.OP_EXITROOM)) {
					if (processReplyToExitRoom(message)) {
						exit = true;
						this.lastStatus = new NGRoomStatus();
						this.lastChallenge = null;
					}
				}
			}
			
			if (!exit) {
				if (this.roomManager.isEnded()) {
					this.processNewStatus(this.lastStatus);
					processEndGame();
					this.lastStatus = new NGRoomStatus();
					this.lastChallenge = null;
					exit = true;
				}
				else {
					NGRoomStatus tempStatus = this.roomManager.checkStatus(player);
					if (this.lastStatus.getStatusNumber() < tempStatus.getStatusNumber()) {
						this.lastStatus = tempStatus;
						this.processNewStatus(this.lastStatus);
					}
					
					if (this.lastChallenge == null) {
						this.lastChallenge = this.roomManager.checkChallenge(player);
					}
					else {
						NGChallenge tempChallenge = this.roomManager.checkChallenge(player);
						if (tempChallenge != null) {
							if (this.lastChallenge.getChallengeNumber() < tempChallenge.getChallengeNumber()) {
								this.lastChallenge = tempChallenge;
								this.processNewChallenge(this.lastChallenge);
							}
						}
					}
				}
			}
			
		}
	}
	
	private void processNewChallenge(NGChallenge challenge) throws IOException {
		//We send the challenge to the client
		//TODO
		NGMessage response = NGGameMessage.questionMessage(challenge.getChallenge());
		dos.writeUTF(new String(((NGGameMessage)response).toByteArray()));
		//Now we set the timeout
		Timeout timeout = new Timeout();
		Timer timer = new Timer();
		timer.schedule(timeout, roomManager.getTimeout(), roomManager.getTimeout());
		boolean answerProvided = false;
		//Loop until an answer is provided or the timeout expires
		while (!timeout.getTimeout_triggered().get() && !answerProvided) {
			if (dis.available() > 0) {
				//The client sent a message
				//TODO Process the message
				NGMessage message = NGMessage.readMessageFromSocket(dis);
				//IF ANSWER Then call roomManager.answer() and proceed
				if (message.getOpcode().equals(NGMessage.OP_ANSWER)) {
					String answer = this.processAnswerGameMessage(message);
					this.roomManager.answer(player, answer);
					answerProvided = true;
				}
				else if (response.getOpcode().equals(NGMessage.OP_EXITROOM)) {
					processReplyToExitRoom(message);
					answerProvided = true;
				}
				else {
					NGMessage alert = NGAlertMessage.alertMessage("Error: A answer was expected.");
					dos.writeUTF(new String(((NGAlertMessage)alert).toByteArray()));
					answerProvided = true;
				}
			}
			else {
				try {
					//To avoid a CPU-consuming busy wait
					Thread.sleep(50);
				}
				catch (InterruptedException e) {
					//Ignore
				}
			}
		}
		if (!answerProvided) {
			//The timeout expired
			timer.cancel();
			//TODO call roomManager.noAnswer() and proceed
			this.roomManager.noAnswer(player);
		}
	}
	
	private void processNewStatus(NGRoomStatus status) throws IOException {
		NGMessage response = NGGameMessage.statusMessage(status.getStatus(), this.roomManager.getScores(), String.valueOf(this.roomManager.getTimeoutInSecons()));
		dos.writeUTF(new String(((NGGameMessage)response).toByteArray()));
		
	}
	
	private void processEndGame() throws IOException {
		NGMessage response = NGGameMessage.endgameMessage(this.roomManager.endGameReason());
		dos.writeUTF(new String(((NGGameMessage)response).toByteArray()));
		response = NGRoomMessage.roomExitOKMessage();
		dos.writeUTF(new String(((NGRoomMessage)response).toByteArray()));
		this.serverManager.leaveRoom(player, this.serverManager.getRoomID(this.roomManager));
		this.roomManager = null;
	}
	
}
