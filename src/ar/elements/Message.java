package ar.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Message {

	private final Map<String, List<String>> headerMap;
	private Map<Content.Type, List<Content>> contentMap;
	private String body;

	public Message() {
		this.headerMap = new HashMap<String, List<String>>();
		this.contentMap = new HashMap<Content.Type, List<Content>>();
	}

	public Map<String, List<String>> getHeaders() {
		return headerMap;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void addContentValue(Content content) {
		List<Content> contents = contentMap.get(content.getType());
		if (contents == null) {
			contents = new ArrayList<Content>();
			contentMap.put(content.getType(), contents);
		}
		contents.add(content);
	}

	public void addHeaderValue(String headerName, String headerValue) {
		List<String> headerValues = headerMap.get(headerName);
		if (headerValues == null) {
			headerValues = new ArrayList<String>();
			headerMap.put(headerName, headerValues);
		}
		headerValues.add(headerValue.toString());
	}

	
	//TODO REFACTORRR!!!!
	public String putEnters(String message) {
		StringBuilder builder = new StringBuilder();
		int i, count = 0;
		builder.append(message.charAt(0));
		for (i = 1; !(message.charAt(i - 1) == '\n' && message.charAt(i) == '\n')
				&& i < message.length(); i++)
			builder.append(message.charAt(i));
		while (i < message.length()) {
			if (message.charAt(i) == '\n')
				count = 0;
			else
				count++;

			builder.append(message.charAt(i));

			if (count == 76) {
				count = 0;
				builder.append('\n');
			}
			i++;
		}

		return builder.toString();
	}

}
