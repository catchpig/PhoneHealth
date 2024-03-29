package com.harman.phonehealth.utils;

import android.annotation.TargetApi;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.harman.phonehealth.entity.OneTimeDetails;
import com.harman.phonehealth.entity.PackageInfoBean;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UseTimeDataManager {
    public static final String TAG = "UseTimeDataManager";

    private static UseTimeDataManager mUseTimeDataManager;
    private Context mContext;
    private int mDayNum;

    private ArrayList<UsageEvents.Event> mEventList;
    private ArrayList<UsageEvents.Event> mEventListChecked;
    private ArrayList<UsageStats> mStatsList;

    //记录打开一次应用，使用的activity详情
    private ArrayList<OneTimeDetails> mOneTimeDetailList = new ArrayList<>();

    //记录某一次打开应用的使用情况（查询某一次使用情况的时候，用于界面显示）
    private OneTimeDetails mOneTimeDetails;

    //主界面数据
    private ArrayList<PackageInfoBean> mPackageInfoBeanList = new ArrayList<>();

    private UseTimeDataManager(Context context) {
        this.mContext = context;
    }

    public static UseTimeDataManager getInstance(Context context) {
        if (mUseTimeDataManager == null) {
            mUseTimeDataManager = new UseTimeDataManager(context);
        }
        return mUseTimeDataManager;
    }

    public int refreshData(String date) {
        long startTime = 0;
        long endTime = 0;
        try {
            startTime = DateTransUtils.getStartClockTimeStamp(date);
            endTime = DateTransUtils.getEndClockTimeStamp(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mEventList = getEventList(startTime, endTime);
        mStatsList = getUsageList(startTime, endTime);

        if (mEventList == null || mEventList.size() == 0 || mStatsList == null || mStatsList.size() == 0) {
            Log.i(TAG, " refreshData() mEventList or mStatsList has no data");
            return 1;
        }

        mEventListChecked = getEventListChecked();
        refreshOneTimeDetailList(0);
        refreshPackageInfoList(startTime);
        return 0;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void refreshPackageInfoList(long date) {
        mPackageInfoBeanList.clear();
        for (int i = 0; i < mStatsList.size(); i++) {
//            屏蔽系统应用
//            if (!isSystemApp(mContext, mStatsList.get(i).getPackageName())) {
            PackageInfoBean info = null;
            try {
                Map<String, Double> classMap = new HashMap<>();
                for (int k = 0; k < mEventListChecked.size(); k++) {
                    if (mStatsList.get(i).getPackageName().equals(mEventListChecked.get(k).getPackageName())) {
                        classMap.merge(mEventListChecked.get(k).getClassName(), 0.5, Double::sum);
                    }
                }
                info = new PackageInfoBean(JsonUtil.mapToJson(classMap), date, getLaunchCount(mStatsList.get(i)), calculateUseTime(mStatsList.get(i).getPackageName()), mStatsList.get(i).getPackageName(), getApplicationNameByPackageName(mContext, mStatsList.get(i).getPackageName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            mPackageInfoBeanList.add(info);
//            }
        }

        for (int n = 0; n < mPackageInfoBeanList.size(); n++) {
            String pkg = mPackageInfoBeanList.get(n).getPackageName();
            for (int m = 0; m < mOneTimeDetailList.size(); m++) {
                if (pkg.equals(mOneTimeDetailList.get(m).getPkgName())) {
                    mPackageInfoBeanList.get(n).addCount();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<UsageEvents.Event> getEventList(long startTime, long endTime) {
        return EventUtils.getEventList(mContext, startTime, endTime);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<UsageStats> getUsageList(long startTime, long endTime) {
        return EventUtils.getUsageList(mContext, startTime, endTime);
    }

    //仅保留 event 中 type 为 1或者2 的
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<UsageEvents.Event> getEventListChecked() {
        ArrayList<UsageEvents.Event> mList = new ArrayList<>();
        for (int i = 0; i < mEventList.size(); i++) {
            if (mEventList.get(i).getEventType() == 1 || mEventList.get(i).getEventType() == 2) {
                mList.add(mEventList.get(i));
            }
        }
        return mList;
    }

    public String getApplicationNameByPackageName(Context context, String packageName) {

        PackageManager pm = context.getPackageManager();
        String Name;
        try {
            Name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Name = "";
        }
        return Name;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<UsageEvents.Event> getEventListCheckWithoutErrorData() {
        ArrayList<UsageEvents.Event> mList = new ArrayList<>();
        for (int i = 0; i < mEventList.size(); i++) {
            if (mEventList.get(i).getEventType() == 1 || mEventList.get(i).getEventType() == 2) {
                mList.add(mEventList.get(i));
            }
        }
        return mList;
    }

    //从 startIndex 开始分类event  直至将event分完
    //每次从0开始，将原本的 mOneTimeDetailList 清除一次,然后开始分类
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void refreshOneTimeDetailList(int startIndex) {
        Log.i(TAG, " refreshOneTimeDetailList() startIndex : " + startIndex);
        if (startIndex == 0) {
            if (mOneTimeDetailList != null) {
                mOneTimeDetailList.clear();
            }
        }
        long totalTime = 0;
        int usedIndex = 0;
        String pkg = null;
        ArrayList<UsageEvents.Event> list = new ArrayList();
        for (int i = startIndex; i < mEventListChecked.size(); i++) {
            if (i == startIndex) {
                pkg = mEventListChecked.get(i).getPackageName();
                list.add(mEventListChecked.get(i));
            } else {
                if (pkg != null) {
                    if (pkg.equals(mEventListChecked.get(i).getPackageName())) {
                        list.add(mEventListChecked.get(i));
                        if (i == mEventListChecked.size() - 1) {
                            usedIndex = i;
                        }
                    } else {
                        usedIndex = i;
                        break;
                    }
                }
            }
        }

        checkEventList(list);
//        Log.i(TAG, " mEventListChecked 本次启动的包名：" + list.get(0).getPackageName() + " 时间：" + DateUtils.formatSameDayTime(list.get(0).getTimeStamp(), System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
        for (int i = 1; i < list.size(); i += 2) {
            if (list.get(i).getEventType() == 2 && list.get(i - 1).getEventType() == 1) {
                totalTime += (list.get(i).getTimeStamp() - list.get(i - 1).getTimeStamp());
            }
        }
        OneTimeDetails oneTimeDetails = new OneTimeDetails(pkg, totalTime, list);
        mOneTimeDetailList.add(oneTimeDetails);

        if (usedIndex < mEventListChecked.size() - 1) {
            refreshOneTimeDetailList(usedIndex);
        }
    }

    public ArrayList<OneTimeDetails> getPkgOneTimeDetailList(String pkg) {

        if ("all".equals(pkg)) {
            return mOneTimeDetailList;
        }
        ArrayList<OneTimeDetails> list = new ArrayList<>();
        if (mOneTimeDetailList != null && mOneTimeDetailList.size() > 0) {
            for (int i = 0; i < mOneTimeDetailList.size(); i++) {
                if (mOneTimeDetailList.get(i).getPkgName().equals(pkg)) {
                    list.add(mOneTimeDetailList.get(i));
                }
            }
        }
        return list;
    }

    // 从头遍历EventList，如果发现异常数据，则删除该异常数据，并从头开始再次进行遍历，直至无异常数据
    // （异常数据是指：event 均为 type=1 和type=2 ，成对出现，一旦发现未成对出现的数据，即视为异常数据）
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void checkEventList(ArrayList<UsageEvents.Event> list) {
        boolean isCheckAgain = false;
        for (int i = 0; i < list.size() - 1; i += 2) {
            if (list.get(i).getClassName().equals(list.get(i + 1).getClassName())) {
                if (list.get(i).getEventType() != 1) {
                    Log.e(UseTimeDataManager.TAG, " EventList 出错： " + list.get(i).getPackageName() + "  " + DateUtils.formatSameDayTime(list.get(i).getTimeStamp(), System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM).toString());
                    list.remove(i);
                    isCheckAgain = true;
                    break;
                }
                if (list.get(i + 1).getEventType() != 2) {
                    Log.e(UseTimeDataManager.TAG, " EventList 出错： " + list.get(i + 1).getPackageName() + "  " + DateUtils.formatSameDayTime(list.get(i + 1).getTimeStamp(), System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM).toString());
                    list.remove(i);
                    isCheckAgain = true;
                    break;
                }
            } else {
//                i和i+1的className对不上，则删除第i个数据，重新检查
                list.remove(i);
                isCheckAgain = true;
                break;
            }
        }
        if (isCheckAgain) {
            checkEventList(list);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ArrayList<PackageInfoBean> getPkgInfoListFromUsageList(long date) throws IllegalAccessException {
        ArrayList<PackageInfoBean> result = new ArrayList<>();

        if (mStatsList != null && mStatsList.size() > 0) {
            for (int i = 0; i < mStatsList.size(); i++) {
                result.add(new PackageInfoBean(null, date, getLaunchCount(mStatsList.get(i)), mStatsList.get(i).getTotalTimeInForeground(), mStatsList.get(i).getPackageName(), getApplicationNameByPackageName(mContext, mStatsList.get(i).getPackageName())));
            }
        }
        return result;
    }

    //判断app是否为系统app
    public boolean isSystemApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai != null && (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 利用反射，获取UsageStats中统计的应用使用次数
    private int getLaunchCount(UsageStats usageStats) throws IllegalAccessException {
        Field field = null;
        try {
            field = usageStats.getClass().getDeclaredField("mLaunchCount");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return (int) field.get(usageStats);
    }

    //根据event计算使用时间
    public long calculateUseTime(String pkg) {
        long useTime = 0;
        for (int i = 0; i < mOneTimeDetailList.size(); i++) {
            if (mOneTimeDetailList.get(i).getPkgName().equals(pkg)) {
                useTime += mOneTimeDetailList.get(i).getUseTime();
            }
        }
        Log.i(TAG, " calculateUseTime: " + useTime);
        return useTime;
    }

    public ArrayList<PackageInfoBean> getPkgInfoListFromEventList() {
        return mPackageInfoBeanList;
    }

    public int getmDayNum() {
        return mDayNum;
    }

    public void setmDayNum(int mDayNum) {
        this.mDayNum = mDayNum;
    }

    public ArrayList<OneTimeDetails> getmOneTimeDetailList() {
        return mOneTimeDetailList;
    }

    public OneTimeDetails getmOneTimeDetails() {
        return mOneTimeDetails;
    }

    public void setmOneTimeDetails(OneTimeDetails mOneTimeDetails) {
        this.mOneTimeDetails = mOneTimeDetails;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UsageStats getUsageStats(String pkg) {
        for (int i = 0; i < mStatsList.size(); i++) {
            if (mStatsList.get(i).getPackageName().equals(pkg)) {
                return mStatsList.get(i);
            }
        }
        return null;
    }
}