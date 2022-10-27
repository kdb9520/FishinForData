import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CollectionManager {
    private static int collectionMenuOption() {
        System.out.print("\r\nCurrent menu: COLLECTIONS\r\n\r\n" +
                "1: View Your Collections (+ Remove Items)\r\n" +
                "2: Create and Fill New Collection\r\n" +
                "3: Add Items to Existing Collection\r\n" +
                Helpers.DELETE + ": ! Delete a Collection !\r\n" +
                "0: Return to Main Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(3, true);
    }

    public static void collectionMenu() {
        int option;
        while ((option = collectionMenuOption()) != 0) {
            int collectionID = -1;
            switch (option) {
                case 1: { // Your collections
                    showCollections(false);
                    break;
                }
                case 2: { // New collection
                    collectionID = createCollection();
                    if (collectionID == -1) {
                        break;
                    } // Only break if creation failed
                    // Intentional fall-through if successful
                }
                case 3: { // Add items
                    addItems(collectionID);
                    break;
                }
                case Helpers.DELETE: { // Delete collection
                    deleteCollection();
                    break;
                }
            }
        }
    }

    private static int showCollections(boolean select) {
        // TODO Implement showing and selecting collections
        System.out.println("(Collections, select " + select + ")");

        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return -1;
        }

        try (conn) {

            String currentUsername = MainClass.username;

            PreparedStatement nameSongCountDur = conn.prepareStatement(
                    "SELECT collection_name, count(cr.release_id) AS number_of_songs,\n" +
                            "sum(length) as total_length\n" +
                            "FROM collection AS c\n" +
                            "JOIN collection_release AS cr ON cr.collection_id=c.collection_id\n" +
                            "JOIN music_release AS mr ON mr.release_id=cr.release_id\n" +
                            "WHERE cr.username=c.username\n" +
                            "AND c.username= ?\n" +
                            "GROUP BY c.collection_id\n"
            );

            nameSongCountDur.setString(1, currentUsername);

            // TODO get number of songs in each collection and total song duration

            try {
                ResultSet result = null;
                result = nameSongCountDur.executeQuery();

                if (result != null) {
                    System.out.println("Your Collections: ");
                    while (result.next()) {
                        String collectionName = result.getString("collection_name");
                        int itemCount = result.getInt("number_of_songs");
                        int collDuration = result.getInt("total_length")/60;
                        System.out.println(collectionName + " contains "
                                + itemCount + " items, for a length of "
                                + collDuration + " minutes.");
                    }
                } else {
                    System.out.println("Could not find collections.");
                }
            } catch (Exception e) {
                System.out.println("Cannot get collections, an error occurred:");
                System.out.println(e.getMessage());
            }

            if (select) {
                System.out.println("Enter collection name to select it.");
                String name = MainClass.in.nextLine();

                PreparedStatement qry = conn.prepareStatement(
                        "SELECT collectionID, COUNT(*) AS size FROM collection WHERE collection_name = ?"
                );
                qry.setString(1, name);

                try {
                    ResultSet result = null;
                    result = qry.executeQuery();

                    int size = result.getInt("size");

                    if (size == 1) {
                        System.out.println("You selected: \n\t" + name);
                        System.out.println("Would you like to add items to the collection?" +
                                "\n\tEnter '1' for 'Yes' or '0' for 'No'.");
                        int add = Integer.parseInt(MainClass.in.nextLine());
                        return add == 1 ? result.getInt("collectionID") : -1;
                    } else if (size > 1) {
                        System.out.println("More than one collection found.");
                    } else {
                        System.out.println("Could not find collection.");
                    }
                } catch (Exception e) {
                    System.out.println("Cannot get collections, an error occurred:");
                    System.out.println(e.getMessage());
                }
            }

        } catch (Exception ignored) {}

        return -1;
    }

    private static int createCollection() {
        // TODO Implement creation of empty collection
        System.out.println("(Create collection)");

        System.out.println("Enter a name for your collection.");
        String newColName = MainClass.in.nextLine();

        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
            return -1;
        }

        try (conn) {

            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO collection(collection_name, username)" +
                            "VALUES (?, ?)"
            );

            st.setString(1, newColName);
            st.setString(2, MainClass.username);

            ResultSet result = null;

            try {

                result = st.executeQuery();

                if (result != null) {
                    System.out.println(newColName + " created!");
                    showCollections(false);
                } else {
                    System.out.println("Unable to create new collection.");
                }

            } catch (Exception e) {
                System.out.println("Could not create collection, an error occurred:");
                System.out.println(e.getMessage());
            }

        } catch (Exception ignored) {}

        return 1;
    }

    private static void addItems(int collectionID) {
        if (collectionID == -1) {
            collectionID = showCollections(true);
        }
        SearchManager.songSearchLoop(collectionID);
    }

    private static void deleteCollection() {
        int collectionID = showCollections(true);
        // TODO Implement confirmation and deletion
        System.out.println("(Delete collection, ID " + collectionID + ")");

        System.out.println("Are you sure you want to do this?" +
                "\n\tEnter '1' for 'Yes' or '0' for 'No'.");
        int option = Integer.parseInt(MainClass.in.next());
        if (option == 1) {

            Connection conn = Helpers.createConnection();
            if (conn == null) {
                System.out.println("Database connection error! Check Helpers.java");
            }

            try (conn) {
                PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM collection WHERE collectionID = ? and username = ?"
                );
                del.setInt(1, collectionID);
                del.setString(2, MainClass.username);

                try {

                    ResultSet result = null;
                    result = del.executeQuery();

                    if (result != null) {
                        System.out.println("Collection deleted!");
                        showCollections(false);
                    } else {
                        System.out.println("Unable to delete collection.");
                    }

                } catch (Exception e) {
                    System.out.println("Could not delete collection, an error occurred:");
                    System.out.println(e.getMessage());
                }

            } catch (Exception ignored) {}

        } else {
            System.out.println("Exiting the delete menu.");
        }
    }
}
