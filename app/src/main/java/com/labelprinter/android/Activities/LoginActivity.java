package com.labelprinter.android.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.labelprinter.android.Common.Common;
import com.labelprinter.android.Common.LocalStorageManager;
import com.labelprinter.android.DBManager.APIManager;
import com.labelprinter.android.DBManager.DbHelper;
import com.labelprinter.android.DBManager.Queries;
import com.labelprinter.android.Models.User;
import com.labelprinter.android.R;

import java.util.ArrayList;

import static com.labelprinter.android.Common.Common.StartPattern;
import static com.labelprinter.android.Common.Common.cm;
import static com.labelprinter.android.Common.Common.currentActivity;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    private EditText userid, password;
    private boolean standalon = false;

    /** すべての許可のリクエストID */
    private final static int ALL_PERMISSIONS_RESULT = 101;

    @Override
    public void onResume() {
        currentActivity = this;
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        currentActivity = this;

        Fabric.with(this, new Crashlytics());

        ArrayList<String> permissions = new ArrayList();
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        permissions.add(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE);

        ArrayList requirePermissions = cm.checkPermissions(permissions);
        if (!requirePermissions.isEmpty()) {
            requestPermissions((String[]) requirePermissions.toArray(new String[requirePermissions.size()]),
                    ALL_PERMISSIONS_RESULT);
        }else {
            cm.getConfigInfoFromXml();
        }



        Common.cm.getStartPattern();
        if (StartPattern == 7) {
            cm.showAlertDlg(getResources().getString(R.string.login_err_title),
                    getResources().getString(R.string.login_err_msg2),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, null);
        }else if (StartPattern == 8) {
            cm.showAlertDlg(getResources().getString(R.string.login_err_title),
                    getResources().getString(R.string.login_err_msg3),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, null);
        }

        userid = findViewById(R.id.userId);
        password = findViewById(R.id.password);

        final Button login = findViewById(R.id.buttonSign);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userid.getText().toString().equals("") || password.getText().toString().equals("")) {
                    Common.cm.showAlertDlg(getResources().getString(R.string.input_err_title),
                            getResources().getString(R.string.input_err_msg), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }, null);
                    return;
                }else {
                    login(userid.getText().toString(), password.getText().toString());
                }

            }
        });

        LocalStorageManager localStorageManager = new LocalStorageManager();
        String userId = localStorageManager.getLoginStatus();
        if (userId != null) {
            userid.setText(userId);
        }else {
            userid.setText("");
        }
        password.setText("");
        String val = localStorageManager.getStartMode();
        if (val != null) {
            if (val.equals("online")) {
                checkServerState();
            }else {
                standalon = true;
                changeStartPattern();
            }
        }else {
            checkServerState();
        }
    }

    private void checkServerState() {
        APIManager apiManager = new APIManager();
        if (apiManager.connectionclass() != null) {//リモートデータベース接続
            standalon = false;
            changeStartPattern();
        }else {//リモートデータベース接続不可
            cm.showAlertDlg(getResources().getString(R.string.login),
                    getResources().getString(R.string.server_connect_error_msg),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            standalon = true;
                            changeStartPattern();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
        }
    }

    private void changeStartPattern() {
        if (standalon) {
            if (StartPattern == 1 ||
                    StartPattern == 3 ||
                    StartPattern == 5) {
                StartPattern ++;
            }
        }else {
            if (StartPattern == 2 ||
                    StartPattern == 4 ||
                    StartPattern == 6) {
                StartPattern --;
            }
        }
    }

    private void login(String id, String pass) {
        APIManager apiManager = new APIManager();
        switch (StartPattern) {
            case 1:
                loginFromLocal(id, pass);
                cm.getTicketInfoFromXml();
                apiManager.syncFromServer();
                break;
            case 2:
                loginFromLocal(id, pass);
                cm.getTicketInfoFromXml();
                break;
            case 3:
                apiManager.syncFromServer();
                loginFromServer(id, pass);
                cm.getTicketInfoFromXml();
                break;
            case 4:
                cm.showAlertDlg(getResources().getString(R.string.login_err_title),
                        getResources().getString(R.string.login_err_msg1),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }, null);
                //test
//                loginFromLocal(id, pass);
//                cm.getTicketInfoFromXml();
                break;
            case 5:
                loginFromLocal(id, pass);
                apiManager.syncFromServer();
                cm.getTicketInfoFromLocal();
                break;
            case 6:
                loginFromLocal(id, pass);
                cm.getTicketInfoFromLocal();
                break;
            default:
                break;
        }
    }

    private void loginFromLocal(String id, String pass) {
        DbHelper dbHelper = new DbHelper(currentActivity);
        Queries query = new Queries(null, dbHelper);
        LocalStorageManager manager = new LocalStorageManager();
        User user = query.getUserInfo(id, cm.getHashCodeFromPass(pass));
        // test
//        User user = new User();
//        user.setId("1");
//        user.setName("テスター");
//        user.setPassword("111111");
        if (user != null) {
            manager.saveLoginInfo(user.getId());
            cm.me = user;
            Intent intent = new Intent(currentActivity, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            cm.showAlertDlg(getResources().getString(R.string.login_err_title),
                    getResources().getString(R.string.login_err_msg),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, null);
        }
    }

    private void loginFromServer(String id, String pass) {
        DbHelper dbHelper = new DbHelper(currentActivity);
        Queries query = new Queries(null, dbHelper);
        LocalStorageManager manager = new LocalStorageManager();
        APIManager apiManager = new APIManager();
        User user = apiManager.loginToServer(id, cm.getHashCodeFromPass(pass));
        // test
//        User user = new User();
//        user.setId("1");
//        user.setName("テスター");
//        user.setPassword("111111");
//        cm.me = user;
        if (user != null) {
            query.addUserInfo(user);
            manager.saveLoginInfo(user.getId());
            cm.me = user;
            Intent intent = new Intent(cm.currentActivity, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            cm.showAlertDlg(getResources().getString(R.string.login_err_title),
                    getResources().getString(R.string.login_err_msg),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                cm.getConfigInfoFromXml();
        }
    }
}
