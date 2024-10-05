import java.util.*;

import server.ChatServer;
public class Main {
	private static final int SERVER_LISTEN_PORT = 8089;
	public static void main(String[] args) {
		ChatServer.create(SERVER_LISTEN_PORT).start();
    }
}
