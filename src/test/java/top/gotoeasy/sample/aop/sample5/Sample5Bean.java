package top.gotoeasy.sample.aop.sample5;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sample5Bean {

    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        map = new HashMap<>();
        map.put("No.", 1);
        map.put("Name", "张三");
        map.put("Date", new Date());
        list.add(map);

        map = new HashMap<>();
        map.put("No.", 2);
        map.put("Name", "李四");
        map.put("Date", new Date(System.currentTimeMillis() - 100000));
        list.add(map);

        map = new HashMap<>();
        map.put("No.", 3);
        map.put("Name", "王五");
        map.put("Date", new Date(System.currentTimeMillis() + 200000));
        list.add(map);

        return list;
    }

}
