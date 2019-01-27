package es.um.redes.nanoGames.server.roomManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;
import es.um.redes.nanoGames.games.NGGame;
import es.um.redes.nanoGames.server.NGPlayerInfo;

public abstract class NGRoomManager implements Cloneable {
	protected NGGame game;
	protected int maxPlayers;
	protected LinkedList<NGPlayerInfo> players;
	protected LinkedList<NGPlayerInfo> score;
	protected String rules;
	protected int gameTimeout; //In milliseconds
	protected short status;
	protected boolean isEnded;
	
	protected NGRoomManager(NGGame game, int maxPlayers, LinkedList<NGPlayerInfo> players, int gameTimeout) {
		this.game = game;
		this.maxPlayers = maxPlayers;
		this.players = players;
		this.gameTimeout = gameTimeout * 1000;
		this.rules = this.setRules();
		this.status = 0;
		this.isEnded = false;
		this.score = null;
	}
	
	//the only requirement to add a player is that only MAX_PLAYERS are accepted
	public boolean registerPlayer(NGPlayerInfo player) {
		if (this.maxPlayers > this.players.size()) {
			this.players.add(player);
			this.newPlayer();
			return true;
		}
		return false;
	}
	//Rules are returned
	public String getRules() {
		return this.rules;
	}
	//Force overwrite rules
	protected abstract String setRules();
	//Endgame reason
	public abstract String endGameReason();
	//restart room
	public abstract void restartGame();
	//The current status is returned
	public abstract NGRoomStatus checkStatus(NGPlayerInfo player);
	//Check for a new challenge. We can make use of that checking in order to build a new one if the conditions are satisfied 
	public abstract NGChallenge checkChallenge(NGPlayerInfo player);
	//The player provided no answer and we process that situation
	public abstract NGRoomStatus noAnswer(NGPlayerInfo player);
	//The answer provided by the player has to be processed
	public abstract NGRoomStatus answer(NGPlayerInfo p, String answer);
	//Check if user is available to leave the room
	public abstract boolean allowExit(NGPlayerInfo p);
	//if a new player enter in the room
	public abstract void newPlayer();
	//The player is removed (maybe the status has to be updated)
	public boolean removePlayer(NGPlayerInfo player) {
		return this.players.remove(player);
	}
	//Creates a copy of the room manager
	public NGRoomManager duplicate() {
		try {
			NGRoomManager copy = (NGRoomManager) super.clone();
			copy.players = new LinkedList<>();
			copy.status = 0;
			copy.isEnded = false;
            return copy;
        } catch (CloneNotSupportedException e) {
            System.err.println("Error: Can not been cloned.");
        }
        return null;
	}
	//Returns the name of the game
	public NGGame getGame() {
		return this.game;
	}
	//Returns the description of the room
	public NGRoomDescription getDescription(int roomID) {
		LinkedList<String> playersNicks = new LinkedList<>();
		for (NGPlayerInfo p : this.players) {
			playersNicks.add(p.getNick());
		}
		return new NGRoomDescription(roomID, this.game.getGame(), this.maxPlayers, playersNicks);
	}
	//Returns the current number of players in the room
	public int playersInRoom() {
		return this.players.size();
	}
	
	public int getTimeout() {
		return gameTimeout;
	}
	
	public int getTimeoutInSecons() {
		return gameTimeout / 1000;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public LinkedList<NGPlayerInfo> getPlayers() {
		return new LinkedList<>(players);
	}
	
	public LinkedList<NGPlayerInfo> getScore() {
		return new LinkedList<>(score);
	}
	
	public Map<String, Integer> getScores() {
		if (!this.isEnded) {
			HashMap<String, Integer> scores = new HashMap<>();
			for (NGPlayerInfo p : this.players) {
				scores.put(p.getNick(), p.getScore());
			}
			return scores;
		}
		else {
			HashMap<String, Integer> scores = new HashMap<>();
			for (NGPlayerInfo p : this.score) {
				scores.put(p.getNick(), p.getScore());
			}
			return scores;
		}
	}
	
	public boolean isEnded() {
		return isEnded;
	}
	protected void endGame() {
		this.isEnded = true;
		if (this.score == null) {
			this.score = new LinkedList<>(this.players);
		}
	}
	public void startGame() {
		this.isEnded = false;
	}
}
