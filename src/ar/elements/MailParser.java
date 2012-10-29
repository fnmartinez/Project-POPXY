package ar.elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class MailParser {

	// De aqui voy a leer el mail, el mail antes paso por una app externa
	private RandomAccessFile reader;
	// Aqui voy a guardar el mail transformado a medida q lo voy parseando
	private RandomAccessFile writer;
	// Due�o del mail
	private User user;
	private Mail mail;

	private TextTransformer textTransformer;
	private ImageTransformer imageTransformer;

	public MailParser(RandomAccessFile reader, RandomAccessFile writer, User user) {
		this.reader = reader;
		this.writer = writer;
		this.user = user;
		this.mail = new Mail();
		this.textTransformer = new TextTransformer();
		this.imageTransformer = new ImageTransformer();
	}

	public RandomAccessFile parseMessage() throws IOException {
		String line = reader.readLine();
		parseHeaders(line);
		return writer;
	}
	//TODO
	private void parseHeaders(String line) throws IOException {
		String headerName = null;
		String headerValue = "";
		do {
			// Primero guardo el nombre del header y desp apendeo los
			// extraValues
			if (headerName == null) {
				int separator = line.indexOf(":");
				if (separator == -1)
					return;
				headerName = line.substring(0, separator);
				if (line.length() > separator)
					// antes de apendear sacar espacios
					headerValue += line.substring(separator + 2).trim();
			} else {
				headerValue += line.trim();
			}
			if (headerName.equals("From") && user.getAnonymous()) {
				writeLine("From: Anonymous <unknown@unknown.com>");
			} else {
				writeLine(line);
			}
		} while ((line = reader.readLine()).length() != 0
				&& (line.startsWith(" ") || line.startsWith("\t") || line.startsWith("."))
				&& (!line.contains("Content-Type")));

		if (headerName.equals("Date")) {
			// TODO hacer fecha
		}
		if (headerName.equals("From")) {
			mail.setFrom(headerValue.split("<")[1].split(">")[0]);
		}
		mail.addHeader(headerName, headerValue);
		
		// Body's start
		if (line.length() == 0) {
			writeLine("");
			parseBody("");
			return;
		} else if (line.contains("Content-Type")) {
			parseBody(line);
			return;
		} else
			parseHeaders(line);
	}

	private void parseBody(String line) throws IOException {
		String boundary = "";
		boolean pointSpace = false;// Para saber si salgo por un espacio y no
									// existe el header ContentType: text/plain
		if (line.equals("")) {
			line = "Content-Type: text/plain";
			pointSpace = true;
		} else {
			writeLine(line);
		}
		if (line.toUpperCase().contains("MULTIPART"))
			boundary = getBoundary(line);

		if (boundary.isEmpty())
			// Single content
			putContent(line, boundary, pointSpace);
		else {
			// Multipart content
			line = reader.readLine();
			do{
				writeLine(line);
				parseContents(boundary);
			}
			while (!(line = reader.readLine()).equals("."));
		}
		writeLine(".");
		System.out.println("LLEGUEEE AL FINALLLLL");
	}

	// Antes de poner la linea en el file, agrego los bytes en las estadisticas
	// globales y por usuario
	private void writeLine(String line) throws IOException {
		writer.write((line + "\n").getBytes());
	}

	private void writeLines(String text) throws IOException {
		StringBuilder builder = new StringBuilder();
		int i = 0, count = 0;
		// builder.append(message.charAt(0));
		while (i < text.length()) {
			if (text.charAt(i) != '\n') {
				builder.append(text.charAt(i));
			}

			if (count == 76 || text.charAt(i) == '\n') {
				count = 0;
				// builder.append('\n');
				// System.out.println(builder.toString());
				writeLine(builder.toString());
				builder = new StringBuilder();
			} else {
				count++;
			}
			i++;

		}

	}

	private void parseContents(String boundary) throws IOException {
		
		String line = reader.readLine();
		if (line.contains("--" + boundary)) {
			writeLine("--" + boundary);
			line = reader.readLine();
		}
		if (line.contains("Content-Type:")) {
			writeLine(line);
			if (line.toUpperCase().contains("MULTIPART")) {
				String subBoundary = getBoundary(line);
				line = reader.readLine();
				writeLine("");
				parseContents(subBoundary);
			} else {
				line = putContent(line, boundary, false);
				if (line.equals("--" + boundary + "--")) {
					writeLine(line);
					return;
				} else if (line.contains("--" + boundary)) {
					writeLine(line);
				}
				parseContents(boundary);
			}
		}
	}

	private String getBoundary(String line) throws IOException {
		if (line.indexOf("=") == -1) {
			line = reader.readLine();
			writeLine(line);
		}
		String boundary = line.substring(line.indexOf("=") + 1);
		String[] tmp = boundary.split("\"");
		if (tmp.length >= 2)
			boundary = tmp[1];
		return boundary;
	}

	// Cuando entro a putContent, el reader apunta a line, pero line ya esta
	// guardada
	private String putContent(String line, String boundary, boolean pointSpace)
			throws IOException {
		// si pointSpace == true, quiere decir que el reader esta apuntando a la
		// linea vacia antes del inicio del cuerpo

		// En type tengo el tipo de contenido
		String type = line.substring(line.indexOf(':') + 2, line.indexOf('/'));
		mail.addContent(type);

		String ret;

		if (type.toUpperCase().equals("TEXT")) {
			ret = putContentText(line, boundary, pointSpace);
		} else {
			if (type.toUpperCase().equals("IMAGE")) {
				ret = putContentImage(line, boundary);
			} else {
				ret = putOtherContext(line, boundary);
			}
		}
		return ret;
	}

	//TODO
	private String putContentText(String contentTypeHeader, String boundary,
			boolean pointSpace) throws IOException {

		String line;
		String encoding = null;
		String text = "";

		// si pointSpace == true, quiere decir que el reader esta apuntando a la
		// linea vacia antes del inicio del cuerpo
		if (pointSpace == false) {
			while ((line = reader.readLine()).length() != 0) {
				writeLine(line);
				if (line.contains("Content-Transfer-Encoding:")) {
					encoding = line.substring(line.indexOf(":") + 2);
				}
			}
			writeLine("");
		}
		// ahora line apunta a el "" antes del body y el "" ya esta guardado en
		// el file
		if (user.getLeet()
				&& contentTypeHeader.toUpperCase().contains("TEXT/PLAIN")) {
			if (encoding == null) {
				while (!isEndLine(line = reader.readLine(), boundary)) {
					writeLine(textTransformer.l33t(line));
				}
				return line;
			} else {

				while (!isEndLine(line = reader.readLine(), boundary)) {
					// need to get the entire image to rotate it
					text += line + "\n";
				}

				if (encoding.toLowerCase().equals("quoted-printable")) {
					// transform and print text according to its encoding
					// text = decodeQuotedPrintable(text);
					// text = textTransformer.l33t(text);
					// writeLines(encodeQuotedPrintable(text));
				} else if (encoding.toLowerCase().equals("8bit")) {
					writeLines(textTransformer.l33t(text));
				}
				return line;

			}
		} else {
			return putLines(boundary);
		}
	}
	//TODO
	private String putContentImage(String contentTypeHeader, String boundary)
			throws IOException {

		String line;
		String encoding = null;
		String text = "";
		String extension = contentTypeHeader.substring(
				contentTypeHeader.indexOf('/') + 1,
				contentTypeHeader.indexOf(';'));
		mail.addStructure(extension);
		// si pointSpace == true, quiere decir que el reader esta apuntando a la
		// linea vacia antes del inicio del cuerpo
		while ((line = reader.readLine()).length() != 0) {
			writeLine(line);
			if (line.contains("Content-Transfer-Encoding:")) {
				encoding = line.substring(line.indexOf(":") + 2);
			}
		}
		writeLine("");
		// ahora line apunta a el "" antes del body y el "" ya esta guardado en
		// el file
		if (user.getRotate()) {
			// Transformar imagen codificada
			while (!isEndLine(line = reader.readLine(), boundary)) {
				// pongo en text toda la imagen codificada en base64
				text += line + "\n";
			}

			return line;
		} else {
			return putLines(boundary);
		}
	}

	private String putOtherContext(String contentTypeHeader, String boundary)
			throws IOException {

		String line;
		String extension = contentTypeHeader.substring(
				contentTypeHeader.indexOf('/') + 1,
				contentTypeHeader.indexOf(';'));
		mail.addStructure(extension);

		while ((line = reader.readLine()).length() != 0) {
			writeLine(line);
		}
		writeLine("");

		// ahora line apunta a el "" antes del body y el "" ya esta guardado en
		// el file
		return putLines(boundary);
	}

	private String putLines(String boundary) throws IOException {

		String line;
		while (!isEndLine(line = reader.readLine(), boundary)) {
			writeLine(line);
		}
		return line;

	}

	private boolean isEndLine(String line, String boundary) {
		return (boundary == "") ? line.equals(".") : line.contains("--"
				+ boundary);
	}

}
