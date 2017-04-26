package com.zenbarrier.wearfull;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textMain = (TextView) findViewById(R.id.textView_main);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = packageInfo.versionName;
            textMain.setText(getString(R.string.hello_wear, version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            textMain.setText(getString(R.string.hello_wear, "2.x"));
        }

    }
}
