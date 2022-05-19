// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.sql.ddl.sql;

import com.gitee.dbswitch.sql.constant.Const;
import com.gitee.dbswitch.sql.ddl.AbstractDatabaseDialect;
import com.gitee.dbswitch.sql.ddl.AbstractSqlDdlOperator;
import com.gitee.dbswitch.sql.ddl.pojo.ColumnDefinition;
import com.gitee.dbswitch.sql.ddl.pojo.TableDefinition;
import com.gitee.dbswitch.sql.ddl.sql.impl.MySqlDialectImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Create语句操作类
 *
 * @author tang
 */
public class DdlSqlCreateTable extends AbstractSqlDdlOperator {

  private TableDefinition table;

  public DdlSqlCreateTable(TableDefinition t) {
    super(Const.CREATE_TABLE);
    this.table = t;
  }

  @Override
  public String toSqlString(AbstractDatabaseDialect dialect) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getName());
    String fullTableName = dialect.getSchemaTableName(table.getSchemaName(), table.getTableName());
    sb.append(fullTableName);
    sb.append(" (");
    sb.append(Const.CR);

    List<ColumnDefinition> columns = table.getColumns();
    List<String> pks = new ArrayList<>();
    for (int i = 0; i < columns.size(); ++i) {
      ColumnDefinition c = columns.get(i);
      if (c.isPrimaryKey()) {
        pks.add(c.getColumnName());
      }

      if (i > 0) {
        sb.append(",");
      } else {
        sb.append("  ");
      }

      String definition = dialect.getFieldDefination(c);
      sb.append(definition);
      sb.append(Const.CR);
    }

    if (!pks.isEmpty()) {
      String pk = dialect.getPrimaryKeyAsString(pks);
      sb.append(", PRIMARY KEY (").append(pk).append(")").append(Const.CR);
    }

    sb.append(" )");
    if (dialect instanceof MySqlDialectImpl) {
      sb.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8 ");
    }

    sb.append(Const.CR);
    return sb.toString();
  }

}
