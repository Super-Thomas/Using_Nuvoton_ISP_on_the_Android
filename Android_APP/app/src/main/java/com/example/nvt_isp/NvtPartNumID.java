package com.example.nvt_isp;

public class NvtPartNumID {
    private String mPartNumber;
    private int mID;
    private int mProjectCode;

    public NvtPartNumID(String PartNumber, int ID, int ProjectCode) {
        mPartNumber = PartNumber;
        mID = ID;
        mProjectCode = ProjectCode;
    }

    public String GetPartNumber() {
        return mPartNumber;
    }

    public int GetID() {
        return mID;
    }

    public int GetProjectCode() {
        return mProjectCode;
    }
}
