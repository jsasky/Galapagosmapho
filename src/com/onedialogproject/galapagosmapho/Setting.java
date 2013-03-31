package com.onedialogproject.galapagosmapho;

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
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;

public class Setting extends Activity {

    private static final boolean DEBUG = false;
    private static final int SHOW_LOG = 1;
    private static final int SHOW_FUNCTION = 2;
    private static final int CONFIRM_CLEAR_LOG = 3;

    private int nNowDialog = SHOW_FUNCTION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;

        DebugTools.notify(this, Pattern.SCREEN_ON);
        Log.append(context, "設定アプリ起動");

        if (Prefs.isActivated(context)) {
            Utils.set(this, true);

            if (!ResidentService.isServiceRunning(context)) {
                startService(new Intent(this, ResidentService.class));
            }
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
        final Context context = this;
        if (Prefs.isActivated(context)) {
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
        case CONFIRM_CLEAR_LOG:
            dialog = showCnfirmClearLog();
            break;
        default:
            break;
        }
        return dialog;
    }

    private Dialog showSelectFunctionDialog() {
        final Context context = this;
        View mainView = LayoutInflater.from(context).inflate(R.layout.main,
                null);
        final CheckBox lte3GCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkBox_lte_3g);
        final CheckBox wifiCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkBox_wifi);
        final CheckBox bluetoothCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkBox_bluetooth);
        final CheckBox mailNotificationCheckBox = (CheckBox) mainView
                .findViewById(R.id.checkbox_mail_notification);
        final CheckBox debugModeCheckbox = (CheckBox) mainView
                .findViewById(R.id.checkbox_debug_mode);

        lte3GCheckBox.setChecked(Prefs.getLte3gSetting(context));
        lte3GCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                Log.append(context, "3G/LTEを" + (isChecked ? "ON" : "OFF")
                        + "に設定");
                Prefs.setLte3gSetting(context, isChecked);

                if (Prefs.isActivated(context)) {
                    mailNotificationCheckBox.setEnabled(true);
                    debugModeCheckbox.setEnabled(true);
                    if (!ResidentService.isServiceRunning(context)) {
                        startService(new Intent(context, ResidentService.class));
                    }
                } else {
                    mailNotificationCheckBox.setEnabled(false);
                    debugModeCheckbox.setEnabled(false);
                    stopService(new Intent(context, ResidentService.class));
                }
            }
        });

        wifiCheckBox.setChecked(Prefs.getWifiSetting(context));
        wifiCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                Log.append(context, "Wi-Fiを" + (isChecked ? "ON" : "OFF")
                        + "に設定");
                Prefs.setWifiSetting(context, isChecked);

                if (Prefs.isActivated(context)) {
                    mailNotificationCheckBox.setEnabled(true);
                    debugModeCheckbox.setEnabled(true);
                    if (!ResidentService.isServiceRunning(context)) {
                        startService(new Intent(context, ResidentService.class));
                    }
                } else {
                    mailNotificationCheckBox.setEnabled(false);
                    debugModeCheckbox.setEnabled(false);
                    stopService(new Intent(context, ResidentService.class));
                }
            }
        });

        bluetoothCheckBox.setChecked(Prefs.getBluetoothSetting(context));
        bluetoothCheckBox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        Log.append(context, "BTを" + (isChecked ? "ON" : "OFF")
                                + "に設定");
                        Prefs.setBluetoothSetting(context, isChecked);

                        if (Prefs.isActivated(context)) {
                            mailNotificationCheckBox.setEnabled(true);
                            debugModeCheckbox.setEnabled(true);
                            if (!ResidentService.isServiceRunning(context)) {
                                startService(new Intent(context,
                                        ResidentService.class));
                            }
                        } else {
                            mailNotificationCheckBox.setEnabled(false);
                            debugModeCheckbox.setEnabled(false);
                            stopService(new Intent(context,
                                    ResidentService.class));
                        }
                    }
                });

        mailNotificationCheckBox.setChecked(NotifyAreaController
                .isActivated(context));
        mailNotificationCheckBox.setEnabled(Prefs.isActivated(context));
        mailNotificationCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        context.startActivity(new Intent(
                                android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        removeDialog(SHOW_FUNCTION);
                        Log.append(context, "着信通知画面を表示");
                    }
                });

        if (!DEBUG) {
            final View divider = (View) mainView
                    .findViewById(R.id.divider_debug_mode);
            divider.setVisibility(View.GONE);
            final TextView debugModeExplanation = (TextView) mainView
                    .findViewById(R.id.text_debug_mode);
            debugModeExplanation.setVisibility(View.GONE);
            debugModeCheckbox.setVisibility(View.GONE);
            Prefs.setDebugMode(context, false);
        }

        debugModeCheckbox.setChecked(Prefs.getDebugMode(context));
        debugModeCheckbox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        Prefs.setDebugMode(context, isChecked);
                        Log.append(context, "動作確認モードを"
                                + (isChecked ? "ON" : "OFF") + "にしました");
                    }
                });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setView(mainView);
        builder.setPositiveButton(R.string.button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.append(context, "設定アプリ終了");
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
                Log.append(context, "設定アプリ終了");
                finish();
            }
        });
        return builder.create();
    }

    private Dialog showLogDialog() {
        final Context context = this;

        View view = LayoutInflater.from(this).inflate(R.layout.log, null);
        final TextView textView = (TextView) view.findViewById(R.id.log_view);

        textView.setText(Log.read(context));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(view);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton(R.string.button_update_log,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(SHOW_LOG);
                        showDialog(SHOW_LOG);
                    }
                });
        builder.setNeutralButton(R.string.button_clear_log,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(SHOW_LOG);
                        showDialog(CONFIRM_CLEAR_LOG);
                    }
                });
        builder.setNegativeButton(R.string.button_function,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(SHOW_LOG);
                        showDialog(SHOW_FUNCTION);
                    }
                });
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Log.append(context, "設定アプリ終了");
                finish();
            }
        });
        return builder.create();
    }

    private Dialog showCnfirmClearLog() {
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(R.string.message_clear_log);
        builder.setPositiveButton(R.string.button_yes,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.clear(context);
                        Log.append(context, "ログをクリアしました");
                        removeDialog(CONFIRM_CLEAR_LOG);
                        showDialog(SHOW_LOG);
                    }
                });
        builder.setNeutralButton(R.string.button_no,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(CONFIRM_CLEAR_LOG);
                        showDialog(SHOW_LOG);
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Log.append(context, "設定アプリ終了");
                finish();
            }
        });
        return builder.create();
    }
}
