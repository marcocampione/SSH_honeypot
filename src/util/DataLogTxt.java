package util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.net.InetAddress;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.*;
import org.bson.codecs.pojo.ClassModel;



public class DataLogTxt {
  private static class Location {
    private String city;
    private String country;
    private String continent;
    @BsonProperty("location")
    private Point location;
    
    public Location() {}
    //public Location(double latitude, double longitude) {
      //  this.location = new Point(latitude, longitude);
    //}

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public Point getLocation(){
        return location;
    }
    
    public void setLocation(Point location) {
        this.location = location;
    }
   }
  
  public String[] geolocalizeIp(String IpAddress) throws IOException {
    String databasePath = "GeoLite2-City/GeoLite2-City.mmdb";
  
    //Remove comment if need to test on windows
    //String databasePath = "GeoLite2-City\\GeoLite2-City.mmdb";

    // Create a DatabaseReader object using the GeoLite2 database
    DatabaseReader dbReader;
    try {
      dbReader = new DatabaseReader.Builder(new File(databasePath)).build();
    } catch (IOException e) {
      System.out.println("Error opening GeoLite2 database: " + e.getMessage());
      return null;
    }

    // Geolocalize the IP address
    InetAddress inetAddress = InetAddress.getByName(IpAddress);
    CityResponse response;
    try {
      response = dbReader.city(inetAddress);
    } catch (GeoIp2Exception e) {
      System.out.println("Error geolocalizing IP address " + IpAddress + ": " + e.getMessage());
      return null;
    }

    // Get the city and country name
    String cityName = response.getCity().getName();
    String countryName = response.getCountry().getName();
    String continent = response.getContinent().getName();
    Double latitude = response.getLocation().getLatitude();
    Double longitude = response.getLocation().getLongitude();
    

    // Return the city and country names as a String array
    return new String[] {cityName, countryName, latitude.toString(),longitude.toString(),continent};
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
  

  public void SavefileDatabase(String Time, String IP,String Username, String Password, String Authentication){
    Properties env = new Properties();
        try {
          env.load(new FileInputStream(".env"));
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        String password_db = env.getProperty("MONGODB_PASSWORD");


        ConnectionString connectionString = new ConnectionString("mongodb+srv://Marcocampione:"+password_db+"@honeypotcampione.xialfkn.mongodb.net/?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        MongoDatabase database = mongoClient.getDatabase("mydatabase");
        MongoCollection<Document> collection = database.getCollection("mycollection");

        String[] IP_location = new String[0];
        
        try {
            IP_location = geolocalizeIp(IP);
            
            //Remove comment if need to test on windows
            //IP_location = logger.geolocalizeIp("82.41.37.103");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Location location = new Location();
        location.setCity(IP_location[0].toString());
        location.setCountry(IP_location[1].toString());
        location.setContinent(IP_location[4].toString());
        //location.setLocation(new Point(Double.parseDouble(IP_location[2]),Double.parseDouble(IP_location[3])));
        //location.setLocation(new Point(IP_location[2],IP_location[3]));

        Document locationDoc = new Document().append("location", location);

        Document doc = new Document()
        .append("Time", Time)
        .append("IP", IP)
        .append("location", locationDoc)
        .append("Username", Username)
        .append("Password", Password)
        .append("Authentication", Authentication);

        collection.insertOne(doc);
        mongoClient.close();
  }
}


