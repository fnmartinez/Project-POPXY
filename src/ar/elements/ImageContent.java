package ar.elements;

import java.awt.image.BufferedImage;

public class ImageContent extends Content{

    private BufferedImage image;    
    
    public ImageContent(String contentTypeHeader) {
            super(contentTypeHeader);
            this.setType(Type.IMAGE);
    }

    public BufferedImage getImage() {
            return image;
    }

    public void setImage(BufferedImage image) {
            this.image = image;
    }
	
}
