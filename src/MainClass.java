import com.jcraft.jsch.Session;

import java.util.*;

public class MainClass {
    public static Scanner in;
    public static String username;
    private static Session session;

    private static void handleLogin() {
        System.out.print("\r\nCurrent menu: NOT LOGGED IN\r\n\r\n" +
                "1: *** Login ***\r\n" +
                "2: Create Account\r\n" +
                "0: Cancel and Exit Program\r\n" +
                "What would you like to do?: ");
        int option = Helpers.getOption(2);
        switch (option) {
            case 1: { // Login
                username = AccountManager.login();
                break;
            }
            case 2: { // Create account
                username = AccountManager.create();
                break;
            }
            default: { // Exit program
                username = null;
            }
        }
    }

    private static void closeResources() {
        //delPortForwardingL throws errors so wrapped in try-catch
        try {
            session.delPortForwardingL(Helpers.localPort);
        }
        catch (Exception e) {
            System.out.println("Error with deleting PortForwarding, session closing anyway");
        }
        finally {
            System.out.println("Closing Session...");
            session.disconnect();
            in.close();
        }
    }

    private static int mainMenuOption() {
        System.out.print("\r\nCurrent menu: MAIN MENU\r\n" +
                "You're signed in as \"" + username + "\"\r\n\r\n" +
                "1: Account and Follows\r\n" +
                "2: *** Music Search ***\r\n" +
                "3: Created Collections\r\n" +
                "0: Logout and Exit Program\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(3);
    }

    public static void main(String[] args) {
        in = new Scanner(System.in);

        //setup SSH port to connect to database and listen on localhost:8080
        session = Helpers.createSession();
        if (session == null) {
            System.out.println("Ending immediately due to failure in creating session.");
            System.out.println("Check the credentials in AdminAccount.java. Goodbye...");
            return;
        }

        handleLogin();
        if (username == null) {
            closeResources();
            return;
        }
        int option;
        while ((option = mainMenuOption()) != 0 && session.isConnected()) {
            System.out.println(option);
            switch (option) {
                case 1: {
                    AccountManager.accountMenu();
                    break;
                }
                case 2: {
                    SearchManager.songSearchLoop();
                    break;
                }
                case 3: {
                    CollectionManager.collectionMenu();
                    break;
                }
            }
        }

        closeResources();
    }
}
