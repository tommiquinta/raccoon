package com.app.rakoon;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SoundEntry.class}, version = 1)
public abstract class SoundDatabase extends RoomDatabase {

	public abstract SoundEntryInterface getSoundInterface();

}
