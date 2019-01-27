package es.um.redes.nanoGames.client.application;

import java.io.IOException;

import es.um.redes.nanoGames.client.shell.NGCommands;

public class NanoGame {

	public static void main(String[] args) {

		//Check the two required arguments 
		if (args.length != 2) {
			System.out.println("Usage: java NanoGame <broker_hostname> <server_hostname>");
			return;
		}
		// Create controller object that will accept and process user commands
		try {
			NGController controller = new NGController(args[0],args[1]);
			// Begin conversation with broker by getting the token
			if (controller.sendToken()) {
				// Begin accepting commands from user using shell
				controller.forceThisCommand(NGCommands.COM_ROOMLIST, null);
				do {
					controller.readGeneralCommandFromShell();
					controller.processCommand();
				} while (controller.shouldQuit() == false);
			}
			else {
				//System.out.println("ERROR: broker not available.");
			}
			System.out.println("Bye.");
		}
		catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}
}
