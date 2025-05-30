
package com.example.sortinggame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView textHighScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textHighScore = findViewById(R.id.textHighScore);
        Button btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        loadHighScore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHighScore(); // Refresh high score when returning from game
    }

    private void loadHighScore() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        textHighScore.setText("High Score: " + highScore);
    }
}

