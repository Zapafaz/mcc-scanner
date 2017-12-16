package edu.mccnh.mccscanner;

import java.io.File;

/**
 * Created by Adam on 10/1/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class FileHandling
{
    // Gets the file extension for a given file.
    public static String getFileExtension(File file)
    {
        String path = file.getName();
        return getFileExtension(path);
    }

    // Gets the file extension for a given String file path.
    public static String getFileExtension(String path)
    {
        String extension = "";
        int i = 0;
        try {
            i = path.lastIndexOf('.');
            if (i >= 0)
            {
                extension = path.substring(i+1);
            }
            return extension;
        } catch (Exception e)
        {
            return "";
        }
    }

    // Get the first file from a given list of files if it matches the given extension.
    public static File checkFilePaths(File[] files, String extension)
    {
        for (File file : files)
        {
            if (getFileExtension(file).equals(extension))
            {
                return file;
            }
        }
        return null;
    }

    // Removes trailing colon from a file path. I don't know why this FilePicker library adds a colon to every path, but it does.
    public static String stripColon(String path)
    {
        if (path != null && path.length() > 0 && ":".equals(path.substring(path.length() - 1)))
        {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
