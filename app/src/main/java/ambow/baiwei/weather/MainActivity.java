package ambow.baiwei.weather;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.czp.library.ArcProgress;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static ambow.baiwei.weather.SplashActivity.getDbHelper;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private ImageView citylist, setting, locationimg;
    private ViewPager viewPager;
    public DatabaseHelper dbHelper = getDbHelper();
    private SQLiteDatabase db;
    private ArrayList<HashMap<String, Object>> city_lists;
    private HashMap<String, Object> city_item;
    private MyPagerAdapter pagerAdapter;
    private TextView citytitle;
    private View mGuideRedPoint;
    private LinearLayout mLlGuidePoints;
    private int disPoints;
    private ConstraintLayout bg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initview();
        click();
        setap();
    }

    private void initview() {
        db = dbHelper.getWritableDatabase();
        city_lists = new ArrayList<HashMap<String, Object>>();
        citylist = findViewById(R.id.city_list_main);
        setting = findViewById(R.id.setting);
        viewPager = findViewById(R.id.viewpager);
        citytitle = findViewById(R.id.city_title);
        locationimg = findViewById(R.id.location_img_main);
        mGuideRedPoint = findViewById(R.id.v_guide_redpoint);
        mLlGuidePoints = findViewById(R.id.ll_guide_points);
        bg = findViewById(R.id.bg);
    }

    private void click() {
//        mGuideRedPoint.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mGuideRedPoint.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                disPoints = (mLlGuidePoints.getChildAt(1).getLeft() - mLlGuidePoints.getChildAt(0).getLeft());
//            }
//        });

        citylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityListActivity.class);
                startActivityForResult(intent, 1106);
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, settingActivity.class);
                startActivity(intent);
            }
        });

    }

    private void dbselect() {
        Cursor c = db.rawQuery("select * from city", null);
        city_lists.clear();
        if (c.moveToFirst()) {
            do {

                city_item = new HashMap<String, Object>();
                Cursor c1 = db.rawQuery("select * from weather_now where cid=?", new String[]{
                        c.getString(c.getColumnIndex("cid"))
                });
                if (c1.moveToFirst()) {
                    do {
                        city_item.put("update_loc", c1.getString(c1.getColumnIndex("update_loc")));
                        city_item.put("fl", c1.getString(c1.getColumnIndex("fl")));
                        city_item.put("tmp", c1.getString(c1.getColumnIndex("tmp")));
                        city_item.put("cond_code", c1.getString(c1.getColumnIndex("cond_code")));
                        city_item.put("cond_txt", c1.getString(c1.getColumnIndex("cond_txt")));
                        city_item.put("wind_deg", c1.getString(c1.getColumnIndex("wind_deg")));
                        city_item.put("wind_dir", c1.getString(c1.getColumnIndex("wind_dir")));
                        city_item.put("wind_sc", c1.getString(c1.getColumnIndex("wind_sc")));
                        city_item.put("wind_spd", c1.getString(c1.getColumnIndex("wind_spd")));
                        city_item.put("hum", c1.getString(c1.getColumnIndex("hum")));
                        city_item.put("pcpn", c1.getString(c1.getColumnIndex("pcpn")));
                        city_item.put("pres", c1.getString(c1.getColumnIndex("pres")));
                        city_item.put("vis", c1.getString(c1.getColumnIndex("vis")));
                        city_item.put("cloud", c1.getString(c1.getColumnIndex("cloud")));
                    } while (c1.moveToNext());
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date d1 = new Date();
                String t1 = format.format(d1);
                c1 = db.rawQuery("select * from forecast where date >= ? AND cid=? order by date ASC limit 6",
                        new String[]{
                                t1,
                                c.getString(c.getColumnIndex("cid"))
                        });
                int i = 0;
                if (c1.moveToFirst()) {
                    do {
                        city_item.put("date" + i, c1.getString(c1.getColumnIndex("date")));
                        city_item.put("tmp_max" + i, c1.getString(c1.getColumnIndex("tmp_max")));
                        city_item.put("tmp_min" + i, c1.getString(c1.getColumnIndex("tmp_min")));
                        city_item.put("cond_txt_d" + i, c1.getString(c1.getColumnIndex("cond_txt_d")));
                        i++;
                    } while (c1.moveToNext());
                }
                String cid = c.getString(c.getColumnIndex("cid"));
                String location = c.getString(c.getColumnIndex("location"));
                String parent_city = c.getString(c.getColumnIndex("parent_city"));
                String admin_area = c.getString(c.getColumnIndex("admin_area"));
                String cnty = c.getString(c.getColumnIndex("cnty"));
                String lat = c.getString(c.getColumnIndex("lat"));
                String lon = c.getString(c.getColumnIndex("lon"));
                String tz = c.getString(c.getColumnIndex("tz"));
                String type = c.getString(c.getColumnIndex("type"));
                city_item.put("cid", cid);
                city_item.put("location", location);
                city_item.put("parent_city", parent_city);
                city_item.put("admin_area", admin_area);
                city_item.put("cnty", cnty);
                city_item.put("lat", lat);
                city_item.put("lon", lon);
                city_item.put("tz", tz);
                city_item.put("type", type);
                city_lists.add(city_item);
            } while (c.moveToNext());
        }
    }

    private void setap() {
        dbselect();
        if (city_lists.size() > 0) {
            citytitle.setText(city_lists.get(0).get("location").toString());
        }
        UIUtils.init(this);
        point(city_lists.size());
        pagerAdapter = new MyPagerAdapter(this, city_lists);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(WeatherOnPageChangeListener);
    }

    private void point(int number) {
        if (number == 1) {
            citytitle.setText(city_lists.get(0).get("location").toString());
            if (new UIUtils().getlocation(MainActivity.this)
                    .equals(city_lists.get(0).get("cid").toString())) {
                locationimg.setVisibility(View.VISIBLE);
            } else {
                locationimg.setVisibility(View.GONE);
            }
        }
        mLlGuidePoints.removeAllViews();
        if (number > 1) {
            mGuideRedPoint.setVisibility(View.VISIBLE);
            while (number-- > 0) {
                View v_point = new View(getApplicationContext());
                v_point.setBackgroundResource(R.drawable.point_smiple);
                int dip = 4;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UIUtils.dp2Px(dip), UIUtils.dp2Px(dip));
                params.leftMargin = UIUtils.dp2Px(3);
                params.rightMargin = UIUtils.dp2Px(3);
                v_point.setLayoutParams(params);
                mLlGuidePoints.addView(v_point);
            }
        } else {
            mGuideRedPoint.setVisibility(View.GONE);
        }
    }

    private void select_point(int position) {
        float leftMargin = UIUtils.dp2Px(10) * (position) + UIUtils.dp2Px(3);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mGuideRedPoint.getLayoutParams();
        layoutParams.leftMargin = Math.round(leftMargin);
        mGuideRedPoint.setLayoutParams(layoutParams);
    }

    private ViewPager.OnPageChangeListener WeatherOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            select_point(i);
            citytitle.setText(city_lists.get(i).get("location").toString());
            setupbg(city_lists.get(i).get("cond_code").toString());
            if (new UIUtils().getlocation(MainActivity.this)
                    .equals(city_lists.get(i).get("cid").toString())) {
                locationimg.setVisibility(View.VISIBLE);
            } else {
                locationimg.setVisibility(View.GONE);
            }

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private void setupbg(String code) {
        int ss;
        try {
            ss = Integer.parseInt(code);
        } catch (Exception e) {
            ss = 100;
        }

        final int finalSs = ss;
        new Thread() {
            @Override
            public void run() {
                if (finalSs == 100) {
                    bg.setBackgroundResource(R.drawable.bg_sunny);
                } else if (finalSs < 200 && finalSs > 100) {
                    bg.setBackgroundResource(R.drawable.bg_cloudy);
                } else if (finalSs >= 200 && finalSs < 300) {
                    bg.setBackgroundResource(R.drawable.bg_windy);
                } else if (finalSs >= 300 && finalSs < 400) {
                    bg.setBackgroundResource(R.drawable.bg_rain);
                } else if (finalSs >= 400 && finalSs < 500) {
                    bg.setBackgroundResource(R.drawable.bg_snow);
                } else {
                    bg.setBackgroundResource(R.drawable.bg_fog);
                }
            }
        }.start();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1106) {
            if (resultCode == 1106) {
                dbselect();
                if (city_lists.size() <= 1) {
                    if (city_lists.size() == 1)
                        setupbg(city_lists.get(0).get("cond_code").toString());
                    pagerAdapter = new MyPagerAdapter(this, city_lists);
                    viewPager.setAdapter(pagerAdapter);
                }
                if (city_lists.size() == 0) {
                    citytitle.setText(R.string.nano_city);
                    bg.setBackgroundResource(R.drawable.bg_sunny);
                }
                pagerAdapter.notifyDataSetChanged();
                int position = data.getIntExtra("item", -1);
                if (position != -1) {
                    viewPager.setCurrentItem(position);
                }
                point(city_lists.size());

            }
            if (resultCode == 1109) {
                dbselect();
                if (city_lists.size() <= 1) {
                    if (city_lists.size() == 1)
                        setupbg(city_lists.get(0).get("cond_code").toString());
                    pagerAdapter = new MyPagerAdapter(this, city_lists);
                    viewPager.setAdapter(pagerAdapter);
                }
                if (city_lists.size() == 0) {
                    citytitle.setText(R.string.nano_city);
                    bg.setBackgroundResource(R.drawable.bg_sunny);
                }
                pagerAdapter.notifyDataSetChanged();
                point(city_lists.size());
            }
        }
    }
}

class MyPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<HashMap<String, Object>> city_lists;

    public MyPagerAdapter(Context context, ArrayList<HashMap<String, Object>> list) {
        mContext = context;
        city_lists = list;
    }

    @Override
    public int getCount() {
        return city_lists.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = View.inflate(mContext, R.layout.ttt, null);
        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        TextView temperature_type = (TextView) view.findViewById(R.id.temperature_type);
        TextView weather_situation = (TextView) view.findViewById(R.id.weather_situation);
        TextView temperature_max_min = (TextView) view.findViewById(R.id.temperature_max_min);
        TextView update_time = (TextView) view.findViewById(R.id.update_time);
        TextView hum = (TextView) view.findViewById(R.id.hum);
        TextView pres = (TextView) view.findViewById(R.id.pres);
        TextView fl = (TextView) view.findViewById(R.id.fl);
        TextView wind_dir = (TextView) view.findViewById(R.id.wind_dir);
        TextView wind_sc = (TextView) view.findViewById(R.id.wind_sc);
        TextView vis = (TextView) view.findViewById(R.id.vis);
        TextView cloud = (TextView) view.findViewById(R.id.cloud);
        TextView weather1 = (TextView) view.findViewById(R.id.weather1);
        TextView weather2 = (TextView) view.findViewById(R.id.weather2);
        TextView weather3 = (TextView) view.findViewById(R.id.weather3);
        TextView weather4 = (TextView) view.findViewById(R.id.weather4);
        TextView weather5 = (TextView) view.findViewById(R.id.weather5);
        TextView tmp_max1 = (TextView) view.findViewById(R.id.min_temp1);
        TextView tmp_max2 = (TextView) view.findViewById(R.id.min_temp2);
        TextView tmp_max3 = (TextView) view.findViewById(R.id.min_temp3);
        TextView tmp_max4 = (TextView) view.findViewById(R.id.min_temp4);
        TextView tmp_max5 = (TextView) view.findViewById(R.id.min_temp5);
        TextView tmp_min1 = (TextView) view.findViewById(R.id.max_temp1);
        TextView tmp_min2 = (TextView) view.findViewById(R.id.max_temp2);
        TextView tmp_min3 = (TextView) view.findViewById(R.id.max_temp3);
        TextView tmp_min4 = (TextView) view.findViewById(R.id.max_temp4);
        TextView tmp_min5 = (TextView) view.findViewById(R.id.max_temp5);
        TextView date1 = (TextView) view.findViewById(R.id.date1);
        TextView date2 = (TextView) view.findViewById(R.id.date2);
        TextView date3 = (TextView) view.findViewById(R.id.date3);
        TextView date4 = (TextView) view.findViewById(R.id.date4);
        TextView date5 = (TextView) view.findViewById(R.id.date5);


        try {
            date1.setText(city_lists.get(position).get("date1").toString());
            weather1.setText(city_lists.get(position).get("cond_txt_d1").toString());
            tmp_max1.setText(city_lists.get(position).get("tmp_max1").toString() + "℃");
            tmp_min1.setText(city_lists.get(position).get("tmp_min1").toString() + "℃");

            date2.setText(city_lists.get(position).get("date2").toString());
            tmp_min2.setText(city_lists.get(position).get("tmp_min2").toString() + "℃");
            tmp_max2.setText(city_lists.get(position).get("tmp_max2").toString() + "℃");
            weather2.setText(city_lists.get(position).get("cond_txt_d2").toString());

            date3.setText(city_lists.get(position).get("date3").toString());
            tmp_min3.setText(city_lists.get(position).get("tmp_min3").toString() + "℃");
            weather3.setText(city_lists.get(position).get("cond_txt_d3").toString());
            tmp_max3.setText(city_lists.get(position).get("tmp_max3").toString() + "℃");

            weather4.setText(city_lists.get(position).get("cond_txt_d4").toString());
            tmp_min4.setText(city_lists.get(position).get("tmp_min4").toString() + "℃");
            tmp_max4.setText(city_lists.get(position).get("tmp_max4").toString() + "℃");
            date4.setText(city_lists.get(position).get("date4").toString());

            weather5.setText(city_lists.get(position).get("cond_txt_d5").toString());
            date5.setText(city_lists.get(position).get("date5").toString());
            tmp_min5.setText(city_lists.get(position).get("tmp_min5").toString() + "℃");
            tmp_max5.setText(city_lists.get(position).get("tmp_max5").toString() + "℃");
        } catch (Exception e) {

        }

        temperature_max_min.setText(city_lists.get(position).get("tmp_max0").toString()
                + "℃ / " + city_lists.get(position).get("tmp_min0").toString() + "℃");


        ArcProgress arcProgress = view.findViewById(R.id.myProgress);
        WindMillView wind1 = view.findViewById(R.id.wm1);
        WindMillView wind2 = view.findViewById(R.id.wm2);
        wind1.startRotate();
        wind2.startRotate();
//        db.execSQL("insert into weather_now(cid,update_loc,fl,tmp," +
//                        "cond_code,cond_txt,wind_deg,wind_dir,wind_sc," +
//                        "wind_spd,hum,pcpn,pres,vis,cloud)" +
//                        " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//        tv.setText(city_lists.get(position).get("location").toString());
        temperature.setText(city_lists.get(position).get("tmp").toString());
        weather_situation.setText(city_lists.get(position).get("cond_txt").toString());
        update_time.setText(city_lists.get(position).get("update_loc").toString());
        hum.setText(city_lists.get(position).get("hum").toString() + "%");
        int hum_num;
        try {
            hum_num = Integer.parseInt(city_lists.get(position).get("hum").toString());
        } catch (Exception e) {
            hum_num = 88;
        }
        arcProgress.setProgress(100 - hum_num);
        pres.setText(city_lists.get(position).get("pres").toString() + "kPa");
        fl.setText(city_lists.get(position).get("fl").toString() + "℃");
        wind_dir.setText(city_lists.get(position).get("wind_dir").toString());
        wind_sc.setText(city_lists.get(position).get("wind_sc").toString() + "级");
        vis.setText(city_lists.get(position).get("vis").toString() + "Km");
        cloud.setText(city_lists.get(position).get("cloud").toString());
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container,position,object); 这一句要删除，否则报错
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
