package edu.mccnh.mccscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.mccnh.mccscanner.ExcelParsing;
import edu.mccnh.mccscanner.Organizer;
import edu.mccnh.mccscanner.R;
import edu.mccnh.mccscanner.Utility;

/**
 * Created by Adam on 9/10/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class MainActivity extends AppCompatActivity implements SettingsFragment.OnFragmentInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener
{

    // Control flow: checkPermissionReadStorage -> onRequestPermissionResult -> checkPermissionCamera -> onRequestPermissionResult -> (continued)...
    // startScan -> (scanning activity) -> onActivityResult -> displayInfo -> (next activity: Display(Admin/Acad)InfoActivity)
    // Any errors along the way get sent to displayError

    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    final int PERMISSION_REQUEST_RATIONALE_CODE_READ_EXTERNAL_STORAGE = 2;
    final int PERMISSION_REQUEST_RATIONALE_CODE_CAMERA = 3;
    public static final String EXTRA_ORDERED_DATA = "edu.mccnh.mccscanner.EXTRA_ORDERED_DATA";
    public static final String EXTRA_ID_CODE = "edu.mccnh.mccscanner.EXTRA_ID_CODE";
    public static final String EXTRA_ERROR_DISPLAY = "edu.mccnh.mccscanner.EXTRA_ERROR_DISPLAY";
    public static final String KEY_PREF_PATH = "pref_excel_file";
    public static final String KEY_PREF_FIRST = "pref_first_run";
    public static final int STRING_ARRAY_ERROR = 2;
    public static final int VALID_CODES = 2;

    // This ugly global variable is so the app doesn't have to re-load the whole workbook into memory with every scan
    public static Workbook currentWorkbook = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // These next 3 setProperty lines are REQUIRED for excel parsing (read: the app) to work
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        // Set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // On startup, check if it's first time running app; if so, show settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isFirstRun = prefs.getBoolean(KEY_PREF_FIRST, true);
        if (isFirstRun)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_PREF_FIRST, false).apply();
            showSettings();
        }
    }

    // Called after each request for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    checkPermissionCamera(this);
                }
                else
                {
                    noPermissions(PERMISSION_REQUEST_RATIONALE_CODE_READ_EXTERNAL_STORAGE);
                }
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startScan();
                }
                else
                {
                    noPermissions(PERMISSION_REQUEST_RATIONALE_CODE_CAMERA);
                }
        }
    }

    // Called after result from QR code scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String path  = getKeyPrefPath();
        if (scanResult != null)
        {
            String qrCode = scanResult.getContents();
            Log.d("SCAN_RESULT", "qrCode: " + qrCode);

            File excelFile = findExcelFile(path);
            if (excelFile != null)
            {
                try
                {
                    int[] codes = decipherQrCode(qrCode);
                    if (codes == null || codes.length < VALID_CODES)
                    {
                        displayError("INVALID_QR_CODE", "No valid QR code was found");
                    }

                    loadWorkbook(path);
                    String[] rawComputerInfo = ExcelParsing.getRawComputerInfo(codes, currentWorkbook);
                    if (rawComputerInfo.length == STRING_ARRAY_ERROR)
                    {
                        displayError(rawComputerInfo[0], rawComputerInfo[1]);
                    }
                    else
                    {
                        String[] orderedInfo = Organizer.OrderRawComputerInfo(rawComputerInfo);
                        if (orderedInfo.length == STRING_ARRAY_ERROR)
                        {
                            displayError(orderedInfo[0], orderedInfo[1]);
                        }
                        else
                        {
                            displayInfo(orderedInfo, codes);
                        }
                    }
                } catch (IOException | InvalidFormatException | EncryptedDocumentException e)
                {
                    displayError(e.getClass().getCanonicalName(), e.getLocalizedMessage());
                }
            }
            else
            {
                displayError("FILE_NOT_FOUND", "Could not find excel file at: " + path);
            }
        }
        else
        {
            displayError("NULL_SCAN", "onActivityResult got null scan from IntentResult (QR code scan)");
        }
    }

    // Loads the file -> workbook from the given path into a static variable (memory) so it can be used for multiple scans
    private void loadWorkbook(String path)
            throws IOException, InvalidFormatException, EncryptedDocumentException
    {
        if (currentWorkbook == null)
        {
            InputStream stream = new FileInputStream(path);
            currentWorkbook = WorkbookFactory.create(stream);
        }
    }
    // Parse QR code into an int array: index 0 is the type of PC (admin or academic), index 1 is the rest of the QR code (i.e. the ID code for the pc)
    private static int[] decipherQrCode(String qrCode)
    {
        if (qrCode == null || qrCode.length() < 5)
        {
            return null;
        }
        int[] codes = new int[2];
        try
        {
            int temp = Integer.parseInt(qrCode.trim());
            if (temp < 20000 && temp >= 10000)
            {
                codes[0] = Utility.ADMIN_SHEET;
            }
            else if (temp < 60000 && temp >= 50000)
            {
                codes[0] = Utility.ACAD_SHEET;
            }
            codes[1] = temp;
        }
        catch (NumberFormatException e)
        {
            Log.d(e.getClass().getCanonicalName(), "decipherQrCode: " + e.getLocalizedMessage());
            return null;
        }
        return codes;
    }

    // Load the excel file path from shared preferences
    private String getKeyPrefPath()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String path = prefs.getString(KEY_PREF_PATH, "");
        path = Utility.stripColon(path);
        return path;
    }

    // Find excel file at given location.
    File findExcelFile(String path)
    {
        File excelFile = new File(path);
        try
        {
            if (Utility.getFileExtension(path).equals(getString(R.string.excel_extension)))
            {
                return excelFile;
            }
            else
            {
                excelFile = Utility.checkFilePaths(excelFile.getParentFile().listFiles(), getString(R.string.excel_extension));
            }
            if (excelFile != null)
            {
                return excelFile;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            displayError(e.getClass().getCanonicalName(), e.getLocalizedMessage());
            return null;
        }
    }

    // Start checking permissions for scanning; scan if have permissions
    public void onScanClick(View view)
    {
        checkPermissionReadStorage(this);
    }

    // Open settings fragment to adjust settings
    public void onSettingsClick(View view)
    {
        showSettings();
    }

    // Shows the settings fragment so user can change excel file location
    public void showSettings()
    {
        getFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, SettingsFragment.newInstance())
                .commit();
    }

    // Sends ordered computer info to next activity where it can be displayed
    private void displayInfo(String[] orderedComputerInfo, int[] codes)
    {
        Intent intent = null;
        if (codes[0] == Utility.ADMIN_SHEET)
        {
            intent = new Intent(this, AdminInfoActivity.class);

        }
        else if (codes[1] == Utility.ACAD_SHEET)
        {
            intent = new Intent( this, AcadInfoActivity.class);
        }
        else
        {
            displayError("INVALID_FORMAT", "AdminInfoActivity.onCreate orderedComputerInfo array size does not match either Admin nor Academic");
        }
        if (intent != null)
        {
            intent.putExtra(EXTRA_ORDERED_DATA, orderedComputerInfo);
            intent.putExtra(EXTRA_ID_CODE, codes[1]);
            startActivity(intent);
        }
    }

    // Starts the QR code scanning activity, via zxing-android-embedded
    private void startScan()
    {
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();
    }

    // Called if no permissions for camera or read storage. App does not require any other permissions, so should never be called otherwise
    private void noPermissions(int code)
    {
        String errorString = "MainActivity.noPermissions received unknown error code: " + code;
        String errorTag = "NO_PERM_UNKNOWN_CODE";
        if (code == PERMISSION_REQUEST_RATIONALE_CODE_READ_EXTERNAL_STORAGE)
        {
            errorString = "MainActivity.noPermissions: no read storage permission, code: " + code;
            errorTag = "NO_PERM_READ_STORAGE";
        }
        else if (code == PERMISSION_REQUEST_RATIONALE_CODE_CAMERA)
        {
            errorString = "MainActivity.noPermissions: no camera permission, code: " + code;
            errorTag = "NO_PERM_CAMERA";
        }
        displayError(errorTag, errorString);
    }

    // Calls checkPermissionCamera if app has read storage permission, requestPermissions(read storage) if not, otherwise calls noPermissions
    public void checkPermissionReadStorage(Activity activity)
    {
        int checkIfPermissionGranted = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkIfPermissionGranted == PackageManager.PERMISSION_DENIED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                noPermissions(PERMISSION_REQUEST_RATIONALE_CODE_READ_EXTERNAL_STORAGE);
            }
            else
            {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
        else if (checkIfPermissionGranted == PackageManager.PERMISSION_GRANTED)
        {
            checkPermissionCamera(this);
        }
    }

    // Calls startScan if app has camera permission, requestPermissions(camera) if not, otherwise calls noPermissions
    public void checkPermissionCamera(Activity activity)
    {
        int checkIfPermissionGranted = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA);
        if (checkIfPermissionGranted == PackageManager.PERMISSION_DENIED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA))
            {
                noPermissions(PERMISSION_REQUEST_RATIONALE_CODE_CAMERA);
            }
            else
            {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
        else if (checkIfPermissionGranted == PackageManager.PERMISSION_GRANTED)
        {
            startScan();
        }
    }

    // Displays the error to the user via new activity
    private void displayError(String tag, String error)
    {
        Log.d(tag, error);
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra(EXTRA_ERROR_DISPLAY, new String[] {tag, error});
        startActivity(intent);
    }

    // I think these are required for fragment to work but they are not used at the moment
    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

    }
}
