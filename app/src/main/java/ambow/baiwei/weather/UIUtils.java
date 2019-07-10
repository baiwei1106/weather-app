package ambow.baiwei.weather;

import android.content.Context;
import android.content.SharedPreferences;

public class UIUtils {

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static int dp2Px(int dp) {
        float density = mContext.getResources().getDisplayMetrics().density;
        int px = (int) (dp * density + .5f);
        return px;
    }

    public static int px2Dp(int px) {
        float density = mContext.getResources().getDisplayMetrics().density;
        int dp = (int) (px / density + .5f);
        return dp;
    }

    private static String GUIDE_NAME = "guide";

    public void setguide(Context context) {
        SharedPreferences sp = context.getSharedPreferences(GUIDE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("flag", true);
        editor.apply();
    }

    public boolean getguide(Context context) {
        SharedPreferences sp = context.getSharedPreferences(GUIDE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean("flag", false);
    }

    private static String LOCATION_NAME = "location";

    public void setlocation(Context context, String loc) {
        SharedPreferences sp = context.getSharedPreferences(LOCATION_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("location", loc);
        editor.apply();
    }

    public String getlocation(Context context) {
        SharedPreferences sp = context.getSharedPreferences(LOCATION_NAME, Context.MODE_PRIVATE);
        return sp.getString("location", "UNKNOWON");
    }

}
