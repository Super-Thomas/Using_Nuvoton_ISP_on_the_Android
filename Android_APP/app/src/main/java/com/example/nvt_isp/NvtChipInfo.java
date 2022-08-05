package com.example.nvt_isp;

public class NvtChipInfo {
    // Number of supported devices
    private static final int MAX_DEVICE_COUNT = 31;
    private NvtPartNumID[] mNvtPartNumID;

    // Static Info.
    private int mID;
    private int mSeriesCode;
    private String mPartNumber;

    // Series code from ISP Tool
    private static final int ISD_94000_SERIES = 0x100001;
    private static final int ISD_91200_SERIES = 0x100002; // I91200/N573
    private static final int ISD_9160_SERIES = 0x100003; // I9160/N575
    private static final int ISD_91300_SERIES = 0x100004;
    private static final int ISD_91000_SERIES = 0x100005;
    private static final int NPCx_SERIES = 0x100006;
    private static final int ISD_96000_SERIES = 0x100007;
    private static final int IDD_DIALOG_CONFIGURATION_MINI51CN = 5085;
    private static final int IDD_DIALOG_CONFIGURATION_NANO100BN = 144;

    public NvtChipInfo() {
        SupportDevicesList();
    }

    // Make table for searching devices
    public void SupportDevicesList()
    {
        int i = 0;
        mNvtPartNumID = new NvtPartNumID[MAX_DEVICE_COUNT];

        mNvtPartNumID[i++] = new NvtPartNumID("I94133A", 0x1D010588, ISD_94000_SERIES);
        mNvtPartNumID[i++] = new NvtPartNumID("I91230G", 0x1D0A0463, ISD_91200_SERIES);
        mNvtPartNumID[i++] = new NvtPartNumID("ISD9130", 0x1D060163, ISD_9160_SERIES);
        mNvtPartNumID[i++] = new NvtPartNumID("I91361", 0x1D010284, ISD_91300_SERIES);
        mNvtPartNumID[i++] = new NvtPartNumID("I91032F", 0x1D010362, ISD_91000_SERIES);
        mNvtPartNumID[i++] = new NvtPartNumID("I96100", 0x1D010800, ISD_96000_SERIES);
        mNvtPartNumID[i++] = new NvtPartNumID("I94124C", 0x1D0705BA, NPCx_SERIES);

        /* MINI51DE */
        mNvtPartNumID[i++] = new NvtPartNumID("MINI51LDE", 0x20205100, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI51QDE", 0x20205101, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI51ZDE", 0x20205103, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI51TDE", 0x20205104, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI51FDE", 0x20205105, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI52LDE", 0x20205200, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI52QDE", 0x20205201, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI52ZDE", 0x20205203, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI52TDE", 0x20205204, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI52FDE", 0x20205205, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI54LDE", 0x20205400, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI54QDE", 0x20205401, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI54ZDE", 0x20205403, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI54TDE", 0x20205404, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI54FDE", 0x20205405, IDD_DIALOG_CONFIGURATION_MINI51CN);
        mNvtPartNumID[i++] = new NvtPartNumID("MINI54FHC", 0x20205406, IDD_DIALOG_CONFIGURATION_MINI51CN);

        // Nano130
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130KE3BN", 0x00113030, IDD_DIALOG_CONFIGURATION_NANO100BN);
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130SE3BN", 0x00113034, IDD_DIALOG_CONFIGURATION_NANO100BN);
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130KD3BN", 0x00113038, IDD_DIALOG_CONFIGURATION_NANO100BN);
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130KD2BN", 0x00113039, IDD_DIALOG_CONFIGURATION_NANO100BN);
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130SD3BN", 0x0011303C, IDD_DIALOG_CONFIGURATION_NANO100BN);
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130SD2BN", 0x0011303D, IDD_DIALOG_CONFIGURATION_NANO100BN);
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130KC2BN", 0x00113040, IDD_DIALOG_CONFIGURATION_NANO100BN);
        mNvtPartNumID[i++] = new NvtPartNumID("Nano130SC2BN", 0x00113042, IDD_DIALOG_CONFIGURATION_NANO100BN);
    }

    public boolean GetChipStaticInfo(int ID)
    {
        boolean bRet = false;
        int i;

        for (i=0; i<MAX_DEVICE_COUNT; i++)
        {
            if (mNvtPartNumID[i].GetID() == ID) {
                mID = mNvtPartNumID[i].GetID();
                mSeriesCode = mNvtPartNumID[i].GetProjectCode();
                mPartNumber = mNvtPartNumID[i].GetPartNumber();
                bRet = true;
                break;
            }
        }

        return bRet;
    }

    public String GetPartNumber() {
        return mPartNumber;
    }

    public void SetPartNumber(String input) {
        mPartNumber = input;
    }
}
