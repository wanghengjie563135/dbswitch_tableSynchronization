// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.service;

import com.gitee.dbswitch.admin.common.converter.ConverterFactory;
import com.gitee.dbswitch.admin.common.excption.DbswitchException;
import com.gitee.dbswitch.admin.common.response.PageResult;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.controller.converter.DbConnectionDetailConverter;
import com.gitee.dbswitch.admin.dao.DatabaseConnectionDAO;
import com.gitee.dbswitch.admin.entity.DatabaseConnectionEntity;
import com.gitee.dbswitch.admin.model.request.DbConnectionCreateRequest;
import com.gitee.dbswitch.admin.model.request.DbConnectionSearchRequest;
import com.gitee.dbswitch.admin.model.request.DbConnectionUpdateRequest;
import com.gitee.dbswitch.admin.model.response.DatabaseTypeDetailResponse;
import com.gitee.dbswitch.admin.model.response.DbConnectionDetailResponse;
import com.gitee.dbswitch.admin.model.response.DbConnectionNameResponse;
import com.gitee.dbswitch.admin.type.SupportDbTypeEnum;
import com.gitee.dbswitch.admin.util.JDBCURL;
import com.gitee.dbswitch.admin.util.PageUtils;
import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.service.IMetaDataByJdbcService;
import com.gitee.dbswitch.core.service.impl.MetaDataByJdbcServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DbConnectionService {

  @Resource
  private DatabaseConnectionDAO databaseConnectionDAO;

  public IMetaDataByJdbcService getMetaDataCoreService(DatabaseConnectionEntity dbConn) {
    String typeName = dbConn.getType().getName().toUpperCase();
    SupportDbTypeEnum supportDbType = SupportDbTypeEnum.valueOf(typeName);
    for (String pattern : supportDbType.getUrl()) {
      final Matcher matcher = JDBCURL.getPattern(pattern).matcher(dbConn.getUrl());
      if (!matcher.matches()) {
        if (1 == supportDbType.getUrl().length) {
          throw new DbswitchException(ResultCode.ERROR_CANNOT_CONNECT_REMOTE, dbConn.getName());
        } else {
          continue;
        }
      }

      String host = matcher.group("host");
      String port = matcher.group("port");
      if (StringUtils.isBlank(port)) {
        port = String.valueOf(supportDbType.getPort());
      }

      if (!JDBCURL.reachable(host, port)) {
        throw new DbswitchException(ResultCode.ERROR_CANNOT_CONNECT_REMOTE, dbConn.getName());
      }
    }
    DatabaseTypeEnum prd = DatabaseTypeEnum.valueOf(dbConn.getType().getName().toUpperCase());
    IMetaDataByJdbcService metaDataService = new MetaDataByJdbcServiceImpl(prd);
    return metaDataService;
  }

  public List<DatabaseTypeDetailResponse> listTypeAll() {
    List<DatabaseTypeDetailResponse> lists = new ArrayList<>();
    for (SupportDbTypeEnum type : SupportDbTypeEnum.values()) {
      DatabaseTypeDetailResponse detail = new DatabaseTypeDetailResponse();
      detail.setId(type.getId());
      detail.setType(type.getName().toUpperCase());
      detail.setDriver(type.getDriver());
      detail.setTemplate(StringUtils.join(type.getUrl(), ","));

      lists.add(detail);
    }

    return lists;
  }

  public PageResult<DbConnectionDetailResponse> listAll(
      DbConnectionSearchRequest request,
      Integer page, Integer size) {
    Supplier<List<DbConnectionDetailResponse>> method = () -> {
      List<DatabaseConnectionEntity> databaseConnectionEntities = databaseConnectionDAO
          .listAll(request.getSearchText());
      return ConverterFactory.getConverter(DbConnectionDetailConverter.class)
          .convert(databaseConnectionEntities);
    };

    return PageUtils.getPage(method, page, size);
  }

  public DbConnectionDetailResponse getDetailById(Long id) {
    return ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(getDatabaseConnectionById(id));
  }

  public Result test(Long id) {
    DatabaseConnectionEntity dbConn = getDatabaseConnectionById(id);
    IMetaDataByJdbcService metaDataService = getMetaDataCoreService(dbConn);
    metaDataService.testQuerySQL(
        dbConn.getUrl(),
        dbConn.getUsername(),
        dbConn.getPassword(),
        dbConn.getType().getSql()
    );
    return Result.success();
  }

  public Result<List<String>> getSchemas(Long id) {
    DatabaseConnectionEntity dbConn = getDatabaseConnectionById(id);
    IMetaDataByJdbcService metaDataService = getMetaDataCoreService(dbConn);
    List<String> schemas = metaDataService.querySchemaList(
        dbConn.getUrl(), dbConn.getUsername(), dbConn.getPassword());
    return Result.success(schemas);
  }

  public Result<List<String>> getSchemaTables(Long id, String schema) {
    DatabaseConnectionEntity dbConn = getDatabaseConnectionById(id);
    IMetaDataByJdbcService metaDataService = getMetaDataCoreService(dbConn);
    List<String> tables = Optional.ofNullable(
        metaDataService.queryTableList(dbConn.getUrl(),
            dbConn.getUsername(),
            dbConn.getPassword(),
            schema))
        .orElseGet(ArrayList::new).stream()
        .filter(t -> !t.isViewTable())
        .map(t -> t.getTableName())
        .collect(Collectors.toList());
    return Result.success(tables);
  }

  public Result<DbConnectionDetailResponse> addDatabaseConnection(
      DbConnectionCreateRequest request) {
    if (StringUtils.isBlank(request.getName())) {
      return Result.failed(ResultCode.ERROR_INVALID_ARGUMENT, "name is empty");
    }

    if (Objects.nonNull(databaseConnectionDAO.getByName(request.getName()))) {
      return Result.failed(ResultCode.ERROR_RESOURCE_ALREADY_EXISTS, "name=" + request.getName());
    }

    DatabaseConnectionEntity conn = request.toDatabaseConnection();
    validJdbcUrlFormat(conn);
    databaseConnectionDAO.insert(conn);

    return Result.success(ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(databaseConnectionDAO.getById(conn.getId())));
  }

  public Result<DbConnectionDetailResponse> updateDatabaseConnection(
      DbConnectionUpdateRequest request) {
    if (Objects.isNull(request.getId()) || Objects
        .isNull(databaseConnectionDAO.getById(request.getId()))) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + request.getId());
    }

    DatabaseConnectionEntity exist = databaseConnectionDAO.getByName(request.getName());
    if (Objects.nonNull(exist) && !exist.getId().equals(request.getId())) {
      return Result.failed(ResultCode.ERROR_RESOURCE_ALREADY_EXISTS, "name=" + request.getName());
    }

    DatabaseConnectionEntity conn = request.toDatabaseConnection();
    validJdbcUrlFormat(conn);
    databaseConnectionDAO.updateById(conn);

    return Result.success(ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(databaseConnectionDAO.getById(conn.getId())));
  }

  public void deleteDatabaseConnection(Long id) {
    databaseConnectionDAO.deleteById(id);
  }

  public PageResult<DbConnectionNameResponse> getNameList(Integer page, Integer size) {
    Supplier<List<DbConnectionNameResponse>> method = () -> {
      List<DatabaseConnectionEntity> lists = databaseConnectionDAO.listAll(null);
      return lists.stream()
          .map(c -> new DbConnectionNameResponse(c.getId(), c.getName()))
          .collect(Collectors.toList());
    };

    return PageUtils.getPage(method, page, size);
  }

  public DatabaseConnectionEntity getDatabaseConnectionById(Long id) {
    DatabaseConnectionEntity dbConn = databaseConnectionDAO.getById(id);
    if (Objects.isNull(dbConn)) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + id);
    }

    return dbConn;
  }

  private void validJdbcUrlFormat(DatabaseConnectionEntity conn) {
    String typeName = conn.getType().getName().toUpperCase();
    SupportDbTypeEnum supportDbType = SupportDbTypeEnum.valueOf(typeName);
    if (!conn.getUrl().startsWith(supportDbType.getUrlPrefix())) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
    }

    for (int i = 0; i < supportDbType.getUrl().length; ++i) {
      String pattern = supportDbType.getUrl()[i];
      Matcher matcher = JDBCURL.getPattern(pattern).matcher(conn.getUrl());
      if (!matcher.matches()) {
        if (i == supportDbType.getUrl().length - 1) {
          throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
        }
      } else {
        if (supportDbType.hasDatabaseName() && StringUtils.isBlank(matcher.group("database"))) {
          throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL,
              "库名没有指定 :" + conn.getUrl());
        }

        break;
      }
    }
  }

}
