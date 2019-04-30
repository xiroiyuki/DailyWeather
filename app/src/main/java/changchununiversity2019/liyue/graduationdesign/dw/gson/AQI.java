package changchununiversity2019.liyue.graduationdesign.dw.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {
    
    public String status;

    @SerializedName("air_now_city")
    public More more;
    
    public class More{
    	public String aqi;
    	public String pm25;
    	
    	@SerializedName("qlty")
    	public String quality;

    }
}
