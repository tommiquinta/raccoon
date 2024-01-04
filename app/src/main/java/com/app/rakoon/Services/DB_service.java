package com.app.rakoon.Services;

import android.content.Context;

import com.app.rakoon.Database.DatabaseHelper;
import com.app.rakoon.Database.SignalEntry;

public class DB_service {
	public Context context;
	private final DatabaseHelper databaseHelper;

	public DB_service(Context context) {
		this.context = context;
		databaseHelper = new DatabaseHelper(context);
	}

	public void save(SignalEntry se){
		databaseHelper.addSignalEntry(se);
	}
}
