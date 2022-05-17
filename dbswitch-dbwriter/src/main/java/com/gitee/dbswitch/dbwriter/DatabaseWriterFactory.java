// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter;

import com.gitee.dbswitch.common.util.DatabaseAwareUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * 数据库写入器构造工厂类
 *
 * @author tang
 */
public class DatabaseWriterFactory {

  private static final Map<String, Function<DataSource, IDatabaseWriter>> DATABASE_WRITER_MAPPER
      = new HashMap<String, Function<DataSource, IDatabaseWriter>>() {

    private static final long serialVersionUID = 3365136872693503697L;

    {
      put("MYSQL", com.gitee.dbswitch.dbwriter.mysql.MySqlWriterImpl::new);
      put("ORACLE", com.gitee.dbswitch.dbwriter.oracle.OracleWriterImpl::new);
      put("SQLSERVER", com.gitee.dbswitch.dbwriter.mssql.SqlServerWriterImpl::new);
      put("SQLSERVER2000", com.gitee.dbswitch.dbwriter.mssql.SqlServerWriterImpl::new);
      put("POSTGRESQL", com.gitee.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl::new);
      put("GREENPLUM", com.gitee.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl::new);
      put("DB2", com.gitee.dbswitch.dbwriter.db2.DB2WriterImpl::new);
      put("DM", com.gitee.dbswitch.dbwriter.dm.DmWriterImpl::new);
      //对于kingbase当前只能使用insert模式
      put("KINGBASE", com.gitee.dbswitch.dbwriter.kingbase.KingbaseInsertWriterImpl::new);
    }
  };

  /**
   * 获取指定数据库类型的写入器
   *
   * @param dataSource 连接池数据源
   * @return 写入器对象
   */
  public static IDatabaseWriter createDatabaseWriter(DataSource dataSource) {
    return DatabaseWriterFactory.createDatabaseWriter(dataSource, false);
  }

  /**
   * 获取指定数据库类型的写入器
   *
   * @param dataSource 连接池数据源
   * @param insert     对于GP/GP数据库来说是否使用insert引擎写入
   * @return 写入器对象
   */
  public static IDatabaseWriter createDatabaseWriter(DataSource dataSource, boolean insert) {
    String type = DatabaseAwareUtils.getDatabaseTypeByDataSource(dataSource).name();
    if (insert) {
      if ("POSTGRESQL".equalsIgnoreCase(type) || "GREENPLUM".equalsIgnoreCase(type)) {
        return new com.gitee.dbswitch.dbwriter.gpdb.GreenplumInsertWriterImpl(dataSource);
      }
    }

    if (!DATABASE_WRITER_MAPPER.containsKey(type)) {
      throw new RuntimeException(
          String.format("[dbwrite] Unsupported database type (%s)", type));
    }

    return DATABASE_WRITER_MAPPER.get(type.trim()).apply(dataSource);
  }

}
