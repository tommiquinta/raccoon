package com.app.rakoon.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import com.app.rakoon.R;

public class Settings extends PreferenceFragmentCompat {

	private static final String PREFERENCE_KEY = "numeric_preference";
	private static final String NUMBER = "last_measurements";
	private static final String SIGNAL = "signal_bg";

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		EditTextPreference numericPreference = findPreference(PREFERENCE_KEY);
		EditTextPreference number = findPreference(NUMBER);

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

	public static boolean get_signal_bg(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(SIGNAL, false);
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
