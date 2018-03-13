package com.example.android.presentor.screenshare;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.DatabaseUtility;
import com.example.android.presentor.faceanalysis.FaceAnalysisActivator;
import com.example.android.presentor.faceanalysis.FaceAnalyzer;
import com.example.android.presentor.screenpinning.ScreenPinningObservable;
import com.example.android.presentor.utils.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by Carlo on 18/11/2017.
 */

public class ShareService {

    private Context mContext;

    private static ShareService ourInstance;

    private ServerThread mServer;
    private ClientThread mClient;

    //for ShareService initialization
    private ShareService() {
        //mHandler = handler;
    }

    public static ShareService getInstance() {
        if (ourInstance == null) {
            ourInstance = new ShareService();
        }
        return ourInstance;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void stop() {
        mServer = null;
        mClient = null;
        ourInstance = null;
    }

    //for server
    public ServerThread startServer(int port) throws IOException {
        mServer = new ServerThread(port);
        return mServer;
    }

    public ServerThread getServer() {
        return mServer;
    }

    public void stopServer() {
        Log.e("ShareService", "stopServer() callback");
        if (mServer != null) {
            mServer.stopServer();
            mServer = null;
        }
    }

    public boolean isServerOpen() {
        return mServer != null && mServer.getStatus();
    }

    //for client methods

    public void connectClient(Context activityContext, String ip, int port) {
        mClient = new ClientThread(activityContext, ip, port);
    }

    public void disconnectClient() {
        mClient.disconnectClient();
        mClient = null;
    }


    public class ServerThread extends Thread {

        private ServerSocket mServerSocket;
        private final Hashtable<Socket, DataOutputStream> mOutputStreamsHashtable = new Hashtable<>();
        private LocalBroadcastManager mBroadCastManager;

        private int mPort;

        private boolean isServerOpen = false;
        private boolean isPause = false;
        private boolean isOnScreenPinningMode = false;
        private boolean isOnFaceAnalysisMode = false;


        private Enumeration getSockets() {
            return mOutputStreamsHashtable.keys();
        }

        public void stopServer() {
            if (isOnFaceAnalysisMode)
                executeSendCommandToAll(ScreenShareConstants.FACE_ANALYSIS_OFF);
            if (isOnScreenPinningMode) executeSendCommandToAll(ScreenShareConstants.SCREEN_PIN_ON);
            executeSendCommandToAll(ScreenShareConstants.ON_STOP);
        }

        public boolean getStatus() {
            return this.isServerOpen;
        }

        public void setStatus(boolean isOn) {
            this.isServerOpen = isOn;
            if (!isOn) {
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean hasClients() {
            return !mOutputStreamsHashtable.isEmpty();
        }

        public void pauseScreenMirroring() {
            isPause = true;
            executeSendCommandToAll(ScreenShareConstants.ON_PAUSE);
        }

        public void resumeScreenMirroring() {
            isPause = false;
            executeSendCommandToAll(ScreenShareConstants.ON_RESUME);
        }

        public boolean isPausedScreenMirroring() {
            return isPause;
        }

        public void setScreenPinningMode(boolean isOn) {
            this.isOnScreenPinningMode = isOn;
            if (isOn) executeSendCommandToAll(ScreenShareConstants.SCREEN_PIN_ON);
            else executeSendCommandToAll(ScreenShareConstants.SCREEN_PIN_OFF);
        }

        public boolean getScreenPinningModeServer() {
            return this.isOnScreenPinningMode;
        }

        public void setFaceAnalysisMode(boolean isOn) {
            this.isOnFaceAnalysisMode = isOn;
            if (isOn) executeSendCommandToAll(ScreenShareConstants.FACE_ANALYSIS_ON);
            else executeSendCommandToAll(ScreenShareConstants.FACE_ANALYSIS_OFF);
        }

        public boolean getFaceAnalysisMode() {
            return this.isOnFaceAnalysisMode;
        }

        public void sendToAll(byte[] buffer) {
            synchronized (mOutputStreamsHashtable) {
                for (Enumeration e = getSockets(); e.hasMoreElements(); ) {
                    Socket s = (Socket) e.nextElement();
                    DataOutputStream dout = mOutputStreamsHashtable.get(s);
                    //send the message
                    try {
                        dout.writeInt(buffer.length);
                        dout.write(buffer, 0, buffer.length);
                    } catch (IOException ie) {
                        removeConnection(s);
                    }
                }
            }
        }

        private void sendCommand(DataOutputStream dout, int command) {
            try {
                dout.writeInt(command);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void sendCommandToAll(int commandCode) {
            synchronized (mOutputStreamsHashtable) {
                for (Enumeration e = getSockets(); e.hasMoreElements(); ) {
                    Socket s = (Socket) e.nextElement();
                    DataOutputStream dout = mOutputStreamsHashtable.get(s);
                    //send the message
                    try {
                        dout.writeInt(commandCode);
                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }
            }
        }

        public void executeSendCommandToAll(final int commandCode) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendCommandToAll(commandCode);
                    if (commandCode == ScreenShareConstants.ON_STOP) {
                        setStatus(false);
                    }
                }
            }).start();
        }

        private void sendServerState(DataOutputStream dout) {
            if (isPause) {
                sendCommand(dout, ScreenShareConstants.ON_PAUSE);
            }
            if (isOnFaceAnalysisMode) {
                sendCommand(dout, ScreenShareConstants.FACE_ANALYSIS_ON);
            }
            if (isOnScreenPinningMode) {
                sendCommand(dout, ScreenShareConstants.SCREEN_PIN_ON);
            }
        }

        private void removeConnection(Socket s) {
            synchronized (mOutputStreamsHashtable) {
                mOutputStreamsHashtable.remove(s);

                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DatabaseUtility.removeDeviceToList(mContext, s.getInetAddress().getHostAddress(), s.getPort());
                mBroadCastManager.sendBroadcast(new Intent(ScreenShareConstants.BROADCAST_DEVICE_COUNT_CHANGED));
            }
        }

        private void listen(int port) throws IOException {

            mServerSocket = new ServerSocket(port);

            while (isServerOpen) {
                //blocking statement
                Socket clientSocket = mServerSocket.accept();

                //somebody is connected
                //get outputstream so we can write to the client;
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                sendServerState(dataOutputStream);

                //to get the client's name
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                mOutputStreamsHashtable.put(clientSocket, dataOutputStream);

                //Add to connected device
                DatabaseUtility.addDeviceToList(mContext, dataInputStream.readUTF(),
                        clientSocket.getInetAddress().getHostAddress(),
                        clientSocket.getPort());

                mBroadCastManager.sendBroadcast(new Intent(ScreenShareConstants.BROADCAST_DEVICE_COUNT_CHANGED));
                Log.e("Looper", "End of loop reach");
            }
        }

        public ServerThread(int port) {
            this.mBroadCastManager = LocalBroadcastManager.getInstance(mContext);
            this.mPort = port;
            setStatus(true);
            start();
        }


        @Override
        public void run() {
            try {
                Log.d("ShareService", "server start listening for connection");
                listen(mPort);
            } catch (IOException e) {
                Log.d("ShareService", "Server Stop");
                mServerSocket = null;
                mOutputStreamsHashtable.clear();
                e.printStackTrace();
            }
        }

    }

    public class ClientThread extends Thread implements ScreenPinningObservable.ScreenPinningObserver,
            FaceAnalysisActivator.FaceAnalysisObserver {

        private Context activityContext;
        private TextView mTextView;
        private ImageView mImageView;
        private Handler mHandler;

        private Socket mSocket;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private DataOutputStream mDataOutputStream;
        private DataInputStream mDataInputStream;

        private ScreenPinningObservable screenPinningObservable;
        private FaceAnalysisActivator faceAnalysisActivator;
        private FaceAnalyzer faceAnalyzer;

        private CountDownTimer cdt;

        private boolean screenPinningRequest = false;
        private boolean screenPinningEnabled = false;
        private boolean isConnected = false;

        //Temporary
        private String clientName = "Kaboomskie";

        public void disconnectClient() {
            this.isConnected = false;
        }

        //create a client to connect to server
        public ClientThread(Context context, String ip, int port) {
            try {
                activityContext = context;
                mTextView = ((Activity) activityContext).findViewById(R.id.screen_pin_tv);
                mImageView = ((Activity) activityContext).findViewById(R.id.image_view_screen_share);
                mHandler = new Handler(Looper.getMainLooper());
                mSocket = new Socket(ip, port);
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
                mDataInputStream = new DataInputStream(mInputStream);
                mDataOutputStream = new DataOutputStream(mOutputStream);
                isConnected = true;
                mDataOutputStream.writeUTF(clientName);
                initFaceAnalysisObserver();
                start();
            } catch (IOException e) {
                e.printStackTrace();
                Utility.showToast(activityContext, "Error connecting to the server.");
            }
        }

        private void disconnect() throws IOException {
            mInputStream.close();
            mInputStream = null;
            mOutputStream.close();
            mOutputStream = null;
            mDataInputStream.close();
            mDataInputStream = null;
            mSocket.close();
            mSocket = null;
            if (cdt != null) {
                cdt.cancel();
            }
            faceAnalyzer.release();
            faceAnalyzer = null;
            ((Activity) activityContext).finish();
        }

        private void initScreenPinningObservable() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setBackgroundColor(activityContext.getResources().getColor(R.color.colorRed));
                    mTextView.setVisibility(View.VISIBLE);
                }
            });
            if (!isInLockTaskMode()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cdt = new CountDownTimer(10000, 1000) {
                            @Override
                            public void onTick(long l) {
                                //TODO: Add string literals on strings.xml, use place holders.
                                mTextView.setText("Please Enable Screen Pinning. Disconnecting in " + l / 1000 + " seconds.");
                            }

                            @Override
                            public void onFinish() {
                                Log.d("CountDownTimer", "onFinish() callBack");
                                isConnected = false;
                                screenPinningRequest = false;
                                mTextView.setVisibility(View.GONE);
                            }
                        }.start();
                    }
                });
            }
            screenPinningObservable = new ScreenPinningObservable() {
                @Override
                public void run() {
                    while (screenPinningRequest) {
                        screenPinningObservable.setState(isInLockTaskMode());
                    }
                    Log.e("ScreenPinningListener", "Listener stopServer");
                }
            };

            screenPinningObservable.setScreenPinningObserver(this);
        }

        private void initFaceAnalysisObserver() {
            faceAnalysisActivator = new FaceAnalysisActivator();
            faceAnalysisActivator.setFaceAnalysisObserver(this);
        }

        private boolean isInLockTaskMode() {
            ActivityManager am = (ActivityManager) activityContext.getSystemService(Context.ACTIVITY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For SDK version 23 and above.
                return am != null && am.getLockTaskModeState()
                        != ActivityManager.LOCK_TASK_MODE_NONE;
            }

            // When SDK version >= 21. This API is deprecated in 23.
            return am != null && am.isInLockTaskMode() &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        }

        private void screenPinOn() {
            //start screen pinning
            initScreenPinningObservable();
            screenPinningObservable.start();
            screenPinningRequest = true;
            Log.d("ShareService", "ScreenPinning is on");
            if (!isInLockTaskMode()) {
                ((Activity) activityContext).startLockTask();
            }
        }

        private void screenPinOff() {
            Log.d("ShareService", "ScreenPinning is off");
            screenPinningRequest = false;   //to stopServer the listener
            if (cdt != null) {
                cdt.cancel();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //TODO: add string literals to string.xml
                    mTextView.setText("Screen Pinning has been disabled by the host.");
                    mTextView.setBackgroundColor(activityContext.getResources().getColor(R.color.colorGreen));
                    mTextView.setVisibility(View.VISIBLE);
                }
            });


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTextView.setVisibility(View.GONE);
                    screenPinningEnabled = false;
                }
            }, 1000);
            ((Activity) activityContext).stopLockTask();

        }

        private void handleCommandReceived(int COMMAND_CODE) {
            Log.d("ShareService", "Command has been received: " + COMMAND_CODE);
            switch (COMMAND_CODE) {
                case ScreenShareConstants.SCREEN_PIN_ON:
                    screenPinOn();
                    break;
                case ScreenShareConstants.SCREEN_PIN_OFF:
                    screenPinOff();
                    break;
                case ScreenShareConstants.FACE_ANALYSIS_ON:
                    faceAnalysisActivator.setState(true);
                    break;
                case ScreenShareConstants.FACE_ANALYSIS_OFF:
                    faceAnalysisActivator.setState(false);
                    break;
                case ScreenShareConstants.ON_RESUME:
                    break;
                case ScreenShareConstants.ON_PAUSE:
                    break;
                case ScreenShareConstants.ON_STOP:
                    break;
                default:
                    break;
            }
        }

        private void listen() {
            try {
                while (isConnected) {
                    final byte[] bytes;
                    int len = mDataInputStream.readInt();
                    Log.d("ShareService", "value of len:" + len);
                    if (len > 0) {
                        bytes = new byte[len];
                        mDataInputStream.readFully(bytes, 0, bytes.length);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                mImageView.setImageBitmap(bm);
                            }
                        });
                    } else {    //it's a command so let this block handle it.
                        handleCommandReceived(len);
                    }
                }
                Log.d("Share Service", "Client Disconnects");
                disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //keeps listening to the inputstream while connected
        @Override
        public void run() {
            //update UI for the stream
            Log.d("ShareService", "client side started");
            listen();
        }

        //---------------------------FaceAnalysisObserver implementation-----------------------------//

        @Override
        public void onFaceAnalysisRequestStateChanged(boolean isOn) {
            if (isOn) {
                Log.e("FaceAnalysis", "Status On");
                faceAnalyzer = new FaceAnalyzer(activityContext);
                faceAnalyzer.createCameraSource();
                faceAnalyzer.start();
            } else {
                Log.e("FaceAnalysis", "Status Off");
                faceAnalyzer.release();
                faceAnalyzer = null;
            }
        }

        //---------------------------ScreenPinningObserver implementation-----------------------------//

        @Override
        public void onStateChanged(boolean inLockTaskMode) {
            if (!inLockTaskMode && screenPinningEnabled) {
                if (cdt != null) {
                    cdt.cancel();
                }
                screenPinningEnabled = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //TODO: Add string literals on strings.xml
                        mTextView.setVisibility(View.VISIBLE);
                        mTextView.setText("Screen Pinning is cancelled. Disconnecting...");
                        mTextView.setBackgroundColor(activityContext.getResources().getColor(R.color.colorRed));
                    }
                });
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("ScreenPinningListener", "clientDisconnects callback disconnect");
                        isConnected = false;
                        screenPinningRequest = false;
                    }
                }, 1000);
            } else if (inLockTaskMode && !screenPinningEnabled) {
                screenPinningEnabled = true;
                if (cdt != null) {
                    cdt.cancel();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //TODO: Add string literals on strings.xml.
                        mTextView.setText("Screen Pinning is Enabled.");
                        mTextView.setBackgroundColor(activityContext.getResources().getColor(R.color.colorGreen));
                    }
                });
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setVisibility(View.GONE);
                    }
                }, 1000);
            }
        }
    }


}
