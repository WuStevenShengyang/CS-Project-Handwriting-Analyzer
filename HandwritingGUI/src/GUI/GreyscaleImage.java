package GUI;

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
    private int bgRGB;
    private int textRGB;
    
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
        bgRGB = Color.black.getRGB();
        textRGB = Color.white.getRGB();
        
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Color c = new Color(image.getRGB(x, y));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                
                int sum = red + green + blue;
                Color grey = new Color(sum, sum, sum);
                image.setRGB(x, y, grey.getRGB());
                if(grey.getRGB() > bgRGB) {
                    bgRGB = grey.getRGB();
                }
                if(grey.getRGB() < textRGB) {
                    textRGB = grey.getRGB();
                }
            }
        }
    }
    
    public GreyscaleImage trim() {
        BufferedImage trim = image;
        //image.getSubimage(x, y, w, h)
        
        return new GreyscaleImage(trim);
    }
    
    public GreyscaleImage highContrast() {
        BufferedImage hc = image;
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if(hc.getRGB(x, y) > (textRGB - bgRGB) / 2 + 300000)
                    hc.setRGB(x, y, -1);
            }
        }
        return new GreyscaleImage(hc);
    }
    
    public GreyscaleImage scale() {
        BufferedImage scaled = image;
        if(width > 1275) {
            scaled = toBufferedImage(scaled.getScaledInstance(1275, Math.round((float) (height * 1275.0 / width)), BufferedImage.SCALE_DEFAULT));
        }
        if(height > 1650) {
            scaled = toBufferedImage(scaled.getScaledInstance(Math.round((float) (width * 1650.0 / height)), 1650, BufferedImage.SCALE_DEFAULT));
        }
        
        return new GreyscaleImage(scaled);
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
}
