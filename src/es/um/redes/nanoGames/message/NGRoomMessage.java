package es.um.redes.nanoGames.message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;

public class NGRoomMessage extends NGMessage {
	
	private String id;
	private ArrayList<NGRoomDescription> rooms;
	
	private NGRoomMessage(String opcode, String id, List<NGRoomDescription> rooms) {
		super(opcode);
		this.id = id;
		if (rooms != null) {
			this.rooms = new ArrayList<>(rooms);
		}
		else {
			this.rooms = new ArrayList<>();
		}
	}
	
	public static NGRoomMessage roomExitMessage() {
		return new NGRoomMessage(OP_EXITROOM, null, null);
	}
	public static NGRoomMessage roomExitOKMessage() {
		return new NGRoomMessage(OP_EXITOK, null, null);
	}
	public static NGRoomMessage roomExitNOMessage() {
		return new NGRoomMessage(OP_EXITNO, null, null);
	}
	public static NGRoomMessage enterRoomMessage(String id) {
		return new NGRoomMessage(OP_ENTERROOM, id, null);
	}
	public static NGRoomMessage invalidRoomMessage(String id) {
		return new NGRoomMessage(OP_INVALIDROOM, id, null);
		}
	public static NGRoomMessage validRoomMessage(String id) {
		return new NGRoomMessage(OP_VALIDROOM, id, null);
	}
	public static NGRoomMessage getRoomListMessage() {
		return new NGRoomMessage(OP_GETROOMLIST, null, null);
	}
	public static NGRoomMessage roomListMessage(List<NGRoomDescription> rooms) {
		return new NGRoomMessage(OP_ROOMLIST, null, rooms);
	}
	
	public String getId() {
		return id;
	}
	public ArrayList<NGRoomDescription> getRooms() {
		return new ArrayList<NGRoomDescription>(this.rooms);
	}
	
	@Override
	public byte[] toByteArray() {
		StringBuffer sb = new StringBuffer();
		sb.append("<message>");
		sb.append("<operation>");
		sb.append(this.getOpcode());
		sb.append("</operation>");
		if (this.getOpcode().equals(OP_ENTERROOM)) {
			sb.append("<room>");
			sb.append(this.id);
			sb.append("</room>");
		}
		else if (this.getOpcode().equals(OP_EXITROOM)) {
			//Does nothing
		}
		else if (this.getOpcode().equals(OP_EXITOK)) {
			//Does nothing
		}
		else if (this.getOpcode().equals(OP_EXITNO)) {
			//Does nothing
		}
		else if (this.getOpcode().equals(OP_INVALIDROOM)) {
			sb.append("<room>");
			sb.append(this.id);
			sb.append("</room>");
		}
		else if (this.getOpcode().equals(OP_VALIDROOM)) {
			sb.append("<room>");
			sb.append(this.id);
			sb.append("</room>");
		}
		else if (this.getOpcode().equals(OP_GETROOMLIST)) {
			//Does nothing
		}
		else if (this.getOpcode().equals(OP_ROOMLIST)) {
			sb.append("<rooms>");
			for (NGRoomDescription room : rooms) {
				sb.append("<room>");
				sb.append("<id>");
				sb.append(room.getRoomID());
				sb.append("</id>");
				sb.append("<name>");
				sb.append(room.getRoomName());
				sb.append("</name>");
				sb.append("<maxPlayers>");
				sb.append(room.getMaxPlayers());
				sb.append("</maxPlayers>");
				sb.append("<players>");
				for (String name: room.getPlayers()) {
					sb.append("<player>");
					sb.append(name);
					sb.append("</player>");
				}
				sb.append("</players>");
				sb.append("</room>");
			}
			sb.append("</rooms>");
		}
		sb.append("</message>");
		String message = sb.toString();
		return message.getBytes();
	}

	public static NGRoomMessage readFromIS(String operation, String message) {
		String roomID = null;
		if (operation.equals(OP_ENTERROOM) ||
			operation.equals(OP_INVALIDROOM) || 
			operation.equals(OP_VALIDROOM)) {
			String regexpr = "<room>(.*?)</room>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			roomID = (mat.find()) ? mat.group(1) : null;
		}
		else if (operation.equals(OP_ROOMLIST)) {
			String rxRooms = "<rooms>(.*?)</rooms>";
			Pattern patRooms = Pattern.compile(rxRooms);
			Matcher matRooms = patRooms.matcher(message);
			String rooms = (matRooms.find()) ? matRooms.group(1) : "null";
			ArrayList<NGRoomDescription> list = new ArrayList<>();
			
			String rxRoom = "<room>(.*?)</room>";
			Pattern patRoom = Pattern.compile(rxRoom);
			Matcher matRoom = patRoom.matcher(rooms);
			String room = "null";
			
			while (matRoom.find()) {
				room = matRoom.group(1);
				String rxID = "<id>(.*?)</id>";
				String rxName = "<name>(.*?)</name>";
				String rxMaxPlayers = "<maxPlayers>(.*?)</maxPlayers>";
				String rxPlayers = "<players>(.*?)</players>";
				Pattern patID = Pattern.compile(rxID);
				Pattern patName = Pattern.compile(rxName);
				Pattern patMaxPlayers = Pattern.compile(rxMaxPlayers);
				Pattern patPlayers = Pattern.compile(rxPlayers);
				Matcher matID = patID.matcher(room);
				Matcher matName = patName.matcher(room);
				Matcher matMaxPlayers = patMaxPlayers.matcher(room);
				Matcher matPlayers = patPlayers.matcher(room);
				String players = (matPlayers.find()) ? matPlayers.group(1) : "null";
				roomID = (matID.find()) ? matID.group(1) : null;
				String Name = (matName.find()) ? matName.group(1) : null;
				String MaxPlayers = (matMaxPlayers.find()) ? matMaxPlayers.group(1) : null;
				ArrayList<String> playersList = new ArrayList<>(Integer.valueOf(MaxPlayers));
				String rxPlayer = "<player>(.*?)</player>";
				Pattern patPlayer = Pattern.compile(rxPlayer);
				Matcher matPlayer = patPlayer.matcher(players);
				while (matPlayer.find()) {
					playersList.add(matPlayer.group(1));
				}
				list.add(new NGRoomDescription(Integer.valueOf(roomID), Name, Integer.valueOf(MaxPlayers).intValue(), playersList));
			}
			return roomListMessage(list);
		}
		else if (operation.equals(OP_GETROOMLIST)) {
			return getRoomListMessage();
		}
		else if (operation.equals(OP_EXITROOM)) {
			return roomExitMessage();
		}
		else if (operation.equals(OP_EXITOK)) {
			return roomExitOKMessage();
		}
		else if (operation.equals(OP_EXITNO)) {
			return roomExitNOMessage();
		}
		
		if (operation.equals(OP_ENTERROOM)) {
			return enterRoomMessage(roomID);
		}
		else if (operation.equals(OP_INVALIDROOM)) {
			return invalidRoomMessage(roomID);
		}
		else if (operation.equals(OP_VALIDROOM)) {
			return	validRoomMessage(roomID);
		}
		else {
			return null;
		}
	}
}
