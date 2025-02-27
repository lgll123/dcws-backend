package org.dromara.test;

import com.formssi.common.core.config.LceAdminConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 单元测试案例
 *
 * @author Lion Li
 */

public class DemoUnitTest {

    public static void main(String[] args) {
        // 创建一个Date对象（假设为当前日期和时间）
        Date currentDate = new Date();

        // 将Date对象转换为ZonedDateTime对象（需要指定时区）
        ZonedDateTime zonedDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault());

        // 转换为LocalDate对象（如果只需要日期部分）
        LocalDate localDate = zonedDateTime.toLocalDate();

        // 将日期增加一天
        LocalDate newLocalDate = localDate.plusDays(1);

        // 如果需要Date对象，可以将LocalDate转换回ZonedDateTime，再转换为Date
        ZonedDateTime newZonedDateTime = newLocalDate.atStartOfDay(ZoneId.systemDefault());
        Date newDate = Date.from(newZonedDateTime.toInstant());

        // 输出结果
        System.out.println("当前日期: " + currentDate);
        System.out.println("增加一天后的日期: " + newDate);

        // 如果你只需要LocalDate（不需要时间部分），可以直接输出newLocalDate
        // System.out.println("增加一天后的日期（LocalDate）: " + newLocalDate);
    }


}
