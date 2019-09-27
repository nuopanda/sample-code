package tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The class Write to local tool.
 */
public class WriteToLocalTool {

  /**
   * Write object to local.
   *
   * @param input the input
   * @param filePath the file path
   * @throws IOException the io exception
   */
  public static void writeObjectToLocal(Object input, String filePath) throws IOException {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    ObjectOutput out = null;
    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
    try {
      out = new ObjectOutputStream(b);
      out.writeObject(input);
      out.flush();
      byte[] serialByteArray = b.toByteArray();
      fileOutputStream.write(serialByteArray);
    } finally {
      try {
        fileOutputStream.close();
        b.close();
      } catch (IOException e) {
        System.err.println(e.toString());
      }
    }
  }

  /**
   * Read object from local object.
   *
   * @param filePath the file path
   * @return the object
   */
  public static Object readObjectFromLocal(String filePath) {
    ObjectInputStream in = null;
    try {
      byte[] btArrayAddressMap = Files.readAllBytes(Paths.get(filePath));
      ByteArrayInputStream b = new ByteArrayInputStream(btArrayAddressMap);
      in = new ObjectInputStream(b);
      return in.readObject();
    } catch (ClassNotFoundException e) {
      System.err.println(e.toString());
    } catch (IOException e) {
      System.err.println(e.toString());
    }
    return null;
  }
}
