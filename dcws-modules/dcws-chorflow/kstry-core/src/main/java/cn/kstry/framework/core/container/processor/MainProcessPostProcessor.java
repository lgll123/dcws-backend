/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.container.processor;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.InclusiveGateway;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.StartEventImpl;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.container.processor.MainProcessPostProcessor.MainProcessPostContext;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主流程处理
 *
 * @author lykan
 */
public class MainProcessPostProcessor extends DiagramTraverseSupport<MainProcessPostContext> implements StartEventPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainProcessPostProcessor.class);

    public MainProcessPostProcessor() {
        super(start -> new MainProcessPostContext((StartEventImpl) start), false);
    }

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        traverse(startEvent);
        return Optional.of(startEvent);
    }

    @Override
    public void doLast(MainProcessPostContext course, StartEvent startEvent) {
        course.startEvent.setMidwayStartIdMapping(course.midwayStartIdMapping);
    }

    @Override
    public int getOrder() {
        return 48;
    }

    @Override
    public void doPlainElement(MainProcessPostContext course, FlowElement flowElement, SubProcess subProcess) {
        LOGGER.debug("ServiceTaskImpl: {}", flowElement);
        String midwayStartId = "";
        if (flowElement instanceof InclusiveGateway) {
              midwayStartId = GlobalUtil.transferNotEmpty(flowElement, InclusiveGateway.class).getMidwayStartId();
            if (StringUtils.isBlank(midwayStartId)) {
                return;
            }
            Map<String, FlowElement> midwayStartIdMapping = course.midwayStartIdMapping;
            AssertUtil.isNull(midwayStartIdMapping.get(midwayStartId), ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                    "MidwayStartId cannot be defined repeatedly in a Story. midwayStartId: {}", midwayStartId);
            midwayStartIdMapping.put(midwayStartId, flowElement);
        }else if ( flowElement instanceof ServiceTask) {
            // todo
              midwayStartId = GlobalUtil.transferNotEmpty(flowElement, ServiceTask.class).getMidwayStartId();
            if (StringUtils.isBlank(midwayStartId)) {
                return;
            }
            Map<String, FlowElement> midwayStartIdMapping = course.midwayStartIdMapping;
            AssertUtil.isNull(midwayStartIdMapping.get(midwayStartId), ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                "MidwayStartId cannot be defined repeatedly in a Story. midwayStartId: {}", midwayStartId);
            midwayStartIdMapping.put(midwayStartId, flowElement);
        }

    }

    static class MainProcessPostContext {

        private final StartEventImpl startEvent;

        public MainProcessPostContext(StartEventImpl startEvent) {
            this.startEvent = startEvent;
        }

        private final Map<String, FlowElement> midwayStartIdMapping = Maps.newHashMap();
    }
}
