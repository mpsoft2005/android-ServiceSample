// ICat.aidl
package com.ben.mylibrary;

import com.ben.mylibrary.ICatCallback;

// Declare any non-default types here with import statements

interface ICat {

    String getColor();

    double getWeight();

    void registerUpdates(ICatCallback callback);

    SharedMemory getSharedMemory();
}
