package com.app.rakoon.Helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SoundHelper {
	private Context context;
	private boolean isRecording = false;
	private AudioRecord audioRecord;
	private int bufferSize;
	private short[] audioData;
	private double db;

	public SoundHelper(Context context) {
		this.context = context;
		bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
		audioData = new short[bufferSize];
	}

	public double getSound() {
		if (isMicrophonePermissionGranted()) {
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
				return 0;
			}
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			audioRecord.startRecording();
			isRecording = true;

			// wait 1 sec before measuring
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			double amplitude = getAmplitude();
			Log.d("AMPLITUDE", String.valueOf(amplitude));
			db = 20 * Math.log10(amplitude);
			stopRecording();

			return Math.floor(db * 100) / 100;
		}
		return -1;
	}

	private void stopRecording() {
		if (isRecording) {
			isRecording = false;
			audioRecord.stop();
			audioRecord.release();
		}
	}

	private double getAmplitude() {
		double maxAmplitude = 0;

		audioRecord.read(audioData, 0, bufferSize);
		for (short s : audioData) {
			if (Math.abs(s) > maxAmplitude) {
				maxAmplitude = Math.abs(s);
			}
		}
		return maxAmplitude;
	}

	private boolean isMicrophonePermissionGranted() {
		return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
	}

}
