package es.um.redes.nanoGames.games;

import java.util.LinkedList;
import es.um.redes.nanoGames.server.NGPlayerInfo;
import es.um.redes.nanoGames.server.roomManager.NGChallenge;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;
import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;

public class NGRoomChainedWordsWithoutTurns extends NGRoomManager {
	
	private int minPlayers;
	private boolean isRunning;
	
	private static final int MAX_POINTS = 20;
	private static final int INCREMENT_POINTS = 10;
	//private static final int DECREASE_POINTS = 10;
	
	public NGRoomChainedWordsWithoutTurns(int maxPlayers, int minPlayers, int gameTimeout) {
		super(new NGGameChainedWordsWithoutTurns("do", null), maxPlayers, new LinkedList<>(), gameTimeout);
		this.minPlayers = minPlayers;
		this.isRunning = false;
		this.game.setPlayer("all");
	}

	@Override
	protected String setRules() {
		return "Chain the last two letters of the opponent's word to generate a "
				+ "new one and continue the game before the time runs out.";
	}
	
	@Override
	public void newPlayer() {
		if (this.players.size() >= this.minPlayers && !isRunning) {
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
		return new NGChallenge(this.status, this.game);
	}

	@Override
	public NGRoomStatus noAnswer(NGPlayerInfo player) {
		this.status = (short)(this.status + 1);
		return new NGRoomStatus(this.status, this.game);
	}

	@Override
	public NGRoomStatus answer(NGPlayerInfo player, String answer) {
		if (!this.isEnded) {
			if (answer.startsWith(((NGGameChainedWordsWithoutTurns)this.game).getSilaba())) {
				((NGGameChainedWordsWithoutTurns)this.game).setSilaba(answer.substring(answer.length() - 2));
				for (int i = 0; i < this.players.size(); i++) {
					if (this.players.get(i).equals(player)) {
						this.players.get(i).incScore(INCREMENT_POINTS);
						if (this.players.get(i).getScore() > MAX_POINTS) {
							this.endGame();
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
	public NGRoomChainedWordsWithoutTurns duplicate() {
		NGRoomChainedWordsWithoutTurns clone = (NGRoomChainedWordsWithoutTurns)super.duplicate();
		clone.isRunning = false;
		return clone;
	}

	@Override
	public String endGameReason() {
		for (NGPlayerInfo p : this.score) {
			if (p.getScore() >= MAX_POINTS) {
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
