package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class test1 {
public static void main(String[] args) throws InterruptedException {
	int minInterval = 1;
	int maxInterval = 1024000;
	int exponent = 2;
	int interval = minInterval;
	while(interval != 1024)
	Thread.sleep(interval);
	interval = Math.min(maxInterval, interval*exponent*1000);
	
	
	
	/*String s = "abc*";
	String s2 = s.replace("*", "");
	System.out.println(s2);
	String s3 = "abc";
	System.out.println(s3.contains(s2));
	String newData = " "; 
	//	newData = in.readLine();
		while(newData.equals(" ")) {
			System.out.println(newData +"hi");
		//	newData = in.readLine();
		}
	/*try {
		ServerSocket soc = new ServerSocket(0);
		System.out.println(InetAddress.getLocalHost().getHostAddress());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/
}
}
