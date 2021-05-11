package com.arhiser.todolist.screens.details;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.arhiser.todolist.App;
import com.arhiser.todolist.EgorService;
import com.arhiser.todolist.R;
import com.arhiser.todolist.model.Note;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NoteDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE = "NoteDetailsActivity.EXTRA_NOTE";

    private Note note;

    private EditText nameText;
    private EditText editText;
    private DatePicker dateText;
    private TimePicker timeText;

    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static int NOTIFY_ID;


    public static void start(Activity caller, Note note) {
        Intent intent = new Intent(caller, NoteDetailsActivity.class);
        if (note != null) {
            intent.putExtra(EXTRA_NOTE, note);
        }
        caller.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setTitle(getString(R.string.note_details_title));

        editText = findViewById(R.id.text);
        dateText = findViewById(R.id.date);
        timeText = findViewById(R.id.time_text);
        nameText = findViewById(R.id.text_name);

        if (getIntent().hasExtra(EXTRA_NOTE)) {
            note = getIntent().getParcelableExtra(EXTRA_NOTE);
            editText.setText(note.text);
            Timestamp sub_date = new Timestamp(note.date);
            String tmp = sub_date.toString();
            dateText.updateDate(Integer.parseInt(tmp.substring(0,4)), Integer.parseInt(tmp.substring(5,7))-1, Integer.parseInt(tmp.substring(8,10)));
            timeText.setCurrentHour(sub_date.getHours());
            timeText.setCurrentMinute(sub_date.getMinutes());
            nameText.setText(note.name);
        } else {
            note = new Note();
        }

        //------------------
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //-----------------

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                if (editText.getText().length() > 0) {
                    note.text = editText.getText().toString();
                    note.name = nameText.getText().toString();
                    note.done = false;
                    note.timestamp = System.currentTimeMillis();
                    Timestamp date_ = new Timestamp(dateText.getCalendarView().getDate());
                    date_.setHours(timeText.getHour());
                    date_.setMinutes(timeText.getMinute());
                    date_.setNanos(0);
                    note.date = date_.getTime();

                    //----------


                    final Intent notificationIntent = new Intent(this, EgorService.class);
                    notificationIntent.putExtra("Objective Time", timeText.getHour()+":"+timeText.getMinute());
                    notificationIntent.putExtra("Objective Date", dateText.getCalendarView().getDate());
                    notificationIntent.putExtra("Objective Name", nameText.getText().toString());
                    notificationIntent.putExtra("Objective Text", editText.getText().toString());

                    Calendar CurrentMoment = Calendar.getInstance();
                    int CurrentTimeHours = CurrentMoment.get(Calendar.HOUR_OF_DAY);
                    int CurrentTimeMinutes = CurrentMoment.get(Calendar.MINUTE);
                    int CurrentMTime = CurrentTimeMinutes + CurrentTimeHours * 60;
                    String CurrentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).toString();
                    String ObjectiveTime = timeText.getHour()+":"+timeText.getMinute();
                    String[] ObjectiveTimeArray = ObjectiveTime.split(":");
                    int ObjectiveTimeHours = Integer.parseInt(ObjectiveTimeArray[0]);
                    int ObjectiveTimeMinutes = Integer.parseInt(ObjectiveTimeArray[1]);
                    int ObjectiveMTime = ObjectiveTimeMinutes + ObjectiveTimeHours * 60;
                    long CurrentInterval = (ObjectiveMTime - CurrentMTime);
                    if (CurrentInterval <= 0 || CurrentTime == ObjectiveTime) {
                        startService(notificationIntent);
                    }
                    else {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startService(notificationIntent);
                            }
                        }, 60000*CurrentInterval-CurrentMoment.get(Calendar.SECOND)*1000);
                    }

                    if (getIntent().hasExtra(EXTRA_NOTE)) {
                        App.getInstance().getNoteDao().update(note);
                    } else {
                        App.getInstance().getNoteDao().insert(note);
                    }
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
