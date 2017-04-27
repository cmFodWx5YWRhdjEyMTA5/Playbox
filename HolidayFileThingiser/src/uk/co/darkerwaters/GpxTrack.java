package uk.co.darkerwaters;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

class GpxTrack {
	String name;
	Date startTimeLocal;
	Date endTimeLocal;
	int timeOffset;
	Trackpoint[] track;
	public String timezoneString;
	
	@Override
	public String toString() {
		return name + 
				" starting at " + Main.filenameDateFormat.format(startTimeLocal) +
				" in " + timezoneString + 
				" with " + (timeOffset / (60 * 60 * 1000)) + "hr offset"; 
	}
	
	public GpxTrack(File file, String extension) {
		// add this track data
		Trackpoint[] track = null;
		if (extension.equalsIgnoreCase(".gpx")) {
			try {
				track = GpxReader.readTrack(file);
			} catch (IOException e) {
				Main.error.append("Error: " + e.getMessage() + "." + Main.newline);
			}
		}
		else if (extension.equalsIgnoreCase(".tcx")) {
			try {
				track = TcxReader.readTrack(file);
			} catch (IOException e) {
				Main.error.append("Error: " + e.getMessage() + "." + Main.newline);
			}
		}
		if (null != track && track.length > 0) {
			// add the the list of tracks
			// and work out the timezone offset
			this.name = file.getName();
			this.timeOffset = 0;
			this.timezoneString = TimezoneMapper.latLngToTimezoneString(track[0].getLatitude(), track[0].getLongitude());
			if (null != this.timezoneString) {
				TimeZone timeZone = TimeZone.getTimeZone(this.timezoneString);
				if (null != timeZone) {
					this.timeOffset = timeZone.getOffset(track[0].getTime().getTime());
				}
			}
			
			// offset all the track times to the local filetime string
			for (Trackpoint point : track) {
				long pointTime = point.getTime().getTime() + this.timeOffset;
				String newTimeString = Main.filenameDateFormat.format(new Date(pointTime));
				try {
					point.setTime(Main.filenameDateFormat.parse(newTimeString));
				} catch (ParseException e) {
					Main.error.append("Error: " + e.getMessage() + "." + Main.newline);
				}
			}
			// this is the track now
			this.track = track;
			this.startTimeLocal = track[0].getTime();
			this.endTimeLocal = track[track.length - 1].getTime();
		}
	}
}