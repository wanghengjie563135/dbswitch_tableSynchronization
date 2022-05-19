// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.sql.service.impl;

import java.util.HashMap;
import java.util.Map;
import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.sql.service.ISqlGeneratorService;
import com.gitee.dbswitch.sql.ddl.AbstractDatabaseDialect;
import com.gitee.dbswitch.sql.ddl.AbstractSqlDdlOperator;
import com.gitee.dbswitch.sql.ddl.pojo.TableDefinition;
import com.gitee.dbswitch.sql.ddl.sql.DdlSqlCreateTable;
import com.gitee.dbswitch.sql.ddl.sql.DdlSqlAlterTable;
import com.gitee.dbswitch.sql.ddl.sql.DdlSqlDropTable;
import com.gitee.dbswitch.sql.ddl.sql.DdlSqlTruncateTable;
import com.gitee.dbswitch.sql.ddl.sql.impl.GreenplumDialectImpl;
import com.gitee.dbswitch.sql.ddl.sql.impl.MySqlDialectImpl;
import com.gitee.dbswitch.sql.ddl.sql.impl.OracleDialectImpl;
import com.gitee.dbswitch.sql.ddl.sql.impl.PostgresDialectImpl;

/**
 * 拼接生成SQL实现类
 * 
 * @author tang
 *
 */
public class MyselfSqlGeneratorServiceImpl implements ISqlGeneratorService {

	private static final Map<DatabaseTypeEnum, String> DATABASE_MAPPER = new HashMap<DatabaseTypeEnum, String>();

	static {
		DATABASE_MAPPER.put(DatabaseTypeEnum.MYSQL, MySqlDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseTypeEnum.ORACLE, OracleDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseTypeEnum.POSTGRESQL, PostgresDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseTypeEnum.GREENPLUM, GreenplumDialectImpl.class.getName());
	}

	public static AbstractDatabaseDialect getDatabaseInstance(DatabaseTypeEnum type) {
		if (DATABASE_MAPPER.containsKey(type)) {
			String className = DATABASE_MAPPER.get(type);
			try {
				return (AbstractDatabaseDialect) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("Unkown database type (%s)", type.name()));
	}

	@Override
	public String createTable(String dbType, TableDefinition t) {
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbType.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlCreateTable(t);
		return operator.toSqlString(dialect);
	}

	@Override
	public String alterTable(String dbType, String handle, TableDefinition t){
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbType.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlAlterTable(t,handle);
		return operator.toSqlString(dialect);
	}

	@Override
	public String dropTable(String dbType, TableDefinition t) {
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbType.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlDropTable(t);
		return operator.toSqlString(dialect);
	}

	@Override
	public String truncateTable(String dbType, TableDefinition t) {
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbType.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlTruncateTable(t);
		return operator.toSqlString(dialect);
	}

}
