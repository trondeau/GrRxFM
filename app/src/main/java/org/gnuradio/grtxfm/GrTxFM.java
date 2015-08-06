package org.gnuradio.grtxfm;

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

import java.util.HashMap;
import java.util.HashSet;


public class GrTxFM extends Activity {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String DEFAULT_USPFS_PATH = "/dev/bus/usb";

    public static Intent intent = null;
    public static PendingIntent permissionIntent;

    private String uspfs_path = null;
    private int fd = -1;

    Button mStartButton;
    SeekBar mFreqSeekBar;
    SeekBar mGainSeekBar;
    TextView mFreqValueTextView;
    TextView mGainValueTextView;
    private Context mContext;

    private final static String properDeviceName(String deviceName) {
        if (deviceName == null) return DEFAULT_USPFS_PATH;
        deviceName = deviceName.trim();
        if (deviceName.isEmpty()) return DEFAULT_USPFS_PATH;

        final String[] paths = deviceName.split("/");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length-2; i++)
            if (i == 0)
                sb.append(paths[i]);
            else
                sb.append("/").append(paths[i]);
        final String stripped_name = sb.toString().trim();
        if (stripped_name.isEmpty())
            return DEFAULT_USPFS_PATH;
        else
            return stripped_name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getApplicationContext();

        SetTMP(getCacheDir().getAbsolutePath());

        intent = getIntent();

        setContentView(R.layout.activity_gr_txfm);
    }

    private void setupRadio() {
        addFreqControls();
        addGainControls();
        addStartButton();
    }

    private void addFreqControls() {
        mFreqSeekBar = (SeekBar) findViewById(R.id.freqSeekBar2);
        mFreqValueTextView = (TextView) findViewById(R.id.freqValueTextView2);
        final Double minfreq = 88.1e6;
        final Double maxfreq = 107.9e6;
        final Double stepfreq = 200e3;
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
                runIntent.putExtra("org.gnuradio.grtxfm.fd", fd);
                runIntent.putExtra("org.gnuradio.grtxfm.uspfs_path", uspfs_path);
                runIntent.putExtra("org.gnuradio.grtxfm.freq", mFreqValueTextView.getText());
                runIntent.putExtra("org.gnuradio.grtxfm.gain", mGainValueTextView.getText());
                startActivity(runIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gr_txfm, menu);
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

        @Override
    protected void onStart() {
        super.onStart();
        Log.d("GrCwTx", "Called onStart.");

        try {
            permissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent("com.android.example.USB_PERMISSION"), 0);
            registerReceiver(mUsbReceiver,
                             new IntentFilter("com.android.example.USB_PERMISSION"));
        }
        catch (Throwable e) {
            Log.d("GrCwTx", "Broadcast permission failed.");
        }

        final UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if(device == null) {
            final HashSet<String> allowed_devices = getAllowedDevices(this);
            final HashMap<String, UsbDevice> usb_device_list = manager.getDeviceList();

            for (UsbDevice candidate : usb_device_list.values()) {
                String candstr = "v" + candidate.getVendorId() + "p" + candidate.getProductId();
                if (allowed_devices.contains(candstr)) {
                    // Need to handle case where we have more than one device connected
                    device = candidate;
                }
            }
            Log.d("GrCwTx", "Selected Device: " + device);
        }

        manager.requestPermission(device, permissionIntent);
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
                        final Integer vendorId = Integer.valueOf(as.getAttributeValue(null, "vendor-id"), 16);
                        final Integer productId = Integer.valueOf(as.getAttributeValue(null, "product-id"), 16);
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


    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.android.example.USB_PERMISSION".equals(action)) {
                synchronized (this) {
                    final UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            Log.d("GrCwTx", "Android granted USB device permission.");

                            final UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
                            if (device != null && !manager.hasPermission(device)) {
                                Log.d("GrCwTx", "Requesting permsission to open USB device.");
                                manager.requestPermission(device, permissionIntent);
                            }

                            final UsbDeviceConnection connection = manager.openDevice(device);
                            if (connection == null) {
                                Log.d("GrCwTx",
                                      "Could not create a connection to USB device.");
                            }

                            fd = connection.getFileDescriptor();
                            uspfs_path = properDeviceName(device.getDeviceName());
                            setupRadio();
                        }
                        else {
                            Log.d("GrCwTx",
                                  "Android granted USB device permissions but device was lost.");
                        }
                    }
                    else {
                        Log.d("GrCwTx",
                              "Android did not give USB device permissions.");
                    }
                }
            }

        }
	};

    public native void SetTMP(String tmpname);

    static {
        System.loadLibrary("fg");
    }
}
