import java.util.*;

public class MainClass {
    public static Scanner in;
    public static String username;

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
        handleLogin();
        if (username == null) {
            System.out.println("Goodbye...");
            return;
        }
        int option;
        while ((option = mainMenuOption()) != 0) {
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
        System.out.println("Goodbye...");
        in.close();
    }
}
