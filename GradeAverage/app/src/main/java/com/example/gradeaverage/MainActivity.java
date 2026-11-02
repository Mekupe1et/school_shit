package com.example.gradeaverage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<Subject> subjects = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] subjectNames = {"Math", "English", "CS"};
        generateSubjects(subjectNames);
        TextView tvResult = findViewById(R.id.tvResult);
        findViewById(R.id.btnCalc).setOnClickListener(v -> {
            double sumGrades = 0;
            double sumUnits = 0;
            for (int i = 0; i < subjects.size(); i++) {
                if (subjects.get(i).hasError()) return;
                sumGrades += subjects.get(i).getGradeInt()*subjects.get(i).getUnitInt();
                sumUnits += subjects.get(i).getUnitInt();
            }
            Toast.makeText(this,
                    "Calculating average...",
                    Toast.LENGTH_SHORT
            ).show();
            tvResult.setText("Weight average: " + String.format("%.1f", sumGrades/sumUnits));
        });

        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < subjects.size(); i++) {
                    subjects.get(i).resetVals();
                }
                tvResult.setText("the average will show up here");
            }
        });
    }

    public void generateSubjects(String[] subjects) {
        for (String subject : subjects) {
            addSubject(subject);
        }
    }
    public void addSubject(String subject){
        subjects.add(new Subject(subject, this));
    }
}