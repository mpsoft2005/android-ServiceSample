package com.ben.sharedmemory;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.system.ErrnoException;
import android.util.Log;

import com.ben.mylibrary.ICat;
import com.ben.mylibrary.ICatCallback;

import java.nio.ByteBuffer;

public class CatService extends Service {

    private final String TAG = "CatService";

    private CatBinder catBinder;
    private String color;
    private double weight;

    private SharedMemory mSharedMemory;
    private Handler mTimerHandler = new Handler();

    private Runnable CatRunable = new Runnable() {
        @Override
        public void run() {
            weight += 1;
            Log.i(TAG, "weight changed: " + weight);
            catBinder.Callback(color, weight);
            mTimerHandler.postDelayed(CatRunable, 1000);
        }
    };

    public CatService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        catBinder = new CatBinder();
        color = "RED";
        weight = 1.789;
        mTimerHandler.postDelayed(CatRunable, 1000);

        try {
            int sharedMemorySize = 4 * 1024 * 1024;
            mSharedMemory = SharedMemory.create("TestSharedMemory", sharedMemorySize);
            ByteBuffer buffer = mSharedMemory.mapReadWrite();

            for (int i = 0; i < 256; i++) {
                buffer.put((byte)i);
            }

        } catch (ErrnoException e) {
            Log.e(TAG, "create shared memory fail:" + e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        /**
         *  返回 CatBinder 对象
         *    在绑定本地 Service 情况下
         *      该 catBinder 会直接传给客户端的 ServiceConnected 对象的 ServiceConnected()
         *    在绑定远程 Service 情况下
         *      将 catBinder 对象的代理传给客户端的 ServiceConnected 对象的 ServiceConnected()
         */
        return catBinder;
    }

    public class CatBinder extends ICat.Stub {

        ICatCallback mCatCallback;

        @Override
        public String getColor() throws RemoteException {
            return color;
        }

        @Override
        public double getWeight() throws RemoteException {
            return weight;
        }

        @Override
        public void registerUpdates(ICatCallback callback) throws RemoteException {
            mCatCallback = callback;
        }

        public void Callback(String color, double weight) {
            try {
                if (mCatCallback != null) {
                    mCatCallback.onChanged(color, weight);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "CatCallback fail:" + e);
            }
        }

        @Override
        public SharedMemory getSharedMemory() throws RemoteException {
            return mSharedMemory;
        }
    }
}
