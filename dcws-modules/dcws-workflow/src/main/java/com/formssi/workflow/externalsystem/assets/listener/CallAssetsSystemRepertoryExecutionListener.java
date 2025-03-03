package com.formssi.workflow.externalsystem.assets.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

import java.util.List;
import java.util.Map;

@Slf4j
public class CallAssetsSystemRepertoryExecutionListener implements ExecutionListener {
    private static final String API_URL = "http://10.100.218.4/api/v1/accessories";
    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMDE1YTgxM2ZhMGFlNzkxMzc3Mzc1MmEzZmMxYjc2MzU1NjAxZDU0NzZmZDk3YjJlNzgwMTA5NjE5ZTA2N2Y3MWUzMTM0YTI3OTU1MmFiYmUiLCJpYXQiOjE3Mzg4OTkzNzAuMjIxMjAxLCJuYmYiOjE3Mzg4OTkzNzAuMjIxMjA0LCJleHAiOjIyMTIxOTg1NzAuMjEwNzU5LCJzdWIiOiI3NDEiLCJzY29wZXMiOltdfQ.0hcozE2jwP7nkrt3oKLrF4sG8R2NSva3cVJHRzdM13cyLscGs4-J6IXoZnZc29CSWJJ3uedINH8bfG-vixFDCjZop3C800LnjSz4y4zwvP3-tqAy9PbufEvJYJW4X-84jRHQY14XWcRjM2LE1gleW6yiJfAZV4X3BqXoH3p6jjMlyP9AQAvRjqWMfnEv5gquj2_CJCXjHr-oaPvJDDQccFUiRLWS3vFq7r2ePqj4KhpEmheCwup5lcxJfZnMxIC6eIQmFqOQMqyFON2ukSOeqZsEdFVD__2TGlbMHsc2uGZDAHI3zRY8vZnYQvq4elNXKTkCNapmzARdKOXBMh_i0T3Qu9RQ7sdqdIGDPksp78SaXVsD-JtAn4m6RyEeZEwYV_UWXZDgcj2AF_PmmXgLPQjo-SMl6V0mSNDIVn8LYen_oLvU5Z8MzzbrgQO9nww3XO7Mp0CTWz0y643mFBdkFdYrPeVstoO38Y5Mn7fvwo07MOGuKYtPfP5vbGq8qT0QqJjw3J7swCIZQCAjsp1Mu8yMnTdcV37Qw_e4iqIsOKOjUciJ-H5EfLjU3b13l5mcvGZImEW3mkhC32lqO4Gmw1egM2rkbfW57ZWY6CFX_8ehuOD0vi3uFKnC0mzlfYFcaMrlbwOO5SBgBzp0jSuB9zudn1wP1iD0-8MHKAfhilY";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void notify(DelegateExecution execution) {
        Map<String, Object> variables = execution.getVariables();
        variables.forEach((k,value)->{
            log.info(k+"---------------"+value.toString());
        });
        try {
            log.info("Calling the external system for Assets with URL: {}", API_URL);

            String requestData = "?search=鼠标&limit=50&offset=0&order_number=null&sort=created_at&order=desc&expand=false";
            Request request = new Request.Builder()
                .url(API_URL+ requestData)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    log.info("External system response: {}", responseBody);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map map = objectMapper.readValue(responseBody, Map.class);
                    List<Map> rows = (List<Map>)map.get("rows");
                    Integer remainingQty = (Integer)rows.get(0).get("remaining_qty");
//                    execution.setVariable("remainingQty",remainingQty);
                } else {
                    log.error("External system call failed with status code: {}", response.code());
                    // 抛出BPMN错误，触发错误边界事件
                    throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: "+response.toString());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            // 抛出BPMN错误，触发错误边界事件
            throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: " + e.getMessage());
        }
    }

}
