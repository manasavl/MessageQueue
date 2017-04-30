package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
	static Socket clientSocket;
	static String serverName;
	static String port;
	static int port1;

	public static boolean validPort(String port) {
		int i = Integer.parseInt(port);
		if (i > 1024 && i < 65535) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
		serverName = args[0];
		port = args[1];
		port1 = Integer.parseInt(port);

		if (validPort(port)) {

			while (true) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					System.out.print("client> ");

					String input = reader.readLine();
					if (input.trim().equals("")) {
						continue;
					} else if (input.trim().equals("quit")) {
						break;
					}

					clientSocket = new Socket(serverName, port1);
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String[] firstStr = input.split("\\s+");
					String firstStr1 = firstStr[0];

					/*
					 * put requests
					 */
					if (firstStr1.equals("put") & firstStr.length >= 4) {
						if (firstStr.length > 4) {
							System.out.println("Please enter message without spaces");
							continue;
						}
						out.println(input);
						System.out.println(in.readLine());
					}
					/*
					 * subscribe requests
					 */
					else if (firstStr1.equals("subscribe") & firstStr.length == 2) {
						new ClientInner(input).start();
					}
					/*
					 * publish requests
					 */
					else if (firstStr1.equals("publish") & firstStr.length == 4) {
						out.println(input);
						System.out.println(in.readLine());
					}
					/*
					 * list requests
					 */
					else if (firstStr1.equals("list") & firstStr.length == 2) {
						out.println(input);
						System.out.println(in.readLine());
					}
					/*
					 * get requests
					 */
					else if (firstStr1.equals("get") & firstStr.length == 2) {
						out.println(input);
						System.out.println(in.readLine());
					} else {
						System.out.println("Invalid arguments");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Invalid Ip address or port ");
		}
	}

	/*
	 * This thread is to process published requests from server.
	 */
	private static class ClientInner extends Thread {
		private Socket socket;
		private String input;
		PrintWriter out;
		BufferedReader in;

		public ClientInner(String input) {
			this.input = input;
		}

		public void run() {
			ServerSocket soc = null;
			try {
				socket = new Socket(serverName, port1);
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				/* 
				 * server socket in client to listen for published messages
				 */
				soc = new ServerSocket(0);
				String ipaddr = InetAddress.getLocalHost().getHostAddress();
				System.out.println(InetAddress.getLocalHost().getHostAddress());
				int port = soc.getLocalPort();
				out.println(input + " " + ipaddr + " " + port);
				System.out.println(in.readLine());

				/* listening for published messages from server */
				while (true) {
					Socket acceptedSocket = soc.accept();

					BufferedReader acceptedIn = new BufferedReader(
							new InputStreamReader(acceptedSocket.getInputStream()));
					System.out.println(acceptedIn.readLine());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					soc.close();
				} catch (Exception e) {
				}
			}
		}
	}

}
