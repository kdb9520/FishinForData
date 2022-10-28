import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchManager {
    public static void songSearchLoop() {
        System.out.println("If you would like to add to a collection, please enter its id, otherwise enter zero.");
        int collectionID = CollectionManager.showCollections(true);
        //int collectionID = Integer.parseInt(MainClass.in.nextLine());
        songSearchLoop(collectionID);
    }




    private static int searchMenuOption() {
        System.out.print("\r\nCurrent menu: Search (case sensitive)\r\n\r\n" +
                "1: Search by song title\r\n" +
                "2: Search by artist name\r\n" +
                "3: Search albums\r\n" +
                "4: Search by genre\r\n" +
                "0: Return to Previous Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(4);
    }



    private static ResultSet searchForMusic(String columnName, String keyword){
        Connection conn = Helpers.createConnection();
        ResultSet rs=null;
        if(conn==null){
            System.out.println("Something wrong with search Connection.");
            return rs;
        }
        try(conn){
            PreparedStatement st = conn.prepareStatement(
                            "select mr.title as song_title, artist_name, genre_name,\n"+
                            "mr.length, mr2.title as album_title,count(lbu.release_id) as listen_count, \n"+
                            "    mr.release_id as song_id, mr2.release_id as album_id \n"+
                            "FROM music_release mr \n"+
                            "JOIN artist_music am on mr.release_id = am.release_id \n"+
                            "JOIN song s on s.release_id=mr.release_id \n"+
                            "LEFT JOIN album_song a on s.release_id = a.song_id \n"+
                            "LEFT JOIN music_release mr2 on mr2.release_id = a.album_id \n"+
                            "LEFT JOIN listened_by_user lbu on mr.release_id = lbu.release_id \n"+
                            "WHERE " + columnName + " like '%" + keyword + "%' "+
                            "GROUP BY song_title, artist_name, genre_name, \n"+
                            "    mr.length, album_title, mr.release_id, mr2.release_id \n"+ 
                            "ORDER BY song_title asc, \n"+
                            "artist_name asc;"
                            //allows us to go through the ResultSet multiple times
                            ,ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
            //st.setString(1, columnName);
            //st.setString(2, keyword);

            rs=st.executeQuery();

        }catch(Exception e){
            System.out.println("Error in searchForMusic() : " + e.toString());
        }finally{}
        return rs;


    }

    /*Display the search results */
    public static void printResultSet(ResultSet rs) throws SQLException{
        int numOfColumns = rs.getMetaData().getColumnCount();

        System.out.println("");
        try{

            //The ResultSet has song_id and album_id as its last two
            //columns but we dont want to show them, hence the minus 2
            for(int i=1; i<=numOfColumns-2; i++){
                System.out.print(rs.getMetaData().getColumnName(i) + " | ");
            }
            System.out.println("");
            int index=1;
            while(rs.next()){
                System.out.print(index + ": ");
                for(int i=1; i<=numOfColumns-2; i++){
                    
                    System.out.print(rs.getString(i) + " | ");

                }
                index++;
                System.out.println("");
            }
        }catch(Exception e){
            System.out.println("Error is printResultSet : " + e.toString());
        }
    }


    //Will add a song or album to the user's collection
    public static void addMusicToCollection(int collectionID, String musicID){
        Connection conn = Helpers.createConnection();
        if(conn==null){
            System.out.println("Something wrong with connection in addMusicToCollection.");
        }
        try(conn){
            PreparedStatement st = conn.prepareStatement(
                            "INSERT INTO collection_release(collection_id, username, release_id) " + 
                            "VALUES(?, ?, ?) "
                            );
            st.setInt(1, collectionID);
            st.setString(2, MainClass.username);
            st.setString(3, musicID);

            int result=st.executeUpdate();
            if (result == 1) {
                System.out.println("Music successfully added to collection");
                //conn.commit();
            } else {
                System.out.println("Failed to add music to collection");
                conn.rollback();
            }

        }catch(Exception e){
            System.out.println("Error in addMusicToCollection() : " + e.toString());
        }
    }


    //Will play a song for the user and update the database given a valid release_id
    public static void playMusic(ResultSet rs, String music_release_id){
        Connection conn = Helpers.createConnection();
        if(conn==null){
            System.out.println("Something wrong with connection in playSong.");
        }
        try(conn){
            PreparedStatement st = conn.prepareStatement("INSERT INTO listened_by_user(username, release_id) " + 
                                    "VALUES(?, ?);"
                                    );
            
            st.setString(1, MainClass.username);
            st.setString(2, rs.getString(music_release_id));
            
            int result = st.executeUpdate();  

            if (result == 1) {
                System.out.println("Music successfully played");
                //conn.commit();
            } else {
                System.out.println("Failed to play music.");
                //Nothing to rollback
                conn.rollback();
            }
        
        }catch(Exception e){
            System.out.println("Error in playSong() : " + e.toString());

        }
    }


    /*Display the menu of what you can do after selecting a song/album.
    Important for musicType to be either "song" or "album", note the lowercasing.
    */
    private static int doSomethingWithSongMenu(ResultSet rs, String musicType) throws SQLException{
        System.out.println("\r\nWhat would you like to do with " + rs.getString(musicType+"_title") + " ?");
        System.out.print("1: Add " + musicType + " to your current collection\r\n" +
                        "2: Play " + musicType + "\r\n" +
                        "0: Return to Search Menu\r\n" +
                        "What would you like to do?: ");
        return Helpers.getOption(2);
    }

    public static void selectSong(int collectionID, ResultSet rs, String columnLabel, String musicType) throws SQLException{
        System.out.print("\r\nSelect a song number from above, or enter 0 to go back to Search Menu:");
        int musicNum = Integer.parseInt(MainClass.in.nextLine());
        //System.out.println(musicNum);

        if(musicNum>0){
            rs.first(); //ResultSet is currently set on the first row returned, which would be number 1.
            if(musicNum>1){
                //get ResultSet to the selected song
                for(int i=0; i<musicNum-1; i++){
                    rs.next();
                }
            }
            
            int option = doSomethingWithSongMenu(rs, musicType);
            switch(option){
                case 1:{
                    //add song to collection
                    addMusicToCollection(collectionID, rs.getString(columnLabel));
                    break;
                }

                case 2:{
                    //play the song or album
                    playMusic(rs, columnLabel);
                    break;
                }
            }

            
        }
    }



    public static void songSearchLoop(int collectionID) {
        /* Looping search based on input, allow songs that
        *   are found to be listened to and added to collection- if searching
        *   by album, the album can also be listened to and added */

        Connection conn = Helpers.createConnection();
        try(conn){

            int option;
            
            while((option = searchMenuOption())!=0){
                switch(option){
                    case 1:{ //by song title
                        System.out.print("What song are you looking for? : ");
                        String songName = MainClass.in.nextLine();
                        //mr.title is music_release.title 
                        ResultSet rs = searchForMusic("mr.title", songName);
                        printResultSet(rs);
                        selectSong(collectionID, rs, "song_id", "song");
                        //System.out.println(rs.getString(1));
                        break;
                    }
                    case 2:{ //by song artist name
                        System.out.print("What artist are you looking for? : ");
                        String artistName = MainClass.in.nextLine();
                        //artist_name is artist_music1.artist_name 
                        ResultSet rs = searchForMusic("artist_name", artistName);
                        printResultSet(rs);
                        selectSong(collectionID, rs, "song_id", "song");
                        break;

                    }
                    case 3:{ //by album
                        System.out.print("What album are you looking for? : ");
                        String albumName = MainClass.in.nextLine();
                        //mr2.title is music_release.title 
                        ResultSet rs = searchForMusic("mr2.title", albumName);
                        printResultSet(rs);
                        selectSong(collectionID, rs, "album_id", "album");
                        break;
                    }
                    case 4:{ //by genre
                        System.out.print("What genre are you looking for? : ");
                        String genre = MainClass.in.nextLine();
                        //genre_name is song1.genre_name 
                        ResultSet rs = searchForMusic("genre_name", genre);
                        printResultSet(rs);
                        selectSong(collectionID, rs, "song_id", "song");
                        break;
                    }
                }

        
            }

        }catch(Exception e){}
        finally{}
        
    }
}
