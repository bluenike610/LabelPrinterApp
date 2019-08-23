package com.labelprinter.android.Dialogs;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.labelprinter.android.Common.Common;
import com.labelprinter.android.DBManager.DbHelper;
import com.labelprinter.android.DBManager.Queries;
import com.labelprinter.android.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.labelprinter.android.Common.Common.cm;
import static com.labelprinter.android.Common.Common.currentActivity;

public class DeviceSettingDialog extends Dialog {

    private EditText deviceName, deviceNum;
    private String devicePlaceName;
    private HashMap initInfo;

    public DeviceSettingDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.device_setting_dialog);

        deviceName = findViewById(R.id.deviceName);
        deviceNum = findViewById(R.id.deviceNum);

        DbHelper dbHelper = new DbHelper(currentActivity);
        Queries query = new Queries(null, dbHelper);
        initInfo = query.getDeviceInfo();

        if (initInfo == null) {
            deviceName.setText(currentActivity.getResources().getString(R.string.app_name));
            deviceNum.setText("0");
        }else {
            deviceName.setText(initInfo.get("tanmatsumei").toString());
            deviceNum.setText(initInfo.get("tanmatsuno").toString());
        }

        final String[] items = currentActivity.getResources().getStringArray(R.array.device_place);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                currentActivity, android.R.layout.simple_spinner_item, items){
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTextColor(Color.BLACK);
                v.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                v.setTextSize(20);
                return v;
            }

            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTextColor(Color.BLACK);
                v.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                v.setTextSize(20);
                return v;
            }
        };

        devicePlaceName = "";
        int selectedIndex = 0;
        if (initInfo != null) {
            selectedIndex = dataAdapter.getPosition(String.valueOf(initInfo.get("hanbaibasho")));
        }
        dataAdapter.setDropDownViewResource(R.layout.spinner_text);
        Spinner devicePlace = findViewById(R.id.devicePlace);
        devicePlace.setAdapter(dataAdapter);
        devicePlace.setSelection(selectedIndex);
        devicePlace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                devicePlaceName = items[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        findViewById(R.id.registryBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceName.getText().toString().equals("") || deviceName.getText().toString().equals("")) {
                    Common.cm.showAlertDlg(currentActivity.getResources().getString(R.string.input_err_title),
                            currentActivity.getResources().getString(R.string.input_err_msg), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }, null);
                    return;
                }else {
                    ContentValues values = new ContentValues();
                    values.put("tanmatsumei", deviceName.getText().toString());
                    values.put("tanmatsuno", cm.parseInteger(deviceNum.getText().toString()));
                    values.put("hanbaibasho", devicePlaceName);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    if (initInfo != null) {
                        values.put("sakuseiuserid", String.valueOf(initInfo.get("sakuseiuserid")));
                        values.put("sakuseinichiji", Double.valueOf((Double) initInfo.get("sakuseinichiji")));
                    }else {
                        values.put("sakuseiuserid", cm.me.getId());
                        values.put("sakuseinichiji", calendar.getTimeInMillis());
                    }
                    values.put("koshinuserid", cm.me.getId());
                    values.put("koshinnichiji", calendar.getTimeInMillis());

                    DbHelper dbHelper = new DbHelper(currentActivity);
                    Queries query = new Queries(null, dbHelper);
                    query.addDeviceInfo(values);
                    dismiss();
                }
            }
        });

        TextView userInfo = findViewById(R.id.userInfo);
        userInfo.setText(cm.getUserInfo());
    }
}
