package com.example.android.presentor.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Carlo on 19/11/2017.
 */

public class ConnectionUtility {
    public static int getPort(Context context) {
        int localPort = Utility.getInt(context, "localport");
        if (localPort < 0) {
            localPort = getNextFreePort();
            Utility.saveInt(context, "localport", localPort);
        }
        return localPort;
    }

    private static int getNextFreePort() {
        int localPort = -1;
        try {
            ServerSocket s = new ServerSocket(0);
            localPort = s.getLocalPort();

            //closing the port
            if (!s.isClosed()) {
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v("DXDXD", Build.MANUFACTURER + ": free port requested: " + localPort);

        return localPort;
    }

    public static void clearPort(Context context) {
        Utility.clearKey(context, "localport");
    }
}
