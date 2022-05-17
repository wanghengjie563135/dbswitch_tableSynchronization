// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.sql.service;

import com.gitee.dbswitch.sql.ddl.pojo.TableDefinition;

/**
 * SQL生成接口类
 * 
 * @author tang
 *
 */
public interface ISqlGeneratorService {

	/**
	 * 生成建表语句
	 * 
	 * @param dbtype 数据库类型
	 * @param t      表描述
	 * @return 建表语句
	 */
	public String createTable(String dbtype, TableDefinition t);

	/**
	 * 生成改表语句
	 * 
	 * @param dbtype 数据库类型
	 * @param handle 操作类型
	 * @param t      表描述
	 * @return 建表语句
	 */
	public String alterTable(String dbtype, String handle, TableDefinition t);

	/**
	 * 生成删表语句
	 * 
	 * @param dbtype 数据库类型
	 * @param t      表描述
	 * @return 建表语句
	 */
	public String dropTable(String dbtype, TableDefinition t);

	/**
	 * 生成清表语句
	 * 
	 * @param dbtype 数据库类型
	 * @param t      表描述
	 * @return 建表语句
	 */
	public String truncateTable(String dbtype, TableDefinition t);
}
