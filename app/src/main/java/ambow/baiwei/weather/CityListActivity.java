package ambow.baiwei.weather;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static ambow.baiwei.weather.SplashActivity.getDbHelper;

public class CityListActivity extends AppCompatActivity {

    private ImageView back;
    private ConstraintLayout buttom;
    private ListView city_list;
    private city_Adapter city_list_ap;
    private ArrayList<HashMap<String, Object>> city_lists;
    private HashMap<String, Object> city_item;
    public DatabaseHelper dbHelper = getDbHelper();
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        initview();
        init();
    }

    private void initview() {
        back = findViewById(R.id.back_ma);
        buttom = findViewById(R.id.bottom_button);
        city_list = findViewById(R.id.city_list);
    }

    private void init() {
        db = dbHelper.getWritableDatabase();
        city_lists = new ArrayList<HashMap<String, Object>>();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(1109);
                finish();
            }
        });

        buttom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CityListActivity.this, SearchCity.class);
                startActivityForResult(intent, 1106);
            }
        });
        city_list_ap = new CityListActivity.city_Adapter(getApplicationContext());
        city_list.setAdapter(city_list_ap);
        city_list.setDividerHeight(0);
        city_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("item",position);
                setResult(1106,intent);
                finish();
//                Toast.makeText(CityListActivity.this, "ddd" + position, Toast.LENGTH_SHORT).show();
            }
        });
        city_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CityListActivity.this);
                builder.setTitle("删除城市");
                builder.setMessage("删除" + city_lists.get(position).get("location").toString() + "，是否确认？");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.execSQL("delete from city where cid=?",
                                new Object[]{city_lists.get(position).get("cid").toString()});
                        dbselect();
                        city_list_ap.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("否", null);
                builder.show();
                return true;
            }
        });
        dbselect();
    }

    private void dbselect() {
        Cursor c = db.rawQuery("select * from city", null);
        city_lists.clear();
        if (c.moveToFirst()) {
            do {
                String cid = c.getString(c.getColumnIndex("cid"));
                String location = c.getString(c.getColumnIndex("location"));
                String parent_city = c.getString(c.getColumnIndex("parent_city"));
                String admin_area = c.getString(c.getColumnIndex("admin_area"));
                String cnty = c.getString(c.getColumnIndex("cnty"));
                String lat = c.getString(c.getColumnIndex("lat"));
                String lon = c.getString(c.getColumnIndex("lon"));
                String tz = c.getString(c.getColumnIndex("tz"));
                String type = c.getString(c.getColumnIndex("type"));
                city_item = new HashMap<String, Object>();
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


    class city_Adapter extends BaseAdapter {
        private Context context = null;

        public city_Adapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return city_lists.size();
        }

        @Override
        public Object getItem(int position) {
            return city_lists.get(position);
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
                convertView = inflater.inflate(R.layout.city_item, null, true);
                mHolder.city_name = convertView.findViewById(R.id.city_name);
                mHolder.city_temp = convertView.findViewById(R.id.city_temp);
                mHolder.location = convertView.findViewById(R.id.location_img);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }
//            String location = Objects.requireNonNull(city_lists.get(position).get("parent_city")).toString();
//            String other = Objects.requireNonNull(city_lists.get(position).get("location")).toString();

            mHolder.city_name.setText(city_lists.get(position).get("location").toString());
            mHolder.city_temp.setText(city_lists.get(position).get("cid").toString());
            if(new UIUtils().getlocation(CityListActivity.this)
                    .equals(city_lists.get(position).get("cid").toString())){
                mHolder.location.setVisibility(View.VISIBLE);
            }
            if (position == 3) {
                convertView.setBackgroundResource(R.drawable.bg_weather_cloud_day);
            }
            return convertView;
        }

        class ViewHolder {
            private TextView city_name;
            private TextView city_temp;
            private TextView city_weather;
            private ImageView location;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1106) {
            if (requestCode == 1106) {
                dbselect();
                city_list_ap.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(1109);
        finish();
    }


}
