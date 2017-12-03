package edu.mccnh.mccscanner.datastorage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public abstract class ComputerInfo
{
    String serialNumber;
    String model;
    String pcLevel;
    String usageScale;
    String datePurchased;
    String pcAge;
    String warrantyStatus;
    String monitorCount;
    String monitorSize;
    String macAddressWired;
    String macAddressWireless;
    String notes;
    int id;

    public ComputerInfo(int id)
    {
        this.id = id;
    }

    public String getPdfFilePath()
    {
        return id + "_" + new SimpleDateFormat("MMMdd-hh-mm-ss", Locale.US).format(Calendar.getInstance().getTime()) + ".pdf";
    }
    public String getSerialNumber()
    {
        return serialNumber;
    }
    public String getModel()
    {
        return model;
    }
    public String getDatePurchased()
    {
        return datePurchased;
    }
    public String getPcLevel()
    {
        return pcLevel;
    }
    public String getPcAge()
    {
        return pcAge;
    }
    public String getUsageScale()
    {
        return usageScale;
    }
    public String getWarrantyStatus()
    {
        return warrantyStatus;
    }
    public String getMonitorCount()
    {
        return monitorCount;
    }
    public String getMonitorSize()
    {
        return monitorSize;
    }
    public String getMacAddressWired()
    {
        return macAddressWired;
    }
    public String getMacAddressWireless()
    {
        return macAddressWireless;
    }
    public String getNotes()
    {
        return notes;
    }
}
