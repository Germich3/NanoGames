package es.um.redes.nanoGames.server;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timeout extends TimerTask {

	private AtomicBoolean timeout_triggered;
	
	public Timeout() {
		this.timeout_triggered = new AtomicBoolean();
	}
	
	@Override
	public void run() {
		this.timeout_triggered.set(true);
	}

	public AtomicBoolean getTimeout_triggered() {
		return timeout_triggered;
	}
	
}
