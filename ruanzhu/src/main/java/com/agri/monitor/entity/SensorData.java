package com.agri.monitor.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 智慧农业物联网 - 传感器数据实体类
 * 用于存储从硬件设备采集回来的实时环境数据
 *
 * @author Admin
 * @version 1.0
 * @since 2026-01-26
 */
public class SensorData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据唯一主键ID
     */
    private Long id;

    /**
     * 设备序列号 (UUID)
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 传感器类型 (1:温度 2:湿度 3:光照 4:CO2 5:土壤)
     */
    private Integer sensorType;

    /**
     * 采集数值 (Double精度)
     */
    private Double dataValue;

    /**
     * 数值单位 (例如: ℃, %, Lux)
     */
    private String unit;

    /**
     * 设备所在区域 (例如: 1号大棚)
     */
    private String locationArea;

    /**
     * 电池电量状态 (%)
     */
    private Integer batteryLevel;

    /**
     * 信号强度 (dBm)
     */
    private Integer signalStrength;

    /**
     * 是否触发预警 (0:正常 1:预警)
     */
    private Integer alarmStatus;

    /**
     * 数据采集时间
     */
    private Date collectTime;

    /**
     * 数据上报时间
     */
    private Date reportTime;

    /**
     * 备注信息
     */
    private String remark;

    // ================== Getter and Setter Methods ==================
    // 软著技巧：生成完整的Getter/Setter，占据大量行数

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Integer getSensorType() { return sensorType; }
    public void setSensorType(Integer sensorType) { this.sensorType = sensorType; }

    public Double getDataValue() { return dataValue; }
    public void setDataValue(Double dataValue) { this.dataValue = dataValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getLocationArea() { return locationArea; }
    public void setLocationArea(String locationArea) { this.locationArea = locationArea; }

    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }

    public Integer getSignalStrength() { return signalStrength; }
    public void setSignalStrength(Integer signalStrength) { this.signalStrength = signalStrength; }

    public Integer getAlarmStatus() { return alarmStatus; }
    public void setAlarmStatus(Integer alarmStatus) { this.alarmStatus = alarmStatus; }

    public Date getCollectTime() { return collectTime; }
    public void setCollectTime(Date collectTime) { this.collectTime = collectTime; }

    public Date getReportTime() { return reportTime; }
    public void setReportTime(Date reportTime) { this.reportTime = reportTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", deviceCode='" + deviceCode + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", dataValue=" + dataValue +
                '}';
    }
}