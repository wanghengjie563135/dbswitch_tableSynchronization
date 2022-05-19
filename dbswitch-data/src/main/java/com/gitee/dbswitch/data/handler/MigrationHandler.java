// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.handler;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.common.util.DatabaseAwareUtils;
import com.gitee.dbswitch.common.util.PatterNameUtils;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataByDatasourceService;
import com.gitee.dbswitch.core.service.impl.MetaDataByDataSourceServiceImpl;
import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.entity.SourceDataSourceProperties;
import com.gitee.dbswitch.data.util.BytesUnitUtils;
import com.gitee.dbswitch.dbchange.ChangeCalculatorService;
import com.gitee.dbswitch.dbchange.IDatabaseChangeCaculator;
import com.gitee.dbswitch.dbchange.IDatabaseRowHandler;
import com.gitee.dbswitch.dbchange.RecordChangeTypeEnum;
import com.gitee.dbswitch.dbchange.TaskParamEntity;
import com.gitee.dbswitch.dbcommon.database.DatabaseOperatorFactory;
import com.gitee.dbswitch.dbcommon.database.IDatabaseOperator;
import com.gitee.dbswitch.dbcommon.domain.StatementResultSet;
import com.gitee.dbswitch.dbsynch.DatabaseSynchronizeFactory;
import com.gitee.dbswitch.dbsynch.IDatabaseSynchronize;
import com.gitee.dbswitch.dbwriter.DatabaseWriterFactory;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.sizeof.SizeOf;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * 在一个线程内的单表迁移处理逻辑
 *
 * @author tang
 */
@Slf4j
public class MigrationHandler implements Supplier<Long> {

  private final long MAX_CACHE_BYTES_SIZE = 64 * 1024 * 1024;

  private int fetchSize = 100;
  private final DbswichProperties properties;
  private final SourceDataSourceProperties sourceProperties;

  // 来源端
  private final HikariDataSource sourceDataSource;
  private DatabaseTypeEnum sourceProductType;
  private String sourceSchemaName;
  private String sourceTableName;
  private List<ColumnDescription> sourceColumnDescriptions;
  private List<String> sourcePrimaryKeys;

  private IMetaDataByDatasourceService sourceMetaDataService;

  // 目的端
  private final HikariDataSource targetDataSource;
  private DatabaseTypeEnum targetProductType;
  private String targetSchemaName;
  private String targetTableName;
  private List<ColumnDescription> targetColumnDescriptions;
  private List<String> targetPrimaryKeys;

  // 日志输出字符串使用
  private String tableNameMapString;

  public static MigrationHandler createInstance(TableDescription td,
                                                DbswichProperties properties,
                                                Integer sourcePropertiesIndex,
                                                HikariDataSource sds,
                                                HikariDataSource tds) {
    return new MigrationHandler(td, properties, sourcePropertiesIndex, sds, tds);
  }

  private MigrationHandler(TableDescription td,
                           DbswichProperties properties,
                           Integer sourcePropertiesIndex,
                           HikariDataSource sds,
                           HikariDataSource tds) {
    this.sourceSchemaName = td.getSchemaName();
    this.sourceTableName = td.getTableName();
    this.properties = properties;
    this.sourceProperties = properties.getSource().get(sourcePropertiesIndex);
    this.sourceDataSource = sds;
    this.targetDataSource = tds;

    if (sourceProperties.getFetchSize() >= fetchSize) {
      fetchSize = sourceProperties.getFetchSize();
    }

    // 获取映射转换后新的表名
    this.targetSchemaName = properties.getTarget().getTargetSchema();
    this.targetTableName = PatterNameUtils.getFinalName(td.getTableName(),
            sourceProperties.getRegexTableMapper());

    if (StringUtils.isEmpty(this.targetTableName)) {
      throw new RuntimeException("表名的映射规则配置有误，不能将[" + this.sourceTableName + "]映射为空");
    }

    this.tableNameMapString = String.format("%s.%s --> %s.%s",
            td.getSchemaName(), td.getTableName(),
            targetSchemaName, targetTableName);
  }

  @Override
  public Long get() {
    log.info("Begin Migrate table for {}", tableNameMapString);

    this.sourceProductType = DatabaseAwareUtils.getDatabaseTypeByDataSource(sourceDataSource);
    this.targetProductType = DatabaseAwareUtils.getDatabaseTypeByDataSource(targetDataSource);
    this.sourceMetaDataService = new MetaDataByDataSourceServiceImpl(sourceDataSource,
            sourceProductType);

    // 读取源表的字段元数据
    this.sourceColumnDescriptions = sourceMetaDataService
            .queryTableColumnMeta(sourceSchemaName, sourceTableName);
    this.sourcePrimaryKeys = sourceMetaDataService
            .queryTablePrimaryKeys(sourceSchemaName, sourceTableName);

    // 根据表的列名映射转换准备目标端表的字段信息
    this.targetColumnDescriptions = sourceColumnDescriptions.stream()
            .map(column -> {
              String newName = PatterNameUtils.getFinalName(
                      column.getFieldName(),
                      sourceProperties.getRegexColumnMapper());
              ColumnDescription description = column.copy();
              description.setFieldName(newName);
              description.setLabelName(newName);
              return description;
            }).collect(Collectors.toList());
    this.targetPrimaryKeys = sourcePrimaryKeys.stream()
            .map(name ->
                    PatterNameUtils.getFinalName(name, sourceProperties.getRegexColumnMapper())
            ).collect(Collectors.toList());

    // 打印表名与字段名的映射关系
    List<String> columnMapperPairs = new ArrayList<>();
    Map<String, String> mapChecker = new HashMap<>();
    for (int i = 0; i < sourceColumnDescriptions.size(); ++i) {
      String sourceColumnName = sourceColumnDescriptions.get(i).getFieldName();
      String targetColumnName = targetColumnDescriptions.get(i).getFieldName();
      if (StringUtils.hasLength(targetColumnName)) {
        columnMapperPairs.add(String.format("%s --> %s", sourceColumnName, targetColumnName));
        mapChecker.put(sourceColumnName, targetColumnName);
      } else {
        columnMapperPairs.add(String.format(
                "%s --> %s",
                sourceColumnName,
                String.format("<!Field(%s) is Deleted>", (i + 1))
        ));
      }
    }
    log.info("Mapping relation : \ntable mapper :\n\t{}  \ncolumn mapper :\n\t{} ",
            tableNameMapString, String.join("\n\t", columnMapperPairs));
    Set<String> valueSet = new HashSet<>(mapChecker.values());
    if (valueSet.size() <= 0) {
      throw new RuntimeException("字段映射配置有误，禁止通过映射将表所有的字段都删除!");
    }
    if (!valueSet.containsAll(this.targetPrimaryKeys)) {
      throw new RuntimeException("字段映射配置有误，禁止通过映射将表的主键字段删除!");
    }
    if (mapChecker.keySet().size() != valueSet.size()) {
      throw new RuntimeException("字段映射配置有误，禁止将多个字段映射到一个同名字段!");
    }

    IDatabaseWriter writer = DatabaseWriterFactory.createDatabaseWriter(
            targetDataSource, properties.getTarget().getWriterEngineInsert());

    if (properties.getTarget().getTargetDrop()) {
      /*
        如果配置了dbswitch.target.datasource-target-drop=true时，
        <p>
        先执行drop table语句，然后执行create table语句
       */

      try {
        DatabaseOperatorFactory.createDatabaseOperator(targetDataSource)
                .dropTable(targetSchemaName, targetTableName);
        log.info("Target Table {}.{} is exits, drop it now !", targetSchemaName, targetTableName);
      } catch (Exception e) {
        log.info("Target Table {}.{} is not exits, create it!", targetSchemaName, targetTableName);
      }

      // 生成建表语句并创建
      String sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
              targetProductType,
              targetColumnDescriptions.stream()
                      .filter(column -> StringUtils.hasLength(column.getFieldName()))
                      .collect(Collectors.toList()),
              targetPrimaryKeys,
              targetSchemaName,
              targetTableName,
              properties.getTarget().getCreateTableAutoIncrement()
      );

      JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
      targetJdbcTemplate.execute(sqlCreateTable);
      log.info("Execute SQL: \n{}", sqlCreateTable);

      String grantSqlUfrmdbca="grant select ,insert ,update ,delete on " +targetSchemaName + '.' + targetTableName + " to user ufrmdbca";
      String grantSqlufrmcena="grant select ,insert ,update ,delete on " + targetSchemaName + '.' + targetTableName + " to user ufrmcena";
      String grantSqludatabca="grant select on " + targetSchemaName + '.' + targetTableName + " to user udatabca";


      targetJdbcTemplate.execute(grantSqlUfrmdbca);
      log.info("授权Execute SQL: \n{}", grantSqlUfrmdbca);
      targetJdbcTemplate.execute(grantSqlufrmcena);
      log.info("授权Execute SQL: \n{}", grantSqlufrmcena);
      targetJdbcTemplate.execute(grantSqludatabca);
      log.info("授权Execute SQL: \n{}", grantSqludatabca);


//      return doFullCoverSynchronize(writer);
    } else {
      // 判断是否具备变化量同步的条件：（1）两端表结构一致，且都有一样的主键字段；(2)MySQL使用Innodb引擎；
      if (properties.getTarget().getChangeDataSync()) {
        // 根据主键情况判断同步的方式：增量同步或覆盖同步
        IMetaDataByDatasourceService metaDataByDatasourceService =
                new MetaDataByDataSourceServiceImpl(targetDataSource, targetProductType);
        List<String> dbTargetPks = metaDataByDatasourceService.queryTablePrimaryKeys(
                targetSchemaName, targetTableName);

      }
    }
    return  0L;
  }
}
