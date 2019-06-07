package changchununiversity2019.liyue.graduationdesign.dw.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    /**
     * 使用OkHttp3发送网络请求。
     *
     * @param address  网络请求的地址
     * @param callback 网络请求的回调
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

}
