package es.um.redes.nanoGames.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NGGame {
	
	public static final String GAME_CHAINEDWORDS = "Chained Words";
	public static final String GAME_CHAINEDWORDSWT = "Chained Words Without Turns";
	
	private String game;
	protected String player;
	
	protected NGGame(String game, String player) {
		this.game = game;
		this.player = player;
	}
	
	public String getGame() {
		return game;
	}
	public String getPlayer() {
		return player;
	}
	public void setPlayer(String player) {
		this.player = player;
	}
	
	public abstract String parseToString();
	
	public abstract String representInString();
	
	public static NGGame parseToNGGame(String toParse) {
		String regexpr = "<game>(.*?)</game>";
		Pattern pat = Pattern.compile(regexpr);
		Matcher mat = pat.matcher(toParse);
		String game = (mat.find()) ? mat.group(1) : null;
		if (game.equals(NGGame.GAME_CHAINEDWORDS)) {
			return NGGameChainedWords.parseToNGGame(toParse);
		}
		else if (game.equals(NGGame.GAME_CHAINEDWORDSWT)) {
			return NGGameChainedWordsWithoutTurns.parseToNGGame(toParse);
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((game == null) ? 0 : game.hashCode());
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
		NGGame other = (NGGame) obj;
		if (game == null) {
			if (other.game != null)
				return false;
		} else if (!game.equals(other.game))
			return false;
		return true;
	}
	
}
