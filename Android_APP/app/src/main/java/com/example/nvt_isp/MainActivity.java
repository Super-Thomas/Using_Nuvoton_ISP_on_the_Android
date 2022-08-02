package com.example.nvt_isp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public static Context mContext;

    private HidBridge mHidBridge;

    private Fragment mUsbListFragment;
    private Fragment mNvtISPFragment;

    private static final int REQEUST_CODE_OPEN_FILE_FOR_APROM = 10;
    private static final int REQEUST_CODE_OPEN_FILE_FOR_EXFLASH = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mHidBridge = new HidBridge();
        mUsbListFragment = new UsbListFragment();
        mNvtISPFragment = new NvtISPFragment();
        SetDefaultFragment();
    }

    public HidBridge GetHidBridge() {
        return mHidBridge;
    }

    public void SetDefaultFragment()
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mUsbListFragment).commitAllowingStateLoss();
    }

    public void ShowISPFragment()
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mNvtISPFragment).commitAllowingStateLoss();
    }

    public void OpenFileDialog(boolean bTypeFlag) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (bTypeFlag == false) {
            startActivityForResult(intent, REQEUST_CODE_OPEN_FILE_FOR_APROM);
        }
        else {
            startActivityForResult(intent, REQEUST_CODE_OPEN_FILE_FOR_EXFLASH);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQEUST_CODE_OPEN_FILE_FOR_APROM && resultCode == RESULT_OK) {
            Uri uri = data.getData(); //The uri with the location of the file
            ((NvtISPFragment)mNvtISPFragment).SetApromUri(uri);
            String fileName = uri.getLastPathSegment();
            ((NvtISPFragment)mNvtISPFragment).SetApromFileName(fileName);
            ((NvtISPFragment)mNvtISPFragment).UpdateGUI();
            ((NvtISPFragment)mNvtISPFragment).ReadFileForAprom();
        }
    }

    public void ShowAlertDialog(String text) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);

        alertBuilder.setTitle("Information");
        alertBuilder.setMessage(text);
        alertBuilder.setPositiveButton("OK", null);

        alertBuilder.show();
    }
}