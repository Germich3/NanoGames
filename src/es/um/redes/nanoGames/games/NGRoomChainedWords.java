package es.um.redes.nanoGames.games;

import java.util.LinkedList;
import es.um.redes.nanoGames.server.NGPlayerInfo;
import es.um.redes.nanoGames.server.roomManager.NGChallenge;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;
import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;

public class NGRoomChainedWords extends NGRoomManager {
	
	private int minPlayers;
	private boolean isRunning;
	
	private static final int MAX_POINTS = 100;
	private static final int INCREMENT_POINTS = 10;
	//private static final int DECREASE_POINTS = 10;
	
	public NGRoomChainedWords(int maxPlayers, int minPlayers, int gameTimeout) {
		super(new NGGameChainedWords("do", null), maxPlayers, new LinkedList<>(), gameTimeout);
		this.minPlayers = minPlayers;
		this.isRunning = false;
	}

	@Override
	protected String setRules() {
		return "Chain the last two letters of the opponent's word to generate a "
				+ "new one and continue the game before the time runs out.";
	}
	
	@Override
	public void newPlayer() {
		if (this.players.size() >= this.minPlayers && !isRunning) {
			this.game.setPlayer(this.players.get(0).getNick());
			isRunning = true;
		}
		this.status = (short)(this.status + 1);
	}
	
	@Override
	public NGRoomStatus checkStatus(NGPlayerInfo player) {
		return new NGRoomStatus(this.status, this.game);
	}
		
	@Override
	public NGChallenge checkChallenge(NGPlayerInfo player) {
		if (this.game.getPlayer() == null) {
			this.game.setPlayer(player.getNick());
			return new NGChallenge(this.status, this.game);
		}
		else if (this.game.getPlayer().equals(player.getNick())) {
			return new NGChallenge(this.status, this.game);
		}
		return null;
	}

	@Override
	public NGRoomStatus noAnswer(NGPlayerInfo player) {
		for (int i = 0; i < this.players.size(); i++) {
			if (this.players.get(i).equals(player)) {
				if (i+1 == this.players.size()) {
					((NGGameChainedWords)this.game).setPlayer(this.players.get(0).getNick());
				} 
				else {
					((NGGameChainedWords)this.game).setPlayer(this.players.get(i + 1).getNick());
				}
			}
		}
		this.status = (short)(this.status + 1);
		return new NGRoomStatus(this.status, this.game);
	}

	@Override
	public NGRoomStatus answer(NGPlayerInfo player, String answer) {
		if (!this.isEnded) {
			if (((NGGameChainedWords)this.game).getPlayer().equals(player.getNick())) {
				if (answer.startsWith(((NGGameChainedWords)this.game).getSilaba())) {
					((NGGameChainedWords)this.game).setSilaba(answer.substring(answer.length() - 2));
					for (int i = 0; i < this.players.size(); i++) {
						if (this.players.get(i).equals(player)) {
							this.players.get(i).incScore(INCREMENT_POINTS);
							if (this.players.get(i).getScore() > MAX_POINTS) {
								this.endGame();
							}
							if (i+1 == this.players.size()) {
								((NGGameChainedWords)this.game).setPlayer(this.players.get(0).getNick());
							} 
							else {
								((NGGameChainedWords)this.game).setPlayer(this.players.get(i + 1).getNick());
							}
						}
					}
				}
				this.status = (short)(this.status + 1);
			}
		}
		return new NGRoomStatus(this.status, this.game);
	}

	@Override
	public boolean allowExit(NGPlayerInfo p) {
		return true;
	}

	@Override
	public NGRoomChainedWords duplicate() {
		NGRoomChainedWords clone = (NGRoomChainedWords)super.duplicate();
		clone.isRunning = false;
		return clone;
	}
	
	@Override
	public String endGameReason() {
		for (NGPlayerInfo p : this.score) {
			if (p.getScore() == MAX_POINTS) {
				return "The winner is: " + p.getNick();
			}
		}
		return "No one is the winner.";
	}
	
	@Override
	public void restartGame() {
		this.players = new LinkedList<>();
		this.status = 0;
		this.isEnded = false;
		this.isRunning = false;
	}
	
}
