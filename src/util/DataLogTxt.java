package util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataLogTxt {
    public void logToFileDummyCommand(String logMessage) {
        try {
          // Set up the file and the BufferedWriter that will write to it
          File logFile = new File("log_dummycommand.txt");
          BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
    
          // Write the log message to the file
          writer.write(logMessage);
          writer.newLine();
    
          // Close the writer
          writer.close();
        } catch (IOException e) {
          // An exception occurred while trying to write to the file
          e.printStackTrace();
        }
      }

      public void logToFileSshEntries(String logMessage) {
        try {
          // Set up the file and the BufferedWriter that will write to it
          File logFile = new File("log_verification.txt");
          BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
    
          // Write the log message to the file
          writer.write(logMessage);
          writer.newLine();
    
          // Close the writer
          writer.close();
        } catch (IOException e) {
          // An exception occurred while trying to write to the file
          e.printStackTrace();
        }
      }
}
