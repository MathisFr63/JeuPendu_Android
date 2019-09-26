package com.example.mafrizot1.pendutest.metier;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mafrizot1.pendutest.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by mafrizot1 on 07/02/18.
 */

public class JeuPendu {

    private boolean win;
    private String word;
    private int found, error;
    private List<Character> listOfLetters;
    private List<String> wordList;
    private Context context;

    public String getWord() {
        return word;
    }

    public int getError() {
        return error;
    }

    public boolean isWin() {
        return win;
    }

    public List<Character> getListOfLetters() {
        return listOfLetters;
    }

    public ArrayList<String> getListOfLettersString(){
        ArrayList<String> tmp = new ArrayList<>();

        for (char c : listOfLetters){
            tmp.add("" + c);
        }
        return tmp;
    }

    public int getFound() {
        return found;
    }



    public JeuPendu(Context context){
        this.context=context;
        initGame();

    }

    public JeuPendu(String word, boolean win, int found, int error, List<Character> listOfLetters, Context context){
        this.word = word;
        this.win = win;
        this.found = found;
        this.error = error;
        this.listOfLetters = listOfLetters;
        this.context = context;
    }

    public void initGame() {
        win = false;
        error = found = 0;
        generateWord();
        listOfLetters = new ArrayList<>();
    }

    public void initGameWithWord(String word) {
        win = false;
        error = found = 0;
        this.word = word;
        listOfLetters = new ArrayList<>();
    }

    public void initGameWithoutWord() {
        win = false;
        error = found = 0;
        listOfLetters = new ArrayList<>();
    }


    private void getListOfWords() {
        wordList = new ArrayList<>();
        try {
            //BufferedReader buffer = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() + "/pendu_liste.txt"));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(context.getAssets().open("pendu_liste.txt")));
            String line;
            while ((line = buffer.readLine()) != null) {
                wordList.add(line);
            }
        } catch (IOException e) {
            Log.e("MonJeu", "Erreur lors de la génération des mots : " + e.getMessage());
        }
    }

    private void generateWord() {
        if (wordList == null || wordList.isEmpty())
            getListOfWords();
        int random = (int) Math.floor(Math.random() * wordList.size());
        word = wordList.get(random).trim();
        //word = "ORDINATEUR";
    }


    public void setInstanceState(String currentWord, int nbLettersFound, int nbError, ArrayList<String> lettersList) {
        word = currentWord;
        found = nbLettersFound;
        error = nbError;

        listOfLetters.clear();
        for (String s : lettersList){
            listOfLetters.add(s.charAt(0));
        }
    }

    public boolean checkIfLetterIsInWord(String letter) {
        return word.contains(letter);
    }

    public void addFound() {
        found++;
    }

    public void AddError() {
        error++;
    }

    public void addLetter(char c) {
        listOfLetters.add(c);
    }

    public void suggestWord(String mot){
        this.word = mot.toUpperCase();
//        initGameWithoutWord();
    }
}