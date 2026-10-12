package com.example.truefalsequiz;

import android.content.Intent;
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
    String[] questions = {
        "did Albert Einstein failed mathematics as a student.",
        "The sky is green."
    };
    boolean[] answers = { false, false };
    int currentQuestion = 0;
    TextView tvQuestion;
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


        tvQuestion = findViewById(R.id.textView);
        tvQuestion.setText(questions[currentQuestion]);

        Button btnTrue = findViewById(R.id.btnTrue);
        btnTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean userAnswer = true;

                boolean isCorrect;
                if (userAnswer == answers[currentQuestion]) {
                    isCorrect = true;
                } else {
                    isCorrect = false;
                }

                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("isCorrect", isCorrect);
                startActivityForResult(intent, 1);
            }
        });
        Button btnFalse = findViewById(R.id.btnFalse);
        btnFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean userAnswer = false;

                boolean isCorrect;
                if (userAnswer == answers[currentQuestion]) {
                    isCorrect = true;
                } else {
                    isCorrect = false;
                }

                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("isCorrect", isCorrect);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            boolean next = data.getBooleanExtra("next", false);
            if (next) {
                currentQuestion = (currentQuestion + 1) % questions.length;
                tvQuestion.setText(questions[currentQuestion]);
            }
        }
    }
}