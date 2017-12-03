package edu.mccnh.mccscanner;

import java.util.ArrayList;

/**
 * Created by Adam on 10/8/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class Organizer
{
    // Organizes raw computer data set into either admin set or acad set, for use with ComputerInfo classes
    public static String[] OrderRawComputerInfo(String[] rawComputerData)
    {
        String[] orderedData;
        String errorString = "";
        String errorTag = "";
        int[] ignoredIndexes = new int[1];
        boolean validSize;

        switch (rawComputerData.length)
        {
            // Admin side has no ignored columns
            case Utility.ADMIN_RAW_SIZE:
                return rawComputerData;
            case Utility.ACAD_RAW_SIZE:
                orderedData = new String[Utility.ACAD_ORDERED_SIZE];
                ignoredIndexes = Utility.ACAD_IGNORED_COLS;
                validSize = true;
                break;
            default:
                errorString = "Organizer.OrderRawComputerInfo received invalidly formatted raw data to be ordered. rawComputerData.length: " + rawComputerData.length;
                errorTag = "INVALID_FORMAT";
                orderedData = null;
                validSize = false;
                break;
        }
        if (validSize)
        {
            // TODO: is there a cleaner way to filter this? This works but it seems convoluted
            boolean validIndex;
            ArrayList<String> list = new ArrayList<>();
            for (int raw = 0; raw < rawComputerData.length; raw++)
            {
                validIndex = true;
                for (int ignored = 0; ignored < ignoredIndexes.length; ignored++)
                {
                    if (ignoredIndexes[ignored] == raw)
                    {
                        validIndex = false;
                        break;
                    }
                }
                if (validIndex)
                {
                    list.add(rawComputerData[raw]);
                }
            }
            orderedData = list.toArray(new String[orderedData.length]);

            Utility.debugWriteArrayToLog("ORGANIZER", orderedData);
            return orderedData;
        }
        return new String[] { errorTag, errorString };
    }
}
