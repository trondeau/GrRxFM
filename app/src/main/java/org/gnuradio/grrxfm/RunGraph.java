package org.gnuradio.grrxfm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import org.gnuradio.controlport.BaseTypes;
import org.gnuradio.grcontrolport.RPCConnection;
import org.gnuradio.grcontrolport.RPCConnectionThrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class RunGraph extends Activity {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String USRP_SOURCE_NAME = "gr uhd usrp source0";
    private static final String USRP_COMMAND_NAME = "command";
    private static final String SENSITIVITY_KNOB_NAME = "quadrature_demod_cf0::gain";

    private int mControlPortPort = 0;

    private static class SetKnobsHandler extends Handler {
        private RPCConnection mConnection;
        public SetKnobsHandler(RPCConnection conn) {
            super();
            mConnection = conn;
        }

        public void handleMessage(Message m) {
            Bundle b = m.getData();
            if(b != null) {
                HashMap<String, RPCConnection.KnobInfo> k =
                        (HashMap<String, RPCConnection.KnobInfo>) b.getSerializable("HashMap");

                HashMap<String, Double> usrpHashMap =
                        (HashMap<String, Double>) b.getSerializable("UsrpHashMap");

                Log.d("RunGraph", "Set Knobs: " + k);
                if((k != null) && (!k.isEmpty())) {
                    mConnection.setKnobs(k);
                }

                if(usrpHashMap != null) {
                    Set usrpSet = usrpHashMap.entrySet();
                    for (Object anUsrpSet : usrpSet) {
                        Map.Entry e = (Map.Entry) anUsrpSet;
                        mConnection.postMessage(USRP_SOURCE_NAME, USRP_COMMAND_NAME,
                                (String) e.getKey(), (Double) e.getValue());
                    }
                }
            }
        }
    }

    public class RunNetworkThread implements Runnable {

        private RPCConnection mConnection;
        private String mHost;
        private Integer mPort;
        private Boolean mConnected;
        private Handler mHandler;

        RunNetworkThread(String host, Integer port) {
            this.mHost = host;
            this.mPort = port;
            this.mConnected = false;
        }

        public void run() {
            if(!mConnected) {
                mConnection = new RPCConnectionThrift(mHost, mPort);
                mConnected = true;
            }

            Looper.prepare();
            mHandler = new SetKnobsHandler(mConnection);
            Looper.loop();
        }

        public RPCConnection getConnection() {
            if(mConnection == null) {
                throw new IllegalStateException("connection not established");
            }
            return mConnection;
        }

        public Handler getHandler() {
            return mHandler;
        }
    }

    public RunNetworkThread mControlPortThread;
    private TextView mFreqValueTextView;
    private TextView mGainValueTextView;
    private TextView mFMValueTextView;
    private SampleView mSampView;

    private RPCConnection.KnobInfo prepareFMDeviationKnob(Double fm) {
        Double k = 2.0 * Math.PI * fm / (320e3);
        return new RPCConnection.KnobInfo(SENSITIVITY_KNOB_NAME, k, BaseTypes.DOUBLE);
    }

    private void postSetKnobMessage(HashMap<String, RPCConnection.KnobInfo> knobs) {
        Handler h = mControlPortThread.getHandler();
        Bundle b = new Bundle();
        b.putSerializable("HashMap", knobs);
        Message m = h.obtainMessage();
        m.setData(b);
        h.sendMessage(m);
    }

    public void postMessage(HashMap<String, Double> knobs) {
        Handler h = mControlPortThread.getHandler();
        Bundle b = new Bundle();
        b.putSerializable("UsrpHashMap", knobs);
        Message m = h.obtainMessage();
        m.setData(b);
        h.sendMessage(m);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rungraph);

        Bundle extras = getIntent().getExtras();
        String usbfs_path = extras.getString("org.gnuradio.grrxfm.usbfs_path");
        int fd = extras.getInt("org.gnuradio.grrxfm.fd");
        String freq = extras.getString("org.gnuradio.grrxfm.freq");
        String gain = extras.getString("org.gnuradio.grrxfm.gain");
        mControlPortPort = extras.getInt("org.gnuradio.grrxfm.port");

        Log.d("RunGraph", "Calling FgInit");
        FgInit(fd, usbfs_path);
        Log.d("RunGraph", "Calling Fgstart");
        FgStart();

        Log.d("RunGraph", "Calling FgRep");
        TextView fgTextView = (TextView) findViewById(R.id.fgEditText);
        String gr = FgRep();
        fgTextView.setText(gr);

        mControlPortThread = new RunNetworkThread("localhost", mControlPortPort);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(mControlPortThread);

        while(true) {
            try {
                mControlPortThread.getConnection();
                break;
            } catch (IllegalStateException e0) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        addFreqControls(Double.parseDouble(freq) * 1e6);
        addGainControls(Double.parseDouble(gain));
        //addSensitivityControls();

        HashMap<String, Double> usrpKnobs = new HashMap<>();
        HashMap<String, RPCConnection.KnobInfo> setKnobs = new HashMap<>();
        usrpKnobs.put("freq", 1e6*Double.parseDouble(freq));
        usrpKnobs.put("gain", Double.parseDouble(gain));
        postSetKnobMessage(setKnobs);
        postMessage(usrpKnobs);

        float [] floatarray = new float[2048];
        for(int i = 0; i < 2048; i++) {
            floatarray[i] = (float)i;
        }
        mSampView = (SampleView)findViewById(R.id.sampleview);
        mSampView.SetData(floatarray);
        //mSampView.SetData(this);
    }

    private void addFreqControls(Double initFreq) {
        SeekBar mFreqSeekBar = (SeekBar) findViewById(R.id.freqSeekBar2);
        mFreqValueTextView = (TextView) findViewById(R.id.freqValueTextView2);
        final Double minfreq = (double)(getResources().getInteger(R.integer.minfreq));
        final Double maxfreq = (double)(getResources().getInteger(R.integer.maxfreq));
        final Double stepfreq = (double)(getResources().getInteger(R.integer.stepfreq));
        final Double nchannels = (maxfreq - minfreq) / stepfreq;

        mFreqSeekBar.setMax(nchannels.intValue());

        mFreqValueTextView.setText(String.format("%.1f", initFreq / 1e6));

        Double dval = 100.0 * (initFreq - minfreq) / (maxfreq - minfreq);
        Integer ival = dval.intValue();
        mFreqSeekBar.setProgress(ival);

        mFreqSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = minfreq + stepfreq * progress;
                mFreqValueTextView.setText(String.format("%.1f", p / 1e6));

                HashMap<String, Double> usrpKnobs = new HashMap<>();
                usrpKnobs.put("freq", p);
                postMessage(usrpKnobs);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void addGainControls(Double initGain) {
        SeekBar mGainSeekBar = (SeekBar) findViewById(R.id.gainSeekBar2);
        mGainValueTextView = (TextView) findViewById(R.id.gainValueTextView2);

        final Double stepgain = 0.5;
        mGainSeekBar.setMax(89 * 2);

        mGainValueTextView.setText(String.format("%f", initGain));

        Double dval = initGain / stepgain;
        Integer ival = dval.intValue();
        mGainSeekBar.setProgress(ival);

        mGainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = stepgain * progress;
                mGainValueTextView.setText(String.format("%.1f", p));

                HashMap<String, Double> usrpKnobs = new HashMap<>();
                usrpKnobs.put("gain", p);
                postMessage(usrpKnobs);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // NOT AVAILABLE YET
    /*
    private void addSensitivityControls() {
        SeekBar mFMSeekBar = (SeekBar) findViewById(R.id.fmSeekBar2);
        mFMValueTextView = (TextView) findViewById(R.id.fmValueTextView2);

        mFMValueTextView.setText("100");
        mFMSeekBar.setProgress(100);

        mFMSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = 1e3 * progress;
                mFMValueTextView.setText(String.format("%.1f", p / 1e3));

                RPCConnection.KnobInfo knob = prepareFMDeviationKnob(p);
                HashMap<String, RPCConnection.KnobInfo> setknobs = new HashMap<>();
                setknobs.put(SENSITIVITY_KNOB_NAME, knob);
                postSetKnobMessage(setknobs);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    */

    public native void FgInit(int fd, String devname);
    public native void FgStart();
    public native String FgRep();

    static {
        System.loadLibrary("fg");
    }
}
