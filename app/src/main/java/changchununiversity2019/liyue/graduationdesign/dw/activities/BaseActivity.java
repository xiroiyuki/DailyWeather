package changchununiversity2019.liyue.graduationdesign.dw.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import changchununiversity2019.liyue.graduationdesign.dw.util.ActivityCollector;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
