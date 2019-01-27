package es.um.redes.nanoGames.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import es.um.redes.nanoGames.server.roomManager.*;

/**
 * This class contains the general status of the whole server (without the logic related to particular games)
 */
class NGServerManager {
	
	//Players registered in this server
	private HashSet<String> players;
	private HashMap<NGRoomManager, Integer> rooms;
	//Current rooms and their related RoomManagers
	//TODO Data structure to relate rooms and RoomManagers
	
	public NGServerManager() {
		this.players = new HashSet<>();
		this.rooms = new HashMap<>();
	}
	
	public void registerRoomManager(NGRoomManager rm) {
		//When a new room manager is registered we assigned it to a room
		this.rooms.put(rm, this.rooms.size() + 1);
	}
	
	//Returns the set of existing rooms
	public synchronized LinkedList<NGRoomDescription> getRoomList() {
		LinkedList<NGRoomDescription> list = new LinkedList<>();
		for (NGRoomManager room : this.rooms.keySet()) {
			list.add(room.getDescription(this.rooms.get(room)));
		}
		return list;
	}
	
	//Given a room it returns the description
	public synchronized NGRoomDescription getRoomDescription(int roomID) {
		//We make use of the RoomManager to obtain an updated description of the room
		for (NGRoomManager room : this.rooms.keySet()) {
			if (roomID == this.rooms.get(room)) {
				return room.getDescription(roomID);
			}
		}
		return null;
	}
	
	//False is returned if the nickname is already registered, True otherwise and the player is registered
	public synchronized boolean addPlayer(NGPlayerInfo player) {
		return this.players.add(player.getNick());
	}
	
	//The player is removed from the list
	public synchronized void removePlayer(NGPlayerInfo player) {
		this.players.remove(player.getNick());
	}
	
	//The player is replace from the list
	public synchronized void replacePlayer(NGPlayerInfo player, NGPlayerInfo replace) {
		this.removePlayer(player);
		this.addPlayer(replace);
	}
	
	//Find player with same nickname
		public synchronized boolean existThisPlayer(String nickname) {
			return this.players.contains(nickname);
		}
	
	//A player request to enter in a room. If the access is granted the RoomManager is returned
	public synchronized NGRoomManager enterRoom(NGPlayerInfo player, int roomID) {
		Iterator<NGRoomManager> it = this.rooms.keySet().iterator();
		while (it.hasNext()) {
			NGRoomManager r = it.next();
			if (roomID == this.rooms.get(r)) {
				if (r.registerPlayer(player)) {
					if (r.getPlayers().size() == r.getMaxPlayers()) {
						registerRoomManager(r.duplicate());
					}
					return r;
				}
			}
		}
		return null;
	}
	
	//A player leaves the room 
	public synchronized void leaveRoom(NGPlayerInfo player, int roomID) {
		Iterator<NGRoomManager> it = this.rooms.keySet().iterator();
		while (it.hasNext()) {
			NGRoomManager r = it.next();
			if (roomID == this.rooms.get(r)) {
				r.removePlayer(player);
				if (r.getPlayers().size() == 0) {
					r.restartGame();
					Set<NGRoomManager> roomsSet = new HashSet<>(this.rooms.keySet());
					for (NGRoomManager room : roomsSet) {
						if (room.getGame().equals(r.getGame())) {
							if (r != room) {
								it.remove();
							}
						}
					}
				}
			}
		}
	}
	
	public synchronized int getRoomID(NGRoomManager room) {
		return this.rooms.get(room);
	}
	
}
