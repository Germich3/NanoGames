package es.um.redes.nanoGames.message;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import es.um.redes.nanoGames.games.NGGame;

public class NGGameMessage extends NGMessage {
	
	private String reason;
	private String answer;
	private NGGame gameRepresentation;
	private HashMap<String, Integer> score;
	private String timeout;
	
	private NGGameMessage(String opcode, String reason, String answer, NGGame gameRepresentation, Map<String, Integer> score, String timeout) {
		super(opcode);
		this.reason = reason;
		this.answer = answer;
		this.gameRepresentation = gameRepresentation;
		if (score != null) {
			this.score = new HashMap<>(score);
		}
		else {
			this.score = null;
		}
		this.timeout = timeout;
	}
	
	public static NGGameMessage questionMessage(NGGame gameRepresentation) {
		return new NGGameMessage(OP_QUESTION, null, null, gameRepresentation, null, null);
	}
	public static NGGameMessage answerMessage(String answer) {
		return new NGGameMessage(OP_ANSWER, null, answer, null, null, null);
	}
	public static NGGameMessage statusMessage(NGGame gameRepresentation, Map<String, Integer> score, String timeout) {
		return new NGGameMessage(OP_STATUS, null, null, gameRepresentation, score, timeout);
	}
	public static NGGameMessage endgameMessage(String reason) {
		return new NGGameMessage(OP_ENDGAME, reason, null, null, null, null);
	}
	
	public String getAnswer() {
		return answer;
	}
	public NGGame getGameRepresentation() {
		return gameRepresentation;
	}
	public String getReason() {
		return reason;
	}
	public Map<String, Integer> getScore() {
		return new HashMap<>(score);
	}
	public String getTimeout() {
		return timeout;
	}
	
	@Override
	public byte[] toByteArray() {
		StringBuffer sb = new StringBuffer();
		sb.append("<message>");
		sb.append("<operation>");
		sb.append(this.getOpcode());
		sb.append("</operation>");
		if (this.getOpcode().equals(OP_QUESTION)) {
			sb.append("<question>");
			sb.append(this.gameRepresentation.parseToString());
			sb.append("</question>");
		}
		else if (this.getOpcode().equals(OP_ANSWER)) {
				sb.append("<answer>");
				sb.append(this.answer);
				sb.append("</answer>");
		}
		else if (this.getOpcode().equals(OP_STATUS)) {
			sb.append("<status>");
			sb.append(this.gameRepresentation.parseToString());
			sb.append("<scores>");
			for (String p : this.score.keySet()) {
				sb.append("<score>");
				sb.append(p + ":" + this.score.get(p));
				sb.append("</score>");
			}
			sb.append("</scores>");
			sb.append("<timeout>");
			sb.append(this.timeout);
			sb.append("</timeout>");
			sb.append("</status>");
		}
		else if (this.getOpcode().equals(OP_ENDGAME)) {
			sb.append("<reason>");
			sb.append(this.reason);
			sb.append("</reason>");
	}
		sb.append("</message>");
		String message = sb.toString();
		return message.getBytes();
	}

	public static NGGameMessage readFromIS(String operation, String message) {
		if (operation.equals(OP_QUESTION)) {
			String regexpr = "<question>(.*?)</question>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			String question = (mat.find()) ? mat.group(1) : null;
			return questionMessage(NGGame.parseToNGGame(question));
		}
		else if (operation.equals(OP_ANSWER)) {
			String regexpr = "<answer>(.*?)</answer>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			String answer = (mat.find()) ? mat.group(1) : null;
			return answerMessage(answer);
		}
		else if (operation.equals(OP_STATUS)) {
			String regexprSta = "<status>(.*?)</status>";
			Pattern patSta = Pattern.compile(regexprSta);
			Matcher matSta = patSta.matcher(message);
			String status = (matSta.find()) ? matSta.group(1) : null;
			String regexprTim = "<timeout>(.*?)</timeout>";
			Pattern patTim = Pattern.compile(regexprTim);
			Matcher matTim = patTim.matcher(message);
			String timeout = (matTim.find()) ? matTim.group(1) : null;
			
			String rxScores = "<scores>(.*?)</scores>";
			Pattern patScores = Pattern.compile(rxScores);
			Matcher matScores = patScores.matcher(message);
			String scores = (matScores.find()) ? matScores.group(1) : "null";
			HashMap<String, Integer> scomap = new HashMap<String, Integer>();
			
			String rxS = "<score>(.*?)</score>";
			Pattern patS = Pattern.compile(rxS);
			Matcher matS = patS.matcher(scores);
			while (matS.find()) {
				String s = matS.group(1);
				String[] parts = s.split(":");
				String player = parts[0];
				String score = parts[1];
				scomap.put(player, Integer.valueOf(score));
			}
			return statusMessage(NGGame.parseToNGGame(status), scomap, timeout);
		}
		else if (operation.equals(OP_ENDGAME)) {
			String regexpr = "<reason>(.*?)</reason>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			String endgame = (mat.find()) ? mat.group(1) : null;
			return endgameMessage(endgame);
		}
		return null;
	}
}
