package ar.elements;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;



public class ImageTransformer {

	public byte[] rotateImage(byte[] imageByte, String extension)
			throws IOException {

		byte[] transformedImageBytes;
		ByteArrayInputStream input = new ByteArrayInputStream(imageByte);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		BufferedImage image = null;
		image = ImageIO.read(input);
		if (image == null) {
			throw new RuntimeException();
		}

		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage imageTransformed = new BufferedImage(w, h, image
				.getColorModel().getTransparency());
		Graphics2D g = imageTransformed.createGraphics();
		g.rotate(Math.toRadians(180), w/2.0, h/2.0);
		g.drawImage(image, 0, 0, null);
		g.dispose();

		ImageIO.write(imageTransformed, extension, output);

		transformedImageBytes = output.toByteArray();

		return transformedImageBytes;
	}

}