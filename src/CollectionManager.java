import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CollectionManager {
    private static int collectionMenuOption() {
        System.out.print("\r\nCurrent menu: COLLECTIONS\r\n\r\n" +
                "1: View Your Collections\r\n" +
                "2: Create and Fill New Collection\r\n" +
                "3: Add Items to Existing Collection\r\n" +
                "4: View Items in an Existing Collection (+ Listen or Delete Items)\r\n" +
                "5: Listen to an Existing Collection\r\n" +
                "6: Rename an Existing Collection \r\n" +
                Helpers.DELETE + ": ! Delete a Collection !\r\n" +
                "0: Return to Main Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(6, true);
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
                case 4: { // remove items
                    viewItems();
                    break;
                }
                case 5: {
                    listenToCollection();
                    break;
                }
                case 6: {
                    renameCollection();
                    break;
                }
                case Helpers.DELETE: { // Delete collection
                    deleteCollection();
                    break;
                }
            }
        }
    }

    public static int showCollections(boolean select) {
        //System.out.println("(Collections, select " + select + ")");

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
                            "LEFT JOIN collection_release AS cr ON cr.collection_id=c.collection_id\n" +
                            "LEFT JOIN music_release AS mr ON mr.release_id=cr.release_id\n" +
                            "WHERE c.username= ?\n" +
                            "GROUP BY c.collection_name, c.collection_id\n"
            );

            nameSongCountDur.setString(1, currentUsername);


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
                        "SELECT collection_id, COUNT(*) AS size FROM collection WHERE " +
                                "collection_name = ? GROUP BY collection_id"
                );
                qry.setString(1, name);

                try {
                    ResultSet result = null;
                    result = qry.executeQuery();
                    result.next();
                    int size = result.getInt("size");

                    if (size == 1) {
                        System.out.println("You selected: \n\t" + name);
//                        System.out.println("Would you like to add items to the collection?" +
//                                "\n\tEnter '1' for 'Yes' or '0' for 'No'.");
//                        int add = Integer.parseInt(MainClass.in.nextLine());
//                        return add == 1 ? result.getInt("collection_id") : -1;
                        return result.getInt("collection_id");
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

    public static void renameCollection() {

        int collectionID = showCollections(true);

        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
        }

        System.out.println("Enter new name:");
        String newName = MainClass.in.nextLine();

        try(conn) {
            PreparedStatement st = conn.prepareStatement(
                    "UPDATE collection SET collection_name = ? WHERE collection_id=? AND username=?"
            );

            st.setString(1, newName);
            st.setInt(2, collectionID);
            st.setString(3, MainClass.username);

            try {

                int rs = 0;
                rs = st.executeUpdate();

                if (rs != 0) {
                    System.out.println("Successfully updated the collection!");
                } else {
                    System.out.println("Not able to update the collection!");
                }

            } catch (Exception e) {
                System.out.println("Error updating:");
                System.out.println(e);
            }

        } catch (Exception ignored) {}
    }

    private static void listenToCollection() {
        int collectionID = showCollections(true);

        Connection conn = Helpers.createConnection();
        if (conn == null) {
            System.out.println("Database connection error! Check Helpers.java");
        }

        try(conn) {
            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO listened_by_user(username, release_id)\n" +
                    "SELECT username, release_id FROM collection_release\n" +
                            "WHERE collection_id=? and username=?"
            );

            st.setInt(1, collectionID);
            st.setString(2, MainClass.username);

            try {

                int rs = 0;
                rs = st.executeUpdate();

                if (rs != 0) {
                    System.out.println("Successfully listened to the collection!");
                } else {
                    System.out.println("Not able to listen to the collection!");
                }

            } catch (Exception e) {
                System.out.println("Error listening:");
                System.out.println(e);
            }

        } catch (Exception ignored) {}
    }

    private static int createCollection() {
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
                    "INSERT INTO collection(collection_name, username)\n" +
                            "VALUES (?, ?)"
            );

            st.setString(1, newColName);
            st.setString(2, MainClass.username);

            int result=0;

            result = st.executeUpdate();

            if (result == 1) {
                System.out.println(newColName + " created!");
                //shows updated list of collections
                showCollections(false);

                //get the id for the new collection to return
                st = conn.prepareStatement(
                        "SELECT collection_id FROM collection " + 
                        "WHERE username = ? AND collection_name = ? "
                    );
                st.setString(1, MainClass.username);
                st.setString(2, newColName);

                ResultSet rs = st.executeQuery();
                rs.next();
                return rs.getInt("collection_id");
            } else {
                System.out.println("Unable to create new collection.");
                conn.rollback();
                return -1;
            }

        } catch (Exception e) {
            System.out.println("Could not create collection, an error occurred:");
            System.out.println(e.getMessage());
            return -1;
        }

        //return -1;
    }

    private static void addItems(int collectionID) {
        if (collectionID == -1) {
            collectionID = showCollections(true);
        }
        SearchManager.songSearchLoop(collectionID);
    }



    private static void viewItems() {
        //users selects their colelction they want to view
        int collectionID = showCollections(true);

        Connection conn = Helpers.createConnection();
        if(conn==null){
            System.out.println("Something wrong with connection in viewItems.");
        }
        try(conn){
            //get all music in a single collection
            PreparedStatement st = conn.prepareStatement(
                    "SELECT collection_name, mr.title, cr.release_id\n" +
                            "FROM collection AS c\n" +
                            "LEFT JOIN collection_release AS cr ON cr.collection_id=c.collection_id\n" +
                            "LEFT JOIN music_release AS mr ON mr.release_id=cr.release_id\n" +
                            "WHERE c.collection_id = ? AND c.username = ?\n" +
                            "GROUP BY c.collection_name, mr.title, cr.release_id, c.collection_id\n"
                            //allows us to go through the ResultSet multiple times
                            ,ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY
            );
            st.setInt(1, collectionID);
            st.setString(2, MainClass.username);

            ResultSet result = st.executeQuery();

            if (result != null) {
                System.out.println("Items in the collection:");
                int numOfColumns = result.getMetaData().getColumnCount();

                System.out.println("");
                try{

                    //Display the music in the collection
                    for(int i=1; i<=numOfColumns; i++){
                        System.out.print(result.getMetaData().getColumnName(i) + " | "); //print out column headers
                    }

                    System.out.println("");
                    int index=1;
                    //print data points
                    while(result.next()){ 
                        System.out.print(index + ": ");
                        for(int i=1; i<=numOfColumns; i++){
                            System.out.print(result.getString(i) + " | ");
                        }
                        index++;
                        System.out.println("");
                    }
                }catch(Exception e){
                    System.out.println("Error is printResultSet : " + e.toString());
                }

                //pass control to selectFromCollection function
                selectFromCollection(collectionID, result);
                
            } else {
                System.out.println("Failed to add music to collection");
                conn.rollback();
            }

        }catch(Exception e){
            System.out.println("Error in addMusicToCollection() : " + e.toString());
        }
    }


    private static int songOptions(){
        System.out.print("\r\nCurrent menu: Music Selection\r\n\r\n" +
                "1: Play Music\r\n" +
                Helpers.DELETE + ": ! Delete Song from Collection !\r\n" +
                "0: Return to Previous Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(1, true);
    }

    private static void selectFromCollection(int collectionID, ResultSet release) {
        System.out.print("\r\nSelect a song number from above, or enter 0 to go back:");
        int musicNum = Integer.parseInt(MainClass.in.nextLine());
        //System.out.println(musicNum);

        if (musicNum != 0) {
            try{
                //Get the resultSet to be on the row index of whichever song the user chose
                if(musicNum>0){
                    release.first(); //ResultSet is currently set on the first row returned, which would be number 1.
                    if(musicNum>1){
                        //get ResultSet to the row of the selected song
                        for(int i=0; i<musicNum-1; i++){
                            release.next();
                        }
                    }
                
                    System.out.println("\nSelected item: " + release.getString("title"));
                    System.out.println("Choose what to do with this item:");
                    int option = songOptions();
                    switch (option) {
                        case 0: {
                            break;
                        }
                        case 1: {
                            //play music already implemented in SearchManager
                            SearchManager.playMusic(release, "release_id");
                            break;
                        }
                        case Helpers.DELETE: {
                            deleteItem(collectionID, release.getString("release_id"));
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
            catch(Exception e){
                System.out.println("Exception in selectFromCollection: " + e);
            }
        }
    }

    private static void deleteItem(int collectionID, String releaseID) {

        System.out.println("(Delete collection item, ID " + releaseID + ")");

        System.out.println("Are you sure you want to do this?" +
                "\n\tEnter '1' for 'Yes' or '0' for 'No'.");
        int option = Helpers.getOption(1, true);
        if (option == 1) {

            Connection conn = Helpers.createConnection();
            if (conn == null) {
                System.out.println("Database connection error! Check Helpers.java");
            }

            try (conn) {
                PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM collection_release WHERE collection_id=? and username=? and release_id=?"
                );
                del.setInt(1, collectionID);
                del.setString(2, MainClass.username);
                del.setString(3, releaseID);

                try {

                    int result = 0;
                    result = del.executeUpdate();

                    if (result != 0) {
                        System.out.println("Item deleted from collection!");
                        showCollections(false);
                    } else {
                        System.out.println("Unable to delete item from collection.");
                        conn.rollback();
                    }

                } catch (Exception e) {
                    System.out.println("Could not delete item, an error occurred:");
                    System.out.println(e.getMessage());
                }

            } catch (Exception ignored) {}

        } else {
            System.out.println("Exiting the delete menu.");
        }

    }

    private static void deleteCollection() {
        int collectionID = showCollections(true);
        System.out.println("(Delete collection, ID " + collectionID + ")");

        System.out.println("Are you sure you want to do this?" +
                "\n\tEnter '1' for 'Yes' or '0' for 'No'.");
        int option = Helpers.getOption(1, true);
        if (option == 1) {

            Connection conn = Helpers.createConnection();
            if (conn == null) {
                System.out.println("Database connection error! Check Helpers.java");
            }

            try (conn) {
                PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM collection WHERE collection_id = ? and username = ?"
                );
                del.setInt(1, collectionID);
                del.setString(2, MainClass.username);

                try {

                    int result = 0;
                    result = del.executeUpdate();

                    if (result != 0) {
                        System.out.println("Collection deleted!");
                        showCollections(false);
                    } else {
                        System.out.println("Unable to delete collection.");
                        conn.rollback();
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
