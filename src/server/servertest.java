package server;
import java.net.*;
import java.io.*;

public class servertest {
	
	private static ServerSocket serverSocket;
	private static Socket clientSocket;

	
	public static void main(String[] args) throws Exception{
	int port = Integer.parseInt(args[0]);
		while (true) {
			try {
				serverSocket = new ServerSocket(port);
				new ServerInner(serverSocket.accept()).start();
			} catch (Exception e) {
				System.out.println("Error in connection");
			} finally {
				serverSocket.close();
			}
		}
	}
	
	public static class ServerInner extends Thread {
		private Socket socket;
		
		public ServerInner(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				System.out.println(in.readLine());
				// DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				out.println("Thanks for conecting");
				//out.writeUTF("Thanks for conecting");
				
			} catch (Exception e) {
				System.out.println("Error in run");
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}


