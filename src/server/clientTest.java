package server;
import java.net.*;
import java.io.*;

public class clientTest {
	static Socket clientSocket;
	public static void main(String[] args) throws Exception {
		String serverName = args[0];
		
		int port = Integer.parseInt(args[1]);
		try {
			System.out.println("Connecting to" + serverName + "on port" + port);
			clientSocket = new Socket(serverName, port);
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println("hi, how are you");
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println(in.readLine());
			System.out.println("Sent data to server");
		} catch (Exception e) {
			e.printStackTrace();
		} finally  {
			clientSocket.close();
		}

	}
}


