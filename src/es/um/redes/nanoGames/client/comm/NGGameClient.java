package es.um.redes.nanoGames.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.message.NGGameMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGNickNameMessage;
import es.um.redes.nanoGames.message.NGRoomMessage;
import es.um.redes.nanoGames.message.NGTokenMessage;
import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;

//This class provides the functionality required to exchange messages between the client and the game server 
public class NGGameClient {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	
	private static final int SERVER_PORT = 6969;

	public NGGameClient(String serverName) throws IOException {
		//Creation of the socket and streams
		socket = new Socket(serverName, SERVER_PORT);
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.dis = new DataInputStream(socket.getInputStream());
		//socket.bind(new InetSocketAddress(2018));
	}

	public String verifyToken(long token) throws IOException {
		//SND(token) and RCV(TOKEN_VALID) or RCV(TOKEN_INVALID)
		NGMessage message = NGTokenMessage.checkTokenMessage(token);
		dos.writeUTF(new String(((NGTokenMessage)message).toByteArray()));
		//Receive response (NGMessage.readMessageFromSocket)
		NGTokenMessage response = (NGTokenMessage)NGMessage.readMessageFromSocket(dis);
		if  (response.getOpcode().equals(NGMessage.OP_VALIDTOKEN)) {
			return response.getNickname();
		} else {
			return null;
		}
	}
	
	public String registerNickname(String newnick) throws IOException {
		//SND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		NGMessage message = NGNickNameMessage.changeNicknameMessage(newnick);
		dos.writeUTF(new String(((NGNickNameMessage)message).toByteArray()));
		//Receive response (NGMessage.readMessageFromSocket)
		NGNickNameMessage response = (NGNickNameMessage)NGMessage.readMessageFromSocket(dis);
		return response.getNewNickname();
	}

	//TODO
	//add additional methods for all the messages to be exchanged between client and game server
	public ArrayList<NGRoomDescription> getRoomsList() throws IOException {
		NGMessage message = NGRoomMessage.getRoomListMessage();
		dos.writeUTF(new String(((NGRoomMessage)message).toByteArray()));
		//Receive response (NGMessage.readMessageFromSocket)
		NGRoomMessage response = (NGRoomMessage)NGMessage.readMessageFromSocket(dis);
		return response.getRooms();
	}
	
	//Enter room request
	public boolean processRequestToEnterRoom(String roomID) throws IOException {
		NGMessage message = NGRoomMessage.enterRoomMessage(roomID);
		dos.writeUTF(new String(((NGRoomMessage)message).toByteArray()));
		//Receive response (NGMessage.readMessageFromSocket)
		NGRoomMessage response = (NGRoomMessage)NGMessage.readMessageFromSocket(dis);
		if (response.getOpcode().equals(NGMessage.OP_VALIDROOM)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//Process incoming reply
		public NGMessage processResponseFromDataInput() throws IOException {
			return NGMessage.readMessageFromSocket(dis);
		}
		
	//Exit the game
		public boolean processRequestToExitRoom() throws IOException {
			NGMessage message = NGRoomMessage.roomExitMessage();
			dos.writeUTF(new String(((NGRoomMessage)message).toByteArray()));
			//Receive response (NGMessage.readMessageFromSocket)
			NGMessage response = NGMessage.readMessageFromSocket(dis);
			if (response instanceof NGRoomMessage) {
				if (response.getOpcode().equals(NGMessage.OP_EXITOK)) {
					return true;
				}
			}
			return false;
		}
		
	//Send answer
	public void sendAnswer(String answer) throws IOException {
		NGMessage message = NGGameMessage.answerMessage(answer);
		dos.writeUTF(new String(((NGGameMessage)message).toByteArray()));
	}
	
	//Used by the shell in order to check if there is data available to read 
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	

	//To close the communication with the server
	public void disconnect() throws IOException {
		//TODO
		socket.close();
	}
}
