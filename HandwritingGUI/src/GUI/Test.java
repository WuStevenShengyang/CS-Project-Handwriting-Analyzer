package GUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {
    public static void main(String[] args) throws IOException {
        String detector = "C:\\Users\\wsywu\\git\\CS-Project-Handwriting-Analyzer-\\OCR_Model\\src\\main.py";
        String targetFile = "C:\\Users\\wsywu\\git\\CS-Project-Handwriting-Analyzer-\\OCR_Model\\sources\\text.txt";
        String command = "python C:\\Users\\wsywu\\OneDrive\\Desktop\\hello.py";

        String outputFolder = "C:\\Users\\wsywu\\git\\CS-Project-Handwriting-Analyzer-\\OCR_Model\\sources\\SplitedImages";
        File outputFolderF = new File(outputFolder);

        // Execute OCR and get outputs
        Process process = Runtime.getRuntime().exec(command);
        System.out.println("wait for");
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
    }
}
