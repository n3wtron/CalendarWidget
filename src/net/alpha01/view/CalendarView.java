package net.alpha01.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import net.alpha01.R;
import net.alpha01.listener.DayClickListener;
import net.alpha01.listener.MonthClickListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CalendarView extends TableLayout {
	private ArrayList<DayTextView> days = new ArrayList<DayTextView>();
	private Object daysAr[];
	private TableRow rows[] = new TableRow[6];
	private DayClickListener dayClickListener = null;
	private Calendar cal;
	private HashMap<Date,Integer> highlightedColorDate= new  HashMap<Date, Integer>();
	private Button nextBtn, prevBtn;
	private TextView currMonthTextView;
	private Resources res;
	private MonthClickListener monthClickListener;

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.DKGRAY);
		// Retrieve resources
		res = getResources();

		// retrieve the year and month argument
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
		setCal(GregorianCalendar.getInstance());

		int m = a.getInteger(R.styleable.CalendarView_month, 0);
		int y = a.getInteger(R.styleable.CalendarView_year, 0);
		if (m > 0) {
			cal.set(Calendar.MONTH, m - 1);
		}
		if (y > 0) {
			cal.set(Calendar.YEAR, y);
		}

		setStretchAllColumns(a.getBoolean(R.styleable.CalendarView_stretchColumns, false));

		// create the first row of buttons
		prevBtn = new Button(context, attrs);
		prevBtn.setText("<<");
		prevBtn.setMaxHeight(10);
		prevBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prevMonth();
			}
		});

		nextBtn = new Button(context, attrs);
		nextBtn.setText(">>");
		nextBtn.setMaxHeight(10);
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextMonth();
			}
		});

		currMonthTextView = new TextView(context, attrs);
		currMonthTextView.setGravity(Gravity.CENTER);

		TableRow firstRow = new TableRow(context, attrs);
		firstRow.addView(prevBtn);
		android.widget.TableRow.LayoutParams firstRowLayout = firstRow.generateLayoutParams(attrs);
		firstRowLayout.span = 5;
		firstRow.addView(currMonthTextView, firstRowLayout);
		firstRow.addView(nextBtn);
		addView(firstRow, 0);

		// Second Row ( Day Of weeks row )
		TypedArray dayOfWeek = res.obtainTypedArray(R.array.DaysOfWeek);
		TableRow secondRow = new TableRow(context, attrs);
		android.widget.TableRow.LayoutParams secondRowParam = secondRow.generateLayoutParams(attrs);
		for (int i = 1; i <= 7; i++) {
			if (i == 0) {
				secondRowParam.setMargins(1, 0, 1, 1);
			} else {
				secondRowParam.setMargins(0, 0, 1, 1);
			}
			TextView dayLbl = new TextView(context, attrs);
			dayLbl.setBackgroundColor(Color.BLACK);
			dayLbl.setText(dayOfWeek.getString(i - 1));
			dayLbl.setGravity(Gravity.CENTER);
			secondRow.addView(dayLbl, secondRowParam);
		}
		addView(secondRow);

		// Set layout for each row (to do border)
		LayoutParams singleRowLayout = generateLayoutParams(attrs);
		singleRowLayout.setMargins(0, 1, 0, 0);
		for (int r = 0; r < 6; r++) {
			rows[r] = new TableRow(context, attrs);
			android.widget.TableRow.LayoutParams rowParam = rows[r].generateLayoutParams(attrs);
			for (int i = 0; i < 7; i++) {
				if (i == 0) {
					rowParam.setMargins(1, 0, 1, 0);
				} else {
					if (r < 5) {
						rowParam.setMargins(0, 0, 1, 0);
					} else {
						rowParam.setMargins(0, 0, 1, 1);
					}
				}
				int curDow = (r * 7) + (i + 1);
				DayTextView day = new DayTextView(context, attrs, getCal().getTime());
				day.setBackgroundColor(Color.BLACK);
				day.setWidth(20);
				day.setText(Integer.toString(curDow));
				days.add(day);
				rows[r].addView(day, i, rowParam);
			}
			addView(rows[r], singleRowLayout);
		}
		daysAr = days.toArray();
		// refresh the current Date
		refresh();
	}

	
	public void nextMonth(){
		cal.add(Calendar.MONTH, 1);
		if (monthClickListener!=null){
			monthClickListener.onNextMonthClickListener(cal);
		}
		refresh();
	}
	
	public void prevMonth(){
		cal.add(Calendar.MONTH, -1);
		if (monthClickListener!=null){
			monthClickListener.onPrevMonthClickListener(cal);
		}
		refresh();
		
	}
	
	/**
	 * Disable a day label 
	 * @param dayView DayTextView to disable
	 */
	private void disableDay(DayTextView dayView) {
		dayView.setText(" ");
		dayView.setEnabled(false);
	}

	/**
	 * Refresh the current month
	 */
	public void refresh() {
		refresh(this.getCal());
	}

	private void refreshSingleDate(int arPos,Calendar currDay){
		Log.d("CalView", "Pos" + arPos + "CurrDay:" + currDay.get(Calendar.DAY_OF_MONTH));
		((DayTextView) daysAr[arPos]).setDate(currDay.getTime());
		((DayTextView) daysAr[arPos]).setText(Integer.toString(currDay.get(Calendar.DAY_OF_MONTH)));
		((DayTextView) daysAr[arPos]).setEnabled(true);
		// Check if highlighted Day
		Date curDate=currDay.getTime();
		curDate.setHours(0);
		if (highlightedColorDate.containsKey(curDate)) {
			((DayTextView) daysAr[arPos]).setTextColor(highlightedColorDate.get(curDate));
		}else{
			((DayTextView) daysAr[arPos]).setTextColor(Color.LTGRAY);
		}
	}
	
	/**
	 * Refresh a single date
	 * @param dt
	 */
	public void refreshSingleDate(Date dt){
		Calendar tmpCal=GregorianCalendar.getInstance();
		tmpCal.setTime(dt);
		int arPos = dt.getDate() + getFdow() - 2;
		refreshSingleDate(arPos, tmpCal);
	}
	
	/**
	 * retrieve the first day of week sum 1 for the human form
	 * @return
	 */
	private int getFdow(){
		int fdow = cal.get(Calendar.DAY_OF_WEEK) - 1;
		// 1 is Sunday
		if (fdow == 0) {
			fdow = 7;
		}
		return fdow;
	}
	
	/**
	 * Refresh the month specified by parameter
	 * @param aCal calendar with month to visualize
	 */
	public void refresh(Calendar aCal) {
		setCal(aCal);
		
		Calendar tmpCal = GregorianCalendar.getInstance();
		tmpCal.setTime(cal.getTime());

		TypedArray monthName = res.obtainTypedArray(R.array.MonthName);
		currMonthTextView.setText(monthName.getString(tmpCal.get(Calendar.MONTH)) + " " + tmpCal.get(Calendar.YEAR));
		// 
		int fdow=getFdow();
		
		// disable the first days
		for (int i = 0; i < fdow - 1; i++) {
			disableDay((DayTextView) daysAr[i]);
		}
		// Set the day number
		int numOfDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		Log.d("CalView", "numOfDays:" + numOfDays);
		for (int i = 1; i <= numOfDays; i++) {
			int arPos = i + fdow - 2;
			refreshSingleDate(arPos, tmpCal);
			Log.d("CalView", "day:" + i);
			// increment the day
			tmpCal.add(Calendar.DAY_OF_MONTH, 1);
		}
		for (int i = numOfDays + fdow - 1; i < daysAr.length; i++) {
			disableDay((DayTextView) daysAr[i]);
		}
	}

	
	private void setCal(Calendar aCal) {
		if (cal == null) {
			cal = GregorianCalendar.getInstance();
		}
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.setTime(aCal.getTime());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
	}

	/**
	 * Retrieve the current calendar
	 * @return current calendar
	 */
	public Calendar getCal() {
		return cal;
	}

	public void setOnDayClickListener(DayClickListener listener) {
		this.dayClickListener = listener;
		Iterator<DayTextView> itD = days.iterator();
		while (itD.hasNext()) {
			DayTextView day = itD.next();
			day.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (dayClickListener != null) {
						dayClickListener.onDayClick(v, ((DayTextView) v).getDate());
					}
				}
			});
		}
	}
	
	public void setOnMonthClickListener(MonthClickListener listener){
		this.monthClickListener=listener;
	}

	/**
	 * Add a day to the highlightedColorList 
	 * @param dt date to add
	 * @param color color to set
	 */
	public void addHighlightedDate(Date dt,int color){
		highlightedColorDate.put(dt, color);
		refreshSingleDate(dt);
	}
	
	
	/**
	 * Clean the specified color and set new highlightedDateList
	 * @param aHighlighedDate
	 * @param color
	 */
	public void setHighlightedDate(HashSet<Date> aHighlighedDate,int color) {
		cleanHighlightedDate(color);
		Iterator<Date> itD = aHighlighedDate.iterator();
		Calendar cal = GregorianCalendar.getInstance();
		while (itD.hasNext()) {
			cal.setTime(itD.next());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			highlightedColorDate.put(cal.getTime(), color);
			refreshSingleDate(cal.getTime());
		}
	}

	/**
	 * Return the highlighted day with the specified color 
	 * @param color 
	 * @return
	 */
	public HashSet<Date> getHighlightedDate(int color) {
		Iterator<Entry<Date,Integer>> itD = this.highlightedColorDate.entrySet().iterator();
		HashSet<Date> result=new HashSet<Date>();
		while (itD.hasNext()){
			Entry<Date,Integer> entity = itD.next();
			if (entity.getValue()==color){
				result.add(entity.getKey());
			}
		}
		return result;
	}

	public void cleanHighlightedDate(int color) {
		Iterator<Entry<Date,Integer>> itD = this.highlightedColorDate.entrySet().iterator();
		while (itD.hasNext()){
			Entry<Date,Integer> entity = itD.next();
			if (entity.getValue()==color){
				highlightedColorDate.remove(entity.getKey());
			}
		}
	}
	
	public void cleanHighlightedDate() {
		this.highlightedColorDate.clear();
	}

}
