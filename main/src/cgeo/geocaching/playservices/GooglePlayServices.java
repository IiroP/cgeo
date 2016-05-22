package cgeo.geocaching.playservices;

import cgeo.geocaching.CgeoApplication;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.content.Context;

public class GooglePlayServices {

    private static boolean isGooglePlayServicesAvailable = false;
    private static boolean initialized = false;

    /**
     * Check if Google Play services is available on this device
     */
    public static boolean isAvailable() {
        if (!initialized) {
            initialize();
        }
        return isGooglePlayServicesAvailable;
    }

    /**
     * cache the result of querying for play services
     */
    private static void initialize() {
        final Context context = CgeoApplication.getInstance();
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            isGooglePlayServicesAvailable = true;
        }
    }
}
