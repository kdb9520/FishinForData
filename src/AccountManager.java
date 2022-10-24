import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AccountManager {
    public static Timestamp lastAccessTimestamp;
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    public static String login() {
        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return null;
        }

        String login_username = null;
        int result = 0;
        int attempt;
        try (conn) {
            for (attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
                //TODO super barebones with no input handling
                System.out.println("Please enter your username:");
                login_username = MainClass.in.nextLine();
                System.out.println("Please enter your password:");
                String login_password = MainClass.in.nextLine();

                Timestamp now = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
                PreparedStatement st = conn.prepareStatement(
                        "UPDATE user_account SET last_access_timestamp = ? " +
                                "WHERE username = ? AND password = ?"
                );
                st.setTimestamp(1, now);
                st.setString(2, login_username);
                st.setBytes(3, Helpers.getHash(login_password));

                //for debug
                try {
                    result = st.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                if (result == 1) {
                    System.out.println("Login success");
                    lastAccessTimestamp = now;
                    conn.commit();
                    break;
                } else {
                    System.out.println("Invalid credentials (attempt " + attempt + ")");
                    //Nothing to rollback
                }
            }
            if (attempt > MAX_LOGIN_ATTEMPTS) {
                System.out.println("Maximum login attempts exceeded.");
            }
        } catch (Exception ignored) {}
        return result == 1 ? login_username : null;
    }

    public static String create() {
        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return null;
        }

        String new_username = null;
        int result = 0;
        try (conn) {
            //TODO super barebones with no input handling
            System.out.println("Please enter a unique username:");
            new_username = MainClass.in.nextLine();
            System.out.println("Please enter a password:");
            String new_password = MainClass.in.nextLine();

            System.out.println("Please enter your email:");
            String new_email = MainClass.in.nextLine();
            System.out.println("Please enter your first name:");
            String new_firstName = MainClass.in.nextLine();
            System.out.println("Please enter your last name:");
            String new_lastName = MainClass.in.nextLine();

            Timestamp now = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO user_account(username, password, email, first_name, " +
                            "last_name, creation_timestamp, last_access_timestamp) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            st.setString(1, new_username);
            st.setBytes(2, Helpers.getHash(new_password));
            st.setString(3, new_email);
            st.setString(4, new_firstName);
            st.setString(5, new_lastName);
            st.setTimestamp(6, now);
            st.setTimestamp(7, now);

            //for debug
            try {
                result = st.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            if (result == 1) {
                System.out.println("Creation success");
                lastAccessTimestamp = now;
                conn.commit();
            } else {
                System.out.println("Failed to create new user account");
                //Nothing to rollback
            }
        } catch (Exception ignored) {}

        return result == 1 ? new_username : null;
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
