package com.kinocode.justdoit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kinocode.justdoit.model.Notes;
import com.kinocode.justdoit.model.NotifPublish;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNoteActivity extends AppCompatActivity {

    @BindView(R.id.btn_createNoteSave) Button btnSave;
    @BindView(R.id.btn_speech_content) ImageButton btnSpeechContent;
    @BindView(R.id.btn_speech_title) ImageButton btnSpeechTitle;
    @BindView(R.id.et_createNoteContent) EditText etContent;
    @BindView(R.id.et_createNoteTitle) EditText etTitle;
    @BindView(R.id.spinner_createNoteCategory) Spinner spinCategory;
    @BindView(R.id.spinner_createNotePriority) Spinner spinPriority;
    @BindView(R.id.datepick_createNoteDate) DatePicker datePicker;

    FirebaseDatabase firebaseDatabase;
    public static Boolean ListSelected;
    public static int ListPosition;
    String[] items_priority = {"Low", "Medium", "High"};
    String[] items_category = {"Work", "School", "Family", "Other"};
    public static Notes notes = new Notes();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_create_note);

        ButterKnife.bind(this);

        firebaseDatabase = FirebaseDatabase.getInstance();


        MainActivity.first = false;

        ArrayAdapter<String> adapter_priority = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items_priority);
        ArrayAdapter<String> adapter_category = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items_category);

        spinPriority.setAdapter(adapter_priority);
        spinCategory.setAdapter(adapter_category);

        if (ListSelected){
            for (int i = 0; i < 3; i++){
                if (spinCategory.getItemAtPosition(i).toString().equalsIgnoreCase(MainActivity.notes.get(ListPosition).getCategory()))spinCategory.setSelection(i);
                if (spinPriority.getItemAtPosition(i).toString().equalsIgnoreCase(MainActivity.notes.get(ListPosition).getPriority()))spinPriority.setSelection(i);
            }

            etTitle.setText("" + MainActivity.notes.get(ListPosition).getTitle());
            etContent.setText("" + MainActivity.notes.get(ListPosition).getContent());

            String[] parts = MainActivity.notes.get(ListPosition).getDeadline_date().split("/");
            String day = parts[0];
            String month = parts[1];
            String year = parts[2];
            datePicker.init(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), null);

        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ListSelected){
                    Notes new_note = new Notes();
                    new_note.setTitle(etTitle.getText().toString());
                    new_note.setContent(etContent.getText().toString());

                    int day = datePicker.getDayOfMonth();
                    int month = datePicker.getMonth() + 1;
                    int year = datePicker.getYear();
                    new_note.setDeadline_date(Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year));

                    String foramattedDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    new_note.setWriting_date(foramattedDate);

                    new_note.setCategory(spinCategory.getSelectedItem().toString());
                    new_note.setPriority(spinPriority.getSelectedItem().toString());
                    MainActivity.notes.add(new_note);

                    int delay_year = year - Calendar.getInstance().get(Calendar.YEAR);
                    int delay_month = month - (Calendar.getInstance().get(Calendar.MONTH)+1);
                    int delay_day = day - Calendar.getInstance().get(Calendar.DATE);

                    double delay_year_mili = 3155760000.0 * delay_year;
                    double delay_month_mili = TimeUnit.DAYS.toMillis(delay_month*30);
                    double delay_day_mili = TimeUnit.DAYS.toMillis(delay_day);

                    long delay = (long) ( delay_day_mili  + delay_month_mili + delay_year_mili);

                    scheduleNotification(getNotification("5 second delay"), delay);

                }else {
                    //Update existing note
                    int day = datePicker.getDayOfMonth();
                    int month = datePicker.getMonth() + 1;
                    int year = datePicker.getYear();

                    String formattedDate = java.text.DateFormat.getDateTimeInstance().format(new Date());

                    MainActivity.notes.get(ListPosition).setPriority(spinPriority.getSelectedItem().toString());
                    MainActivity.notes.get(ListPosition).setCategory(spinCategory.getSelectedItem().toString());
                    MainActivity.notes.get(ListPosition).setWriting_date(formattedDate);
                    MainActivity.notes.get(ListPosition).setDeadline_date(Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year));
                    MainActivity.notes.get(ListPosition).setContent(etContent.getText().toString());
                    MainActivity.notes.get(ListPosition).setTitle(etTitle.getText().toString());

                    int delay_year = year - Calendar.getInstance().get(Calendar.YEAR);
                    int delay_month = month - (Calendar.getInstance().get(Calendar.MONTH)+1);
                    int delay_day = day - Calendar.getInstance().get(Calendar.DATE);

                    double delay_year_mili = 3155760000.0 * delay_year;
                    double delay_month_mili = TimeUnit.DAYS.toMillis(delay_month*30);
                    double delay_day_mili = TimeUnit.DAYS.toMillis(delay_day);

                    long delay = (long) ( delay_day_mili  + delay_month_mili + delay_year_mili);

                    scheduleNotification(getNotification("5 second delay"), delay);
                }

                database_operations();
                finish();
                startActivity(new Intent(CreateNoteActivity.this, MainActivity.class));
            }
        });

        btnSpeechContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_get_content));
                try {

                    startActivityForResult(intent, 1 );

                }catch (ActivityNotFoundException a){
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.speech_support),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSpeechTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_get_title));
                try {

                    startActivityForResult(intent, 2 );

                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.speech_support),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1: {
                if (resultCode == RESULT_OK && null != data){
                    ArrayList<String> notes = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etContent.setText(notes.get(0));
                }
                break;
            }

            case 2: {
                if (resultCode == RESULT_OK && null != data){
                    ArrayList<String> notes_title = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etTitle.setText(notes_title.get(0));
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(CreateNoteActivity.this, MainActivity.class));
    }

    private void scheduleNotification(Notification notification, long delay) {
        Intent notificationIntent = new Intent(this, NotifPublish.class);
        notificationIntent.putExtra(NotifPublish.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotifPublish.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content){
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Don't forget!");
        builder.setContentText("Check the deadline for Deadline!");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        return builder.build();
    }

    public void database_operations() {

        //DatabaseReference dbRefUser = MainActivity.firebaseDatabase.getReference(MainActivity.userId);
        //dbRefUser.removeValue();

        DatabaseReference dbRef = MainActivity.firebaseDatabase.getReference(MainActivity.userId + "/Notes/");

        for (int i = 0; i < MainActivity.notes.size(); i++){
            if (MainActivity.notes.get(i).getTitle().length() > 0){
                String key = dbRef.push().getKey();
                DatabaseReference dbRefKey = MainActivity.firebaseDatabase.getReference(MainActivity.userId + "/Notes/" + key);
                dbRefKey.setValue(MainActivity.notes.get(i));
            }else {
                String key = dbRef.push().getKey();
                DatabaseReference dbRefKey = MainActivity.firebaseDatabase.getReference(MainActivity.userId + "/Notes/" + key);
                dbRefKey.removeValue();
            }
        }
    }


}
