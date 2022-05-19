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

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.model.ColumnDescription;
import java.util.List;

/**
 * 支持MariaDB数据库的元信息实现
 *
 * @author tang
 */
public class DatabaseMariaDBImpl extends DatabaseMysqlImpl {

  public DatabaseMariaDBImpl() {
    super("org.mariadb.jdbc.Driver");
  }

  @Override
  public DatabaseTypeEnum getDatabaseType() {
    return DatabaseTypeEnum.MARIADB;
  }

}
