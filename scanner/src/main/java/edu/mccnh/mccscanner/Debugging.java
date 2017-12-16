package edu.mccnh.mccscanner;

import android.util.Log;

/**
 * Created by Adam on 12/15/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class Debugging
{
    // Writes a given string array to the log.
    public static void write(String sourceTag, String[] toLog)
    {
        for (int i = 0; i < toLog.length; i++)
        {
            Log.d(sourceTag, "Array item #" + i + ": " + toLog[i]);
        }
    }
}
