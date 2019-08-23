package com.labelprinter.android.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.citizen.sdk.labelprint.LabelPrinter;
import com.labelprinter.android.Common.Common;
import com.labelprinter.android.Common.DownTimer;
import com.labelprinter.android.Common.LocalStorageManager;
import com.labelprinter.android.DBManager.DbHelper;
import com.labelprinter.android.DBManager.Queries;
import com.labelprinter.android.Dialogs.ReportingDialog;
import com.labelprinter.android.Dialogs.TicketNumberInputDlg;
import com.labelprinter.android.Dialogs.TicketingDlg;
import com.labelprinter.android.Models.TicketInfo;
import com.labelprinter.android.Models.TicketModel;
import com.labelprinter.android.Models.TicketType;
import com.labelprinter.android.PrinterManager.PrinterManager;
import com.labelprinter.android.R;
import com.labelprinter.android.Views.TicketListItemView;

import java.util.ArrayList;

import static com.labelprinter.android.Common.Common.cm;
import static com.labelprinter.android.Common.Common.currentActivity;

/**
 *
 * 発券画面
 *
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TicketNumberInputDlg.TicketNumClickListener, TicketListItemView.TicketItemClickListener {

    private LinearLayout ticketListView;
    private ArrayList<ArrayList> ticketViewArr;
    private ArrayList<LinearLayout> tabLayoutArr;
    private ArrayList<LinearLayout> tabMaskArr;
    private ArrayList<TextView> tabViewArr;
    private TextView sumPriceTxt;
    private ScrollView ticketScrollView;
    private RelativeLayout loadingLayout;

    private ArrayList<TicketInfo> ticketingList;
    private ArrayList<TicketType> tabList;

    private int selectedIndex;
    private int selectedTabIndex = 0;
    private int selectedPayType;
    private boolean isLoading = false;

    @Override
    public void onResume() {
        currentActivity = this;
        super.onResume();

        TextView userInfo = findViewById(R.id.userInfo);
        userInfo.setText(cm.getUserInfo());

        if (cm.ticketTypes.size() > 0) {
            LocalStorageManager localStorageManager = new LocalStorageManager();
            String result = localStorageManager.getHideTicketType();
            ArrayList<String> hideTypes = new ArrayList<>();
            tabList = new ArrayList<>();
            if (result != null) {
                if (!result.equals("")) {
                    hideTypes = cm.convertToArrayListFromString(result);
                    for (TicketType type : cm.ticketTypes) {
                        boolean isHidden = false;
                        for (String name : hideTypes) {
                            if (type.getName().equals(name)) {
                                isHidden = true;
                            }
                        }
                        if (!isHidden)
                            tabList.add(type);
                    }
                }else {
                    tabList = (ArrayList<TicketType>) cm.ticketTypes.clone();
                }
            }else {
                tabList = (ArrayList<TicketType>) cm.ticketTypes.clone();
            }
            for (int i=0; i<tabViewArr.size(); i++) {
                if (i > tabList.size()-1) {
                    tabLayoutArr.get(i).setVisibility(View.INVISIBLE);
                }else {
                    tabLayoutArr.get(i).setVisibility(View.VISIBLE);
                    TicketType type = tabList.get(i);
                    tabViewArr.get(i).setText(type.getName());
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentActivity = this;

        DbHelper dbHelper = new DbHelper(this);
        Queries query = new Queries(null, dbHelper);
        query.setTestData();
        LocalStorageManager manager = new LocalStorageManager();
//        if (manager.getLoginStatus() == null) {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//        }else {
//        }

        initUI();
    }

    /**
     *
     * 画面UIコンポーネントの初期化関数
     *
     */
    private void initUI() {

        ticketViewArr = new ArrayList<>();
        tabLayoutArr = new ArrayList<>();
        tabMaskArr = new ArrayList<>();
        tabViewArr = new ArrayList<>();

        ticketingList = new ArrayList<>();

        ArrayList<TextView> items1 = new ArrayList<>();
        items1.add((TextView) findViewById(R.id.ticket11));
        items1.add((TextView) findViewById(R.id.ticket12));
        items1.add((TextView) findViewById(R.id.ticket13));
        items1.add((TextView) findViewById(R.id.ticket14));
        items1.add((TextView) findViewById(R.id.ticket15));
        ticketViewArr.add(items1);
        ArrayList<TextView> items2 = new ArrayList<>();
        items2.add((TextView) findViewById(R.id.ticket21));
        items2.add((TextView) findViewById(R.id.ticket22));
        items2.add((TextView) findViewById(R.id.ticket23));
        items2.add((TextView) findViewById(R.id.ticket24));
        items2.add((TextView) findViewById(R.id.ticket25));
        ticketViewArr.add(items2);
        ArrayList<TextView> items3 = new ArrayList<>();
        items3.add((TextView) findViewById(R.id.ticket31));
        items3.add((TextView) findViewById(R.id.ticket32));
        items3.add((TextView) findViewById(R.id.ticket33));
        items3.add((TextView) findViewById(R.id.ticket34));
        items3.add((TextView) findViewById(R.id.ticket35));
        ticketViewArr.add(items3);
        ArrayList<TextView> items4 = new ArrayList<>();
        items4.add((TextView) findViewById(R.id.ticket41));
        items4.add((TextView) findViewById(R.id.ticket42));
        items4.add((TextView) findViewById(R.id.ticket43));
        items4.add((TextView) findViewById(R.id.ticket44));
        items4.add((TextView) findViewById(R.id.ticket45));
        ticketViewArr.add(items4);

        for (int k=0; k<ticketViewArr.size(); k++) {
            ArrayList list = ticketViewArr.get(k);
            for (int i=0; i<list.size(); i++) {
                TextView view = (TextView) list.get(i);
                view.setTag(String.valueOf(k) + "," + String.valueOf(i));
                final TicketModel model = cm.getTicketModelFormPos(k, i);
                if (model !=null) {
                    GradientDrawable bgShape = (GradientDrawable) view.getBackground();
                    bgShape.mutate();
                    bgShape.setColor(Color.parseColor(model.getBgColor()));
                    view.setTextColor(Color.parseColor(model.getFgColor()));
                    if (model.getPrice() > 0)
                        view.setText(model.getName() + "\n" + "\n" + getResources().getString(R.string.lb_yen2) + model.getPrice());
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (!isLoading) {
                                if (model.getPrice() != 0) {
                                    final TicketInfo info = new TicketInfo();
                                    info.setModel(model);
                                    info.setNum(0);
                                    info.setType(tabList.get(selectedTabIndex));
                                    final TicketNumberInputDlg dlg = new TicketNumberInputDlg(currentActivity, info, new TicketNumberInputDlg.TicketNumClickListener() {
                                        @Override
                                        public void OnDeleteBtnClicked() {
                                        }

                                        @Override
                                        public void OnConfirmBtnClicked(int num) {
                                            info.setNum(num);
                                            ticketingList.add(info);
                                            setTicketList();
                                        }
                                    });
                                    dlg.show();
                                }
                            }
                            return false;
                        }
                    });
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (model.getPrice() != 0) {
                                if (ticketingList.size() > 0) {
                                    TicketInfo oldInfo = ticketingList.get(ticketingList.size()-1);
                                    if (oldInfo.getModel().getName().equals(model.getName())) {
                                        int num = oldInfo.getNum();
                                        oldInfo.setNum(num+1);
                                        ticketingList.set(ticketingList.size()-1, oldInfo);
                                    }else {
                                        TicketInfo info = new TicketInfo();
                                        info.setModel(model);
                                        info.setType(tabList.get(selectedTabIndex));
                                        info.setNum(1);
                                        ticketingList.add(info);
                                    }
                                }else {
                                    TicketInfo info = new TicketInfo();
                                    info.setModel(model);
                                    info.setType(tabList.get(selectedTabIndex));
                                    info.setNum(1);
                                    ticketingList.add(info);
                                }
                                setTicketList();
                            }
                        }
                    });
                }
            }
        }

        tabLayoutArr.add((LinearLayout) findViewById(R.id.ticketTypeLayout1));
        tabLayoutArr.add((LinearLayout) findViewById(R.id.ticketTypeLayout2));
        tabLayoutArr.add((LinearLayout) findViewById(R.id.ticketTypeLayout3));
        tabLayoutArr.add((LinearLayout) findViewById(R.id.ticketTypeLayout4));
        for (LinearLayout layout : tabLayoutArr) {
            layout.setOnClickListener(this);
        }

        tabMaskArr.add((LinearLayout) findViewById(R.id.overLayer1));
        tabMaskArr.add((LinearLayout) findViewById(R.id.overLayer2));
        tabMaskArr.add((LinearLayout) findViewById(R.id.overLayer3));
        tabMaskArr.add((LinearLayout) findViewById(R.id.overLayer4));

        tabViewArr.add((TextView) findViewById(R.id.ticketType1));
        tabViewArr.add((TextView) findViewById(R.id.ticketType2));
        tabViewArr.add((TextView) findViewById(R.id.ticketType3));
        tabViewArr.add((TextView) findViewById(R.id.ticketType4));

        Button systemBtn = findViewById(R.id.btnSystem);
        systemBtn.setOnClickListener(this);

        Button reportBtn = findViewById(R.id.btnReport);
        reportBtn.setOnClickListener(this);

        Button clearBtn = findViewById(R.id.btnClear);
        clearBtn.setOnClickListener(this);

        Button refundBtn = findViewById(R.id.btnRefund);
        refundBtn.setOnClickListener(this);

        Button reticketingBtn = findViewById(R.id.btnReticketing);
        reticketingBtn.setOnClickListener(this);

        Button consignBtn = findViewById(R.id.btnConsign);
        consignBtn.setOnClickListener(this);

        Button receivableBtn = findViewById(R.id.btnReceivable);
        receivableBtn.setOnClickListener(this);

        Button cashBtn = findViewById(R.id.btnCash);
        cashBtn.setOnClickListener(this);

        ticketListView = findViewById(R.id.ticketListLayout);
        sumPriceTxt = findViewById(R.id.sumPriceTxt);

        ticketScrollView = findViewById(R.id.ticketScroll);

        loadingLayout = findViewById(R.id.loadingLayout);
        loadingLayout.setVisibility(View.INVISIBLE);

        setTicketList();
    }

    private void setTicketList() {
        ticketListView.removeAllViews();
        int price = 0;
        for (int i=0; i<ticketingList.size(); i++) {
            TicketInfo info = ticketingList.get(i);
            TicketListItemView itemView = new TicketListItemView(currentActivity, i, info, this);
            ticketListView.addView(itemView);
            TicketModel model = info.getModel();
            price += model.getPrice() * info.getNum();
        }

        ticketScrollView.post(new Runnable() {
            @Override
            public void run() {
                ticketScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        if (price > 0)
            sumPriceTxt.setText(cm.numberFormat(price));
        else
            sumPriceTxt.setText("");
    }

    private void tabChanged(int ind) {
        selectedTabIndex = ind;
        for (LinearLayout layout : tabLayoutArr) {
            layout.setBackground(getResources().getDrawable(R.drawable.border_main));
        }
        tabLayoutArr.get(ind).setBackground(getResources().getDrawable(R.drawable.border_white));
        for (LinearLayout layout : tabMaskArr) {
            layout.setVisibility(View.INVISIBLE);
        }
        tabMaskArr.get(ind).setVisibility(View.VISIBLE);
        ArrayList<TicketInfo> tempList = (ArrayList<TicketInfo>) ticketingList.clone();
        for (int i=0; i<ticketingList.size(); i++) {
            TicketInfo info = tempList.get(i);
            info.setType(tabList.get(ind));
            ticketingList.set(i, info);
        }
    }

    private void showTicketingDlg(int ind) {
        if (ticketingList.size() > 0) {
            TicketingDlg dlg = new TicketingDlg(currentActivity, ind, ticketingList, new TicketingDlg.TicketingLinstener() {
                @Override
                public void OnTicketingBtnClicked(LabelPrinter printer) { //to print
                    //test
                    DbHelper dbHelper = new DbHelper(currentActivity);
                    Queries query = new Queries(null, dbHelper);
                    query.addSellInfoWithData(ticketingList, selectedPayType);
                    ticketingList.clear();
                    setTicketList();

//                    checkingPintState(printer, 0, 0, "");
                }

                @Override
                public void OnTicketingInvoiceBtnClicked(LabelPrinter printer, int value, String only) { //to print
                    //test
                    DbHelper dbHelper = new DbHelper(currentActivity);
                    Queries query = new Queries(null, dbHelper);
                    query.addSellInfoWithData(ticketingList, selectedPayType);
                    query.addInvoiceInfoWithData(value, only, selectedPayType);

//                    checkingPintState(printer, 1, value, only);
                }

                @Override
                public void OnRefundTicketingBtnClicked() {
                    DbHelper dbHelper = new DbHelper(currentActivity);
                    Queries query = new Queries(null, dbHelper);
                    query.addSellInfoWithData(ticketingList, selectedPayType);
                    ticketingList.clear();
                    setTicketList();
                }
            });
            dlg.show();
        }else {
            // error alert
        }
    }

    private void checkingPintState(final LabelPrinter printer, final int invoiceOption, final int invoiceValue, final String invoiceOnlyStr) {
        isLoading = true;
        loadingLayout.setVisibility(View.VISIBLE);
        final DownTimer myTimer = new DownTimer(1, 500);
        myTimer.setOnFinishListener(new DownTimer.OnFinishListener() {

            @Override
            public void onFinish() {
                if (printer != null){
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

                        if (invoiceOption != 2) {
                            DbHelper dbHelper = new DbHelper(currentActivity);
                            Queries query = new Queries(null, dbHelper);
                            query.addSellInfoWithData(ticketingList, selectedPayType);
                            if (invoiceOption == 1)
                                query.addInvoiceInfoWithData(invoiceValue, invoiceOnlyStr, selectedPayType);
                        }

                        ticketingList.clear();
                        setTicketList();
                        cm.hasPrintingErr = false;
                        loadingLayout.setVisibility(View.INVISIBLE);
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
            case R.id.ticketTypeLayout1:
                tabChanged(0);
                break;
            case R.id.ticketTypeLayout2:
                tabChanged(1);
                break;
            case R.id.ticketTypeLayout3:
                tabChanged(2);
                break;
            case R.id.ticketTypeLayout4:
                tabChanged(3);
                break;
            case R.id.btnSystem:
                Intent intent = new Intent(currentActivity, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.btnReport:
                ReportingDialog dlg = new ReportingDialog(this);
                dlg.show();
                break;
            case R.id.btnClear:
                ticketingList.clear();
                setTicketList();
                break;
            case R.id.btnRefund:
                showTicketingDlg(0);
                selectedPayType = 5;
                break;
            case R.id.btnReticketing:
                if (ticketingList.size() > 0) {
                    PrinterManager manager = new PrinterManager();
                    LabelPrinter printer = manager.printerStart(ticketingList, 0, "");
                    checkingPintState(printer, 2, 0, "");
                }
                break;
            case R.id.btnConsign:
                showTicketingDlg(2);
                selectedPayType = 4;
                break;
            case R.id.btnReceivable:
                showTicketingDlg(3);
                selectedPayType = 2;
                break;
            case R.id.btnCash:
                showTicketingDlg(4);
                selectedPayType = 1;
                break;
        }
    }

    @Override
    public void OnDeleteBtnClicked() {
        ticketingList.remove(selectedIndex);
        setTicketList();
    }

    @Override
    public void OnConfirmBtnClicked(int num) {
        TicketInfo info = ticketingList.get(selectedIndex);
        info.setNum(num);
        ticketingList.set(selectedIndex, info);
        setTicketList();
    }

    @Override
    public void OnTicketItemClicked(int ind) {
        selectedIndex = ind;
        TicketNumberInputDlg dlg = new TicketNumberInputDlg(currentActivity, ticketingList.get(ind), this);
        dlg.show();
    }
}
