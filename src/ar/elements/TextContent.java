package ar.elements;

public class TextContent extends Content {

	private String text;

	public TextContent(String contentTypeHeader) {
		super(contentTypeHeader);
		this.setType(Type.TEXT);
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
