// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.entity;

import java.util.concurrent.TimeUnit;
import lombok.Data;

@Data
public class TargetDataSourceProperties {

  private String url;
  private String driverClassName;
  private String username;
  private String password;
  private Long connectionTimeout = TimeUnit.SECONDS.toMillis(60);
  private Long maxLifeTime = TimeUnit.MINUTES.toMillis(60);

  private String targetSchema = "";
  private Boolean targetDrop = Boolean.TRUE;
  private Boolean createTableAutoIncrement = Boolean.FALSE;
  private Boolean writerEngineInsert = Boolean.FALSE;
  private Boolean changeDataSync = Boolean.FALSE;
}
