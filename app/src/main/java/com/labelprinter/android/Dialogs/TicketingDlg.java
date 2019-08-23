package com.labelprinter.android.Dialogs;

import android.app.Dialog;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.citizen.sdk.labelprint.LabelPrinter;
import com.labelprinter.android.Common.Common;
import com.labelprinter.android.Models.TicketInfo;
import com.labelprinter.android.Models.TicketModel;
import com.labelprinter.android.PrinterManager.PrinterManager;
import com.labelprinter.android.R;
import com.labelprinter.android.Views.TicketListItemView;

import java.util.ArrayList;

import static com.labelprinter.android.Common.Common.cm;
import static com.labelprinter.android.Common.Common.currentActivity;

public class TicketingDlg extends Dialog {

    private LinearLayout ticketListView;
    private ArrayList<TicketInfo> models;
    private TicketingLinstener linstener;
    private int paymentType;
    private int ticketingMoney, remainMoney;
    private long preMoney = 0;
    private TextView ticketingMoneyTxt, remainMoneyTxt;
    private EditText preMoneyTxt, invoiceTxt;

    public TicketingDlg(@NonNull Context context, final int type, ArrayList<TicketInfo> datas, final TicketingLinstener linstener) {
        super(context);
        setContentView(R.layout.ticketing_dialog);

        this.models = datas;
        this.linstener = linstener;
        this.paymentType = type;

        ticketListView = findViewById(R.id.ticketListLayout);
        TextView paymentTypeTxt = findViewById(R.id.cashLb);
        paymentTypeTxt.setText(context.getResources().getStringArray(R.array.TicketingType)[paymentType]);

        TextView ticketingMoneyLb = findViewById(R.id.ticketingMoneyLb);
        if (paymentType == 0)
            ticketingMoneyLb.setText(R.string.refund_money);
        else
            ticketingMoneyLb.setText(R.string.ticketing_money);

        preMoneyTxt = findViewById(R.id.preMoney);
        invoiceTxt = findViewById(R.id.invoiceTxt);
        if (type == 4) {
            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(10);
            preMoneyTxt.setFilters(FilterArray);
            preMoneyTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    preMoneyTxt.setSelection(count);
                    String val = String.valueOf(s).replace(",", "");
                    if (val.equals("")) val = "0";
                    if (!String.valueOf(preMoney).equals(val)) {
                        preMoney = Long.valueOf(String.valueOf(val));
                        preMoneyTxt.setText(cm.numberFormat(cm.parseInteger(String.valueOf(preMoney))));
                        remainMoney = (int) (ticketingMoney - preMoney);
                        remainMoneyTxt.setText(cm.numberFormat(remainMoney) + currentActivity.getResources().getString(R.string.lb_yen));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }else {
            preMoneyTxt.setEnabled(false);
//            invoiceTxt.setEnabled(false);
        }

        Button ticketingBtn = findViewById(R.id.ticketingBtn);
        ticketingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linstener != null) {
                    if (paymentType == 0) {
                        linstener.OnRefundTicketingBtnClicked();
                        dismiss();
                    }else {
                        if (type == 4) {
                            if (preMoney == 0) {
                                Common.cm.showAlertDlg(currentActivity.getResources().getString(R.string.input_err_title),
                                        currentActivity.getResources().getString(R.string.price_err_msg), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }, null);
                                return;
                            }
                        }
                        PrinterManager manager = new PrinterManager();
                        LabelPrinter printer = manager.printerStart(models, 0, "");

                        //test
//                        if(printer != null) {
                            linstener.OnTicketingBtnClicked(printer);
                            dismiss();
//                        }
                    }
                }
            }
        });

        Button ticketingInvoiceBtn = findViewById(R.id.ticketingInvoiceBtn);
        ticketingInvoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentType == 0) {
                    linstener.OnRefundTicketingBtnClicked();
                    dismiss();
                }else {
                    if (preMoney == 0 || invoiceTxt.getText().toString().equals("")) {
                        Common.cm.showAlertDlg(currentActivity.getResources().getString(R.string.input_err_title),
                                currentActivity.getResources().getString(R.string.price_name_err_msg), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }, null);
                        return;
                    }else {
                        if (linstener != null) {
                            PrinterManager manager = new PrinterManager();
                            LabelPrinter printer = manager.printerStart(models, ticketingMoney, invoiceTxt.getText().toString());

                            if(printer != null) {
                                linstener.OnTicketingInvoiceBtnClicked(printer, Integer.valueOf((int) preMoney), invoiceTxt.getText().toString());
                                dismiss();
                            }
                        }
                    }
                }
            }
        });

        ticketingMoneyTxt = findViewById(R.id.ticketingMoney);
        remainMoneyTxt = findViewById(R.id.remainMoney);

        TextView userInfo = findViewById(R.id.userInfo);
        userInfo.setText(cm.getUserInfo());

        setTicketList();
    }

    private void setTicketList() {
        ticketListView.removeAllViews();
        int price = 0;
        for (int i=0; i<models.size(); i++) {
            TicketInfo info = models.get(i);
            TicketListItemView itemView = new TicketListItemView(cm.currentActivity, i, info, new TicketListItemView.TicketItemClickListener() {
                @Override
                public void OnTicketItemClicked(int ind) {

                }
            });
            TicketModel model = info.getModel();
            price += model.getPrice() * info.getNum();
            ticketListView.addView(itemView);
        }
        ticketingMoney = price;
        if (price > 0) ticketingMoneyTxt.setText(cm.numberFormat(price) + currentActivity.getResources().getString(R.string.lb_yen));
    }

    public interface TicketingLinstener {
        public abstract void OnTicketingBtnClicked(LabelPrinter printer);
        public abstract void OnTicketingInvoiceBtnClicked(LabelPrinter printer, int value, String only);
        public abstract void OnRefundTicketingBtnClicked();
    }
}
