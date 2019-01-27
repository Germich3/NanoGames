package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGAlertMessage extends NGMessage {

	private String alert;
	
	private NGAlertMessage(String opcode, String alert) {
		super(opcode);
		this.alert = alert;
	}

	public static NGAlertMessage alertMessage(String alert) {
		return new NGAlertMessage(OP_ALERT, alert);
	}
	
	public String getAlert() {
		return alert;
	}
	
	@Override
	public byte[] toByteArray() {
		StringBuffer sb = new StringBuffer();
		sb.append("<message>");
		sb.append("<operation>");
		sb.append(this.getOpcode());
		sb.append("</operation>");
		sb.append("<reason>");
		sb.append(this.alert);
		sb.append("</reason>");
		sb.append("</message>");
		String message = sb.toString();
		return message.getBytes();
	}

	public static NGAlertMessage readFromIS(String operation, String message) {
		String regexpr = "<reason>(.*?)</reason>";
		Pattern pat = Pattern.compile(regexpr);
		Matcher mat = pat.matcher(message);
		String reason = (mat.find()) ? mat.group(1) : null;
		return alertMessage(reason);
	}
	
}
