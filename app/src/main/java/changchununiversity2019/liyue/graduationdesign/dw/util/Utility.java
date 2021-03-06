package changchununiversity2019.liyue.graduationdesign.dw.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import changchununiversity2019.liyue.graduationdesign.dw.db.City;
import changchununiversity2019.liyue.graduationdesign.dw.db.County;
import changchununiversity2019.liyue.graduationdesign.dw.db.Province;
import changchununiversity2019.liyue.graduationdesign.dw.gson.AQI;
import changchununiversity2019.liyue.graduationdesign.dw.gson.Weather;

public class Utility {

    /**
     * 解析服务器返回的省级JSON数据。
     *
     * @param response 服务器返回的省级JSON数据
     * @return 解析是否成功
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject provinceObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的市级JSON数据。
     *
     * @param response   服务器返回的市级JSON数据
     * @param provinceId 该市所属的省份的id
     * @return 解析是否成功
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray cities = new JSONArray(response);
                for (int i = 0; i < cities.length(); i++) {
                    JSONObject cityObject = cities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的区级JSON数据。
     *
     * @param response 服务器返回的区级JSON数据
     * @param cityId   该区级城市所属的市级城市的id
     * @return 解析是否成功
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray counties = new JSONArray(response);
                for (int i = 0; i < counties.length(); i++) {
                    JSONObject countyObject = counties.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 转化常规天气信息的JSON数据为Weather实体类。
     *
     * @param response 天气信息的JSON数据
     * @return Weather实体类
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            return new Gson().fromJson(response, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转化当天空气质量的JSON数据为AQI实体类。
     *
     * @param response 当天空气质量的JSON数据
     * @return AQI实体类
     */
    public static AQI handleAQIResponse(String response) {
        try {
            return new Gson().fromJson(response, AQI.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
