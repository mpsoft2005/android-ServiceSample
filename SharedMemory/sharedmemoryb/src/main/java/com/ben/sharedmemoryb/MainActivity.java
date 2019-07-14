package com.ben.sharedmemoryb;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.system.ErrnoException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ben.mylibrary.ICat;
import com.ben.mylibrary.ICatCallback;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private SharedMemory mSharedMemory;

    private Button bind_service_button;

    private ICatCallback.Stub mCatCallback = new ICatCallback.Stub() {
        @Override
        public void onChanged(String color, double weight) throws RemoteException {
            Log.i(TAG, "mCatCallback onChanged: " + weight);
        }
    };

    private ServiceConnection CatServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ICat catService = ICat.Stub.asInterface(service);
            try {
                catService.registerUpdates(mCatCallback);
                Log.i(TAG, "cat color=" + catService.getColor());
                Log.i(TAG, "cat weight=" + catService.getWeight());
                TestSharedMemory(catService.getSharedMemory());
            } catch (RemoteException e) {
                Log.i(TAG, "call cat service fail" + e);
            }
        }

        private void TestSharedMemory(SharedMemory shm) {
            try {
                Log.i(TAG, String.format("shared memory size=%d", shm.getSize()));

                ByteBuffer buffer = shm.mapReadOnly();
                Log.i(TAG, String.format("buffer.capacity=%d buffer.remaining=%d", buffer.capacity(), buffer.remaining()));

                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                Log.i(TAG, "data.length=" + data.length);
                for (int i  = 0; i < 128; i++) {
                    Log.i(TAG, String.format("  %d %d", i, buffer.get(i)));
                }
            } catch (ErrnoException e) {
                Log.e(TAG, "test shared memory");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bind_service_button = (Button)findViewById(R.id.bind_service_button);
        bind_service_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == bind_service_button) {
            Intent intent = new Intent();
            intent.setPackage("com.ben.sharedmemory");
            intent.setAction("com.ben.sharedmemory.CAT_SERVICE");
            bindService(intent, CatServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }
}
