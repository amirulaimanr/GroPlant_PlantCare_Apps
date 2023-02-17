
package com.groplant.Activities;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.groplant.DataStructures.PlantList;
import com.groplant.DataStructures.Settings;
import com.groplant.JobSchedulers.NotificationJobService;
import com.groplant.NumberPickers.GreenNumberPicker;
import com.groplant.R;
import com.groplant.Utils.CommunicationKeys;

public class SettingsActivity extends AppCompatActivity {

    private Settings settings;

    private SwitchCompat notifEnablerSwitch;

    private ConstraintLayout notifTimingBox;
    private TextView notifTimingTextView;
    private TimePickerDialog notifTimingTimePickerDialog;

    private ConstraintLayout notifPostponeBox;
    private TextView notifPostponeNumberTextView;
    private TextView notifPostponeHourTextView;

    private ConstraintLayout freeTreesBox;
    private TextView freeTreesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_layout);

        settings = new Settings(this);

        //Prepare Notification Timing UI
        notifTimingBox = findViewById(R.id.settings_notif_timing_box);
        notifTimingTextView = findViewById(R.id.settings_notif_timing_hour);
        formatNotifTimingTextView();

        //Prepare Notification Postpone UI
        notifPostponeBox = findViewById(R.id.settings_notif_remind_box);
        notifPostponeNumberTextView = findViewById(R.id.settings_notif_remind_num_hours);
        notifPostponeHourTextView = findViewById(R.id.settings_notif_remind_text_hours);
        notifPostponeNumberTextView.setText(String.valueOf(settings.getNotifRepetInterval()));
        notifPostponeHourTextView.setText(getResources().getQuantityString(R.plurals.hours, settings.getNotifRepetInterval()));

        // Prepare Notification Enabler UI    (after everything because it can modify the upper ones)
        notifEnablerSwitch = findViewById(R.id.settings_notif_enabler_switch);
        notifEnablerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){ onNotifEnablerSwitchChangedOn(); }
                else { onNotifEnablerSwitchChangedOff(); }
            }
        });
        notifEnablerSwitch.setChecked(settings.getNotifEnabled());      // Triggers onCheckedChanged call bc after it

        //free trees
        freeTreesBox = findViewById(R.id.settings_free_box);
        freeTreesTextView = findViewById(R.id.settings_free_text);

        freeTreesBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goLink("https://freetreesociety.org/index.php/whats-on/#giveaways");
            }
        });
    }

    private void goLink(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW,uri));
    }

    public void onNotifEnablerSwitchChangedOn(){
        settings.setNotifEnabled(true);

        int orange = getResources().getColor(R.color.colorOrange);

        notifTimingTextView.setTextColor(orange);
        notifTimingBox.setClickable(true);

        notifPostponeBox.setClickable(true);
        notifPostponeNumberTextView.setTextColor(orange);
        notifPostponeHourTextView.setTextColor(orange);
    }

    public void onNotifEnablerSwitchChangedOff(){
        settings.setNotifEnabled(false);

        int grey = getResources().getColor(R.color.colorGrey);

        notifTimingBox.setClickable(false);
        notifTimingTextView.setTextColor(grey);

        notifPostponeBox.setClickable(false);
        notifPostponeNumberTextView.setTextColor(grey);
        notifPostponeHourTextView.setTextColor(grey);
    }

    private void formatNotifTimingTextView(){
        String unpadded = "" + settings.getNotifHour();
        String result = "00".substring(unpadded.length()) + unpadded;
        unpadded = "" + settings.getNotifMinute();
        result = result + ":" + "00".substring(unpadded.length()) + unpadded;
        notifTimingTextView.setText(result);
    }

    public void onNotifTimingBoxClick(View v){
        if (notifTimingTimePickerDialog == null) {
            notifTimingTimePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int clickedHour, int clickedMinute) {
                    settings.setNotifHour(clickedHour);
                    settings.setNotifMinute(clickedMinute);
                    //Cancel notifJob with previous timing and set the new one
                    NotificationJobService.cancelScheduledJob(SettingsActivity.this);
                    NotificationJobService.scheduleNextJob(SettingsActivity.this);

                    formatNotifTimingTextView();
                }
            }, settings.getNotifHour(), settings.getNotifMinute(), true);
        }
        notifTimingTimePickerDialog.show();
        Window window = notifTimingTimePickerDialog.getWindow();
        window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public void onNotifPostponeBoxClick(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        View view = LayoutInflater.from(this).inflate(
                R.layout.settings_postpone_dialog,
                (ConstraintLayout) findViewById(R.id.layout_dialog_container)
        );

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        final GreenNumberPicker numberPicker = view.findViewById(R.id.settings_postpone_numberpicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(23);
        numberPicker.setValue(settings.getNotifRepetInterval());
        numberPicker.setWrapSelectorWheel(false);

        view.findViewById(R.id.postpone_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store new value
                settings.setNotifRepetInterval(numberPicker.getValue());
                // Update visual text:
                notifPostponeNumberTextView.setText(String.valueOf(settings.getNotifRepetInterval()));
                notifPostponeHourTextView.setText(getResources().getQuantityString(R.plurals.hours, settings.getNotifRepetInterval()));
                alertDialog.dismiss();
            }
        });

        if(alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        alertDialog.show();
    }

    public void onDeleteBoxClick(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setMessage(R.string.settings_delete_all_warning_text)
                .setTitle(R.string.settings_delete_all_warning_title)
                .setCancelable(true)
                .setPositiveButton(R.string.settings_delete_all_warning_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //Delete all plants
                        PlantList plantList = PlantList.getInstance(SettingsActivity.this);
                        plantList.deleteAll(SettingsActivity.this);
                        setResult(CommunicationKeys.Settings_Main_ResultDeleteAll);

                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.settings_delete_all_warning_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onLicensesBoxClick(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(this).inflate(
                R.layout.settings_licenses_dialog,
                (ConstraintLayout) findViewById(R.id.layout_dialog_container)
        );

        //Make the links clickable
        TextView textView = (TextView) view.findViewById(R.id.settings_license_text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.license_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        if(alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        alertDialog.show();
    }

    public void onAboutBoxClick(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(this).inflate(
                R.layout.settings_about_dialog,
                (ConstraintLayout) findViewById(R.id.layout_dialog_container)
        );
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.about_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        if(alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        alertDialog.show();
    }

    public void onHomeButtonClick(View v){
        finish();   // Carry DeleteAll flag if applicable
    }

}
