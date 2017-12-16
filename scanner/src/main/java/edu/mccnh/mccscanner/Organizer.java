package edu.mccnh.mccscanner;

import java.util.ArrayList;

import edu.mccnh.mccscanner.datastorage.AcadComputerInfo;
import edu.mccnh.mccscanner.datastorage.AdminComputerInfo;

/**
 * Created by Adam on 10/8/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class Organizer
{
    // Organizes raw computer data set into either admin set or acad set, for use with ComputerInfo classes
    @SuppressWarnings("ConstantConditions") // ConstantConditions warns of always null value for admin ignored indexes - it's (final) null now, but might change in future build, so check just in case, rather than flatly returning raw
    public static String[] organize(String[] rawComputerData)
    {
        String[] orderedData;
        String errorString = "";
        String errorTag = "";
        int[] ignoredIndexes = new int[1];
        boolean validSize;

        switch (rawComputerData.length)
        {
            // Admin side has no ignored columns
            case AdminComputerInfo.RAW_SIZE:
                orderedData = new String[AdminComputerInfo.ORDERED_SIZE];
                ignoredIndexes = AdminComputerInfo.IGNORED_COLS;
                if (ignoredIndexes == null || ignoredIndexes.length < 1)
                {
                    return rawComputerData;
                }
                validSize = true;
                break;
            case AcadComputerInfo.RAW_SIZE:
                orderedData = new String[AcadComputerInfo.ORDERED_SIZE];
                ignoredIndexes = AcadComputerInfo.IGNORED_COLS;
                if (ignoredIndexes == null || ignoredIndexes.length < 1)
                {
                    return rawComputerData;
                }
                validSize = true;
                break;
            default:
                errorString = "Organizer.organize received invalidly formatted raw data to be ordered. rawComputerData.length: " + rawComputerData.length;
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

            Debugging.write("ORGANIZER", orderedData);
            return orderedData;
        }
        return new String[] { errorTag, errorString };
    }
}
