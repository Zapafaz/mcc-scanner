package edu.mccnh.mccscanner;

import android.annotation.SuppressLint;
import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class ExcelParsing
{
    private static final int NO_ID_COLUMN = -5;
    private static final int UNSET_COLUMN_ID = -1;
    public static final int NO_ID_CODE = -4;

    // These are used to store ID column location so each scan doesn't have to relocate it
    private static int acadIdColumn = UNSET_COLUMN_ID;
    private static int adminIdColumn = UNSET_COLUMN_ID;

    // Take id codes and parse workbook to get raw data
    public static String[] getRawComputerInfo(int[] codes, Workbook workbook)
            throws NumberFormatException
    {
        String errorString;
        String errorTag;
        String[] rawInfo;
        if (codes == null || codes.length < 2)
        {
            errorTag = "INVALID_QR_CODE";
            errorString = "No valid QR code was found";
            rawInfo = new String[] {errorTag, errorString};
        }
        else
        {
            if ((codes[0] == Utility.ADMIN_SHEET || codes[0] == Utility.ACAD_SHEET))
            {
                if (isValidIdCode(codes[1]))
                {
                    rawInfo = parse(codes[1], codes[0], workbook);
                }
                else
                {
                    errorString = "ExcelParsing.getRawComputerInfo received invalid PC id format: " + codes[1] + " sheet id: " + codes[0];
                    errorTag = "INVALID_ID_CODE";
                    rawInfo = new String[] {errorTag, errorString};
                }
            }
            else
            {
                errorString = "ExcelParsing.getRawComputerInfo received invalid sheet id: " + codes[0] + " PC id: " + codes[1];
                errorTag = "INVALID_SHEET";
                rawInfo = new String[] {errorTag, errorString};
            }
        }
        return rawInfo;
    }

    // Determines which sheet to get data from, then gets raw data from row by using the id code
    // It could just get the data by a row ID code but then if the computer in that row is different from what it was when the QR sticker was created, it will be the wrong data
    // e.g. QR code points to row 37, but row 37 now contains data for a different computer than the one scanned
    @SuppressLint("DefaultLocale") // Don't need to care about locale for this method (or really, any of this app), not sure why it thinks I should
    private static String[] parse(int idCode, int sheetId, Workbook workbook)
            throws NumberFormatException
    {
        DataFormatter formatter = new DataFormatter();
        String[] computerData = null;
        Sheet sheet = null;
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        String errorString;
        String errorTag;
        int idColumn;
        boolean sheetFound;

        switch (sheetId)
        {
            case Utility.ADMIN_SHEET:
                computerData = new String[Utility.ADMIN_RAW_SIZE];
                errorString = String.format("ExcelParsing.parse failed to find cell in Admin sheet for idCode: %d", idCode);
                errorTag = "ADMIN_NOT_FOUND";
                sheet = workbook.getSheetAt(Utility.ADMIN_SHEET);
                sheetFound = true;
                break;
            case Utility.ACAD_SHEET:
                computerData = new String[Utility.ACAD_RAW_SIZE];
                errorString = String.format("ExcelParsing.parse failed to find cell in Acad sheet for idCode: %d", idCode);
                errorTag = "ACAD_NOT_FOUND";
                sheet = workbook.getSheetAt(Utility.ACAD_SHEET);
                sheetFound = true;
                break;
            default:
                errorString = String.format("ExcelParsing.parse failed to find valid sheet for sheetId: %d", sheetId);
                errorTag = "INVALID_SHEET";
                sheetFound = false;
                break;
        }
        if (sheetFound)
        {
            idColumn = findIdColumn(sheet, sheetId);
            if (idColumn == NO_ID_COLUMN)
            {
                errorTag = "NO_ID_COLUMN";
                errorString = "Could not find the ID code column (column starting with" + Utility.ID_COL_INDICATOR + " (unicode code point: U+2611)) in sheet:" + sheet.getSheetName();
            }
            else
            {
                // Iterates over every row rather than starting at the ID/header row, just in case - should only be a few extra rows, anyway...
                for (Row row : sheet)
                {
                    Cell idCell = row.getCell(idColumn);
                    if (idCodeMatchesCellCode(idCode, idCell))
                    {
                        // TODO: There's probably a cleaner way to iterate over each cell in a row...
                        int i = 0;
                        for (Cell cell : row)
                        {
                            computerData[i] = formatter.formatCellValue(cell, evaluator);
                            i++;
                            if (i >= computerData.length)
                            {
                                Utility.debugWriteArrayToLog("EXCEL_PARSER", computerData);
                                return computerData;
                            }
                        }
                    }
                }
            }
        }
        return new String[] { errorTag, errorString };
    }

    // Finds the ID column by looking for its header, or returns it from memory if it was previously found.
    private static int findIdColumn(Sheet sheet, int sheetId)
    {
        // Return previously found ID column location if it exists
        if (sheetId == Utility.ADMIN_SHEET && adminIdColumn != UNSET_COLUMN_ID)
        {
            return adminIdColumn;
        }
        else if (sheetId == Utility.ACAD_SHEET && acadIdColumn != UNSET_COLUMN_ID)
        {
            return acadIdColumn;
        }

        for (Row row : sheet)
        {
            for (Cell cell : row)
            {
                if (isIdColumnHeader(cell))
                {
                    int index = cell.getColumnIndex();
                    // Remember ID column location for future scans
                    if (sheetId == Utility.ADMIN_SHEET && adminIdColumn == UNSET_COLUMN_ID)
                    {
                        adminIdColumn = index;
                    }
                    else if (sheetId == Utility.ACAD_SHEET && acadIdColumn == UNSET_COLUMN_ID)
                    {
                        acadIdColumn = index;
                    }
                    return index;
                }
            }
        }
        return NO_ID_COLUMN;
    }

    // Check if the given cell is the ID column header by checking if it's first character is \u2611 (☑, ballot box with check)
    @SuppressWarnings("ConstantConditions") // flow analysis is wrong here; String.trim() makes it think val cannot be null but trim() returns null if it's used on String variable that is null.
    private static boolean isIdColumnHeader(Cell cell)
    {
        DataFormatter formatter = new DataFormatter();
        String val = formatter.formatCellValue(cell).trim();
        if (val != null && val.length() > 0)
        {
            val = val.substring(0, 1);
            if (val.equals(Utility.ID_COL_INDICATOR))
            {
                return true;
            }
        }
        return false;
    }

    // Check if a given ID code matches the code in a given cell.
    private static boolean idCodeMatchesCellCode(int idCode, Cell cell)
    {
        DataFormatter formatter = new DataFormatter();
        String cellData = formatter.formatCellValue(cell);
        int cellCode = -1;
        if (cellData != null && cellData.length() > 0)
        {
            try
            {
                cellCode = Integer.parseInt(cellData);
            }
            catch (NumberFormatException e)
            {
                Log.d(e.getClass().getCanonicalName(), e.getLocalizedMessage());
            }
        }
        return idCode == cellCode;
    }

    // Check if a given integer matches either ID code format.
    private static boolean isValidIdCode(int idCode)
    {
        return ((idCode < 20000 && idCode >= 10000) || (idCode < 60000 && idCode >= 50000));
    }
}
