package client;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageCheck {

    public static boolean isValidImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return false;
            }
            // Verifica dimensioni ragionevoli (ad esempio, non più grandi di 10000x10000 pixel)
            int width = image.getWidth();
            int height = image.getHeight();
            if (width <= 0 || height <= 0 || width > 10000 || height > 10000) {
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}