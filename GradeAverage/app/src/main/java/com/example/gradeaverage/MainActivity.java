package com.example.gradeaverage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        EditText etName = findViewById(R.id.etName);
        Subject.addCalcAvg(()->updateAvgNoChecks(tvResult));
        findViewById(R.id.btnCalc).setOnClickListener(v -> {
            double avg = updateAvg(tvResult);
            if(avg ==-1)return;
            Toast.makeText(this,
                    "Calculating average...",
                    Toast.LENGTH_SHORT
            ).show();
            Intent i = new Intent(MainActivity.this, CertificateActivity.class);
            i.putExtra("avg", avg);
            i.putExtra("name", etName.getText().toString());
            startActivity(i);
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
        findViewById(R.id.btnAddSubject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEditableSubject();
            }
        });
    }

    public void generateSubjects(String[] subjects) {
        for (String subject : subjects) {
            addSubject(subject);
        }
        addEditableSubject();
    }
    public void addSubject(String subject){
        subjects.add(new Subject(subject, this));
    }
    public void addEditableSubject(){
        subjects.add(new EditableSubject(this));
    }
    public double updateAvg(TextView tvResult){
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).hasError()) return -1;
        }
        return updateAvgNoChecks(tvResult);
    }
    public double updateAvgNoChecks(TextView tvResult){
        double sumGrades = 0;
        double sumUnits = 0;
        for (int i = 0; i < subjects.size(); i++) {
            sumGrades += subjects.get(i).getWeightedGrade();
            sumUnits += subjects.get(i).getUnitInt();
        }
        tvResult.setText("Weighted average: " + String.format("%.1f", sumGrades / sumUnits));
        return sumGrades / sumUnits;
    }
}