# Git

## 后端

git clone http://10.31.55.10:18080/dev_4_formssicloud/lce/lce-admin.git

## 前端

git clone http://10.31.55.10:18080/dev_4_formssicloud/lce/lce-web.git

##  

# DB

## DDL

### chor_flow

```sql
DROP TABLE IF EXISTS `chor_flow`;
CREATE TABLE `chor_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `api_id` bigint(20) NOT NULL COMMENT '对应的API的id',
  `flow_name` varchar(100) NOT NULL COMMENT '流程名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述信息',
  `file_content` longtext COMMENT '文件数据，bpmn数据等',
  `img` longtext COMMENT '流程缩略图，Base64数据',
  `system_id` varchar(100) DEFAULT NULL COMMENT '系统id，对应sys_dict_type数据的dict_id字段',
  `module_id` varchar(100) DEFAULT NULL COMMENT '模块id，对应sys_dict_data数据的dict_code字段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除: 1-已删除 0-未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `chor_flow_unique` (`flow_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1873623460040732675 DEFAULT CHARSET=utf8mb4 COMMENT='流程编排表';
```

### sys_bussys

```sql
CREATE TABLE `sys_bussys` (
  `system_code` varchar(100) NOT NULL COMMENT '系统代码',
  `system_name` varchar(100) NOT NULL COMMENT '系统名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除: 1-已删除 0-未删除',
  PRIMARY KEY (`system_code`),
  UNIQUE KEY `sys_bussys_system_name_IDX` (`system_name`) USING BTREE
)  COMMENT='应用系统表';
```

### sys_bussys_module

```sql
CREATE TABLE `sys_bussys_module` (
  `module_code` varchar(100) NOT NULL COMMENT '模块代码',
  `system_code` varchar(100) NOT NULL COMMENT '系统代码,关联sys_system',
  `module_name` varchar(100) NOT NULL COMMENT '模块名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除: 1-已删除 0-未删除',
  PRIMARY KEY (`module_code`,`system_code`),
  KEY `sys_bussys_module_module_name_IDX` (`module_name`) USING BTREE
) COMMENT='应用系统模块表';
```

## DML

### sys_bussys

#### insert

```sql
INSERT INTO low_code_engine.sys_bussys (system_code, system_name, description, create_time, create_by, update_time, update_by, deleted) VALUES('system01', '系统01', '流程编排系统', '2024-12-19 11:39:38', NULL, '2024-12-19 11:39:38', NULL, 0);
INSERT INTO low_code_engine.sys_bussys (system_code, system_name, description, create_time, create_by, update_time, update_by, deleted) VALUES('system02', '系统02', '流程编排系统', '2024-12-19 11:39:38', NULL, '2024-12-19 11:39:38', NULL, 0);
```

#### delete

```sql

```

### sys_bussys_module

#### insert

```sql
INSERT INTO low_code_engine.sys_bussys_module (module_code, system_code, module_name, description, create_time, create_by, update_time, update_by, deleted) VALUES('module01', 'system01', '模块1', '流程编排系统模块1', '2024-12-19 11:40:40', NULL, '2024-12-19 11:40:40', NULL, 0);
INSERT INTO low_code_engine.sys_bussys_module (module_code, system_code, module_name, description, create_time, create_by, update_time, update_by, deleted) VALUES('module02', 'system01', '模块2', '流程编排系统模块2', '2024-12-19 11:40:40', NULL, '2024-12-19 11:40:40', NULL, 0);
INSERT INTO low_code_engine.sys_bussys_module (module_code, system_code, module_name, description, create_time, create_by, update_time, update_by, deleted) VALUES('module03', 'system02', '模块3', '流程编排系统模块3', '2024-12-19 11:40:40', NULL, '2024-12-19 11:40:40', NULL, 0);
```

#### delete

```sql

```

# Code

## java

### 接口文档地址

#### swagger url

http://localhost:8080/swagger-ui.html

#### knife4j url

http://localhost:8080/api-docs/doc.html

#### springdoc-openapi

http://localhost:8080/doc.html

#### Json

http://localhost:8080/v3/api-docs

http://localhost:8080/v3/api-docs/swagger-config

### 新增模块

#### `low-code-modules/low-code-chorflow`

流程编排子模块

#### `low-code-modules/low-code-chorflow-rest`

用于测试流程编排的REST项目

# 测试流程

## 数据库

### 新增流程

```sql
INSERT INTO low_code_engine.chor_flow (id, api_id, flow_name, description, file_content, img, system_id, module_id, create_time, create_by, update_time, update_by, deleted) VALUES(1876510027546148866, 1, 'userFlow', '重试流程请求', '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_0001" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="save_money" name="save_money" isExecutable="true">
    <bpmn:documentation>存钱</bpmn:documentation>
    <bpmn:startEvent id="userFlow" name="userFlow">
      <bpmn:documentation>user流程</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="idempotent" value="true" />
          <camunda:property name="idempotent-keys" value="[&#34;data.id&#34;,&#34;data.name&#34;]" />
          <camunda:property name="transactional-suspend" value="false" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_0vq2psv</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:serviceTask id="register" name="register">
      <bpmn:documentation>注冊</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/register&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;name&#34;:&#34;@req.data.name&#34;,&#10;            &#34;pwd&#34;:&#34;@req.data.pwd&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;register&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0vq2psv</bpmn:incoming>
      <bpmn:outgoing>Flow_1ja7ey4</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_0vq2psv" sourceRef="userFlow" targetRef="register" />
    <bpmn:serviceTask id="login" name="login">
      <bpmn:documentation>登录</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/login&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;name&#34;:&#34;@req.data.name&#34;,&#10;            &#34;pwd&#34;:&#34;@req.data.pwd&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;login&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ja7ey4</bpmn:incoming>
      <bpmn:outgoing>Flow_00ax9ic</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_1ja7ey4" sourceRef="register" targetRef="login" />
    <bpmn:serviceTask id="cost" name="cost">
      <bpmn:documentation>消费</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/cost&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;userId&#34;:&#34;@var.register.response.data.userId&#34;,&#10;            &#34;cost&#34;:&#34;@req.data.cost&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;cost&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1d31owo</bpmn:incoming>
      <bpmn:outgoing>Flow_1obhznk</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_00ax9ic" sourceRef="login" targetRef="Gateway_0qr5ea2" />
    <bpmn:inclusiveGateway id="Gateway_0qr5ea2">
      <bpmn:incoming>Flow_00ax9ic</bpmn:incoming>
      <bpmn:outgoing>Flow_1d31owo</bpmn:outgoing>
      <bpmn:outgoing>Flow_155sbcc</bpmn:outgoing>
    </bpmn:inclusiveGateway>
    <bpmn:sequenceFlow id="Flow_1d31owo" sourceRef="Gateway_0qr5ea2" targetRef="cost" />
    <bpmn:serviceTask id="deposit" name="deposit">
      <bpmn:documentation>储蓄</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/deposit&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;userId&#34;:&#34;@var.register.response.data.userId&#34;,&#10;            &#34;deposit&#34;:&#34;@req.data.deposit&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;deposit&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_155sbcc</bpmn:incoming>
      <bpmn:outgoing>Flow_138ko5a</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_155sbcc" sourceRef="Gateway_0qr5ea2" targetRef="deposit" />
    <bpmn:serviceTask id="getBalance" name="getBalance">
      <bpmn:documentation>获取余额</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/getBalance&#34;,&#10;        &#34;method&#34;:&#34;GET&#34;,&#10;        &#34;params&#34;:{&#10;            &#34;userId&#34;:&#34;@var.register.response.data.userId&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;getBalance&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1obhznk</bpmn:incoming>
      <bpmn:incoming>Flow_138ko5a</bpmn:incoming>
      <bpmn:outgoing>Flow_0gdn936</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_1obhznk" sourceRef="cost" targetRef="getBalance" />
    <bpmn:sequenceFlow id="Flow_138ko5a" sourceRef="deposit" targetRef="getBalance" />
    <bpmn:endEvent id="end" name="end">
      <bpmn:documentation>end</bpmn:documentation>
      <bpmn:incoming>Flow_0gdn936</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_0gdn936" sourceRef="getBalance" targetRef="end" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="save_money">
      <bpmndi:BPMNShape id="Event_1hogd19_di" bpmnElement="userFlow">
        <dc:Bounds x="252" y="302" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="250" y="345" width="46" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_02tz2wt" bpmnElement="register">
        <dc:Bounds x="380" y="280" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_0spj0mr" bpmnElement="login">
        <dc:Bounds x="550" y="280" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0qr5ea2_di" bpmnElement="Gateway_0qr5ea2">
        <dc:Bounds x="700" y="295" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_00685b8" bpmnElement="cost">
        <dc:Bounds x="800" y="200" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_1r2fphm" bpmnElement="deposit">
        <dc:Bounds x="800" y="360" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_0mb9cb3" bpmnElement="getBalance">
        <dc:Bounds x="1000" y="270" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_08caul9_di" bpmnElement="end">
        <dc:Bounds x="1172" y="292" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1181" y="335" width="19" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_0vq2psv_di" bpmnElement="Flow_0vq2psv">
        <di:waypoint x="288" y="320" />
        <di:waypoint x="380" y="320" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ja7ey4_di" bpmnElement="Flow_1ja7ey4">
        <di:waypoint x="480" y="320" />
        <di:waypoint x="550" y="320" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_00ax9ic_di" bpmnElement="Flow_00ax9ic">
        <di:waypoint x="650" y="320" />
        <di:waypoint x="700" y="320" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1d31owo_di" bpmnElement="Flow_1d31owo">
        <di:waypoint x="725" y="295" />
        <di:waypoint x="725" y="240" />
        <di:waypoint x="800" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_155sbcc_di" bpmnElement="Flow_155sbcc">
        <di:waypoint x="725" y="345" />
        <di:waypoint x="725" y="400" />
        <di:waypoint x="800" y="400" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1obhznk_di" bpmnElement="Flow_1obhznk">
        <di:waypoint x="900" y="240" />
        <di:waypoint x="950" y="240" />
        <di:waypoint x="950" y="310" />
        <di:waypoint x="1000" y="310" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_138ko5a_di" bpmnElement="Flow_138ko5a">
        <di:waypoint x="900" y="400" />
        <di:waypoint x="950" y="400" />
        <di:waypoint x="950" y="310" />
        <di:waypoint x="1000" y="310" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0gdn936_di" bpmnElement="Flow_0gdn936">
        <di:waypoint x="1100" y="310" />
        <di:waypoint x="1172" y="310" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
', NULL, NULL, NULL, '2025-01-07 06:03:02', NULL, '2025-01-07 07:25:43', NULL, 0);
```

### bpmn示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_0001" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="save_money" name="save_money" isExecutable="true">
    <bpmn:documentation>存钱</bpmn:documentation>
    <bpmn:startEvent id="userFlow" name="userFlow">
      <bpmn:documentation>user流程</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="idempotent" value="true" />
          <camunda:property name="idempotent-keys" value="[&#34;data.id&#34;,&#34;data.name&#34;]" />
          <camunda:property name="transactional-suspend" value="false" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_0vq2psv</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:serviceTask id="register" name="register">
      <bpmn:documentation>注冊</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/register&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;name&#34;:&#34;@req.data.name&#34;,&#10;            &#34;pwd&#34;:&#34;@req.data.pwd&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;register&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0vq2psv</bpmn:incoming>
      <bpmn:outgoing>Flow_1ja7ey4</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_0vq2psv" sourceRef="userFlow" targetRef="register" />
    <bpmn:serviceTask id="login" name="login">
      <bpmn:documentation>登录</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/login&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;name&#34;:&#34;@req.data.name&#34;,&#10;            &#34;pwd&#34;:&#34;@req.data.pwd&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;login&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ja7ey4</bpmn:incoming>
      <bpmn:outgoing>Flow_00ax9ic</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_1ja7ey4" sourceRef="register" targetRef="login" />
    <bpmn:serviceTask id="cost" name="cost">
      <bpmn:documentation>消费</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/cost&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;name&#34;:&#34;@req.data.name&#34;,&#10;            &#34;cost&#34;:&#34;@req.data.cost&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;cost&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1d31owo</bpmn:incoming>
      <bpmn:outgoing>Flow_1obhznk</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_00ax9ic" sourceRef="login" targetRef="Gateway_0qr5ea2" />
    <bpmn:inclusiveGateway id="Gateway_0qr5ea2">
      <bpmn:incoming>Flow_00ax9ic</bpmn:incoming>
      <bpmn:outgoing>Flow_1d31owo</bpmn:outgoing>
      <bpmn:outgoing>Flow_155sbcc</bpmn:outgoing>
    </bpmn:inclusiveGateway>
    <bpmn:sequenceFlow id="Flow_1d31owo" sourceRef="Gateway_0qr5ea2" targetRef="cost" />
    <bpmn:serviceTask id="deposit" name="deposit">
      <bpmn:documentation>储蓄</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/deposit&#34;,&#10;        &#34;method&#34;:&#34;POST&#34;,&#10;        &#34;body&#34;:{&#10;            &#34;name&#34;:&#34;@req.data.name&#34;,&#10;            &#34;deposit&#34;:&#34;@req.data.deposit&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;deposit&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_155sbcc</bpmn:incoming>
      <bpmn:outgoing>Flow_138ko5a</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_155sbcc" sourceRef="Gateway_0qr5ea2" targetRef="deposit" />
    <bpmn:serviceTask id="getBalance" name="getBalance">
      <bpmn:documentation>获取余额</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-component" value="KstryFlowService" />
          <camunda:property name="task-params" value="{&#10;    &#34;request&#34;:{&#10;        &#34;url&#34;:&#34;http://localhost:8081/user/getBalance&#34;,&#10;        &#34;method&#34;:&#34;GET&#34;,&#10;        &#34;params&#34;:{&#10;            &#34;userId&#34;:&#34;@var.register.response.data.userId&#34;&#10;        }&#10;    },&#10;    &#34;config&#34;:{&#10;        &#34;id&#34;:&#34;getBalance&#34;,&#10;        &#34;transactional&#34;:false,&#10;        &#34;transactional-red&#34;:false&#10;    }&#10;}" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1obhznk</bpmn:incoming>
      <bpmn:incoming>Flow_138ko5a</bpmn:incoming>
      <bpmn:outgoing>Flow_0gdn936</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_1obhznk" sourceRef="cost" targetRef="getBalance" />
    <bpmn:sequenceFlow id="Flow_138ko5a" sourceRef="deposit" targetRef="getBalance" />
    <bpmn:endEvent id="end" name="end">
      <bpmn:documentation>end</bpmn:documentation>
      <bpmn:incoming>Flow_0gdn936</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_0gdn936" sourceRef="getBalance" targetRef="end" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="save_money">
      <bpmndi:BPMNShape id="Event_1hogd19_di" bpmnElement="userFlow">
        <dc:Bounds x="252" y="302" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="250" y="345" width="46" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_02tz2wt" bpmnElement="register">
        <dc:Bounds x="380" y="280" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_0spj0mr" bpmnElement="login">
        <dc:Bounds x="550" y="280" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0qr5ea2_di" bpmnElement="Gateway_0qr5ea2">
        <dc:Bounds x="700" y="295" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_00685b8" bpmnElement="cost">
        <dc:Bounds x="800" y="200" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_1r2fphm" bpmnElement="deposit">
        <dc:Bounds x="800" y="360" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_0mb9cb3" bpmnElement="getBalance">
        <dc:Bounds x="1000" y="270" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_08caul9_di" bpmnElement="end">
        <dc:Bounds x="1172" y="292" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1181" y="335" width="19" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_0vq2psv_di" bpmnElement="Flow_0vq2psv">
        <di:waypoint x="288" y="320" />
        <di:waypoint x="380" y="320" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ja7ey4_di" bpmnElement="Flow_1ja7ey4">
        <di:waypoint x="480" y="320" />
        <di:waypoint x="550" y="320" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_00ax9ic_di" bpmnElement="Flow_00ax9ic">
        <di:waypoint x="650" y="320" />
        <di:waypoint x="700" y="320" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1d31owo_di" bpmnElement="Flow_1d31owo">
        <di:waypoint x="725" y="295" />
        <di:waypoint x="725" y="240" />
        <di:waypoint x="800" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_155sbcc_di" bpmnElement="Flow_155sbcc">
        <di:waypoint x="725" y="345" />
        <di:waypoint x="725" y="400" />
        <di:waypoint x="800" y="400" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1obhznk_di" bpmnElement="Flow_1obhznk">
        <di:waypoint x="900" y="240" />
        <di:waypoint x="950" y="240" />
        <di:waypoint x="950" y="310" />
        <di:waypoint x="1000" y="310" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_138ko5a_di" bpmnElement="Flow_138ko5a">
        <di:waypoint x="900" y="400" />
        <di:waypoint x="950" y="400" />
        <di:waypoint x="950" y="310" />
        <di:waypoint x="1000" y="310" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0gdn936_di" bpmnElement="Flow_0gdn936">
        <di:waypoint x="1100" y="310" />
        <di:waypoint x="1172" y="310" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>

```

### 配置接口信息

在doc_info、doc_param表新增数据，略

### 调用接口

```shell
curl --location --request POST 'http://127.0.0.1:8081/chorflow/request' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb2dpbiIsImxvZ2luSWQiOiJzeXNfdXNlcjoxIiwicm5TdHIiOiJpOUxXbzhyaW1ubDdWeUx3OWVVT0EyMW9VcEIyMlJHdCIsImNsaWVudGlkIjoiZTVjZDdlNDg5MWJmOTVkMWQxOTIwNmNlMjRhN2IzMmUiLCJ0ZW5hbnRJZCI6IjAwMDAwMCIsInVzZXJJZCI6MSwidXNlck5hbWUiOiJhZG1pbiIsImRlcHRJZCI6MTAzLCJkZXB0TmFtZSI6IueglOWPkemDqOmXqCIsImRlcHRDYXRlZ29yeSI6IiJ9.Ws6Oqdzy8G9mKBjtEhvTy_1K8aLZYGCGpaqvJK09dO0' \
--header 'Content-Language: zh_CN' \
--header 'clientid: e5cd7e4891bf95d1d19206ce24a7b32e' \
--header 'Cookie: username=admin; rememberMe=true; password=pG2HuF9XginIQqzN6KtE+cNsLAQrGF2YzEk1HbLCC4UMt5ZtaduU1DFcfJcRbIYQ18BjmfeK4xIykpMqWPNO2w' \
--header 'Content-Type: application/json' \
--data-raw '{
    "startId": "userFlow",  
    "data": {  
        "name": "tom",
        "pwd": "123",
        "cost": 10,
        "deposit": 30
    },
    "responses": [  
        {
            "key": "userId",
            "path": "var.login.response.data.userId"
        },
        {
            "key": "balance",
            "path": "var.getBalance.response.data"
        }
    ]
}'
```

# Others

## JDK17启动报错问题

### 原因

这不是一个代码问题，而是Java命令行参数的一种用法。

`--add-opens` 是 Java 9 引入的一个命令行参数，用于放宽强封装，允许对指定模块的特定包进行深度反射。

在这里，`java.base` 是 Java 的基础模块，`java.lang` 是该模块中的一个包。`ALL-UNNAMED`
表示对未命名模块的所有代码都有这样的深度反射访问权限。

具体来说，`--add-opens java.base/java.lang=ALL-UNNAMED` 的意思是允许所有未命名模块反射访问
`java.base` 模块的 `java.lang` 包中的所有类和成员。

这个参数通常用于运行那些依赖于 Java 核心库的反射 API 的框架或者应用程序，因为 Java 9 之后对 Java
核心库的封装更加严格了。

这个参数通常在运行时使用

### 解决

加入启动参数

```
java --add-opens java.base/java.lang=ALL-UNNAMED -jar your_application.jar
```

## startEvent数据模版

起始节点加入属性方法

加入bpmn:extensionElements-camunda:properties-camunda:property属性，顺序在bpmn:documentation下面，否则会报错

注意：JSON格式符号需转义

### bpmn示例

```xml
 <bpmn:startEvent id="simple" name="simple">
      <bpmn:documentation>simple</bpmn:documentation>
     
	  <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="idempotent" value="true" />
          <camunda:property name="idempotent-keys" value="[&#34;data.id&#34;,&#34;data.name&#34;] />
          <camunda:property name="transactional-suspend" value="false" />
        </camunda:properties>
      </bpmn:extensionElements>
          
      <bpmn:outgoing>Flow_0sfx9m5</bpmn:outgoing>
</bpmn:startEvent>
```

## 服务节点数据模版

服务节点加入属性方法

`<camunda:property name="task-params">`属性，加入json内容

### bpmn示例

```xml
 <bpmn:serviceTask id="getBalance" name="getBalance">
      <bpmn:documentation>获取用户越</bpmn:documentation>
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-service" value="rest-service" />
          <camunda:property name="task-params" value="{&#10;	&#34;request&#34;: {&#10;		&#34;url&#34;: &#34;http://localhost:8888/userAccount/getBalance&#34;,&#10;		&#34;method&#34;: &#34;GET&#34;,&#10;		&#34;headers&#34;: {&#10;			&#34;token&#34;: &#34;@var.verify.response.data.token&#34;&#10;		},&#10;		&#34;params&#34;: {&#10;			&#34;cost&#34;: &#34;@req.data.cost&#34;&#10;		},&#10;		&#34;body&#34;: {&#10;			&#34;costTime&#34;: &#34;2025-01-03 12:12:12&#34;,&#10;			&#34;userId&#34;: &#34;@var.verify.response.data.id&#34;&#10;		}&#10;	},&#10;	&#34;config&#34;: {&#10;		&#34;id&#34;: &#34;getBalance&#34;,&#10;		&#34;transactional&#34;: false,&#10;		&#34;transactional-red&#34;: false&#10;	}&#10;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0vq2psv</bpmn:incoming>
      <bpmn:outgoing>Flow_1o4mfnu</bpmn:outgoing>
    </bpmn:serviceTask>
```

### json示例

`@req` 表示复合接口的请求参数，后面拼接为jsonPath

`@var` 表示前面流程的参数，后面拼接为jsonPath

`@var.verify`表示节点信息id为`verify`的ServiceTask

```json
{
    "request": {
        "url": "http://localhost:8888/userAccount/getBalance",
        "method": "GET",
        "headers": {
            "token": "@var.verify.response.data.token"
        },
        "params": {
            "cost": "@req.data.cost" 
        },
        "body": {
            "costTime": "2025-01-03 12:12:12",
            "userId": "@var.verify.response.data.id"
        }
    },
    "config": {
        "id": "getBalance", //必传，和当前的taskId保持一直
        "transactional": false, //是否处理分布式事务
        "transactional-red": false //是否为分布式事务红线
    }
}
```

## 中途节点启动

**不通过包含网关，从指定 Service Task 开始执行流程的方法**

Issue: https://gitee.com/kstry/kstry-core/issues/IAGNJ9
问题: 为了尽量避免这个问题，midway-start-id放到聚合节点上，可以减少配置异常导致后续节点永不可达的情况