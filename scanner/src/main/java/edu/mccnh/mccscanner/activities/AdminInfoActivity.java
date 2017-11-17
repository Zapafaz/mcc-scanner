package edu.mccnh.mccscanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import edu.mccnh.mccscanner.R;
import edu.mccnh.mccscanner.Utility;
import edu.mccnh.mccscanner.datastorage.AdminComputerInfo;

/**
 * Created by Adam on 10/8/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class AdminInfoActivity extends AppCompatActivity
{
    private  AdminComputerInfo infoToDisplay;

    private TextView lastName;
    private TextView firstName;
    private TextView department;
    private TextView computerName;
    private TextView lastPasswordChange;
    private TextView serialNumber;
    private TextView model;
    private TextView datePurchased;
    private TextView pcLevelAndUsageScale;
    private TextView pcAge;
    private TextView warrantyStatus;
    private TextView macWired;
    private TextView macWireless;
    private TextView lastReimage;
    private TextView notes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_info);

        lastName = (TextView)findViewById(R.id.admin_last_name);
        firstName = (TextView)findViewById(R.id.admin_first_name);
        department = (TextView)findViewById(R.id.admin_department);
        computerName = (TextView)findViewById(R.id.admin_computer_name);
        lastPasswordChange = (TextView)findViewById(R.id.admin_last_pw);
        serialNumber = (TextView)findViewById(R.id.admin_serial_num);
        model = (TextView)findViewById(R.id.admin_model);
        datePurchased = (TextView)findViewById(R.id.admin_date_purchased);
        pcLevelAndUsageScale = (TextView)findViewById(R.id.admin_level_and_usage);
        pcAge = (TextView)findViewById(R.id.admin_comp_age);
        warrantyStatus = (TextView)findViewById(R.id.admin_warranty);
        macWired = (TextView)findViewById(R.id.admin_mac_wired);
        macWireless = (TextView)findViewById(R.id.admin_mac_wireless);
        lastReimage = (TextView)findViewById(R.id.admin_last_reimage);
        notes = (TextView)findViewById(R.id.admin_notes);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = getIntent();
        String[] orderedComputerInfo = intent.getStringArrayExtra(MainActivity.EXTRA_ORDERED_DATA);
        if (orderedComputerInfo.length == Utility.ADMIN_ORDERED_SIZE)
        {
            infoToDisplay = new AdminComputerInfo(orderedComputerInfo);
        }
        else
        {
            displayError("INVALID_FORMAT", "AdminInfoActivity.onCreate orderedComputerInfo array size does not match either Admin nor Academic");
        }
        if (infoToDisplay != null)
        {
            Utility.debugWriteArrayToLog("DISPLAY", orderedComputerInfo);
            lastName.setText(infoToDisplay.getLastName());
            firstName.setText(infoToDisplay.getFirstName());
            department.setText(infoToDisplay.getDepartment());
            computerName.setText(infoToDisplay.getComputerName());
            lastPasswordChange.setText(infoToDisplay.getLastPasswordChange());
            serialNumber.setText(infoToDisplay.getSerialNumber());
            model.setText(infoToDisplay.getModel());
            datePurchased.setText(infoToDisplay.getDatePurchased());
            pcLevelAndUsageScale.setText(getString(R.string.level_and_usage, infoToDisplay.getPcLevel(), infoToDisplay.getUsageScale()));
            pcAge.setText(infoToDisplay.getPcAge());
            warrantyStatus.setText(infoToDisplay.getWarrantyStatus());
            macWired.setText(infoToDisplay.getMacAddressWired());
            macWireless.setText(infoToDisplay.getMacAddressWireless());
            lastReimage.setText(infoToDisplay.getLastReimage());
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
