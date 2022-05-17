// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.util;

import com.gitee.dbswitch.data.entity.SourceDataSourceProperties;
import com.gitee.dbswitch.data.entity.TargetDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DataSource工具类
 *
 * @author tang
 */
@Slf4j
public final class DataSourceUtils {

  /**
   * 创建于指定数据库连接描述符的连接池
   *
   * @param properties 数据库连接描述符
   * @return HikariDataSource连接池
   */
  public static HikariDataSource createSourceDataSource(SourceDataSourceProperties properties) {
    HikariDataSource ds = new HikariDataSource();
    ds.setPoolName("The_Source_DB_Connection");
    ds.setJdbcUrl(properties.getUrl());
    ds.setDriverClassName(properties.getDriverClassName());
    ds.setUsername(properties.getUsername());
    ds.setPassword(properties.getPassword());
    if (properties.getDriverClassName().contains("oracle")) {
      ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
      // https://blog.csdn.net/qq_20960159/article/details/78593936
      System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");
    } else if (properties.getDriverClassName().contains("db2")) {
      ds.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
    } else {
      ds.setConnectionTestQuery("SELECT 1");
    }
    ds.setMaximumPoolSize(8);
    ds.setMinimumIdle(5);
    ds.setMaxLifetime(properties.getMaxLifeTime());
    ds.setConnectionTimeout(properties.getConnectionTimeout());
    ds.setIdleTimeout(60000);

    return ds;
  }

  /**
   * 创建于指定数据库连接描述符的连接池
   *
   * @param properties 数据库连接描述符
   * @return HikariDataSource连接池
   */
  public static HikariDataSource createTargetDataSource(TargetDataSourceProperties properties) {
    if (properties.getUrl().trim().startsWith("jdbc:hive2://")) {
      throw new UnsupportedOperationException("Unsupported hive as target datasource!!!");
    }

    HikariDataSource ds = new HikariDataSource();
    ds.setPoolName("The_Target_DB_Connection");
    ds.setJdbcUrl(properties.getUrl());
    ds.setDriverClassName(properties.getDriverClassName());
    ds.setUsername(properties.getUsername());
    ds.setPassword(properties.getPassword());
    if (properties.getDriverClassName().contains("oracle")) {
      ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
    } else if (properties.getDriverClassName().contains("db2")) {
      ds.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
    } else {
      ds.setConnectionTestQuery("SELECT 1");
    }
    ds.setMaximumPoolSize(8);
    ds.setMinimumIdle(5);
    ds.setMaxLifetime(properties.getMaxLifeTime());
    ds.setConnectionTimeout(properties.getConnectionTimeout());
    ds.setIdleTimeout(60000);

    // 如果是Greenplum数据库，这里需要关闭会话的查询优化器
    if (properties.getDriverClassName().contains("postgresql")) {
      org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource();
      dataSource.setDriverClassName(properties.getDriverClassName());
      dataSource.setUrl(properties.getUrl());
      dataSource.setUsername(properties.getUsername());
      dataSource.setPassword(properties.getPassword());
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      String versionString = jdbcTemplate.queryForObject("SELECT version()", String.class);
      if (Objects.nonNull(versionString) && versionString.contains("Greenplum")) {
        log.info(
            "#### Target database is Greenplum Cluster, Close Optimizer now: set optimizer to 'off' ");
        ds.setConnectionInitSql("set optimizer to 'off'");
      }
    }

    return ds;
  }

  private DataSourceUtils() {
  }

}
