// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.common.util;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

/**
 * 数据库类型识别工具类
 *
 * @author tang
 */
public final class DatabaseAwareUtils {

  private static final Map<String, DatabaseTypeEnum> productNameMap;

  private static final Map<String, DatabaseTypeEnum> driverNameMap;

  static {
    productNameMap = new HashMap<>();
    driverNameMap = new HashMap<>();

    productNameMap.put("Greenplum", DatabaseTypeEnum.GREENPLUM);
    productNameMap.put("Microsoft SQL Server", DatabaseTypeEnum.SQLSERVER);
    productNameMap.put("DM DBMS", DatabaseTypeEnum.DM);
    productNameMap.put("KingbaseES", DatabaseTypeEnum.KINGBASE);
    productNameMap.put("Apache Hive", DatabaseTypeEnum.HIVE);
    productNameMap.put("MySQL", DatabaseTypeEnum.MYSQL);
    productNameMap.put("MariaDB", DatabaseTypeEnum.MARIADB);
    productNameMap.put("Oracle", DatabaseTypeEnum.ORACLE);
    productNameMap.put("PostgreSQL", DatabaseTypeEnum.POSTGRESQL);
    productNameMap.put("DB2 for Unix/Windows", DatabaseTypeEnum.DB2);
    productNameMap.put("Hive", DatabaseTypeEnum.HIVE);

    driverNameMap.put("MySQL Connector Java", DatabaseTypeEnum.MYSQL);
    driverNameMap.put("MariaDB Connector/J", DatabaseTypeEnum.MARIADB);
    driverNameMap.put("Oracle JDBC driver", DatabaseTypeEnum.ORACLE);
    driverNameMap.put("PostgreSQL JDBC Driver", DatabaseTypeEnum.POSTGRESQL);
    driverNameMap.put("Kingbase8 JDBC Driver", DatabaseTypeEnum.KINGBASE);
    driverNameMap.put("IBM Data Server Driver for JDBC and SQLJ", DatabaseTypeEnum.DB2);
    driverNameMap.put("dm.jdbc.driver.DmDriver", DatabaseTypeEnum.DM);
    driverNameMap.put("Hive JDBC", DatabaseTypeEnum.HIVE);
  }

  /**
   * 获取数据库的产品名
   *
   * @param dataSource 数据源
   * @return 数据库产品名称字符串
   */
  public static DatabaseTypeEnum getDatabaseTypeByDataSource(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      String driverName = connection.getMetaData().getDriverName();
      if (driverNameMap.containsKey(driverName)) {
        return driverNameMap.get(driverName);
      }

      DatabaseTypeEnum type = productNameMap.get(productName);
      if (null == type) {
        throw new IllegalStateException("Unable to detect database type from data source instance");
      }
      return type;
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  /**
   * 检查MySQL数据库表的存储引擎是否为Innodb
   *
   * @param schemaName schema名
   * @param tableName  table名
   * @param dataSource 数据源
   * @return 为Innodb存储引擎时返回True, 否在为false
   */
  public static boolean isMysqlInnodbStorageEngine(String schemaName, String tableName,
      DataSource dataSource) {
    String sql = "SELECT count(*) as total FROM information_schema.tables "
        + "WHERE table_schema=? AND table_name=? AND ENGINE='InnoDB'";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, schemaName);
      ps.setString(2, tableName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }

      return false;
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  private DatabaseAwareUtils() {
  }

}
