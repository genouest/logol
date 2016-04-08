package org.irisa.genouest.logol;

public class Interval {
	
	private static final String SEPARATOR=",";
	
	public String interval=null;
	public String x;
	public String y;
	
	public Interval(String interval) {
		this.setInterval(interval);
	}
	
	
	public void setInterval(String interval) {
	if(interval == null) {
		x="0";
		y="0";
		return;
	}
	String tmpInt = interval.substring(1,interval.length()-1);
	String[] values = tmpInt.split(SEPARATOR);
	x=values[0];
	y=values[1];
	}
}
