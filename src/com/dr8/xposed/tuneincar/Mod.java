package com.dr8.xposed.tuneincar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getIntField;

import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Mod implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

	private static String PKGNAME = "tunein.player";
	private static String TAG = "TuneInCar: ";
	private Context msplashCtx;
	private Context mbaseCtx;
	private static XSharedPreferences prefs;
	private static int presetsId;
	private static int recentsId;
	private static int recommendedId;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName.equals(PKGNAME)) {
			try {
				findAndHookMethod("tunein.ui.actvities.SplashScreenActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam mparam) {
						Intent localintent = new Intent();
						msplashCtx = (Context) mparam.thisObject;
						UiModeManager mode = (UiModeManager) msplashCtx.getSystemService(Context.UI_MODE_SERVICE);
						if (mode.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
							String mPackage = "tunein.ui.actvities";
							String mClass = ".TuneInCarModeActivity";
							localintent.setComponent(new ComponentName(PKGNAME, mPackage + mClass));
							msplashCtx.startActivity(localintent);
						} else {
							return;
						}
					}
				});
			
				findAndHookMethod("tunein.ui.actvities.TuneInBaseActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam mparam) {
						mbaseCtx = (Context) mparam.thisObject;
					}
				});
				
				findAndHookMethod("tunein.ui.actvities.TuneInCarModeActivity", lpparam.classLoader, "setup", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam mparam) {
						presetsId = (int) getIntField(mparam.thisObject, "presetsCatalogId");
						recentsId = (int) getIntField(mparam.thisObject, "recentsCatalogId");
						recommendedId = (int) getIntField(mparam.thisObject, "recommendedCatalogId");
						XposedBridge.log(TAG + "recommended ID after setup " + recommendedId);
					}
				});
				
				findAndHookMethod("tunein.ui.actvities.TuneInCarModeActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam mparam) {
						prefs.reload();
						if (prefs.getString("default_screen", "follows").equals("follows")) {
							callMethod(mparam.thisObject, "showRequestedScreen", presetsId);
						} else if (prefs.getString("default_screen", "follows").equals("recents")) {
							callMethod(mparam.thisObject, "showRequestedScreen", recentsId);
						} else if (prefs.getString("default_screen", "follows").equals("recommended")) {
							callMethod(mparam.thisObject, "createCatalogRecommended");
							recommendedId = (int) getIntField(mparam.thisObject, "recommendedCatalogId");
							XposedBridge.log(TAG + "recommended ID after create " + recommendedId);
							callMethod(mparam.thisObject, "showRequestedScreen", recommendedId);
							XposedBridge.log(TAG + "recommended ID at request " + recommendedId);
						}
						return;
					}
				});
			} catch (Throwable t) {
				XposedBridge.log(TAG + t);
			}
		}	
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		if (resparam.packageName.equals(PKGNAME)) {
			resparam.res.hookLayout(PKGNAME, "layout", "activity_carmode_buttons_land", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					try {
						Button exitBtn = (Button) liparam.view.findViewById(liparam.res.getIdentifier("carModeExit", "id", PKGNAME));
						exitBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								callMethod(mbaseCtx, "stopAudioAndExit");
							}
						});

					} catch (Throwable t) { XposedBridge.log(t); }
				}
			});
			resparam.res.hookLayout(PKGNAME, "layout", "activity_carmode_buttons", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					try {
						Button exitBtn = (Button) liparam.view.findViewById(liparam.res.getIdentifier("carModeExit", "id", PKGNAME));
						exitBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								callMethod(mbaseCtx, "stopAudioAndExit");
							}
						});

					} catch (Throwable t) { XposedBridge.log(t); }
				}
			});
		} else {
			return;
		}

	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		prefs = new XSharedPreferences("com.dr8.xposed.tuneincar", "com.dr8.xposed.tuneincar_preferences");		
	}

}
