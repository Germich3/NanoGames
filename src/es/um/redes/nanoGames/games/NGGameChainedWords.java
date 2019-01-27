package es.um.redes.nanoGames.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGGameChainedWords extends NGGame {
	
	//TODO atributos del juego
	private String silaba;
	
	public NGGameChainedWords(String silaba, String player) {
		super(NGGame.GAME_CHAINEDWORDS, player);
		this.silaba = silaba;
	}
	
	public String getSilaba() {
		return silaba;
	}
	
	public void setSilaba(String silaba) {
		this.silaba = silaba;
	}
	
	@Override
	public String parseToString() {
		return "<game>" + NGGame.GAME_CHAINEDWORDS + "</game><silaba>" + this.silaba + "</silaba><player>" + this.player + "</player>";
	}

	public static NGGameChainedWords parseToNGGame(String toParse) {
		String regexprSil = "<silaba>(.*?)</silaba>";
		Pattern patSil = Pattern.compile(regexprSil);
		Matcher matSil = patSil.matcher(toParse);
		String sil = (matSil.find()) ? matSil.group(1) : null;
		String regexprPla = "<player>(.*?)</player>";
		Pattern patPla = Pattern.compile(regexprPla);
		Matcher matPla = patPla.matcher(toParse);
		String pla = (matPla.find()) ? matPla.group(1) : null;
		return new NGGameChainedWords(sil, pla);
	}

	@Override
	public String representInString() {
		return "The syllable is: " + this.silaba;
	}
}
