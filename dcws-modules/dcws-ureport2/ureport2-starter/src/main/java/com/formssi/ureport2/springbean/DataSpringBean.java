package com.formssi.ureport2.springbean;

import com.formssi.ureport2.dto.UserDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * SpringBean DEMO
 *
 * @author zhangmiao
 */
@Component("dataSpringBean")
public class DataSpringBean {

  public List<Map<String, Object>> listLocalMap(String dsName, String datasetName,
      Map<String, Object> parameters) {
    List<Map<String, Object>> list = new ArrayList<>();
    Map<String, Object> m1 = new HashMap<>();
    m1.put("year", "2024");
    m1.put("month", "7");
    m1.put("amount", 7100);
    list.add(m1);
    Map<String, Object> m2 = new HashMap<>();
    m2.put("year", "2024");
    m2.put("month", "8");
    m2.put("amount", 6200);
    list.add(m2);
    return list;
  }

  public List<UserDTO> listLocalUserDTO(String dsName, String datasetName,
      Map<String, Object> parameters) {
    List<UserDTO> list = new ArrayList<>();
    for (long i = 0; i < 10; i++) {
      UserDTO user = new UserDTO();
      user.setId(i);
      user.setUserName("userName_" + i % 2);
      user.setNickName("nickName_" + i % 2);
      user.setAccount("account_" + i);
      user.setAmount(100 + i);
      list.add(user);
    }
    return list;
  }

}