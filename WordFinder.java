package handwritingToText;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class WordFinder {
	private BufferedImage rawImage;
	private BufferedImage srcImage;
	private byte[] srcData;
	
	private int width;
	private int height;
	
	private int[] labelBuffer; // Array of length width * height representing image. Filled with indexes of labelTable
	
	private int[] labelTable; // Blob labeled with index i is connected to blob labeled with labelTable[i]
	private int[] xMinTable;
	private int[] xMaxTable;
	private int[] yMinTable;
	private int[] yMaxTable;
	private int[] massTable;
	
	private ArrayList<Word> blobList;
	
	class Word {
		int xMin;
		int xMax;
		int yMin;
		int yMax;
		int mass;
		
		public Word(int xMin, int xMax, int yMin, int yMax, int mass) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
			this.mass = mass;
		}
		
		public String toString() {
			return String.format("X: %4d -> %4d, Y: %4d -> %4d, mass: %6d", xMin, xMax, yMin, yMax, mass);
		}
	}
	
	public ArrayList<Word> getLineWords(ArrayList<Word> line) {
		ArrayList<Word> lineWords = new ArrayList<Word>();
		
		// Sort line by x and get average blob height
		double avHeight = 0.0;
		for(int i = 0; i < line.size() - 1; i++) {
			int minIndex = i;
			for(int j = i; j < line.size(); j++) {
				if(line.get(j).xMin < line.get(minIndex).xMin) {
					minIndex = j;
				} else if(line.get(j).xMin == line.get(minIndex).xMin
						&& line.get(j).xMax < line.get(minIndex).xMax) {
					minIndex = j;
				}
			}
			avHeight += line.get(minIndex).yMax - line.get(minIndex).yMin;
			Word temp = line.get(i);
			line.set(i, line.get(minIndex));
			line.set(minIndex, temp);
		}
		avHeight = 1.0 * (avHeight + (line.get(line.size() - 1).yMax - line.get(line.size() - 1).yMin)) / line.size();
		
		// Condense close blobs into words by adding to a word until exceeds tolerance
		int xMin = line.get(0).xMin;
		int xMax = line.get(0).xMax;
		int yMin = line.get(0).yMin;
		int yMax = line.get(0).yMax;
		int mass = line.get(0).mass;
		//int tolerance = 13;
		double tolerance = avHeight / 1.8;
		System.out.println("tolerance = " + tolerance);
		for(int i = 1; i < line.size(); i++) {
			Word blob = line.get(i);
			if(blob.xMin - tolerance <= xMax) {
				xMax = blob.xMax;
				if(blob.yMin < yMin) {
					yMin = blob.yMin;
				}
				if(blob.yMax > yMax) {
					yMax = blob.yMax;
				}
				mass += blob.mass;
			} else {
				lineWords.add(new Word(xMin, xMax, yMin, yMax, mass));
				xMin = blob.xMin;
				xMax = blob.xMax;
				yMin = blob.yMin;
				yMax = blob.yMax;
				mass = blob.mass;
			}
		}
		lineWords.add(new Word(xMin, xMax, yMin, yMax, mass));
		
		return lineWords;
	}
	
	public void showWords(ArrayList<ArrayList<Word>> wordList, BufferedImage srcImage) {
		Graphics2D g = srcImage.createGraphics();
		g.setColor(Color.BLACK);
		for(int i = 0; i < wordList.size(); i++) {
			ArrayList<Word> line = wordList.get(i);
			for(int j = 0; j < wordList.get(i).size(); j++) {
				int xMin = line.get(j).xMin;
				int xMax = line.get(j).xMax;
				int yMin = line.get(j).yMin;
				int yMax = line.get(j).yMax;
				g.drawRect(xMin, yMin, xMax - xMin, yMax - yMin);
			}
		}
		g.dispose();
		JFrame jf = new JFrame();
		ImageIcon icon = new ImageIcon(srcImage);
		JLabel label = new JLabel(icon);
		jf.add(label);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}
	
	public WordFinder(GreyscaleImage image) {
		// Convert source image to RGB
		rawImage = image.getBufferedImage();
		width = rawImage.getWidth();
		height = rawImage.getHeight();
		srcImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d = srcImage.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);
		g2d.drawImage(rawImage, 0, 0, null);
		g2d.dispose();
		
		Raster raster = srcImage.getData();
		DataBuffer buffer = raster.getDataBuffer();
		if(buffer.getDataType() != DataBuffer.TYPE_BYTE) {
			System.err.println("Wrong image data type");
			System.exit(1);
		}
		if(buffer.getNumBanks() != 1) {
			System.err.println("Wrong image data format");
			System.exit(1);
		}
		DataBufferByte byteBuffer = (DataBufferByte) buffer;
		srcData = byteBuffer.getData(0);
		if(width * height * 3 != srcData.length) {
			System.err.println("Unexpected image data size. Should be RGB image");
			System.exit(1);
		}
		
		// Create Monochrome version - using basic threshold technique
		byte[] monoData = new byte[width * height];
		int srcInd = 0;
		int monoInd = 0;
		while (srcInd < srcData.length)	{
			int val = ((srcData[srcInd]&0xFF) + (srcData[srcInd + 1]&0xFF) + (srcData[srcInd + 2]&0xFF)) / 3;
			monoData[monoInd] = (val > 128) ? (byte) 0xFF : 0;
			
			srcInd += 3;
			monoInd += 1;
		}
		srcData = monoData;
		
		labelBuffer = new int[width * height];
		
		// Max number of blobs is image with equally spaced single pixel blobs
		int tableSize = width * height / 4;
		labelTable = new int[tableSize];
		xMinTable = new int[tableSize];
		xMaxTable = new int[tableSize];
		yMinTable = new int[tableSize];
		yMaxTable = new int[tableSize];
		massTable = new int[tableSize];
		
		blobList = new ArrayList<Word>();
	}
	
	public void detectBlobs(int minMass, int maxMass) {
		// Only neighbors that have already been labeled will be checked
		// A B C
		// D X
		int srcInd = 0;
		// Indexes of A, B, C, D in labelBuffer
		int a = -width - 1;
		int b = -width;
		int c = -width + 1;
		int d = -1;
		
		int label = 1;
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				labelBuffer[srcInd] = 0;
				
				// Check if foreground pixel
				if(srcData[srcInd] == (byte) 0) {
					// Find label for neighbors, 0 if out of range
					int aLabel = (x > 0 && y > 0) ? labelTable[labelBuffer[a]] : 0;
					int bLabel = (y > 0) ? labelTable[labelBuffer[b]] : 0;
					int cLabel = (x < width - 1 && y > 0) ? labelTable[labelBuffer[c]] : 0;
					int dLabel = (x > 0) ? labelTable[labelBuffer[d]] : 0;
					
					// Look for label with least nonzero value
					int min = Integer.MAX_VALUE;
					if(aLabel != 0 && aLabel < min) min = aLabel;
					if(bLabel != 0 && bLabel < min) min = bLabel;
					if(cLabel != 0 && cLabel < min) min = cLabel;
					if(dLabel != 0 && dLabel < min) min = dLabel;
					
					// If no neighbors labeled, label X with label and add label to labelTable
					if(min == Integer.MAX_VALUE) {
						labelBuffer[srcInd] = label;
						labelTable[label] = label;
						
						// Initialize min/max x, y for label
						xMinTable[label] = x;
						xMaxTable[label] = x;
						yMinTable[label] = y;
						yMaxTable[label] = y;
						massTable[label] = 1;
						
						label++;
					} else { // Neighbor labeled
						// Label pixel with min neighbor label
						labelBuffer[srcInd] = min;
						
						// Update min/max x, y for label
						yMaxTable[min] = y;
						massTable[min]++;
						if(x < xMinTable[min]) xMinTable[min] = x;
						if(x > xMaxTable[min]) xMaxTable[min] = x;
						
						if (aLabel != 0) labelTable[aLabel] = min;
						if (bLabel != 0) labelTable[bLabel] = min;
						if (cLabel != 0) labelTable[cLabel] = min;
						if (dLabel != 0) labelTable[dLabel] = min;
					}
				}
				
				srcInd++; a++; b++; c++; d++;
			}
		}
		
		// Iterate through labels pushing min/max x, y values towards min label
		for(int i = label - 1; i > 0; i--) {
			if(labelTable[i] != i) {
				if (xMaxTable[i] > xMaxTable[labelTable[i]]) xMaxTable[labelTable[i]] = xMaxTable[i];
				if (xMinTable[i] < xMinTable[labelTable[i]]) xMinTable[labelTable[i]] = xMinTable[i];
				if (yMaxTable[i] > yMaxTable[labelTable[i]]) yMaxTable[labelTable[i]] = yMaxTable[i];
				if (yMinTable[i] < yMinTable[labelTable[i]]) yMinTable[labelTable[i]] = yMinTable[i];
				massTable[labelTable[i]] += massTable[i];
				
				// Set labelTable[label] to blob the label is associated with
				int l = i;
				while(l != labelTable[l]) l = labelTable[l];
				labelTable[i] = l;
			} else {
				// Ignore blobs that butt against corners
				/*if(i == labelBuffer[0]) continue; // Top Left
				if(i == labelBuffer[width]) continue; // Top Right
				if(i == labelBuffer[(width*height) - width + 1]) continue;	// Bottom Left
				if(i == labelBuffer[(width*height) - 1]) continue; // Bottom Right*/
				
				if(massTable[i] >= minMass && (massTable[i] <= maxMass || maxMass == -1)) {
					Word blob = new Word(xMinTable[i], xMaxTable[i], yMinTable[i], yMaxTable[i], massTable[i]);
					blobList.add(blob);
				}
			}
		}
	}
	
	public void findWords(ArrayList<ArrayList<Word>> wordList) {
		detectBlobs(0, -1);
		
		// Sort blobs by yMin
		for(int i = 0; i < blobList.size() - 1; i++) {
			int minIndex = i;
			for(int j = i; j < blobList.size(); j++) {
				if(blobList.get(j).yMin <= blobList.get(minIndex).yMin) {
					minIndex = j;
				}
			}
			Word temp = blobList.get(i);
			blobList.set(i, blobList.get(minIndex));
			blobList.set(minIndex, temp);
		}
		
		// Separate blobs into lines
		ArrayList<ArrayList<Word>> lines = new ArrayList<ArrayList<Word>>();
		int yMax = blobList.get(0).yMax;
		ArrayList<Word> line = new ArrayList<Word>();
		line.add(blobList.get(0));
		for(int i = 1; i < blobList.size(); i++) {
			Word blob = blobList.get(i);
			if(yMax - blob.yMin > 0) {
				line.add(blob);
				if(blob.yMax > yMax) {
					yMax = blob.yMax;
				}
			} else {
				lines.add(line);
				yMax = blob.yMax;
				line = new ArrayList<Word>();
				line.add(blob);
			}
		}
		lines.add(line);
		
		// Compile lines of words into one ArrayList
		for(int i = 0; i < lines.size(); i++) {
			wordList.add(getLineWords(lines.get(i)));
			//wordList.add(getLineBlob(lines.get(i)));
		}
		
		// Print words
		/*System.out.println(wordList.size() + " WORDS");
		for (BlobFinder.Blob word : wordList) System.out.println(word);*/
		
		showWords(wordList, srcImage);
	}

}
