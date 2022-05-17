// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.dao;

import com.gitee.dbswitch.admin.entity.SystemUserEntity;
import com.gitee.dbswitch.admin.mapper.SystemUserMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class SystemUserDAO {

  @Resource
  private SystemUserMapper systemUserMapper;

  public SystemUserEntity getById(Long id) {
    return systemUserMapper.selectByPrimaryKey(id);
  }

  public SystemUserEntity findByUsername(String username) {
    return systemUserMapper.findByUsername(username);
  }

  public void updateUserPassword(String username, String newPassword) {
    systemUserMapper.updateUserPassword(username, newPassword);
  }
}
