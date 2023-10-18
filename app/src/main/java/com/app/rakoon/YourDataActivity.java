package com.app.rakoon;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class YourDataActivity extends AppCompatActivity {
	private ListView data_view;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_layout);
		data_view = findViewById(R.id.data_view);

		loadData();
	}

	private void loadData() {
		new LoadDataAsyncTask().execute();
	}

	private void updateUI(List<?> data) {
		ArrayAdapter<?> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
		data_view.setAdapter(arrayAdapter);
	}

	private class LoadDataAsyncTask extends AsyncTask<Void, Void, List<?>> {
		@Override
		protected List<?> doInBackground(Void... params) {
			DatabaseHelper databaseHelper = new DatabaseHelper(YourDataActivity.this);
			return databaseHelper.getSounds();
		}

		@Override
		protected void onPostExecute(List<?> data) {
			if (data != null) {
				updateUI(data);
			} else {
				Toast.makeText(YourDataActivity.this, "error fetching data",Toast.LENGTH_SHORT).show();
			}
		}
	}
}
