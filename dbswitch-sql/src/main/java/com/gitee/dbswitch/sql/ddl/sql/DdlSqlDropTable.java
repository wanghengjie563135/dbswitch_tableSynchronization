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
import com.gitee.dbswitch.sql.ddl.pojo.TableDefinition;

/**
 * Drop语句操作类
 *
 * @author tang
 */
public class DdlSqlDropTable extends AbstractSqlDdlOperator {

  private TableDefinition table;

  public DdlSqlDropTable(TableDefinition t) {
    super(Const.DROP_TABLE);
    this.table = t;
  }

  @Override
  public String toSqlString(AbstractDatabaseDialect dialect) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getName());
    String fullTableName = dialect.getSchemaTableName(table.getSchemaName(), table.getTableName());
    sb.append(fullTableName);
    return sb.toString();
  }

}