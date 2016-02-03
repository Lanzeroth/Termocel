package com.ocr.termocel.utilities;

import com.ocr.termocel.model.Temperature;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Gets the Temperatures DataSet and writes it to an XLS file
 */
public class ExcelReportWriter {
    private WritableCellFormat timesBoldUnderline;
    private WritableCellFormat arial;
    private File inputFile;
    private List<Temperature> mDataSet;


    public void setOutputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setData(List<Temperature> dataSet) {
        mDataSet = dataSet;
    }

    public File write() throws IOException, WriteException {
//        File file = new File(inputFile);
        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("en", "EN"));

        WritableWorkbook workbook = Workbook.createWorkbook(inputFile, wbSettings);
        workbook.createSheet("Report", 0);
        WritableSheet excelSheet = workbook.getSheet(0);
        createLabel(excelSheet);
        createContent(excelSheet);

        workbook.write();
        workbook.close();

        return inputFile;
    }

    private void createLabel(WritableSheet sheet)
            throws WriteException {
        // Lets create a arial font
        WritableFont arial10pt = new WritableFont(WritableFont.ARIAL, 10);
        // Define the cell format
        arial = new WritableCellFormat(arial10pt);
        // Lets automatically wrap the cells
        arial.setWrap(true);

        // create create a bold font with unterlines
        WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false,
                UnderlineStyle.SINGLE);
        timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
        // Lets automatically wrap the cells
        timesBoldUnderline.setWrap(true);

        CellView cv = new CellView();
        cv.setFormat(arial);
        cv.setFormat(timesBoldUnderline);
        cv.setAutosize(true);

        // Write a few headers
        addCaption(sheet, 0, 0, "Estado");
        addCaption(sheet, 1, 0, "Temperatura F");
        addCaption(sheet, 2, 0, "Humedad");
        addCaption(sheet, 3, 0, "Fecha");


    }

    private void createContent(WritableSheet sheet) throws WriteException,
            RowsExceededException {
        // Write a few number
        for (int i = 0; i < mDataSet.size(); i++) {

            Temperature current = mDataSet.get(i);

            addLabel(sheet, 0, i + 1, current.status);
            addDouble(sheet, 1, i + 1, current.tempInFahrenheit);
            addDouble(sheet, 2, i + 1, current.humidity);

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(mDataSet.get(i).timestamp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault());
            simpleDateFormat.setCalendar(calendar);

            addLabel(sheet, 3, i + 1, simpleDateFormat.format(calendar.getTime()));
        }
    }

    private void addCaption(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        label = new Label(column, row, s, timesBoldUnderline);
        sheet.addCell(label);
    }

    private void addNumber(WritableSheet sheet, int column, int row,
                           Integer integer) throws WriteException, RowsExceededException {
        Number number;
        number = new Number(column, row, integer, arial);
        sheet.addCell(number);
    }

    private void addDouble(WritableSheet sheet, int column, int row,
                           Double doubleNumber) throws WriteException, RowsExceededException {
        Number number;
        number = new Number(column, row, doubleNumber, arial);
        sheet.addCell(number);
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, arial);
        sheet.addCell(label);
    }

}
