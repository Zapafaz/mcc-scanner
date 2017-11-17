package edu.mccnh.mccscanner.datastorage;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class AcadComputerInfo extends ComputerInfo
{
    // Acad side is Sheet #2, 1 if 0-indexed
    // Total number of columns is 8 after throwing away unwanted/unused columns.
    // Total number of columns with unwanted/ignored columns is 13.
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
    // 4 PC Level
    // 5 Age of the Computers
    // 6 Usage Level
    // 7 Date Purchased
    // 8 Warranty
    // 9 Projector (IGNORED)
    // 10 Apple TV (IGNORED)
    // 11 LCD TV (IGNORED)
    // 12 Printer (IGNORED)
    // 13 Notes

    public AcadComputerInfo(String[] orderedAcadData)
    {
        roomArea = orderedAcadData[0];
        numberOfComputers = orderedAcadData[1];
        model = orderedAcadData[2];
        pcLevel = orderedAcadData[3];
        pcAge = orderedAcadData[4];
        usageScale = orderedAcadData[5];
        datePurchased = orderedAcadData[6];
        warrantyStatus = orderedAcadData[7];
        notes = orderedAcadData[8];
    }

    private final String roomArea;
    private final String numberOfComputers;

    public String getRoomArea()
    {
        return roomArea;
    }

    public String getNumberOfComputers()
    {
        return numberOfComputers;
    }
}
