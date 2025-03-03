package com.formssi.workflow.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.*;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DcwsDateUtils extends org.apache.commons.lang3.time.DateUtils {

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date startDate, Date endDate) {
        long time = endDate.getTime()- startDate.getTime();
        long day = time / (24 * 60 * 60 * 1000);
        long hour = (time / (60 * 60 * 1000) - day * 24);
        long minute = ((time / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (time / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

        if (day > 0) {
            return day + "天" + hour + "小时" + minute + "分钟";
        }
        if (hour > 0) {
            return hour + "小时" + minute + "分钟";
        }
        if (minute > 0) {
            return minute + "分钟";
        }
        if (second > 0) {
            return second + "秒";
        } else {
            return 0 + "秒";
        }
    }



    /**
     * 增加天数
     */
    public static Date plusDays(Date date ,long daysToAdd) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault());
        LocalDate localDate = zonedDateTime.toLocalDate();
        LocalDate newLocalDate = localDate.plusDays(daysToAdd);
        ZonedDateTime newZonedDateTime = newLocalDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(newZonedDateTime.toInstant());
    }
}
