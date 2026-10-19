package com.example.pizzaorder;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnBack = findViewById(R.id.btnBack);

        Button btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paymentMethod = "";
                int id = ((RadioGroup)findViewById(R.id.paymentMethod)).getCheckedRadioButtonId();
                if (id == R.id.card) {
                    paymentMethod = "אשראי";
                } else if (id == R.id.cash) {
                    paymentMethod = "מזומן";
                }
                Toast.makeText(PaymentActivity.this,  "שולם ב" + paymentMethod, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        TextView tvSummary = findViewById(R.id.tvSummary);
        TextView tvPayment = findViewById(R.id.tvPaymentSize);

        Order order;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            order = getIntent().getParcelableExtra("order", Order.class);
        } else {
            order = null;
        }
        if (order == null) {
            Toast.makeText(this, "לא נמצאה הזמנה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String size = order.getSize();
        String crust = order.getCrust();
        int sum = order.getSum();

        tvSummary.setText("ההזמנה שלך: פיצה " + size + ", " + crust);
        tvPayment.setText("סכום: " + sum + " שח");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
    }
}