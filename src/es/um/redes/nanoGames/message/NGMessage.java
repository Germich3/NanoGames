package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NGMessage {
	private String opcode;
	public static final String OP_CHECKTOKEN = "checkToken";
	public static final String OP_VALIDTOKEN = "validtoken";
	public static final String OP_INVALIDTOKEN = "invalidtoken";
	public static final String OP_NEWNICKNAME = "newnickname";
	public static final String OP_CHANGENICKNAME = "changenickname";
	public static final String OP_EXITROOM = "exitroom";
	public static final String OP_EXITOK = "exitok";
	public static final String OP_EXITNO = "exitno";
	public static final String OP_ENTERROOM = "enterroom";
	public static final String OP_INVALIDROOM = "invalidroom";
	public static final String OP_VALIDROOM = "validroom";
	public static final String OP_GETROOMLIST = "getroomlist";
	public static final String OP_ROOMLIST = "roomlist";
	public static final String OP_QUESTION = "question";
	public static final String OP_ANSWER = "answer";
	public static final String OP_STATUS = "status";
	public static final String OP_GETRULES = "getrules";
	public static final String OP_RULES = "rules";
	public static final String OP_ENDGAME = "endgame";
	public static final String OP_ALERT = "alert";
	//All codes for the rest of messages
	//TODO
	public NGMessage(String opcode) {
		this.opcode = opcode;
	}
	
	//Returns the opcode of the message
	public String getOpcode() {
		return opcode;

	}

	//Method to be implemented specifically by each subclass of NGMessage
	protected abstract byte[] toByteArray();
	
	//Method to be implemented specifically by each subclass of NGMessage
	protected static NGMessage readFromIS(String operation, String message) {
		return null;
	}

	//Reads the opcode of the incoming message and uses the subclass to parse the rest of the message
	public static NGMessage readMessageFromSocket(DataInputStream dis) throws IOException { 
		String message = dis.readUTF();
		String regexpr = "<operation>(.*?)</operation>";
		Pattern pat = Pattern.compile(regexpr);
		Matcher mat = pat.matcher(message);
		String operation = (mat.find()) ? mat.group(1) : null;
		//We use the operation to differentiate among all the subclasses
		//The following case is just an example
		if (operation.equals(OP_CHECKTOKEN) ||
			operation.equals(OP_VALIDTOKEN) ||
			operation.equals(OP_INVALIDTOKEN)) {
			return NGTokenMessage.readFromIS(operation, message);
		}
		else if (operation.equals(OP_NEWNICKNAME) ||
			operation.equals(OP_CHANGENICKNAME)) {	
			return NGNickNameMessage.readFromIS(operation, message);
		}
		else if (operation.equals(OP_ENTERROOM) ||
			operation.equals(OP_EXITROOM) ||
			operation.equals(OP_EXITOK) ||
			operation.equals(OP_EXITNO) ||
			operation.equals(OP_VALIDROOM) ||
			operation.equals(OP_INVALIDROOM) ||
			operation.equals(OP_GETROOMLIST) ||
			operation.equals(OP_ROOMLIST)) {	
			return NGRoomMessage.readFromIS(operation, message);
		}
		else if (operation.equals(OP_QUESTION) ||
			operation.equals(OP_ANSWER) ||
			operation.equals(OP_STATUS) ||
			operation.equals(OP_ENDGAME)) {	
			return NGGameMessage.readFromIS(operation, message);
		}
		else if (operation.equals(OP_GETRULES) ||
			operation.equals(OP_RULES)) {	
			return NGRulesMessage.readFromIS(operation, message);
		}
		else if (operation.equals(OP_ALERT)) {	
			return NGAlertMessage.readFromIS(operation, message);
		}
		else {
			System.err.println("Unknown message type received:" + operation);
		}
		return null;
	}
}
