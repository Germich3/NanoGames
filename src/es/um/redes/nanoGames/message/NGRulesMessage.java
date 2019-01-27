package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGRulesMessage extends NGMessage {

	private String rules;
	private Integer room;
	
	private NGRulesMessage(String opcode, String rules, Integer room) {
		super(opcode);
		this.rules = rules;
		this.room = room;
	}

	public String getRules() {
		return rules;
	}
	public int getRoom() {
		return room;
	}
	
	public static NGRulesMessage getRulesMessage(int room) {
		return new NGRulesMessage(OP_GETRULES, null, room);
	}
	public static NGRulesMessage rulesMessage(String rules) {
		return new NGRulesMessage(OP_RULES, rules, null);
	}
	
	@Override
	public byte[] toByteArray() {
		StringBuffer sb = new StringBuffer();
		sb.append("<message>");
		sb.append("<operation>");
		sb.append(this.getOpcode());
		sb.append("</operation>");
		if (this.getOpcode().equals(OP_GETRULES)) {
			sb.append("<room>");
			sb.append(this.room);
			sb.append("</room>");
		}
		else if (this.getOpcode().equals(OP_RULES)) {
			sb.append("<rules>");
			sb.append(this.rules);
			sb.append("</rules>");
		}
		sb.append("</message>");
		String message = sb.toString();
		return message.getBytes();
	}

	public static NGRulesMessage readFromIS(String operation, String message) {
		if (operation.equals(OP_GETRULES)) {
			String regexpr = "<room>(.*?)</room>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			String roomID = (mat.find()) ? mat.group(1) : null;
			return getRulesMessage(Integer.valueOf(roomID));
		}
		else if (operation.equals(OP_RULES)) {
			String regexpr = "<rules>(.*?)</rules>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			String rules = (mat.find()) ? mat.group(1) : null;
			return rulesMessage(rules);
		}
		return null;
	}
}
