package ar.elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MessageParser {

	private BufferedReader reader;
	private PrintWriter writer;
	private User user;

	// private TextTransformer textTransformer;
	// private ImageTransformer imageTransformer;
	
	public MessageParser(BufferedReader reader, PrintWriter writer, User user) {
		this.reader = reader;
		this.writer = writer;
		this.user = user;
		//this.textTransformer = new TextTransformer();
		//this.imageTransformer = new ImageTransformer();
	}

	public MessageParser(BufferedReader reader, PrintWriter writer) {
		this.reader = reader;
		this.writer = writer;
		this.user = null;
		//this.textTransformer = new TextTransformer();
		//this.imageTransformer = new ImageTransformer();
	}

	public Message parseMessage() throws IOException {
		Message message = new Message();
		String response = reader.readLine();
		parseHeaders(response, message);
		parseBody(message);
		return message;
	}

	private void parseHeaders(String response, Message message)
			throws IOException {
		String headerName = null;
		StringBuilder headerValue = new StringBuilder();
		do {
			send(response);
			// For each header, first get the name
			// and then get the content that might be
			// in different lines and append them in
			// a string
			if (headerName == null) {
				int separator = response.indexOf(":");
				if (separator == -1)
					return;
				headerName = response.substring(0, separator);
				if (response.length() > separator)
					//antes de apendear sacar espacios
					headerValue.append(response.substring(separator + 2));
			} else
				headerValue.append(response);
		} while ((response = reader.readLine()).length() != 0
				&& (response.startsWith(" ") || response.startsWith("\t")));

		message.addHeaderValue(headerName, headerValue.toString());

		// Body's start
		if (response.length() == 0) {
			send("");
			return;
		} else
			parseHeaders(response, message);
	}

	private void parseBody(Message message) throws IOException {
		String response, contentTypeHeader, boundary = "";
		if (message.getHeaders().get("Content-Type") == null)
			contentTypeHeader = "Content-Type: text/plain";
		else
			contentTypeHeader = "Content-Type: "
					+ message.getHeaders().get("Content-Type").get(0);

		if (contentTypeHeader.toUpperCase().contains("MULTIPART"))
			boundary = getBoundary(contentTypeHeader);

		if (boundary.isEmpty())
			// Single content
			putContent(message, contentTypeHeader, boundary);
		else {
			// Multipart content
			do
				parseContents(message, boundary);
			while (!(response = reader.readLine()).equals("."));
		}
		send(".");
	}

	private void send(String line) {
		writer.println(line);
		User.addGlobalTransferedBytes((long) line.length());
		user.addTransferedBytes((long) line.length());
	}
	
	private void parseContents(Message message, String boundary)
			throws IOException {
		String response = reader.readLine();

		if (response.contains("--" + boundary))
			response = reader.readLine();

		if (response.contains("Content-Type:")) {
			if (response.toUpperCase().contains("MULTIPART")) {
				send("--" + boundary);
				send(response);
				String subBoundary = getBoundary(response);
				send("");
				response = reader.readLine();
				parseContents(message, subBoundary);
			} else {
				response = putContent(message, response, boundary);
				if (response.equals("--" + boundary + "--")) {
					send(response);
					return;
				}
				parseContents(message, boundary);
			}
		}
	}
	
	private String getBoundary(String line) throws IOException {
		if (line.indexOf("=") == -1) {
			line = reader.readLine();
			send(line);
		}
		String boundary = line.substring(line.indexOf("=") + 1);
		String[] tmp = boundary.split("\"");
		if (tmp.length >= 2)
			boundary = tmp[1];
		return boundary;
	}

	
	private String putContent(Message message, String header, String boundary)
			throws IOException {
		Content content;
		String response, contentTypeHeader = header.substring(header
				.indexOf(':') + 2);
		String type = contentTypeHeader.substring(0,
				contentTypeHeader.indexOf('/'));
		String encoding = null;
		if (type.toUpperCase().equals("TEXT"))
			content = new TextContent(contentTypeHeader);
		else if (type.toUpperCase().equals("IMAGE"))
			content = new ImageContent(contentTypeHeader);
		else
			content = new OtherContent(contentTypeHeader);

//		id++;
//		content.setId(id);

		if (!boundary.isEmpty()) {
			send("--" + boundary);
			// Read content's headers if the message use multipart
			// because if the message is a simple content, it has the
			// content's headers in the message's headers
			response = header;
			do {
				send(response);
				if (response.contains("Content-Transfer-Encoding:"))
					encoding = response.substring(response.indexOf(":") + 2);
			} while ((response = reader.readLine()).length() != 0);
			send(response);
		} else {
			if (message.getHeaders().get("Content-Transfer-Encoding") != null)
				encoding = message.getHeaders()
						.get("Content-Transfer-Encoding").get(0);
		}

		boolean needToTransformImage = needToTransformImage(type);
		boolean needToTransformText = needToTransformText(type);
		// Put content's data in contentText
		StringBuilder contentText = new StringBuilder();
		if (!boundary.isEmpty())
			response = putContentText(contentText, boundary, type,
					contentTypeHeader, encoding);
		else
			response = putContentText(contentText, "", type, contentTypeHeader,
					encoding);

		if (needToTransformImage && type.toUpperCase().equals("IMAGE")) {
			// transform and print image
			if (encoding != null && encoding.equals("base64")) {
				String imageFormat = contentTypeHeader.substring(
						contentTypeHeader.indexOf('/') + 1,
						contentTypeHeader.indexOf(';'));
				String transformedImage = imageTransformer.transform(
						contentText.toString(), imageFormat);
				printLines(transformedImage);
			}
		} else if (needToTransformText && encoding != null
				&& contentTypeHeader.toUpperCase().contains("TEXT/PLAIN")) {
			if (encoding.equals("quoted-printable")) {
				// transform and print text according to its encoding
				String text = decodeQuotedPrintable(contentText.toString());
				text = textTransformer.transform(text, null);
				printLines(encodeQuotedPrintable(text));
			} else if (encoding.equals("8bit")) {
				printLines(textTransformer.transform(contentText.toString(),
						message));
			}

		}

		// Add content to message
		message.addContent(content);
		return response;
	}

	private String putContentText(StringBuilder contentText, String boundary,
			String type, String contentTypeHeader, String encoding)
			throws IOException {
		boolean needToTransformImage = needToTransformImage(type);
		boolean needToTransformText = needToTransformText(type);
		String response;
		if (needToTransformImage || (needToTransformText && encoding != null)) {
			while (!checkLine(response = reader.readLine(), boundary)) {
				// need to get the entire image to rotate it
				contentText.append(response + "\n");
			}
		} else if (needToTransformText
				&& contentTypeHeader.toUpperCase().contains("TEXT/PLAIN")) {
			while (!checkLine(response = reader.readLine(), boundary)) {
				// lines can be transformed one by one
				String transformedLine = textTransformer.transform(response,
						null);
				send(transformedLine);
			}
		} else {
			while (!checkLine(response = reader.readLine(), boundary)) {
				send(response);
			}
		}
		return response;
	}

	private boolean checkLine(String line, String boundary) {
		return (boundary == "") ? line.equals(".") : line.contains("--"
				+ boundary);
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

	private void printLines(String message) {
		StringBuilder builder = new StringBuilder();
		int i = 0, count = 0;
		// builder.append(message.charAt(0));
		while (i < message.length()) {
			if (message.charAt(i) != '\n') {
				builder.append(message.charAt(i));
			}

			if (count == 76 || message.charAt(i) == '\n') {
				count = 0;
				// builder.append('\n');
				// System.out.println(builder.toString());
				send(builder.toString());
				builder = new StringBuilder();
			} else {
				count++;
			}
			i++;
		}
	}

	private boolean needToTransformImage(String type) {
		if (user != null) {
			if (type.toUpperCase().equals("IMAGE")) {
				if (user.getRotate() != null
						&& user.getRotate()) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean needToTransformText(String type) {
		if (user != null) {
			if (type.toUpperCase().equals("TEXT")) {
				if (user.getLeet() != null
						&& user.getLeet()) {
					return true;
				}
			}
		}
		return false;
	}
}
