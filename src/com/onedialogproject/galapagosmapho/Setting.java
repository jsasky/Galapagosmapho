package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.R;
import com.onedialogproject.galapagosmapho.DebugTools.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class Setting extends Activity {

    private static final int SHOW_LOG = 1;
    private static final int SHOW_FUNCTION = 2;

    private int nNowDialog = SHOW_FUNCTION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;
        DebugTools.notify(this, Pattern.SCREEN_ON);
        Utils.addLog(context, "設定アプリ起動");
        if (Prefs.getMainSetting(this)
                && !ResidentService.isServiceRunning(context)) {
            startService(new Intent(this, ResidentService.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showDialog(nNowDialog);
    }

    @Override
    public void onPause() {
        super.onPause();
        removeDialog(nNowDialog);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Prefs.getMainSetting(this)) {
            Utils.set(this, true);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        nNowDialog = id;
        switch (id) {
        case SHOW_LOG:
            dialog = showLogDialog();
            break;
        case SHOW_FUNCTION:
            dialog = showSelectFunctionDialog();
            break;
        default:
            break;
        }
        return dialog;
    }

    private Dialog showSelectFunctionDialog() {
        final Context context = this;
        View mainView = LayoutInflater.from(this).inflate(R.layout.main, null);

        final CheckBox mainSettingCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkbox_main_setting);
        final CheckBox wifiOnOffCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkbox_wifi_on_off);
        final CheckBox delayOffCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkbox_delay_off);
        final CheckBox mailNotificationCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkbox_mail_notification);

        mainSettingCheckBox.setChecked(Prefs.getMainSetting(context));
        mainSettingCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        Utils.addLog(context, "メイン設定を"
                                + (isChecked ? "ON" : "OFF") + "に変更しました");
                        Prefs.setMainSetting(context, isChecked);
                        wifiOnOffCheckBox.setEnabled(isChecked);
                        mailNotificationCheckBox.setEnabled(isChecked);
                        delayOffCheckBox.setEnabled(isChecked);
                        if (isChecked) {
                            startService(new Intent(context,
                                    ResidentService.class));

                        } else {
                            stopService(new Intent(context,
                                    ResidentService.class));
                        }
                    }
                });

        wifiOnOffCheckBox.setChecked(Prefs.getWifiSetting(context));
        wifiOnOffCheckBox.setEnabled(Prefs.getMainSetting(context));
        wifiOnOffCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        Utils.addLog(context, "WiFi設定を"
                                + (isChecked ? "ON" : "OFF") + "に変更しました");
                        Prefs.setWifiSetting(context, isChecked);
                    }
                });

        delayOffCheckBox.setChecked(Prefs.getDelayOff(context));
        delayOffCheckBox.setEnabled(Prefs.getMainSetting(context));
        delayOffCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        Utils.addLog(context, "遅延切断を"
                                + (isChecked ? "ON" : "OFF") + "に変更しました");
                        Prefs.setDelayOff(context, isChecked);
                    }
                });

        mailNotificationCheckBox.setChecked(NotifyAreaController
                .isActivated(context));
        mailNotificationCheckBox.setEnabled(Prefs.getMainSetting(context));
        mailNotificationCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        context.startActivity(new Intent(
                                android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        removeDialog(SHOW_FUNCTION);
                        Utils.addLog(context, "設定画面を表示しました");
                    }
                });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_function);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setView(mainView);
        builder.setPositiveButton(R.string.button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder.setNeutralButton(R.string.button_show_log,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(SHOW_FUNCTION);
                        showDialog(SHOW_LOG);
                    }
                });
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        return builder.create();
    }

    private Dialog showLogDialog() {
        final Context context = this;

        View view = LayoutInflater.from(this).inflate(R.layout.log, null);
        final CheckBox debugModeCheckbox = (CheckBox) view
                .findViewById(R.id.checkbox_debug_mode);
        final TextView textView = (TextView) view.findViewById(R.id.log_view);

        textView.setText(Prefs.getLog(this));

        debugModeCheckbox.setChecked(Prefs.getDebugMode(context));
        debugModeCheckbox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        Prefs.setDebugMode(context, isChecked);
                        Utils.addLog(context, "デバッグ設定を"
                                + (isChecked ? "ON" : "OFF") + "に変更しました");
                    }
                });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_log);
        builder.setView(view);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton(R.string.button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.button_function,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(SHOW_LOG);
                        showDialog(SHOW_FUNCTION);
                    }
                });
        builder.setNeutralButton(R.string.button_clear_log,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Utils.clearLog(context);
                        removeDialog(SHOW_LOG);
                        showDialog(SHOW_LOG);
                    }
                });
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        return builder.create();
    }
}
