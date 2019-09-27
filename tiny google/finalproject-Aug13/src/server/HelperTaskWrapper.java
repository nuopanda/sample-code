package server;

import java.io.File;
import java.io.Serializable;

/**
 * The type Helper task wrapper.
 */
public class HelperTaskWrapper implements Serializable {


  private long start;
  private long end;
  private boolean isDone;
  private String filePath;


  /**
   * Instantiates a new Helper task wrapper.
   *
   * @param filePath the file path
   * @param start the start
   * @param end the end
   */
  public HelperTaskWrapper(String filePath, long start, long end) {
    this.start = start;
    this.end = end;
    this.filePath = filePath;
    this.isDone = false;
  }


  /**
   * Gets start index.
   *
   * @return the start index
   */
  public long getStart() {
    return start;
  }

  /**
   * Gets end index.
   *
   * @return the end index
   */
  public long getEnd() {
    return end;
  }

  /**
   * the status of the task
   *
   * @return true if the task is done, false otherwise
   */
  public boolean isDone() {
    return isDone;
  }

  /**
   * Set task as done.
   *
   * @param isDone the task is done.
   */
  public void setDone(boolean isDone) {
    this.isDone = isDone;
  }


  /**
   * Gets file path.
   *
   * @return the file path
   */
  public String getFilePath() {
    return filePath;
  }


  /**
   * Gets file name for a task from the given file path.
   *
   * @return the file name
   */
  public String getTaskFilename() {
    File f = new File(this.filePath);
    return f.getName();
  }

  @Override
  public String toString() {
    return filePath + ':' +
        start + ':' + end;
  }
}
