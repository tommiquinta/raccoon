package com.app.rakoon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
 public interface SoundEntryInterface {
	@Insert
	public void addEntry(SoundEntry soundEntry);

	@Update
	public void upfateEntry(SoundEntry soundEntry);

	@Delete
	public void deleteEntry(SoundEntry soundEntry);

	@Query("select * from Sound")
	public List<SoundEntry> getAllSoundEntries();

	@Query("select * from Sound where sound_id==:id")
	public SoundEntry getSoundEntry(int id);

}
