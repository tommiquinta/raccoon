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

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		EditTextPreference numericPreference = findPreference(PREFERENCE_KEY);

		assert numericPreference != null;
		numericPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			try {
				int numericValue = Integer.parseInt((String) newValue);

				if (numericValue < 1 || numericValue > 10) {
					Toast.makeText(requireContext(), "Inserisci un numero compreso tra 1 e 10", Toast.LENGTH_SHORT).show();
					return false;
				}


				saveNumericValue(requireContext(), numericValue);

				// Aggiorna la visualizzazione della preferenza
				numericPreference.setSummary(String.valueOf(numericValue));

				preference.setSummary(String.valueOf(numericValue));
				// Imposta il summary con il valore attuale dalle SharedPreferences
				int savedNumericValue = getNumericValue(requireContext());
				numericPreference.setSummary(String.valueOf(savedNumericValue));

				return true;

			} catch (NumberFormatException e) {
				Toast.makeText(requireContext(), "Inserisci un numero intero valido", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}

	// Metodo per salvare il valore nelle SharedPreferences
	private static void saveNumericValue(Context context, int numericValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREFERENCE_KEY, numericValue);
		editor.apply();
	}

	// Metodo per recuperare il valore dalle SharedPreferences
	public static int getNumericValue(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		return sharedPreferences.getInt(PREFERENCE_KEY, 3); // Il valore di default è 0
	}

	@Override
	public void onResume() {
		super.onResume();
		int savedNumericValue = getNumericValue(requireContext());

		EditTextPreference numericPreference = findPreference(PREFERENCE_KEY);

		if (numericPreference != null) {
			numericPreference.setSummary(String.valueOf(savedNumericValue));
		}
	}

}
