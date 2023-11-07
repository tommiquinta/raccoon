package com.app.rakoon.Helpers;


import android.util.Log;

import java.text.ParseException;

import mil.nga.mgrs.MGRS;

/**
 * this is an helper-class used to get the other three vertices given the bottom-left one in MGRS
 */
public class VerticesHelper {
	public void setBottom_left(String bottom_left) {
		this.bottom_left = bottom_left;
	}

	private String bottom_left;

	public String getBottom_left() {

		return bottom_left;
	}

	public String getBottom_right() throws ParseException {
		long easting = MGRS.parse(bottom_left).getEasting();
		easting = easting / 10;
		easting = easting + 1;
		long northing = MGRS.parse(bottom_left).getNorthing();
		northing = northing / 10;

		bottom_right = bottom_left.substring(0, 5) + easting + northing;

		return bottom_right;
	}


	public String getTop_left() throws ParseException {
		long easting = MGRS.parse(bottom_left).getEasting();
		easting = easting / 10;
		long northing = MGRS.parse(bottom_left).getNorthing();
		northing = northing / 10;
		northing = northing + 1;

		top_left = bottom_left.substring(0, 5) + easting + northing;

		return top_left;
	}

	public String getTop_right() throws ParseException {
		long easting = MGRS.parse(bottom_left).getEasting();
		easting = easting / 10;
		easting = easting + 1;
		long northing = MGRS.parse(bottom_left).getNorthing();
		northing = northing / 10;
		northing = northing + 1;

		top_right = bottom_left.substring(0, 5) + easting + northing;

		return top_right;
	}


	private String bottom_right;

	private String top_left;

	private String top_right;

	public VerticesHelper() {
		String bottom_left = this.bottom_left;
	}

	;


}
