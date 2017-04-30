package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Split {
	
	public static void main(String[] args) throws Exception {
		
		System.out.print("Enter String >");
		BufferedReader reader = new BufferedReader(new
				 InputStreamReader(System.in));
		
		String input = reader.readLine();
		String[] bindStr = input.split("\\s+");
		String str1 = bindStr[0];
		String str2 = bindStr[1];
		String str3 = bindStr[2];
		String str4 = bindStr[3];
		
		//if(str1.equals("bind")&)
		System.out.println(bindStr.length == 4);
		
	/*	for(int i = 0; i < bindStr.length; i++) {
			System.out.println(bindStr[i]);
		}
		*/
	}

}
