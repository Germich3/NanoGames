package es.um.redes.nanoGames.server.roomManager;

import es.um.redes.nanoGames.games.NGGame;

public class NGChallenge {
	public short challengeNumber;
	//TODO Change the challenge to represent accurately your game challenge
	public NGGame challenge;
	
	//Status initialization
	public NGChallenge() {
		this(((short) 0), null);
	}

	public NGChallenge(short currentChallengeNumber, NGGame currentChallenge) {
		this.challengeNumber = currentChallengeNumber;
		this.challenge = currentChallenge;
	}
	
	public short getChallengeNumber() {
		return challengeNumber;
	}
	public NGGame getChallenge() {
		return challenge;
	}

}
