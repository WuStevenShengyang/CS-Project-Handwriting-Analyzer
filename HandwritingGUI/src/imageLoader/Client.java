package imageLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import GUI.GreyscaleImage;
import imageLoader.BlobFinder.Word;

public class Client {


        public static void main(String[] args) throws IOException {
            String path = "C:\\Users\\wsywu\\OneDrive\\Desktop\\OCRImages\\handwritten3.jpg";
            String fileName = "hello.PNG";
            GreyscaleImage image = new GreyscaleImage(path);
            
            //Downloads image to your eclipse-workspace
            /*File output = new File("greyscale_" + fileName);
            ImageIO.write(image.getBufferedImage(), "png", output);*/
            
            BlobFinder finder = new BlobFinder(image);
            ArrayList<ArrayList<Word>> wordList = new ArrayList<ArrayList<Word>>();
            finder.findWords(wordList);
            System.out.println("Found words:");
            for(ArrayList<Word> line : wordList) {
                for(Word w : line) {
                    System.out.print(w);
                }
                System.out.println();
            }
        }
}
