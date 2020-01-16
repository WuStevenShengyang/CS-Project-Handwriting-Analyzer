package handwritingToText;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GreyscaleImage {
	private BufferedImage image;
	private int width;
	private int height;
	
	public GreyscaleImage(String imagePath) {
		try {
			File input = new File(imagePath);
			image = ImageIO.read(input);
			initialize();
		} catch(IOException e) {}
	}
	
	public GreyscaleImage(BufferedImage image) {
		this.image = image;
		initialize();
	}
	
	public void initialize() {
		width = image.getWidth();
		height = image.getHeight();
		if(width > 1275) {
			height = Math.round((float) (height * 1275.0 / width));
			width = 1275;
			image = toBufferedImage(image.getScaledInstance(1275, height, BufferedImage.SCALE_DEFAULT));
		}
		if(height > 1650) {
			width = Math.round((float) (width * 1650.0 / height));
			height = 1650;
			image = toBufferedImage(image.getScaledInstance(width, 1650, BufferedImage.SCALE_DEFAULT));
		}
		
		int[] histData = new int[256];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				Color c = new Color(image.getRGB(x, y));
				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);
				
				int sum = red + green + blue;
				Color grey = new Color(sum, sum, sum);
				histData[grey.getRGB() & 0xff]++;
				
				image.setRGB(x, y, grey.getRGB());
			}
		}
		
		int totalPx = width * height;
		
		float sum = 0;
		for(int i = 0; i < 256; i++) {
			sum += i * histData[i];
		}
		
		float sumB = 0;
		int wB = 0;
		int wF = 0;
		
		float varMax = 0;
		int threshold = 0;
		
		for(int i = 0; i < 256; i++) {
			wB += histData[i];
			if(wB ==0) continue;
			
			wF = totalPx - wB;
			if(wF == 0) break;
			
			sumB += (float) (i * histData[i]);
			
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;
			
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
			
			if(varBetween > varMax) {
				varMax = varBetween;
				threshold = i;
			}
		}
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if((image.getRGB(x, y) & 0xff) > threshold) {
					image.setRGB(x, y, Color.white.getRGB());
				}
				if((image.getRGB(x, y) & 0xff) < threshold) {
					image.setRGB(x, y, Color.black.getRGB());
				}
			}
		}
		
		System.out.println("threshold: " + threshold);
	}
	
	public BufferedImage toBufferedImage(Image image) {
	    BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(image, 0, 0, null);
	    bGr.dispose();
	    
	    return bimage;
	}
	
	public void show() {
		JFrame frame = new JFrame();
		ImageIcon icon = new ImageIcon(image);
		JLabel label = new JLabel(icon);
		frame.add(label);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public BufferedImage getBufferedImage() {
		return image;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

}
