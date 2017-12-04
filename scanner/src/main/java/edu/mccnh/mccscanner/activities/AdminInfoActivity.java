package edu.mccnh.mccscanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.mccnh.mccscanner.Creator;
import edu.mccnh.mccscanner.R;
import edu.mccnh.mccscanner.Utility;
import edu.mccnh.mccscanner.datastorage.AdminComputerInfo;

/**
 * Created by Adam on 10/8/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

// TODO: maybe extend from another activity to avoid duplicated code w/ AcadInfoActivity
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
    private TextView monitorCount;
    private TextView monitorSize;
    private TextView macWired;
    private TextView macWireless;
    private TextView phoneExtension;
    private TextView phoneType;
    private TextView lastReimage;
    private TextView notes;
    private Button printButton;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_info);
        lastName = findViewById(R.id.admin_last_name);
        firstName = findViewById(R.id.admin_first_name);
        department = findViewById(R.id.admin_department);
        computerName = findViewById(R.id.admin_computer_name);
        lastPasswordChange = findViewById(R.id.admin_last_pw);
        serialNumber = findViewById(R.id.admin_serial_num);
        model = findViewById(R.id.admin_model);
        datePurchased = findViewById(R.id.admin_date_purchased);
        pcLevelAndUsageScale = findViewById(R.id.admin_level_and_usage);
        pcAge = findViewById(R.id.admin_comp_age);
        warrantyStatus = findViewById(R.id.admin_warranty);
        monitorCount = findViewById(R.id.admin_monitor_count);
        monitorSize = findViewById(R.id.admin_monitor_size);
        macWired = findViewById(R.id.admin_mac_wired);
        macWireless = findViewById(R.id.admin_mac_wireless);
        phoneExtension = findViewById(R.id.admin_phone_ext);
        phoneType = findViewById(R.id.admin_phone_type);
        lastReimage = findViewById(R.id.admin_last_reimage);
        notes = findViewById(R.id.admin_notes);
        printButton = findViewById(R.id.admin_print_button);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = getIntent();
        String[] orderedComputerInfo = intent.getStringArrayExtra(MainActivity.EXTRA_ORDERED_DATA);
        int id = intent.getIntExtra(MainActivity.EXTRA_ID_CODE, 0);
        if (orderedComputerInfo.length == Utility.ADMIN_ORDERED_SIZE)
        {
            infoToDisplay = new AdminComputerInfo(orderedComputerInfo, id);
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
            monitorCount.setText(infoToDisplay.getMonitorCount());
            monitorSize.setText(infoToDisplay.getMonitorSize());
            macWired.setText(infoToDisplay.getMacAddressWired());
            macWireless.setText(infoToDisplay.getMacAddressWireless());
            phoneExtension.setText(infoToDisplay.getPhoneExtension());
            phoneType.setText(infoToDisplay.getPhoneType());
            lastReimage.setText(infoToDisplay.getLastReimage());
            notes.setText(infoToDisplay.getNotes());
            fileName = infoToDisplay.getPdfFilePath();
            printButton.setText(getString(R.string.print_button, fileName));
        }
    }

    public void onPrintClick(View view)
    {
        optimizeForPrint();
        Creator creator = new Creator(fileName, infoToDisplay, this);
        try
        {
            creator.createPdf();
        }
        catch(Exception e)
        {

        }
    }

    private void optimizeForPrint()
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
