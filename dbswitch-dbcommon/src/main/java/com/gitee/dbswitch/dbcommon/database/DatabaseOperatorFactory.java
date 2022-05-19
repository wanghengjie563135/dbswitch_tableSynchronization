// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbcommon.database;

import com.gitee.dbswitch.common.util.DatabaseAwareUtils;
import com.gitee.dbswitch.dbcommon.database.impl.DB2DatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.DmDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.GreenplumDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.HiveDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.KingbaseDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.MysqlDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.OracleDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.PostgreSqlDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.SqlServerDatabaseOperator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * 数据库操作器构造工厂类
 *
 * @author tang
 */
public final class DatabaseOperatorFactory {

  private static final Map<String, Function<DataSource, IDatabaseOperator>> DATABASE_OPERATOR_MAPPER = new HashMap<String, Function<DataSource, IDatabaseOperator>>() {

    private static final long serialVersionUID = -5278835613240515265L;

    {
      put("MYSQL", MysqlDatabaseOperator::new);
      put("MARIADB", MysqlDatabaseOperator::new);
      put("ORACLE", OracleDatabaseOperator::new);
      put("SQLSERVER", SqlServerDatabaseOperator::new);
      put("SQLSERVER2000", SqlServerDatabaseOperator::new);
      put("POSTGRESQL", PostgreSqlDatabaseOperator::new);
      put("GREENPLUM", GreenplumDatabaseOperator::new);
      put("DB2", DB2DatabaseOperator::new);
      put("DM", DmDatabaseOperator::new);
      put("KINGBASE", KingbaseDatabaseOperator::new);
      put("HIVE", HiveDatabaseOperator::new);
    }
  };

  /**
   * 根据数据源获取数据的读取操作器
   *
   * @param dataSource 数据库源
   * @return 指定类型的数据库读取器
   */
  public static IDatabaseOperator createDatabaseOperator(DataSource dataSource) {
    String type = DatabaseAwareUtils.getDatabaseTypeByDataSource(dataSource).name();
    if (!DATABASE_OPERATOR_MAPPER.containsKey(type)) {
      throw new RuntimeException(
          String.format("[dbcommon] Unsupported database type (%s)", type));
    }

    return DATABASE_OPERATOR_MAPPER.get(type).apply(dataSource);
  }

}
