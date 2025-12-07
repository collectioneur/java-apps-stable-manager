//package pl.agh.lab.io;
//
//import pl.agh.lab.model.StableManager;
//
//import java.io.*;
//
//public class BinaryStorage {
//
//    public static void saveToFile(StableManager manager, File file) throws IOException {
//        try (ObjectOutputStream oos =
//                     new ObjectOutputStream(new FileOutputStream(file))) {
//            oos.writeObject(manager);
//        }
//    }
//
//    public static StableManager loadFromFile(File file) throws IOException, ClassNotFoundException {
//        try (ObjectInputStream ois =
//                     new ObjectInputStream(new FileInputStream(file))) {
//            Object obj = ois.readObject();
//            return (StableManager) obj;
//        }
//    }
//}
