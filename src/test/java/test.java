
import java.io.*;
import java.net.Socket;
import java.security.*;

import icedev.ws.*;
import icedev.ws.util.LineReader;

public class test {

	public test() {
		// TODO Auto-generated constructor stub
	}
	
	private static long getDigits(String gunwo) {
		String digits = "";
		for(int i=0; i< gunwo.length(); i++) {
			char c = gunwo.charAt(i);
			if(Character.isDigit(c))
				digits += c;
		}
		return Long.parseLong(digits);
	}
	
	private static int getSpaces(String gunwo) {
		int spaces = 0;
		for(int i=0; i< gunwo.length(); i++) {
			char c = gunwo.charAt(i);
			if(c == ' ')
				spaces += 1;
		}
		return spaces;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("line.separator", "\r\n");
		String key1 = "42P 1 e794U*p57 1`  5";
		String key2 = "3  X5 2h  7 c   16W44 W 4 0d";
		
		
		String packed = Pack.pack("ii", 421, 521);
		
		Pack p = Pack.unpack(packed);
		
		int x = p.nextInt();
		int y = p.nextInt();
		
		
		System.out.println("x" + x + ", y"+y);
		
		int spaces1 = getSpaces(key1);
		int spaces2 = getSpaces(key2);
		long digits1 = getDigits(key1);
		long digits2 = getDigits(key2);
		
		
		int k1 = (int) (digits1 / spaces1);
		int k2 = (int) (digits2 / spaces2);
		
		System.out.println(digits1 + " / " + spaces1 + " = " + k1);
		System.out.println(digits2 + " / " + spaces2 + " = " + k2);
		
		byte[] key3 = unhex("FA:91:A7:65:C0:6B:9E:B7");
		byte[] bytes = new byte[16];

		bytes[0] = (byte) ((k1>>24)& 0xFF);
		bytes[1] = (byte) ((k1>>16)& 0xFF);
		bytes[2] = (byte) ((k1>>8)& 0xFF);
		bytes[3] = (byte) (k1 & 0xFF);

		bytes[4] = (byte) ((k2>>24)& 0xFF);
		bytes[5] = (byte) ((k2>>16)& 0xFF);
		bytes[6] = (byte) ((k2>>8)& 0xFF);
		bytes[7] = (byte) (k2 & 0xFF);
		
		System.arraycopy(key3, 0, bytes, 8, 8);
		
		
		Socket sock = new Socket("127.0.0.1", 8080);
		
		
		OutputStream out = sock.getOutputStream();
		
		
		PrintStream ps = new PrintStream(out);
		
		ps.println("GET /czat.exe HTTP/1.1");
		ps.println("Upgrade: WebSocket");
		ps.println("Connection: Upgrade");
		ps.println("Host: 127.0.0.1:8080");
		ps.println("Origin: http://plecypioko.pl");
		ps.println("Sec-WebSocket-Key1: 42P 1 e794U*p57 1`  5");
		ps.println("Sec-WebSocket-Key2: 3  X5 2h  7 c   16W44 W 4 0d");
		ps.println();
		ps.write(unhex("FA:91:A7:65:C0:6B:9E:B7"));
		ps.flush();
		
		
		InputStream in = sock.getInputStream();
		
		LineReader lr = new LineReader(in);
		
		String line;
		while(!(line=lr.readLine()).isEmpty()) {
			System.out.println(line);
		}
		
		byte[] keyz = new byte[4 + 4 + 8];
		
		int r = in.read(keyz);
		System.out.println("readen" + r);
//		
		byte[] buffer = new byte[4000];
//		String str = hex(keyz,  0, r);
//		System.out.println(str);
		while(true) {
			
			int opcode = in.read();
			sock.setSoTimeout(3500); // we dont want to get stuck reading packet
			
			if(opcode == 0xFF) {
				throw new IOException("end!");
			}
			if(opcode != 0x00)
				throw new IOException("Expected 0x00, got 0x" + hex(opcode));
			
			int i = 0;
			while(true) {
				int read = in.read();
				if(read==0xFF) {
					System.out.println("wiadomosc: " + new String(buffer, 0, i, "UTF8"));
				} else {
					buffer[i++] = (byte) read;
				}
				
				if(read == -1)
					throw new IOException("end?");
			}
			
			
//			int read = in.read();
//			if(read==-1)
//				return;
//			//if(read == '\r')
//			//	System.out.print("\\r");
//			//else
//				System.out.println(hex(read));
		}
//		MessageDigest md5;
//		try {
//			md5 = MessageDigest.getInstance( "MD5" );
//		} catch ( NoSuchAlgorithmException e ) {
//			throw new RuntimeException( e );
//		}
//		bytes = md5.digest( bytes );
//		System.out.println(hex(key3));
//		System.out.println(hex(bytes));
		//System.out.println(new String(bytes, "utf8"));
	}
	
	static final char[] hex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	private static String hex(byte[] bytes, int off, int len) {
		String hax = "";
		boolean first = true;
		for(int i= 0; i<len; i++) {
			if(first)
				first=false;
			else
				hax += ":";
			int val = bytes[off+i] & 0xFF;
			hax += hex[val/16];
			hax += hex[val%16];
		}
		return hax;
	}
	private static String hex(int val) {
		return hex[val/16] + "" + hex[val%16];
	}
	private static byte[] unhex(String hax) {
		String[] haxes = hax.split("\\:");
		byte[] hexes = new byte[haxes.length];
		
		for(int i=0;i<haxes.length; i++) {
			hexes[i] = (byte) Integer.parseInt(haxes[i], 16);
		}
		return hexes;
	}

}
