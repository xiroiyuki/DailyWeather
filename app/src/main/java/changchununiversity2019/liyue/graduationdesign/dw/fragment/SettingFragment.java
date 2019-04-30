package changchununiversity2019.liyue.graduationdesign.dw.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;

import changchununiversity2019.liyue.graduationdesign.dw.R;
import changchununiversity2019.liyue.graduationdesign.dw.activities.MainActivity;
import changchununiversity2019.liyue.graduationdesign.dw.activities.WeatherActivity;
import changchununiversity2019.liyue.graduationdesign.dw.services.AutoUpdateService;
import changchununiversity2019.liyue.graduationdesign.dw.util.ServiceUtility;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment {

    private SwitchPreference getGPSLocationPreference;
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
        autoUpdatePreference = (SwitchPreference)findPreference("autoUpdate");

        backHomePreference = (PreferenceScreen)findPreference("backHome");
        backHomePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
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
