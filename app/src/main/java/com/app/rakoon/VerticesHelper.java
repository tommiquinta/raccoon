package com.app.rakoon;


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

	public String getBottom_right() {
		char col = bottom_left.charAt(8);
		int col_int = Integer.parseInt(String.valueOf(col));
		int new_col = col_int + 1;

		if (new_col == 10) {
			bottom_right = bottom_left.substring(0, 7) + new_col + bottom_left.substring(9, 13);
		} else {
			bottom_right = bottom_left.substring(0, 8) + new_col + bottom_left.substring(9, 13);
		}

		return bottom_right;
	}


	public String getTop_left() {
		char row = bottom_left.charAt(12);
		int row_int = Integer.parseInt(String.valueOf(row));
		int new_row = row_int + 1;

		if (new_row == 10) {
			top_left = bottom_left.substring(0, 11) + "10";
		} else {
			top_left = bottom_left.substring(0, 12) + new_row;
		}


		return top_left;
	}

	public String getTop_right() {
		char col = bottom_left.charAt(8);
		int col_int = Character.getNumericValue(col);
		int new_col = col_int + 1;

		char row = bottom_left.charAt(12);
		int row_int = Character.getNumericValue(row);
		int new_row = row_int + 1;

		String top_right;

		if (new_col == 10 && new_row == 10) {
			top_right = bottom_left.substring(0, 7) + new_col + bottom_left.substring(9, 11) + "10";
		} else if (new_col == 10) {
			top_right = bottom_left.substring(0, 7) + new_col + bottom_left.substring(9, 12) + new_row;
		} else if (new_row == 10) {
			top_right = bottom_left.substring(0, 8) + new_col + bottom_left.substring(9, 11) + "10";
		} else {
			top_right = bottom_left.substring(0, 8) + new_col + bottom_left.substring(9, 12) + new_row;
		}

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
