package krtl.xposed.iflytekt10

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook: IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!lpparam.packageName.equals("com.android.packageinstaller")) {
            return
        }
        hook(lpparam)
    }

    private fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "com.android.packageinstaller.PackageInstallerActivity",
            lpparam.classLoader,
            "checkIfAllowedAndInitiateInstall",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val activityInstance = param.thisObject
                    XposedHelpers.callMethod(activityInstance, "initiateInstall")
                    param.result = null
                }
            }
        )

    }
}