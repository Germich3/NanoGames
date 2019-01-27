package es.um.redes.nanoGames.server.roomManager;

import java.util.LinkedList;
import java.util.List;

public class NGRoomDescription {
	
	private int roomID;
	private String gameName;
	private int maxPlayers;
	private LinkedList<String> players;
	
	public NGRoomDescription(int roomID, String gameName, int maxPlayers, List<String> players) {
		this.roomID = roomID;
		this.gameName = gameName;
		this.maxPlayers = maxPlayers;
		this.players = new LinkedList<>(players);
	}
	
	public void addPlayer(String nickname) {
		players.add(nickname);
	}
	
	public int getRoomID() {
		return roomID;
	}
	public String getRoomName() {
		return gameName;
	}
	public int getMaxPlayers() {
		return maxPlayers;
	}
	public LinkedList<String> getPlayers() {
		return new LinkedList<String>(players);
	}

}
