package com.example.gradeaverage;

import android.app.Activity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Pair;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.function.Function;

public class Subject {
    protected final static int maxLengthUnits = 1;
    protected final static int maxLengthGrade = 3;
    protected final Pair<Integer, Integer> validGradeRange = new Pair<>(0,100);
    protected final Pair<Integer, Integer> validUnitsRange = new Pair<>(1,5);
    protected String name;
    protected EditText etUnits;
    protected EditText etGrade;
    protected TextView tvSubject;
    protected Activity activity;
    private static Runnable calcAvg;
    public Subject(String subject, Activity activity){
        this.name = subject;
        this.activity = activity;

        createElements();
        addToView();

    }
    public static void addCalcAvg(Runnable calcAvg) {
        Subject.calcAvg = calcAvg;
    }

    public void createElements(){
        tvSubject = createSubjectTextView(name);
        etGrade = createGradeEditText();
        etUnits = createUnitsEditText();
    }

    public String getSubjectName(){
        return name;
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
        EditText et = new EditText(activity);
        et.setHint(type);
        et.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLen)});
        et.setGravity(android.view.Gravity.CENTER);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (calcAvg != null) calcAvg.run();
            }
        });
        return et;
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
        try {
            return Integer.parseInt(et.getText().toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    public int getGradeInt(){
        return getIntFromEditText(etGrade);
    }
    public int getUnitInt(){
        return getGradeInt()==-1||getIntFromEditText(etUnits)==-1?0:getIntFromEditText(etUnits);
    }

    public void resetVals(){
        etGrade.setText("");
        etUnits.setText("");
    }
    public int getWeightedGrade(){
        int grade = getGradeInt();
        int units = getUnitInt();
        if (grade == -1 || units == -1) return 0;
        return getGradeInt()*getUnitInt()+((getUnitInt()-3)*10)*getUnitInt();
    }
}
