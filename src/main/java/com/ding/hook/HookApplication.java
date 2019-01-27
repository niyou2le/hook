package com.ding.hook;

import com.ding.hook.bean.User;
import org.apache.commons.collections.MapUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class HookApplication {

    final static Map<String, User> cacheData = new HashMap<>();
    static final String filePath = System.getProperty("user.dir") + File.separator + "save_point.binary";

    public static void main(String[] args) throws IOException {
        System.out.printf("filePath=%s", filePath);

        SpringApplication.run(HookApplication.class, args);

        cacheData.put("test1", new User(1L, "testName1"));
        cacheData.put("test2", new User(2L, "testName2"));
        cacheData.put("test3", new User(3L, "testName3"));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ObjectOutputStream oos = null;
                try {
                    if (MapUtils.isNotEmpty(cacheData)) {
                        File cacheFile = new File(filePath);
                        if (!cacheFile.exists()) {
                            cacheFile.createNewFile();
                        }
                        oos = new ObjectOutputStream(new FileOutputStream(filePath));
                        oos.writeObject(cacheData);
                        oos.flush();
                    }
                } catch (IOException ex) {

                } finally {
                    try {
                        if (oos != null) {
                            oos.close();
                        }
                    } catch (IOException ex) {

                    }
                }
            }
        });
    }

    @PostConstruct
    public void recoverSavePoint() throws Exception {
        ObjectInputStream ois = null;
        try {
            File cacheFile = new File(filePath);
            if (cacheFile.exists()) {
                ois = new ObjectInputStream(new FileInputStream(filePath));
                Map<String, User> cacheMap = (Map<String, User>) ois.readObject();
                for (Map.Entry<String, User> entry : cacheMap.entrySet()) {
                    cacheData.put(entry.getKey(), entry.getValue());
                }
                cacheFile.delete();
            }
        } catch (IOException e) {

        } finally {
            if (ois != null) {
                ois.close();
            }
        }

        System.out.println(cacheData.toString());
    }
}

