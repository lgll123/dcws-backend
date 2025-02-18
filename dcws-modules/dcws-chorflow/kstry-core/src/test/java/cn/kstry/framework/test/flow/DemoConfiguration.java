package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.test.flow.myself.DemoService;
import cn.kstry.framework.test.util.TestUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableKstry(bpmnPath = "./bpmn/flow/midFlow." + TestUtil.TEST_PROCESS_TYPE)
@PropertySource("classpath:application.properties")
@Import(DemoService.class)
public class DemoConfiguration {

    @Bean(name = "custom-fly-t")
    public ThreadPoolExecutor executor() {
        return new ThreadPoolExecutor(
                5,
                10,
                10,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(3),
                new ThreadFactoryBuilder().setNameFormat("custom-fly-t-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
