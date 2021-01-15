package com.mahanthesh.africar.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mahanthesh.africar.R;
import com.mahanthesh.africar.model.JobRequest;

public class RideCompleteActivty extends AppCompatActivity {
    private Button completeButton;
    private JobRequest jobRequest;
    private TextView amountTextView, pickupTextView, dropTextView, timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_complete_activty);
        jobRequest = (JobRequest) getIntent().getSerializableExtra("RideInfo");
        amountTextView = findViewById(R.id.amountTextView);
        pickupTextView = findViewById(R.id.pickupTextView);
        dropTextView = findViewById(R.id.dropTextView);
        timeTextView = findViewById(R.id.timeTextView);
        completeButton = findViewById(R.id.submitButton);
        String price = " " + jobRequest.getRide_cost();
        amountTextView.setText(price);
        timeTextView.setText(jobRequest.getAcceptedTime());
        pickupTextView.setText("Pickup Location");
        dropTextView.setText("Drop Location");
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // completed the ride and push the ride to another info
                finish();
            }
        });

    }
}