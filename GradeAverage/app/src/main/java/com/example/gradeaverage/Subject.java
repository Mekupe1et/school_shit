package com.example.gradeaverage;

import android.app.Activity;
import android.content.Context;
import android.text.InputFilter;
import android.util.Pair;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Subject {
    private final static int maxLengthUnits = 1;
    private final static int maxLengthGrade = 3;
    private final Pair<Integer, Integer> validGradeRange = new Pair<>(0,100);
    private final Pair<Integer, Integer> validUnitsRange = new Pair<>(1,5);
    private String name;
    private EditText etUnits;
    private EditText etGrade;
    private TextView tvSubject;
    private Activity activity;
    public Subject(String subject, Activity activity){
        this.name = subject;
        this.activity = activity;

        createElements();
        addToView();

    }

    public void createElements(){
        tvSubject = createSubjectTextView(name);
        etGrade = createGradeEditText();
        etUnits = createUnitsEditText();
    }

    public void addToView(){
        LinearLayout mainLayout = activity.findViewById(R.id.subjectsLayout);

        LinearLayout subjectLayout = new LinearLayout(activity);

        subjectLayout.setOrientation(LinearLayout.HORIZONTAL);
        subjectLayout.addView(tvSubject);
        subjectLayout.addView(etGrade);
        subjectLayout.addView(etUnits);

        mainLayout.addView(subjectLayout);
    }

    public TextView createSubjectTextView(String subject) {
        TextView tvSubject = new TextView(activity);
        tvSubject.setText(subject);
        tvSubject.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        ));
        tvSubject.setGravity(android.view.Gravity.CENTER);
        tvSubject.setTextSize(18);
        return tvSubject;
    }
    public EditText createGradeEditText() {
        return createEditText("grade", maxLengthGrade);
    }
    public EditText createUnitsEditText() {
        return createEditText("units", maxLengthUnits);
    }

    public EditText createEditText(String type, int maxLen) {
        EditText etUnits = new EditText(activity);
        etUnits.setHint(type);
        etUnits.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        etUnits.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etUnits.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLen)});
        etUnits.setGravity(android.view.Gravity.CENTER);
        return etUnits;
    }

    public boolean hasError(){
        return errorHandler(etGrade, validGradeRange)
                || errorHandler(etUnits, validUnitsRange);
    }
    public boolean errorHandler(EditText et, Pair<Integer, Integer> validRange) {
        if(et.getText().toString().isEmpty()) {
            et.setError("No val has been inputted");
            et.requestFocus();
            return true;
        } else if (getIntFromEditText(et)<validRange.first || getIntFromEditText(et)>validRange.second) {
            et.setError("Val is outside of valid range");
            et.requestFocus();
            return true;
        }
        return false;
    }
    public int getIntFromEditText(EditText et){
        return Integer.parseInt(et.getText().toString());
    }
    public int getGradeInt(){
        return getIntFromEditText(etGrade);
    }
    public int getUnitInt(){
        return getIntFromEditText(etUnits);
    }

    public void resetVals(){
        etGrade.setText("");
        etUnits.setText("");
    }
}
