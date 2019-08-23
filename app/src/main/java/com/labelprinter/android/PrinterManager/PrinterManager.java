package com.labelprinter.android.PrinterManager;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.citizen.sdk.labelprint.LabelConst;
import com.citizen.sdk.labelprint.LabelDesign;
import com.citizen.sdk.labelprint.LabelPrinter;
import com.labelprinter.android.Common.Common;
import com.labelprinter.android.Models.PrinterInfo;
import com.labelprinter.android.Models.TicketInfo;
import com.labelprinter.android.Models.TicketModel;
import com.labelprinter.android.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static com.labelprinter.android.Common.Common.cm;
import static com.labelprinter.android.Common.Common.currentActivity;

public class PrinterManager {

    private ArrayList<TicketInfo> ticketInfos;

    public PrinterManager () {
        //
        // Runtime Permission
        //
        getPermissions(permissions);
    }

    public LabelPrinter printerStart(ArrayList<TicketInfo> infos, long invoceMoney, String invoiceName) {
        this.ticketInfos = infos;
        // Constructor
        LabelPrinter printer = new LabelPrinter();

        // Set context
        printer.setContext(currentActivity);

        // Get Address
        UsbDevice usbDevice = null;                                               // null (Automatic detection)

        // Connect
        int result = printer.connect(LabelConst.CLS_PORT_USB, usbDevice);       // Android 3.1 ( API Level 12 ) or later
        if (LabelConst.CLS_SUCCESS == result) {
            // Set Property (Measurement unit)
            printer.setMeasurementUnit(LabelConst.CLS_UNIT_MILLI);

            for (int infoNum=0; infoNum<ticketInfos.size(); infoNum++) {
                TicketInfo info = ticketInfos.get(infoNum);
                TicketModel model = info.getModel();
                for (int modelNum=0; modelNum<info.getNum(); modelNum++) {
                    // Create an instance (LabelDesign Class)
                    LabelDesign design = new LabelDesign();

                    // Design label
                    getDesignFromTicketInfo(design, model);
                    cm.hasPrintingErr = false;

                    if (infoNum == ticketInfos.size()-1 && modelNum == info.getNum()-1) {
                        // Set Property (Cut on last page)
                        printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_CUT);

                        // Print label

                        int printResult = printer.print(design, 1);
                        if (LabelConst.CLS_SUCCESS != printResult) {
                            cm.hasPrintingErr = true;
                        }
                        if (invoceMoney > 0) {
                            // Set Property (Cut on last page)
                            printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_CUT);

                            // Print label

                            getDesignFromInvoiceInfo(design, invoceMoney, invoiceName);
                            printResult = printer.print(design, 1);
                            if (LabelConst.CLS_SUCCESS != printResult) {
                                cm.hasPrintingErr = true;
                            }
                        }
                    }else {
                        // Set Property (Tear Off)
                        printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_TEAROFF);

                        // Print design
                        int printResult = printer.print(design, 1);
                        if (LabelConst.CLS_SUCCESS != printResult) {
                            cm.hasPrintingErr = true;
                        }
                    }
                }
                if (invoceMoney > 0) {

                }
            }

            // Disconnect
            printer.disconnect();
        } else {
            // Connect Error
            Toast.makeText(currentActivity, "Connect or Printer Error : " + Integer.toString(result), Toast.LENGTH_LONG).show();
            return null;
        }

        return printer;
    }

    public LabelPrinter invoiceOnlyPrintStart(long invoceMoney, String invoiceName) {
        // Constructor
        LabelPrinter printer = new LabelPrinter();

        // Set context
        printer.setContext(currentActivity);

        // Get Address
        UsbDevice usbDevice = null;                                               // null (Automatic detection)

        // Connect
        int result = printer.connect(LabelConst.CLS_PORT_USB, usbDevice);       // Android 3.1 ( API Level 12 ) or later
        if (LabelConst.CLS_SUCCESS == result) {
            LabelDesign design = new LabelDesign();

            // Set Property (Measurement unit)
            printer.setMeasurementUnit(LabelConst.CLS_UNIT_MILLI);

            // Set Property (Cut on last page)
            printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_CUT);

            // Print label

            getDesignFromInvoiceInfo(design, invoceMoney, invoiceName);
            cm.hasPrintingErr = false;
            int printResult = printer.print(design, 1);
            if (LabelConst.CLS_SUCCESS != printResult) {
                cm.hasPrintingErr = true;
            }

            // Disconnect
            printer.disconnect();
        } else {
            // Connect Error
            Toast.makeText(currentActivity, "Connect or Printer Error : " + Integer.toString(result), Toast.LENGTH_LONG).show();
            return null;
        }

        return printer;
    }

    public LabelPrinter settlementPrinterStart(String title, ArrayList<HashMap> mainList, ArrayList<HashMap> subList) {
        // Constructor
        LabelPrinter printer = new LabelPrinter();

        // Set context
        printer.setContext(currentActivity);

        // Get Address
        UsbDevice usbDevice = null;                                               // null (Automatic detection)

        // Connect
        int result = printer.connect(LabelConst.CLS_PORT_USB, usbDevice);       // Android 3.1 ( API Level 12 ) or later
        if (LabelConst.CLS_SUCCESS == result) {
            // Set Property (Measurement unit)
            printer.setMeasurementUnit(LabelConst.CLS_UNIT_MILLI);
            LabelDesign design = new LabelDesign();

            getDesignFromMainList(design, mainList, title);

            printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_CUT);
            int printResult = printer.print(design, 1);
            if (LabelConst.CLS_SUCCESS != printResult) {
                cm.hasPrintingErr = true;
            }

            getDesignFromSubList(design, subList, title);

            printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_CUT);
            printResult = printer.print(design, 1);
            if (LabelConst.CLS_SUCCESS != printResult) {
                cm.hasPrintingErr = true;
            }

            // Disconnect
            printer.disconnect();
        } else {
            // Connect Error
            Toast.makeText(currentActivity, "Connect or Printer Error : " + Integer.toString(result), Toast.LENGTH_LONG).show();
            return null;
        }

        return printer;
    }

    private void getDesignFromTicketInfo (LabelDesign design, TicketModel model) {

        if (cm.printerInfos.size() >0) {
            for (PrinterInfo info : cm.printerInfos) {
                if (info.getType().equals("RYOSHUSHO"))
                    continue;
                String content = "";
                Calendar nowDate = Calendar.getInstance();

                String fontName = info.getFont();
                int fontSize = (int) info.getFontSize();
                int kanjiSize = LabelConst.CLS_PRT_FNT_KANJI_SIZE_16;
                if (fontSize <= 16) {
                    kanjiSize = LabelConst.CLS_PRT_FNT_KANJI_SIZE_16;
                }else if (fontSize > 16 && fontSize <= 24) {
                    kanjiSize = LabelConst.CLS_PRT_FNT_KANJI_SIZE_24;
                }else if (fontSize > 24 && fontSize <= 32) {
                    kanjiSize = LabelConst.CLS_PRT_FNT_KANJI_SIZE_32;
                }else if (fontSize > 32 && fontSize <= 48) {
                    kanjiSize = LabelConst.CLS_PRT_FNT_KANJI_SIZE_48;
                }

                int style = LabelConst.CLS_FNT_DEFAULT;
                if (info.getIsBold() > 0) {
                    if (info.getIsItalic() > 0) {
                        style = (LabelConst.CLS_FNT_BOLD | LabelConst.CLS_FNT_ITALIC);
                    }else {
                        style = LabelConst.CLS_FNT_BOLD;
                    }
                }else {
                    if (info.getIsItalic() > 0) {
                        style = LabelConst.CLS_FNT_ITALIC;
                    }
                }

                int startX = (int) (info.getStartX()); // mm
                int startY = (int) (info.getStartY()); // mm
                int endX = (int) (info.getEndX()); // mm
                int endY = (int) (info.getEndY()); // mm

                if (info.getPrinterType().equals("TEXT")) {
                    if (info.getFormat().equals("HAKKENBI")) { //発券日(当日)を示す
                        content = nowDate.get(Calendar.YEAR) + "年 " + (nowDate.get(Calendar.MONTH)+1) + "月 " + nowDate.get(Calendar.DAY_OF_MONTH) + "日";
                    }else if (info.getFormat().equals("YUKOKIGEN")) { //チケット定義情報の有効期間(日) + 発券日を示す
                        content = "発券日 " + nowDate.get(Calendar.YEAR) + "年 " + (nowDate.get(Calendar.MONTH)+1) + "月 " + nowDate.get(Calendar.DAY_OF_MONTH) + "日";
                    }else if (info.getFormat().equals("KENSHUMEI")) { //チケット定義情報の券種名を示す 券種名(1日　大人など）を示す
                        content = model.getName();
                    }else if (info.getFormat().equals("KAKAKU")) { //チケットの価格を示す
                        content = String.valueOf(model.getPrice());
                    }else if (info.getFormat().equals("GENZAINICHIJI")) { //現在日時を示す
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日(E) hh:mm", Locale.JAPANESE);
                        content = sdf.format(nowDate);
                    }

//                    case 1
//                    drawTextPtrFont (String data, int locale, int font, int rotation, int hexp, int vexp, int size, int x, int y)
//                    design.drawTextPtrFont(content,
//                            LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
//                            LabelConst.CLS_RT_NORMAL, 1, 1,
//                            kanjiSize, startX, startY);

//                    case 2
//                    drawTextDLFont (String data, int encoding, String fontID, int rotation, int hexp, int vexp, int point, int x, int y)
                    // TrueTypeダウンロードフォント
//                    design.drawTextDLFont(content,
//                            LabelConst.CLS_ENC_CDPG_IBM850, "S50",
//                            LabelConst.CLS_RT_NORMAL, 1, 1, fontSize, startX, startY);

//                    case 3
//                    drawTextLocalFont (String data, Typeface fontType, int rotation, int hRatio, int vRatio, int point, int style, int x, int y, int resolution, int measurementUnit)
                    // 解像度指定 203dpi、単位指定 mm
                    design.drawTextLocalFont(content, Typeface.create(fontName, Typeface.NORMAL),
                            LabelConst.CLS_RT_NORMAL, 100, 100, fontSize,
                            style, startX, startY,
                            LabelConst.CLS_PRT_RES_203, LabelConst.CLS_UNIT_MILLI);

                }else if (info.getPrinterType().equals("IMAGE")) {
                    content = info.getFileName();
//                    drawNVBitmap (String name, int hexp, int vexp, int x, int y)
                    design.drawNVBitmap (content, 1, 1, startX, startY);

//                    drawBitmap (String filePath, int rotation, int width, int height, int x, int y)
//                    File root = android.os.Environment.getExternalStorageDirectory();
//                    File dir = new File(root.getAbsolutePath() + "/LabelPrinter");
//                    if(dir.exists()) {
//                        File file = new File(dir, content);
//                        design.drawBitmap (file.getAbsolutePath(), LabelConst.CLS_RT_NORMAL, endX - startX, endY - startY, startX, startY);
////                    drawBitmap (String filePath, int rotation, int width, int height, int x, int y, int resolution, int measurementUnit)
//                        design.drawBitmap (file.getAbsolutePath(), LabelConst.CLS_RT_NORMAL, endX - startX, endY - startY, startX, startY,
//                                LabelConst.CLS_PRT_RES_300, LabelConst.CLS_UNIT_MILLI);
//                    }

                }else if (info.getPrinterType().equals("BARCODE")) {
//                    drawBarCode (String data, int symbology, int rotation, int thick, int narrow, int height, int x, int y,int showText)
//                    String interleaved25 = "1234567890";
//                    design.drawBarCode(interleaved25, LabelConst.CLS_BCS_INTERLEAVED25,
//                            LabelConst.CLS_RT_NORMAL, 5, 2, 50, 10, 100,
//                            LabelConst.CLS_BCS_TEXT_SHOW);
                }else if (info.getPrinterType().equals("LINE")) {
//                    drawLine (int x1, int y1, int x2, int y2, int thickness)
                    design.drawLine (startX, startY, endX, endY, 1);
                }
            }
        }else {
            Common.cm.showAlertDlg(currentActivity.getResources().getString(R.string.printer_connect_err_title),
                    currentActivity.getResources().getString(R.string.printer_connect_err_msg), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, null);
            return;
        }
    }

    private void getDesignFromInvoiceInfo (LabelDesign design, long invoiceMoney, String invoiceName) {
        if (cm.printerInfos.size() >0) {
            for (PrinterInfo info : cm.printerInfos) {
                if (info.getType().equals("TICKET"))
                    continue;
                String content = String.valueOf(invoiceMoney);
                Calendar nowDate = Calendar.getInstance();

                String fontName = info.getFont();
                int fontSize = (int) info.getFontSize();

                int style = LabelConst.CLS_FNT_DEFAULT;
                if (info.getIsBold() > 0) {
                    if (info.getIsItalic() > 0) {
                        style = (LabelConst.CLS_FNT_BOLD | LabelConst.CLS_FNT_ITALIC);
                    }else {
                        style = LabelConst.CLS_FNT_BOLD;
                    }
                }else {
                    if (info.getIsItalic() > 0) {
                        style = LabelConst.CLS_FNT_ITALIC;
                    }
                }

                int startX = (int) (info.getStartX()); // mm
                int startY = (int) (info.getStartY()); // mm
                int endX = (int) (info.getEndX()); // mm
                int endY = (int) (info.getEndY()); // mm

                if (info.getPrinterType().equals("TEXT")) {
//                    if (info.getFormat().equals("HAKKENBI")) { //発券日(当日)を示す
//                        content = nowDate.get(Calendar.YEAR) + "年 " + (nowDate.get(Calendar.MONTH)+1) + "月 " + nowDate.get(Calendar.DAY_OF_MONTH) + "日";
//                    }else if (info.getFormat().equals("YUKOKIGEN")) { //チケット定義情報の有効期間(日) + 発券日を示す
//                        content = "発券日 " + nowDate.get(Calendar.YEAR) + "年 " + (nowDate.get(Calendar.MONTH)+1) + "月 " + nowDate.get(Calendar.DAY_OF_MONTH) + "日";
//                    }else if (info.getFormat().equals("KENSHUMEI")) { //チケット定義情報の券種名を示す 券種名(1日　大人など）を示す
//                        content = model.getName();
//                    }else if (info.getFormat().equals("KAKAKU")) { //チケットの価格を示す
//                        content = String.valueOf(model.getPrice());
//                    }else if (info.getFormat().equals("GENZAINICHIJI")) { //現在日時を示す
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日(E) hh:mm", Locale.JAPANESE);
//                        content = sdf.format(nowDate);
//                    }

//                    case 1
//                    drawTextPtrFont (String data, int locale, int font, int rotation, int hexp, int vexp, int size, int x, int y)
//                    design.drawTextPtrFont(content,
//                            LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
//                            LabelConst.CLS_RT_NORMAL, 1, 1,
//                            kanjiSize, startX, startY);

//                    case 2
//                    drawTextDLFont (String data, int encoding, String fontID, int rotation, int hexp, int vexp, int point, int x, int y)
                    // TrueTypeダウンロードフォント
//                    design.drawTextDLFont(content,
//                            LabelConst.CLS_ENC_CDPG_IBM850, "S50",
//                            LabelConst.CLS_RT_NORMAL, 1, 1, fontSize, startX, startY);

//                    case 3
//                    drawTextLocalFont (String data, Typeface fontType, int rotation, int hRatio, int vRatio, int point, int style, int x, int y, int resolution, int measurementUnit)
                    // 解像度指定 203dpi、単位指定 mm
                    design.drawTextLocalFont(content, Typeface.create(fontName, Typeface.NORMAL),
                            LabelConst.CLS_RT_NORMAL, 100, 100, fontSize,
                            style, startX, startY,
                            LabelConst.CLS_PRT_RES_203, LabelConst.CLS_UNIT_MILLI);

                }else if (info.getPrinterType().equals("IMAGE")) {
                    content = info.getFileName();
//                    drawNVBitmap (String name, int hexp, int vexp, int x, int y)
                    design.drawNVBitmap (content, 1, 1, startX, startY);

//                    drawBitmap (String filePath, int rotation, int width, int height, int x, int y)
//                    File root = android.os.Environment.getExternalStorageDirectory();
//                    File dir = new File(root.getAbsolutePath() + "/LabelPrinter");
//                    if(dir.exists()) {
//                        File file = new File(dir, content);
//                        design.drawBitmap (file.getAbsolutePath(), LabelConst.CLS_RT_NORMAL, endX - startX, endY - startY, startX, startY);
////                    drawBitmap (String filePath, int rotation, int width, int height, int x, int y, int resolution, int measurementUnit)
//                        design.drawBitmap (file.getAbsolutePath(), LabelConst.CLS_RT_NORMAL, endX - startX, endY - startY, startX, startY,
//                                LabelConst.CLS_PRT_RES_300, LabelConst.CLS_UNIT_MILLI);
//                    }

                }else if (info.getPrinterType().equals("BARCODE")) {
//                    drawBarCode (String data, int symbology, int rotation, int thick, int narrow, int height, int x, int y,int showText)
//                    String interleaved25 = "1234567890";
//                    design.drawBarCode(interleaved25, LabelConst.CLS_BCS_INTERLEAVED25,
//                            LabelConst.CLS_RT_NORMAL, 5, 2, 50, 10, 100,
//                            LabelConst.CLS_BCS_TEXT_SHOW);
                }else if (info.getPrinterType().equals("LINE")) {
//                    drawLine (int x1, int y1, int x2, int y2, int thickness)
                    design.drawLine (startX, startY, endX, endY, 1);
                }
            }
        }else {
            Common.cm.showAlertDlg(currentActivity.getResources().getString(R.string.printer_connect_err_title),
                    currentActivity.getResources().getString(R.string.printer_connect_err_msg), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, null);
            return;
        }
    }

    private void getDesignFromMainList(LabelDesign design, ArrayList<HashMap> list, String title) {

        HashMap header = new HashMap();
        header.put("code", currentActivity.getResources().getString(R.string.lb_code));
        header.put("type", currentActivity.getResources().getString(R.string.lb_type));
        header.put("unit", currentActivity.getResources().getString(R.string.lb_unit));
        header.put("sell", currentActivity.getResources().getString(R.string.lb_sell));
        header.put("refund", currentActivity.getResources().getString(R.string.lb_refund));
        header.put("total", currentActivity.getResources().getString(R.string.lb_total));
        header.put("price", currentActivity.getResources().getString(R.string.lb_price));
        list.add(0, header);

        int startY = 0;

        design.drawTextPtrFont(title,
                LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                LabelConst.CLS_RT_NORMAL, 1, 1,
                LabelConst.CLS_PRT_FNT_KANJI_SIZE_24, 50, startY);
        startY += 100;
        design.drawLine (0, startY, 1000, startY, 2);
        startY += 30;

        for (int i=0; i<list.size(); i++) {
            HashMap map = new HashMap();
            design.drawTextPtrFont(String.valueOf(map.get("code")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 10, startY);
            design.drawTextPtrFont(String.valueOf(map.get("type")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 90, startY);
            design.drawTextPtrFont(String.valueOf(map.get("unit")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 290, startY);
            design.drawTextPtrFont(String.valueOf(map.get("sell")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 440, startY);
            design.drawTextPtrFont(String.valueOf(map.get("refund")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 520, startY);
            design.drawTextPtrFont(String.valueOf(map.get("total")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 600, startY);
            design.drawTextPtrFont(String.valueOf(map.get("price")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 700, startY);
            startY += 70;
            design.drawLine (0, startY, 1000, startY, 1);
            startY += 30;
        }
    }

    private void getDesignFromSubList(LabelDesign design, ArrayList<HashMap> list, String title) {
        int startY = 0;

        design.drawTextPtrFont(title,
                LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                LabelConst.CLS_RT_NORMAL, 1, 1,
                LabelConst.CLS_PRT_FNT_KANJI_SIZE_24, 50, startY);
        startY += 100;
        design.drawLine (0, startY, 1000, startY, 2);
        startY += 30;

        for (int i=0; i<list.size(); i++) {
            HashMap map = new HashMap();
            design.drawTextPtrFont(String.valueOf(map.get("title")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 10, startY);
            design.drawTextPtrFont(String.valueOf(map.get("num")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 210, startY);
            design.drawTextPtrFont(String.valueOf(map.get("price")),
                    LabelConst.CLS_LOCALE_JP, LabelConst.CLS_PRT_FNT_KANJI,
                    LabelConst.CLS_RT_NORMAL, 1, 1,
                    LabelConst.CLS_PRT_FNT_KANJI_SIZE_16, 310, startY);
            startY += 70;
            design.drawLine (0, startY, 1000, startY, 1);
            startY += 30;
        }
    }


        /**
         * Permissions
         * @see <a href="https://developer.android.com/guide/topics/security/permissions#perm-groups">Dangerous permissions and permission groups.</a>
         */
    final String[] permissions = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    /** requestCode for {@link ActivityCompat.OnRequestPermissionsResultCallback} @Override#onRequestPermissionsResult*/
    private static final int PERMISSION_REQUEST_CODE = 1234;

    /**
     * Get the permissions.
     * @param permissions ex. {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}
     */
    private void getPermissions(@NonNull final String[] permissions) {

        if (!checkPermissions(permissions)) {
            requestPermissions(permissions);
        }
    }
    /**
     * Checks whether this app has all permissions.<br>
     * @param permissions ex. {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}
     * @return true:All permissions granted, false:Permission denied
     */
    private boolean checkPermissions(@NonNull final String[] permissions) {

        for (String permission : permissions) {
            int permissionResult = ContextCompat.checkSelfPermission(currentActivity, permission);
            if (permissionResult != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return false;				// non-permission
            }
        }
        return true;
    }
    /**
     * Requests permissions to be granted to this app.<br>
     * @param permissions ex. {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}
     */
    private void requestPermissions(@NonNull final String[] permissions) {
        ActivityCompat.requestPermissions(currentActivity, permissions, PERMISSION_REQUEST_CODE);
    }
}
