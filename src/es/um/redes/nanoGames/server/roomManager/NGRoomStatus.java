package es.um.redes.nanoGames.server.roomManager;

import es.um.redes.nanoGames.games.NGGame;

public class NGRoomStatus {
	public short statusNumber;
	//TODO Change the status to represent accurately your game status
	public NGGame status;
	
	//Status initialization
	public NGRoomStatus() {
		this(((short) 0), null);
	}

	public NGRoomStatus(int currentStatus, NGGame status) {
		this.statusNumber = ((short) currentStatus);
		this.status = status;
	}
	
	public short getStatusNumber() {
		return statusNumber;
	}
	
	public NGGame getStatus() {
		return status;
	}
}
