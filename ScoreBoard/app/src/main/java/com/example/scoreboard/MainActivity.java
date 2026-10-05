package com.example.scoreboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_SCORE_A = "scoreA";
    private static final String KEY_SCORE_B = "scoreB";
    private static final String KEY_GAME_OVER = "gameOver";

    int scoreA = 0;
    TextView tvScoreA;

    int scoreB = 0;
    TextView tvScoreB;
    boolean gameOver = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnWhistle = findViewById(R.id.btnWhistle);
        btnWhistle.setOnClickListener(v -> Toast.makeText(MainActivity.this, getString(R.string.toast_game_started), Toast.LENGTH_SHORT).show());

        tvScoreA = findViewById(R.id.tvScoreA);
        tvScoreB = findViewById(R.id.tvScoreB);

        // Restore state if available
        if (savedInstanceState != null) {
            scoreA = savedInstanceState.getInt(KEY_SCORE_A, 0);
            scoreB = savedInstanceState.getInt(KEY_SCORE_B, 0);
            gameOver = savedInstanceState.getBoolean(KEY_GAME_OVER, false);
            tvScoreA.setText(String.valueOf(scoreA));
            tvScoreB.setText(String.valueOf(scoreB));
        }

        // Team A buttons
        findViewById(R.id.btnA3).setOnClickListener(v -> changeScore(0, 3));
        findViewById(R.id.btnA2).setOnClickListener(v -> changeScore(0, 2));
        findViewById(R.id.btnA1).setOnClickListener(v -> changeScore(0, 1));
        findViewById(R.id.btnAn1).setOnClickListener(v -> changeScore(0, -1));

        // Team B buttons
        findViewById(R.id.btnB3).setOnClickListener(v -> changeScore(1, 3));
        findViewById(R.id.btnB2).setOnClickListener(v -> changeScore(1, 2));
        findViewById(R.id.btnB1).setOnClickListener(v -> changeScore(1, 1));
        findViewById(R.id.btnBn1).setOnClickListener(v -> changeScore(1, -1));

        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(v -> {
            scoreA = 0;
            scoreB = 0;
            tvScoreA.setText(getString(R.string.zero));
            tvScoreB.setText(getString(R.string.zero));
            gameOver = false;
        });

        Button btnEnd = findViewById(R.id.btnEnd);
        btnEnd.setOnClickListener(v -> {
            gameOver = true;
            if (scoreA > scoreB) {
                Toast.makeText(MainActivity.this, getString(R.string.team_a_won), Toast.LENGTH_SHORT).show();
            } else if (scoreB > scoreA) {
                Toast.makeText(MainActivity.this, getString(R.string.team_b_won), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.draw), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE_A, scoreA);
        outState.putInt(KEY_SCORE_B, scoreB);
        outState.putBoolean(KEY_GAME_OVER, gameOver);
    }

    private void changeScore(int team, int delta) {
        if (gameOver) {
            return;
        }
        if (team == 0) {
            scoreA += delta;
            if (scoreA < 0) {
                scoreA = 0;
                Toast.makeText(this, getString(R.string.toast_cannot_below_zero), Toast.LENGTH_SHORT).show();
            }
            tvScoreA.setText(String.valueOf(scoreA));
        } else {
            scoreB += delta;
            if (scoreB < 0) {
                scoreB = 0;
                Toast.makeText(this, getString(R.string.toast_cannot_below_zero), Toast.LENGTH_SHORT).show();
            }
            tvScoreB.setText(String.valueOf(scoreB));
        }
    }
}