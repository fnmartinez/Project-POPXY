package ar.elements;

import org.joda.time.DateTime;

public class IntervalTime {

	private int minFrom;
	private int minTo;
	
	public IntervalTime(int minFrom, int minTo){
		this.minFrom = minFrom;
		this.minTo = minTo;
	}
	
	public int getMinFrom() {
		return minFrom;
	}
	public void setMinFrom(int minFrom) {
		this.minFrom = minFrom;
	}
	public int getMinTo() {
		return minTo;
	}
	public void setMinTo(int minTo) {
		this.minTo = minTo;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + minFrom;
		result = prime * result + minTo;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntervalTime other = (IntervalTime) obj;
		if (minFrom != other.minFrom)
			return false;
		if (minTo != other.minTo)
			return false;
		return true;
	}
	public boolean isInInterval() {
		int minute = new DateTime().getMinuteOfDay();
		return minute >= minFrom && minute <= minTo;
	}
	
}
