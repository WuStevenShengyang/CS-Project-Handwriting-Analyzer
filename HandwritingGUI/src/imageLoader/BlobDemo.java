package imageLoader;

//Blob Finder Demo
//A.Greensted
//http://www.labbookpages.co.uk
//Please use however you like. I'd be happy to hear any feedback or comments.

import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import imageLoader.BlobFinder.Blob;

public class BlobDemo {
    // Gets a blob with the dimensions of the whole line
    ArrayList<BlobFinder.Blob> wordList = new ArrayList<BlobFinder.Blob>();
    public BlobFinder.Blob getLineBlob(ArrayList<BlobFinder.Blob> line) {
        int xMin = line.get(0).xMin;
        int xMax = line.get(0).xMax;
        int yMin = line.get(0).yMin;
        int yMax = line.get(0).yMax;
        int mass = line.get(0).mass;
        for (int i = 1; i < line.size(); i++) {
            BlobFinder.Blob word = line.get(i);
            xMin = Math.min(xMin, word.xMin);
            xMax = Math.max(xMax, word.xMax);
            yMin = Math.min(yMin, word.yMin);
            yMax = Math.max(yMax, word.yMax);
            mass += word.mass;
        }

        return new BlobFinder.Blob(xMin, xMax, yMin, yMax, mass);
    }

    // Gets words in a line
    public ArrayList<BlobFinder.Blob> getWordList(ArrayList<BlobFinder.Blob> line) {
        // Sort line
        for (int i = 0; i < line.size() - 1; i++) {
            int minIndex = i;
            for (int j = i; j < line.size(); j++) {
                if (line.get(j).xMin < line.get(minIndex).xMin) {
                    minIndex = j;
                } else if (line.get(j).xMin == line.get(minIndex).xMin && line.get(j).xMax < line.get(minIndex).xMax) {
                    minIndex = j;
                }
            }
            BlobFinder.Blob temp = line.get(i);
            line.set(i, line.get(minIndex));
            line.set(minIndex, temp);
        }

        // Condense close blobs into words by adding to a word until exceeds tolerance
        int xMin = line.get(0).xMin;
        int xMax = line.get(0).xMax;
        int yMin = line.get(0).yMin;
        int yMax = line.get(0).yMax;
        int mass = line.get(0).mass;
        int tolerance = 25;
        for (int i = 1; i < line.size(); i++) {
            BlobFinder.Blob blob = line.get(i);
            if (blob.xMin - tolerance <= xMax) {
                xMax = blob.xMax;
                if (blob.yMin < yMin) {
                    yMin = blob.yMin;
                }
                if (blob.yMax > yMax) {
                    yMax = blob.yMax;
                }
                mass += blob.mass;
            } else {
                wordList.add(new BlobFinder.Blob(xMin, xMax, yMin, yMax, mass));
                xMin = blob.xMin;
                xMax = blob.xMax;
                yMin = blob.yMin;
                yMax = blob.yMax;
                mass = blob.mass;
            }
        }
        wordList.add(new BlobFinder.Blob(xMin, xMax, yMin, yMax, mass));

        return wordList;
    }

    public void showWords(ArrayList<BlobFinder.Blob> wordList, BufferedImage srcImage) {
        Graphics2D g = srcImage.createGraphics();
        g.setColor(Color.BLACK);
        for (int i = 0; i < wordList.size(); i++) {
            int xMin = wordList.get(i).xMin;
            int xMax = wordList.get(i).xMax;
            int yMin = wordList.get(i).yMin;
            int yMax = wordList.get(i).yMax;
            g.drawRect(xMin, yMin, xMax - xMin, yMax - yMin);
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

    public BlobDemo(String filename) {
        // Load Source image
        BufferedImage srcImage = null;
        BufferedImage rawImage = null;

        try {
            File imgFile = new File(filename);
            rawImage = javax.imageio.ImageIO.read(imgFile);
        } catch (IOException ioE) {
            System.err.println(ioE);
            System.exit(1);
        }

        int width = rawImage.getWidth();
        int height = rawImage.getHeight();
        srcImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = srcImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.drawImage(rawImage, 0, 0, null);
        g2d.dispose();

        // Get raw image data
        Raster raster = srcImage.getData();
        DataBuffer buffer = raster.getDataBuffer();

        int type = buffer.getDataType();
        if (type != DataBuffer.TYPE_BYTE) {
            System.err.println("Wrong image data type");
            System.exit(1);
        }
        if (buffer.getNumBanks() != 1) {
            System.err.println("Wrong image data format");
            System.exit(1);
        }

        DataBufferByte byteBuffer = (DataBufferByte) buffer;
        byte[] srcData = byteBuffer.getData(0);

        // Sanity check image
        if (width * height * 3 != srcData.length) {
            System.err.println("Unexpected image data size. Should be RGB image");
            System.exit(1);
        }

        // Output Image info
        System.out.printf("Loaded image: '%s', width: %d, height: %d, num bytes: %d\n", filename, width, height,
                srcData.length);

        // Create Monochrome version - using basic threshold technique
        byte[] monoData = new byte[width * height];
        int srcPtr = 0;
        int monoPtr = 0;

        while (srcPtr < srcData.length) {
            int val = ((srcData[srcPtr] & 0xFF) + (srcData[srcPtr + 1] & 0xFF) + (srcData[srcPtr + 2] & 0xFF)) / 3;
            monoData[monoPtr] = (val > 128) ? (byte) 0xFF : 0;

            srcPtr += 3;
            monoPtr += 1;
        }

        byte[] dstData = new byte[srcData.length];

        // Create Blob Finder
        BlobFinder finder = new BlobFinder(width, height);

        ArrayList<BlobFinder.Blob> blobList = new ArrayList<BlobFinder.Blob>();
        finder.detectBlobs(monoData, dstData, 0, -1, (byte) 0, blobList);

        // List Blobs
        System.out.printf("Found %d blobs:\n", blobList.size());
        for (BlobFinder.Blob blob : blobList)
            System.out.println(blob);

        // Sort blobs by yMin
        for (int i = 0; i < blobList.size() - 1; i++) {
            int minIndex = i;
            for (int j = i; j < blobList.size(); j++) {
                if (blobList.get(j).yMin <= blobList.get(minIndex).yMin) {
                    minIndex = j;
                }
            }
            BlobFinder.Blob temp = blobList.get(i);
            blobList.set(i, blobList.get(minIndex));
            blobList.set(minIndex, temp);
        }

        // Separate blobs into lines
        ArrayList<ArrayList<BlobFinder.Blob>> lines = new ArrayList<ArrayList<BlobFinder.Blob>>();
        int yMax = blobList.get(0).yMax;
        ArrayList<BlobFinder.Blob> line = new ArrayList<BlobFinder.Blob>();
        line.add(blobList.get(0));
        for (int i = 1; i < blobList.size(); i++) {
            BlobFinder.Blob blob = blobList.get(i);
            if (yMax - blob.yMin > 0) {
                line.add(blob);
                if (blob.yMax > yMax) {
                    yMax = blob.yMax;
                }
            } else {
                lines.add(line);
                yMax = blob.yMax;
                line = new ArrayList<BlobFinder.Blob>();
                line.add(blob);
            }
        }
        lines.add(line);

        // Compile lines of words into one ArrayList
        ArrayList<BlobFinder.Blob> wordList = new ArrayList<BlobFinder.Blob>();
        for (int i = 0; i < lines.size(); i++) {
            wordList.addAll(getWordList(lines.get(i)));
            // wordList.add(getLineBlob(lines.get(i)));
        }

        // Print words
        /*
         * System.out.println(wordList.size() + " WORDS"); for (BlobFinder.Blob word :
         * wordList) System.out.println(word);
         */

        //showWords(wordList, srcImage);

        // Create GUI
        /*
         * RGBFrame srcFrame = new RGBFrame(width, height, srcData); RGBFrame dstFrame =
         * new RGBFrame(width, height, dstData); JPanel panel = new JPanel(new
         * BorderLayout(5, 5)); panel.setBorder(new javax.swing.border.EmptyBorder(5, 5,
         * 5, 5)); panel.add(srcFrame, BorderLayout.WEST); panel.add(dstFrame,
         * BorderLayout.EAST); JFrame frame = new JFrame("Blob Detection Demo");
         * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         * frame.getContentPane().add(panel); frame.pack(); frame.setVisible(true);
         */
    }
    public ArrayList<BlobFinder.Blob> getList(){
        return wordList;
    }
    public static void main(String[] args) {
        // new
        // BlobDemo("C:\\Users\\wsywu\\OneDrive\\Desktop\\OCRImages\\handwritten2.jpg");
        /*
         * if (args.length<1) { System.err.println("Provide image filename");
         * System.exit(1); }
         */

    }
}