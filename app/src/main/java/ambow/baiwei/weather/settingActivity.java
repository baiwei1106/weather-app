package ambow.baiwei.weather;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class settingActivity extends AppCompatActivity {

    private ImageView back;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initview();
        click();
    }

    private void initview() {
        back = findViewById(R.id.back_se);
        constraintLayout = findViewById(R.id.constraintLayout10);
    }

    private void click() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(settingActivity.this,about.class);
                startActivity(intent);
            }
        });
    }
}
