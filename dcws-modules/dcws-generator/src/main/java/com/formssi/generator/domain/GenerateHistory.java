package com.formssi.generator.domain;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("generate_history")
public class GenerateHistory {
	private Integer id;
	private String configContent;
	private String md5Value;
	private Date generateTime;
	private Integer version;
}