package com.labelprinter.android.Application;

import androidx.multidex.MultiDexApplication;

import com.labelprinter.android.Common.Common;
import com.labelprinter.android.Common.Config;
import com.labelprinter.android.Utils.FontsOverride;

/**
 *
 * Application of App
 * */
public class MyApplication extends MultiDexApplication {

    public void onCreate(){
        super.onCreate();

        Common.myApp = this;

//        FontsOverride.setDefaultFont(this, "SERIF", "fonts/HelveticaNeueMed.ttf");
    }

}
