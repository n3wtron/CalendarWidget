package net.alpha01.view;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class DayTextView extends TextView{
	private Date date;
	public DayTextView(Context context, AttributeSet attrs,Date dt) {
		super(context, attrs);
		this.setDate(dt);
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Date getDate() {
		return date;
	}

}
