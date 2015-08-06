package org.gnuradio.grtxfm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.gnuradio.controlport.BaseTypes;
import org.gnuradio.grcontrolport.RPCConnection;
import org.gnuradio.grcontrolport.RPCConnectionThrift;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by trondeau on 8/3/15.
 */
public class RunGraph extends Activity {

     private class CPHandler extends Handler {
         private RPCConnection mConnection;
         public CPHandler(RPCConnection conn) {
             super();
             mConnection = conn;
         }

         public void handleMessage(Message m) {
             Bundle b = m.getData();
             if(b != null) {
                 HashMap<String, RPCConnection.KnobInfo> k =
                         (HashMap<String, RPCConnection.KnobInfo>) b.getSerializable("HashMap");

                 if(k != null) {
                     mConnection.setKnobs(k);
                 }
             }
         }
     };

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
            mHandler = new CPHandler(mConnection);
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

    private RunNetworkThread mControlPortThread;
    private SeekBar mFreqSeekBar;
    private SeekBar mGainSeekBar;
    private TextView mFreqValueTextView;
    private TextView mGainValueTextView;

    private final String freqKnobName = "gr uhd usrp sink0::center_freq";
    private final String gainKnobName = "gr uhd usrp sink0::gain";

    private RPCConnection.KnobInfo prepareFreqKnob(Double freq) {
        RPCConnection.KnobInfo knob =
                new RPCConnection.KnobInfo(freqKnobName, freq, BaseTypes.DOUBLE);
        Log.d("PrepFreqKnob", "Freq: " + freq);
        return knob;
    }

    private RPCConnection.KnobInfo prepareGainKnob(Double gain) {
        RPCConnection.KnobInfo knob =
                new RPCConnection.KnobInfo(gainKnobName, gain, BaseTypes.DOUBLE);
        Log.d("PrepGainKnob", "Gain: " + gain);
        return knob;
    }

    private void postSetKnobMessage(HashMap<String, RPCConnection.KnobInfo> knobs) {
        Handler h = mControlPortThread.getHandler();
        Bundle b = new Bundle();
        b.putSerializable("HashMap", knobs);
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
        String uspfs_path = extras.getString("org.gnuradio.grtxfm.uspfs_path");
        int fd = extras.getInt("org.gnuradio.grtxfm.fd");
        String freq = extras.getString("org.gnuradio.grtxfm.freq");
        String gain = extras.getString("org.gnuradio.grtxfm.gain");

        FgInit(fd, uspfs_path);
        FgStart();

        TextView fgTextView = (TextView) findViewById(R.id.fgEditText);
        String gr = FgRep();
        fgTextView.setText(gr);

        mControlPortThread = new RunNetworkThread("localhost", 9090);
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

        RPCConnection.KnobInfo knobFreq = prepareFreqKnob(1e6*Double.parseDouble(freq));
        RPCConnection.KnobInfo knobGain = prepareGainKnob(Double.parseDouble(gain));
        HashMap<String, RPCConnection.KnobInfo> setknobs = new HashMap<>();
        setknobs.put(freqKnobName, knobFreq);
        setknobs.put(gainKnobName, knobGain);
        postSetKnobMessage(setknobs);

        addFreqControls(Double.parseDouble(freq)*1e6);
        addGainControls(Double.parseDouble(gain));
    }

    private void addFreqControls(Double initFreq) {
        mFreqSeekBar = (SeekBar) findViewById(R.id.freqSeekBar2);
        mFreqValueTextView = (TextView) findViewById(R.id.freqValueTextView2);
        final Double minfreq = 88.1e6;
        final Double maxfreq = 107.9e6;
        final Double stepfreq = 200e3;
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

                RPCConnection.KnobInfo knobFreq = prepareFreqKnob(p);
                HashMap<String, RPCConnection.KnobInfo> setknobs = new HashMap<>();
                setknobs.put(freqKnobName, knobFreq);
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

    private void addGainControls(Double initGain) {
        mGainSeekBar = (SeekBar) findViewById(R.id.gainSeekBar2);
        mGainValueTextView = (TextView) findViewById(R.id.gainValueTextView2);

        final Double stepgain = 0.5;
        mGainSeekBar.setMax(89 * 2);

        mGainValueTextView.setText(String.format(initGain.toString()));

        Double dval = initGain / stepgain;
        Integer ival = dval.intValue();
        mGainSeekBar.setProgress(ival);

        mGainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = stepgain * progress;
                mGainValueTextView.setText(String.format("%.1f", p));

                RPCConnection.KnobInfo knobGain = prepareGainKnob(p);
                HashMap<String, RPCConnection.KnobInfo> setknobs = new HashMap<>();
                setknobs.put(gainKnobName, knobGain);
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

    public native void FgInit(int fd, String devname);
    public native void FgStart();
    public native String FgRep();

    static {
        System.loadLibrary("fg");
    }
}
