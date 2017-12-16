package edu.mccnh.mccscanner;

import org.junit.Assert;
import org.junit.Test;

import edu.mccnh.mccscanner.datastorage.AcadComputerInfo;
import edu.mccnh.mccscanner.datastorage.AdminComputerInfo;
import edu.mccnh.mccscanner.datastorage.ComputerInfoIdentifier;
import edu.mccnh.mccscanner.datastorage.ComputerInfoType;

/**
 * Created by Adam on 12/16/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class ExcelParsingTest
{
    private static String path = "H:\\Projects\\Software\\Java\\MCCScanner\\scanner\\src\\test\\test_failer_book.xlsx";

    private static ComputerInfoIdentifier identifierGenerator(ComputerInfoType type, int rowId)
    {
        int sheetId;
        switch (type)
        {
            case Acad:
                sheetId = AcadComputerInfo.SHEET_ID;
                break;
            case Admin:
                sheetId = AdminComputerInfo.SHEET_ID;
                break;
            default:
                sheetId = 3;
                break;
        }
        return new ComputerInfoIdentifier(rowId, sheetId);
    }

    @Test
    public void getRawComputerInfoTest()
    {
        try
        {
            // These next 3 setProperty lines are REQUIRED for excel parsing (read: the app) to work
            System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
            System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
            System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
            ExcelParsing.loadWorkbook(path);
            for (int i = 11111; i < 11129; i++)
            {
                ComputerInfoIdentifier identifier = identifierGenerator(ComputerInfoType.Admin, i);
                Assert.assertEquals("Raw admin size should match getRaw.length", AdminComputerInfo.RAW_SIZE, ExcelParsing.getRawComputerInfo(identifier).length);
            }
            for (int i = 50001; i < 50116; i++)
            {
                ComputerInfoIdentifier identifier = identifierGenerator(ComputerInfoType.Acad, i);
                Assert.assertEquals("Raw acad size should match getRaw.length", AcadComputerInfo.RAW_SIZE, ExcelParsing.getRawComputerInfo(identifier).length);
            }
            for (int i = 0; i < 1; i++)
            {
                ComputerInfoIdentifier identifier = identifierGenerator(ComputerInfoType.None, i);
                Assert.assertNotEquals("Type.None should not match acad size", AcadComputerInfo.RAW_SIZE, ExcelParsing.getRawComputerInfo(identifier).length);
                Assert.assertNotEquals("Type.None should not match admin size", AdminComputerInfo.RAW_SIZE, ExcelParsing.getRawComputerInfo(identifier).length);
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
        }
    }
}
