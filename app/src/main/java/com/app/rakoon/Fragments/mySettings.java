package com.app.rakoon.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import com.app.rakoon.R;
import com.app.rakoon.Services.Constants;

public class mySettings extends PreferenceFragmentCompat {

	public static final String ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS";
	public static final String ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS = "2";
	private static final String PREFERENCE_KEY = "numeric_preference";
	private static final String NUMBER = "last_measurements";

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		EditTextPreference numericPreference = findPreference(PREFERENCE_KEY);
		EditTextPreference number = findPreference(NUMBER);

		CheckBoxPreference signal_bg = findPreference(Constants.SIGNAL);
		CheckBoxPreference sound_bg = findPreference(Constants.SOUND);
		CheckBoxPreference wifi_bg = findPreference(Constants.WIFI);

		assert numericPreference != null;
		numericPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			try {
				int numericValue = Integer.parseInt((String) newValue);

				if (numericValue < 1 || numericValue > 10) {
					Toast.makeText(requireContext(), "Inserisci un numero compreso tra 1 e 10", Toast.LENGTH_SHORT).show();
					return false;
				}
				saveNumericValue(requireContext(), numericValue);
				numericPreference.setSummary(String.valueOf(numericValue));
				preference.setSummary(String.valueOf(numericValue));
				return true;
			} catch (NumberFormatException e) {
				Toast.makeText(requireContext(), "Inserisci un numero intero valido", Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		assert signal_bg != null;
		signal_bg.setOnPreferenceChangeListener((preference, newValue) -> {
			try {
				boolean signal = (boolean) newValue;
				save_boolean(requireContext(), signal, Constants.SIGNAL);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});

		assert sound_bg != null;
		sound_bg.setOnPreferenceChangeListener((preference, newValue) -> {
			try {
				boolean sound = (boolean) newValue;
				save_boolean(requireContext(), sound, Constants.SOUND);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});

		assert wifi_bg != null;
		wifi_bg.setOnPreferenceChangeListener((preference, newValue) -> {
			try {
				boolean wifi = (boolean) newValue;

				save_boolean(requireContext(), wifi, Constants.WIFI);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});

		assert number != null;
		number.setOnPreferenceChangeListener((preference, newValue) -> {
			try {
				int numberValue = Integer.parseInt((String) newValue);

				if (numberValue < 1) {
					Toast.makeText(requireContext(), "Inserisci un numero maggiore di 0", Toast.LENGTH_SHORT).show();
					return false;
				}
				saveNumber(requireContext(), numberValue);
				number.setSummary(String.valueOf(numberValue));
				preference.setSummary(String.valueOf(numberValue));
				return true;
			} catch (NumberFormatException e) {
				Toast.makeText(requireContext(), "Inserisci un numero intero valido", Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		int savedNumericValue = getNumericValue(requireContext());
		int savedNumber = getNumber(requireContext());
		numericPreference.setSummary(String.valueOf(savedNumericValue));
		number.setSummary(String.valueOf(savedNumber));
	}
	private static void saveNumericValue(Context context, int numericValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREFERENCE_KEY, numericValue);
		editor.apply();
	}
	private static void saveNumber(Context context, int numericValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(NUMBER, numericValue);
		editor.apply();
	}

	public static int getNumericValue(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		return sharedPreferences.getInt(PREFERENCE_KEY, 3);
	}

	public static int getNumber(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		return sharedPreferences.getInt(NUMBER, 3);
	}

	private static void save_boolean(Context context, boolean bool, String type) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(type, bool);
		editor.apply();
	}

	public static boolean get_boolean_bg(Context context, String type){
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(type, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		int savedNumericValue = getNumericValue(requireContext());
		int savedNumber = getNumber(requireContext());

		EditTextPreference numericPreference = findPreference(PREFERENCE_KEY);
		EditTextPreference number = findPreference(NUMBER);

		if (numericPreference != null) {
			numericPreference.setSummary(String.valueOf(savedNumericValue));
		}

		if (number != null) {
			number.setSummary(String.valueOf(savedNumber));
		}
	}
}
