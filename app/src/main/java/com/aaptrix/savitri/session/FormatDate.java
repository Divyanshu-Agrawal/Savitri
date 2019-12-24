package com.aaptrix.savitri.session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatDate {
	
	private String currentDate, inputFormat, outputFormat;
	
	public FormatDate(String currentDate, String inputFormat, String outputFormat) {
		this.currentDate = currentDate;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
	}
	
	public String format() {
		SimpleDateFormat format = new SimpleDateFormat(inputFormat, Locale.getDefault());
		Date newDate = null;
		try {
			newDate = format.parse(currentDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		format = new SimpleDateFormat(outputFormat, Locale.getDefault());
		return format.format(newDate);
	}
}
