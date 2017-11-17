package edu.mccnh.mccscanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import edu.mccnh.mccscanner.R;
import edu.mccnh.mccscanner.Utility;
import edu.mccnh.mccscanner.datastorage.AcadComputerInfo;

/**
 * Created by Adam on 10/8/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class AcadInfoActivity extends AppCompatActivity
{
    private AcadComputerInfo infoToDisplay;
    private TextView roomArea;
    private TextView numberOfPcs;
    private TextView model;
    private TextView pcLevelAndUsageScale;
    private TextView pcAge;
    private TextView purchased;
    private TextView warranty;
    private TextView notes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acad_info);
        roomArea = (TextView)findViewById(R.id.acad_room_area);
        numberOfPcs = (TextView)findViewById(R.id.acad_comp_count);
        model = (TextView)findViewById(R.id.acad_model);
        pcLevelAndUsageScale = (TextView)findViewById(R.id.acad_level_and_usage);
        pcAge = (TextView)findViewById(R.id.acad_comp_age);
        purchased = (TextView)findViewById(R.id.acad_date_purchased);
        warranty = (TextView)findViewById(R.id.acad_warranty);
        notes = (TextView)findViewById(R.id.acad_notes);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = getIntent();
        String[] orderedComputerInfo = intent.getStringArrayExtra(MainActivity.EXTRA_ORDERED_DATA);
        if (orderedComputerInfo.length == Utility.ACAD_ORDERED_SIZE)
        {
            infoToDisplay = new AcadComputerInfo(orderedComputerInfo);
        }
        else
        {
            displayError("INVALID_FORMAT", "AcadInfoActivity.onCreate orderedComputerInfo array size does not match Acad info size");
        }
        if (infoToDisplay != null)
        {
            Utility.debugWriteArrayToLog("DISPLAY", orderedComputerInfo);
            roomArea.setText(infoToDisplay.getRoomArea());
            numberOfPcs.setText(infoToDisplay.getNumberOfComputers());
            model.setText(infoToDisplay.getModel());
            pcLevelAndUsageScale.setText(getString(R.string.level_and_usage, infoToDisplay.getPcLevel(), infoToDisplay.getUsageScale()));
            pcAge.setText(infoToDisplay.getPcAge());
            purchased.setText(infoToDisplay.getDatePurchased());
            warranty.setText(infoToDisplay.getWarrantyStatus());
            notes.setText(infoToDisplay.getNotes());
        }
    }

    // Displays the error to the user via new activity
    private void displayError(String tag, String error)
    {
        Log.d(tag, error);
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra(MainActivity.EXTRA_ERROR_DISPLAY, new String[] {tag, error});
        startActivity(intent);
    }
}
