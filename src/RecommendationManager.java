import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RecommendationManager {

    public static void recommendationMenu() {
        int option;
        while ((option = recommendationMenuOption()) != 0) {
            switch (option) {
                case 1: { // 50 most popular in last 30 days
                    topFiftyLastThirty();
                    break;
                }
                case 2: { // 50 most popular among friends
                    topFiftyAmongFriends();
                    break;
                }
                case 3: { // top 5 genres in past month
                    topFiveGenres();
                    break;
                }
                case 4: { // for you
//                    emailSearch();
                    break;
                }
            }
        }
    }

    private static int recommendationMenuOption() {
        System.out.print("\r\nCurrent menu: RECOMMENDATIONS\r\n\r\n" +
                "1: View the top 50 most popular songs in the last 30 days\r\n" +
                "2: View the top 50 most popular songs among my friends\r\n" +
                "3: View the top 5 most popular genres of the month\r\n" +
                "4: For you: Recommend songs to listen to based on your play history\r\n" +
                "0: Return to Main Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(4);
    }

    private static void topFiveGenres() {

        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return;
        }

        try (conn) {

            PreparedStatement st = conn.prepareStatement(
                    "select genre_name, count(lbu_pk) as listen_count " +
                            "from listened_by_user lbu " +
                            "left join ( " +
                            "select release_id, genre_name " +
                            "from song s " +
                            "UNION ALL " +
                            "select album_id as release_id, genre_name " +
                            "from album_genre " +
                            ") as t " +
                            "on lbu.release_id = t.release_id " +
                            "where EXTRACT(MONTH FROM listen_date) = EXTRACT(MONTH FROM now()) " +
                            "group by genre_name " +
                            "order by listen_count desc " +
                            "limit 5"
            );

            ResultSet rs = st.executeQuery();
            if (rs != null) {
                System.out.println("Top 5 genres this month");
                SearchManager.printResultSet(rs, 0);
            } else {
                System.out.println("No songs were found :(");
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static void topFiftyAmongFriends() {

        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return;
        }

        try (conn) {

            PreparedStatement st = conn.prepareStatement(
                "SELECT title, artist_name, COUNT(lbu_pk) AS listen_count " +
                "FROM listened_by_user lbu " +
                "JOIN follow f ON lbu.username=f.followee " +
                "LEFT JOIN music_release mr ON lbu.release_id = mr.release_id " +
                "LEFT JOIN artist_music am ON mr.release_id = am.release_id " +
                "WHERE follower=? " +
                "GROUP BY lbu.release_id, title, artist_name " +
                "ORDER BY listen_count DESC " +
                "LIMIT 50"
            );

            st.setString(1, MainClass.username);

            ResultSet rs = st.executeQuery();
            if (rs != null) {
                System.out.println("Top 50 songs among your friends");
                SearchManager.printResultSet(rs, 0);
            } else {
                System.out.println("No songs were found :(");
                System.out.println("Get some friends!");
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static void topFiftyLastThirty() {

        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return;
        }

        try (conn) {

            PreparedStatement st = conn.prepareStatement(
                "SELECT title, artist_name, COUNT(lbu_pk) AS listen_count " +
                "FROM listened_by_user lbu " +
                "JOIN music_release mr ON lbu.release_id = mr.release_id " +
                "JOIN artist_music am ON mr.release_id = am.release_id " +
                "WHERE listen_date > now() - INTERVAL '30 day' " +
                "GROUP BY title, lbu.release_id, artist_name " +
                "ORDER BY listen_count DESC " +
                "LIMIT 50"
            );

            ResultSet rs = st.executeQuery();
            if (rs != null) {
                System.out.println("Top 50 songs in the last 30 days");
                SearchManager.printResultSet(rs, 0);
            } else {
                System.out.println("No songs were found :(");
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
