package edu.mccnh.mccscanner.datastorage;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class AdminComputerInfo extends ComputerInfo
{
    // Ignored columns:
    // NONE

    // Raw columns:
    // 0 Last Name
    // 1 First Name
    // 2 Department
    // 3 Computer Name
    // 4 Last Password Change
    // 5 Serial Number
    // 6 Model
    // 7 Date Purchased
    // 8 PC Level (1-3)
    // 9 Age of the PC
    // 10 Usage Scale (1-3)
    // 11 Warranty Status
    // 12 Number of Monitors
    // 13 Monitor Size(s)
    // 14 MAC Address (Wired)
    // 15 MAC Address (Wireless)
    // 16 Phone Extension
    // 17 Phone Type (12 or 6 Buttons)
    // 18 Re-Imaged
    // 19 Notes

    public AdminComputerInfo(String[] orderedAdminData)
    {
        lastName = orderedAdminData[0];
        firstName = orderedAdminData[1];
        department = orderedAdminData[2];
        computerName = orderedAdminData[3];
        lastPasswordChange = orderedAdminData[4];
        serialNumber = orderedAdminData[5];
        model = orderedAdminData[6];
        datePurchased = orderedAdminData[7];
        pcLevel = orderedAdminData[8];
        pcAge = orderedAdminData[9];
        usageScale = orderedAdminData[10];
        warrantyStatus = orderedAdminData[11];
        macAddressWired = orderedAdminData[12];
        macAddressWireless = orderedAdminData[13];
        lastReimage = orderedAdminData[14];
        notes = orderedAdminData[15];
    }

    private final String lastName;
    private final String firstName;
    private final String department;
    private final String computerName;
    private final String lastPasswordChange;
    private final String serialNumber;
    private final String macAddressWired;
    private final String macAddressWireless;
    private final String lastReimage;

    public String getLastName()
    {
        return lastName;
    }
    public String getFirstName()
    {
        return firstName;
    }
    public String getDepartment()
    {
        return department;
    }
    public String getComputerName()
    {
        return computerName;
    }
    public String getLastPasswordChange()
    {
        return lastPasswordChange;
    }
    public String getSerialNumber()
    {
        return serialNumber;
    }
    public String getMacAddressWired()
    {
        return macAddressWired;
    }
    public String getMacAddressWireless()
    {
        return macAddressWireless;
    }
    public String getLastReimage()
    {
        return lastReimage;
    }
}
