import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtility{
  private static final String URL = "jdbc:mysql://localhost:3306/campus_lost_and_found";
  private static final String USER = "root";
  private static final String PASS = "herculus123";

  public static Connection getConnection(){
    try{
      return DriverManager.getConnection(URL,USER,PASS);
    }
    catch(SQLException e){
      System.out.println("Failed to Connect to Database !");
      e.printStackTrace();
      return null;
    }
  }
}