package util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.net.InetAddress;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import org.json.JSONObject;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;



public class DataLogTxt {
  
  public String[] geolocalizeIp(String IpAddress) throws IOException {
    try {
      // Send a GET request to the IP-API API to get the location of the IP address
      URL url = new URL("http://ip-api.com/json/" + IpAddress + "?fields=7368703");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(5000);
      con.setReadTimeout(5000);

      // Read the response from the API
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuilder content = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
      in.close();
      // Parse the JSON response from the API
      JSONObject json = new JSONObject(content.toString());

      // Get the city and country name from the JSON object
      String query =json.getString("query");
      String status = json.getString("status");
      String continent = json.getString("continent");
      String continentCode =json.getString("continentCode");
      String country = json.getString("country");
      String countryCode =json.getString("countryCode");
      String region =json.getString("region");
      String regionName =json.getString("regionName");
      String city = json.getString("city");
      String zip =json.getString("zip");
      float latitude = json.getFloat("lat");
      float longitude = json.getFloat("lon");
      String isp =json.getString("isp");
      String org =json.getString("org");
      String as =json.getString("as");
      String asName =json.getString("asname");
      

      // Return the city and country names as a String array
      return new String[] {query,status,continent,continentCode,country,countryCode,region,regionName,city,zip,Float.toString(longitude),Float.toString(latitude),isp,org,as,asName};
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
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
        String connection_string_db = env.getProperty("MONGODB_CONNECTION_STRING");

        //########## GEOIP ###########
        String[] IP_Data = new String[0];
        
        try {
            IP_Data = geolocalizeIp(IP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //############################

        ConnectionString connectionString = new ConnectionString(connection_string_db);
        
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("HoneypotDB_easypsw");
        MongoCollection<Document> collection = database.getCollection("mycollection_easypsw");



        Document doc = new Document()
        .append("time", Time)
        .append("ip", IP_Data[0])
        .append("status", IP_Data[1])
        .append("continent", IP_Data[2])
        .append("continentCode", IP_Data[3])
        .append("country", IP_Data[4])
        .append("countryCode", IP_Data[5])
        .append("region", IP_Data[6])
        .append("regionName", IP_Data[7])
        .append("city", IP_Data[8])
        .append("zip", IP_Data[9])
        
        .append("location", new Document("type", "Point").append("coordinates", Arrays.asList(Double.parseDouble(IP_Data[10]), Double.parseDouble(IP_Data[11]))))
        .append("isp", IP_Data[12])
        .append("org", IP_Data[13])
        .append("as", IP_Data[14])
        .append("asname", IP_Data[15])
        
        .append("username", Username)
        .append("password", Password)
        .append("authentication", Authentication);
        
        collection.createIndex(new Document("location", "2dsphere"));
        collection.insertOne(doc);
        mongoClient.close();
  }
}


