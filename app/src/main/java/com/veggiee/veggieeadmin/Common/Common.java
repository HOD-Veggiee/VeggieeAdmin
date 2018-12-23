package com.veggiee.veggieeadmin.Common;

import com.veggiee.veggieeadmin.Model.Staff;
import com.veggiee.veggieeadmin.Remote.APIService;
import com.veggiee.veggieeadmin.Remote.RetrofitClient;

public class Common {

    public static Staff currentStaff;
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final int PICK_IMAGE_REQUEST = 71;

    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static String PHONE_TEXT = "userPhone";

    public static String convertCodeToStatus(String code)
    {
        switch (code)
        {
            case "0":
                return "Order placed.";

            case "1":
                return "In Process.";

            case "2":
                return "On way.";

            default:
                return "In Process";
        }
    }

    public static APIService getFCMClient()
    {
        return RetrofitClient.getClient(fcmURL).create(APIService.class);
    }
}
