import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.*;


public class AccountManager {
    public static ZonedDateTime lastAccessTimestamp;

    public static String login() {
        System.out.println("login called");
        return "(from login)";
    }

    public static String create() {
        System.out.println("create called");

        Connection conn = Helpers.createConnection();
        if(conn==null){
            return "Something went wrong establishing connection to database! Check Helpers.java";
        }

        try{
            
            //TODO super barebones with no input handling
            System.out.println("Please enter a unique username:");
            String new_username = MainClass.in.nextLine();
            //TODO add sha-256 hashing for password
            System.out.println("Please enter a password:");
            String new_password = MainClass.in.nextLine();

            System.out.println("Please enter your email:");
            String new_email = MainClass.in.nextLine();
            System.out.println("Please enter your first name:");
            String new_firstName = MainClass.in.nextLine();
            System.out.println("Please enter your last name:");
            String new_lastName = MainClass.in.nextLine();

            Statement st = conn.createStatement();
            String sql_base="INSERT INTO user_account(username, password, email, first_name, last_name, creation_timestamp, last_access_timestamp) VALUES(\'%s\', \'%s\', \'%s\', \'%s\', \'%s\', NOW(), NOW());";
            
            String sql = String.format(sql_base, new_username, new_password, new_email, new_firstName, new_lastName);
            //System.out.println(sql);
            int result =0;
            //result = st.executeQuery(sql);

            //for debug
            try{
                result = st.executeUpdate(sql);
            }
            catch(Exception e){
                System.out.println(e);
            }

            if(result==1){
                System.out.println("Creation success");
            }
            else{
                System.out.println("Failed to create new user account");
            }

            //Update lastAccessTimestamp
            lastAccessTimestamp = ZonedDateTime.now();
            
        }catch(Exception e){}
        finally{
            try{ //idk why these throw errors, intended for create() to throw the exception
                conn.rollback();
                conn.close();}
            catch(Exception e){}
        }
        

        return "(from create)";
    }

    private static int accountMenuOption() {
        System.out.print("\r\nCurrent menu: ACCOUNT/FOLLOWS\r\n\r\n" +
                "1: Your Account Profile/Management\r\n" +
                "2: Following You (+ Follow Back)\r\n" +
                "3: Your Follows (+ Remove Follow)\r\n" +
                "4: Search for Accounts to Follow\r\n" +
                "0: Return to Main Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(4);
    }

    public static void accountMenu() {
        int option;
        while ((option = accountMenuOption()) != 0) {
            System.out.println(option);
            switch (option) {
                case 1: { // Account profile
                    System.out.println("What others see:");
                    System.out.println("Your username and email, as well as...");
                    showPublicProfile(MainClass.username, null);
                    System.out.println("What you can see and change:");
                    showPrivateProfileMenu();
                    break;
                }
                case 2: { // Who's following you
                    followingYou();
                    break;
                }
                case 3: { // Who you're following
                    yourFollows();
                    break;
                }
                case 4: { // Follow by email
                    emailSearch();
                    break;
                }
            }
        }
    }

    private static void showPublicProfile(String username, String email) {
        if (username == null) {
            // TODO Query for username if not provided in method, for safety
            username = "?";
        }
        if (email != null) {
            // Only print email and username if not self-displaying
            System.out.println("Email: " + email);
            System.out.println("Username: " + username);
        }
        // TODO Phase 4 only: display #collections/followers/following and top10
        System.out.println("(More statistics to be added in Phase 4)");
    }

    private static void showPrivateProfileMenu() {
        /* TODO Ideally, show everything except password, but allow user to change
        *   any non-timestamp thing (including password)- if username gets changed,
        *   remember to update MainClass.username for consistency */
        System.out.println("(Private profile)");
    }

    private static void followingYou() {
        // TODO Implement pagination through following you
        System.out.println("(Following you)");
    }

    private static void yourFollows() {
        // TODO Implement pagination through your follows
        System.out.println("(Your follows)");
    }

    private static void emailSearch() {
        // TODO Implement search through emails via input
        System.out.println("(Email search)");
    }
}
