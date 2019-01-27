package es.um.redes.nanoGames.server;

import org.apache.commons.lang3.RandomStringUtils;

public class NGPlayerInfo {
	
	//TODO Include additional fields if required
	private String nick; //Nickname of the user
	private byte status; //Current status of the user (according to the automata)
	private int score;  //Current score of the user
	
	//Default constructor
	public NGPlayerInfo() {
			this.nick = RandomStringUtils.randomAlphanumeric(8);
			this.status = 0;
			this.score = 0;
	}
	
	//Constructor to make copies
	public NGPlayerInfo(NGPlayerInfo p) {
		this.nick = new String(p.nick);
		this.status = p.status;
		this.score = p.score;
	}
	
	public String getNick() {
		return nick;
	}
	public int getScore() {
		return score;
	}
	public byte getStatus() {
		return status;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	
	public void incScore(int value) {
		this.score = this.score + value;
	}
	
	public void decScore(int value) {
		this.score = this.score - value;
		if (this.score < 0) {
			this.score = 0;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nick == null) ? 0 : nick.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NGPlayerInfo other = (NGPlayerInfo) obj;
		if (nick == null) {
			if (other.nick != null)
				return false;
		} else if (!nick.equals(other.nick))
			return false;
		return true;
	}
	
}
