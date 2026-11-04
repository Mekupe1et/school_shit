package com.example.gradeaverage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
        Subject.addCalcAvg(() -> updateAvgNoChecks(tvResult));
        setupKeyboardNavigation();

        findViewById(R.id.btnCalc).setOnClickListener(v -> {
            double avg = updateAvg(tvResult);
            if (avg == -1) return;
            Toast.makeText(this,
                    "Calculating average...",
                    Toast.LENGTH_SHORT
            ).show();
            Intent i = new Intent(MainActivity.this, CertificateActivity.class);
            i.putExtra("avg", avg);
            i.putExtra("name", etName.getText().toString());
            startActivity(i);
            hideKeyboard(etName);
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
                setupKeyboardNavigation();
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

    private void setupKeyboardNavigation() {
        ArrayList<View> fields = new ArrayList<>();
        fields.add(findViewById(R.id.etName));

        for (Subject subject : subjects) {
            if (subject.tvSubject instanceof EditText) {
                fields.add(subject.tvSubject);
            }
            if (subject.etGrade != null) {
                fields.add(subject.etGrade);
            }
            if (subject.etUnits != null) {
                fields.add(subject.etUnits);
            }
        }

        for (int i = 0; i < fields.size(); i++) {
            View current = fields.get(i);
            if (current == null) continue;
            if (current.getId() == View.NO_ID) {
                current.setId(View.generateViewId());
            }

            View next = (i < fields.size() - 1) ? fields.get(i + 1) : null;
            if (next != null && next.getId() == View.NO_ID) {
                next.setId(View.generateViewId());
            }

            if (current instanceof EditText) {
                EditText editText = (EditText) current;
                editText.setImeOptions(next == null ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
                editText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_NEXT && next != null) {
                        next.requestFocus();
                        if (next instanceof EditText) {
                            ((EditText) next).setSelection(((EditText) next).getText().length());
                        }
                        return true;
                    }
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        calculateAverage();
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void calculateAverage() {
        TextView tvResult = findViewById(R.id.tvResult);
        double avg = updateAvg(tvResult);
        if (avg == -1) return;
        Toast.makeText(this, "Calculating average...", Toast.LENGTH_SHORT).show();
        EditText etName = findViewById(R.id.etName);
        Intent i = new Intent(MainActivity.this, CertificateActivity.class);
        i.putExtra("avg", avg);
        i.putExtra("name", etName.getText().toString());
        startActivity(i);
        hideKeyboard(etName);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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