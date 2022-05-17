// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.mapper;

import com.gitee.dbswitch.admin.entity.SystemUserEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SystemUserMapper extends Mapper<SystemUserEntity> {

  @Select("select * from `DBSWITCH_SYSTEM_USER` where username=#{username} limit 1")
  SystemUserEntity findByUsername(@Param("username") String username);

  @Update("update `DBSWITCH_SYSTEM_USER` set password=#{password} where username=#{username} ")
  void updateUserPassword(@Param("username") String username, @Param("password") String password);

}
