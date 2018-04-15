package com.example.android.presentor.networkservicediscovery;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.android.presentor.db.DatabaseUtility;
import com.example.android.presentor.screenshare.ShareService;

/**
 * Created by Carlo on 17/11/2017.
 */

public class NsdHelper {

    private Context mContext;

    private static NsdHelper ourInstance;

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;


    private static final String SERVICE_TYPE = "_http._tcp";
    //separator for service name and service host


    // There is an additional dot at the end of service name most probably by os, this is to
    // rectify that problem
    private static final String SERVICE_TYPE_PLUS_DOT = SERVICE_TYPE + ".";

    private static final String TAG = "NSDHelperDXDX: ";

    private String mServiceName;

    private NsdHelper() {

    }

    public static NsdHelper getInstance(){
        if (ourInstance == null) {
            ourInstance = new NsdHelper();
        }
        return ourInstance;
    }

    public void init(Context context){
        this.mContext = context.getApplicationContext();
        this.mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void releaseNsdHelper(){
        mContext = null;
        ourInstance = null;
    }

    public void setServiceName(String serviceName, String serviceCreator, String servicePassword) {
        this.mServiceName = servicePassword + "_" + serviceCreator + "_" + serviceName;
    }


    private void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                String serviceType = service.getServiceType();
                Log.d(TAG, "Service discovery success: " + service.getServiceName());
                // For some reason the service type received has an extra dot with it, hence
                // handling that case
                boolean isOurService = serviceType.equals(SERVICE_TYPE) || serviceType.equals
                        (SERVICE_TYPE_PLUS_DOT);
                if (!isOurService) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                    return;
                }
                if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                    return;
                }
                mNsdManager.resolveService(service, new MyResolveListener());
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                DatabaseUtility.removeServiceToList(mContext, service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }


    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service registered: " + NsdServiceInfo);
                Toast.makeText(mContext, "Service Registered", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.d(TAG, "Service registration failed: " + arg1);
                Toast.makeText(mContext, "Service Failed", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG, "Service unregistered: " + arg0.getServiceName());
                Toast.makeText(mContext, "Service Unregistered", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "Service unregistration failed: " + errorCode);
                Toast.makeText(mContext, "Service Unregistration failed", Toast.LENGTH_LONG).show();
            }
        };
    }


    public void registerService(int port) {
//        broadcaster = LocalBroadcastManager.getInstance(mContext);
        stopRegisterService();  // Cancel any previous registration request
        initializeRegistrationListener();
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        Log.v(TAG, Build.MANUFACTURER + " registering service: " + port);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void discoverServices() {
        //stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mDiscoveryListener = null;
        }
    }

    public void stopRegisterService() {
        if (mRegistrationListener != null) {
            try {
                Log.d("NsdHelper", "Service unregister");
                mNsdManager.unregisterService(mRegistrationListener);
            } catch(Exception e){
                e.printStackTrace();
            }
            mRegistrationListener = null;
        }
    }


    private class MyResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.v(TAG, "Resolve Succeeded. " + serviceInfo);
            if (serviceInfo.getServiceName().equals(mServiceName)) {
                Log.d(TAG, "Same IP.");
                return;
            }
            DatabaseUtility.addServiceToList(mContext, serviceInfo);
        }

        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            Log.d(TAG, "Service registration failed: " + i);
            //Toast.makeText(mContext, "Service Failed", Toast.LENGTH_LONG).show();
            mNsdManager.resolveService(nsdServiceInfo, new MyResolveListener());
        }
    }
}
