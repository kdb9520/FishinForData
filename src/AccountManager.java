import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class AccountManager {
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
                    salt = set.getString("salt");
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
                        "SELECT 1 FROM user_account WHERE username = ?"
                );
                st.setString(1, new_username);
                ResultSet set = st.executeQuery();
                if (set.next() || new_username.equals("")) {
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
                        "SELECT 1 FROM user_account WHERE email = ?"
                );
                st.setString(1, new_email);
                ResultSet set = st.executeQuery();
                if (set.next() || new_email.equals("")) {
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
                "1: Your Account Profile\r\n" +
                "2: Following You (List Only)\r\n" +
                "3: Your Follows (+ Unfollow)\r\n" +
                "4: Account Search (View Profile / Follow)\r\n" +
                "0: Return to Main Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(4);
    }

    public static void accountMenu() {
        int option;
        while ((option = accountMenuOption()) != 0) {
            switch (option) {
                case 1: { // Account profile
                    System.out.println("What others see:");
                    System.out.println("Your username and email, as well as...");
                    showPublicProfile(MainClass.username, null);
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

    private static void showPublicProfile(String profile_username, String profile_email) {
        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return;
        }
        try (conn) {
            if (profile_email != null) {
                // Only print email and username if not self-displaying
                if (profile_username == null) { // Get username if not given
                    PreparedStatement st = conn.prepareStatement(
                            "SELECT username FROM user_account WHERE email = ?"
                    );
                    st.setString(1, profile_email);
                    ResultSet result = st.executeQuery();
                    if (!result.next()) {
                        System.out.println("Email \"" + profile_email + "\" not found.");
                        result.close();
                        return;
                    }
                    profile_username = result.getString("username");
                    result.close();
                }
                System.out.println("Email: " + profile_email);
                System.out.println("Username: " + profile_username);
            }

            PreparedStatement st = conn.prepareStatement(
                    "SELECT artist_name, COUNT(DISTINCT lbu) AS num_listens, " +
                            "COUNT(DISTINCT co) AS num_collections, " +
                            "COUNT(DISTINCT followers) AS num_followers, " +
                            "COUNT(DISTINCT followees) AS num_following " +
                            "FROM user_account u " +
                            "LEFT JOIN collection co ON u.username = co.username " +
                            "LEFT JOIN follow followers ON u.username = followers.followee " +
                            "LEFT JOIN follow followees ON u.username = followees.follower " +
                            "LEFT JOIN listened_by_user lbu ON u.username = lbu.username " +
                            "LEFT JOIN artist_music am ON lbu.release_id = am.release_id " +
                            "WHERE u.username = ? " +
                            "GROUP BY (u.username, artist_name) " +
                            "ORDER BY num_listens DESC, artist_name LIMIT 10"
            ); // Get user's top 10 listened artists + counts of collections/followers/following
            st.setString(1, profile_username);
            ResultSet result = st.executeQuery();
            if (!result.next()) {
                System.out.println("Error fetching additional statistics!\r\n");
                result.close();
                return;
            } // Display simple counts first
            System.out.println("# Collections: " + result.getInt("num_collections"));
            System.out.println("# Followers: " + result.getInt("num_followers"));
            System.out.println("# Following: " + result.getInt("num_following"));

            System.out.println("Top 10 Listened Artists:\r\n");
            String artist_name = result.getString("artist_name");
            if (artist_name == null) {
                System.out.println("(No listened artists)\r\n");
                result.close();
                return;
            } // Print #1 separately to check if there are any listens at all
            int num_listens = result.getInt("num_listens");
            System.out.println("#1: " + artist_name +
                    " | Listens: " + num_listens);

            // Iterate through #2 to #10
            for (int i = 2; i <= 10; i++) {
                if (!result.next() || (artist_name = result
                        .getString("artist_name")) == null) {
                    System.out.println("(No more listened artists)");
                    break;
                } // (Stop early if there are fewer than 10 listened artists)
                num_listens = result.getInt("num_listens");
                System.out.println("#" + i + ": " + artist_name +
                        " | Listens: " + num_listens);
            } // Always close the ResultSet and add an extra newline
            result.close();
            System.out.println();
        } catch (Exception ignored) {}
    }

    private static void followingYou() {
        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return;
        }
        System.out.println("Users following you:");
        try (conn) {
            PreparedStatement st = conn.prepareStatement(
                    "SELECT username, email FROM user_account u " +
                            "JOIN follow f on u.username = f.follower " +
                            "WHERE f.followee = ? ORDER BY username"
            );
            st.setString(1, MainClass.username);
            ResultSet result = st.executeQuery();
            SearchManager.printResultSet(result, 0);
            result.close();
        } catch (Exception ignored) {}
    }

    private static void yourFollows() {
        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return;
        }
        System.out.println("Users you're following:");
        try (conn) {
            PreparedStatement st = conn.prepareStatement(
                    "SELECT username, email FROM user_account u " +
                            "JOIN follow f on u.username = f.followee " +
                            "WHERE f.follower = ? ORDER BY username",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY
            );
            st.setString(1, MainClass.username);
            ResultSet result = st.executeQuery();
            SearchManager.printResultSet(result, 0);
            while (true) {
                System.out.println("Enter a number to select that user, or 0 to exit.");
                int selection = Integer.parseInt(MainClass.in.nextLine());
                if (selection == 0) {
                    break;
                } //Check that selection refers to an actual row
                if (selection < 0 || !result.absolute(selection)) {
                    System.out.println("Invalid selection.");
                    continue;
                }
                String followee_username = result.getString("username");
                String followee_email = result.getString("email");
                result.close(); //Current result is no longer useful
                showPublicProfile(followee_username, followee_email);
                System.out.println("Unfollow? 0=keep following, " +
                        Helpers.DELETE +  "=unfollow: ");
                if (Helpers.getOption(0, true) == Helpers.DELETE) {
                    PreparedStatement st2 = conn.prepareStatement(
                            "DELETE FROM follow WHERE follower = ? AND followee = ?"
                    ); //Only delete from follow (result.deleteRow deletes from user_account too)
                    st2.setString(1, MainClass.username);
                    st2.setString(2, followee_username);
                    if (st2.executeUpdate() == 1) {
                        System.out.println("Unfollowed.");
                    } else {
                        System.out.println("Error unfollowing!");
                    } //Reload result with updated list
                    result = st.executeQuery();
                    System.out.println("Remaining users you're following:");
                    SearchManager.printResultSet(result, 0);
                }
            }
            result.close();
        } catch (Exception ignored) {}
    }

    private static void emailSearch() {
        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return;
        }
        try (conn) {
            while (true) {
                System.out.println("Enter an email (exact, case sensitive), or nothing to exit.");
                String search_email = MainClass.in.nextLine();
                if (search_email.equals("")) {
                    break;
                } //Follower check must go inside left join to show username if not already following
                PreparedStatement st = conn.prepareStatement(
                        "SELECT username, follower FROM user_account u " +
                                "LEFT JOIN follow f ON u.username = f.followee " +
                                "AND f.follower = ? WHERE email = ?"
                );
                st.setString(1, MainClass.username);
                st.setString(2, search_email);
                ResultSet result = st.executeQuery();
                if (!result.next()) {
                    System.out.println("Email \"" + search_email + "\" not found.");
                    result.close();
                    continue;
                }
                String search_username = result.getString("username");

                showPublicProfile(search_username, search_email);
                if (search_username.equals(MainClass.username)) {
                    System.out.println("You cannot follow yourself.");
                    result.close();
                    continue;
                }
                if (result.getString("follower") != null) {
                    System.out.println("You're already following this user.");
                    System.out.println("(To unfollow, use the \"Your Follows\" menu)");
                    result.close();
                    continue;
                } //Only ask to follow if not already following
                result.close();
                System.out.println("Follow? 0=ignore, 1=add follow: ");
                if (Helpers.getOption(1) == 1) {
                    st = conn.prepareStatement("INSERT INTO " +
                            "follow(follower, followee) VALUES (?, ?)");
                    st.setString(1, MainClass.username);
                    st.setString(2, search_username);
                    if (st.executeUpdate() == 1) {
                        System.out.println("You're now following this user.");
                    } else {
                        System.out.println("Error adding follow!");
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
