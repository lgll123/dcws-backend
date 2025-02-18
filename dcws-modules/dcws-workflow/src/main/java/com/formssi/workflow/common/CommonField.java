package com.formssi.workflow.common;

import java.io.Serializable;
import lombok.Data;

@Data
public class CommonField implements Serializable {

  String table;

  String primaryKey;

  String status;

}
