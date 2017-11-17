package edu.mccnh.mccscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
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

public class MainActivity extends AppCompatActivity implements Preference.OnPreferenceChangeListener, SettingsFragment.OnFragmentInteractionListener
{

    // Control flow: checkPermissionReadStorage -> onRequestPermissionResult -> checkPermissionCamera -> onRequestPermissionResult -> (continued)...
    // startScan -> (scanning activity) -> onActivityResult -> displayInfo -> (next activity: DisplayAdmin/AcadInfoActivity)
    // Any errors along the way get sent to displayError

    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    final int PERMISSION_REQUEST_RATIONALE_CODE_READ_EXTERNAL_STORAGE = 2;
    final int PERMISSION_REQUEST_RATIONALE_CODE_CAMERA = 3;
    public static final String EXTRA_ORDERED_DATA = "edu.mccnh.mccscanner.EXTRA_ORDERED_DATA";
    public static final String EXTRA_ERROR_DISPLAY = "edu.mccnh.mccscanner.EXTRA_ERROR_DISPLAY";
    public static final String KEY_PREF_PATH = "pref_excel_file";
    public static final String KEY_PREF_FIRST = "pref_first_run";
    private static String excelPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // These next 3 setProperty lines are REQUIRED for excel parsing (read: the app) to work
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String path = prefs.getString(KEY_PREF_PATH, "");
        path = Utility.stripColon(path);
        if (scanResult != null)
        {
            String qrCode = scanResult.getContents();
            Log.d("SCAN_RESULT", "qrCode: " + qrCode);

            File excelFile = findExcelFile(path);
            if (excelFile != null)
            {
                try
                {
                    InputStream inputStream = new FileInputStream(path);
                    Workbook workbook = WorkbookFactory.create(inputStream);
                    String[] rawComputerInfo = ExcelParsing.getRawComputerInfo(qrCode, workbook);
                    inputStream.close();
                    workbook.close();
                    if (rawComputerInfo.length == 2)
                    {
                        displayError(rawComputerInfo[0], rawComputerInfo[1]);
                    }
                    else
                    {
                        String[] orderedInfo = Organizer.OrderRawComputerInfo(rawComputerInfo);
                        if (orderedInfo.length == 2)
                        {
                            displayError(orderedInfo[0], orderedInfo[1]);
                        }
                        else
                        {
                            displayInfo(orderedInfo);
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object o)
    {
        switch (preference.getKey())
        {
            case KEY_PREF_PATH:
                excelPath = (String)o;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri)
    {

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

    public void showSettings()
    {
        getFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, SettingsFragment.newInstance())
                .commit();
    }

    // Sends ordered computer info to next activity where it can be displayed
    private void displayInfo(String[] orderedComputerInfo)
    {
        Intent intent = null;
        if (orderedComputerInfo.length == Utility.ADMIN_ORDERED_SIZE)
        {
            intent = new Intent(this, AdminInfoActivity.class);

        }
        else if (orderedComputerInfo.length == Utility.ACAD_ORDERED_SIZE)
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
}
