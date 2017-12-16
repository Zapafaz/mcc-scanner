package edu.mccnh.mccscanner.datastorage;

/**
 * Created by Adam on 12/15/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */
public class ComputerInfoIdentifier
{
    private int rowId;
    private int sheetId;
    public ComputerInfoIdentifier(int rowId, int sheetId)
    {
        this.rowId = rowId;
        this.sheetId = sheetId;
    }

    // Gets the row ID, the identifier that should match the identifier in the row the computer is found in
    public int getRowId()
    {
        return rowId;
    }

    // Gets the sheet ID, the identifier for which sheet the computer is in (Academic or Administrative)
    public int getSheetId()
    {
        return sheetId;
    }
}
