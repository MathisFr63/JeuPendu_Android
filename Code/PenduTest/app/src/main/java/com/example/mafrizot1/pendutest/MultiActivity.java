package com.example.mafrizot1.pendutest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mafrizot1.pendutest.R;

import java.util.List;
import com.example.mafrizot1.pendutest.R;
import com.example.mafrizot1.pendutest.metier.JeuPendu;
import com.example.mafrizot1.pendutest.model.BluetoothConnectionService;

import android.content.Context;


/**
 * Created by audouard on 14/02/18.
 */

public class MultiActivity extends AppCompatActivity {

    private LinearLayout container;
    private TextView tv_typed_letters;
    private EditText et_letter;
    private ImageView iv_image;
    private Button btn_suggest;
    private JeuPendu jeu;
    private String word;

//    private BluetoothConnectionService bluetoothConnectionService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jeu = new JeuPendu(this.getApplicationContext());
        word = getIntent().getStringExtra("EXTRA_TEXT");
//        bluetoothConnectionService = (BluetoothConnectionService) getIntent().getSerializableExtra("EXTRA_BCS");
        Log.d("multiactivity word :", word);
        if (savedInstanceState != null) {
            jeu.setInstanceState(savedInstanceState.getString("currentWord"), savedInstanceState.getInt("nbLettersFound"), savedInstanceState.getInt("nbError"), savedInstanceState.getStringArrayList("lettersList"));
        }
        setContentView(R.layout.main_view);

        tv_typed_letters = findViewById(R.id.tv_typed_letters);
        iv_image = findViewById(R.id.iv_image);
        container = findViewById(R.id.ll_word);
        btn_suggest = findViewById(R.id.btn_suggest);
        et_letter = findViewById(R.id.et_letter);

        initGame(word);


        et_letter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_letter.getText().toString().equals(""))
                    return;

                Log.d("PenduTest", "ON TEXT CHANGED");
                String letterFromInput = et_letter.getText().toString().toUpperCase();

                if (!jeu.getListOfLetters().contains(letterFromInput.charAt(0))) {
                    jeu.addLetter(letterFromInput.charAt(0));
                    if (jeu.checkIfLetterIsInWord(letterFromInput))
                        checkIfLetterIsInWord(letterFromInput.charAt(0), jeu.getWord());

//                    Si la partie est gagnée
                    if (jeu.getFound() == jeu.getWord().length()) {
                        createWinDialog(true);
                        initGame(word);
                    }

//                    Si la lettre n'est pas dans le mot
                    if (!jeu.getWord().contains(letterFromInput)) {
                        jeu.AddError();
                        setImage(jeu.getError());
                    }

                    if (jeu.getError() == 6) {
                        createWinDialog(false);
                        initGame(word);
                    }

//                    Affichage des lettres entrées
                    showAllLetters();
                } else {
                    Toast.makeText(getApplicationContext(), "Vous avez déjà entré cette lettre", Toast.LENGTH_SHORT).show();
                }
                et_letter.setText("");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        btn_suggest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                createWordEntryDialog();
            }
        });

    }

    private void createWordEntryDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);


        alert.setTitle(R.string.suggest);
        alert.setMessage("Mot :");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);
        input.requestFocus();


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                jeu.suggestWord(input.getText().toString());
                Log.d("PenduTestMulti", "Appel du write sur le bluetooth");
//                bluetoothConnectionService.write(input.getText().toString().getBytes());
//                initGameWithoutWord();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        AlertDialog alertToShow = alert.create();
        alertToShow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertToShow.show();
    }

    private void refreshView() {
        iv_image.setBackgroundResource(R.drawable.image0);
        tv_typed_letters.setText("");

        container.removeAllViews();

        for (int i = 0; i < jeu.getWord().length(); i++) {
            TextView oneLetter = (TextView) getLayoutInflater().inflate(R.layout.text_view, null);
            container.addView(oneLetter);
        }
    }

    private void initGame(String word) {
        jeu.initGameWithWord(word);
        refreshView();
    }

    private void initGameWithoutWord() {
        jeu.initGameWithoutWord();
        refreshView();
    }

    private void checkIfLetterIsInWord(char letter, String word) {
        for (int i = 0; i < word.length(); i++) {
            if (letter == word.charAt(i)) {
                TextView tv = (TextView) container.getChildAt(i);
                Log.i("Test", "L'index est: " + i);
                tv.setText(String.valueOf(letter));
                jeu.addFound();
            }
        }
    }

    private void showAllLetters() {
        String chaine = "";
        List<Character> list = jeu.getListOfLetters();
        for (int i = 0; i < list.size(); i++) {
            chaine += list.get(i) + "\n";
        }

        if (!chaine.equals("")) {
            tv_typed_letters.setText(chaine);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (jeu.getWord() != null) {
            outState.putString("currentWord", jeu.getWord());
            outState.putInt("nbLettersFound", jeu.getFound());
            outState.putInt("nbError", jeu.getError());
            outState.putStringArrayList("lettersList", jeu.getListOfLettersString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        jeu.setInstanceState(savedInstanceState.getString("currentWord"), savedInstanceState.getInt("nbLettersFound"), savedInstanceState.getInt("nbError"), savedInstanceState.getStringArrayList("lettersList"));
    }

    private void setImage(int error) {
        switch (error) {
            case 1:
                iv_image.setBackgroundResource(R.drawable.image1);
                break;
            case 2:
                iv_image.setBackgroundResource(R.drawable.image2);
                break;
            case 3:
                iv_image.setBackgroundResource(R.drawable.image3);
                break;
            case 4:
                iv_image.setBackgroundResource(R.drawable.image4);
                break;
            case 5:
                iv_image.setBackgroundResource(R.drawable.image5);
                break;
            case 6:
                iv_image.setBackgroundResource(R.drawable.image6);
                break;
            default:
                iv_image.setBackgroundResource(R.drawable.image0);
                break;
        }
    }

    private void createWinDialog(boolean win) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (win)
            builder.setTitle("Vous avez gagné !");
        else {
            builder.setTitle("Vous avez perdu !");
            builder.setMessage("Le mot qu'il fallait trouvé était : " + jeu.getWord());
        }

//        !!! Lorsque l'on perd et que l'on clique à côté du bouton rejouer, la partie n'est pas réinitialiser et on peut continuer à jouer !!!
        //j'ai testé et pour moi la partie est réinitialiser

        builder.setPositiveButton(getResources().getString(R.string.replay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                initGameWithoutWord();
            }
        });

        builder.create().show();
    }

}




