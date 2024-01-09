package com.app.rakoon.Helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class SignalHelper {
	private final Context context;

	public SignalHelper(Context context) {
		this.context = context;
	}

	public int getSignal() {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
			return 0;
		}
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
		CellInfoLte cellInfoLte = null;

		for (CellInfo cellInfo : cellInfoList) {
			if (cellInfo instanceof CellInfoLte) {
				cellInfoLte = (CellInfoLte) cellInfo;
				break;
			}
		}

		int signalStrength = 0;
		int signalLevel = 0;
		if (cellInfoLte != null) {
			CellSignalStrengthLte signalStrengthLte = cellInfoLte.getCellSignalStrength();
			signalStrength = signalStrengthLte.getDbm();
			signalLevel = signalStrengthLte.getLevel();

		}
		return signalLevel;
	}

}
