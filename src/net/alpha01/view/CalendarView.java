package net.alpha01.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;

import net.alpha01.R;
import net.alpha01.listener.DayClickListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class CalendarView extends TableLayout {
	private ArrayList<DayTextView> days=new ArrayList<DayTextView>();
	private TableRow rows[]=new TableRow[6];
	private DayClickListener dayClickListener=null;
	private Calendar cal;
	private HashSet<Date> highlighedDate=new HashSet<Date>();
	private int highLightColor=Color.RED;
	private TextView nextLbl,prevLbl;
	private TextView currMonthTextView;
	private Resources res;
	
	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.DKGRAY);
		//retreive resources
		res=getResources();
		
		//retrieve the year and month argument
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
		int m = a.getIndex(R.styleable.CalendarView_month);
		int y = a.getIndex(R.styleable.CalendarView_year);
		setCal(GregorianCalendar.getInstance());
		if (m > 0 && y >0 ){
			cal.set(Calendar.YEAR, y);
			cal.set(Calendar.MONTH, m);
		}
		
		//create the first row of buttons
		prevLbl=new TextView(context, attrs);
		prevLbl.setText("<<");
		prevLbl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cal.add(Calendar.MONTH,-1);
				refresh();
			}
		});
		
		nextLbl=new TextView(context, attrs);
		nextLbl.setText(">>");
		nextLbl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cal.add(Calendar.MONTH,1);
				refresh();
			}
		});
		
		currMonthTextView=new TextView(context, attrs);
		currMonthTextView.setGravity(Gravity.CENTER);
				
		TableRow firstRow = new TableRow(context, attrs);
		firstRow.addView(prevLbl);
		android.widget.TableRow.LayoutParams firstRowLayout=firstRow.generateLayoutParams(attrs);
		firstRowLayout.span=5;
		firstRow.addView(currMonthTextView,firstRowLayout);
		firstRow.addView(nextLbl);
		addView(firstRow,0);
		
		//Second Row ( Day Of weeks row ) 
		TypedArray dayOfWeek = res.obtainTypedArray(R.array.DaysOfWeek);
		TableRow secondRow = new TableRow(context,attrs);
		for (int i=1;i<=7;i++){
			TextView dayLbl = new TextView(context,attrs);
			dayLbl.setText(dayOfWeek.getString(i-1));
			dayLbl.setGravity(Gravity.CENTER);
			secondRow.addView(dayLbl);
		}
		addView(secondRow);
		
		//Set layout for each row (to do border)
		LayoutParams singleRowLayout=generateLayoutParams(attrs);
		singleRowLayout.setMargins(0, 1, 0, 0);
		for (int r=0;r<6;r++){
			rows[r]=new TableRow(context, attrs);
			rows[r].setBackgroundColor(Color.BLACK);
			
			for (int i=0;i<7;i++){
				int curDow = (r*7)+(i+1);
				DayTextView day = new DayTextView(context, attrs,getCal().getTime());
				
				day.setWidth(30);
				day.setText(Integer.toString(curDow));
				day.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (dayClickListener!=null){
							dayClickListener.onDayClick(v, ((DayTextView)v).getDate());
						}
					}
				});
				days.add(day);
				rows[r].addView(day,i);
			}
			addView(rows[r],singleRowLayout);
		}
		//refresh the current Date
		refresh();
	}
	
	private void disableDay (DayTextView d){
		d.setText(" ");
		d.setEnabled(false);
		d.setBackgroundColor(Color.TRANSPARENT);
	}
	
	public void refresh(){
		refresh(this.getCal());
	}
	
	public void  refresh (Calendar aCal){
		setCal(aCal);
		Calendar tmpCal = GregorianCalendar.getInstance();
		tmpCal.setTime(cal.getTime());
		
		TypedArray monthName = res.obtainTypedArray(R.array.MonthName);
		currMonthTextView.setText(monthName.getString(tmpCal.get(Calendar.MONTH))+" "+tmpCal.get(Calendar.YEAR));
		//retrieve the first day of week
		int fdow = cal.get(Calendar.DAY_OF_WEEK)-1;
		//1 is Sunday
		if (fdow==0){
			fdow=7;
		}
		
		Object daysAr[]=days.toArray();
		//disable the first days
		for (int i=0;i<fdow-1;i++){
			disableDay((DayTextView)daysAr[i]);
		}
		//Set the day number
		int numOfDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		Log.d("CalView","numOfDays:"+numOfDays);
		for (int i=1;i<=numOfDays;i++){
			int arPos=i+fdow-2;
			Log.d("CalView","Pos"+arPos+"CurrDay:"+tmpCal.get(Calendar.DAY_OF_MONTH));
			((DayTextView)daysAr[arPos]).setDate(tmpCal.getTime());
			((DayTextView)daysAr[arPos]).setText(Integer.toString(tmpCal.get(Calendar.DAY_OF_MONTH)));
			((DayTextView)daysAr[arPos]).setEnabled(true);
			//Check if highlighted Day
			if (highlighedDate.contains(tmpCal.getTime())){
				((DayTextView)daysAr[arPos]).setTextColor(highLightColor);
			}
			Log.d("CalView", "day:"+i);
			//increment the day
			tmpCal.add(Calendar.DAY_OF_MONTH,1);
		}
		for (int i=numOfDays+fdow-1;i<daysAr.length;i++){
			disableDay((DayTextView)daysAr[i]);
		}
	}
	
	public void setCal(Calendar aCal) {
		if (cal==null){
			cal=GregorianCalendar.getInstance();
		}
		cal.setTime(aCal.getTime());
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
	}

	public Calendar getCal() {
		return cal;
	}

	public void setOnDayClickListener (DayClickListener listener){
		this.dayClickListener=listener;
	}

	public void setHighlighedDate(HashSet<Date> aHighlighedDate) {
		this.highlighedDate.clear();
		Iterator<Date> itD = aHighlighedDate.iterator();
		Calendar cal = GregorianCalendar.getInstance();
		while (itD.hasNext()){
			cal.setTime(itD.next());
			cal.set(Calendar.HOUR,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			highlighedDate.add(cal.getTime());
		}
	}

	public HashSet<Date> getHighlighedDate() {
		return highlighedDate;
	}


	public void setHighLightColor(int highLightColor) {
		this.highLightColor = highLightColor;
	}


	public int getHighLightColor() {
		return highLightColor;
	}

}
