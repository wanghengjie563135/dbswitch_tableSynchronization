// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.service.impl;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.common.util.DatabaseAwareUtils;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.DatabaseFactory;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.SchemaTableData;
import com.gitee.dbswitch.core.model.SchemaTableMeta;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataByDatasourceService;
import com.gitee.dbswitch.core.util.GenerateSqlUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

/**
 * 用DataSource对象的元数据获取服务
 *
 * @author tang
 */
public class MetaDataByDataSourceServiceImpl implements IMetaDataByDatasourceService {

  private DataSource dataSource;

  private AbstractDatabase database;

  public MetaDataByDataSourceServiceImpl(DataSource dataSource) {
    this(dataSource, DatabaseAwareUtils.getDatabaseTypeByDataSource(dataSource));
  }

  public MetaDataByDataSourceServiceImpl(DataSource dataSource, DatabaseTypeEnum type) {
    this.dataSource = dataSource;
    this.database = DatabaseFactory.getDatabaseInstance(type);
  }

  @Override
  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public List<String> querySchemaList() {
    try (Connection connection = dataSource.getConnection()) {
      return database.querySchemaList(connection);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<TableDescription> queryTableList(String schemaName) {
    try (Connection connection = dataSource.getConnection()) {
      return database.queryTableList(connection, schemaName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getTableDDL(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return database.getTableDDL(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getViewDDL(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return database.getViewDDL(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<String> queryTableColumnName(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return database.queryTableColumnName(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return database.queryTableColumnMeta(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<ColumnDescription> querySqlColumnMeta(String querySql) {
    try (Connection connection = dataSource.getConnection()) {
      return database.querySelectSqlColumnMeta(connection, querySql);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return database.queryTablePrimaryKeys(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public SchemaTableMeta queryTableMeta(String schemaName, String tableName) {
    SchemaTableMeta tableMeta = new SchemaTableMeta();

    try (Connection connection = dataSource.getConnection()) {
      TableDescription tableDesc = database.queryTableMeta(connection, schemaName, tableName);
      if (null == tableDesc) {
        throw new IllegalArgumentException("Table Or View Not Exist");
      }

      List<ColumnDescription> columns = database.queryTableColumnMeta(
          connection, schemaName, tableName);

      List<String> pks;
      String createSql;
      if (tableDesc.isViewTable()) {
        pks = Collections.emptyList();
        createSql = database.getViewDDL(connection, schemaName, tableName);
      } else {
        pks = database.queryTablePrimaryKeys(connection, schemaName, tableName);
        createSql = database.getTableDDL(connection, schemaName, tableName);
      }

      tableMeta.setSchemaName(schemaName);
      tableMeta.setTableName(tableName);
      tableMeta.setTableType(tableDesc.getTableType());
      tableMeta.setRemarks(tableDesc.getRemarks());
      tableMeta.setColumns(columns);
      tableMeta.setPrimaryKeys(pks);
      tableMeta.setCreateSql(createSql);

      return tableMeta;
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public SchemaTableData queryTableData(String schemaName, String tableName, int rowCount) {
    try (Connection connection = dataSource.getConnection()) {
      return database.queryTableData(connection, schemaName, tableName, rowCount);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public void testQuerySQL(String sql) {
    try (Connection connection = dataSource.getConnection()) {
      database.testQuerySQL(connection, sql);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getDDLCreateTableSQL(DatabaseTypeEnum type, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, boolean autoIncr) {
    return GenerateSqlUtils.getDDLCreateTableSQL(
        type, fieldNames, primaryKeys, schemaName, tableName, autoIncr);
  }

}
