// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.database;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.impl.DatabaseDB2Impl;
import com.gitee.dbswitch.core.database.impl.DatabaseDmImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseGreenplumImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseHiveImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseKingbaseImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseMariaDBImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseMysqlImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseOracleImpl;
import com.gitee.dbswitch.core.database.impl.DatabasePostgresImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseSqlserver2000Impl;
import com.gitee.dbswitch.core.database.impl.DatabaseSqlserverImpl;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

/**
 * 数据库实例构建工厂类
 *
 * @author tang
 */
public final class DatabaseFactory {

  private static final Map<DatabaseTypeEnum, String> DATABASE_MAPPER = new HashMap<DatabaseTypeEnum, String>() {

    private static final long serialVersionUID = 9202705534880971997L;

    {
      put(DatabaseTypeEnum.MYSQL, DatabaseMysqlImpl.class.getName());
      put(DatabaseTypeEnum.ORACLE, DatabaseOracleImpl.class.getName());
      put(DatabaseTypeEnum.SQLSERVER2000, DatabaseSqlserver2000Impl.class.getName());
      put(DatabaseTypeEnum.SQLSERVER, DatabaseSqlserverImpl.class.getName());
      put(DatabaseTypeEnum.POSTGRESQL, DatabasePostgresImpl.class.getName());
      put(DatabaseTypeEnum.GREENPLUM, DatabaseGreenplumImpl.class.getName());
      put(DatabaseTypeEnum.MARIADB, DatabaseMariaDBImpl.class.getName());
      put(DatabaseTypeEnum.DB2, DatabaseDB2Impl.class.getName());
      put(DatabaseTypeEnum.DM, DatabaseDmImpl.class.getName());
      put(DatabaseTypeEnum.KINGBASE, DatabaseKingbaseImpl.class.getName());
      put(DatabaseTypeEnum.HIVE, DatabaseHiveImpl.class.getName());
    }
  };

  public static AbstractDatabase getDatabaseInstance(DatabaseTypeEnum type) {
    if (DATABASE_MAPPER.containsKey(type)) {
      String className = DATABASE_MAPPER.get(type);
      try {
        return (AbstractDatabase) Class.forName(className).newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    throw new UnsupportedOperationException(
        String.format("Unknown database type (%s)", type.name()));
  }

  private DatabaseFactory() {
  }

}
