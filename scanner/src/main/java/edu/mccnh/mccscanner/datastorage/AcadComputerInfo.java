package edu.mccnh.mccscanner.datastorage;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class AcadComputerInfo extends ComputerInfo
{
    public static final int VALID_CODE_LOWER = 50000;
    public static final int VALID_CODE_UPPER = 60000;
    public static final int SHEET_ID = 1;
    public static final int RAW_SIZE = 19;
    public static final int ORDERED_SIZE = 14;
    public static final int[] IGNORED_COLS = new int[]{ 0, 14, 15, 16, 17};

    // Ignored columns:
    // Age of the Computers
    // Projector
    // Apple TV
    // LCD TV
    // Printer

    // Raw columns (0-indexed):
    // 0 ????? (IGNORED)
    // 1 Room/area (starts at col B for some reason??)
    // 2 Number of Computers
    // 3 Make/model
    // 4 Serial
    // 5 PC Level
    // 6 Age of the Computers
    // 7 Usage Level
    // 8 Date Purchased
    // 9 Warranty
    // 10 MAC Address (Wireless)
    // 11 MAC Address (Wired)
    // 12 Monitor Size
    // 13 Monitor Count
    // 14 Projector (IGNORED)
    // 15 Apple TV (IGNORED)
    // 16 LCD TV (IGNORED)
    // 17 Printer (IGNORED)
    // 18 Notes
    // TOTAL COUNT: 19
    // SANS IGNORED: 14

    public AcadComputerInfo(String[] orderedAcadData, int id)
    {
        super(id);
        originalData = orderedAcadData;
        roomArea = orderedAcadData[0];
        numberOfComputers = orderedAcadData[1];
        model = orderedAcadData[2];
        serialNumber = orderedAcadData[3];
        pcLevel = orderedAcadData[4];
        pcAge = orderedAcadData[5];
        usageScale = orderedAcadData[6];
        datePurchased = orderedAcadData[7];
        warrantyStatus = orderedAcadData[8];
        macAddressWireless = orderedAcadData[9];
        macAddressWired = orderedAcadData[10];
        monitorSize = orderedAcadData[11];
        monitorCount = orderedAcadData[12];
        notes = orderedAcadData[13];
        type = ComputerInfoType.Acad;
    }

    private String roomArea;
    private String numberOfComputers;

    public String getRoomArea()
    {
        return roomArea;
    }
    public String getNumberOfComputers()
    {
        return numberOfComputers;
    }
}
