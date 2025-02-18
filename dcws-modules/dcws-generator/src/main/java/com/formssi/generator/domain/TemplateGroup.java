package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author : zsljava
 * @date Date : 2020-12-15 9:49
 * @Description:
 */
@Data
@TableName("template_group")
public class TemplateGroup {
    private Integer id;
    private String groupName;
    private Integer isDeleted;

}