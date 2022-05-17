// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.sql.ddl;

import com.gitee.dbswitch.sql.ddl.pojo.ColumnDefinition;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据库方言抽象类
 *
 * @author tang
 */
public abstract class AbstractDatabaseDialect {

  public String getSchemaTableName(String schemaName, String tableName) {
    return String.format("\"%s\".\"%s\"", schemaName.trim(), tableName.trim());
  }

  public String getQuoteFieldName(String fieldName) {
    return String.format("\"%s\"", fieldName.trim());
  }

  public abstract String getFieldTypeName(ColumnDefinition column);

  public abstract String getFieldDefination(ColumnDefinition column);

  public String getPrimaryKeyAsString(List<String> pks) {
    if (!pks.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("\"");
      sb.append(StringUtils.join(pks, "\" , \""));
      sb.append("\"");
      return sb.toString();
    }

    return "";
  }

}
