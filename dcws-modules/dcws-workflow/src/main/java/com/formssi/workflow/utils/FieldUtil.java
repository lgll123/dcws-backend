package com.formssi.workflow.utils;

import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.formssi.workflow.common.CommonField;
import org.apache.ibatis.reflection.property.PropertyNamer;

public class FieldUtil {

  public static <T, R> String getFieldName(SFunction<T, R> function) {
    LambdaMeta meta = LambdaUtils.extract(function);
    return PropertyNamer.methodToProperty(meta.getImplMethodName());
  }

  public static void main(String[] args) {
    String fieldName = getFieldName(CommonField::getPrimaryKey);
    System.out.println("Field name: " + fieldName);
  }

}
