package com.example.mylibrary;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    EditText etBook, etAuthor, etPages, etRead;
    TextView tvStatus;
    String loadedTitle = "";
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
        db = FirebaseFirestore.getInstance();

        etBook = findViewById(R.id.etBook);
        etAuthor = findViewById(R.id.etAuthor);
        etPages = findViewById(R.id.etPages);
        etRead = findViewById(R.id.etRead);
        tvStatus = findViewById(R.id.tvStatus);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            if(handleEt(etBook) == null) return;
            if(handleEt(etAuthor) == null) return;
            if(handleEt(etPages) == null) return;
            if(handleEt(etRead) == null) return;

            String title = etBook.getText().toString();
            String author = etAuthor.getText().toString();
            int pages = Integer.parseInt(etPages.getText().toString());
            int read = Integer.parseInt(etRead.getText().toString());
            Toast.makeText(MainActivity.this, "All Good! \nBook: " + title + "\nAuthor: " + author + "\nPages: " + pages, Toast.LENGTH_LONG).show();
            Book book = new Book(title, author, pages, read);

            tvStatus.setText("Saving...");

            db.collection("books").document("current").set(book)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            tvStatus.setText("saved on the cloud");
                            Toast.makeText(MainActivity.this, "the book has been saved", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            tvStatus.setText("svaing failed: " + e.getMessage());
                        }
                    });
        });
        Button btnLoad = findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(v -> {
            tvStatus.setText("loading...");

            db.collection("books").document("current").get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot doc) {
                            if (!doc.exists()) {
                                tvStatus.setText("no books saved yet");
                                return;
                            }
                            Book book = doc.toObject(Book.class);
                            if (book != null) {
                                etBook.setText(book.getTitle());
                                etAuthor.setText(book.getAuthor());
                                etPages.setText(String.valueOf(book.getPages()));
                                etRead.setText(String.valueOf(book.getRead()));
                                int percentage = book.getPages() > 0 ? (book.getRead() * 100 / book.getPages()) : 0;
                                tvStatus.setText("you have read " + book.getRead() + " pages out of " + book.getPages() + " pages(" + percentage + "%)");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            tvStatus.setText("loading failed: " + e.getMessage());
                        }
                    });        });

    }
    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().isEmpty();
    }
    private String handleEt(EditText editText) {
        if (isEmpty(editText)) {
            editText.setError("Please enter a value");
            editText.requestFocus();
            return null;
        } else {
            return editText.getText().toString();
        }
    }
}