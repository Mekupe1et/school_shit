package com.example.pizzaorder;

import android.content.Intent;
import android.os.Build;
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

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btnPayment = findViewById(R.id.btnPay);
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, PaymentActivity.class);
                intent.putExtra("order", order);
                startActivity(intent);
            }
        });

    }
}