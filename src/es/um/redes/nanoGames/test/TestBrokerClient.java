package es.um.redes.nanoGames.test;

import java.io.IOException;

import es.um.redes.nanoGames.broker.BrokerClient;

public class TestBrokerClient {

	public static void main(String[] args) {
		try {
			BrokerClient bk1 = new BrokerClient("192.168.56.3");
			System.out.println(bk1.getToken());
		} catch (IOException e) {
			System.out.println("Error: " + e);
		}
		
	}

}
