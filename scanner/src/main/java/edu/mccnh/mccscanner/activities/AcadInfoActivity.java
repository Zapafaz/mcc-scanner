package edu.mccnh.mccscanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.mccnh.mccscanner.R;
import edu.mccnh.mccscanner.Utility;
import edu.mccnh.mccscanner.datastorage.AcadComputerInfo;

/**
 * Created by Adam on 10/8/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */
// TODO: maybe extend from another activity to avoid duplicated code w/ AdminInfoActivity
public class AcadInfoActivity extends AppCompatActivity
{
    private AcadComputerInfo infoToDisplay;
    private TextView roomArea;
    private TextView numberOfPcs;
    private TextView serialNumber;
    private TextView model;
    private TextView pcLevelAndUsageScale;
    private TextView pcAge;
    private TextView purchased;
    private TextView warranty;
    private TextView monitorCount;
    private TextView monitorSize;
    private TextView macWired;
    private TextView macWireless;
    private TextView notes;
    private Button printButton;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acad_info);
        roomArea = findViewById(R.id.acad_room_area);
        numberOfPcs = findViewById(R.id.acad_comp_count);
        serialNumber = findViewById(R.id.acad_serial_num);
        model = findViewById(R.id.acad_model);
        pcLevelAndUsageScale = findViewById(R.id.acad_level_and_usage);
        pcAge = findViewById(R.id.acad_comp_age);
        purchased = findViewById(R.id.acad_date_purchased);
        warranty = findViewById(R.id.acad_warranty);
        monitorCount = findViewById(R.id.acad_monitor_count);
        monitorSize = findViewById(R.id.acad_monitor_size);
        macWired = findViewById(R.id.acad_mac_wired);
        macWireless = findViewById(R.id.acad_mac_wireless);
        notes = findViewById(R.id.acad_notes);
        printButton = findViewById(R.id.acad_print_button);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = getIntent();
        String[] orderedComputerInfo = intent.getStringArrayExtra(MainActivity.EXTRA_ORDERED_DATA);
        int id = intent.getIntExtra(MainActivity.EXTRA_ID_CODE, 0);
        if (orderedComputerInfo.length == Utility.ACAD_ORDERED_SIZE)
        {
            infoToDisplay = new AcadComputerInfo(orderedComputerInfo, id);
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
            serialNumber.setText(infoToDisplay.getSerialNumber());
            model.setText(infoToDisplay.getModel());
            pcLevelAndUsageScale.setText(getString(R.string.level_and_usage, infoToDisplay.getPcLevel(), infoToDisplay.getUsageScale()));
            pcAge.setText(infoToDisplay.getPcAge());
            purchased.setText(infoToDisplay.getDatePurchased());
            warranty.setText(infoToDisplay.getWarrantyStatus());
            monitorCount.setText(infoToDisplay.getMonitorCount());
            monitorSize.setText(infoToDisplay.getMonitorSize());
            macWired.setText(infoToDisplay.getMacAddressWired());
            macWireless.setText(infoToDisplay.getMacAddressWireless());
            notes.setText(infoToDisplay.getNotes());
            fileName = infoToDisplay.getPdfFilePath();
            printButton.setText(getString(R.string.print_button, fileName));
        }
    }

    public void onPrintClick(View view)
    {
        ViewGroup layout = (ViewGroup)printButton.getParent();
        if (layout != null)
        {
            layout.removeView(printButton);
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
