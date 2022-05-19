// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.common.type;

/**
 * 数据库类型的枚举定义
 *
 * @author Tang
 */
public enum DatabaseTypeEnum {
  /**
   * 未知数据库类型
   */
  UNKNOWN(0),

  /**
   * MySQL数据库类型
   */
  MYSQL(1),

  /**
   * Oracle数据库类型
   */
  ORACLE(2),

  /**
   * SQLServer 2000数据库类型
   */
  SQLSERVER2000(3),

  /**
   * SQLServer数据库类型
   */
  SQLSERVER(4),

  /**
   * PostgreSQL数据库类型
   */
  POSTGRESQL(5),

  /**
   * Greenplum数据库类型
   */
  GREENPLUM(6),

  /**
   * MariaDB数据库类型
   */
  MARIADB(7),

  /**
   * DB2数据库类型
   */
  DB2(8),

  /**
   * DM数据库类型
   */
  DM(9),

  /**
   * Kingbase数据库类型
   */
  KINGBASE(10),

  /**
   * HIVE数据库
   */
  HIVE(11),
  ;

  private int index;

  DatabaseTypeEnum(int idx) {
    this.index = idx;
  }

  public int getIndex() {
    return index;
  }

}
