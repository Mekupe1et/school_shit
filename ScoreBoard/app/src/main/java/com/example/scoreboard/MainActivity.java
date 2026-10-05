package com.example.scoreboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
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
        btnWhistle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "המשחק התחיל!", Toast.LENGTH_SHORT).show();
            }
        });
        tvScoreA = findViewById(R.id.tvScoreA);

        Button btnA3 = findViewById(R.id.btnA3);
        Button btnA2 = findViewById(R.id.btnA2);
        Button btnA1 = findViewById(R.id.btnA1);
        Button btnAn1 = findViewById(R.id.btnAn1);

        btnA3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreA = scoreA + 3;
                tvScoreA.setText(String.valueOf(scoreA));
            }
        });

        btnA2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreA = scoreA + 2;
                tvScoreA.setText(String.valueOf(scoreA));
            }
        });

        btnA1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreA = scoreA + 1;
                tvScoreA.setText(String.valueOf(scoreA));
            }
        });
        btnAn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreA = scoreA - 1;
                if (scoreA < 0) {
                    scoreA = 0;
                    Toast.makeText(MainActivity.this, "אי אפשר לרדת מתחת לאפס", Toast.LENGTH_SHORT).show();
                }
                tvScoreA.setText(String.valueOf(scoreA));
            }
        });

        tvScoreB = findViewById(R.id.tvScoreB);

        Button btnB3 = findViewById(R.id.btnB3);
        Button btnB2 = findViewById(R.id.btnB2);
        Button btnB1 = findViewById(R.id.btnB1);
        Button btnBn1 = findViewById(R.id.btnBn1);

        btnB3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreB = scoreB + 3;
                tvScoreB.setText(String.valueOf(scoreB));
            }
        });

        btnB2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreB = scoreB + 2;
                tvScoreB.setText(String.valueOf(scoreB));
            }
        });

        btnB1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreB = scoreB + 1;
                tvScoreB.setText(String.valueOf(scoreB));
            }
        });
        btnBn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOver) {
                    return;
                }
                scoreB = scoreB - 1;
                if (scoreB < 0) {
                    scoreB = 0;
                    Toast.makeText(MainActivity.this, "אי אפשר לרדת מתחת לאפס", Toast.LENGTH_SHORT).show();
                }
                tvScoreB.setText(String.valueOf(scoreB));
            }
        });
        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoreA = 0;
                scoreB = 0;
                tvScoreA.setText("0");
                tvScoreB.setText("0");
                gameOver = false;
            }
        });
        Button btnEnd = findViewById(R.id.btnEnd);
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOver = true;
                if (scoreA > scoreB) {
                    Toast.makeText(MainActivity.this, "היואבים ניצחו!", Toast.LENGTH_SHORT).show();
                } else if (scoreB > scoreA) {
                    Toast.makeText(MainActivity.this, "הג'ונים ניצחו!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "תיקו!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}