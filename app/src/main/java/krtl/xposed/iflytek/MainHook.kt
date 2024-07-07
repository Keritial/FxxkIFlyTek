package krtl.xposed.iflytek

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook: IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName

        when (packageName) {
            "com.android.packageinstaller" -> hookPackageInstaller(lpparam)
            else -> return
        }
    }

    private fun hookPackageInstaller(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageInstallerClassLoader = lpparam.classLoader
        val packageInstallerActivity = "com.android.packageinstaller.PackageInstallerActivity"

        // Bypass checks and initiate install
        findAndHookMethod(
            packageInstallerActivity,
            packageInstallerClassLoader,
            "checkIfAllowedAndInitiateInstall",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedHelpers.callMethod(param.thisObject, "initiateInstall")
                    param.result = null // Prevent original method execution
                }
            }
        )

        // Allow new app installs
        findAndHookMethod(
            packageInstallerActivity,
            packageInstallerClassLoader,
            // IFlyTek-proprietary method for preventing new app installs
            "isPackageInstalled",
            String::class.java,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam) = true
            }
        )

        // Some unknown weird stuff
        findAndHookMethod(
            packageInstallerActivity,
            packageInstallerClassLoader,
            "onResume",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mOkButton = getObjectField(param.thisObject, "mOk")
                    val mEnableOk = getBooleanField(param.thisObject, "mEnableOk")

                    if (mOkButton != null) {
                        XposedHelpers.callMethod(mOkButton, "setEnabled", mEnableOk)
                    }
                }
            }
        )
    }
}