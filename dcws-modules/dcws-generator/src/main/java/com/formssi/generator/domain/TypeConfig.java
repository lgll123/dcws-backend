package com.formssi.generator.domain;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 字段类型配置表
 */
@Data
@TableName("type_config")
public class TypeConfig {
	private Integer id;
	/** 数据库类型 */
	private String dbType;
	/** 基本类型 */
	private String baseType;
	/** 装箱类型 */
	private String boxType;
}
