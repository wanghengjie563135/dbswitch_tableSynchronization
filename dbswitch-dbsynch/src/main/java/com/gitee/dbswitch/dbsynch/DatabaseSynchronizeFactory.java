// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbsynch;

import com.gitee.dbswitch.common.util.DatabaseAwareUtils;
import com.gitee.dbswitch.dbsynch.db2.DB2DatabaseSyncImpl;
import com.gitee.dbswitch.dbsynch.dm.DmDatabaseSyncImpl;
import com.gitee.dbswitch.dbsynch.kingbase.KingbaseDatabaseSyncImpl;
import com.gitee.dbswitch.dbsynch.mssql.SqlServerDatabaseSyncImpl;
import com.gitee.dbswitch.dbsynch.mysql.MySqlDatabaseSyncImpl;
import com.gitee.dbswitch.dbsynch.oracle.OracleDatabaseSyncImpl;
import com.gitee.dbswitch.dbsynch.pgsql.GreenplumDatabaseSyncImpl;
import com.gitee.dbswitch.dbsynch.pgsql.PostgresqlDatabaseSyncImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * 数据库同步器构造工厂类
 *
 * @author tang
 */
public final class DatabaseSynchronizeFactory {

  private static final Map<String, Function<DataSource, IDatabaseSynchronize>> DATABASE_SYNC_MAPPER = new HashMap<String, Function<DataSource, IDatabaseSynchronize>>() {

    private static final long serialVersionUID = -2359773637275934408L;

    {
      put("MYSQL", MySqlDatabaseSyncImpl::new);
      put("ORACLE", OracleDatabaseSyncImpl::new);
      put("SQLSERVER", SqlServerDatabaseSyncImpl::new);
      put("SQLSERVER2000", SqlServerDatabaseSyncImpl::new);
      put("POSTGRESQL", PostgresqlDatabaseSyncImpl::new);
      put("GREENPLUM", GreenplumDatabaseSyncImpl::new);
      put("DB2", DB2DatabaseSyncImpl::new);
      put("DM", DmDatabaseSyncImpl::new);
      put("KINGBASE", KingbaseDatabaseSyncImpl::new);
    }
  };

  /**
   * 获取指定数据源的同步器
   *
   * @param dataSource 数据源
   * @return 同步器对象
   */
  public static IDatabaseSynchronize createDatabaseWriter(DataSource dataSource) {
    String type = DatabaseAwareUtils.getDatabaseTypeByDataSource(dataSource).name();
    if (!DATABASE_SYNC_MAPPER.containsKey(type)) {
      throw new RuntimeException(
          String.format("[dbsynch] Unsupported database type (%s)", type));
    }

    return DATABASE_SYNC_MAPPER.get(type).apply(dataSource);
  }
}
