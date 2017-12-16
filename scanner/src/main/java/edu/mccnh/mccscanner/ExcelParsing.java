package edu.mccnh.mccscanner;

import android.annotation.SuppressLint;
import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.util.ArrayList;

import edu.mccnh.mccscanner.datastorage.AcadComputerInfo;
import edu.mccnh.mccscanner.datastorage.AdminComputerInfo;
import edu.mccnh.mccscanner.datastorage.ComputerInfoIdentifier;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class ExcelParsing
{
    private static final String ID_COL_INDICATOR = "\u2611"; // ☑, ballot box with check

    // TODO: implement column ignoring in raw scan by checking for this in header row cells
    private static final String IGNORED_COL_INDICATOR = "\u2612"; // ☒, ballot box with X

    // These are used to store ID column location so each scan doesn't have to relocate it
    private static Cell acadIdCell = null;
    private static Cell adminIdCell = null;

    private static Workbook workbook = null;

    // Loads the workbook from the given path into a static variable (memory) so it can be used for multiple scans
    public static void loadWorkbook(String path)
        throws Exception
    {
        if (workbook == null)
        {
            workbook = WorkbookFactory.create(new FileInputStream(path));
        }
    }

    // Take identifier and parse current workbook to get raw data -- entry point for this class
    public static String[] getRawComputerInfo(ComputerInfoIdentifier identifier)
            throws Exception
    {
        String errorString;
        String errorTag;
        String[] rawInfo;
        if (identifier == null)
        {
            errorTag = "INVALID_QR_CODE";
            errorString = "getRawComputerInfo: Identifier was null";
            rawInfo = new String[] {errorTag, errorString};
        }
        else
        {
            int rowId = identifier.getRowId();
            int sheetId = identifier.getSheetId();
            if ((sheetId == AdminComputerInfo.SHEET_ID || sheetId == AcadComputerInfo.SHEET_ID))
            {
                if (isValidIdCode(rowId))
                {
                    rawInfo = parse(identifier);
                }
                else
                {
                    errorString = "ExcelParsing.getRawComputerInfo received invalid row id format: " + rowId + " sheet id: " + sheetId;
                    errorTag = "INVALID_ID_CODE";
                    rawInfo = new String[] {errorTag, errorString};
                }
            }
            else
            {
                errorString = "ExcelParsing.getRawComputerInfo received invalid sheet id: " + sheetId + " row id: " + rowId;
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
    private static String[] parse(ComputerInfoIdentifier identifier)
            throws Exception
    {
        Sheet sheet = null;
        String errorString;
        String errorTag;
        int rowId = identifier.getRowId();
        int sheetId = identifier.getSheetId();
        int idColumn;
        boolean validSheet;

        switch (sheetId)
        {
            case AdminComputerInfo.SHEET_ID:
                sheet = workbook.getSheetAt(AdminComputerInfo.SHEET_ID);

                errorString = String.format("ExcelParsing.parse failed to find cell in Admin sheetId (given: #%d, %s) for rowId: "+ rowId, sheetId, sheet.getSheetName());
                errorTag = "ADMIN_NOT_FOUND";
                validSheet = true;
                break;
            case AcadComputerInfo.SHEET_ID:
                sheet = workbook.getSheetAt(AcadComputerInfo.SHEET_ID);

                errorString = String.format("ExcelParsing.parse failed to find cell in Acad sheetId (given: #%d, %s) for rowId: " + rowId, sheetId, sheet.getSheetName());
                errorTag = "ACAD_NOT_FOUND";
                validSheet = true;
                break;
            default:
                errorString = String.format("ExcelParsing.parse failed to find valid sheet for sheetId: %d", sheetId);
                errorTag = "INVALID_SHEET";
                validSheet = false;
                break;
        }
        if (validSheet)
        {
            Cell idCell = findIdColumnHeader(sheet, sheetId);
            if (idCell == null)
            {
                errorTag = "NO_ID_COLUMN";
                errorString = "Could not find the row ID column (column starting with " + ID_COL_INDICATOR + " (Unicode code point: U+2611)) in sheet: " + sheet.getSheetName();
            }
            else
            {
                idColumn = idCell.getColumnIndex();

                // Iterates over every row rather than starting at the ID/header row, just in case - should only be a few extra rows, anyway...
                for (Row row : sheet)
                {
                    if (doesCellMatch(row.getCell(idColumn), rowId))
                    {
                        return getAllCells(row, idColumn);
                    }
                }
            }
        }
        return new String[] { errorTag, errorString };
    }

    // Get all cells as strings up to a given maximum column.
    private static String[] getAllCells(Row row, int exclusiveMax)
            throws Exception
    {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        DataFormatter formatter = new DataFormatter();
        ArrayList<String> data = new ArrayList<>();
        String temp;
        for (int i = 0; i < exclusiveMax; i++)
        {
            temp = formatter.formatCellValue(row.getCell(i), evaluator);
            // Change null to empty string just in case
            if (temp == null)
            {
                temp = "";
            }
            data.add(temp);
        }
        return data.toArray(new String[0]);
    }

    // Finds the ID column by looking for its header, or returns it from memory if it was previously found.
    private static Cell findIdColumnHeader(Sheet sheet, int sheetId)
    {
        // Return previously found ID column location if it exists
        if (sheetId == AdminComputerInfo.SHEET_ID && adminIdCell != null)
        {
            return adminIdCell;
        }
        else if (sheetId == AcadComputerInfo.SHEET_ID && acadIdCell != null)
        {
            return acadIdCell;
        }

        for (Row row : sheet)
        {
            for (Cell cell : row)
            {
                if (doesCellContain(cell, ID_COL_INDICATOR))
                {
                    // Remember ID column location for future scans
                    if (sheetId == AdminComputerInfo.SHEET_ID && adminIdCell == null)
                    {
                        adminIdCell = cell;
                    }
                    else if (sheetId == AcadComputerInfo.SHEET_ID && acadIdCell == null)
                    {
                        acadIdCell = cell;
                    }
                    return cell;
                }
            }
        }
        return null;
    }

    // Check if the given cell contains the given string.
    @SuppressWarnings("ConstantConditions") // flow analysis is wrong here because it isn't analyzing String.trim(), which can return null.
    private static boolean doesCellContain(Cell cell, String string)
    {
        DataFormatter formatter = new DataFormatter();
        String val = formatter.formatCellValue(cell).trim();
        if (val != null && val.length() > 0)
        {
            if (val.startsWith(string) || val.contains(string))
            {
                return true;
            }
        }
        return false;
    }

    // Check if the given integer matches the integer in the given cell.
    @SuppressWarnings("ConstantConditions") // flow analysis is wrong here because it isn't analyzing String.trim(), which can return null.
    private static boolean doesCellMatch(Cell cell, int i)
    {
        DataFormatter formatter = new DataFormatter();
        String cellData = formatter.formatCellValue(cell).trim();
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
        return i == cellCode;
    }

    // Check if a given integer matches either ID code format.
    private static boolean isValidIdCode(int idCode)
    {
        return ((idCode < AdminComputerInfo.VALID_CODE_UPPER && idCode >= AdminComputerInfo.VALID_CODE_LOWER) || (idCode < AcadComputerInfo.VALID_CODE_UPPER && idCode >= AcadComputerInfo.VALID_CODE_LOWER));
    }
}
