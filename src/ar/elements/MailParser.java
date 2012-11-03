package ar.elements;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.joda.time.DateTime;

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

	public MailParser(RandomAccessFile reader, RandomAccessFile writer,
			User user) {
		this.reader = reader;
		this.writer = writer;
		this.user = user;
		this.mail = new Mail();
		this.textTransformer = new TextTransformer();
		this.imageTransformer = new ImageTransformer();
	}

	public RandomAccessFile parseMessage() throws IOException {
		String line = reader.readLine();
		parseHeaders(line,"","");
		return writer;
	}

	public RandomAccessFile parseOnlyHeadersMessage() throws IOException {
		String line = reader.readLine();
		parseOnlyHeaders(line,"","");
		return writer;
	}

	private void writeLine(String line) throws IOException {
		writer.write((line + "\r\n").getBytes());
	}

	private void writeLines(String text) throws IOException {
		StringBuilder builder = new StringBuilder();
		int i = 0, count = 0;
		while (i < text.length()) {
			if (text.charAt(i) != '\n') {
				builder.append(text.charAt(i));
			}
			if (count == 75 || text.charAt(i) == '\n') {
				count = 0;
				writeLine(builder.toString());
				builder = new StringBuilder();
			} else {
				count++;
			}
			i++;
		}
	}

	private void parseOnlyHeaders(String line, String contentType,
			String boundary) throws IOException {
		String headerName = null;
		String headerValue = "";
		do {
			// Primero guardo el nombre del header y desp apendeo los
			// extraValues
			if (headerName == null) {
				int separator = line.indexOf(":");
				if (separator == -1) {
					System.out.println("Invalid mail: invalid header");
					return;
				}
				headerName = line.substring(0, separator);
				if (line.length() > separator)
					// antes de apendear sacar espacios
					headerValue += line.substring(separator + 2).trim();
			} else {
				headerValue += line.trim();
			}
			if (headerName.equals("From") && user.getAnonymous()) {
				writeLine("From: Anonymous <unknown@unknown.com>");
			} else if (headerName.equals("Return-path") && user.getAnonymous()) {
				writeLine("Return-path: <unknown@unknown.com>");
			} else if (headerName.equals("Content-Type")) {
				if (line.toUpperCase().contains("MULTIPART")) {
					contentType = line;
					writeLine(line);
					boundary = getBoundary(line);
				}
			} else {
				writeLine(line);
			}
		} while ((line = reader.readLine()).length() != 0
				&& (line.startsWith(" ") || line.startsWith("\t") || line
						.startsWith(".")));

		if (headerName.equals("Date")) {
			String[] date = headerValue.split("\\s");
			String[] compHour = date[4].split(":");
			int year = Integer.valueOf(date[3]);
			int month = getMonth(date[2]);
			int day = Integer.valueOf(date[1]);
			int hour = Integer.valueOf(compHour[0]);
			int min = Integer.valueOf(compHour[1]);
			int sec = Integer.valueOf(compHour[2]);
			mail.setDate(new DateTime(year, month, day, hour, min, sec, 0));
		}
		if (headerName.equals("From")) {
			mail.setFrom(headerValue.split("<")[1].split(">")[0]);
		}
		mail.addHeader(headerName, headerValue);

		// Body's start
		if (line.length() == 0) {
			writeLine(line);
			line = reader.readLine();
			if (line.equals(".")) {
				writeLine(line);
				System.out.println("Parser only headers: OK");
				return;
			} else {
				System.out
						.println("Invalid mail: Needs \".\" at the end of the mail");
				return;
			}
		} else {
			parseOnlyHeaders(line, contentType, boundary);
		}
	}

	private void parseHeaders(String line, String contentType, String boundary)
			throws IOException {
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
			} else if (headerName.equals("Return-path") && user.getAnonymous()) {
				writeLine("Return-path: <unknown@unknown.com>");
			} else if (headerName.equals("Content-Type")) {
				if (line.toUpperCase().contains("MULTIPART")) {
					contentType = line;
					writeLine(line);
					boundary = getBoundary(line);
				}
			} else {
				writeLine(line);
			}
		} while ((line = reader.readLine()).length() != 0
				&& (line.startsWith(" ") || line.startsWith("\t") || line
						.startsWith(".")));
		// Date: Sun, 28 Oct 2012 19:56:58 -0300
		if (headerName.equals("Date")) {
			String[] date = headerValue.split("\\s");
			String[] compHour = date[4].split(":");
			int year = Integer.valueOf(date[3]);
			int month = getMonth(date[2]);
			int day = Integer.valueOf(date[1]);
			int hour = Integer.valueOf(compHour[0]);
			int min = Integer.valueOf(compHour[1]);
			int sec = Integer.valueOf(compHour[2]);
			mail.setDate(new DateTime(year, month, day, hour, min, sec, 0));
		}
		if (headerName.equals("From")) {
			mail.setFrom(headerValue.split("<")[1].split(">")[0]);
		}
		mail.addHeader(headerName, headerValue);

		// Body's start
		if (line.length() == 0) {
			writeLine("");
			parseBody("", contentType, boundary);
			return;
		} else
			parseHeaders(line, contentType, boundary);
	}

	private void parseBody(String line, String contentType, String boundary)
			throws IOException {

		boolean pointSpace = true;// Para saber si salgo por un espacio y no
									// existe el header ContentType: text/plain
		boolean firstTime = true;
		if (contentType.equals("")) {
			line = "Content-Type: text/plain";
		} else {
			line = contentType;
		}
		if (boundary.isEmpty()) {
			// Single content
			putContent(line, boundary, pointSpace);
		} else {
			// Multipart content
			do {
				if (firstTime) {
					firstTime = false;
				} else {
					writeLine(line);
				}
				String ret = parseContents(boundary);
				if (ret != null && ret.equals(".")) {
					break;
				}
			} while (!(line = reader.readLine()).equals("."));
		}
		writeLine(".");
		System.out.println("Parser: OK");
	}

	private String parseContents(String boundary) throws IOException {

		String line = reader.readLine();
		if (line == null) {
			return ".";
		}
		if (line.contains("--" + boundary)) {
			writeLine("--" + boundary);
			line = reader.readLine();
		}
		if (line.contains("Content-Type:")) {
			writeLine(line);
			if (line.toUpperCase().contains("MULTIPART")) {
				String subBoundary = getBoundary(line);
				line = reader.readLine();
				writeLine(line);
				parseContents(subBoundary);
				return "";
			} else {
				line = putContent(line, boundary, false);
				if (line.equals("--" + boundary + "--")) {
					writeLine(line);
					return null;
				} else if (line.contains("--" + boundary)) {
					writeLine(line);
				}
				parseContents(boundary);
				return null;
			}
		} else {
			if (!line.equals(".")) {
				writeLine(line);
				parseContents(boundary);
				return null;
			} else {
				return ".";
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
		if (tmp.length >= 2) {
			boundary = tmp[1];
		}
		return boundary;
	}

	// Cuando entro a putContent, el reader apunta a line, pero line ya esta
	// guardada
	private String putContent(String line, String boundary, boolean pointSpace)
			throws IOException {
		// En type tengo el tipo de contenido
		String type = line.substring(line.indexOf(':') + 2, line.indexOf('/'));
		mail.addContent(type);

		String ret;
		System.out.println("TYPE" + type);
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
				if ((encoding.toLowerCase().equals("base64"))) {
					while (!isEndLine(line = reader.readLine(), boundary)) {
						// Pongo el texto completo en text
						text += line;
					}
					text += '\n';
					// byte[] txt = decodeBase64(text);
					// System.out.println("Antes de transformar:" +
					// txt.toString());
					// String transformed =
					// textTransformer.l33t(txt.toString());
					// System.out.println("DESPUES de transformar:" +
					// transformed);
					// text = encodeBase64(transformed.getBytes());
				} else if (encoding.toLowerCase().equals("8bit")) {
					while (!isEndLine(line = reader.readLine(), boundary)) {
						text += line;
					}
					text += '\n';
					text = textTransformer.l33t(text);
				} else if (encoding.toLowerCase().equals("quoted-printable")) {
					while (!isEndLine(line = reader.readLine(), boundary)) {
						text += line + '\n';
					}
					text = decodeQuotedPrintable(text);
					text = textTransformer.l33t(text);
					text = encodeQuotedPrintable(text);
				} else {
					while (!isEndLine(line = reader.readLine(), boundary)) {
						text += line;
					}
					text += '\n';
				}
				writeLines(text);
				return line;
			}
		} else {
			return putLines(boundary);
		}
	}

	private String putContentImage(String contentTypeHeader, String boundary)
			throws IOException {

		String line;
		String encoding = null;
		String text = "";
		String extension;
		int last = contentTypeHeader.indexOf(';');
		if (last != -1) {
			extension = contentTypeHeader.substring(
					contentTypeHeader.indexOf('/') + 1, last);
		} else {
			extension = contentTypeHeader.substring(contentTypeHeader
					.indexOf('/') + 1);
		}

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

		// ahora line apunta a el "" antes del body y el "" guardado
		if (user.getRotate() && encoding.toLowerCase().equals("base64")) {
			// Transformar imagen codificada
			while (!isEndLine(line = reader.readLine(), boundary)) {
				text += line;
			}
			text += line;
			if (!text.isEmpty()) {
				byte[] image = decodeBase64(text);
				image = imageTransformer.rotateImage(image, extension);
				text = encodeBase64(image);
				writeLines(text);
			}
			return line;
		} else {
			return putLines(boundary);
		}
	}

	private String putOtherContext(String contentTypeHeader, String boundary)
			throws IOException {

		String line;
		String extension;
		int last = contentTypeHeader.indexOf(';');
		if (last != -1) {
			extension = contentTypeHeader.substring(
					contentTypeHeader.indexOf('/') + 1, last);
		} else {
			extension = contentTypeHeader.substring(contentTypeHeader
					.indexOf('/') + 1);
		}
		mail.addStructure(extension);

		while ((line = reader.readLine()).length() != 0) {
			writeLine(line);
		}
		writeLine("");

		// ahora line apunta a el "" antes del body y el "" ya esta guardado en
		// el file
		return putLines(boundary);
	}

	// Escribo linea por linea sin transformar nada
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

	private byte[] decodeBase64(String text) {
		byte[] decodedText = Base64.decodeBase64(text);
		return decodedText;
	}

	private String encodeBase64(byte[] encodedText) {
		String text = Base64.encodeBase64String(encodedText);
		return text;
	}

	private String decodeQuotedPrintable(String quotedPrintable) {
		try {
			quotedPrintable = quotedPrintable.replaceAll("=\n", "-\n");
			QuotedPrintableCodec codec = new QuotedPrintableCodec("ISO-8859-1");
			return codec.decode(quotedPrintable);
		} catch (Exception e) {
			return null;
		}
	}

	private String encodeQuotedPrintable(String text) {
		try {
			QuotedPrintableCodec codec = new QuotedPrintableCodec("ISO-8859-1");
			String ans = codec.encode(text);
			ans = ans.replaceAll("-=0A", "=\n");
			ans = ans.replaceAll("=0A", "\n");
			return ans;
		} catch (Exception e) {
			return null;
		}
	}

	private int getMonth(String string) {
		if (string.equals("Jan")) {
			return 1;
		}
		if (string.equals("Feb")) {
			return 2;
		}
		if (string.equals("Mar")) {
			return 3;
		}
		if (string.equals("Apr")) {
			return 4;
		}
		if (string.equals("May")) {
			return 5;
		}
		if (string.equals("Jun")) {
			return 6;
		}
		if (string.equals("Jul")) {
			return 7;
		}
		if (string.equals("Aug")) {
			return 8;
		}
		if (string.equals("Sep")) {
			return 9;
		}
		if (string.equals("Oct")) {
			return 10;
		}
		if (string.equals("Nov")) {
			return 11;
		}
		if (string.equals("Dec")) {
			return 12;
		}
		return -1;
	}

	public Mail getMail() {
		return mail;
	}

}
