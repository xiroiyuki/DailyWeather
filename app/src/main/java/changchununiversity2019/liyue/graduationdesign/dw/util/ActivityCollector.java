package changchununiversity2019.liyue.graduationdesign.dw.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();//用于存储Activity的列表

    /**
     * 添加Activity到列表中。
     *
     * @param activity
     */
    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    /**
     * 停止列表中所有的Activity。
     */
    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }

    /**
     * 移除列表中指定的Activity。
     *
     * @param activity 需要被移除的Activity
     */
    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }
}
