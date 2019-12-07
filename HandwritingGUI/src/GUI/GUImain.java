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

public class GUImain {

    private JFrame frame;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUImain window = new GUImain();
                    window.frame.setVisible(true);
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
        JLabel label = new JLabel();
        frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.setBounds(100, 100, 600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setVisible(true);

        // Image Label
        JLabel lblImage = new JLabel("Image");
        lblImage.setFont(new Font("Yu Gothic", Font.BOLD | Font.ITALIC, 19));
        lblImage.setBounds(12, 91, 66, 21);
        frame.getContentPane().add(lblImage);
        lblImage.setVisible(false);

        // Text Label
        JLabel lblText = new JLabel("Text");
        lblText.setFont(new Font("Yu Gothic", Font.BOLD | Font.ITALIC, 19));
        lblText.setBounds(12, 391, 56, 16);
        frame.getContentPane().add(lblText);
        lblText.setVisible(false);

        // Browse image button
        JButton btnBrowse = new JButton("Browse");
        btnBrowse.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 26));
        btnBrowse.setBounds(12, 28, 164, 45);
        frame.getContentPane().add(btnBrowse);

        btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JLabel l = new JLabel("No File Selected");
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                int r = fileChooser.showOpenDialog(null);
                String path = "";

                if (r == JFileChooser.APPROVE_OPTION) {
                    frame.getContentPane().remove(label);
                    path = fileChooser.getSelectedFile().getAbsolutePath();
                } else {
                    path = "Operation Canceled";
                }

                // Set labels to visible
                lblImage.setVisible(true);
                lblText.setVisible(true);

                // Gray Scaled Image
                GreyscaleImage imageG = new GreyscaleImage(path);

                // Normal Image
                File input = new File(path);
                BufferedImage image = null;
                try {
                    image = ImageIO.read(input);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                label.setBounds(100, 100, 300, 200);

                Image dimage = image.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);

                ImageIcon icon = new ImageIcon(dimage);
                label.setIcon(icon);
                frame.getContentPane().add(label);
                frame.revalidate();
  
                

            }
        });
    }
}
