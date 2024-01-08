package com.app.rakoon.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.app.rakoon.R;


public class Settings extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		// Ottieni la preferenza numerica
		EditTextPreference numericPreference = findPreference("numeric_preference");

		// Imposta il listener per il cambiamento di preferenza
		assert numericPreference != null;
		numericPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			// Verifica se il nuovo valore è un numero intero
			try {
				int numericValue = Integer.parseInt((String) newValue);

				if (numericValue < 1 || numericValue > 10) {
					Toast.makeText(requireContext(), "Inserisci un numero compreso tra 1 e 10", Toast.LENGTH_SHORT).show();
					return false;
				}
				preference.setSummary(String.valueOf(numericValue));
				return true;
			} catch (NumberFormatException e) {
				Toast.makeText(requireContext(), "Inserisci un numero intero valido", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}
}

