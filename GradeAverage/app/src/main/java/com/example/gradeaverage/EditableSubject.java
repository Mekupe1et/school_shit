package com.example.gradeaverage;

import android.app.Activity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditableSubject extends Subject{

    public EditableSubject(Activity activity) {
        super("", activity);
        tvSubject = new EditText(activity);
    }
    @Override
    public TextView createSubjectTextView(String subject) {
        EditText tvSubject = new EditText(activity);
        tvSubject.setHint("Subject");
        tvSubject.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        ));
        tvSubject.setGravity(android.view.Gravity.CENTER);
        tvSubject.setTextSize(18);
        return tvSubject;
    }
    @Override
    public String getSubjectName(){
        return tvSubject.getText().toString();
    }

}
