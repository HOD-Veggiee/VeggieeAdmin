package com.veggiee.veggieeadmin.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.veggiee.veggieeadmin.Model.Staff;
import com.veggiee.veggieeadmin.Remote.APIService;
import com.veggiee.veggieeadmin.Remote.RetrofitClient;

public class Common {

    public static Staff currentStaff;
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static final int PICK_IMAGE_REQUEST = 71;

    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static String PHONE_TEXT = "userPhone";

    public static String convertCodeToStatus(String code)
    {
        switch (code)
        {
            case "0":
                return "Pending";

            case "1":
                return "Preparing";

            case "2":
                return "On it's way";

            case "3":
                return "Completed";

            default:
                return "Pending";
        }
    }

    public static String convertCodeToRoll(int code)
    {
        switch (code)
        {
            case 0:
                return "staff";

            case 1:
                return "admin";

            default:
                return "staff";
        }
    }

    public static APIService getFCMClient()
    {
        return RetrofitClient.getClient(fcmURL).create(APIService.class);
    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();

            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
