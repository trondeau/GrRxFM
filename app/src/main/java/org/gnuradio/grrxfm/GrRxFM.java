package org.gnuradio.grrxfm;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


public class GrRxFM extends Activity {

    Button mStartButton;
    SeekBar mFreqSeekBar;
    SeekBar mGainSeekBar;
    TextView mFreqValueTextView;
    TextView mGainValueTextView;
    private Context mContext;

    public static Intent intent = null;

    private String usbfs_path = null;
    private int fd = -1;
    private int cpport = 0;
    private static final String UHD_USB_INFO_FILE = "uhd_usb_info.txt";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getApplicationContext();

        ReadUSBInfoFile();
        ReadThriftConfig();

        intent = getIntent();

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null) {
            Log.d("GrRxFM", "Didn't get a device; finding it now.");

            final HashSet<String> allowed_devices = getAllowedDevices(this);
            final HashMap<String, UsbDevice> usb_device_list = usbManager.getDeviceList();

            for (UsbDevice candidate : usb_device_list.values()) {
                String candstr = "v" + candidate.getVendorId() + "p" + candidate.getProductId();
                if (allowed_devices.contains(candstr)) {
                    // Need to handle case where we have more than one device connected
                    device = candidate;
                }
            }
        }
        Log.d("GrRxFM", "Selected Device: " + device);

        usbManager.requestPermission(device, permissionIntent);

        final UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection == null) {
            Log.d("GrRxFM", "Didn't get a USB Device Connection");
            finish();
        }

        assert connection != null;
        fd = connection.getFileDescriptor();

        assert device != null;
        int vid = device.getVendorId();
        int pid = device.getProductId();

        Log.d("GrRxFM", "Found fd: " + fd + "  vid: " + vid + "  pid: " + pid);

        SetTMP(getCacheDir().getAbsolutePath());

        setContentView(R.layout.activity_gr_rxfm);

        setupRadio();
    }


    private void ReadUSBInfoFile() {
        File sdcard = Environment.getExternalStorageDirectory();
        File fname = new File(sdcard, UHD_USB_INFO_FILE);

        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(fname);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder filecontents = new StringBuilder("");
        byte[] inbuf = new byte[1024];
        try {
            int n;
            assert fstream != null;
            while ((n = fstream.read(inbuf)) != -1) {
                filecontents.append(new String(inbuf, 0, n));
            }
            fstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String t = filecontents.toString();
        String[] text_parts = t.split("\n");
        String strfd = text_parts[0];
        String strusb = text_parts[1];

        fd = Integer.valueOf(strfd);
        usbfs_path = strusb;

        Log.d("GrTxFM", "Got USB Info from File: " + fd + ", " + usbfs_path);
    }

    private void ReadThriftConfig() {
        File sdcard = Environment.getExternalStorageDirectory();
        File fname = new File(sdcard, ".gnuradio/thrift.conf");

        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(fname);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder filecontents = new StringBuilder("");
        byte[] inbuf = new byte[1024];
        try {
            int n;
            assert fstream != null;
            while ((n = fstream.read(inbuf)) != -1) {
                filecontents.append(new String(inbuf, 0, n));
            }
            fstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String t = filecontents.toString();
        String[] text_parts = t.split("\n");
        for(String s: text_parts) {
            String s_rep = s.replaceAll("\\s*", "");
            String[] line_parts = s_rep.split("=");
            if(line_parts[0].equals("port")) {
                String s_port = line_parts[1];
                cpport = Integer.valueOf(s_port);
            }
        }
    }

    private void setupRadio() {
        addFreqControls();
        addGainControls();
        addStartButton();
    }

    private void addFreqControls() {
        mFreqSeekBar = (SeekBar) findViewById(R.id.freqSeekBar2);
        mFreqValueTextView = (TextView) findViewById(R.id.freqValueTextView2);
        final Double minfreq = (double)(getResources().getInteger(R.integer.minfreq));
        final Double maxfreq = (double)(getResources().getInteger(R.integer.maxfreq));
        final Double stepfreq = (double)(getResources().getInteger(R.integer.stepfreq));
        final Double nchannels = (maxfreq - minfreq) / stepfreq;

        mFreqSeekBar.setMax(nchannels.intValue());

        mFreqValueTextView.setText(String.format("%.1f", minfreq / 1e6));

        mFreqSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = minfreq + stepfreq * progress;
                mFreqValueTextView.setText(String.format("%.1f", p / 1e6));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void addGainControls() {
        mGainSeekBar = (SeekBar) findViewById(R.id.gainSeekBar2);
        mGainValueTextView = (TextView) findViewById(R.id.gainValueTextView2);

        final Double stepgain = 0.5;
        mGainSeekBar.setMax(89 * 2);

        mGainValueTextView.setText(String.format("0"));

        mGainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = stepgain * progress;
                mGainValueTextView.setText(String.format("%.1f", p));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void addStartButton() {
        mStartButton = (Button)findViewById(R.id.startButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent runIntent = new Intent(mContext, RunGraph.class);
                runIntent.putExtra("org.gnuradio.grrxfm.fd", fd);
                runIntent.putExtra("org.gnuradio.grrxfm.usbfs_path", usbfs_path);
                runIntent.putExtra("org.gnuradio.grrxfm.freq", mFreqValueTextView.getText());
                runIntent.putExtra("org.gnuradio.grrxfm.gain", mGainValueTextView.getText());
                runIntent.putExtra("org.gnuradio.grrxfm.port", cpport);
                startActivity(runIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gr_rxfm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Reads from the device_filter.xml file to get a list of the allowable devices.
     */
    private static HashSet<String> getAllowedDevices(final Context ctx) {
        final HashSet<String> ans = new HashSet<String>();
        try {
            final XmlResourceParser xml = ctx.getResources().getXml(R.xml.device_filter);

            xml.next();
            int eventType;
            while ((eventType = xml.getEventType()) != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (xml.getName().equals("usb-device")) {
                        final AttributeSet as = Xml.asAttributeSet(xml);
                        final Integer vendorId = Integer.valueOf(as.getAttributeValue(null, "vendor-id"), 10);
                        final Integer productId = Integer.valueOf(as.getAttributeValue(null, "product-id"), 10);
                        ans.add("v"+vendorId+"p"+productId);
                    }
                    break;
                }
                xml.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ans;
    }

    public native void SetTMP(String tmpname);

    static {
        System.loadLibrary("fg");
    }
}
