package com.example.android.presentor.screenshare;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.DatabaseUtility;
import com.example.android.presentor.faceanalysis.FaceAnalysisActivator;
import com.example.android.presentor.faceanalysis.FaceAnalyzer;
import com.example.android.presentor.screenpinning.ScreenPinningObservable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Carlo on 18/11/2017.
 */

public class ShareService {

    private Context mContext;

    public static final int SCREEN_PIN_ON = -1;
    public static final int SCREEN_PIN_OFF = -2;
    public static final int FACE_ANALYSIS_ON = -3;
    public static final int FACE_ANALYSIS_OFF = -4;


    //private Handler mHandler;
    private final static ShareService ourInstance = new ShareService();

    private Server mServer;

    //for server variables
    private boolean isServerOpen = false;
    private boolean isPause = false;
    private boolean isOnScreenPinningModeServer = false;
    private boolean isOnFaceAnalysisModeServer = false;

    //for client variables
    private boolean isClientConnected = false;


    //for ShareService initialization
    private ShareService() {
        //mHandler = handler;
    }

    public static ShareService getInstance() {
        return ourInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    //for server methods
    public void startServer(Activity act, int port) throws IOException {
        setServerStatus(true);
        mServer = new Server(act, port);
    }

    public void stopServer() {
        setServerStatus(false);
        mServer = null;
    }

    public void setServerActivity(Activity act){
        mServer.serverActivity = act;
    }

    public void setScreenPinningModeServer(boolean b) {
        this.isOnScreenPinningModeServer = b;
    }

    public boolean getScreenPinningModeServer() {
        return this.isOnScreenPinningModeServer;
    }

    public boolean getServerStatus() {
        return isServerOpen;
    }

    public void setServerStatus(boolean status) {
        this.isServerOpen = status;
    }

    public boolean serverHasClients() {
        //returns false if outputstreamshashtable is null;
        //outputstreamhashtable is null if service is onstop;
        boolean b = false;
        if (mServer != null) {
            b = !mServer.mOutputStreamsHashtable.isEmpty();
        }

        return b;
    }

    public void pauseScreenMirroringServer() {
        isPause = true;
    }

    public void resumeScreenMirroringServer() {
        isPause = false;
    }

    public boolean onPauseScreenMirroringServer() {
        return isPause;
    }

    public void sendToAllClientsConnected(byte[] buffer) {
        mServer.sendToAll(buffer);
    }

    public void sendCommandToAllClientsConnected(int commandCode) {
        mServer.sendCommandToAll(commandCode);
    }

    //for client methods
    public void setClientStatus(boolean status) {
        this.isClientConnected = status;
    }

    public void connectClient(Activity act, String ip, int port) {
        setClientStatus(true);
        new Client(act, ip, port);
    }

    public void disconnectClient() {
        setClientStatus(false);
    }

    public class Server extends Thread {

        private Activity serverActivity;

        private ServerSocket mServerSocket;
        private Hashtable mOutputStreamsHashtable;

        private int mPort;

        public Server(Activity act, int port) throws IOException {
            serverActivity = act;
            mPort = port;
            start();
        }

        private Enumeration getSockets() {
            return mOutputStreamsHashtable.keys();
        }

        private void sendToAll(byte[] buffer) {
            synchronized (mOutputStreamsHashtable) {
                for (Enumeration e = getSockets(); e.hasMoreElements(); ) {
                    Socket s = (Socket) e.nextElement();
                    DataOutputStream dout = (DataOutputStream) mOutputStreamsHashtable.get(s);
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

        private void sendCommand(int command, DataOutputStream dout) {
            try {
                dout.writeInt(command);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void sendCommandToAll(int commandCode) {
            Log.d("ShareService", "sendCommandToAllClientsConnected() called");
            synchronized (mOutputStreamsHashtable) {
                for (Enumeration e = getSockets(); e.hasMoreElements(); ) {
                    Socket s = (Socket) e.nextElement();
                    DataOutputStream dout = (DataOutputStream) mOutputStreamsHashtable.get(s);
                    //send the message
                    try {
                        dout.writeInt(commandCode);
                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }
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
                DatabaseUtility.removeDeviceToList(mContext, serverActivity,
                        s.getInetAddress().getHostAddress(), s.getPort());
            }
        }

        private void listen(int port) throws IOException {

            mServerSocket = new ServerSocket(port);
            mOutputStreamsHashtable = new Hashtable();

            while (isServerOpen) {
                //blocking statement
                Socket clientSocket = mServerSocket.accept();
                //to get the client's data
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                Log.d("ShareService listen", "address: " + clientSocket.getInetAddress() + ", port: " + clientSocket.getPort());
                Log.d("ShareService listen", "somebody is connected");
                //mClient = new Client(clientSocket);

                //somebody is connected
                //get outputstream so we can write to the client;

                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                if (isOnScreenPinningModeServer) {
                    sendCommand(SCREEN_PIN_ON, dataOutputStream);
                }
                mOutputStreamsHashtable.put(clientSocket, dataOutputStream);

                //Add to connected device
                DatabaseUtility.addDeviceToList(mContext, serverActivity,
                        dataInputStream.readUTF(), clientSocket.getInetAddress().getHostAddress(),
                        clientSocket.getPort());

            }
            Log.d("ShareService", "Server Stop");
            mServerSocket.close();
            mServerSocket = null;
            mOutputStreamsHashtable.clear();
            mOutputStreamsHashtable = null;
            isOnScreenPinningModeServer = false;
        }

        @Override
        public void run() {
            try {
                Log.d("ShareService", "server start listening for connection");
                listen(mPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class Client extends Thread {

        private Activity clientActivity;
        private TextView tv;

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

        private long bitmapReceive;
        private String clientName = "Kaboomskie";

        //create a client to connect to server
        private Client(Activity act, String ip, int port) {
            try {
                clientActivity = act;
                tv = (TextView) clientActivity.findViewById(R.id.screen_pin_tv);
                mSocket = new Socket(ip, port);
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
                mDataInputStream = new DataInputStream(mInputStream);
                mDataOutputStream = new DataOutputStream(mOutputStream);
                isClientConnected = true;
                mDataOutputStream.writeUTF(clientName);
                initFaceAnalysisObserver();
                start();
            } catch (IOException e) {
                e.printStackTrace();
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
            clientActivity.finish();
            faceAnalyzer.release();
        }

        private void initScreenPinningObservable() {
            clientActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setBackgroundColor(mContext.getResources().getColor(R.color.colorRed));
                    tv.setVisibility(View.VISIBLE);
                }
            });

            if (!isInLockTaskMode()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        cdt = new CountDownTimer(10000, 1000) {
                            @Override
                            public void onTick(long l) {
                                tv.setText("Please Enable Screen Pinning. Disconnecting in " + l / 1000 + " seconds.");
                            }

                            @Override
                            public void onFinish() {
                                Log.d("CountDownTimer", "onFinish() callBack");
                                isClientConnected = false;
                                screenPinningRequest = false;
                                tv.setVisibility(View.GONE);
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

            screenPinningObservable.setScreenPinningObserver(new ScreenPinningObservable.ScreenPinningObserver() {
                @Override
                public void onStateChanged(boolean inLockTaskMode) {
                    if (!inLockTaskMode && screenPinningEnabled) {
                        if (cdt != null) {
                            cdt.cancel();
                        }
                        screenPinningEnabled = false;
                        clientActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setVisibility(View.VISIBLE);
                                tv.setText("Screen Pinning is cancelled. Disconnecting...");
                                tv.setBackgroundColor(mContext.getResources().getColor(R.color.colorRed));
                            }
                        });
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.e("ScreenPinningListener", "clientDisconnects callback disconnect");
                                isClientConnected = false;
                                screenPinningRequest = false;
                            }
                        }, 1000);
                    } else if (inLockTaskMode && !screenPinningEnabled) {
                        screenPinningEnabled = true;
                        if (cdt != null) {
                            cdt.cancel();
                        }
                        clientActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText("Screen Pinning is Enabled.");
                                tv.setBackgroundColor(mContext.getResources().getColor(R.color.colorGreen));
                            }
                        });
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                clientActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }, 1000);
                    }
                }
            });
        }

        private void initFaceAnalysisObserver(){
            faceAnalysisActivator = new FaceAnalysisActivator();
            faceAnalyzer = new FaceAnalyzer(mContext);
            faceAnalyzer.createCameraSource();
            faceAnalysisActivator.setFaceAnalysisObserver(new FaceAnalysisActivator.FaceAnalysisObserver() {
                @Override
                public void onFaceAnalysisRequestStateChanged(boolean isOn) {
                    if(isOn){
                        Log.e("FaceAnalysis", "Status On");
                        faceAnalyzer.start();
                    }else{
                        Log.e("FaceAnalysis", "Status Off");
                        faceAnalyzer.stop();
                    }
                }
            });
        }

        private boolean isInLockTaskMode() {
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For SDK version 23 and above.
                return am.getLockTaskModeState()
                        != ActivityManager.LOCK_TASK_MODE_NONE;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // When SDK version >= 21. This API is deprecated in 23.
                return am.isInLockTaskMode();
            }
            return false;
        }

        private void screenPinOn() {
            //start screen pinning
            initScreenPinningObservable();
            screenPinningObservable.start();
            screenPinningRequest = true;
            Log.d("ShareService", "ScreenPinning is on");
            if (!isInLockTaskMode()) {
                clientActivity.startLockTask();
            }
        }

        private void screenPinOff() {
            Log.d("ShareService", "ScreenPinning is off");
            screenPinningRequest = false;   //to stopServer the listener
            if (cdt != null) {
                cdt.cancel();
            }
            clientActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setText("Screen Pinning has been disabled by the host.");
                    tv.setBackgroundColor(mContext.getResources().getColor(R.color.colorGreen));
                    tv.setVisibility(View.VISIBLE);
                }
            });
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    clientActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setVisibility(View.GONE);
                            screenPinningEnabled = false;
                        }
                    });
                }
            }, 1000);
            clientActivity.stopLockTask();
        }

        private void handleCommandReceived(int COMMAND_CODE) {
            Log.d("ShareService", "Command has been received: " + COMMAND_CODE);
            switch (COMMAND_CODE) {
                case SCREEN_PIN_ON:
                    screenPinOn();
                    break;
                case SCREEN_PIN_OFF:
                    screenPinOff();
                    break;
                case FACE_ANALYSIS_ON:
                    faceAnalysisActivator.setState(true);
                    break;
                case FACE_ANALYSIS_OFF:
                    faceAnalysisActivator.setState(false);
                    break;
                default:
                    break;
            }
        }

        private void listen() {
            try {
                while (isClientConnected) {
                    final byte[] bytes;
                    int len = mDataInputStream.readInt();
                    Log.d("ShareService", "value of len:" + len);
                    if (len > 0) {
                        bytes = new byte[len];
                        mDataInputStream.readFully(bytes, 0, bytes.length);
                        clientActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageView iv = clientActivity.findViewById(R.id.image_view_screen_share);
                                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                iv.setImageBitmap(bm);
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
    }


}
