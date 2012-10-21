package ar.elements;

public class Content {

	public enum Type {
		TEXT, IMAGE, OTHER
	}
	private Type type;
	private String contentTypeHeader;

	public Content(String contentTypeHeader) {
		this.contentTypeHeader = contentTypeHeader;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setContentTypeHeader(String contentTypeHeader) {
		this.contentTypeHeader = contentTypeHeader;
	}
	
	public String getContentTypeHeader() {
		return contentTypeHeader;
	}

	
}
