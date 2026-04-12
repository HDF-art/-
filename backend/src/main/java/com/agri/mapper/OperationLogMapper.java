package com.agri.mapper;

import com.agri.model.OperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Select("<script>" +
            "SELECT ol.*, u.username, u.role as user_role, u.farm_id as user_farm_id, f.name as user_farm_name " +
            "FROM operation_log ol " +
            "LEFT JOIN user u ON ol.user_id = u.id " +
            "LEFT JOIN farm f ON u.farm_id = f.id " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND ol.user_id = #{userId} </if>" +
            "<if test='username != null and username != \"\"'> AND u.username LIKE CONCAT('%', #{username}, '%') </if>" +
            "<if test='userRole != null'> AND u.role = #{userRole} </if>" +
            "<if test='userFarmId != null'> AND u.farm_id = #{userFarmId} </if>" +
            "<if test='operationType != null and operationType != \"\"'> AND ol.operation_type = #{operationType} </if>" +
            "<if test='module != null and module != \"\"'> AND ol.module = #{module} </if>" +
            "<if test='result != null and result != \"\"'> AND ol.result = #{result} </if>" +
            "<if test='startDate != null'> AND DATE(ol.created_at) &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND DATE(ol.created_at) &lt;= #{endDate} </if>" +
            "<if test='ipAddress != null and ipAddress != \"\"'> AND ol.ip_address = #{ipAddress} </if>" +
            "ORDER BY ol.created_at DESC" +
            "</script>")
    IPage<Map<String, Object>> selectLogPage(Page<?> page, 
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("userRole") Integer userRole,
            @Param("userFarmId") Long userFarmId,
            @Param("operationType") String operationType,
            @Param("module") String module,
            @Param("result") String result,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("ipAddress") String ipAddress);

    @Select("<script>" +
            "SELECT ol.*, u.username, u.role as user_role, u.farm_id as user_farm_id, f.name as user_farm_name " +
            "FROM operation_log ol " +
            "LEFT JOIN user u ON ol.user_id = u.id " +
            "LEFT JOIN farm f ON u.farm_id = f.id " +
            "WHERE ol.id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "ORDER BY ol.created_at DESC" +
            "</script>")
    List<Map<String, Object>> selectLogsByIds(@Param("ids") List<Long> ids);

    @Select("SELECT DISTINCT operation_type FROM operation_log ORDER BY operation_type")
    List<String> selectAllOperationTypes();

    @Select("SELECT DISTINCT module FROM operation_log ORDER BY module")
    List<String> selectAllModules();

    @Select("<script>" +
            "SELECT COUNT(*) as count, operation_type FROM operation_log " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='startDate != null'> AND DATE(created_at) &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND DATE(created_at) &lt;= #{endDate} </if>" +
            "GROUP BY operation_type ORDER BY count DESC" +
            "</script>")
    List<Map<String, Object>> selectOperationTypeStats(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Select("<script>" +
            "SELECT COUNT(*) as count, module FROM operation_log " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='startDate != null'> AND DATE(created_at) &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND DATE(created_at) &lt;= #{endDate} </if>" +
            "GROUP BY module ORDER BY count DESC" +
            "</script>")
    List<Map<String, Object>> selectModuleStats(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Select("<script>" +
            "SELECT COUNT(*) as count, DATE(created_at) as date FROM operation_log " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='startDate != null'> AND DATE(created_at) &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND DATE(created_at) &lt;= #{endDate} </if>" +
            "GROUP BY DATE(created_at) ORDER BY date DESC" +
            "</script>")
    List<Map<String, Object>> selectDailyStats(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Select("<script>" +
            "SELECT ol.* FROM operation_log ol " +
            "LEFT JOIN user u ON ol.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='userIds != null and userIds.size() > 0'>" +
            "AND ol.user_id IN " +
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            "<if test='farmIds != null and farmIds.size() > 0'>" +
            "AND u.farm_id IN " +
            "<foreach item='id' collection='farmIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY ol.created_at DESC" +
            "</script>")
    List<OperationLog> selectLogsByUserOrFarm(
            @Param("userIds") List<Long> userIds,
            @Param("farmIds") List<Long> farmIds);
}
