package me.thisorthat.sa.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;

public class CMDSender {
	public static void sendCommand(String command, String server) {
		try {
			if (SendAll.enabled) {
				PrintWriter out = new PrintWriter(SendAll.getSoc().getOutputStream(), true);

				out.println("exec:*:" + SendAll.passwordHashed + ";" + server + ";" + command);
				System.err.println("Sent command '" + command + "' to server '" + server + "'");
			} else {
				System.err.println("Cannot send command! No server connection!");
				return;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
