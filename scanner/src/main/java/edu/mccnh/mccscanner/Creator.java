package edu.mccnh.mccscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.mccnh.mccscanner.activities.MainActivity;
import edu.mccnh.mccscanner.datastorage.AcadComputerInfo;
import edu.mccnh.mccscanner.datastorage.AdminComputerInfo;
import edu.mccnh.mccscanner.datastorage.ComputerInfo;
import edu.mccnh.mccscanner.datastorage.ComputerInfoType;

/**
 * Created by Adam on 12/3/2017.
 * For CIS291M Capstone Senior Seminar
 * Instructor: Adnan Tahir
 */

public class Creator
{
    private final int cellPadding = 12;
    private final int labelBottomPadding = 5;
    private String fileName;
    private AcadComputerInfo acadComputerInfo;
    private AdminComputerInfo adminComputerInfo;
    private ComputerInfoType type;
    private Context context;
    private Font headerFont = new Font(Font.FontFamily.HELVETICA, 24f, Font.BOLDITALIC);
    private Font labelFont = new Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD);
    private Font cellFont = new Font(Font.FontFamily.HELVETICA, 14f);

    public Creator(String fileName, ComputerInfo info, Context context)
    {
        this.fileName = fileName;
        type = info.getType();
        if (type == ComputerInfoType.Admin)
        {
            adminComputerInfo = (AdminComputerInfo)info;
        }
        else
        {
            acadComputerInfo = (AcadComputerInfo)info;
        }
        this.context = context;
    }

    public void createPdf()
            throws Exception
    {
        if ((adminComputerInfo == null && acadComputerInfo == null))
        {
            throw new NullPointerException("Tried to create pdf with no computer information");
        }
        else if (fileName == null)
        {
            throw new NullPointerException("Tried to create pdf with no file name");
        }

        PdfPTable fullTable;
        if (type == ComputerInfoType.Admin)
        {
            fullTable = createAdminTable();
        }
        else
        {
            fullTable = createAcadTable();
        }

        File folder = getOutputFolder();
        Document document = new Document(PageSize.LETTER);
        OutputStream stream = new FileOutputStream( folder + "/" + fileName);
        PdfWriter.getInstance(document, stream);
        document.open();
        document.add(fullTable);
        document.addTitle(fileName);
        document.addAuthor(context.getString(R.string.app_name));
        document.close();
    }

    // Creates a table with all of the admin info in it.
    // TODO: rewrite this (and createAcadTable) with loops - use two String array, one for header text & one for info
    // This and createAcadTable are quite possibly the worst code I've written - should really not be hardcoded like this but I'm too lazy to write loops for it
    private PdfPTable createAdminTable()
            throws Exception
    {
        int oneColumnRow = Utility.ADMIN_PDF_COL_COUNT;
        PdfPTable table = new PdfPTable(oneColumnRow);
        int threeColumnRow = Utility.ADMIN_3_COL_ROW_SPAN;
        int twoColumnRow = Utility.ADMIN_2_COL_ROW_SPAN;
        table.setHeaderRows(1);
        table.addCell(createHeaderCell(context.getString(R.string.admin_header_label) + " id: " + adminComputerInfo.getId(), oneColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.last_name), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.first_name), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.department), threeColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getLastName(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getFirstName(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getDepartment(), threeColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.comp_name), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.model), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.serial), threeColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getComputerName(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getModel(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getSerialNumber(), threeColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.purchased), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.warranty), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.comp_age), threeColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getDatePurchased(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getWarrantyStatus(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getSerialNumber(), threeColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.pw_changed), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.reimaged), threeColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.lvl_usage_header), threeColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getLastPasswordChange(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getLastReimage(), threeColumnRow));
        table.addCell(createCell(adminComputerInfo.getPcLevel() + " : : " + adminComputerInfo.getUsageScale(), threeColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.mac_wired), twoColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.mac_wireless), twoColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getMacAddressWired(), twoColumnRow));
        table.addCell(createCell(adminComputerInfo.getMacAddressWireless(), twoColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.monitor_count), twoColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.monitor_size), twoColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getMonitorCount(), twoColumnRow));
        table.addCell(createCell(adminComputerInfo.getMonitorSize(), twoColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.phone_ext), twoColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.phone_type), twoColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getPhoneExtension(), twoColumnRow));
        table.addCell(createCell(adminComputerInfo.getPhoneType(), twoColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.notes), oneColumnRow));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getNotes(), oneColumnRow));
        table.completeRow();
        table.setWidthPercentage(100);
        return table;
    }

    // Create a table for the academic side info.
    private PdfPTable createAcadTable()
            throws Exception
    {
        int oneColumnRow = Utility.ACAD_PDF_COL_COUNT;
        PdfPTable table = new PdfPTable(oneColumnRow);
        int fourColumnRow = Utility.ACAD_4_COL_ROW_SPAN;
        int twoColumnRow = Utility.ACAD_2_COL_ROW_SPAN;
        table.setHeaderRows(1);
        table.addCell(createHeaderCell(context.getString(R.string.acad_header_label) + "id: " + acadComputerInfo.getId(), oneColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.room_area), fourColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.comp_count), fourColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.lvl_usage_header), fourColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.comp_age), fourColumnRow));
        table.completeRow();
        table.addCell(createCell(acadComputerInfo.getRoomArea(), fourColumnRow));
        table.addCell(createCell(acadComputerInfo.getNumberOfComputers(), fourColumnRow));
        table.addCell(createCell(acadComputerInfo.getPcLevel() + " : : " + acadComputerInfo.getUsageScale(), fourColumnRow));
        table.addCell(createCell(acadComputerInfo.getPcAge(), fourColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.purchased), fourColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.warranty), fourColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.model), fourColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.serial), fourColumnRow));
        table.completeRow();
        table.addCell(createCell(acadComputerInfo.getDatePurchased(), fourColumnRow));
        table.addCell(createCell(acadComputerInfo.getWarrantyStatus(), fourColumnRow));
        table.addCell(createCell(acadComputerInfo.getModel(), fourColumnRow));
        table.addCell(createCell(acadComputerInfo.getSerialNumber(), fourColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.mac_wired), twoColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.mac_wireless), twoColumnRow));
        table.completeRow();
        table.addCell(createCell(acadComputerInfo.getMacAddressWired(), twoColumnRow));
        table.addCell(createCell(acadComputerInfo.getMacAddressWireless(), twoColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.monitor_count), twoColumnRow));
        table.addCell(createLabelCell(context.getString(R.string.monitor_size), twoColumnRow));
        table.completeRow();
        table.addCell(createCell(acadComputerInfo.getMonitorCount(), twoColumnRow));
        table.addCell(createCell(acadComputerInfo.getMonitorSize(), twoColumnRow));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.notes), oneColumnRow));
        table.completeRow();
        table.addCell(createCell(acadComputerInfo.getNotes(), oneColumnRow));
        table.completeRow();

        table.setWidthPercentage(100);
        return table;
    }

    private PdfPCell createCell(String text, int colSpan)
            throws Exception
    {
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
        cell.setColspan(colSpan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(cellPadding);
        return cell;
    }

    private PdfPCell createLabelCell(String text, int colSpan)
            throws Exception
    {
        PdfPCell cell = new PdfPCell(new Phrase(text, labelFont));
        cell.setColspan(colSpan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(cellPadding);
        cell.setPaddingBottom(labelBottomPadding);
        return cell;
    }

    private PdfPCell createHeaderCell(String text, int colSpan)
            throws Exception
    {
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setColspan(colSpan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(cellPadding);
        return cell;
    }

    // Get output folder, based on current input file preference. Returns folder with separator (/) as last character.
    @SuppressWarnings("ResultOfMethodCallIgnored") // Don't care about the booleans from File.mkdir
    private File getOutputFolder()
            throws Exception
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String path = prefs.getString(MainActivity.KEY_PREF_PATH, "");
        path = Utility.stripColon(path);
        File scanner;
        // If input folder is already a "Scanner" folder, don't make new folder named Scanner
        if (path.contains("Scanner"))
        {
            scanner = new File (new File(path).getParent());
        }
        else
        {
            scanner = new File (new File(path).getParent() + "/Scanner");
        }
        if (!scanner.exists())
        {
            scanner.mkdir();
            Log.d("DIR CREATED", "getOutputFolder: " + scanner.toString());
        }
        File folder = new File(scanner + "/" + new SimpleDateFormat("MMMdd", Locale.US).format(Calendar.getInstance().getTime()));
        if(!folder.exists())
        {
            folder.mkdir();
            Log.d("DIR CREATED", "getOutputFolder: " + folder.toString());
        }
        Log.d("RETURN DIR", "getOutputFolder: " + folder.toString());
        return folder;
    }
}
