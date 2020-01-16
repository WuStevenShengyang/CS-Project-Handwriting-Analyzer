package handwritingToText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import handwritingToText.WordFinder.Word;

public class Client {

	public static void main(String[] args) throws IOException {
		String path = "C:\\Users\\s-zouci\\OneDrive - Bellevue School District\\2019-20\\7 - Special Topics in Computer Science\\";
		String fileName = "hello.PNG";
		GreyscaleImage image = new GreyscaleImage(path + fileName);
		
		//Downloads image to your eclipse-workspace
		/*File output = new File("greyscale_" + fileName);
		ImageIO.write(image.getBufferedImage(), "png", output);*/
		
		WordFinder finder = new WordFinder(image);
		ArrayList<ArrayList<Word>> wordList = new ArrayList<ArrayList<Word>>();
		finder.findWords(wordList);
		System.out.println("Found words:");
		for(ArrayList<Word> line : wordList) {
			for(Word w : line) {
				System.out.println(w);
			}
		}
	}

}
