package changchununiversity2019.liyue.graduationdesign.dw.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;

    @SerializedName("tmp_max")
    public String temperatureMax;
    
    @SerializedName("tmp_min")
    public String temperatureMin;

    @SerializedName("cond_txt_d")
    public String dayCondition;

}
