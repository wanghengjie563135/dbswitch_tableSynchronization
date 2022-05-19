// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.controller.publicapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.config.SwaggerConfig;
import com.gitee.dbswitch.sql.ddl.pojo.ColumnDefinition;
import com.gitee.dbswitch.sql.ddl.pojo.TableDefinition;
import com.gitee.dbswitch.sql.service.ISqlGeneratorService;
import com.gitee.dbswitch.sql.service.impl.MyselfSqlGeneratorServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"表结构拼接生成接口"})
@RestController
@RequestMapping(value = SwaggerConfig.API_V1 + "/generator")
public class GeneratorController {

  private ISqlGeneratorService generatorService = new MyselfSqlGeneratorServiceImpl();

  @RequestMapping(value = "/create_table", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "创建表", notes = "根据输入的表结构生成建表SQL语句，请求的示例包体格式为: \n"
      + "{\n" +
      "    \"type\":\"mysql\",\n" +
      "    \"schema_name\":\"public\",\n" +
      "    \"table_name\":\"test_table\",\n" +
      "    \"column_list\":[\n" +
      "        {\n" +
      "            \"field_name\":\"col1\",\n" +
      "            \"comment\":\"列1\",\n" +
      "            \"field_type\":\"int\",\n" +
      "            \"length_or_precision\":11,\n" +
      "            \"scale\":0,\n" +
      "            \"nullable\":0,\n" +
      "            \"primary_key\":1,\n" +
      "            \"auto_increment\":1,\n" +
      "            \"default_value\":null\n" +
      "        },\n" +
      "        {\n" +
      "            \"field_name\":\"col2\",\n" +
      "            \"comment\":\"列2\",\n" +
      "            \"field_type\":\"char\",\n" +
      "            \"length_or_precision\":25,\n" +
      "            \"scale\":0,\n" +
      "            \"nullable\":0,\n" +
      "            \"primary_key\":0,\n" +
      "            \"auto_increment\":0,\n" +
      "            \"default_value\":\"test\"\n" +
      "        }\n" +
      "    ]\n" +
      "}")
  public Result<Map> createTable(@RequestBody String body) {
    JSONObject object = JSON.parseObject(body);
    String type = object.getString("type");
    TableDefinition t = new TableDefinition();
    t.setSchemaName(object.getString("schema_name"));
    t.setTableName(object.getString("table_name"));
    JSONArray jsonArray = object.getJSONArray("column_list");
    for (int i = 0; i < jsonArray.size(); ++i) {
      JSONObject item = jsonArray.getJSONObject(i);
      ColumnDefinition cd = new ColumnDefinition();
      cd.setColumnName(item.getString("field_name"));
      cd.setColumnComment(item.getString("comment"));
      cd.setColumnType(item.getString("field_type"));
      cd.setLengthOrPrecision(item.getInteger("length_or_precision"));
      cd.setScale(item.getInteger("scale"));
      if (item.getInteger("primary_key") > 0) {
        //主键须非空
        cd.setNullable(false);
        cd.setPrimaryKey(true);
      } else {
        cd.setNullable(item.getInteger("nullable") > 0);
        cd.setPrimaryKey(false);
      }
      cd.setAutoIncrement(
          null != item.getInteger("auto_increment") && item.getInteger("auto_increment") > 0);
      cd.setDefaultValue(item.getString("default_value"));

      t.addColumns(cd);
    }

    Map<String, Object> ret = new HashMap<String, Object>();
    ret.put("sql", generatorService.createTable(type, t));
    return Result.success(ret);
  }

  @RequestMapping(value = "/alter_table", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "修改表", notes = "根据输入的修改表结构信息生成修改表的SQL语句，请求的示例包体格式为: \n "
      + "	{\n" +
      "			\"type\":\"mysql\",\n" +
      "		    \"schema_name\":\"public\",\n" +
      "		    \"table_name\":\"test_table\",\n" +
      "		    \"operator\":\"add\",\n" +
      "		    \"column_list\":[\n" +
      "		        {\n" +
      "		            \"field_name\":\"col1\",\n" +
      "		            \"comment\":\"列1\",\n" +
      "		            \"field_type\":\"int\",\n" +
      "		            \"length_or_precision\":11,\n" +
      "		            \"scale\":0,\n" +
      "		            \"nullable\":1,\n" +
      "		            \"primary_key\":0,\n" +
      "		            \"default_value\":null\n" +
      "		        }\n" +
      "		      ]\n" +
      "		}")
  public Result<Map> alterTable(@RequestBody String body) {
    JSONObject object = JSON.parseObject(body);
    String type = object.getString("type");
    String handle = object.getString("operator");
    TableDefinition t = new TableDefinition();
    t.setSchemaName(object.getString("schema_name"));
    t.setTableName(object.getString("table_name"));
    JSONArray jsonArray = object.getJSONArray("column_list");
    for (int i = 0; i < jsonArray.size(); ++i) {
      JSONObject item = jsonArray.getJSONObject(i);
      ColumnDefinition cd = new ColumnDefinition();
      cd.setColumnName(item.getString("field_name"));
      cd.setColumnComment(item.getString("comment"));
      cd.setColumnType(item.getString("field_type"));
      cd.setLengthOrPrecision(item.getInteger("length_or_precision"));
      cd.setScale(item.getInteger("scale"));
      //if(item.getInteger("primary_key")>0) {
      //	//主键须非空
      //	cd.setNullable(false);
      //	cd.setPrimaryKey(true);
      //}else {
      cd.setNullable(item.getInteger("nullable") > 0);
      cd.setPrimaryKey(false);
      //}
      cd.setDefaultValue(item.getString("default_value"));

      t.addColumns(cd);
    }

    Map<String, Object> ret = new HashMap<String, Object>();
    ret.put("sql", generatorService.alterTable(type, handle, t));
    return Result.success(ret);
  }

  @RequestMapping(value = "/drop_table", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "删除表", notes = "根据输入的表信息生成删除表的SQL语句，请求的示例包体格式为:  \n"
      + "	{\n" +
      "			\"type\":\"mysql\",\n" +
      "		    \"schema_name\":\"public\",\n" +
      "		    \"table_name\":\"test_table\"\n" +
      "		}")
  public Result<Map> dropTable(@RequestBody String body) {
    JSONObject object = JSON.parseObject(body);
    String type = object.getString("type");
    TableDefinition t = new TableDefinition();
    t.setSchemaName(object.getString("schema_name"));
    t.setTableName(object.getString("table_name"));

    Map<String, Object> ret = new HashMap<String, Object>();
    ret.put("sql", generatorService.dropTable(type, t));
    return Result.success(ret);
  }

  @RequestMapping(value = "/truncate_table", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "截断表", notes = "根据输入的表信息生成截断表的SQL语句，请求的示例包体格式为:  \n"
      + "	{\n" +
      "			\"type\":\"mysql\",\n" +
      "		    \"schema_name\":\"public\",\n" +
      "		    \"table_name\":\"test_table\"\n" +
      "		}")
  public Result<Map> truncateTable(@RequestBody String body) {
    JSONObject object = JSON.parseObject(body);
    String type = object.getString("type");
    TableDefinition td = new TableDefinition();
    td.setSchemaName(object.getString("schema_name"));
    td.setTableName(object.getString("table_name"));

    Map<String, Object> ret = new HashMap<String, Object>();
    ret.put("sql", generatorService.truncateTable(type, td));
    return Result.success(ret);
  }

}
