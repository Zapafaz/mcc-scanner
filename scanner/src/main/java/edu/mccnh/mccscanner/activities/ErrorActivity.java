package edu.mccnh.mccscanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import edu.mccnh.mccscanner.R;

public class ErrorActivity extends AppCompatActivity
{
    private TextView errorTagView;
    private TextView errorMsgView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        errorTagView = findViewById(R.id.errorTagView);
        errorMsgView = findViewById(R.id.errorMsgView);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = getIntent();
        String[] errors = intent.getStringArrayExtra(MainActivity.EXTRA_ERROR_DISPLAY);
        errorTagView.setText(errors[0]);
        errorMsgView.setText(errors[1]);
    }

    public void handleClick(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
