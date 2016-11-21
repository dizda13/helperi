import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by hadoop on 10/31/16.
 */
public class pisi {


    public static void main(String[] args){
        spoji();
    }

    private static void spoji(){
        Properties mySqlProperties;
        String MYSQL_CONNECTION_URL;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        mySqlProperties = new Properties();
        mySqlProperties.put("user", "root");
        mySqlProperties.put("password", "$erap10n");
        mySqlProperties.put("driver", "com.mysql.jdbc.Driver");
        MYSQL_CONNECTION_URL ="jdbc:mysql://sq.cgaclgzy3nho.eu-west-1.rds.amazonaws.com:3306/new_data?user=root&password=$erap10n";
        Connection connection=null;
        try {
            connection=DriverManager.getConnection(MYSQL_CONNECTION_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String csvFile = "/home/ec2-user/hepekCSV.txt";
        String pointFile="/home/ec2-user/hepek.txt";
        BufferedReader readerOtherValues = null;
        BufferedReader readerPoint = null;
        String cvsLine = "";
        String pointLine = "";
        String cvsSplitBy = ",";

        try {

            readerOtherValues = new BufferedReader(new FileReader(csvFile));
            readerPoint = new BufferedReader(new FileReader(pointFile));
            while ((cvsLine = readerOtherValues.readLine()) != null) {
                pointLine=readerPoint.readLine();
                // use comma as separator
                String[] values = cvsLine.split(cvsSplitBy);

                connection.createStatement().executeUpdate("INSERT INTO unit_position VALUES ("+values[0]+","+values[1]+","+pointLine+","
                +values[2]+","+values[3]+","+values[4]+","+values[5]+","+values[6]+","+values[7]+")");

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (readerOtherValues != null) {
                try {
                    readerOtherValues.close();
                    readerPoint.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
