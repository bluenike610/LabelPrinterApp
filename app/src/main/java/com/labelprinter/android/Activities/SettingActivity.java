package com.labelprinter.android.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.citizen.sdk.labelprint.LabelPrinter;
import com.labelprinter.android.Common.Common;
import com.labelprinter.android.Common.Config;
import com.labelprinter.android.Common.DownTimer;
import com.labelprinter.android.Common.LocalStorageManager;
import com.labelprinter.android.DBManager.DbHelper;
import com.labelprinter.android.DBManager.Queries;
import com.labelprinter.android.Dialogs.DeviceSettingDialog;
import com.labelprinter.android.Dialogs.InvoiceDialog;
import com.labelprinter.android.Dialogs.StartModeDialog;
import com.labelprinter.android.Dialogs.TicketTypeSettingDialog;
import com.labelprinter.android.R;

import static com.labelprinter.android.Common.Common.cm;
import static com.labelprinter.android.Common.Common.currentActivity;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView serverIPTxt, lastSyncTxt;
    private RelativeLayout loadingLayout;
    private boolean isLoading = false;

    @Override
    public void onResume() {
        currentActivity = this;
        super.onResume();
        TextView userInfo = findViewById(R.id.userInfo);
        userInfo.setText(cm.getUserInfo());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        currentActivity = this;

        initUI();
    }

    private void initUI () {
        findViewById(R.id.startModeBtn).setOnClickListener(this);
        findViewById(R.id.deviceSettingBtn).setOnClickListener(this);
        findViewById(R.id.ticketTypeBtn).setOnClickListener(this);
        findViewById(R.id.invoiceBtn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);

        serverIPTxt = findViewById(R.id.serverIPTxt);
        serverIPTxt.setText(Config.SERVER_IP_ADDRESS);

        lastSyncTxt = findViewById(R.id.lastSyncTxt);
        LocalStorageManager localStorageManager = new LocalStorageManager();
        String syncDate = localStorageManager.getLastSyncDate();
        if (syncDate != null) {
            lastSyncTxt.setText(syncDate);
        }

        loadingLayout = findViewById(R.id.loadingLayout);
        loadingLayout.setVisibility(View.INVISIBLE);
    }

    private void checkingPrintState(final LabelPrinter printer, final int value, final String only) {
        isLoading = true;
        loadingLayout.setVisibility(View.VISIBLE);
        final DownTimer myTimer = new DownTimer(1, 500);
        myTimer.setOnFinishListener(new DownTimer.OnFinishListener() {

            @Override
            public void onFinish() {
                if (printer != null) {
                    if (printer.getPrinting() == 1) { // printing now...
                        if (cm.hasPrintingErr) {
                            loadingLayout.setVisibility(View.INVISIBLE);
                            myTimer.initialize();
                            isLoading = false;
                            Common.cm.showAlertDlg(currentActivity.getResources().getString(R.string.printing_err_title),
                                    currentActivity.getResources().getString(R.string.printing_err_msg), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }, null);
                            return;
                        }
                    }else { // completed print
                        cm.hasPrintingErr = false;
                        loadingLayout.setVisibility(View.INVISIBLE);
                        DbHelper dbHelper = new DbHelper(currentActivity);
                        Queries query = new Queries(null, dbHelper);
                        query.addInvoiceInfoWithData(value, only, 1);
                        myTimer.initialize();
                        isLoading = false;
                    }
                }else {
                    cm.hasPrintingErr = false;
                    loadingLayout.setVisibility(View.INVISIBLE);
                    myTimer.initialize();
                    isLoading = false;
                }
            }

            @Override
            public void onTick(int progressValue) {

            }
        });
        myTimer.start();
    }

    @Override
    public void onClick(View v) {
        if (isLoading) return;
        switch (v.getId()) {
            case R.id.startModeBtn:
                StartModeDialog startModeDialog = new StartModeDialog(currentActivity);
                startModeDialog.show();
                break;
            case R.id.deviceSettingBtn:
                DeviceSettingDialog deviceSettingDialog = new DeviceSettingDialog(currentActivity);
                deviceSettingDialog.show();
                break;
            case R.id.ticketTypeBtn:
                TicketTypeSettingDialog ticketTypeSettingDialog = new TicketTypeSettingDialog(currentActivity);
                ticketTypeSettingDialog.show();
                break;
            case R.id.invoiceBtn:
                InvoiceDialog invoiceDialog = new InvoiceDialog(currentActivity, new InvoiceDialog.InvoicePrinterListner() {
                    @Override
                    public void OnInvoiceBtnClicked(LabelPrinter printer, int value, String only) {
                        //test
                        DbHelper dbHelper = new DbHelper(currentActivity);
                        Queries query = new Queries(null, dbHelper);
                        query.addInvoiceInfoWithData(value, only, 1);

//                        checkingPrintState(printer, value, only);
                    }
                });
                invoiceDialog.show();
                break;
            case R.id.backBtn:
                finish();
                break;
        }
    }
}
