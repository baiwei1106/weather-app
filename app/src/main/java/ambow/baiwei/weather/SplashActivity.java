package ambow.baiwei.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static ambow.baiwei.weather.DatabaseHelper.DB_NAME;
import static ambow.baiwei.weather.DatabaseHelper.VERSION;
import static ambow.baiwei.weather.SearchCity.max_city;
import static ambow.baiwei.weather.SearchCity.search_key;
import static java.lang.Thread.sleep;

public class SplashActivity extends AppCompatActivity {
    public static DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private boolean debug = true;
    private Button skip;
    private int sum_second = 200;
    private ImageView loading;
    private String skip_t, remain_t, sec_t;
    private CountDownTimer timer;
    private Intent intent_service;
    private ArrayList<HashMap<String, Object>> search_list;
    private HashMap<String, Object> search_item;
    private String search_url = "https://search.heweather.net/find?" +
            "key=" + search_key +
            "&location=";
    private String weather_url = "https://api.heweather.net/s6/weather/now?" +
            "key=" + search_key +
            "&location=";
    private String forecast_url = "https://api.heweather.net/s6/weather/forecast?" +
            "key=" + search_key +
            "&location=";
    private boolean flag = true;
    public static DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    private void init() {
        loading = findViewById(R.id.loading_sp);
        loading();
        db = dbHelper.getWritableDatabase();
        search_list = new ArrayList<HashMap<String, Object>>();
        skip = findViewById(R.id.skip_bt);
        skip_t = getResources().getString(R.string.skip);
        remain_t = getResources().getString(R.string.remain);
        sec_t = getResources().getString(R.string.sec);
        final Button start = findViewById(R.id.start);
//        skip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                start();
//            }
//        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                new UIUtils().setguide(SplashActivity.this);
                if(flag){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            intent_service = new Intent(getApplicationContext(), AmapLocation.class);
                            intent_service.putExtra("messenger", new Messenger(handler));
                            startService(intent_service);
                        }
                    });
                }else{
                    start();
                }

            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setTitle("权限申请");
        builder.setMessage("为了软件正常运行需要使用储存、定位权限，是否确认？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verifyStoragePermissions(SplashActivity.this);
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                flag=false;
            }
        });
        if (!new UIUtils().getguide(this)) {
            builder.show();
        }
    }

    private void start() {
//        timer.cancel();
        dump();
    }

    private void dump() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    private void timer() {
        timer = new CountDownTimer(sum_second * 1000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long minute = ((millisUntilFinished / 1000) / 60);
                long second = ((millisUntilFinished / 1000) % 60) + minute * 60;
                skip.setText(skip_t + " " + remain_t + String.valueOf(second + 1) + sec_t);
            }

            @Override
            public void onFinish() {
                dump();
            }
        };
        timer.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        dbHelper = new DatabaseHelper(SplashActivity.this, DB_NAME, null, VERSION);
        dbHelper.getWritableDatabase();
        if (new UIUtils().getguide(this)) {
            sum_second = 0;
            start();
        }
        init();
//        timer();

    }

    private boolean checkGpsIsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isOpen;
    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.RECORD_AUDIO

    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    list(msg.getData().getString("City"));
                    stopService(intent_service);
                    break;
                case 2:
                    if (msg.getData().getBoolean("flag") && search_list.size() > 0) {

                        db.execSQL("insert into city(cid,location,parent_city,admin_area,cnty,lat,lon,type,tz)" +
                                        " values(?,?,?,?,?,?,?,?,?)",
                                new Object[]{
                                        search_list.get(0).get("cid"),
                                        search_list.get(0).get("location"),
                                        search_list.get(0).get("parent_city"),
                                        search_list.get(0).get("admin_area"),
                                        search_list.get(0).get("cnty"),
                                        search_list.get(0).get("lat"),
                                        search_list.get(0).get("lon"),
                                        search_list.get(0).get("tz"),
                                        search_list.get(0).get("type")
                                });
                    }
                    Cursor c1 = db.rawQuery("select * from city", null);
                    while (c1.moveToNext()) {
                        Log.e("location", c1.getString(c1.getColumnIndex("location")));
                    }
                    new UIUtils().setlocation(SplashActivity.this, search_list.get(0).get("cid").toString());
                    start();
                    break;
            }
        }
    };


    private void list(final String location) {
        new Thread() {
            @Override
            public void run() {
                NetUtils.sendRequestWithOkhttp(search_url + location, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        String loc = "";
                        String responseData = null;
                        boolean flag = false;
                        try {
                            search_list.clear();
                            responseData = response.body().string();
                            JSONObject HeWeather6 = new JSONObject(responseData);
                            JSONArray jsonArray = HeWeather6.getJSONArray("HeWeather6");
                            JSONObject jsonmain = jsonArray.getJSONObject(0);
                            String status = jsonmain.getString("status");
                            if (status.equals("ok")) {
                                flag = true;
                                Log.d("status", "OK");
                                JSONArray json_city_array = jsonmain.getJSONArray("basic");
                                for (int i = 0; i < json_city_array.length(); i++) {
                                    JSONObject json_city_item = json_city_array.getJSONObject(i);
                                    loc = json_city_item.getString("cid");
                                    search_item = new HashMap<String, Object>();
                                    search_item.put("cid", json_city_item.getString("cid"));
                                    search_item.put("location", json_city_item.getString("location"));
                                    search_item.put("parent_city", json_city_item.getString("parent_city"));
                                    search_item.put("admin_area", json_city_item.getString("admin_area"));
                                    search_item.put("cnty", json_city_item.getString("cnty"));
                                    search_item.put("lat", json_city_item.getString("lat"));
                                    search_item.put("lon", json_city_item.getString("lon"));
                                    search_item.put("tz", json_city_item.getString("tz"));
                                    search_item.put("type", json_city_item.getString("type"));
                                    search_list.add(search_item);
                                }
                            } else {
                                flag = false;
                                Log.d("status", "ERROR");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("json", e.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("json", e.toString());
                        }
                        weather(loc);
                        Log.e("weather", loc);
                    }
                });
            }
        }.start();
    }

    private void weather(final String location) {
        new Thread() {
            @Override
            public void run() {
                NetUtils.sendRequestWithOkhttp(weather_url + location, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        String responseData = null;
                        boolean flag = false;
                        try {
                            responseData = response.body().string();
                            JSONObject HeWeather6 = new JSONObject(responseData);
                            JSONArray jsonArray = HeWeather6.getJSONArray("HeWeather6");
                            JSONObject jsonmain = jsonArray.getJSONObject(0);
                            String status = jsonmain.getString("status");
                            if (status.equals("ok")) {
                                flag = true;
                                Log.d("status", "OK");
                                JSONObject json_city_item = jsonmain.getJSONObject("now");
                                JSONObject json_time_item = jsonmain.getJSONObject("update");

                                db.execSQL("insert into weather_now(cid,update_loc,fl,tmp," +
                                                "cond_code,cond_txt,wind_deg,wind_dir,wind_sc," +
                                                "wind_spd,hum,pcpn,pres,vis,cloud)" +
                                                " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                        new Object[]{
                                                location,
                                                json_time_item.getString("loc"),
                                                json_city_item.getString("fl"),
                                                json_city_item.getString("tmp"),
                                                json_city_item.getString("cond_code"),
                                                json_city_item.getString("cond_txt"),
                                                json_city_item.getString("wind_deg"),
                                                json_city_item.getString("wind_dir"),
                                                json_city_item.getString("wind_sc"),
                                                json_city_item.getString("wind_spd"),
                                                json_city_item.getString("hum"),
                                                json_city_item.getString("pcpn"),
                                                json_city_item.getString("pres"),
                                                json_city_item.getString("vis"),
                                                json_city_item.getString("cloud")

                                        });
                                Log.e("db", "onResponse: OK");

                            } else {
                                flag = false;
                                Log.d("status", "ERROR");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("json", e.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("json", e.toString());
                        }
                        forecast(location);
                    }
                });
            }
        }.start();
    }

    private void forecast(final String location) {
        new Thread() {
            @Override
            public void run() {
                NetUtils.sendRequestWithOkhttp(forecast_url + location, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        String responseData = null;
                        boolean flag = false;
                        try {
                            responseData = response.body().string();
                            JSONObject HeWeather6 = new JSONObject(responseData);
                            JSONArray jsonArray = HeWeather6.getJSONArray("HeWeather6");
                            JSONObject jsonmain = jsonArray.getJSONObject(0);
                            String status = jsonmain.getString("status");
                            if (status.equals("ok")) {
                                flag = true;
                                Log.d("status", "OK");
                                JSONArray json_city_list = jsonmain.getJSONArray("daily_forecast");
                                for (int i = 0; i < json_city_list.length(); i++) {
                                    JSONObject json_city_item = json_city_list.getJSONObject(i);
                                    db.execSQL("insert into forecast(cid,date,tmp_max,tmp_min,cond_txt_d)" +
                                                    " values(?,?,?,?,?)",
                                            new Object[]{
                                                    location,
                                                    json_city_item.getString("date"),
                                                    json_city_item.getString("tmp_max"),
                                                    json_city_item.getString("tmp_min"),
                                                    json_city_item.getString("cond_txt_d")
                                            });
                                }


                                Log.e("db112", "baiwei OK");

                            } else {
                                flag = false;
                                Log.d("status", "ERROR");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("json", e.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("json", e.toString());
                        }
                        Message message = new Message();
                        message.what = 2;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("flag", flag);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                });
            }
        }.start();
    }

    public void loading() {
        Thread thread = new Thread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int t = 1;
                while (true) {
                    String str = "connecting_" + String.format("%02d", t++);
                    if (t == 60) t = 1;
                    int resid = getResources().getIdentifier(str, "drawable", getPackageName());
                    loading.setImageResource(resid);
                    try {
                        sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
