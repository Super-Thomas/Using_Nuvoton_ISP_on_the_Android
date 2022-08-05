// ns50254/HidBridge.java

package com.example.nvt_isp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class HidBridge {
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private boolean mbUsbReadyFlag;

    private static final String ACTION_USB_PERMISSION = "com.example.nvt_isp.USB_PERMISSION";

    // Locker object that is responsible for locking read/write thread.
    private Object mLocker = new Object();
    //private Thread mReadingThread = null;

    // The queue that contains the read data.
    //private Queue<byte[]> mReceivedQueue;

    /**
     * Creates a hid bridge to the dongle. Should be created once.
     */
    public HidBridge() {
        //mReceivedQueue = new LinkedList<byte[]>();
        mbUsbReadyFlag = false;
    }

    public boolean GetUsbReadyFlag() {
        return mbUsbReadyFlag;
    }

    public void SetUsbReadyFlag(boolean bReady) {
        mbUsbReadyFlag = bReady;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null) {
                            // call method to set up device communication
                            SetUsbReadyFlag(true);
                        }
                    }
                    else {
                        // permission denied for the device + device
                    }
                }
            }
        }
    };

    /**
     * Searches for the device and opens it if successful
     * @return true, if connection was successful
     */
    public boolean OpenDevice(ListView listView, Context context) {
        mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        mUsbDevice = null;
        List<String> usbList = new ArrayList<>();
        List<UsbDevice> usbListforConnection = new ArrayList<>();

        // Iterate all the available devices and find ours.
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            String usbString = "Vendor: ";
            usbString += Integer.toString(device.getVendorId());
            usbString += " Product: ";
            usbString += Integer.toString(device.getProductId());
            usbList.add(usbString);
            usbListforConnection.add(device);
        }

        if (usbList.size() > 0)
        {
            ArrayAdapter<String> adpater = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, usbList);
            listView.setAdapter(adpater);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    mUsbDevice = usbListforConnection.get(position);

                    // Create and intent and request a permission.
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    context.registerReceiver(mUsbReceiver, filter);
                    mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
                    //StartReadingThread();
                    ((MainActivity)MainActivity.mContext).ShowISPFragment();
                }
            });
        }

        return true;
    }

    /**
     * Closes the reading thread of the device.
     */
    /*public void CloseTheDevice() {
        StopReadingThread();
    }*/

    /**
     * Starts the thread that continuously reads the data from the device.
     * Should be called in order to be able to talk with the device.
     */
    /*public void StartReadingThread() {
        if (mReadingThread == null) {
            mReadingThread = new Thread(readerReceiver);
            mReadingThread.start();
        } else {
            // Reading thread already started
        }
    }*/

    /**
     * Stops the thread that continuously reads the data from the device.
     * If it is stopped - talking to the device would be impossible.
     */
    @SuppressWarnings("deprecation")
    /*public void StopReadingThread() {
        if (mReadingThread != null) {
            // Just kill the thread. It is better to do that fast if we need that asap.
            mReadingThread.stop();
            mReadingThread = null;
        } else {
            // No reading thread to stop
        }
    }*/

    /**
     * Write data to the usb hid. Data is written as-is, so calling method is responsible for adding header data.
     * @param bytes is the data to be written.
     * @return true if succeed.
     */
    public boolean WriteData(byte[] bytes) {
        try
        {
            // Lock that is common for read/write methods.
            synchronized (mLocker) {
                if (GetUsbReadyFlag() == false) {
                    return false;
                }

                UsbInterface writeIntf = mUsbDevice.getInterface(0);
                UsbEndpoint writeEp = writeIntf.getEndpoint(1);
                UsbDeviceConnection writeConnection = mUsbManager.openDevice(mUsbDevice);

                // Lock the usb interface.
                writeConnection.claimInterface(writeIntf, true);

                // Write the data as a bulk transfer with defined data length.
                int r = writeConnection.bulkTransfer(writeEp, bytes, bytes.length, 0);
                if (r != -1) {
                    // Written %s bytes to the dongle. Data written: %s, r, composeString(bytes)
                } else {
                    // Error happened while writing data. No ACK
                }

                // Release the usb interface.
                writeConnection.releaseInterface(writeIntf);
                writeConnection.close();
            }
        }
        catch(NullPointerException e) {
            // Error happend while writing. Could not connect to the device or interface is busy?
            return false;
        }

        return true;
    }

    /**
     * @return true if there are any data in the queue to be read.
     */
    /*public boolean IsThereAnyReceivedData() {
        synchronized(mLocker) {
            return !mReceivedQueue.isEmpty();
        }
    }*/

    /**
     * Queue the data from the read queue.
     * @return queued data.
     */
    /*public byte[] GetReceivedDataFromQueue() {
        synchronized(mLocker) {
            return mReceivedQueue.poll();
        }
    }*/

    public byte[] GetReceivedData() {
        UsbEndpoint readEp;
        UsbDeviceConnection readConnection = null;
        UsbInterface readIntf = null;
        boolean readerStartedMsgWasShown = false;

        if (mUsbDevice == null) {
            // No device to read from
            return null;
        }

        if (GetUsbReadyFlag() == false) {
            return null;
        }

        // Lock that is common for read/write methods.
        synchronized (mLocker) {
            try {
                readIntf = mUsbDevice.getInterface(0);
                readEp = readIntf.getEndpoint(0);

                try {
                    readConnection = mUsbManager.openDevice(mUsbDevice);
                    if (readConnection == null) {
                        // Cannot start reader because the user didn't gave me permissions or the device is not present. Retrying in 2 sec...
                        return null;
                    }

                    // Claim and lock the interface in the android system.
                    readConnection.claimInterface(readIntf, true);
                } catch (SecurityException e) {
                    // Cannot start reader because the user didn't gave me permissions. Retrying in 2 sec...
                    return null;
                }

                // Read the data as a bulk transfer with the size = MaxPacketSize
                int packetSize = readEp.getMaxPacketSize();
                byte[] bytes = new byte[packetSize];
                int r = readConnection.bulkTransfer(readEp, bytes, packetSize, 50);

                // Release the interface lock.
                readConnection.releaseInterface(readIntf);
                readConnection.close();

                if (r > 0) {
                    return bytes;
                }

            } catch (NullPointerException e) {
                // Error happened while reading. No device or the connection is busy
                return null;
            } catch (ThreadDeath e) {
                if (readConnection != null) {
                    readConnection.releaseInterface(readIntf);
                    readConnection.close();
                }

                return null;
            }
        }

        return null;
    }

    // The thread that continuously receives data from the dongle and put it to the queue.
    /*private Runnable readerReceiver = new Runnable() {
        public void run() {
            UsbEndpoint readEp;
            UsbDeviceConnection readConnection = null;
            UsbInterface readIntf = null;
            boolean readerStartedMsgWasShown = false;

            if (mUsbDevice == null) {
                // No device to read from
                return;
            }

            // We will continuously ask for the data from the device and store it in the queue.
            while (true) {
                // Lock that is common for read/write methods.
                synchronized (mLocker) {
                    try
                    {
                        if (GetUsbReadyFlag() == false) {
                            Sleep(1000);
                            continue;
                        }

                        if (mUsbDevice == null) {
                            // No device. Recheking in 10 sec...
                            Sleep(10000);
                            continue;
                        }

                        readIntf = mUsbDevice.getInterface(0);
                        readEp = readIntf.getEndpoint(0);

                        try
                        {
                            readConnection = mUsbManager.openDevice(mUsbDevice);
                            if (readConnection == null) {
                                // Cannot start reader because the user didn't gave me permissions or the device is not present. Retrying in 2 sec...
                                Sleep(2000);
                                continue;
                            }

                            // Claim and lock the interface in the android system.
                            readConnection.claimInterface(readIntf, true);
                        }
                        catch (SecurityException e) {
                            // Cannot start reader because the user didn't gave me permissions. Retrying in 2 sec...
                            Sleep(2000);
                            continue;
                        }

                        // Show the reader started message once.
                        if (!readerStartedMsgWasShown) {
                            // !!! Reader was started !!!
                            readerStartedMsgWasShown = true;
                        }

                        // Read the data as a bulk transfer with the size = MaxPacketSize
                        int packetSize = readEp.getMaxPacketSize();
                        byte[] bytes = new byte[packetSize];
                        int r = readConnection.bulkTransfer(readEp, bytes, packetSize, 50);
                        if (r >= 0) {
                            byte[] trancatedBytes = new byte[r]; // Truncate bytes in the honor of r
                            int i=0;
                            for (byte b : bytes) {
                                trancatedBytes[i] = b;
                                i++;
                            }

                            mReceivedQueue.add(trancatedBytes); // Store received data
                            // Message received of lengths %s and content: %s, r, composeString(bytes)
                        }

                        // Release the interface lock.
                        readConnection.releaseInterface(readIntf);
                        readConnection.close();
                    }
                    catch (NullPointerException e) {
                        // Error happened while reading. No device or the connection is busy
                    }
                    catch (ThreadDeath e) {
                        if (readConnection != null) {
                            readConnection.releaseInterface(readIntf);
                            readConnection.close();
                        }

                        throw e;
                    }
                }

                // Sleep for 10 ms to pause, so other thread can write data or anything.
                // As both read and write data methods lock each other - they cannot be run in parallel.
                // Looks like Android is not so smart in planning the threads, so we need to give it a small time
                // to switch the thread context.
                Sleep(10);
            }
        }
    };*/

    private void Sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
