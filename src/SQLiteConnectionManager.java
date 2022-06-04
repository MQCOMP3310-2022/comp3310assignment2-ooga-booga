import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnectionManager {

    //private Connection wordleDBConn = null;
    private String databaseURL = "";
    
    private String wordleDropTableString = "DROP TABLE IF EXISTS wordlist;";
    private String wordleCreateString = 
          "CREATE TABLE wordlist (\n" 
        + "	id integer PRIMARY KEY,\n"
        + "	word text NOT NULL\n"
        + ");";
    
    private String validWordsDropTableString = "DROP TABLE IF EXISTS validWords;";
    private String validWordsCreateString = 
          "CREATE TABLE validWords (\n" 
        + "	id integer PRIMARY KEY,\n"
        + "	word text NOT NULL\n"
        + ");";

    //private String populateWordle;
    //private String populateValidWords;


    /**
     * Set the database file name in the sqlite project to use
     *
     * @param fileName the database file name
     */
    public SQLiteConnectionManager(String filename)
    {
        databaseURL = "jdbc:sqlite:sqlite/" + filename;
    }

    /**
     * Connect to a sample database
     *
     * @param fileName the database file name
     */
    public void createNewDatabase(String fileName) {

        try (Connection conn = DriverManager.getConnection(databaseURL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Check that the file has been cr3eated
     *
     * @return true if the file exists in the correct location, false otherwise. If no url defined, also false.
     */
    public boolean checkIfConnectionDefined(){
        if(databaseURL == ""){
            return false;
        }else{
            try (Connection conn = DriverManager.getConnection(databaseURL)) {
                if (conn != null) {
                    return true; 
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Create the table structures (2 tables, wordle words and valid words)
     *
     * @return true if the table structures have been created.
     */
    public boolean createWordleTables(){
        if(databaseURL != ""){
            try (   Connection conn = DriverManager.getConnection(databaseURL);
                    Statement stmt = conn.createStatement()
                ) 
            {
                if (conn != null) {
                    stmt.execute(wordleDropTableString);
                    stmt.execute(wordleCreateString);
                    stmt.execute(validWordsDropTableString);
                    stmt.execute(validWordsCreateString);
                    return true;  
                } 
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }
            
        }
        return false;
        
    }

    /**
     * Take an id and a word and store the pair in the valid words
     * @param id the unique id for the word
     * @param word the word to store
     */
    public void addValidWord(int id, String word){
        if(word != null){
            //string cleaning
            word=word.replaceAll("[^a-z]","");
            int wordLength = word.length();
            if(wordLength>4){
                //substring the word, could not be a valid word, but best we can do for now
                word.substring(0, 5);
            }
        }        

        String sql = "INSERT INTO validWords(id,word) VALUES(?,?)";

        try (Connection conn = DriverManager.getConnection(databaseURL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, word);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
    
    /**
     * get the entry in the validWords database
     * @param index the id of the word entry to get
     * @return
     */
    public String getWordAtIndex(){
        //gets a random word w/o tainted variable possibility from an outside call to this function
        //only tainting that could possible is within this function

        Integer randomWord = (int) Math.floor(Math.random()*(numberOfWords()+1));

        //finds that random word
        String sql = "SELECT word FROM validWords where id="+randomWord+";";
        String result = "";
        try (Connection conn = DriverManager.getConnection(databaseURL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet cursor = pstmt.executeQuery();
            if(cursor.next()){
                System.out.println("successful next curser sqlite");
                result = cursor.getString(1);
                result.toLowerCase();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("getWordAtIndex===========================");
        System.out.println("sql: " + sql);
        System.out.println("result: " + result);

        return result;
    }

    /**
     * @param guess the string to check if it is a valid word.
     * @return true if guess exists in the database, false otherwise
     */
    public boolean isValidWord(String guess)
    {
        //check to see if string is null or not 4 letters
        if 
        (
            guess == null ||
            guess.length() != 4
        ) 
        { return false; }

        //change string to ensure values are all lower case
        guess.toLowerCase();        

        //check to see if characters entered are all letters
        for (int i = 0; i < 4; i++) {
            if ((Character.isLetter(guess.charAt(i)) == false)) {
                return false;
            }
        }

        String sql = "SELECT count(id) as total FROM validWords WHERE word like'"+guess+"';";
        
        try (   Connection conn = DriverManager.getConnection(databaseURL);
                    PreparedStatement stmt = conn.prepareStatement(sql)
                ) 
            {
                if (conn != null) {
                    ResultSet resultRows  = stmt.executeQuery();
                    while (resultRows.next())
                    {
                        int result = resultRows.getInt("total");
                        System.out.println("Total found:" + result);
                        if(result >= 1)
                        {
                            return true;
                        } 
                        else
                        {
                            return false;
                        }
                    }
                     
                }
                return false;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }

    }

     /**
      * 
      * @return returns the number of words in the list
      */
     private int numberOfWords() {
        int result = 0;
        String sql = "SELECT count(*) as total FROM validWords;";
        try (Connection conn = DriverManager.getConnection(databaseURL);
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
        ResultSet resultRows  = stmt.executeQuery();
        while (resultRows.next()) {
            result = resultRows.getInt("total");
        }   
    
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }
}   
