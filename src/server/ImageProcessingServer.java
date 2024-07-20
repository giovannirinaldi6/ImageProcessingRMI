package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import shared.InterfaceImageProcessing;

public class ImageProcessingServer {
    public static void main(String[] args) {
        try {
            InterfaceImageProcessing service = new ImplInterfaceImageProcessing();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ImageProcessingService", service);
            System.out.println("Image Processing Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}