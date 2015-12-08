package ee.ttu.schedule.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.vadimstrukov.ttuschedule.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import ee.ttu.schedule.drawable.DayOfMonthDrawable;
import ee.ttu.schedule.provider.EventContract;

public class ScheduleFragment extends Fragment implements WeekView.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener {

    public static final int TYPE_DAY_VIEW = 1;
    public static final int TYPE_THREE_DAY_VIEW = 2;
    private static final String ARG_TYPE = "arg_type";
    private int WEEK_TYPE;

    private WeekView mWeekView;
    private String[] colorArray;

    public ScheduleFragment() {

    }

    public static ScheduleFragment newInstance(int type){
        Bundle args = new Bundle();
        ScheduleFragment scheduleFragment = new ScheduleFragment();
        args.putInt(ARG_TYPE, type);
        scheduleFragment.setArguments(args);
        return scheduleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        colorArray = getResources().getStringArray(R.array.colors);
        if(getArguments() != null)
            WEEK_TYPE = getArguments().getInt(ARG_TYPE, TYPE_THREE_DAY_VIEW);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        mWeekView = (WeekView) rootView.findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setDateTimeInterpreter(getDateTimeInterpreter(WEEK_TYPE == TYPE_THREE_DAY_VIEW));
        mWeekView.goToHour(8);
        switch (WEEK_TYPE){
            case TYPE_DAY_VIEW:
                mWeekView.setNumberOfVisibleDays(1);
                break;
            case TYPE_THREE_DAY_VIEW:
                mWeekView.setNumberOfVisibleDays(3);
                break;
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_today);
        setTodayIcon((LayerDrawable) menuItem.getIcon(), getActivity(), "EET");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mWeekView.goToToday();
        mWeekView.goToHour(8);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        String description = null;
        String location = null;
        Cursor cursor = getActivity().getContentResolver().query(EventContract.Event.CONTENT_URI, null, "_id = ?", new String[]{String.valueOf(event.getId())}, null);
        assert cursor != null;
        if(cursor.moveToFirst()){
            description = cursor.getString(3);
            location = cursor.getString(4);
        }
        cursor.close();

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        DecimalFormat mFormat = new DecimalFormat("00");
        mFormat.setRoundingMode(RoundingMode.DOWN);
        alertDialog.setTitle(event.getName());
        String dateStart = mFormat.format((double) event.getStartTime().get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format((double) event.getStartTime().get(Calendar.MINUTE));
        String dateEnd = mFormat.format((double) event.getEndTime().get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format((double) event.getEndTime().get(Calendar.MINUTE));
        alertDialog.setMessage(dateStart + "--" + dateEnd + "\n" + description + "\n" + location);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new LinkedList<>();
        Cursor cursor = getActivity().getContentResolver().query(EventContract.Event.CONTENT_URI, null, null, null, null);
        assert cursor != null;
        if(newMonth == Calendar.getInstance().get(Calendar.MONTH)) {
            if (cursor.moveToFirst()) {
                do {
                    Calendar startTime = GregorianCalendar.getInstance();
                    Calendar endTime = GregorianCalendar.getInstance();
                    startTime.setTime(new Date(cursor.getLong(1)));
                    endTime.setTime(new Date(cursor.getLong(2)));
                    WeekViewEvent event = new WeekViewEvent(cursor.getInt(0), cursor.getString(5), startTime, endTime);
                    event.setColor(Color.parseColor(colorArray[new Random().nextInt(colorArray.length)]));
                    events.add(event);
                }
                while (cursor.moveToNext());
            }
        }
        cursor.close();
        return events;
    }


    private DateTimeInterpreter getDateTimeInterpreter(final boolean shortDate){
        return new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour + ":00";
            }
        };
    }


    private void setTodayIcon(LayerDrawable icon, Context context, String timezone) {
        DayOfMonthDrawable today;
        // Reuse current drawable if possible
        Drawable currentDrawable = icon.findDrawableByLayerId(R.id.today_icon_day);
        if (currentDrawable != null && currentDrawable instanceof DayOfMonthDrawable) {
            today = (DayOfMonthDrawable)currentDrawable;
        } else {
            today = new DayOfMonthDrawable(context);
        }
        // Set the day and update the icon
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getDefault());
        today.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
        icon.mutate();
        icon.setDrawableByLayerId(R.id.today_icon_day, today);
    }
}
