package changchununiversity2019.liyue.graduationdesign.dw.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public String status;

    public Basic basic;

    public Now now;

    @SerializedName("lifestyle")
    public List<Suggestion> suggestions;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;

    }
}
