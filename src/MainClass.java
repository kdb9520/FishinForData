import java.util.*;

public class MainClass {
    public static Scanner in;
    public static Object user;

    private static void handleLogin() {
        System.out.print("\r\nCurrent menu: NOT LOGGED IN\r\n\r\n" +
                "1: Login\r\n" +
                "2: Create Account\r\n" +
                "0: Cancel and Exit Program\r\n" +
                "What would you like to do?: ");
        int option = Helpers.getOption(2);
        switch (option) {
            case 1: {
                user = AccountManager.login();
                break;
            }
            case 2: {
                user = AccountManager.create();
                break;
            }
            default: {
                user = null;
            }
        }
    }

    private static int mainMenuOption() {
        System.out.print("\r\nCurrent menu: MAIN MENU\r\n\r\n" +
                "0: Logout and Exit Program\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(9);
    }

    public static void main(String[] args) {
        in = new Scanner(System.in);
        handleLogin();
        if (user == null) {
            System.out.println("Goodbye...");
            return;
        }
        int option;
        while ((option = mainMenuOption()) != 0) {
            System.out.println(option);
            switch (option) {

            }
        }
        System.out.println("Goodbye...");
        in.close();
    }
}
