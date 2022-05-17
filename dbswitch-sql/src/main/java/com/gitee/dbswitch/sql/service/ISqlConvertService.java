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

import java.util.Map;
import com.gitee.dbswitch.common.type.DatabaseTypeEnum;

/**
 * SQL语言共分为四大类：数据查询语言DQL，数据操纵语言DML，数据定义语言DDL，数据控制语言DCL
 * 
 * @author tang
 *
 */
public interface ISqlConvertService {

	/**
	 * 标准DQL/DML类SQL的转换
	 * 
	 * @param sql 待转换的SQL语句
	 * @return 转换为三种数据库Oracle/MySQL/PostgreSQL数据库类型后的SQL语句
	 */
	public Map<String, String> dmlSentence(String sql);

	/**
	 * 标准DQL/DML类SQL的转换
	 * 
	 * @param sql 待转换的SQL语句
	 * @return 转换为指定数据库类型后的SQL语句
	 */
	public String dmlSentence(String sql, DatabaseTypeEnum target);

	/**
	 * 指定源数据库到目的数据库的DQL/DML类SQL的转换
	 * 
	 * @param source 源数据库类型
	 * @param target 目的数据库类型
	 * @param sql    待转换的SQL语句
	 * @return 转换为目的数据库类型后的SQL语句
	 */
	public String dmlSentence(DatabaseTypeEnum source, DatabaseTypeEnum target, String sql);

	/**
	 * 标准DDL类SQL的转换
	 * 
	 * @param sql 待转换的SQL语句
	 * @return 转换为三种数据库Oracle/MySQL/PostgreSQL数据库类型后的SQL语句
	 */
	public Map<String, String> ddlSentence(String sql);

	/**
	 * 标准DDL类SQL的转换
	 * 
	 * @param sql 待转换的SQL语句
	 * @return 转换为指定数据库类型后的SQL语句
	 */
	public String ddlSentence(String sql, DatabaseTypeEnum target);

	/**
	 * 指定源数据库到目的数据库的DDL类SQL的转换
	 * 
	 * @param source 源数据库类型
	 * @param target 目的数据库类型
	 * @param sql    待转换的SQL语句
	 * @return 转换为目的数据库类型后的SQL语句
	 */
	public String ddlSentence(DatabaseTypeEnum source, DatabaseTypeEnum target, String sql);

	/**
	 * 标准DCL类SQL的转换
	 * 
	 * @param sql 待转换的SQL语句
	 * @return 转换为三种数据库Oracle/MySQL/PostgreSQL数据库类型后的SQL语句
	 */
	public Map<String, String> dclSentence(String sql);

	/**
	 * 指定源数据库到目的数据库的DCL类SQL的转换
	 * 
	 * @param source 源数据库类型
	 * @param target 目的数据库类型
	 * @param sql    待转换的SQL语句
	 * @return 转换为目的数据库类型后的SQL语句
	 */
	public String dclSentence(DatabaseTypeEnum source, DatabaseTypeEnum target, String sql);
}
