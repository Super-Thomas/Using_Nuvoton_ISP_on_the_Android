package com.example.nvt_isp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NvtISPFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NvtISPFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mView;

    private Thread mIspThread = null;

    private static final char STATE_INIT = 0x00;
    private static final char STATE_TRY_TO_CONNECT_USB = 0x01;
    private static final char STATE_WAITING_FOR_DEVICEID = 0x02;
    private static final char STATE_USB_CONNECTED = 0x03;
    private static final char STATE_TRY_TO_PROGRAM_FOR_APROM = 0x04;
    private static final char STATE_TRY_TO_PROGRAM_FOR_EXFLASH = 0x05;
    private static final char STATE_PROGRAM_IS_DONE = 0x06;
    private int mState = STATE_INIT;
    private boolean mbConnectFlag = false;

    private NvtChipInfo mNvtChipInfo;

    private String mApromFileName;
    private Uri mApromUri;
    int mApromFileSize;
    byte[] mApromFileBuffer;

    private String mExFlashFileName;
    private Uri mExFlashUri;
    int mExFlashFileSize;
    byte[] mExFlashFileBuffer;

    private boolean mUpdateFlag;
    private int mPercent;
    private int mTotalLength;
    private int mUpdateLength;
    private int mUpdateLengthForAprom;
    private int mUpdateLengthForExFlash;
    private int mStartAddress;
    private int mCheckSum;
    private int mCmdIndex = 18;

    // Command List for ISP Tool
    private static final int CMD_UPDATE_APROM = 0x000000A0;
    private static final int CMD_UPDATE_CONFIG = 0x000000A1;
    private static final int CMD_READ_CONFIG = 0x000000A2;
    private static final int CMD_ERASE_ALL = 0x000000A3;
    private static final int CMD_SYNC_PACKNO = 0x000000A4;
    private static final int CMD_GET_FWVER = 0x000000A6;
    private static final int CMD_SET_APPINFO = 0x000000A7;
    private static final int CMD_GET_APPINFO = 0x000000A8;
    private static final int CMD_RUN_APROM = 0x000000AB;
    private static final int CMD_RUN_LDROM = 0x000000AC;
    private static final int CMD_RESET = 0x000000AD;
    private static final int CMD_CONNECT = 0x000000AE;
    private static final int CMD_DISCONNECT = 0x000000AF;
    private static final int CMD_GET_DEVICEID = 0x000000B1;
    private static final int CMD_UPDATE_DATAFLASH = 0x000000C3;
    private static final int CMD_WRITE_CHECKSUM = 0x000000C9;
    private static final int CMD_GET_FLASHMODE = 0x000000CA;
    private static final int CMD_RESEND_PACKET = 0x000000FF;
    private static final int CMD_ERASE_SPIFLASH = 0x000000D0;
    private static final int CMD_UPDATE_SPIFLASH = 0x000000D1;

    Button mBtnConnect;
    Button mBtnAprom;
    Button mBtnExFlash;
    Button mBtnProgram;
    TextView mTvState;
    TextView mTvDeviceName;
    TextView mTvAprom;
    TextView mTvExFlash;
    ProgressBar mProgressBar;
    TextView mTvPercent;

    public NvtISPFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NvtISPFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NvtISPFragment newInstance(String param1, String param2) {
        NvtISPFragment fragment = new NvtISPFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_nvt_i_s_p, container, false);

        mBtnConnect = (Button)mView.findViewById(R.id.button_connect);
        mBtnAprom = (Button)mView.findViewById(R.id.button_aprom);
        mBtnExFlash = (Button)mView.findViewById(R.id.button_exflash);
        mBtnProgram = (Button)mView.findViewById(R.id.button_program);
        mTvState = (TextView)mView.findViewById(R.id.textView_state);
        mTvDeviceName = (TextView)mView.findViewById(R.id.textView_devicename);
        mTvAprom = (TextView)mView.findViewById(R.id.textView_aprom);
        mTvExFlash = (TextView)mView.findViewById(R.id.textView_exflash);
        mProgressBar = (ProgressBar)mView.findViewById(R.id.progressBar) ;
        mTvPercent = (TextView)mView.findViewById(R.id.textView_percent);

        mProgressBar.setProgress(0);

        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If there haven't usb connection,
                if (mbConnectFlag == false) {
                    // It will try to connect USB
                    mState = STATE_TRY_TO_CONNECT_USB;
                    mTvState.setText((String)"Please reset the target board");
                }
                // If there have usb connection,
                else {
                    // It will try to disconnect USB
                }
            }
        });

        mBtnAprom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApromFileSize = 0;
                mApromFileBuffer = null;
                mApromUri = null;
                mApromFileName = "";
                System.gc();

                ((MainActivity)getActivity()).OpenFileDialog(false);
            }
        });

        mBtnExFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExFlashFileSize = 0;
                mExFlashFileBuffer = null;
                mExFlashUri = null;
                mExFlashFileName = "";
                System.gc();

                ((MainActivity)getActivity()).OpenFileDialog(true);
            }
        });

        mBtnProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If there haven't usb connection,
                if (mbConnectFlag == false) {
                    // Please connect USB
                    ((MainActivity)getActivity()).ShowToastMessage("Please connect USB");
                }
                else if ((mApromUri == null || mApromFileName.length() <= 0 || mApromFileSize <= 0 || mApromFileBuffer == null) &&
                        (mExFlashUri == null || mExFlashFileName.length() <= 0 || mExFlashFileSize <= 0 || mExFlashFileBuffer == null)) {
                    // Please select APROM or External Flash file
                    ((MainActivity)getActivity()).ShowToastMessage("Please select file for APROM or External Flash");
                }
                else if (mState != STATE_TRY_TO_PROGRAM_FOR_APROM && mState != STATE_TRY_TO_PROGRAM_FOR_EXFLASH) {
                    mUpdateFlag = true;
                    mPercent = 0;
                    mStartAddress = 0;
                    mUpdateLength = 0;
                    mUpdateLengthForAprom = 0;
                    mUpdateLengthForExFlash = 0;
                    mTotalLength = mApromFileSize + mExFlashFileSize;
                    mState = STATE_TRY_TO_PROGRAM_FOR_APROM;
                }
            }
        });

        mNvtChipInfo =  new NvtChipInfo();
        StartISPThread();

        return mView;
    }

    public static final int GetFileSize(InputStream inputStream) {
        int size = 0;

        try {
            size = inputStream.available();
        }
        catch (IOException e) {
            //e.printStackTrace();
        }

        return size;
    }

    public boolean DoEraseAll()
    {
        boolean bRet = false;

        return bRet;
    }

    public boolean ReadFileForAprom() {
        boolean bRet = false;

        if (mApromUri != null) {
            try {
                InputStream in = getActivity().getContentResolver().openInputStream(mApromUri);
                mApromFileSize = GetFileSize(in);

                if (mApromFileSize > 0 && mApromFileBuffer == null) {
                    mApromFileBuffer = new byte[mApromFileSize];
                    while (in.read(mApromFileBuffer) > 0) {
                        // Read size: + length
                    }
                }

                in.close();

                bRet = true;
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        return bRet;
    }

    public boolean ReadFileForExFlash() {
        boolean bRet = false;

        if (mExFlashUri != null) {
            try {
                InputStream in = getActivity().getContentResolver().openInputStream(mExFlashUri);
                mExFlashFileSize = GetFileSize(in);

                if (mExFlashFileSize > 0 && mExFlashFileBuffer == null) {
                    mExFlashFileBuffer = new byte[mExFlashFileSize];
                    while (in.read(mExFlashFileBuffer) > 0) {
                        // Read size: + length
                    }
                }

                in.close();

                bRet = true;
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        return bRet;
    }

    public boolean DoProgramForAprom()
    {
        boolean bRet = false;

        if (mApromUri != null && mApromFileBuffer != null) {
            if (mUpdateFlag == true) {
                if (mUpdateLengthForAprom < mApromFileSize) {
                    int updateSize = UpdateAPROM(mStartAddress, mApromFileSize, mStartAddress + mUpdateLengthForAprom, mUpdateLengthForAprom, mApromFileBuffer);
                    mUpdateLengthForAprom += updateSize;
                    mUpdateLength += updateSize;
                    float fPercent = (float)mUpdateLength / (float)mTotalLength * 100.0f;
                    mPercent = (int)(Math.round(fPercent));
                    mUpdateFlag = false;

                    UpdateGUI();
                }
                else {
                    mState = STATE_TRY_TO_PROGRAM_FOR_EXFLASH;
                }
            }

            bRet = true;
        }
        else {
            mState = STATE_TRY_TO_PROGRAM_FOR_EXFLASH;
        }

        return bRet;
    }

    public boolean DoProgramForExFlash()
    {
        boolean bRet = false;

        if (mExFlashUri != null && mExFlashFileBuffer != null) {
            if (mUpdateFlag == true) {
                if (mUpdateLengthForExFlash < mExFlashFileSize) {
                    int updateSize = UpdateExFlash(mStartAddress, mExFlashFileSize, mStartAddress + mUpdateLengthForExFlash, mUpdateLengthForExFlash, mExFlashFileBuffer);
                    mUpdateLengthForExFlash += updateSize;
                    mUpdateLength += updateSize;
                    float fPercent = (float)mUpdateLength / (float)mTotalLength * 100.0f;
                    mPercent = (int)(Math.round(fPercent));
                    mUpdateFlag = false;
                }
                else {
                    mState = STATE_PROGRAM_IS_DONE;
                }

                UpdateGUI();
            }

            bRet = true;
        }
        else {
            mState = STATE_PROGRAM_IS_DONE;

            UpdateGUI();
        }

        return bRet;
    }

    public int CalCheckSum(byte[] bytes, int size)
    {
        int sum = 0;
        int i;
        short value = 0; // for unsigned char

        for (i=0; i<size; i++)
        {
            value = (short)(bytes[i] & 0xFF);
            sum += value;
        }

        return sum;
    }

    public int UpdateAPROM(int startAddr, int totalLength, int currAddr, int i, byte[] fileBuffer)
    {
        int updateLength = -1;
        int writeLength = totalLength - (currAddr - startAddr);
        byte[] Bytes = new byte[64];

        if (startAddr == currAddr) {
            if (writeLength > 64 - 16/*start_addr, total_len*/) {
                writeLength = 64 - 16;
            }

            Bytes[0] = (byte)CMD_UPDATE_APROM;
            Bytes[1] = (byte)(CMD_UPDATE_APROM >> 8);
            Bytes[2] = (byte)(CMD_UPDATE_APROM >> 16);
            Bytes[3] = (byte)(CMD_UPDATE_APROM >> 24);

            Bytes[4] = (byte)mCmdIndex;
            Bytes[5] = (byte)(mCmdIndex >> 8);
            Bytes[6] = (byte)(mCmdIndex >> 16);
            Bytes[7] = (byte)(mCmdIndex >> 24);

            Bytes[8] = (byte)startAddr;
            Bytes[9] = (byte)(startAddr >> 8);
            Bytes[10] = (byte)(startAddr >> 16);
            Bytes[11] = (byte)(startAddr >> 24);

            Bytes[12] = (byte)totalLength;
            Bytes[13] = (byte)(totalLength >> 8);
            Bytes[14] = (byte)(totalLength >> 16);
            Bytes[15] = (byte)(totalLength >> 24);

            System.arraycopy(fileBuffer, 0, Bytes, 16, writeLength);

            mCheckSum = CalCheckSum(Bytes, 64);
            ((MainActivity)getActivity()).GetHidBridge().WriteData(Bytes);
        }
        else {
            if (writeLength > 64 - 8) {
                writeLength = 64 - 8;
            }

            Bytes[0] = (byte)0x00;
            Bytes[1] = (byte)(0x00 >> 8);
            Bytes[2] = (byte)(0x00 >> 16);
            Bytes[3] = (byte)(0x00 >> 24);

            Bytes[4] = (byte)mCmdIndex;
            Bytes[5] = (byte)(mCmdIndex >> 8);
            Bytes[6] = (byte)(mCmdIndex >> 16);
            Bytes[7] = (byte)(mCmdIndex >> 24);

            System.arraycopy(fileBuffer, i, Bytes, 8, writeLength);

            mCheckSum = CalCheckSum(Bytes, 64);
            ((MainActivity)getActivity()).GetHidBridge().WriteData(Bytes);
        }

        updateLength = writeLength;

        return updateLength;
    }

    public int UpdateExFlash(int startAddr, int totalLength, int currAddr, int i, byte[] fileBuffer)
    {
        int updateLength = -1;
        int writeLength = totalLength - (currAddr - startAddr);
        byte[] Bytes = new byte[64];

        if (writeLength > 64 - 16/*start_addr, total_len*/) {
            writeLength = 64 - 16;
        }

        Bytes[0] = (byte)CMD_UPDATE_SPIFLASH;
        Bytes[1] = (byte)(CMD_UPDATE_SPIFLASH >> 8);
        Bytes[2] = (byte)(CMD_UPDATE_SPIFLASH >> 16);
        Bytes[3] = (byte)(CMD_UPDATE_SPIFLASH >> 24);

        Bytes[4] = (byte)mCmdIndex;
        Bytes[5] = (byte)(mCmdIndex >> 8);
        Bytes[6] = (byte)(mCmdIndex >> 16);
        Bytes[7] = (byte)(mCmdIndex >> 24);

        Bytes[8] = (byte)startAddr;
        Bytes[9] = (byte)(startAddr >> 8);
        Bytes[10] = (byte)(startAddr >> 16);
        Bytes[11] = (byte)(startAddr >> 24);

        Bytes[12] = (byte)writeLength;
        Bytes[13] = (byte)(writeLength >> 8);
        Bytes[14] = (byte)(writeLength >> 16);
        Bytes[15] = (byte)(writeLength >> 24);

        System.arraycopy(fileBuffer, i, Bytes, 16, writeLength);

        mCheckSum = CalCheckSum(Bytes, 64);
        ((MainActivity)getActivity()).GetHidBridge().WriteData(Bytes);

        updateLength = writeLength;

        return updateLength;
    }

    public String GetApromFileName() {
        return mApromFileName;
    }

    public void SetApromFileName(String input) {
        mApromFileName = input;
    }

    public Uri GetApromUri() {
        return mApromUri;
    }

    public void SetApromUri(Uri input) {
        mApromUri = input;
    }

    public String GetExFlashFileName() {
        return mExFlashFileName;
    }

    public void SetExFlashFileName(String input) {
        mExFlashFileName = input;
    }

    public Uri GetExFlashUri() {
        return mExFlashUri;
    }

    public void SetExFlashUri(Uri input) {
        mExFlashUri = input;
    }

    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            // Update UI

            // If there have usb connection,
            if (mbConnectFlag == true) {
                mBtnConnect.setText((String) "Disconnect");
                mTvState.setText((String)"USB Connected");
            }
            else {
                mBtnConnect.setText((String) "Connect");
                mTvState.setText((String)"USB Disconnected");
            }

            mTvDeviceName.setText((String)mNvtChipInfo.GetPartNumber());
            mTvAprom.setText((String)mApromFileName);
            mTvExFlash.setText((String)mExFlashFileName);

            mProgressBar.setProgress(mPercent);
            String text = "";
            text = Integer.toString(mPercent) + "%";
            mTvPercent.setText((String)text);

            if (mState == STATE_PROGRAM_IS_DONE)
            {
                ((MainActivity)getActivity()).ShowToastMessage("Program is done");
                mState = STATE_USB_CONNECTED;
            }
        }
    };

    public void UpdateGUI() {
        Message msg = handler.obtainMessage();
        handler.sendMessage(msg);
    }

    public void StartISPThread() {
        if (mIspThread == null) {
            mIspThread = new Thread(ispThread);
            mIspThread.start();
        }
        else {
            // Reading thread already started
        }
    }

    @SuppressWarnings("deprecation")
    public void StopISPThread() {
        if (mIspThread != null) {
            // Just kill the thread. It is better to do that fast if we need that asap.
            mIspThread.stop();
            mIspThread = null;
        } else {
            // No reading thread to stop
        }
    }

    public void SendConnect() {
        byte[] Bytes = new byte[64];

        Bytes[0] = (byte)CMD_CONNECT;
        Bytes[1] = (byte)(CMD_CONNECT >> 8);
        Bytes[2] = (byte)(CMD_CONNECT >> 16);
        Bytes[3] = (byte)(CMD_CONNECT >> 24);

        ((MainActivity)getActivity()).GetHidBridge().WriteData(Bytes);
    }

    public void SendGetDeviceID() {
        byte[] Bytes = new byte[64];

        Bytes[0] = (byte)CMD_GET_DEVICEID;
        Bytes[1] = (byte)(CMD_GET_DEVICEID >> 8);
        Bytes[2] = (byte)(CMD_GET_DEVICEID >> 16);
        Bytes[3] = (byte)(CMD_GET_DEVICEID >> 24);

        ((MainActivity)getActivity()).GetHidBridge().WriteData(Bytes);
    }

    private void RecvProc(byte[] recvData) {
        int cmd = ((recvData[3] & 0xFF) << 24 | (recvData[2] & 0xFF) << 16 | (recvData[1] & 0xFF) << 8 | (recvData[0] & 0xFF));

        switch (cmd) {
            case CMD_CONNECT:
                SendGetDeviceID();
                mState = STATE_WAITING_FOR_DEVICEID;
                break;

            case CMD_GET_DEVICEID:
                int deviceID = ((recvData[11] & 0xFF) << 24 | (recvData[10] & 0xFF) << 16 | (recvData[9] & 0xFF) << 8 | (recvData[8] & 0xFF));
                if (mNvtChipInfo.GetChipStaticInfo(deviceID)) {
                    mbConnectFlag = true;
                    mState = STATE_USB_CONNECTED;
                    UpdateGUI();
                }
                else {
                    mbConnectFlag = false;
                    mState = STATE_INIT;
                    mNvtChipInfo.SetPartNumber("Unknown");
                    UpdateGUI();
                }
                break;

            default:
                if (mCheckSum == cmd) {
                    mUpdateFlag = true;
                }
                break;
        }
    }

    private Runnable ispThread = new Runnable() {
        @Override
        public void run() {
            while (true) {
                //byte[] recvData = ((MainActivity)getActivity()).GetHidBridge().GetReceivedDataFromQueue();
                byte[] recvData = ((MainActivity)getActivity()).GetHidBridge().GetReceivedData();
                if (recvData != null) {
                    RecvProc(recvData);
                }

                switch (mState) {
                    case STATE_TRY_TO_CONNECT_USB:
                        SendConnect();
                        break;
                    case STATE_WAITING_FOR_DEVICEID:
                        break;
                    case STATE_USB_CONNECTED:
                        break;
                    case STATE_TRY_TO_PROGRAM_FOR_APROM:
                        DoProgramForAprom();
                        break;
                    case STATE_TRY_TO_PROGRAM_FOR_EXFLASH:
                        DoProgramForExFlash();
                        break;
                    default:
                        break;
                }

                //Sleep(1);
            }
        }
    };

    private void Sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}