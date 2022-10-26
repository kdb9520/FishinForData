import com.jcraft.jsch.*;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.*;
import java.util.Properties;

public class Helpers {
    public static final int DELETE = 999;

    public static final String host = "starbug.cs.rit.edu";
    public static final int databasePort = 5432;
    public static final int localPort = 8080;
    public static final String databaseName = "p32002_30";
    public static final String driverName = "org.postgresql.Driver";

    private static final AdminAccount account = new AdminAccount();

    public static int getOption(int max) {
        return getOption(max, false);
    }

    public static int getOption(int max, boolean deleteOk) {
        while (true) {
            try {
                int input = Integer.parseInt(
                        MainClass.in.nextLine());
                if ((input >= 0 && input <= max) ||
                        (deleteOk && input == DELETE)) {
                    System.out.println();
                    return input;
                }
            } catch (Exception ignored) { }
            System.out.print("Invalid input, try again: ");
        }
    }

    public static String getCappedLengthInput() {
        return getCappedLengthInput(50); //Most common cap is 50
    }

    public static String getCappedLengthInput(int maxLength) {
        while (true) {
            String input = MainClass.in.nextLine();
            if (input.length() <= maxLength) {
                return input;
            }
            System.out.println("Input too long, try again (max " + maxLength + " characters):");
        }
    }

    //Session opening and closing will be handled by MainClass
    public static Session createSession() {
        Session session = null;
        try {
            String username = account.GetUsername();
            String password = account.GetPassword(); 
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            //This is creating an SSH session
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications",
                    "publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Session Connected");
            session.setPortForwardingL(localPort, "localhost", databasePort);
            //System.out.println("Port Forwarded");
        }
        catch (Exception e) {
            System.out.printf("Error in createSession(): %s%n", e);
            e.printStackTrace();
        }

        return session;
    }

    //Connection opening and closing will be handled by MainClass
    public static Connection createConnection() {
        Connection conn = null;
        String username = account.GetUsername();
        String password = account.GetPassword(); 
        try {
            //Starbug is forwarding to localhost:8080 so we'll connect to there.
            String url = "jdbc:postgresql://localhost:" + localPort + "/" + databaseName;

            //this connects to starbug postgresql database using 'postgresql' jar from JDBC
            //System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", username);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            //System.out.println("Database connection established");

            // Do something with the database EXAMPLE
            //PreparedStatement st = conn.prepareStatement(
            //        "SELECT * FROM user_account");
            //ResultSet rs = st.executeQuery();
            //rs.next();
            //System.out.println(rs.getString("username"));
            //rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //conn.close();
        return conn;
    }

    public static byte[] getHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return null; //Should never happen, "SHA-256" always exists
        }
    }
}
