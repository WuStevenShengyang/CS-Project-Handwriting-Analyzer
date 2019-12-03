package handwritingToText;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Client {

	public static void main(String[] args) throws IOException {
		GreyscaleImage image = new GreyscaleImage("C:\\Users\\s-zouci\\OneDrive - Bellevue School District\\Saved Pictures\\BookGang.jpg");
		image = image.scale().highContrast();
		image.show();
		
		//Downloads image to your eclipse-workspace
		File output = new File("GreyscaleImageTest3.png");
		ImageIO.write(image.getBufferedImage(), "png", output);
		
	}

}
