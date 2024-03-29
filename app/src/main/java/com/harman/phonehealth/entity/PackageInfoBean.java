package com.harman.phonehealth.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PackageInfoBean {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int usedCount;
    private long usedTime;
    private String packageName;
    private String appName;
    private long date;
    private String classMap;

    public PackageInfoBean(String classMap, long date, int usedCount, long usedTime, String packageName, String appName) {
        this.classMap = classMap;
        this.date = date;
        this.usedCount = usedCount;
        this.usedTime = usedTime;
        this.packageName = packageName;
        this.appName = appName;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addCount() {
        usedCount++;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        PackageInfoBean standardDetail = (PackageInfoBean) o;
        return standardDetail.getPackageName().equals(this.packageName);
    }

    @Override
    public int hashCode() {
        return (packageName + usedTime).hashCode();
    }

    @Override
    public String toString() {
        return "PackageInfoBean{" +
                "usedCount=" + usedCount +
                ", usedTime=" + usedTime +
                ", packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                '}';
    }

    public String getClassMap() {
        return classMap;
    }

    public void setClassMap(String classMap) {
        this.classMap = classMap;
    }
}