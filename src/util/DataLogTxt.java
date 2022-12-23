package util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.net.InetAddress;
import java.util.Locale;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

public class DataLogTxt {

  public String[] geolocalizeIp(String ipAddress, String databasePath) throws IOException {
    // Create a DatabaseReader object using the GeoLite2 database
    DatabaseReader dbReader;
    try {
      dbReader = new DatabaseReader.Builder(new File(databasePath)).build();
    } catch (IOException e) {
      System.out.println("Error opening GeoLite2 database: " + e.getMessage());
      return null;
    }

    // Geolocalize the IP address
    InetAddress inetAddress = InetAddress.getByName(ipAddress);
    CityResponse response;
    try {
      response = dbReader.city(inetAddress);
    } catch (GeoIp2Exception e) {
      System.out.println("Error geolocalizing IP address " + ipAddress + ": " + e.getMessage());
      return null;
    }

    // Get the city and country name
    String cityName = response.getCity().getName();
    String countryName = response.getCountry().getName();

    // Return the city and country names as a String array
    return new String[] {cityName, countryName};
  }


  public void logToFileDummyCommand(String logMessage) {
    try {
      // Set up the file and the BufferedWriter that will write to it
      File logFile = new File("log_dummycommand.txt");
      BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
      // Get the current time
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String currentTime = dateFormat.format(new Date());

      // Write the log message to the file
      writer.write("[" +currentTime + "] " + logMessage);
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
    
      // Get the current time
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String currentTime = dateFormat.format(new Date());
         
      // Write the log message to the file
      writer.write("[" +currentTime + "] " + logMessage);
      writer.newLine();
    
      // Close the writer
      writer.close();
    } catch (IOException e) {
      // An exception occurred while trying to write to the file
      e.printStackTrace();
    }
  }
}
