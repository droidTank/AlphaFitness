// IStepCounterInterface.aidl
package com.example.swapn.alphafitness;

// Declare any non-default types here with import statements

interface IStepCounterInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    int getCounter();
    double getDistance();
    void setRecording(boolean value);
    boolean getRecording();
}
