package com.netease.cloudmusic.datareport;

import org.json.JSONArray;
import org.json.JSONException;

public class DataReportUtils {

    public static String getSubMenuId(int menuId) {
        return "sub:" + menuId;
    }

    public static String buildCompleteRefers(String eventRefer, String multiRefer) {
        try {
            JSONArray jsonArray = new JSONArray(multiRefer);
            if (eventRefer == null) {
                eventRefer = "";
            }
            StringBuilder sb = new StringBuilder(eventRefer);
            for (int i = 0; i < jsonArray.length(); i++) {
                sb.append(",").append(jsonArray.getString(i));
            }
            if (sb.length() == 0) {
                return ",";
            } else {
                return sb.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return ",";
        }
    }

}
