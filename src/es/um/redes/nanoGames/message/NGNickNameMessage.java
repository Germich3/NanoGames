package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGNickNameMessage extends NGMessage {
	
	private String newNickname;
	
	private NGNickNameMessage(String opcode, String newNickname) {
		super(opcode);
		this.newNickname = newNickname;
	}
	public static NGNickNameMessage changeNicknameMessage(String newNickname) {
		return new NGNickNameMessage(OP_CHANGENICKNAME, newNickname);
	}
	public static NGNickNameMessage newNicknameMessage(String newNickname) {
		return new NGNickNameMessage(OP_NEWNICKNAME, newNickname);
	}
	
	public String getNewNickname() {
		return newNickname;
	}
	
	@Override
	public byte[] toByteArray() {
		StringBuffer sb = new StringBuffer();
		sb.append("<message>");
		sb.append("<operation>");
		sb.append(this.getOpcode());
		sb.append("</operation>");
		if (this.getOpcode().equals(OP_CHANGENICKNAME)) {
			sb.append("<newnickname>");
			sb.append(this.newNickname);
			sb.append("</newnickname>");
		}
		else if (this.getOpcode().equals(OP_NEWNICKNAME)) {
				sb.append("<newnickname>");
				sb.append(this.newNickname);
				sb.append("</newnickname>");
		}
		sb.append("</message>");
		String message = sb.toString();
		return message.getBytes();
	}
	
	public static NGNickNameMessage readFromIS(String operation, String message) {
			String regexpr = "<newnickname>(.*?)</newnickname>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			String newNick = (mat.find()) ? mat.group(1) : null;
			if (operation.equals(OP_CHANGENICKNAME)) {
				return changeNicknameMessage(newNick);
			}
			else if (operation.equals(OP_NEWNICKNAME)) {
				return newNicknameMessage(newNick);
			}
			return null;
	}
	
}
