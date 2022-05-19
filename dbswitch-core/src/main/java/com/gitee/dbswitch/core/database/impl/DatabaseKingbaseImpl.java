// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.database.impl;

import com.gitee.dbswitch.common.constant.Const;
import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.IDatabaseInterface;
import com.gitee.dbswitch.core.database.constant.PostgresqlConst;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 支持Kingbase数据库的元信息实现
 *
 * @author tang
 */
public class DatabaseKingbaseImpl extends AbstractDatabase implements IDatabaseInterface {

  private static final String SHOW_CREATE_VIEW_SQL =
      "SELECT pg_get_viewdef((select pg_class.relfilenode from pg_catalog.pg_class \n"
          + "join pg_catalog.pg_namespace on pg_class.relnamespace = pg_namespace.oid \n"
          + "where pg_namespace.nspname='%s' and pg_class.relname ='%s'),true) ";

  public DatabaseKingbaseImpl() {
    super("com.kingbase8.Driver");
  }

  @Override
  public DatabaseTypeEnum getDatabaseType() {
    return DatabaseTypeEnum.KINGBASE;
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    String sql = PostgresqlConst.CREATE_TABLE_SQL_TPL
        .replace(PostgresqlConst.TPL_KEY_SCHEMA, schemaName)
        .replace(PostgresqlConst.TPL_KEY_TABLE, tableName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return rs.getString(1);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_VIEW_SQL, schemaName, tableName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return rs.getString(1);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format(" %s LIMIT 0 ", sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
  }

  @Override
  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    return String.format("SELECT * FROM \"%s\".\"%s\"  ", schemaName, tableName);
  }

  @Override
  protected String getTestQuerySQL(String sql) {
    return String.format("explain %s", sql.replace(";", ""));
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc,
      boolean addCr) {
    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();
    int type = v.getType();

    String retval = " \"" + fieldname + "\"   ";

    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
        retval += "TIMESTAMP";
        break;
      case ColumnMetaData.TYPE_TIME:
        retval += "TIME";
        break;
      case ColumnMetaData.TYPE_DATE:
        retval += "DATE";
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval += "BOOLEAN";
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_INTEGER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          if (useAutoInc) {
            retval += "BIGSERIAL";
          } else {
            retval += "BIGINT";
          }
        } else {
          if (length > 0) {
            if (precision > 0 || length > 18) {
              if ((length + precision) > 0 && precision > 0) {
                // Numeric(Precision, Scale): Precision = total length; Scale = decimal places
                retval += "NUMERIC(" + (length + precision) + ", " + precision + ")";
              } else {
                retval += "DOUBLE PRECISION";
              }
            } else {
              if (length > 9) {
                retval += "BIGINT";
              } else {
                if (length < 5) {
                  retval += "SMALLINT";
                } else {
                  retval += "INTEGER";
                }
              }
            }

          } else {
            retval += "DOUBLE PRECISION";
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length < 1 || length >= AbstractDatabase.CLOB_LENGTH) {
          retval += "TEXT";
        } else {
          if (null != pks && pks.contains(fieldname)) {
            retval += "VARCHAR(" + length + ")";
          } else {
            retval += "TEXT";
          }
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "BYTEA";
        break;
      default:
        retval += "TEXT";
        break;
    }

    if (addCr) {
      retval += Const.CR;
    }

    return retval;
  }
}
