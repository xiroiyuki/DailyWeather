package changchununiversity2019.liyue.graduatondesign.dailyweather.util;

import android.text.TextUtils;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import changchununiversity2019.liyue.graduatondesign.dailyweather.db.City;
import changchununiversity2019.liyue.graduatondesign.dailyweather.db.County;
import changchununiversity2019.liyue.graduatondesign.dailyweather.db.Province;

public class Utility {

    /**
     * 解析服务器返回的省级数据。
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray provinces = new JSONArray(response);
                for(int i = 0;i < provinces.length();i++){
                    JSONObject provinceObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            }catch(JSONException exception){
                exception.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的市级数据。
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray cities = new JSONArray(response);
                for(int i = 0;i < cities.length();i++){
                    JSONObject cityObject = cities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析各处理服务器返回的县级数据。
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray counties = new JSONArray(response);
                for(int i = 0;i < counties.length();i++){
                    JSONObject countyObject = counties.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
