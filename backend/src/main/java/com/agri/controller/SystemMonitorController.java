package com.agri.controller;

import com.agri.utils.ResponseUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemMonitorController {

    @GetMapping("/monitor")
    public ResponseUtils.ApiResponse<Map<String, Object>> getSystemMonitor() {
        Map<String, Object> monitor = new HashMap<>();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        long heapUsed = heapUsage.getUsed();
        long heapMax = heapUsage.getMax();
        long heapCommitted = heapUsage.getCommitted();
        long nonHeapUsed = nonHeapUsage.getUsed();

        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsedMB", heapUsed / (1024 * 1024));
        memory.put("heapMaxMB", heapMax / (1024 * 1024));
        memory.put("heapCommittedMB", heapCommitted / (1024 * 1024));
        memory.put("heapUsedPercent", heapMax > 0 ? Math.round((double) heapUsed / heapMax * 10000.0) / 100.0 : 0);
        memory.put("nonHeapUsedMB", nonHeapUsed / (1024 * 1024));
        monitor.put("memory", memory);

        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        jvm.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        jvm.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        jvm.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        jvm.put("availableProcessors", runtime.availableProcessors());
        monitor.put("jvm", jvm);

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> os = new HashMap<>();
        os.put("name", osBean.getName());
        os.put("version", osBean.getVersion());
        os.put("arch", osBean.getArch());
        os.put("availableProcessors", osBean.getAvailableProcessors());
        os.put("systemLoadAverage", osBean.getSystemLoadAverage());
        monitor.put("os", os);

        monitor.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        monitor.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());

        return ResponseUtils.success(monitor);
    }
}
