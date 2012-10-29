package ar.elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class testtt {

	public static void main(String[] args) {
		User.resetGlobalConfiguration();
		User user = new User("vicky");
		File from = new File("mails/textPlainBase64.txt");
		File to = new File("mails/mail.txt");
		
		user.setLeet(true);
		user.setAnonymous(false);
		user.setRotate(true);
		RandomAccessFile writer;
		RandomAccessFile reader;
		MailParser parser;

		try {
			writer = new RandomAccessFile(to,"rw");
			reader = new RandomAccessFile(from,"rw");
			parser = new MailParser(reader, writer, user);

			writer = parser.parseMessage();
			if (writer != null) {
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
