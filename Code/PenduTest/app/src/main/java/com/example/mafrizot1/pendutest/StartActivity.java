package com.example.mafrizot1.pendutest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

//import com.example.mafrizot1.pendutest.BTActivity;

/**
 * Created by mafrizot1 on 07/02/18.
 */

public class StartActivity extends AppCompatActivity {

    private Button btn_singleplayer;
    private Button btn_multiplayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_view);

        btn_singleplayer = findViewById(R.id.btn_singleplayer);

        btn_singleplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_multiplayer = findViewById(R.id.btn_multiplayer);

        btn_multiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(StartActivity.this, BTActivity.class);
                Intent intent = new Intent(StartActivity.this, BTActivity.class);
                startActivity(intent);
            }
        });
    }
}
