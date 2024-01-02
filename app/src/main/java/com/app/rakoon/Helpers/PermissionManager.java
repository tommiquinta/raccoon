package com.app.rakoon.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class PermissionManager {

	private static PermissionManager instance = null;
	private Context context;

	private PermissionManager() {
	}

	// singelton
	public static PermissionManager getInstance(Context context) {
		if (instance == null) {
			instance = new PermissionManager();
		}
		instance.init(context);
		return instance;
	}

	private void init(Context context) {
		this.context = context;
	}

	public boolean checkPermissions(String[] permissions) {
		int size = permissions.length;

		for (int i = 0; i < size; i++) {
			if(ContextCompat.checkSelfPermission(context, permissions[i]) == PermissionChecker.PERMISSION_DENIED) {
				return false;
			}
		}
		return true;
	}

	public boolean handlePermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResult){
		boolean allGranted = true;

		if(grantResult.length > 0){
			for(int i=0; i<grantResult.length; i++){
				if(grantResult[i] == PackageManager.PERMISSION_GRANTED){
					Toast.makeText(activity, "Permission granted.", Toast.LENGTH_SHORT).show();
				} else {
					allGranted = false;
					Toast.makeText(activity, "Permission denied.", Toast.LENGTH_SHORT).show();
					showPermissionRationale(activity, requestCode, permissions, permissions[i]);
					break;
				}
			}
		} else {
			allGranted = false;
		}
		return allGranted;
	}

	private void showPermissionRationale(Activity activity, int requestCode, String[] permissions, String deniedPermission) {
		if (ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)) {
			showMessageBox("You need to allow all permissions!",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							askPermissions(activity, permissions, requestCode);
						}
					},
					new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							// Azioni in caso di annullamento (opzionale)
						}
					});
		}
	}

	private void showMessageBox(String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnCancelListener cancelListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context); // Assicurati di passare il contesto corretto (potrebbe essere 'activity' nel tuo caso)
		builder.setMessage(message)
				.setPositiveButton("OK", positiveListener)
				.setOnCancelListener(cancelListener)
				.create()
				.show();
	}

	public void askPermissions(Activity activity, String[] permissions, int requestCode) {
		ActivityCompat.requestPermissions(activity, permissions, requestCode);
	}
}


