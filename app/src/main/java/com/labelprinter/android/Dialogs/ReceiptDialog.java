package com.labelprinter.android.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.citizen.sdk.labelprint.LabelPrinter;
import com.labelprinter.android.Common.Common;
import com.labelprinter.android.Common.LocalStorageManager;
import com.labelprinter.android.PrinterManager.PrinterManager;
import com.labelprinter.android.R;

import static com.labelprinter.android.Common.Common.cm;
import static com.labelprinter.android.Common.Common.currentActivity;

public class ReceiptDialog extends Dialog {

    private long money = 0;
    private EditText moneyTxt, receiptTxt;
    private ReceiptPrinterListner listner;

    public ReceiptDialog(@NonNull Context context, final ReceiptPrinterListner listner) {
        super(context);
        setContentView(R.layout.receipt_dialog);
        this.listner = listner;

        moneyTxt = findViewById(R.id.preMoney);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(10);
        moneyTxt.setFilters(FilterArray);
        moneyTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                moneyTxt.setSelection(count);
                String val = String.valueOf(s).replace(",", "");
                if (val.equals("")) val = "0";
                if (!String.valueOf(money).equals(val)) {
                    money = Long.valueOf(String.valueOf(val));
                    moneyTxt.setText(cm.numberFormat(cm.parseInteger(String.valueOf(money))));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        receiptTxt = findViewById(R.id.receiptTxt);


        findViewById(R.id.ticketingReceiptBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receiptTxt.getText().toString().equals("") || money == 0) {
                    Common.cm.showAlertDlg(currentActivity.getResources().getString(R.string.input_err_title),
                            currentActivity.getResources().getString(R.string.receipt_name_err_msg), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }, null);
                    return;
                }else {
                    //print
                    if (listner != null) {
                        PrinterManager manager = new PrinterManager();
                        LabelPrinter printer = manager.receiptOnlyPrintStart(money, receiptTxt.getText().toString());

                        //test
                        if(printer != null) {
                            listner.OnReceiptBtnClicked(printer, (int) money, receiptTxt.getText().toString());
                            dismiss();
                        }
                    }
                }
            }
        });

        TextView userInfo = findViewById(R.id.userInfo);
        userInfo.setText(cm.getUserInfo());
    }

    public interface ReceiptPrinterListner {
        public abstract void OnReceiptBtnClicked(LabelPrinter printer, int value, String only);
    }
}
