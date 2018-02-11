package com.example.android.presentor.networkservicediscovery;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.presentor.R;
import com.example.android.presentor.db.DatabaseUtility;
import com.example.android.presentor.db.ServicesDbHelper;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * Created by Carlo on 17/11/2017.
 */

public class NsdHelper {

    private Context mContext;

//    private LocalBroadcastManager broadcaster;

    private static final NsdHelper ourInstance = new NsdHelper();

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;


    public static final String SERVICE_TYPE = "_http._tcp";
    public static final String BROADCAST_REGISTER_SUCCESS = "RegisterBroadcast";
    public static final String BROADCAST_UNREGISTER_SUCCESS = "UnregisterBroadcast";
    //separator for service name and service host
    public static final String UNDERSCORE = "_";

    // There is an additional dot at the end of service name most probably by os, this is to
    // rectify that problem
    public static final String SERVICE_TYPE_PLUS_DOT = SERVICE_TYPE + ".";

    public static final String TAG = "NSDHelperDXDX: ";



    ServicesDbHelper mServicesDbHelper;
    public String mServiceName;

    NsdServiceInfo mService;

    private NsdHelper() {
//        mContext = context;
//        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public static NsdHelper getInstatnce(){
        return  ourInstance;
    }

    public void init(Context context){
        this.mContext = context.getApplicationContext();
        this.mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initClientSide(){
        mServicesDbHelper = new ServicesDbHelper(mContext);
        initializeResolveListener();
        //discoverServices();
    }



    public void setServiceName(String serviceName, String serviceCreator, String servicePassword) {
        this.mServiceName = serviceName + UNDERSCORE + serviceCreator + UNDERSCORE + servicePassword;
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
                if(service.getServiceName().equals(mServiceName)){
                    Log.d(TAG, "Same machine: " + mServiceName);
                    return;
                }
                mNsdManager.resolveService(service, mResolveListener);
//                else if (service.getServiceName().equals(mServiceName)) {
//                    Log.d(TAG, "Same machine: " + mServiceName);
//                } else if (service.getServiceName().contains(mServiceName)) {
//                    Log.d(TAG, "different machines. (" + service.getServiceName() + "-" +
//                            mServiceName + ")");
//                    mNsdManager.resolveService(service, mResolveListener);
//                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                DatabaseUtility.removeServiceToList(mContext, mService);
                if (mService == service) {
                    mService = null;
                }
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

    private void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.v(TAG, "Resolve Succeeded. " + serviceInfo);
                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                DatabaseUtility.addServiceToList(mContext, mService);
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service registered: " + NsdServiceInfo);
                Toast.makeText(mContext, "Service Registered", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(BROADCAST_REGISTER_SUCCESS);
//                broadcaster.sendBroadcast(intent);
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
//                Intent intent = new Intent(BROADCAST_UNREGISTER_SUCCESS);
//                broadcaster.sendBroadcast(intent);
//                //DatabaseUtility.clearConnectedDevice(mContext);
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
            } catch (Exception e){
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
            } finally {
            }
            mRegistrationListener = null;
        }
    }

}
