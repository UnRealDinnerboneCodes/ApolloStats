package com.unrealdinnerbone.specfeatures;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ChatReader
{
    private final static Logger LOGGER = LogUtils.getLogger();
    public static void main(String[] args) throws IOException {
        Path logs = Path.of("C:\\Users\\unrealdinner\\Desktop\\Logs");
        for (Path path : Files.list(logs).toList()) {
            if(path.getFileName().toString().endsWith(".log")) {
                LOGGER.info("Reading: " + path.getFileName());
                for (String readAllLine : Files.readAllLines(path, Charset.forName("windows-1250"))) {
                    
                }
            }


//            for each chatset
//            for (Map.Entry<String, Charset> stringCharsetEntry : Charset.availableCharsets().entrySet()) {
//                try {
//
//                    boolean value = Files.readAllLines(path, stringCharsetEntry.getValue())
//                            .stream()
//                            .anyMatch(s -> s.contains("»"));
//                    if(value) {
//                        LOGGER.info("Found charset: " + stringCharsetEntry.getKey());
//                    }else {
//                        LOGGER.warn("Not found charset: " + stringCharsetEntry.getKey());
//                    }
//                }catch (Exception e) {
//                }
//            }
//            return;
        }

    }
}
