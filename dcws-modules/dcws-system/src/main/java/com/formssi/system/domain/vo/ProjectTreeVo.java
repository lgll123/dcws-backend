//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.formssi.system.domain.vo;

import java.util.List;
import lombok.Data;

@Data
public class ProjectTreeVo {
    private Long id;
    private String name;
    private Integer type;
    private List<ProjectTreeVo> children;
}
