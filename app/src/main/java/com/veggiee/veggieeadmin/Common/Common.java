package com.veggiee.veggieeadmin.Common;

import com.veggiee.veggieeadmin.Model.Staff;

public class Common {

    public static Staff currentStaff;
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final int PICK_IMAGE_REQUEST = 71;

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
}
