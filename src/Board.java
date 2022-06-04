import java.awt.Graphics;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
// import java.util.Scanner;
// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.SQLException;


public class Board {
    Grid grid;
    SQLiteConnectionManager wordleDatabaseConnection;
    int numberOfWords;
    int min = 0;
    double secretWordIndex;
    int wordIndex;
    private int keyCode;

    public Board(){
        wordleDatabaseConnection = new SQLiteConnectionManager("words.db");
        int setupStage = 0;

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined())
        {
            System.out.println("Wordle created and connected.");
            if(wordleDatabaseConnection.createWordleTables())
            {
                System.out.println("Wordle structures in place.");
                setupStage = 1;
            }
        }

        if(setupStage == 1)
        {
            //let's add some words to valid 4 letter words from the data.txt file

            try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
                String line;
                int i = 1;
                while ((line = br.readLine()) != null) {
                   //System.out.println(line);
                   wordleDatabaseConnection.addValidWord(i,line);
                   i++;
                }
                numberOfWords = i;
                setupStage = 2;
            }catch(IOException e)
            {
                System.out.println(e.getMessage());
            }

        }
        else{
            System.out.println("Not able to Launch. Sorry!");
        }

        grid = new Grid(6,4, wordleDatabaseConnection);
        String theWord = wordleDatabaseConnection.getWordAtIndex();
        grid.setWord(theWord);
    }

    public void resetBoard(){
        grid.reset();
    }

    void paint(Graphics g){
        grid.paint(g);
    }    

    public void keyPressed(KeyEvent e){

        keyCode = e.getKeyCode();
        switch(keyCode) {
            case KeyEvent.VK_ENTER:
                grid.keyPressedEnter();
                break;
            case KeyEvent.VK_BACK_SPACE:
                grid.keyPressedBackspace();
                break;
            case KeyEvent.VK_ESCAPE:
                grid.keyPressedEscape();
                String theWord = wordleDatabaseConnection.getWordAtIndex();
                grid.setWord(theWord);
        }

        if(!e.isShiftDown() && keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z){

            //sanitize e.getKeyChar()
            char letter = e.getKeyChar();
            if ((Character.isLetter(letter) == false)) { 
                System.out.println("Invalid character!");
            } else {
                grid.keyPressedLetter(letter);
            }
        }


        /*
        System.out.println("Key Pressed! " + e.getKeyCode());

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            
            System.out.println("Enter Key");
        }
        if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            grid.keyPressedBackspace();
            System.out.println("Backspace Key");
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            grid.keyPressedEscape();
            
            secretWordIndex = ( secretWordIndex + 1 ) % numberOfWords;
            String theWord = wordleDatabaseConnection.getWordAtIndex(wordIndex);
            grid.setWord(theWord);

            System.out.println("Escape Key");
        }
        if(!e.isShiftDown() && e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z){
            grid.keyPressedLetter(e.getKeyChar());
            System.out.println("Character Key");
        }

        */

    }
}
