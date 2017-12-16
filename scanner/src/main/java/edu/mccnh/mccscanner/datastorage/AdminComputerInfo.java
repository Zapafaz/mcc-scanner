package edu.mccnh.mccscanner.datastorage;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class AdminComputerInfo extends ComputerInfo
{
    public static final int VALID_CODE_LOWER = 10000;
    public static final int VALID_CODE_UPPER = 20000;
    public static final int SHEET_ID = 0;
    public static final int RAW_SIZE = 20;
    public static final int ORDERED_SIZE = 20;
    public static final int[] IGNORED_COLS = null;

    // Ignored columns:
    // NONE...anymore

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
    // 12 Monitor Count
    // 13 Monitor Size(s)
    // 14 MAC Address (Wired)
    // 15 MAC Address (Wireless)
    // 16 Phone Extension
    // 17 Phone Type (12 or 6 Buttons)
    // 18 Re-Imaged
    // 19 Notes
    // TOTAL COUNT: 20

    public AdminComputerInfo(String[] orderedAdminData, int id)
    {
        super(id);
        originalData = orderedAdminData;
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
        monitorCount = orderedAdminData[12];
        monitorSize = orderedAdminData[13];
        macAddressWired = orderedAdminData[14];
        macAddressWireless = orderedAdminData[15];
        phoneExtension = orderedAdminData[16];
        phoneType = orderedAdminData[17];
        lastReimage = orderedAdminData[18];
        notes = orderedAdminData[19];
        type = ComputerInfoType.Admin;
    }

    private String lastName;
    private String firstName;
    private String department;
    private String computerName;
    private String lastPasswordChange;
    private String phoneExtension;
    private String phoneType;
    private String lastReimage;

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
    public String getPhoneExtension()
    {
        return phoneExtension;
    }
    public String getPhoneType()
    {
        return phoneType;
    }
    public String getLastReimage()
    {
        return lastReimage;
    }
}
