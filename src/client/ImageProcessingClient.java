package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import shared.ImageResult;
import shared.InterfaceImageProcessing;

public class ImageProcessingClient {
	
	private static String clientID;
	
	private static Scanner scanner = new Scanner(System.in);
	
    public static void main(String[] args) {
  
        if (args.length < 2) {
            System.out.println("Usage: java ImageProcessingClient <image-file-name> <operation>");
            System.out.println("Available operations: grayscale, sobel, oilpaint");
            return;
        }

        String fileName = args[0];
        String operation = args[1];
        String filePath = "./" + fileName; // Assume che il file si trovi nella stessa cartella del client

        try {
            File selectedFile = new File(filePath);
            if (!selectedFile.exists()) {
                System.out.println("File not found: " + filePath);
                return;
            }

            
            if (!ImageCheck.isValidImage(selectedFile)) {
                System.out.println("The image is corrupted or unsupported.");
                return;
            }

            BufferedImage image = ImageIO.read(selectedFile);
            byte[] imageBytes = bufferedImageToByteArray(image);
            String hash = calculateHash(imageBytes);

            // Collegamento al registry RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            InterfaceImageProcessing service = (InterfaceImageProcessing) registry.lookup("ImageProcessingService");
            
            clientID = service.generateClientID();
            System.out.println("Client registered with ID: " + clientID);
            
            ImageResult result = null;
           
            switch (operation.toLowerCase()) {
                case "grayscale":
                	result = service.processImage(imageBytes, hash);
                    break;
                case "sobel":
                	result = service.sobelEdgeDetection(imageBytes, hash);
                    break;
                case "oilpaint":
                	result = service.oilPaintEffect(imageBytes, hash);
                    break;
                default:
                    System.out.println("Unknown operation: " + operation);
                    return;
            }

            if (result != null && result.getMessage()==null) {
                // Verifica dell'hash dell'immagine ricevuta
            	byte[] processedBytes = result.getImageBytes();
                String newHash = result.getHash();
                
                if (!newHash.equals(calculateHash(processedBytes))) {
                    System.out.println("The received image is corrupted.");
                    return;
                }

                BufferedImage processedImage = byteArrayToBufferedImage(processedBytes);
                BufferedImage rgbImage = new BufferedImage(processedImage.getWidth(), processedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                rgbImage.createGraphics().drawImage(processedImage, 0, 0, null);
                File outputfile = new File("output.jpg");
                ImageIO.write(rgbImage, "jpg", outputfile);
                System.out.println("Image processed and saved as output.jpg");
            }
            else {
            	System.out.println(result.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Press for Exit");
        scanner.nextLine();
        scanner.close();
    
    }

    private static byte[] bufferedImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    private static BufferedImage byteArrayToBufferedImage(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return ImageIO.read(bais);
    }

    private static String calculateHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
