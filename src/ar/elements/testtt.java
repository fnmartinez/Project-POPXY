package ar.elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class testtt {

	public static void main(String[] args) {
		User.resetGlobalConfiguration();
		User user = new User("vicky");
		File from = new File("mails/mailNoMIME.txt");
		File to = new File("mails/mail.txt");
		
		user.setLeet(false);
		user.setAnonymous(false);
		user.setRotate(false);
		BufferedWriter writer;
		BufferedReader reader;
		MailParser parser;

		try {
			writer = new BufferedWriter(new FileWriter(to));
			reader = new BufferedReader(new FileReader(from));
			parser = new MailParser(reader, writer, user);

			writer = parser.parseMessage();
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
