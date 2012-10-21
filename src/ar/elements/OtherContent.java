package ar.elements;

public class OtherContent extends Content{

    private String content;
    
    public OtherContent(String contentTypeHeader) {
            super(contentTypeHeader);
            this.setType(Type.OTHER);
    }

    public String getContent() {
            return content;
    }

    public void setContent(String content) {
            this.content = content;
    }
	
}
