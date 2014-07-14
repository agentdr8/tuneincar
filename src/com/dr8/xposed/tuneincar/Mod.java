package com.dr8.xposed.tuneincar;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Mod implements IXposedHookLoadPackage {

	private static String PKGNAME = "tunein.player";
	private static String TAG = "TuneInCar: ";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName.equals(PKGNAME)) {
			try {
				findAndHookMethod("tunein.ui.actvities.SplashScreenActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam mparam) {
						Intent localintent = new Intent();
						Context mCtx = (Context) mparam.thisObject;
						String mPackage = "tunein.ui.actvities";
						String mClass = ".TuneInCarModeActivity";
						localintent.setComponent(new ComponentName(PKGNAME, mPackage + mClass));
						mCtx.startActivity(localintent);
					}
				});
			} catch (Throwable t) {
				XposedBridge.log(TAG + t);
			}
		}	
	}

}
