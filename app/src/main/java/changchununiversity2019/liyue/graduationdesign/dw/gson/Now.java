package changchununiversity2019.liyue.graduationdesign.dw.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("fl")
    public String feelTemperature;

    @SerializedName("hum")
    public String humidity;

    @SerializedName("wind_dir")
    public String windDir;

    @SerializedName("wind_sc")
    public String windPower;

    @SerializedName("cond_txt")
    public String weatherCondition;

}
