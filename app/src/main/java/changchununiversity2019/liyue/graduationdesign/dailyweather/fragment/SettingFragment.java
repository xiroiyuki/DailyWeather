package changchununiversity2019.liyue.graduationdesign.dailyweather.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import changchununiversity2019.liyue.graduationdesign.dailyweather.R;
import changchununiversity2019.liyue.graduationdesign.dailyweather.activities.MainActivity;
import changchununiversity2019.liyue.graduationdesign.dailyweather.activities.WeatherActivity;
import changchununiversity2019.liyue.graduationdesign.dailyweather.services.AutoUpdateService;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.ServiceUtility;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment {

    private SwitchPreference getGPSLocationPreference;
    private SwitchPreference pushPreference;
    private SwitchPreference autoUpdatePreference;
    private PreferenceScreen selectLocationPreference;

    private PreferenceScreen backHomePreference;

    public SettingFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.my_preference);

        getGPSLocationPreference = (SwitchPreference)findPreference("acquireLocation");
        selectLocationPreference = (PreferenceScreen) findPreference("autoSelect");

        if(getGPSLocationPreference.isChecked()){
            selectLocationPreference.setEnabled(false);
        }else{
            selectLocationPreference.setEnabled(true);
        }
    }


    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        getGPSLocationPreference = (SwitchPreference)findPreference("acquireLocation");
        selectLocationPreference = (PreferenceScreen) findPreference("autoSelect");
        pushPreference = (SwitchPreference)findPreference("pushInfoSet");
        autoUpdatePreference = (SwitchPreference)findPreference("autoUpdate");

        backHomePreference = (PreferenceScreen)findPreference("backHome");
        backHomePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Toast.makeText(getActivity(),getActivity().toString(),Toast.LENGTH_LONG).show();
                startActivity(new Intent(getActivity(), WeatherActivity.class));
                return false;
            }
        });

        getGPSLocationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getGPSLocationPreference.isChecked()){
                    selectLocationPreference.setEnabled(false);
                }else{
                    selectLocationPreference.setEnabled(true);
                }
                return false;
            }
        });

        selectLocationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                //intent.putExtra("weather_id",getActivity().getIntent().getStringExtra("weather_id"));
                startActivity(intent);
                return false;
            }
        });

        autoUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean isActive = ServiceUtility.isServiceRunning(getActivity(),"AutoUpdateService");
                if(autoUpdatePreference.isChecked()){
                    if(!isActive){
                        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
                        getActivity().startService(intent);
                    }
                }else{
                    if(isActive){
                        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
                        getActivity().stopService(intent);
                    }
                }
                return false;
            }
        });

    }
}
