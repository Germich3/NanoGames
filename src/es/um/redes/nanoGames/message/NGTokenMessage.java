package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGTokenMessage extends NGMessage {

	private long token;
	private String nickname;

	
	private NGTokenMessage(String opcode, long token, String nickname) {
		super(opcode);
		this.token = token;
		this.nickname = nickname;
	}

	public static NGTokenMessage checkTokenMessage(long token) {
		return new NGTokenMessage(OP_CHECKTOKEN, token, null);
	}
	public static NGTokenMessage invalidTokenMessage() {
		return new NGTokenMessage(OP_INVALIDTOKEN, 0, null);
	}
	public static NGTokenMessage validTokenMessage(String nickname) {
		return new NGTokenMessage(OP_VALIDTOKEN, 0, nickname);
	}
	
	public long getToken() {
		return token;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	@Override
	public byte[] toByteArray() {
		StringBuffer sb = new StringBuffer();
		sb.append("<message>");
		sb.append("<operation>");
		sb.append(this.getOpcode());
		sb.append("</operation>");
		if (this.getOpcode().equals(OP_CHECKTOKEN)) {
			sb.append("<token>");
			sb.append(this.token);
			sb.append("</token>");
		}
		else if (this.getOpcode().equals(OP_VALIDTOKEN)) {
			sb.append("<nickname>");
			sb.append(this.nickname);
			sb.append("</nickname>");
		}
		else if (this.getOpcode().equals(OP_INVALIDTOKEN)) {
			//Does Nothing
		}
		sb.append("</message>");
		String message = sb.toString();
		return message.getBytes();
	}
	
	public static NGTokenMessage readFromIS(String operation, String message) {
		if (operation.equals(OP_CHECKTOKEN)) {
			long newToken = 0;
			String regexpr = "<token>(.*?)</token>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			newToken = (mat.find()) ? Long.valueOf(mat.group(1)) : null;
			return checkTokenMessage(newToken);
		}
		else if (operation.equals(OP_VALIDTOKEN)) {
			String newNickname = null;
			String regexpr = "<nickname>(.*?)</nickname>";
			Pattern pat = Pattern.compile(regexpr);
			Matcher mat = pat.matcher(message);
			if (mat.find()) {
				if (!mat.group(1).equals("null")) {
					newNickname = mat.group(1);
				}
			}
			return validTokenMessage(newNickname);
		}
		else {
			return invalidTokenMessage();
		}
	}
}
