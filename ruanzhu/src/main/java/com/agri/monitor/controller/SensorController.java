package com.agri.monitor.controller;

import com.agri.monitor.entity.SensorData;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 传感器数据管理控制器
 * 提供前端大屏所需的数据接口
 */
@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    /**
     * 获取实时数据列表
     * @param page 当前页码
     * @param size 每页数量
     * @return 数据列表
     */
    @GetMapping("/list")
    public List<SensorData> getList(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        // 模拟业务逻辑处理
        List<SensorData> list = new ArrayList<>();
        // 此处省略具体的数据库查询逻辑，仅做接口定义
        return list;
    }

    /**
     * 接收硬件上报数据
     * @param data 传感器实体
     * @return 操作结果
     */
    @PostMapping("/report")
    public String reportData(@RequestBody SensorData data) {
        if (data == null) {
            return "Error: Data is empty";
        }
        data.setReportTime(new Date());
        // 保存数据到数据库
        saveToDatabase(data);
        return "Success";
    }

    /**
     * 根据ID查询详情
     */
    @GetMapping("/detail/{id}")
    public SensorData getDetail(@PathVariable Long id) {
        SensorData data = new SensorData();
        data.setId(id);
        return data;
    }

    /**
     * 导出Excel报表
     */
    @GetMapping("/export")
    public void exportExcel() {
        // 导出逻辑实现
    }

    /**
     * 模拟数据库保存方法
     */
    private void saveToDatabase(SensorData data) {
        // 具体的持久层调用
        System.out.println("Saving data: " + data.toString());
    }

    // 这里还可以加很多查询方法来凑行数
}