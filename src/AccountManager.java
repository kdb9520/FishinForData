import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

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
                System.out.println("Please enter your username:");
                login_username = MainClass.in.nextLine(); //If too long, it'll be rejected anyway
                System.out.println("Please enter your password:");
                String login_password = MainClass.in.nextLine();

                Timestamp now = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
                PreparedStatement st = conn.prepareStatement(
                        "SELECT salt FROM user_account WHERE username = ?"
                );
                st.setString(1, login_username);
                String salt = null;
                //for debug
                try {
                    ResultSet set = st.executeQuery();
                    if (!set.next()) { //User doesn't exist
                        System.out.println("Invalid credentials (attempt " + attempt + ")");
                        continue;
                    }
                    salt = set.getString(1);
                    set.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                if (salt == null) { //Some other error getting the salt
                    System.out.println("Invalid credentials (attempt " + attempt + ")");
                    continue;
                }

                st = conn.prepareStatement(
                        "UPDATE user_account SET last_access_timestamp = ? " +
                                "WHERE username = ? AND password = ?"
                );
                st.setTimestamp(1, now);
                st.setString(2, login_username);
                st.setBytes(3, Helpers.getHash(login_password + salt));

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
                } else { //No username+password combo matching (likely wrong password)
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
            while (new_username == null) { //Ensure username is unique
                System.out.println("Please enter a unique username:");
                new_username = Helpers.getCappedLengthInput();
                PreparedStatement st = conn.prepareStatement(
                        "SELECT username FROM user_account WHERE username = ?"
                );
                st.setString(1, new_username);
                ResultSet set = st.executeQuery();
                if (set.next()) {
                    System.out.println("Username \"" + new_username + "\" is already taken.");
                    new_username = null;
                }
                set.close();
            }
            System.out.println("Please enter a password:");
            String new_password = MainClass.in.nextLine(); //Password can be any length

            String new_email = null;
            while (new_email == null) { //Ensure email is unique
                System.out.println("Please enter your email:");
                new_email = Helpers.getCappedLengthInput();
                PreparedStatement st = conn.prepareStatement(
                        "SELECT email FROM user_account WHERE email = ?"
                );
                st.setString(1, new_email);
                ResultSet set = st.executeQuery();
                if (set.next()) {
                    System.out.println("Email \"" + new_email + "\" is already taken.");
                    new_email = null;
                }
                set.close();
            }
            System.out.println("Please enter your first name:");
            String new_firstName = Helpers.getCappedLengthInput();
            System.out.println("Please enter your last name:");
            String new_lastName = Helpers.getCappedLengthInput();

            Timestamp now = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
            byte[] saltArr = new byte[24]; //24 full-range bytes = 32 base-64 characters
            new SecureRandom().nextBytes(saltArr);
            String salt = Base64.getEncoder().encodeToString(saltArr);
            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO user_account(username, password, email, first_name, " +
                            "last_name, creation_timestamp, last_access_timestamp, salt) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            st.setString(1, new_username);
            st.setBytes(2, Helpers.getHash(new_password + salt));
            st.setString(3, new_email);
            st.setString(4, new_firstName);
            st.setString(5, new_lastName);
            st.setTimestamp(6, now);
            st.setTimestamp(7, now);
            st.setString(8, salt);

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
        /* TODO Phase 4 only: Ideally, show everything except password, but allow user
            to change any non-timestamp thing (including password)- if username gets
            changed, remember to update MainClass.username for consistency */
        System.out.println("(Private profile to be added in Phase 4)");
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
