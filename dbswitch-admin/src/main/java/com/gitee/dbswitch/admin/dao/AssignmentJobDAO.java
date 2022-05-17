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

import com.gitee.dbswitch.admin.entity.AssignmentJobEntity;
import com.gitee.dbswitch.admin.mapper.AssignmentJobMapper;
import com.gitee.dbswitch.admin.model.ops.OpsTaskJobTrend;
import com.gitee.dbswitch.admin.type.JobStatusEnum;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.entity.Example;

@Repository
public class AssignmentJobDAO {

  @Resource
  private AssignmentJobMapper assignmentJobMapper;

  public AssignmentJobEntity newAssignmentJob(Long assignmentId, Integer scheduleMode,
      String jobKey) {
    AssignmentJobEntity assignmentJobEntity = new AssignmentJobEntity();
    assignmentJobEntity.setAssignmentId(assignmentId);
    assignmentJobEntity.setJobKey(jobKey);
    assignmentJobEntity.setScheduleMode(scheduleMode);
    assignmentJobEntity.setStartTime(new Timestamp(System.currentTimeMillis()));
    assignmentJobEntity.setFinishTime(new Timestamp(System.currentTimeMillis()));
    assignmentJobEntity.setStatus(JobStatusEnum.RUNNING.getValue());
    assignmentJobMapper.insertSelective(assignmentJobEntity);
    return assignmentJobEntity;
  }

  public AssignmentJobEntity getById(Long id) {
    return assignmentJobMapper.selectByPrimaryKey(id);
  }

  public List<AssignmentJobEntity> getByAssignmentId(Long assignmentId) {
    Objects.requireNonNull(assignmentId, "assignmentId不能为null");

    AssignmentJobEntity condition = new AssignmentJobEntity();
    condition.setAssignmentId(assignmentId);

    Example example = new Example(AssignmentJobEntity.class);
    example.createCriteria().andEqualTo(condition);
    example.orderBy("createTime").desc();
    return assignmentJobMapper.selectByExample(example);
  }

  public void updateSelective(AssignmentJobEntity assignmentJobEntity) {
    Objects.requireNonNull(assignmentJobEntity.getId(), "AssignmentJob的id不能为null");
    assignmentJobMapper.updateByPrimaryKeySelective(assignmentJobEntity);
  }

  public int getCountByStatus(JobStatusEnum status) {
    AssignmentJobEntity condition = new AssignmentJobEntity();
    condition.setStatus(status.getValue());

    Example example = new Example(AssignmentJobEntity.class);
    example.createCriteria().andEqualTo(condition);
    return assignmentJobMapper.selectCountByExample(example);
  }

  public int getTotalCount() {
    return Optional.ofNullable(assignmentJobMapper.selectAll())
        .orElseGet(ArrayList::new).size();
  }

  public List<OpsTaskJobTrend> queryTaskJobTrend(Integer days) {
    return assignmentJobMapper.queryTaskJobTrend(days);
  }

}
