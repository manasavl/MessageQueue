package server;

import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.*;

/*
 * This class maintains ip address and port of Queue server.
 * This class is also used to store ip address and port of subscribed clients.
 */
class QueueServerDetails {
	private String ip;
	private int port;

	public QueueServerDetails() {

	}

	public QueueServerDetails(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}

/*
 * This class works as QueueServer and ExchangeServer based on command line
 * arguments.
 */
public class Server {
	private static ServerSocket queueSocket;
	private static ServerSocket exchangeSocket;
	private static HashMap<String, LinkedList<String>> hm = new HashMap<String, LinkedList<String>>();
	private static HashMap<String, QueueServerDetails> hms = new HashMap<String, QueueServerDetails>();
	private static HashMap<String, QueueServerDetails> hmps = new HashMap<String, QueueServerDetails>();

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: create queue/exchange <queuename/exchangename>");
			return;
		}

		String s1 = args[0];
		String s2 = args[1];
		String s3 = args[2];

		/*
		 * If server is started as queue server.
		 */
		if (s1.equals("create") & s2.equals("queue")) {
			queueSocket = new ServerSocket(0);
			int port = queueSocket.getLocalPort();
			System.out.println("port number: " + port);
			hm.put(args[2], new LinkedList<String>());

			/*
			 * Accepting connections in queue server.
			 */
			try {
				while (true) {
					new QueueInner(queueSocket.accept(), s3).start();
				}
			} catch (Exception e) {
				System.out.println("Error in connection");
				e.printStackTrace();
			} finally {
				queueSocket.close();
			}
		}

		/*
		 * If server is started as exchange.
		 */
		else if (s1.equals("create") & s2.equals("exchange")) {
			exchangeSocket = new ServerSocket(0);
			int port = exchangeSocket.getLocalPort();
			System.out.println("port number: " + port);

			/*
			 * This thread job is to prompt for bind requests
			 */
			new BindQueue().start();

			/*
			 * Accepting messages in Exchange server
			 */
			try {
				while (true) {
					new ExchangeInner(exchangeSocket.accept(), s3).start();
				}
			} catch (Exception e) {
				System.out.println("Error in connection");
			} finally {
				exchangeSocket.close();
			}
		} else
			System.out.println("Invalid Arguments");
	}

	/*
	 * This thread is to process bind queue requests by exchange server.
	 */
	private static class BindQueue extends Thread {
		Socket cSocket;

		public void run() {
			while (true) {
				try {
					System.out.print("server> ");
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					String input = reader.readLine();
					if (input.trim().equals("")) {
						continue;
					} else if (input.trim().equals("quit")) {
						break;
					}

					String[] parsedArray = input.split("\\s+");

					/* Parsing bind requests and contacting queue server before adding to hashmap */
					if (parsedArray[0].equals("bind") & parsedArray.length == 4 & validPort(parsedArray[3])) {
						int portNum = Integer.parseInt(parsedArray[3]);
						cSocket = new Socket(parsedArray[2], portNum);
						PrintWriter out = new PrintWriter(cSocket.getOutputStream(), true);
						out.println("bind " + parsedArray[1]);
						BufferedReader in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

						String response = in.readLine();
						if (response.equals("ok")) {
							hms.put(parsedArray[1], new QueueServerDetails());
							hms.get(parsedArray[1]).setIp(parsedArray[2]);
							hms.get(parsedArray[1]).setPort(portNum);
							System.out.println("Exchange server bind with " + parsedArray[1] + " successful");
						} else {
							System.out.println("Invalid queue name");
						}
						cSocket.close();
					} else {
						System.out.println("Invalid Arguments, format is: bind queuename ipaddress port");
					}
				} catch (Exception e) {
					System.out.println("Invalid host name or port");
					continue;
				}
			}
		}
	}

	/*
	 * This thread is to process Queue server requests.
	 */
	private static class QueueInner extends Thread {
		private Socket socket;
		private String queueName;

		public QueueInner(Socket socket, String queueName) {
			this.socket = socket;
			this.queueName = queueName;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				String inputString = in.readLine();
				String[] parsedArray = inputString.split("\\s+");

				/*
				 * Validations done by Queue server during bind operation in
				 * Exchange server
				 */
				if (parsedArray[0].equals("bind")) {
					if (hm.containsKey(parsedArray[1])) {
						out.println("ok");
					} else {
						out.println("not");
					}
				}
				/*
				 * Returning number of messages in queue
				 */
				else if (parsedArray[0].equals("list")) {
					if (parsedArray[1].equals(queueName)) {
						System.out.println("listing queue");
						out.println(hm.get(queueName).size());
					} else {
						out.println("wrong queue name");
					}
				}
				/*
				 * Returning message from queue in FIFO order
				 */
				else if (parsedArray[0].equals("get")) {
					if (parsedArray[1].equals(queueName)) {
						System.out.println("Returned message from queue");
						out.println(hm.get(queueName).removeLast());
					} else {
						out.println("wrong queue name");
					}
				}
				/*
				 * Adding message to queue
				 */
				else if (parsedArray[0].equals("add")) {
					System.out.println("Added message to queue");
					hm.get(queueName).addFirst(parsedArray[1]);
					out.println("added");
				}
			} catch (Exception e) {
				e.printStackTrace();
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

	/*
	 * This thread is to process exchange server requests.
	 */
	private static class ExchangeInner extends Thread {
		private Socket socket;
		private String exchangeName;

		public ExchangeInner(Socket socket, String exchangeName) {
			this.socket = socket;
			this.exchangeName = exchangeName;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				String[] parsedArray = in.readLine().split("\\s+");

				/* Processing put requests from client */
				if (parsedArray[0].equals("put")) {
					if (!parsedArray[1].equals(exchangeName)) {
						out.println("Invalid exchangeName");
						return;
					}

					/*
					 *  Server will process if queue exists in hashmap
					 */
					if (hms.containsKey(parsedArray[2])) {
						QueueServerDetails qsd = hms.get(parsedArray[2]);
						Socket testsocket = new Socket(qsd.getIp(), qsd.getPort());
						PrintWriter output = new PrintWriter(testsocket.getOutputStream(), true);
						output.println("add " + parsedArray[3]);
						System.out.println("Sent the message to queue");

						/* Reading message from queue server */
						BufferedReader input = new BufferedReader(new InputStreamReader(testsocket.getInputStream()));
						System.out.println(input.readLine());

						testsocket.close();

						/* Sending message back to client */
						out.println("Sent message to queue server and added in queue");
					} 
					/*
					 * Processing fan out requests.
					 */
					else if (parsedArray[2].contains("*")) {
						String queueName = parsedArray[2].replace("*", "");
						for (String key : hms.keySet()) {
							if (key.contains(queueName)) {
								QueueServerDetails qs = hms.get(key);
								Socket multiplesocket = new Socket(qs.getIp(), qs.getPort());
								PrintWriter output = new PrintWriter(multiplesocket.getOutputStream(), true);
								output.println("add " + parsedArray[3]);
								System.out.println("Sent the message to queue");
								BufferedReader input = new BufferedReader(
										new InputStreamReader(multiplesocket.getInputStream()));
								System.out.println(input.readLine());
								multiplesocket.close();
							}
						}
						out.println("Sent message to queue server and added in all queues");
					} 
					/*
					 * Exponentially waiting for queue to be added for put operations if queue
					 * does not exist.
					 */
					else {
						int minInterval = 1000;
						int maxInterval = 1024000;
						int exponent = 2;
						int interval = minInterval;
						
						while (true) {
							/* Sleeping exponentially until 1024 seconds */
							Thread.sleep(Math.min(maxInterval, interval));
							
							/* Checking for queue in hashmap */
							if (hms.containsKey(parsedArray[2])) {
								QueueServerDetails qsd = hms.get(parsedArray[2]);
								Socket testsocket = new Socket(qsd.getIp(), qsd.getPort());
								PrintWriter output = new PrintWriter(testsocket.getOutputStream(), true);
								output.println("add " + parsedArray[3]);
								System.out.println("Sent the message to queue");

								/* Reading message from queue server */
								BufferedReader input = new BufferedReader(
										new InputStreamReader(testsocket.getInputStream()));
								System.out.println(input.readLine());

								testsocket.close();

								/* Sending message back to client */
								out.println("Sent message to queue server and added in queue");
								break;
							} 
							/* Increasing sleeping time exponentially */
							else {
								interval = interval * exponent;
								System.out.println("Waiting for queue to be up for " + interval + " ms" );
							}
						}
					}
				} 
				/*
				 * Processing subscribe requests from client
				 */
				else if (parsedArray[0].equals("subscribe")) {
					String ipaddr = parsedArray[2];
					String port = parsedArray[3];
					int port1 = Integer.parseInt(port);
					hmps.put(parsedArray[1], new QueueServerDetails(ipaddr, port1));
					out.println("You are Subscribed to " + parsedArray[1]);
				} 
				/*
				 * Processing publish requests from client 
				 */
				else if (parsedArray[0].equals("publish")) {
					for (String key : hmps.keySet()) {
						if (parsedArray[2].equals(key)) {
							QueueServerDetails qsd = hmps.get(key);
							Socket subsocket = new Socket(qsd.getIp(), qsd.getPort());
							PrintWriter output = new PrintWriter(subsocket.getOutputStream(), true);
							output.println("New message: " + parsedArray[3]);
							subsocket.close();
						}
					}	
					out.println("published message to subscribed queues");
				} 
				
				else {
					out.println("Invalid put exchange format or subscribe format");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * This function is validating port range.
	 */
	public static boolean validPort(String port) {
		int i = Integer.parseInt(port);
		if (i > 1024 && i < 65535) {
			return true;
		} else {
			return false;
		}
	}
}
