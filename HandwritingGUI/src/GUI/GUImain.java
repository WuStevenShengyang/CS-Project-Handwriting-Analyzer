package GUI;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.filechooser.*;
import java.io.File;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Image;
import java.awt.Color;

public class GUImain {

    private JFrame frmHn;
    private JLabel label;
    private GreyscaleImage imageG;
    private BufferedImage image;
    private int maxHeight = 400;
    private int maxWidth = 550;
    private String text;

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
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        label = new JLabel();

        frmHn = new JFrame();
        frmHn.getContentPane().setBackground(Color.LIGHT_GRAY);
        frmHn.setBackground(Color.LIGHT_GRAY);
        frmHn.setTitle("Handwriting Recognition");
        frmHn.setAlwaysOnTop(true);
        frmHn.setBounds(100, 100, 700, 900);
        frmHn.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmHn.getContentPane().setLayout(null);
        frmHn.setVisible(true);
        frmHn.setResizable(false);

        // Image Label
        JLabel lblImage = new JLabel("Image");
        lblImage.setFont(new Font("Yu Gothic", Font.BOLD | Font.ITALIC, 19));
        lblImage.setBounds(12, 91, 66, 21);
        frmHn.getContentPane().add(lblImage);
        lblImage.setVisible(true);

        // Text Label
        JLabel lblText = new JLabel("Text");
        lblText.setFont(new Font("Yu Gothic", Font.BOLD | Font.ITALIC, 19));
        lblText.setBounds(10, 505, 56, 16);
        frmHn.getContentPane().add(lblText);
        lblText.setVisible(true);

        // Browse image button
        JButton btnBrowse = new JButton("Browse");
        btnBrowse.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 26));
        btnBrowse.setBounds(12, 28, 164, 45);
        frmHn.getContentPane().add(btnBrowse);
        
        // Text area
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.BOLD, 19));
        textArea.setEditable(false);
        textArea.setBounds(12, 532, 662, 318);
        frmHn.getContentPane().add(textArea);
        
        // Notification button
        JLabel lblimageMustBe = new JLabel("(Image must be in either .jpg or .png)");
        lblimageMustBe.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblimageMustBe.setBounds(183, 41, 384, 24);
        frmHn.getContentPane().add(lblimageMustBe);
        
        // Warning button
        JLabel lblWarning = new JLabel("Incompatible format!");
        lblWarning.setForeground(Color.RED);
        lblWarning.setFont(new Font("Sylfaen", Font.BOLD, 32));
        lblWarning.setBounds(183, 184, 422, 217);
        frmHn.getContentPane().add(lblWarning);
        lblWarning.setVisible(false);

        btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                boolean isCompatible = true;
                
                // Open desktop directory and let the user choose an image
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                int r = fileChooser.showOpenDialog(null);
                String path = "";

                if (r == JFileChooser.APPROVE_OPTION) {
                    frmHn.getContentPane().remove(label);
                    try {
                        path = fileChooser.getSelectedFile().getAbsolutePath();
                    } catch (Exception ex) {
                    }

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

                        }

                        int[] resized = resizeImage();

                        convertToText();
                        textArea.setText(text);

                        // Display image
                        label.setBounds(100, 100, resized[0], resized[1]);
                        Image dimage = image.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
                        ImageIcon icon = new ImageIcon(dimage);
                        label.setIcon(icon);
                        frmHn.getContentPane().add(label);
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
        }

        result[0] = width;
        result[1] = height;

        return result;

    }

    // Convert image to text
    private void convertToText() {
        // TODO
        text = "some text";
    }
}
