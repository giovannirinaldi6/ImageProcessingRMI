package server;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.imageio.ImageIO;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import shared.InterfaceImageProcessing;
import shared.ImageResult;

public class ImplInterfaceImageProcessing extends UnicastRemoteObject implements InterfaceImageProcessing {
	
	private Map<String, String> cache;
	private Set<String> clientIDs;
	private String clientID;
	
    protected ImplInterfaceImageProcessing() throws RemoteException {
        super();
        cache = new HashMap<>();
        clientIDs = new HashSet<>();
    }

    @Override
    public synchronized String generateClientID() throws RemoteException {
        do {
            clientID = UUID.randomUUID().toString();
        } while (clientIDs.contains(clientID));
        clientIDs.add(clientID);
        System.out.println("Assigned new client ID: " + clientID);
        return clientID;
    }
    
    @Override
    public ImageResult processImage(byte[] imageBytes, String hash) throws RemoteException {
        return process(imageBytes, hash, "processImage");
    }

    @Override
    public ImageResult sobelEdgeDetection(byte[] imageBytes, String hash) throws RemoteException {
        return process(imageBytes, hash, "sobelEdgeDetection");
    }

    @Override
    public ImageResult oilPaintEffect(byte[] imageBytes, String hash) throws RemoteException {
        return process(imageBytes, hash, "oilPaintEffect");
    }

    private ImageResult process(byte[] imageBytes, String hash, String operation) throws RemoteException {
        try {
            if (!calculateHash(imageBytes).equals(hash)) {
                throw new RemoteException("Image hash does not match, image may be corrupted.");
            }
            
            String cacheKey = hash + "_" + operation;
            
            // Verifica nella cache
            if (cache.containsKey(cacheKey)) {
                String clientID = cache.get(cacheKey);
                String message = "This image has already been processed by client ID: " + clientID;
                return new ImageResult(message);
            }

            BufferedImage image = byteArrayToBufferedImage(imageBytes);
            BufferedImage processedImage = null;
            switch (operation) {
                case "processImage":
                    processedImage = applyGrayscale(image);
                    break;
                case "sobelEdgeDetection":
                    processedImage = applySobelEdgeDetection(image);
                    break;
                case "oilPaintEffect":
                    processedImage = applyOilPaintEffect(image);
                    break;
                default:
                    throw new RemoteException("Unknown operation: " + operation);
            }

            byte[] processedBytes = bufferedImageToByteArray(processedImage);
            String newHash = calculateHash(processedBytes);
            
            cache.put(cacheKey, clientID);
            
            return new ImageResult(processedBytes, newHash);

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RemoteException("Error processing image", e);
        }
    }

    private BufferedImage applyGrayscale(BufferedImage image) {
    	int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                int newColor = new Color(gray, gray, gray).getRGB();
                grayImage.setRGB(x, y, newColor);
            }
        }
        return grayImage;
    }

    private BufferedImage applySobelEdgeDetection(BufferedImage image) {
    	int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] sobelX = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };

        int[][] sobelY = {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixelX = 0;
                int pixelY = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int gray = new Color(image.getRGB(x + i, y + j)).getRed();
                        pixelX += gray * sobelX[i + 1][j + 1];
                        pixelY += gray * sobelY[i + 1][j + 1];
                    }
                }

                int magnitude = Math.min(255, (int) Math.sqrt(pixelX * pixelX + pixelY * pixelY));
                edgeImage.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        return edgeImage;
    }

    private BufferedImage applyOilPaintEffect(BufferedImage image) {
    	 int radius = 5;
         int intensityLevels = 32;

         int width = image.getWidth();
         int height = image.getHeight();
         BufferedImage oilPaintedImage = new BufferedImage(width, height, image.getType());

         for (int y = radius; y < height - radius; y++) {
             for (int x = radius; x < width - radius; x++) {
                 int[] intensityCount = new int[intensityLevels];
                 int[] avgRed = new int[intensityLevels];
                 int[] avgGreen = new int[intensityLevels];
                 int[] avgBlue = new int[intensityLevels];

                 for (int i = -radius; i <= radius; i++) {
                     for (int j = -radius; j <= radius; j++) {
                         Color color = new Color(image.getRGB(x + i, y + j));
                         int intensity = (color.getRed() + color.getGreen() + color.getBlue()) / 3 * intensityLevels / 256;
                         intensityCount[intensity]++;
                         avgRed[intensity] += color.getRed();
                         avgGreen[intensity] += color.getGreen();
                         avgBlue[intensity] += color.getBlue();
                     }
                 }

                 int maxIndex = 0;
                 for (int k = 1; k < intensityLevels; k++) {
                     if (intensityCount[k] > intensityCount[maxIndex]) {
                         maxIndex = k;
                     }
                 }

                 int red = avgRed[maxIndex] / intensityCount[maxIndex];
                 int green = avgGreen[maxIndex] / intensityCount[maxIndex];
                 int blue = avgBlue[maxIndex] / intensityCount[maxIndex];

                 oilPaintedImage.setRGB(x, y, new Color(red, green, blue).getRGB());
             }
         }
         return oilPaintedImage;
    }

    private byte[] bufferedImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    private BufferedImage byteArrayToBufferedImage(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return ImageIO.read(bais);
    }

    private String calculateHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
