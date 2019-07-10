package ambow.baiwei.weather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String DB_NAME="weather.db";
    public static final int VERSION=1;
    public static final String TABLE_NAME="Diary";
    public static final String CREATE_CITY="" +
            "create table city(" +
            "id integer primary key autoincrement," +
            "cid text ," +
            "location text," +
            "parent_city text," +
            "admin_area text," +
            "cnty text," +
            "lat text," +
            "lon text," +
            "type text," +
            "tz text)";

    public static final String CREATE_WEATHER_NOW="" +
            "create table weather_now(" +
            "id integer primary key autoincrement," +
            "cid text ," +
            "update_loc text," +
            "fl text," +
            "tmp text," +
            "cond_code text," +
            "cond_txt text," +
            "wind_deg text," +
            "wind_dir text," +
            "wind_sc text," +
            "wind_spd text," +
            "hum text," +
            "pcpn text," +
            "pres text," +
            "vis text," +
            "cloud text)";

    public static final String CREATE_WEATHER_forecast="" +
            "create table forecast(" +
            "id integer primary key autoincrement," +
            "cid text ," +
            "date text," +
            "tmp_max text," +
            "tmp_min text," +
            "cond_txt_d text)";


//    fl	体感温度，默认单位：摄氏度	23
//    tmp	温度，默认单位：摄氏度	21
//    cond_code	实况天气状况代码	100
//    cond_txt	实况天气状况描述	晴
//    wind_deg	风向360角度	305
//    wind_dir	风向	西北
//    wind_sc	风力	3-4
//    wind_spd	风速，公里/小时	15
//    hum	相对湿度	40
//    pcpn	降水量	0
//    pres	大气压强	1020
//    vis	能见度，默认单位：公里	10
//    cloud	云量	23

    private Context mContext;

    public DatabaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext=context;

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_WEATHER_NOW);
        db.execSQL(CREATE_WEATHER_forecast);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
