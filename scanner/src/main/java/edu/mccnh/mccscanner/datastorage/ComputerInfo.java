package edu.mccnh.mccscanner.datastorage;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public abstract class ComputerInfo
{
    String model;
    String pcLevel;
    String usageScale;
    String datePurchased;
    String pcAge;
    String warrantyStatus;
    String notes;

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

    public String getNotes()
    {
        return notes;
    }
}
