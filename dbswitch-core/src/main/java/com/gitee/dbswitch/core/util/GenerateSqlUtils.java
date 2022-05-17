// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.util;

import com.gitee.dbswitch.common.constant.Const;
import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.DatabaseFactory;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼接SQL工具类
 *
 * @author tang
 */
public class GenerateSqlUtils {

  public static String getDDLCreateTableSQL(
      DatabaseTypeEnum type,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      boolean autoIncr) {
    StringBuilder sb = new StringBuilder();
    List<String> pks = fieldNames.stream()
        .filter((cd) -> primaryKeys.contains(cd.getFieldName()))
        .map((cd) -> cd.getFieldName())
        .collect(Collectors.toList());

    AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);

    sb.append(Const.CREATE_TABLE);
    // if(ifNotExist && type!=DatabaseType.ORACLE) {
    // sb.append( Const.IF_NOT_EXISTS );
    // }
    sb.append(db.getQuotedSchemaTableCombination(schemaName, tableName));
    sb.append("(");

    for (int i = 0; i < fieldNames.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      } else {
        sb.append("  ");
      }

      ColumnMetaData v = fieldNames.get(i).getMetaData();
      sb.append(db.getFieldDefinition(v, pks, autoIncr, false));
    }

    if (!pks.isEmpty()) {
      String pk = db.getPrimaryKeyAsString(pks);
      sb.append(", PRIMARY KEY (").append(pk).append(")");
    }

    sb.append(")");
    if (DatabaseTypeEnum.MYSQL == type) {
      sb.append("ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
    }

    return DDLFormatterUtils.format(sb.toString());
  }
}
