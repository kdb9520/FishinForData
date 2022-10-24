import java.sql.Connection;
import java.util.Properties;
import java.sql.DriverManager;

import com.jcraft.jsch.*;



public class Helpers {
    public static final int DELETE = 999;

    private static final String host = "starbug.cs.rit.edu";
    private static final int databasePort = 5432;
    private static final int localPort = 8080;
    private static final String databaseName = "p32002_30";
    private static final String driverName = "org.postgresql.Driver";

    private static AdminAccount account = new AdminAccount();
    

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




    //Session opening and closing will be handled by MainClass
    public static Session createSession(){
        Session session = null;
        try{
            String username=account.Get_Username();
            String password = account.GetPassword(); 
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            //This is creating an SSH session 
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Session Connected");
            session.setPortForwardingL(localPort, "localhost", databasePort);
            //System.out.println("Port Forwarded");
        }
        catch(Exception e){
            System.out.println(String.format("Error in createSession(): %s", e));
            e.printStackTrace();
        }
        
        return session;
    }

    //Connection opening and closing will be handled by 
    public static Connection createConnection(){


        Connection conn = null;
        String username=account.Get_Username();
        String password = account.GetPassword(); 
        try {
            

            //Starbug is forwarding to localhost:8080 so we'll connect to there.
            String url = "jdbc:postgresql://localhost:"+ localPort + "/" + databaseName;

            //this connects to starbug postgresql database using 'postgresql' jar from JDBC
            //System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", username);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            //System.out.println("Database connection established");
            
            // Do something with the database EXAMPLE
            //Statement st = conn.createStatement();
            //ResultSet rs = st.executeQuery("Select * FROM user_account;");
            //rs.next();
            //System.out.println(rs.getString("username"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        //conn.close();
        return conn;
    }
}
