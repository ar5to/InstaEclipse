package ps.reso.instaeclipse.utils.core;

import java.util.Arrays;
import java.util.List;

public class CommonUtils {
    public static final String IG_PACKAGE_NAME = "com.instagram.android";
    public static final String MY_PACKAGE_NAME = "ps.reso.instaeclipse";

    public static final String[] MODDED_IG_PACKAGES = {
            "com.instagold.android",
            "com.instaflux.app",
            "com.myinsta.android",
            "cc.honista.app",
            "com.instaprime.android",
            "com.instafel.android",
            "com.instadm.android",
            "com.dfistagram.android",
            "com.Instander.android",
            "com.aero.instagram",
            "com.instapro.android",
            "com.instaflow.android",
            "com.instagram1.android",
            "com.instagram2.android",
            "com.instagramclone.android",
            "com.instaclone.android"
    };

    /*
    Dev Purposes
    public static final String USER_SESSION_CLASS = "com.instagram.common.session.UserSession";
    */

    /**
     * Checks if a given package name is the official Instagram package or a known modded version.
     *
     * @param packageName The package name to check.
     * @return true if the package name is a recognized Instagram version, false otherwise.
     */
    public static boolean isInstagramPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        if (packageName.equals(IG_PACKAGE_NAME)) {
            return true;
        }
        for (String moddedPackage : MODDED_IG_PACKAGES) {
            if (moddedPackage.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}