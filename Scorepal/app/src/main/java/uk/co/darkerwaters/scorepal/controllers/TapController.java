package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TapController extends Controller {

    private static final int K_NOEACHVALUES = 3;
    private static final int K_NOTOTALVALUES = 6;
    private static final long K_PROCESSING_PERIOD = 100L;

    private static final int K_SILENCELEADIN = 0;
    private static final int K_TAPONE = 1;
    private static final int K_SILENCEBETWEEN = 2;
    private static final int K_TAPTWO = 3;
    private static final int K_SILENCELEADOUT = 4;
    private static final int K_NUMBEREVENTS = 5;

    private static final float K_DEFAULTTNOISETHRESHOLD = 3f;
    private static final float K_DEFAULTRNOISETHRESHOLD = 2f;

    private static final long K_DEFAULTSILENCEGAPMIN = 200L;
    private static final long K_DEFAULTSILENCEGAPMAX = 1000L;
    private static final long K_DEFAULTSILENCEPERIOD = 1000L;

    private static final int K_TX = 0;
    private static final int K_TY = 1;
    private static final int K_TZ = 2;
    private static final int K_RX = 3;
    private static final int K_RY = 4;
    private static final int K_RZ = 5;

    private static final int K_WORKINGOLD = 0;
    private static final int K_WORKINGNEW = 1;
    private static final int K_WORKINGDIF = 2;

    private final SensorManager sensorManager;

    private Sensor gyroscope;
    private Sensor accelerometer;

    private SensorEventListener gyroscopeListener;
    private SensorEventListener accelerometerListener;

    public interface TapControllerRawListener {
        void onTapControllerDataUpdate(float tx, float ty, float tz, float rx, float ry, float rz, int detectionIndex);
    }

    private final List<TapControllerRawListener> rawListenerList;

    private final float[] peakTranslations = new float[K_NOEACHVALUES];
    private final float[] peakRotations = new float[K_NOEACHVALUES];

    private final float[][] workingValues = new float[3][K_NOTOTALVALUES];
    private int workingIndex;

    private final Object threadLock = new Object();
    private Thread workingThread = null;
    private volatile boolean isGatheringData = false;

    private final float[] noiseThresholds = new float[K_NOTOTALVALUES];
    private long silenceThresholdIn = K_DEFAULTSILENCEPERIOD;
    private long silenceThresholdOut = K_DEFAULTSILENCEPERIOD;
    private long silenceThresholdBetMin = K_DEFAULTSILENCEGAPMIN;
    private long silenceThresholdBetMax = K_DEFAULTSILENCEGAPMAX;

    private final long[] recordedEvents = new long[K_NUMBEREVENTS];

    private final float[] tapDetection;

    private int detectionIndex = -1;

    public TapController(Context context) {
        // setup the members for this controller
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // setup the lists
        this.rawListenerList = new ArrayList<TapControllerRawListener>();
        Arrays.fill(this.peakTranslations, 0f);
        Arrays.fill(this.peakRotations, 0f);
        Arrays.fill(this.recordedEvents, 0L);

        Arrays.fill(this.workingValues[K_WORKINGOLD], 0f);
        Arrays.fill(this.workingValues[K_WORKINGNEW], 0f);
        Arrays.fill(this.workingValues[K_WORKINGDIF], 0f);

        // setup the noise thresholds we can ignore below
        this.noiseThresholds[K_TX] = 8;
        this.noiseThresholds[K_TY] = 5;
        this.noiseThresholds[K_TZ] = 10;
        this.noiseThresholds[K_RX] = 2;
        this.noiseThresholds[K_RY] = 2;
        this.noiseThresholds[K_RZ] = 2;

        // set the silence lead in and out too
        this.silenceThresholdIn = K_DEFAULTSILENCEPERIOD;
        this.silenceThresholdOut = K_DEFAULTSILENCEPERIOD;
        this.silenceThresholdBetMin = K_DEFAULTSILENCEGAPMIN;
        this.silenceThresholdBetMax = K_DEFAULTSILENCEGAPMAX;

        // set the default tap detection values
        this.tapDetection = new float[] {0f, 0f, 1f, 0f, 0f, 0f};
        this.detectionIndex = -1;
    }

    public boolean addListener(TapControllerRawListener listener) {
        synchronized (this.rawListenerList) {
            return this.rawListenerList.add(listener);
        }
    }

    public boolean removeListener(TapControllerRawListener listener) {
        synchronized (this.rawListenerList) {
            return this.rawListenerList.remove(listener);
        }
    }

    public boolean start() {
        boolean isGyroRegistered = false;
        if (null != this.gyroscope) {
            this.gyroscopeListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    onGyroscopeChanged(sensorEvent);
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    onGyroscopeAccuracyChanged(sensor, i);

                }
            };
            // register the listener
            isGyroRegistered = this.sensorManager.registerListener(this.gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        }
        boolean isAccelRegistered = false;
        if (null != this.accelerometer) {
            this.accelerometerListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    onAccelerometerChanged(sensorEvent);
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    onAccelerometerAccuracyChanged(sensor, i);

                }
            };
            // register the listener
            isAccelRegistered = this.sensorManager.registerListener(this.accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
        // create the thread to process this data
        createProcessingThread();
        return isGyroRegistered && isAccelRegistered;
    }

    private void createProcessingThread() {
        this.isGatheringData = true;
        this.workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // while we want to gather data, process the data collected
                while (isGatheringData) {
                    // process the slice of gathered data
                    processDataSlice();
                    synchronized (threadLock) {
                        try {
                            // wait on the lock for the sleeping period
                            threadLock.wait(K_PROCESSING_PERIOD);
                        }
                        catch (InterruptedException e) {
                            // whatever
                        }
                    }
                }
                // kill the pointer for niceness
                workingThread = null;
            }
        });
        // start up the thread
        this.workingThread.start();
    }

    public boolean stop() {
        // stop the thread
        this.isGatheringData = false;
        synchronized (this.threadLock) {
            // wake up the thread so it ends
            this.threadLock.notifyAll();
        }
        // and stop listening to the sensors
        if (null != this.gyroscopeListener) {
            this.sensorManager.unregisterListener(gyroscopeListener);
            this.gyroscopeListener = null;
        }
        if (null != this.accelerometerListener) {
            this.sensorManager.unregisterListener(accelerometerListener);
            this.accelerometerListener = null;
        }
        return null == this.gyroscopeListener && null == this.accelerometerListener;
    }

    private void onAccelerometerAccuracyChanged(Sensor sensor, int i) {
        synchronized (this.peakTranslations) {
            Arrays.fill(this.peakTranslations, 0f);
        }
    }

    private void onAccelerometerChanged(SensorEvent event) {
        synchronized (this.peakTranslations) {
            // gather the peak values for each
            for (int i = 0; i < K_NOEACHVALUES; ++i) {
                if (Math.abs(event.values[i]) > Math.abs(this.peakTranslations[i])) {
                    // set this higher value
                    this.peakTranslations[i] = event.values[i];
                }
            }
        }
    }

    private void onGyroscopeAccuracyChanged(Sensor sensor, int i) {
        synchronized (this.peakRotations) {
            Arrays.fill(this.peakRotations, 0f);
        }
    }

    private void onGyroscopeChanged(SensorEvent event) {
        synchronized (this.peakRotations) {
            // gather the peak values for each
            for (int i = 0; i < K_NOEACHVALUES; ++i) {
                if (Math.abs(event.values[i]) > Math.abs(this.peakRotations[i])) {
                    // set this higher value
                    this.peakRotations[i] = event.values[i];
                }
            }
        }
    }

    private void processDataSlice() {
        // get a copy of the data ASAP
        synchronized (this.peakTranslations) {
            for (workingIndex = 0; workingIndex < K_NOEACHVALUES; ++workingIndex) {
                // get the value out
                this.workingValues[K_WORKINGNEW][workingIndex] = this.peakTranslations[workingIndex];
                // reset it
                this.peakTranslations[workingIndex] = 0;
            }
        }
        // get the rotations too
        synchronized (this.peakRotations) {
            for (workingIndex = 0; workingIndex < K_NOEACHVALUES; ++workingIndex) {
                // get the value out
                this.workingValues[K_WORKINGNEW][workingIndex + K_NOEACHVALUES] = this.peakRotations[workingIndex];
                // reset it
                this.peakRotations[workingIndex] = 0;
            }
        }
        // work on these copies to free up the gathering, check for some detection
        boolean detectedSilence = true;
        boolean detectedTap = true;
        float absDelta;
        for (workingIndex = 0; workingIndex < K_NOTOTALVALUES; ++workingIndex) {
            // calculate the diff
            this.workingValues[K_WORKINGDIF][workingIndex]
                    = this.workingValues[K_WORKINGNEW][workingIndex]
                    - this.workingValues[K_WORKINGOLD][workingIndex];
            // need the delta to check thresholds
            absDelta = Math.abs(this.workingValues[K_WORKINGDIF][workingIndex]);
            // check to see if this is silence
            if (absDelta > this.noiseThresholds[workingIndex]) {
                // this is above the noise threshold - not silence
                detectedSilence = false;
            }
            else {
                // this is counted as silence
                absDelta = 0f;
                this.workingValues[K_WORKINGDIF][workingIndex] = 0f;
            }
            if (detectedTap && this.tapDetection[workingIndex] > absDelta) {
                // this is outside the detection of a tap - not a tap
                detectedTap = false;
            }
            // change the old to the new
            this.workingValues[K_WORKINGOLD][workingIndex] = this.workingValues[K_WORKINGNEW][workingIndex];
        }
        // is this the silence lead in?
        long currentTime = System.currentTimeMillis();
        switch (detectionIndex) {
            case -1:
                // we are at the start, looking for silence
                if (detectedSilence) {
                    this.recordedEvents[K_SILENCELEADIN] = currentTime;
                    ++detectionIndex;
                }
                break;
            case K_SILENCELEADIN:
                // we are checking for a nice long silence
                if (detectedTap) {
                    // we have a tap, this is ok if we are beyond the initial silence lead-in
                    if (currentTime - this.recordedEvents[K_SILENCELEADIN] >= this.silenceThresholdIn) {
                        // we have had enough silence to detect this tap
                        this.recordedEvents[K_TAPONE] = currentTime;
                        ++detectionIndex;
                    }
                    else {
                        // this was not enough of a period of silence
                        this.detectionIndex = -1;
                    }
                }
                else if (false == detectedSilence) {
                    // wasn't a tap, but also wasn't silence
                    this.detectionIndex = -1;
                }
                break;
            case K_TAPONE:
                // we are checking for a shorter silence following the tap
                if (detectedSilence) {
                    // we have silence, start detecting the length of this
                    this.recordedEvents[K_SILENCEBETWEEN] = currentTime;
                    ++detectionIndex;
                }
                else if (false == detectedTap) {
                    // not silence following a tap and not the tap hanging around
                    detectionIndex = -1;
                }
                break;
            case K_SILENCEBETWEEN:
                // we are checking for a silence between the tap
                if (detectedTap) {
                    // we have a tap, this is ok if we are beyond the silence gap period
                    if (currentTime - this.recordedEvents[K_SILENCEBETWEEN] >= this.silenceThresholdBetMin
                     && currentTime - this.recordedEvents[K_SILENCEBETWEEN] <= this.silenceThresholdBetMax) {
                        // we have had enough silence to detect this second tap
                        this.recordedEvents[K_TAPTWO] = currentTime;
                        ++detectionIndex;
                    }
                    else {
                        // this was not the correct period of silence
                        this.detectionIndex = -1;
                    }
                }
                else if (false == detectedSilence) {
                    // wasn't a tap, but also wasn't silence
                    this.detectionIndex = -1;
                }
                else if (currentTime - this.recordedEvents[K_SILENCEBETWEEN] > this.silenceThresholdBetMax) {
                    // too much silence
                    this.detectionIndex = -1;
                }
                break;
            case K_TAPTWO:
                // we are checking for another silence following the tap
                if (detectedSilence) {
                    // we have silence, start detecting the length of this
                    this.recordedEvents[K_SILENCELEADOUT] = currentTime;
                    ++detectionIndex;
                }
                else if (false == detectedTap) {
                    // not silence following a tap and not the tap hanging around
                    detectionIndex = -1;
                }
                break;
            case K_SILENCELEADOUT:
            default:
                // we are checking for a silence at the end too
                if (detectedSilence) {
                    if (currentTime - this.recordedEvents[K_SILENCELEADOUT] >= this.silenceThresholdOut) {
                        // detection occurred
                        informControllerListeners(InputType.doubleTap);
                        detectionIndex = -1;
                    }
                }
                else if (false == detectedTap) {
                    // not silence following a tap and not the tap hanging around
                    detectionIndex = -1;
                }
                break;
        }


        synchronized (this.rawListenerList) {
            for (TapControllerRawListener listener : this.rawListenerList) {
                // inform the listeners of this raw data we just collected
                listener.onTapControllerDataUpdate(
                        this.workingValues[K_WORKINGDIF][K_TX],
                        this.workingValues[K_WORKINGDIF][K_TY],
                        this.workingValues[K_WORKINGDIF][K_TZ],
                        this.workingValues[K_WORKINGDIF][K_RX],
                        this.workingValues[K_WORKINGDIF][K_RY],
                        this.workingValues[K_WORKINGDIF][K_RZ],
                        detectionIndex);
            }
        }
    }
}
