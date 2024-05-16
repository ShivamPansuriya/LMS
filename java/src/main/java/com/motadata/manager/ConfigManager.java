package com.motadata.manager;

import com.motadata.utils.Constants;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ConfigManager
{
    private ConfigManager(){}

    public static final String DB_URI = "jdbc:mysql://localhost:3306/data";

    public static final String DB_USER = "root";

    public static final String DB_PASS = "9898";

    private static final String cpuStatsInsertQ = "INSERT INTO `data`.`CpuStats` (`device_id`, `system_load_avg1_min`, `system_load_avg5_min`, `system_load_avg15_min`,`interrupts_per_sec`, `cpu_interrupt_percent`, `cpu_user_percent`, `cpu_percent`, `cpu_io_percent`, `cpu_kernel_percent`, `cpu_idle_percent`, `cpu_type`, `cpu_cores`, `context_switches_per_sec`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String memoryStatsInsertQ = "INSERT INTO MemoryStats (device_id, memory_total_bytes, memory_available_bytes, cache_memory_bytes, swap_memory_provisioned_bytes, swap_memory_free_bytes, swap_memory_used_bytes, swap_memory_used_percent, swap_memory_free_percent, buffer_memory_bytes, memory_used_bytes, memory_free_bytes, memory_free_percent, memory_used_percent, overall_memory_free_percent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String networkStatsInsertQ = "INSERT INTO NetworkStats (device_id, network_udp_connections, network_tcp_connections, network_tcp_retransmissions, network_error_packets, network_out_bytes_rate) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String systemStatsInsertQ = "INSERT INTO SystemStats (device_id, system_vendor, system_os_name, system_os_version, started_time, started_time_seconds, system_model, system_name, product_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String diskStatsInsertQ = "INSERT INTO DiskStats (device_id, opened_file_descriptors, disk_capacity_bytes, disk_free_bytes, disk_free_percent, disk_used_percent, disk_used_bytes, disk_io_time_percent) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final AtomicLong counter = new AtomicLong(0);
    public static long generateID()
    {
        return counter.getAndIncrement();
    }

    public static Connection getConnection(Logger logger)
    {
        Connection connection=null;

        try
        {
            connection = DriverManager.getConnection(DB_URI, DB_USER, DB_PASS);

        } catch(SQLException e)
        {
            logger.error("{}",e.getMessage());
        }

        return connection;

    }

    public static List<PreparedStatement> prepare(Connection connection,JsonObject result, int deviceID) throws SQLException
    {
        var statements = new ArrayList<PreparedStatement>();

        var cpuInsertStmt = connection.prepareStatement(cpuStatsInsertQ);

        cpuInsertStmt.setInt(1,deviceID);

        cpuInsertStmt.setFloat(2,result.getFloat(Constants.SYSTEM_LOAD_AVG1_MIN));

        cpuInsertStmt.setFloat(3,result.getFloat(Constants.SYSTEM_LOAD_AVG5_MIN));

        cpuInsertStmt.setFloat(4,result.getFloat(Constants.SYSTEM_LOAD_AVG15_MIN));

        cpuInsertStmt.setFloat(5,result.getFloat(Constants.INTERRUPT_PER_SECONDS));

        cpuInsertStmt.setFloat(6,result.getFloat(Constants.SYSTEM_CPU_INTERRUPT_PERCENT));

        cpuInsertStmt.setFloat(7,result.getFloat(Constants.SYSTEM_CPU_USER_PERCENT));

        cpuInsertStmt.setFloat(8,result.getFloat(Constants.SYSTEM_CPU_PERCENT));

        cpuInsertStmt.setFloat(9,result.getFloat(Constants.SYSTEM_CPU_IO_PERCENT));

        cpuInsertStmt.setFloat(10,result.getFloat(Constants.SYSTEM_CPU_KERNEL_PERCENT));

        cpuInsertStmt.setFloat(11,result.getFloat(Constants.SYSTEM_CPU_IDLE_PERCENT));

        cpuInsertStmt.setString(12,result.getString(Constants.SYSTEM_CPU_TYPE));

        cpuInsertStmt.setInt(13,Integer.parseInt(result.getString(Constants.SYSTEM_CPU_CORE)));

        cpuInsertStmt.setFloat(14,result.getFloat(Constants.SYSTEM_CONTEXT_SWITCHES_PER_SEC));

        statements.add(cpuInsertStmt);

        var memoryInsertStmt = connection.prepareStatement(memoryStatsInsertQ);

        memoryInsertStmt.setInt(1, deviceID);

        memoryInsertStmt.setFloat(2, result.getFloat(Constants.SYSTEM_MEMORY_TOTAL_BYTES));

        memoryInsertStmt.setFloat(3, result.getFloat(Constants.SYSTEM_MEMORY_AVAILABLE_BYTES));

        memoryInsertStmt.setFloat(4, result.getFloat(Constants.SYSTEM_CACHE_MEMORY_BYTES));

        memoryInsertStmt.setFloat(5, result.getFloat(Constants.SYSTEM_SWAP_MEMORY_PROVISIONED));

        memoryInsertStmt.setFloat(6, result.getFloat(Constants.SYSTEM_SWAP_MEMORY_FREE_BYTES));

        memoryInsertStmt.setFloat(7, result.getFloat(Constants.SYSTEM_SWAP_MEMORY_USED));

        memoryInsertStmt.setFloat(8, result.getFloat(Constants.SYSTEM_SWAP_MEMORY_USED_PERCENT));

        memoryInsertStmt.setFloat(9, result.getFloat(Constants.SYSTEM_SWAP_MEMORY_FREE_PERCENT));

        memoryInsertStmt.setFloat(10, result.getFloat(Constants.SYSTEM_BUFFER_MEMORY_BYTES));

        memoryInsertStmt.setFloat(11, result.getFloat(Constants.SYSTEM_MEMORY_USED_BYTES));

        memoryInsertStmt.setFloat(12, result.getFloat(Constants.SYSTEM_MEMORY_FREE_BYTES));

        memoryInsertStmt.setFloat(13, result.getFloat(Constants.SYSTEM_MEMORY_FREE_PERCENT));

        memoryInsertStmt.setFloat(14, result.getFloat(Constants.SYSTEM_MEMORY_USED_PERCENT));

        memoryInsertStmt.setFloat(15, result.getFloat(Constants.SYSTEM_OVERALL_MEMORY_FREE_PERCENT));

        statements.add(memoryInsertStmt);

        var networkInsertStmt = connection.prepareStatement(networkStatsInsertQ);

        networkInsertStmt.setInt(1, deviceID);

        networkInsertStmt.setFloat(2, result.getFloat(Constants.SYSTEM_NETWORK_UDP_CONNECTIONS));

        networkInsertStmt.setFloat(3, result.getFloat(Constants.SYSTEM_NETWORK_TCP_CONNECTIONS));

        networkInsertStmt.setFloat(4, result.getFloat(Constants.SYSTEM_NETWORK_TCP_RETRANSMISSIONS));

        networkInsertStmt.setFloat(5, result.getFloat(Constants.SYSTEM_NETWORK_ERROR_PACKETS));

        networkInsertStmt.setFloat(6, result.getFloat(Constants.SYSTEM_NETWORK_OUT_BYTES_RATE));

        statements.add(networkInsertStmt);

        var systemInsertStmt = connection.prepareStatement(systemStatsInsertQ);

        systemInsertStmt.setInt(1, deviceID);

        systemInsertStmt.setString(2, result.getString(Constants.VENDOR));

        systemInsertStmt.setString(3, result.getString(Constants.SYSTEM_NAME));

        systemInsertStmt.setString(4, result.getString(Constants.SYSTEM_VERSION));

        systemInsertStmt.setString(5, result.getString(Constants.START_TIME));

        systemInsertStmt.setLong(6, Long.parseLong(result.getString(Constants.START_TIME_SECOND)));

        systemInsertStmt.setString(7, result.getString(Constants.SYSTEM_MODEL));

        systemInsertStmt.setString(8, result.getString(Constants.SYSTEM_NAME));

        systemInsertStmt.setString(9, result.getString(Constants.SYSTEM_PRODUCT));

        statements.add(systemInsertStmt);

        var diskInsertStmt = connection.prepareStatement(diskStatsInsertQ);

        diskInsertStmt.setInt(1, deviceID);

        diskInsertStmt.setFloat(2, result.getFloat(Constants.SYSTEM_OPENED_FILE_DESCRIPTORS));

        diskInsertStmt.setFloat(3, result.getFloat(Constants.SYSTEM_DISK_CAPACITY_BYTES));

        diskInsertStmt.setFloat(4, result.getFloat(Constants.SYSTEM_DISK_FREE_BYTES));

        diskInsertStmt.setFloat(5, result.getFloat(Constants.SYSTEM_DISK_FREE_PERCENT));

        diskInsertStmt.setFloat(6, result.getFloat(Constants.SYSTEM_DISK_USED_PERCENT));

        diskInsertStmt.setFloat(7, result.getFloat(Constants.SYSTEM_DISK_USED_BYTES));

        diskInsertStmt.setFloat(8, result.getFloat(Constants.SYSTEM_DISK_IO_TIME_PERCENT));

        statements.add(diskInsertStmt);

        return statements;
    }

}
