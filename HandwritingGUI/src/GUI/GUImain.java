package GUI;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.filechooser.*;

import imageLoader.BlobDemo;
import imageLoader.BlobFinder;
import imageLoader.BlobFinder.Blob;

import java.io.BufferedReader;
import java.io.File;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Canvas;
import java.awt.Label;
import java.awt.Scrollbar;
import javax.swing.border.LineBorder;

public class GUImain {

    public static class FilePaths {
        // File Paths
        public static String fnOCRdetector = "C:\\Users\\wsywu\\git\\CS-Project-Handwriting-Analyzer-\\OCR_Model\\src\\main.py";
        public static String fnResultText = "C:\\Users\\wsywu\\git\\CS-Project-Handwriting-Analyzer-\\OCR_Model\\runtimeData\\recognized.txt";
        public static String fnBlobLoc = "C:\\Users\\wsywu\\git\\CS-Project-Handwriting-Analyzer-\\OCR_Model\\runtimeData\\Blobs\\";
    }

    private JFrame frmHn;
    private JLabel label;
    private JLabel labelS;
    private GreyscaleImage imageG;
    private BufferedImage image;
    private int maxHeight = 450;
    private int maxWidth = 550;

    private String text;
    private String path;
    private ArrayList<Box> bounds;
    private double zoom = 1.0;
    private int xOff = 720;
    private int yOff = 100;
    private int xOffS = 80;
    private int yOffS = 150;
    private ArrayList<BlobFinder.Blob> blobList;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUImain window = new GUImain();
                    window.frmHn.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Create the application.
     */
    public GUImain() {
        try {
            initialize();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() throws Exception {
        label = new JLabel();
        labelS = new JLabel();
        bounds = new ArrayList<>();

        frmHn = new JFrame();
        frmHn.getContentPane().setBackground(Color.LIGHT_GRAY);
        frmHn.setBackground(Color.LIGHT_GRAY);
        frmHn.setTitle("Handwriting Recognition");
        frmHn.setBounds(100, 100, 1400, 900);
        frmHn.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmHn.getContentPane().setLayout(null);
        frmHn.setVisible(true);
        frmHn.setResizable(false);

        // Image Label
        JLabel lblImage = new JLabel("Grayscaled Image");
        lblImage.setFont(new Font("Yu Gothic", Font.BOLD | Font.ITALIC, 19));
        lblImage.setBounds(708, 58, 235, 21);
        frmHn.getContentPane().add(lblImage);
        lblImage.setVisible(true);

        // Text Label
        JLabel lblText = new JLabel("Text");
        lblText.setFont(new Font("Yu Gothic", Font.BOLD | Font.ITALIC, 19));
        lblText.setBounds(722, 499, 56, 16);
        frmHn.getContentPane().add(lblText);
        lblText.setVisible(true);

        // Browse image button
        JButton btnBrowse = new JButton("Browse");
        btnBrowse.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 26));
        btnBrowse.setBounds(12, 28, 164, 45);
        frmHn.getContentPane().add(btnBrowse);

        // Text area
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.BOLD, 19));
        textArea.setEditable(false);
        textArea.setBounds(710, 526, 662, 318);
        frmHn.getContentPane().add(textArea);

        // Notification button
        JLabel lblimageMustBe = new JLabel("(.jpg or .png)");
        lblimageMustBe.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblimageMustBe.setBounds(183, 41, 384, 24);
        frmHn.getContentPane().add(lblimageMustBe);

        // Warning button
        JLabel lblWarning = new JLabel("Incompatible format!");
        lblWarning.setForeground(Color.RED);
        lblWarning.setFont(new Font("Sylfaen", Font.BOLD, 32));
        lblWarning.setBounds(145, 193, 422, 217);
        frmHn.getContentPane().add(lblWarning);

        // Sample Image Label
        JLabel lblImageS = new JLabel("Original Image");
        lblImageS.setBounds(12, 96, 210, 31);
        frmHn.getContentPane().add(lblImageS);
        lblImageS.setFont(new Font("Yu Gothic", Font.BOLD | Font.ITALIC, 19));
        lblImageS.setVisible(true);

        lblWarning.setVisible(false);

        btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Reset
                for (int i = 0; i < bounds.size(); i++) {
                    frmHn.getContentPane().remove(bounds.get(i));
                }
                zoom = 1.0;
                bounds = new ArrayList<>();

                boolean isCompatible = true;

                // Open desktop directory and let the user choose an image
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                int r = fileChooser.showOpenDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {
                    frmHn.getContentPane().remove(label);

                    path = fileChooser.getSelectedFile().getAbsolutePath();

                    // Gray Scaled Image
                    try {
                        imageG = new GreyscaleImage(path);
                        lblWarning.setVisible(false);
                    } catch (Exception ex) {
                        lblWarning.setVisible(true);
                        isCompatible = false;
                    }

                    if (isCompatible) {
                        File input = new File(path);
                        image = null;
                        // Normal Image

                        try {
                            image = ImageIO.read(input);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        int[] resized = resizeImage();

                        try {
                            getBound();
                        } catch (Exception e2) {
                            // TODO Auto-generated catch block
                            e2.printStackTrace();
                        }

                        try {
                            saveSplitedImage();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        // OCR
                        try {
                            convertToText();
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        textArea.setText(text);
                        for (int i = 0; i < bounds.size(); i++) {
                            frmHn.getContentPane().add(bounds.get(i));
                        }
                        // Display image
                        label.setBounds(xOff, yOff, resized[0], resized[1]);
                        Image dimage = imageG.getBufferedImage().getScaledInstance(label.getWidth(), label.getHeight(),
                                Image.SCALE_SMOOTH);
                        ImageIcon icon = new ImageIcon(dimage);
                        label.setIcon(icon);
                        frmHn.getContentPane().add(label);

                        // Display Sample Image
                        labelS.setBounds(xOffS, yOffS, resized[0], resized[1]);
                        Image imageS = image.getScaledInstance(labelS.getWidth(), labelS.getHeight(),
                                Image.SCALE_SMOOTH);
                        ImageIcon iconS = new ImageIcon(imageS);
                        labelS.setIcon(iconS);
                        frmHn.getContentPane().add(labelS);

                        frmHn.revalidate();
                        frmHn.repaint();
                    }

                } else {
                    path = "Operation Canceled";
                }

            }

        });

    }

    // Resize image (maintain the aspect ratio)
    private int[] resizeImage() {
        int height = image.getHeight();
        int width = image.getWidth();
        int[] result = new int[2];

        while (height > maxHeight || width > maxWidth) {
            height = (int) (height * 1.0 / 1.1);
            width = (int) (width * 1.0 / 1.1);
            zoom *= 1.1;
        }

        result[0] = width;
        result[1] = height;

        return result;

    }

    // Convert image to text
    private void convertToText() throws Exception {
        text = "";
        String command = "python " + FilePaths.fnOCRdetector;

        // Execute OCR and get outputs
        String s;
        Process process = Runtime.getRuntime().exec(command);

        // Print Error Stream
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

        // Read recognized text from result folder
        File result = new File(FilePaths.fnResultText);
        Scanner scanner = new Scanner(result);
        for (int i = 0; i < blobList.size(); i++) {
            text += scanner.next() + " ";
            if (i < blobList.size() - 1) {
                System.out.println(1);
                if (blobList.get(i).xMax > blobList.get(i + 1).xMin && blobList.get(i).yMax < blobList.get(i).yMin) {
                    text += "\n";

                }
            }

        }
        scanner.close();

    }

    // Get bounding box of each word
    private void getBound() throws Exception {
        BlobDemo blobFinder = new BlobDemo(path);
        blobList = blobFinder.getList();
        for (int i = 0; i < blobList.size(); i++) {
            int x = (int) (blobList.get(i).xMin * 1.0 / zoom) + xOff;
            int y = (int) (blobList.get(i).yMin * 1.0 / zoom) + yOff;
            int xMax = (int) (blobList.get(i).xMax * 1.0 / zoom) + xOff;
            int yMax = (int) (blobList.get(i).yMax * 1.0 / zoom) + yOff;
            int width = xMax - x;
            int height = yMax - y;

            Box boundBox = Box.createHorizontalBox();
            boundBox.setBorder(new LineBorder(Color.RED, 3));
            boundBox.setBounds(x, y, width, height);
            bounds.add(boundBox);

        }
    }

    private void saveSplitedImage() throws IOException {
        for (int i = 0; i < blobList.size(); i++) {
            int x = (int) (blobList.get(i).xMin);
            int y = (int) (blobList.get(i).yMin);
            int xMax = (int) (blobList.get(i).xMax);
            int yMax = (int) (blobList.get(i).yMax);
            int width = xMax - x;
            int height = yMax - y;

            try {
                BufferedImage corp = imageG.getBufferedImage().getSubimage(x, y, width, height);
                File outputfile = new File(FilePaths.fnBlobLoc + i);
                ImageIO.write(corp, "png", outputfile);
            } catch (Exception e) {

            }

        }
    }

}
