package com.example.android.presentor.screenshare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.example.android.presentor.utils.ConnectionUtility;
import com.example.android.presentor.utils.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Carlo on 18/11/2017.
 */

public class ShareService {

    //private Handler mHandler;
    private Server mServer;
    private Client mClient;

    public boolean isServerOpen = false;
    public boolean hasClients = false;

    public ShareService() {
        //mHandler = handler;
    }

    public void start(int port) throws IOException{
        isServerOpen = true;
        mServer = new Server(port);
    }

    public void stop(){
        isServerOpen = false;
        hasClients = false;
    }

    public void connect(String ip, int port, Handler handler, ImageView imageView){
        mClient = new Client(ip, port, handler, imageView);
    }

    public void send(byte[] buffer){
        Client client;
        synchronized (this) {
            client = mClient;
        }
        client.send(buffer);
    }

//    public void sendToClient(String message){
//        mClient.sendToClient(message);
//    }

    public class Server extends Thread{

        private ServerSocket mServerSocket;
        int mPort;

        public Server(int port) throws  IOException {
            mPort = port;
            start();
        }

        private void listen(int port) throws IOException{

            mServerSocket = new ServerSocket(port);

            while(isServerOpen){
                //blocking statement
                Socket socket = mServerSocket.accept();
                hasClients = true;
                Log.d("ShareService listen", "address: " + socket.getInetAddress() + ", port: " + socket.getPort() );
                Log.d("ShareService listen", "somebody is connected");
                //somebody is connected
                mClient = new Client(socket);
            }

            Log.d("ShareService", "Server Stop");
            mServerSocket.close();
            mServerSocket = null;
        }

        @Override
        public void run() {
            try {
                Log.d("ShareService", "server start listening for connection");
                listen(mPort);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public class Client extends Thread{

        Handler updateUiHandler;
        ImageView mImageView;

        private Socket mSocket;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private DataOutputStream mDataOutputStream;
        private DataInputStream mDataInputStream;

        long bitmapReceive;

        //the client that is received by the server
        //runs on server
        public Client (Socket socket){
            try {
                mSocket = socket;
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        //create a client to connect to server
        //runs on client
        public Client(String ip, int port, Handler handler, ImageView imageView){
            try {
                updateUiHandler = handler;
                mImageView = imageView;
                mSocket = new Socket(ip, port);
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
                mDataInputStream = new DataInputStream(mInputStream);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //keeps listening to the inputstream while connected
        @Override
        public void run() {
            //update UI for the stream
            Log.d("ShareService", "client side started");
            try {
                while(true){
                    byte[] bytes;
                    int len = mDataInputStream.readInt();
                    bytes = new byte[len];
                    if(len > 0){
                        mDataInputStream.readFully(bytes, 0, bytes.length);
                    }
                    updateUiHandler.post(new updateUiThread(bytes, mImageView));
                    bitmapReceive++;
                    Log.d("Share Service", "Bitmap received: " + bitmapReceive);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //the host sending screenshot
        public void send(byte[] buffer){
            try {
                mDataOutputStream = new DataOutputStream(mOutputStream);
                mDataOutputStream.writeInt(buffer.length);
                mDataOutputStream.write(buffer, 0, buffer.length);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

//        public void sendToClient(String message){
//            try{
//                mDataOutputStream.writeUTF(message);
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }

    }

    class updateUiThread implements Runnable{
        private byte[] byteArray;
        private ImageView mImageView;
        public updateUiThread(byte[] bytes, ImageView imageView){
            byteArray = bytes;
            mImageView = imageView;
        }

        @Override
        public void run() {
            Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            mImageView.setImageBitmap(bm);
        }
    }


}
