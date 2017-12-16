package edu.mccnh.mccscanner.activities;

import android.Manifest;
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

import java.io.File;

import edu.mccnh.mccscanner.ExcelParsing;
import edu.mccnh.mccscanner.FileHandling;
import edu.mccnh.mccscanner.Organizer;
import edu.mccnh.mccscanner.R;
import edu.mccnh.mccscanner.datastorage.AcadComputerInfo;
import edu.mccnh.mccscanner.datastorage.AdminComputerInfo;
import edu.mccnh.mccscanner.datastorage.ComputerInfoIdentifier;

/**
 * Created by Adam on 9/10/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */
// TODO: Add a display activity that displays results with headers based on headers from file; maybe base column count on result length? and add option to show raw results in settings
public class MainActivity extends AppCompatActivity implements SettingsFragment.OnFragmentInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener
{

    // Control flow: checkPermissionReadStorage -> onRequestPermissionResult -> checkPermissionWriteStorage -> onRequestPermissionResult -> checkPermissionCamera -> onRequestPermissionResult -> (continued)...
    // startScan -> (scanning activity) -> onActivityResult -> handleQrCode -> displayInfo -> (next activity: Display(Admin/Acad)InfoActivity)
    // Any errors along the way get sent to displayError


    final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    final int PERMISSIONS_REQUEST_CAMERA = 2;
    final int PERMISSION_REQUEST_RATIONALE_CODE_READ_EXTERNAL_STORAGE = 3;
    final int PERMISSION_REQUEST_RATIONALE_CODE_WRITE_EXTERNAL_STORAGE = 4;
    final int PERMISSION_REQUEST_RATIONALE_CODE_CAMERA = 5;
    public static final String EXTRA_ORDERED_DATA = "edu.mccnh.mccscanner.EXTRA_ORDERED_DATA";
    public static final String EXTRA_ID_CODE = "edu.mccnh.mccscanner.EXTRA_ID_CODE";
    public static final String EXTRA_ERROR_DISPLAY = "edu.mccnh.mccscanner.EXTRA_ERROR_DISPLAY";
    public static final String KEY_PREF_PATH = "pref_excel_file";
    public static final String KEY_PREF_FIRST = "pref_first_run";
    public static final int STRING_ARRAY_ERROR = 2;

    private Boolean debugScanAcad = false;

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

    // Deciphers QR code and gets info to display, then sends it to displayInfo
    private void handleQrCode(String qrCode)
    {
        String path  = getKeyPrefPath();

        File excelFile = findExcelFile(path);
        if (excelFile != null)
        {
            try
            {
                ComputerInfoIdentifier identifier = decipherQrCode(qrCode);
                if (identifier == null)
                {
                    displayError("INVALID_QR_CODE", "No valid QR code was found");
                }

                ExcelParsing.loadWorkbook(path);

                String[] rawComputerInfo = ExcelParsing.getRawComputerInfo(identifier);
                if (rawComputerInfo.length == STRING_ARRAY_ERROR)
                {
                    displayError(rawComputerInfo[0], rawComputerInfo[1]);
                }
                else
                {
                    String[] orderedInfo = Organizer.organize(rawComputerInfo);
                    if (orderedInfo.length == STRING_ARRAY_ERROR)
                    {
                        displayError(orderedInfo[0], orderedInfo[1]);
                    }
                    else
                    {
                        displayInfo(orderedInfo, identifier);
                    }
                }
            } catch (Exception e)
            {
                displayError(e.getClass().getCanonicalName(), e.getLocalizedMessage());
            }
        }
        else
        {
            displayError("FILE_NOT_FOUND", "Could not find excel file at: " + path);
        }
    }

    // Called after result from QR code scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scanResult != null)
        {
            String qrCode = scanResult.getContents();
            if (qrCode.length() > 0)
            {
                Log.d("SCAN_RESULT", "qrCode: " + qrCode);
                handleQrCode(qrCode);
            }
        }
        else
        {
            displayError("NULL_SCAN", "onActivityResult got null scan from IntentResult (QR code scan)");
        }
    }

    // Parse QR code into an identifier with sheetId (admin or academic) and rowId (i.e. the identifier that should match the identifier in the row the computer is found in)
    private static ComputerInfoIdentifier decipherQrCode(String qrCode)
    {
        if (qrCode == null || qrCode.length() < 5)
        {
            return null;
        }
        int sheetId = 0;
        int rowId = 0;
        try
        {
            int temp = Integer.parseInt(qrCode.trim());
            if (temp < AdminComputerInfo.VALID_CODE_UPPER && temp >= AdminComputerInfo.VALID_CODE_LOWER)
            {
                sheetId = AdminComputerInfo.SHEET_ID;
            }
            else if (temp < AcadComputerInfo.VALID_CODE_UPPER && temp >= AcadComputerInfo.VALID_CODE_LOWER)
            {
                sheetId = AcadComputerInfo.SHEET_ID;
            }
            else
            {
                throw new UnsupportedOperationException("decipherQrCode: Identifier mismatch, code ("+temp+") matches neither Academic (50000 to 59999) nor Administrative (10000 to 19999)");
            }
            rowId = temp;
        }
        catch (NumberFormatException e)
        {
            Log.d(e.getClass().getCanonicalName(), "decipherQrCode: " + e.getLocalizedMessage());
        }
        return new ComputerInfoIdentifier(rowId, sheetId);
    }

    // Load the excel file path from shared preferences
    private String getKeyPrefPath()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String path = prefs.getString(KEY_PREF_PATH, "");
        path = FileHandling.stripColon(path);
        return path;
    }

    // Find excel file at given location.
    File findExcelFile(String path)
    {
        File excelFile = new File(path);
        try
        {
            if (FileHandling.getFileExtension(path).equals(getString(R.string.excel_extension)))
            {
                return excelFile;
            }
            else
            {
                excelFile = FileHandling.checkFilePaths(excelFile.getParentFile().listFiles(), getString(R.string.excel_extension));
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
    private void displayInfo(String[] orderedComputerInfo, ComputerInfoIdentifier identifier)
    {
        Intent intent = null;
        int sheetId = identifier.getSheetId();
        int rowId = identifier.getRowId();
        if (sheetId == AdminComputerInfo.SHEET_ID)
        {
            intent = new Intent(this, AdminInfoActivity.class);

        }
        else if (sheetId == AcadComputerInfo.SHEET_ID)
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
            intent.putExtra(EXTRA_ID_CODE, rowId);
            startActivity(intent);
        }
    }

    // Starts the QR code scanning activity, via zxing-android-embedded
    private void startScan()
    {
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();
    }

    // Called if no permissions for camera or write/read storage. App does not require any other permissions, so should never be called otherwise
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
        else if (code == PERMISSION_REQUEST_RATIONALE_CODE_WRITE_EXTERNAL_STORAGE)
        {
            errorString = "MainActivity.noPermissions: no write storage permission, code: " + code;
            errorTag = "NO_PERM_WRITE_STORAGE";
        }
        displayError(errorTag, errorString);
    }

    // Called after each request for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        if (grantResults.length > 0)
        {
            Log.d("METHOD_START", "requestCode: " +requestCode+ " grantResult (0=granted): "+ grantResults[0] + "onRequestPermissionsResult: " + System.currentTimeMillis());
        }
        switch(requestCode)
        {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    checkPermissionWriteStorage(this);
                }
                else
                {
                    noPermissions(PERMISSION_REQUEST_RATIONALE_CODE_READ_EXTERNAL_STORAGE);
                }
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    checkPermissionCamera(this);
                }
                else
                {
                    noPermissions(PERMISSION_REQUEST_RATIONALE_CODE_WRITE_EXTERNAL_STORAGE);
                }
            case PERMISSIONS_REQUEST_CAMERA:
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

    // Calls checkPermissionWriteStorage if app has read storage permission, requestPermissions(read storage) if not, otherwise calls noPermissions
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
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
        else if (checkIfPermissionGranted == PackageManager.PERMISSION_GRANTED)
        {
            checkPermissionWriteStorage(this);
        }
    }

    // Calls checkPermissionCamera if app has write storage permission, requestPermissions(write storage) if not, otherwise calls noPermissions
    public void checkPermissionWriteStorage(Activity activity)
    {
        int checkIfPermissionGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkIfPermissionGranted == PackageManager.PERMISSION_DENIED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                noPermissions(PERMISSION_REQUEST_RATIONALE_CODE_WRITE_EXTERNAL_STORAGE);
            }
            else
            {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_RATIONALE_CODE_WRITE_EXTERNAL_STORAGE);
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
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
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
