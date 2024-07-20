package shared;

import java.io.Serializable;

public class ImageResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private byte[] imageBytes;
    private String hash;
    private String message;

    public ImageResult(byte[] imageBytes, String hash) {
        this.imageBytes = imageBytes;
        this.hash = hash;
        this.message = null;
    }
    
    public ImageResult(String message) {
    	this.message = message;
    }
    

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public String getHash() {
        return hash;
    }
    
    public String getMessage() {
        return message;
    }
}