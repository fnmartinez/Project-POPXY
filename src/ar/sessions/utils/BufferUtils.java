package ar.sessions.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class BufferUtils {
	

	public static Charset charset = Charset.forName("UTF-8");
	public static CharsetEncoder encoder = charset.newEncoder();
	public static CharsetDecoder decoder = charset.newDecoder();
	
	public static String byteBufferToString(ByteBuffer buffer){
		String msg = new String(buffer.array());
		msg = msg.substring(buffer.position(), buffer.limit());
		return msg;
	}
	
	public static String byteBufferToString(ByteBuffer[] buffer){
		  String data = "";
		  for(ByteBuffer b: buffer){
			  data = data + byteBufferToString(b);
		  }
		  return data;
		}

}
