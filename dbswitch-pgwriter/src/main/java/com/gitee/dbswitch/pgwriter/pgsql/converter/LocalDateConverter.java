package com.gitee.dbswitch.pgwriter.pgsql.converter;

import com.gitee.dbswitch.pgwriter.pgsql.utils.TimeStampUtils;
import java.time.LocalDate;

public class LocalDateConverter implements IValueConverter<LocalDate, Integer> {

  @Override
  public Integer convert(final LocalDate date) {
    return TimeStampUtils.toPgDays(date);
  }

}
