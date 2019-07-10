package ambow.baiwei.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


import static ambow.baiwei.weather.SplashActivity.getDbHelper;
import static java.lang.Thread.sleep;

public class SearchCity extends AppCompatActivity {
    public static final int max_city = 6;
    private SQLiteDatabase db;
    public DatabaseHelper dbHelper = getDbHelper();
    private EditText search;
    private ConstraintLayout show;
    private TextView city_title;
    private ImageView loading, back;
    private ListView search_city;
    private ConstraintLayout city_none;
    private int search_number = 50;
    public static final String search_key = "a99a512784ca44f195fc5b02ef285f9f";
    private String search_url = "https://search.heweather.net/find?" +
            "key=" + search_key +
            "&number=" + search_number +
            "&location=";
    private String top_city = "https://search.heweather.net/top?" +
            "group=cn" +
            "&key=" + search_key +
            "&number=21";
    private String weather_url = "https://api.heweather.net/s6/weather/now?" +
            "key=" + search_key +
            "&location=";
    private String forecast_url = "https://api.heweather.net/s6/weather/forecast?" +
            "key=" + search_key +
            "&location=";

    private ArrayList<HashMap<String, Object>> search_list;
    private HashMap<String, Object> search_item;
    public ArrayList<HashMap<String, Object>> top_list;
    private HashMap<String, Object> top_item;
    private int COMPLETED_SEARCH = 1;
    private int COMPLETED_SEARCH_TOP = 2;
    private Search_city_Adapter city_list;
    private ListView top_city1, top_city2, top_city3;
    private SimpleAdapter simAdapt1, simAdapt2, simAdapt3;
    private ArrayList<HashMap<String, Object>> data1 = new ArrayList<HashMap<String, Object>>();
    private ArrayList<HashMap<String, Object>> data2 = new ArrayList<HashMap<String, Object>>();
    private ArrayList<HashMap<String, Object>> data3 = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);
        db = dbHelper.getWritableDatabase();
        initview();
        init();
        loading();
    }


    public void initview() {
        search = findViewById(R.id.search_et);
        show = findViewById(R.id.show);
        loading = findViewById(R.id.loading);
        search_city = findViewById(R.id.search_city);
        city_title = findViewById(R.id.city_title);
        city_none = findViewById(R.id.city_none);
        top_city1 = findViewById(R.id.top_city1);
        top_city2 = findViewById(R.id.top_city2);
        top_city3 = findViewById(R.id.top_city3);
        back = findViewById(R.id.back);

    }

    public void loading() {
        Thread thread = new Thread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int t = 1;
                while (true) {
                    @SuppressLint("DefaultLocale") String str = "connecting_" + String.format("%02d", t++);
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

    public void init() {
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    loading.setVisibility(View.GONE);
                    city_title.setText(R.string.top_city);
                    city_title.setVisibility(View.GONE);
                    search_city.setVisibility(View.GONE);
                    show.setVisibility(View.VISIBLE);
                    city_none.setVisibility(View.GONE);
                } else {
                    loading.setVisibility(View.VISIBLE);
                    city_title.setVisibility(View.GONE);
                    city_title.setText(R.string.all_city);
                    search_city.setVisibility(View.GONE);
                    show.setVisibility(View.GONE);
                    city_none.setVisibility(View.GONE);
                }
                list(s.toString());
            }
        });
        search_list = new ArrayList<HashMap<String, Object>>();
        top_list = new ArrayList<HashMap<String, Object>>();
        city_list = new Search_city_Adapter(getApplicationContext());
        search_city.setAdapter(city_list);
        search_city.setDivider(new ColorDrawable(getResources().getColor(R.color.darkslategrey)));
        search_city.setDividerHeight(2);
        top_city1.setDividerHeight(0);
        top_city2.setDividerHeight(0);
        top_city3.setDividerHeight(0);
        search_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressLint("Recycle") Cursor c = db.rawQuery("select * from city where cid = ?",
                        new String[]{Objects.requireNonNull(search_list.get(position).get("cid")).toString()});
                @SuppressLint("Recycle") Cursor c1 = db.rawQuery("select * from city", null);
                if (c1.getCount() > max_city) {
                    Toast.makeText(SearchCity.this, R.string.hint_max_city, Toast.LENGTH_SHORT).show();
                } else if (c.getCount() == 0) {
                    db.execSQL("insert into city(cid,location,parent_city,admin_area,cnty,lat,lon,type,tz)" +
                                    " values(?,?,?,?,?,?,?,?,?)",
                            new Object[]{
                                    search_list.get(position).get("cid"),
                                    search_list.get(position).get("location"),
                                    search_list.get(position).get("parent_city"),
                                    search_list.get(position).get("admin_area"),
                                    search_list.get(position).get("cnty"),
                                    search_list.get(position).get("lat"),
                                    search_list.get(position).get("lon"),
                                    search_list.get(position).get("tz"),
                                    search_list.get(position).get("type")
                            });
                    weather(search_list.get(position).get("cid").toString());
                } else {
                    Toast.makeText(SearchCity.this, R.string.hint_exist_city, Toast.LENGTH_SHORT).show();
                }
            }
        });
        top_city1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressLint("Recycle") Cursor c = db.rawQuery("select * from city where cid = ?",
                        new String[]{Objects.requireNonNull(data1.get(position).get("cid")).toString()});
                @SuppressLint("Recycle") Cursor c1 = db.rawQuery("select * from city", null);
                if (c1.getCount() > max_city) {
                    Toast.makeText(SearchCity.this, R.string.hint_max_city, Toast.LENGTH_SHORT).show();
                } else if (c.getCount() == 0) {
                    db.execSQL("insert into city(cid,location,parent_city,admin_area,cnty,lat,lon,type,tz)" +
                                    " values(?,?,?,?,?,?,?,?,?)",
                            new Object[]{
                                    data1.get(position).get("cid"),
                                    data1.get(position).get("location"),
                                    data1.get(position).get("parent_city"),
                                    data1.get(position).get("admin_area"),
                                    data1.get(position).get("cnty"),
                                    data1.get(position).get("lat"),
                                    data1.get(position).get("lon"),
                                    data1.get(position).get("tz"),
                                    data1.get(position).get("type")
                            });
                    weather(data1.get(position).get("cid").toString());
                } else {
                    Toast.makeText(SearchCity.this, R.string.hint_exist_city, Toast.LENGTH_SHORT).show();
                }
            }
        });
        top_city2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressLint("Recycle") Cursor c = db.rawQuery("select * from city where cid = ?",
                        new String[]{Objects.requireNonNull(data2.get(position).get("cid")).toString()});
                @SuppressLint("Recycle") Cursor c1 = db.rawQuery("select * from city", null);
                if (c1.getCount() > max_city) {
                    Toast.makeText(SearchCity.this, R.string.hint_max_city, Toast.LENGTH_SHORT).show();
                } else if (c.getCount() == 0) {
                    db.execSQL("insert into city(cid,location,parent_city,admin_area,cnty,lat,lon,type,tz)" +
                                    " values(?,?,?,?,?,?,?,?,?)",
                            new Object[]{
                                    data2.get(position).get("cid"),
                                    data2.get(position).get("location"),
                                    data2.get(position).get("parent_city"),
                                    data2.get(position).get("admin_area"),
                                    data2.get(position).get("cnty"),
                                    data2.get(position).get("lat"),
                                    data2.get(position).get("lon"),
                                    data2.get(position).get("tz"),
                                    data2.get(position).get("type")
                            });
                    weather(data2.get(position).get("cid").toString());
                } else {
                    Toast.makeText(SearchCity.this, R.string.hint_exist_city, Toast.LENGTH_SHORT).show();
                }
            }
        });
        top_city3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressLint("Recycle") Cursor c = db.rawQuery("select * from city where cid = ?",
                        new String[]{Objects.requireNonNull(data3.get(position).get("cid")).toString()});
                @SuppressLint("Recycle") Cursor c1 = db.rawQuery("select * from city", null);
                if (c1.getCount() > max_city) {
                    Toast.makeText(SearchCity.this, R.string.hint_max_city, Toast.LENGTH_SHORT).show();
                } else if (c.getCount() == 0) {
                    db.execSQL("insert into city(cid,location,parent_city,admin_area,cnty,lat,lon,type,tz)" +
                                    " values(?,?,?,?,?,?,?,?,?)",
                            new Object[]{
                                    data3.get(position).get("cid"),
                                    data3.get(position).get("location"),
                                    data3.get(position).get("parent_city"),
                                    data3.get(position).get("admin_area"),
                                    data3.get(position).get("cnty"),
                                    data3.get(position).get("lat"),
                                    data3.get(position).get("lon"),
                                    data3.get(position).get("tz"),
                                    data3.get(position).get("type")
                            });
                    weather(data3.get(position).get("cid").toString());
                } else {
                    Toast.makeText(SearchCity.this, R.string.hint_exist_city, Toast.LENGTH_SHORT).show();
                }
            }
        });
        loading.setVisibility(View.VISIBLE);
        city_title.setVisibility(View.GONE);
        city_title.setText(R.string.top_city);
        simAdapt1 = new SimpleAdapter(
                this,
                data1,
                R.layout.top_city,
                new String[]{"location"},
                new int[]{R.id.item_top_city});

        top_city1.setAdapter(simAdapt1);
        simAdapt2 = new SimpleAdapter(
                this,
                data2,
                R.layout.top_city,
                new String[]{"location"},
                new int[]{R.id.item_top_city});

        top_city2.setAdapter(simAdapt2);
        simAdapt3 = new SimpleAdapter(
                this,
                data3,
                R.layout.top_city,
                new String[]{"location"},
                new int[]{R.id.item_top_city});

        top_city3.setAdapter(simAdapt3);
        top_city();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(1106, intent);
                finish();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED_SEARCH) {
                if (msg.getData().getBoolean("flag")) {
                    city_list.notifyDataSetChanged();
                    loading.setVisibility(View.GONE);
                    city_title.setVisibility(View.VISIBLE);
                    city_title.setText(R.string.all_city);
                    search_city.setVisibility(View.VISIBLE);
                    show.setVisibility(View.GONE);
                    city_none.setVisibility(View.GONE);
                } else {
                    if (search.getText().toString().trim().length() != 0) {
                        loading.setVisibility(View.GONE);
                        city_title.setVisibility(View.VISIBLE);
                        city_title.setText(R.string.all_city);
                        search_city.setVisibility(View.GONE);
                        show.setVisibility(View.GONE);
                        city_none.setVisibility(View.VISIBLE);
                    } else {
                        loading.setVisibility(View.GONE);
                        city_title.setText(R.string.top_city);
                        city_title.setVisibility(View.VISIBLE);
                        search_city.setVisibility(View.GONE);
                        show.setVisibility(View.VISIBLE);
                        city_none.setVisibility(View.GONE);
                    }
                }
            } else if (msg.what == COMPLETED_SEARCH_TOP) {
                if (msg.getData().getBoolean("flag")) {
                    simAdapt1.notifyDataSetChanged();
                    simAdapt2.notifyDataSetChanged();
                    simAdapt3.notifyDataSetChanged();
                    loading.setVisibility(View.GONE);
                    city_title.setText(R.string.top_city);
                    city_title.setVisibility(View.VISIBLE);
                    search_city.setVisibility(View.GONE);
                    show.setVisibility(View.VISIBLE);
                    city_none.setVisibility(View.GONE);
                }
            }

            if(msg.what==33){
                Toast.makeText(SearchCity.this, R.string.hint_success_city, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(1106, intent);
                finish();
            }
        }
    };

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
                                Log.e("db", "onResponse: OK" );

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
                        message.what = 33;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("flag", flag);
                        message.setData(bundle);
                        handler.sendMessage(message);

                    }
                });
            }
        }.start();
    }
    private void top_city() {
        new Thread() {
            @Override
            public void run() {
                NetUtils.sendRequestWithOkhttp(top_city, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        String responseData = null;
                        boolean flag = false;
                        try {
                            top_list.clear();
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
                                    top_item = new HashMap<String, Object>();
                                    top_item.put("cid", json_city_item.getString("cid"));
                                    top_item.put("location", json_city_item.getString("location"));
                                    top_item.put("parent_city", json_city_item.getString("parent_city"));
                                    top_item.put("admin_area", json_city_item.getString("admin_area"));
                                    top_item.put("cnty", json_city_item.getString("cnty"));
                                    top_item.put("lat", json_city_item.getString("lat"));
                                    top_item.put("lon", json_city_item.getString("lon"));
                                    top_item.put("tz", json_city_item.getString("tz"));
                                    top_item.put("type", json_city_item.getString("type"));
                                    top_list.add(top_item);
                                    if (i % 3 == 0) {
                                        data1.add(top_item);
                                    } else if (i % 3 == 1) {
                                        data2.add(top_item);
                                    } else if (i % 3 == 2) {
                                        data3.add(top_item);
                                    }
                                }
                            } else {
                                flag = false;
                                Log.d("status", "ERROR");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("json", e.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("json", e.toString());
                        }
                        Message message = new Message();
                        message.what = COMPLETED_SEARCH_TOP;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("flag", flag);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                });
            }
        }.start();
    }

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
                        Message message = new Message();
                        message.what = COMPLETED_SEARCH;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("flag", flag);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                });
            }
        }.start();
    }


    class Search_city_Adapter extends BaseAdapter {
        private Context context = null;

        public Search_city_Adapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return search_list.size();
        }

        @Override
        public Object getItem(int position) {
            return search_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mHolder;
            if (convertView == null) {
                mHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.search_item, null, true);
                mHolder.city_item_main = (TextView) convertView.findViewById(R.id.item_city);
                mHolder.city_item_other = (TextView) convertView.findViewById(R.id.item_other);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }
            String location = "";
            String other = "";
            if (search_list.size() <= 0) {

            } else {
                location = Objects.requireNonNull(search_list.get(position).get("location")).toString();
                String parent_city, admin_area, cnty;
                parent_city = Objects.requireNonNull(search_list.get(position).get("parent_city")).toString();
                admin_area = Objects.requireNonNull(search_list.get(position).get("admin_area")).toString();
                cnty = Objects.requireNonNull(search_list.get(position).get("cnty")).toString();
                if (parent_city.equals(admin_area)) {
                    if (location.equals(admin_area)) {
                        other = "，" + cnty;
                    } else {
                        other = "，" + parent_city + "，" + cnty;
                    }
                } else {
                    if (location.equals(parent_city)) {
                        other = "，" + admin_area + "，" + cnty;
                    } else {
                        other = "，" + parent_city + "，" + admin_area + "，" + cnty;
                    }
                }
                if (location.length() + other.length() > 20) {
                    other = "，" + parent_city + "，" + cnty;
                }
                if (location.length() + other.length() > 20) {
                    other = "，" + cnty;
                }
            }

            mHolder.city_item_main.setText(location);
            mHolder.city_item_other.setText(other);
            return convertView;
        }

        class ViewHolder {
            private TextView city_item_main;
            private TextView city_item_other;
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(1106, intent);
        finish();
    }
}