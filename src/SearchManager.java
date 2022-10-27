import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchManager {
    public static void songSearchLoop() {
        System.out.println("If you would like to add to a collection, please enter its id, otherwise enter zero.");
        int collectionID = Integer.parseInt(MainClass.in.nextLine());
        songSearchLoop(collectionID);
    }




    private static int searchMenuOption() {
        System.out.print("\r\nCurrent menu: Search (case sensitive)\r\n\r\n" +
                "1: Search by song title\r\n" +
                "2: Search by artist name\r\n" +
                "3: Search albums\r\n" +
                "4: Search by genre\r\n" +
                "0: Return to Main Menu\r\n" +
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
                            "mr.length, mr2.title as album_title, mr.release_id as song_id, mr2.release_id as album_id \n"+
                            //"    --,count(lbu.release_id)"+
                            "FROM music_release_test mr \n"+
                            "JOIN artist_music1 am on mr.release_id = am.release_id \n"+
                            "JOIN song_1 s on s.release_id=mr.release_id \n"+
                            "LEFT JOIN album_song1 a on s.release_id = a.song_id \n"+
                            "LEFT JOIN music_release_test mr2 on mr2.release_id = a.album_id \n"+
                            "--JOIN listened_by_user lbu on mr.release_id = lbu.release_id \n"+
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

    public static void printResultSet(ResultSet rs) throws SQLException{
        int numOfColumns = rs.getMetaData().getColumnCount();

        System.out.println("");
        try{

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

        }
    }


    public static void addSongToCollection(int collectionID, String songID){
        Connection conn = Helpers.createConnection();
        if(conn==null){
            System.out.println("Something wrong with connection in addSongToCollection.");
        }
        try(conn){
            PreparedStatement st = conn.prepareStatement(
                            "INSERT INTO collection_release(collection_id, username, release_id) " + 
                            "VALUES(?, ?, ?) "
                            );
            st.setInt(1, collectionID);
            st.setString(2, MainClass.username);
            st.setString(3, songID);

            int result=st.executeUpdate();
            if (result == 1) {
                System.out.println("Song successfully added to collection");
                conn.commit();
            } else {
                System.out.println("Failed to add song to collection");
                conn.rollback();
            }

        }catch(Exception e){
            System.out.println("Error in searchForMusic() : " + e.toString());
        }
    }


    public static void playSong(ResultSet rs){
        Connection conn = Helpers.createConnection();
        if(conn==null){
            System.out.println("Something wrong with connection in addSongToCollection.");
        }
        try(conn){
            PreparedStatement st = conn.prepareStatement("INSERT INTO listened_by_user(username, release_id) " + 
                                    "VALUES(?, ?);"
                                    );
            
            st.setString(1, MainClass.username);
            st.setString(2, rs.getString("song_id"));
            
            int result = st.executeUpdate();  

            if (result == 1) {
                System.out.println("Song successfully played");
                //conn.commit();
            } else {
                System.out.println("Failed to play song.");
                //Nothing to rollback
                conn.rollback();
            }
        
        }catch(Exception e){
            System.out.println("Error in playSong() : " + e.toString());

        }
    }


    private static int doSomethingWithSongMenu(ResultSet rs) throws SQLException{
        System.out.println("\r\nWhat would you like to do with " + rs.getString("song_title") + " ?");
        System.out.print("1: Add song to your current collection\r\n" +
                        "2: Play Song\r\n" +
                        "0: Return to Search Menu\r\n" +
                        "What would you like to do?: ");
        return Helpers.getOption(2);
    }

    public static void selectSong(int collectionID, ResultSet rs) throws SQLException{
        System.out.print("\r\nSelect a song number from above, or enter 0 to go back to Search Menu:");
        int songNum = Integer.parseInt(MainClass.in.nextLine());
        System.out.println(songNum);

        if(songNum>0){
            rs.first(); //ResultSet is currently set on the first row returned, which would be number 1.
            if(songNum>1){
                //get ResultSet to the selected song
                for(int i=0; i<songNum-1; i++){
                    rs.next();
                }
            }
            
            int option = doSomethingWithSongMenu(rs);
            switch(option){
                case 1:{
                    //add song to collection
                    addSongToCollection(collectionID, rs.getString("song_id"));
                    break;
                }

                case 2:{
                    playSong(rs);
                    break;
                }
            }

            
        }
    }



    public static void songSearchLoop(int collectionID) {
        /* TODO Implement looping search based on input, allow songs that
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
                        selectSong(collectionID, rs);
                        //System.out.println(rs.getString(1));
                        break;
                    }
                    case 2:{ //by song artist name
                        System.out.print("What artist are you looking for? : ");
                        String artistName = MainClass.in.nextLine();
                        //artist_name is artist_music1.artist_name 
                        ResultSet rs = searchForMusic("artist_name", artistName);
                        printResultSet(rs);
                        selectSong(collectionID, rs);
                        break;

                    }
                    case 3:{ //by album
                        System.out.print("What album are you looking for? : ");
                        String albumName = MainClass.in.nextLine();
                        //mr2.title is music_release.title 
                        ResultSet rs = searchForMusic("mr2.title", albumName);
                        printResultSet(rs);
                        rs.first();
                        break;
                    }
                    case 4:{ //by genre
                        System.out.print("What genre are you looking for? : ");
                        String genre = MainClass.in.nextLine();
                        //genre_name is song1.genre_name 
                        ResultSet rs = searchForMusic("genre_name", genre);
                        printResultSet(rs);
                        selectSong(collectionID, rs);
                        break;
                    }
                }

        
            }

        }catch(Exception e){}
        finally{}
        
    }
}
