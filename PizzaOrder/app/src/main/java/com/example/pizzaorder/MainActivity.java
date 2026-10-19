package com.example.pizzaorder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    RadioGroup rgSize;
    int sum = 0;
    private int rbSmallId;
    private int rbMediumId;
    private int rbLargeId;
    private Button btnSelectCrust;
    private int selectedCrustIndex = -1;
    private String selectedCrustName = null;
    private final String[] crustOptions = {"דק", "עבה"};
    private final String[] crustValues = {"דקה", "עבה"};

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

        Button btnOrder = findViewById(R.id.btnOrder);
        rgSize = findViewById(R.id.rgSize);
        btnSelectCrust = findViewById(R.id.btnSelectCrust);
        btnSelectCrust.setText("בחרו בצק");
        generateSizes();
        updateSum();

        rgSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSum();
            }
        });

        btnSelectCrust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("בחרו בצק");
                builder.setSingleChoiceItems(crustOptions, selectedCrustIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCrustIndex = which;
                        selectedCrustName = crustValues[which];
                        btnSelectCrust.setText("בצק: " + crustOptions[which]);
                        updateSum();
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedSizeId = rgSize.getCheckedRadioButtonId();
                String size;
                if (checkedSizeId == rbSmallId) {
                    size = "קטנה";
                } else if (checkedSizeId == rbMediumId) {
                    size = "בינונית";
                } else if (checkedSizeId == rbLargeId) {
                    size = "משפחתית";
                } else {
                    Toast.makeText(MainActivity.this, "בחרו גודל לפני ההזמנה", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedCrustName == null) {
                    Toast.makeText(MainActivity.this, "בחרו סוג בצק לפני ההזמנה", Toast.LENGTH_SHORT).show();
                    return;
                }

                Order order = new Order(sum, size, selectedCrustName);

                Toast.makeText(MainActivity.this, "נבחר: " + order.getSize() + ", " + order.getCrust(), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("אישור הזמנה");
                builder.setMessage("להזמין פיצה " + order.getSize() + " עם בצק " + order.getCrust() + "?");

                builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "ההזמנה התקבלה!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
                        intent.putExtra("order", order);
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("ביטול", null);
                builder.show();
            }
        });

        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("ניקוי בחירות");
                builder.setMessage("האם אתה בטוח שברצונך לנקות את הבחירות שלך?");
                builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rgSize.check(rbMediumId);
                        selectedCrustIndex = -1;
                        selectedCrustName = null;
                        btnSelectCrust.setText("בחרו בצק");
                        updateSum();
                    }
                });
                builder.setNegativeButton("ביטול", null);
                builder.show();
            }
        });
    }

    public void updateSum() {
        int sizeAdd = 0;
        if (rgSize.getCheckedRadioButtonId() == rbMediumId) {
            sizeAdd = 5;
        } else if (rgSize.getCheckedRadioButtonId() == rbLargeId) {
            sizeAdd = 12;
        }

        int crustAdd = 0;
        if ("עבה".equals(selectedCrustName)) {
            crustAdd = 7;
        }

        sum = 30 + sizeAdd + crustAdd;
        TextView paymentSizeLabel = findViewById(R.id.paymentSizeLabel);
        paymentSizeLabel.setText(sum + " שח");
    }

    public void generateSizes() {
        rgSize.removeAllViews();
        String[] priceAdditions = {"", "+5", "+12"};
        String[] sizes = {"קטנה", "בינונית", "משפחתית"};

        for (int i = 0; i < sizes.length; i++) {
            String size = sizes[i] + " " + priceAdditions[i];
            RadioButton radioButton = new RadioButton(this);
            int id = View.generateViewId();
            radioButton.setId(id);
            if (i == 0) rbSmallId = id;
            else if (i == 1) rbMediumId = id;
            else if (i == 2) rbLargeId = id;
            radioButton.setText(size);
            rgSize.addView(radioButton);
        }

        rgSize.check(rbMediumId);
    }
}