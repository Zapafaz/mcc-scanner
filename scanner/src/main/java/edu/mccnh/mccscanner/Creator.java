package edu.mccnh.mccscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfDocument;
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
    private final int cellPadding = 20;
    private final int labelBottomPadding = 0;
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

        PdfDocument document = new PdfDocument();
        document.open();
        document.add(fullTable);
        document.addTitle(fileName);
        document.addAuthor(context.getString(R.string.app_name));
        OutputStream stream = new FileOutputStream( folder + fileName);
        PdfWriter writer = PdfWriter.getInstance(document, stream);
        document.addWriter(writer);
    }

    private PdfPTable createAdminTable()
            throws Exception
    {
        PdfPTable table = new PdfPTable(Utility.ADMIN_PDF_COL_COUNT);
        table.setHeaderRows(1);
        table.addCell(createHeaderCell(context.getString(R.string.admin_header_label) + " id: " + adminComputerInfo.getId(), Utility.ADMIN_PDF_COL_COUNT));
        table.completeRow();
        table.addCell(createLabelCell(context.getString(R.string.last_name), Utility.ADMIN_3_COL_ROW_SPAN));
        table.addCell(createLabelCell(context.getString(R.string.first_name), Utility.ADMIN_3_COL_ROW_SPAN));
        table.addCell(createLabelCell(context.getString(R.string.department), Utility.ADMIN_3_COL_ROW_SPAN));
        table.completeRow();
        table.addCell(createCell(adminComputerInfo.getLastName(), Utility.ADMIN_3_COL_ROW_SPAN));
        table.addCell(createCell(adminComputerInfo.getFirstName(), Utility.ADMIN_3_COL_ROW_SPAN));
        table.addCell(createCell(adminComputerInfo.getDepartment(), Utility.ADMIN_3_COL_ROW_SPAN));
        table.completeRow();
        table.setWidthPercentage(100);
        return table;
    }

    private PdfPTable createAcadTable()
            throws Exception
    {
        PdfPTable table = new PdfPTable(Utility.ACAD_PDF_COL_COUNT);
        table.addCell("");

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

    private File getOutputFolder()
            throws Exception
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String path = prefs.getString(MainActivity.KEY_PREF_PATH, "");
        path = Utility.stripColon(path);
        File scanner = new File (new File(path).getParent() + "/Scanner/");
        if (!scanner.exists())
        {
            scanner.mkdir();
        }
        File folder = new File(scanner + new SimpleDateFormat("MMMdd", Locale.US).format(Calendar.getInstance().getTime()));
        if(!folder.exists())
        {
            folder.mkdir();
        }
        return folder;
    }
}
