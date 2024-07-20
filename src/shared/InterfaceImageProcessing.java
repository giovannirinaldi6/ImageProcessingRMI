package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceImageProcessing extends Remote {
	ImageResult processImage(byte[] imageBytes, String hash) throws RemoteException;
	ImageResult sobelEdgeDetection(byte[] imageBytes, String hash) throws RemoteException;
	ImageResult oilPaintEffect(byte[] imageBytes, String hash) throws RemoteException;
	String generateClientID() throws RemoteException;
}
