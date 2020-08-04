package com.zh.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigUtil {
    public static String readWhiteList() throws Exception {
        StringBuilder sb = new StringBuilder();
        File directory = new File(".");
        String FilePath = directory.getCanonicalPath();
        String pathname = FilePath + "\\WhiteList.config";

        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                if (!line.startsWith("#") && line.length() > 0)
                    sb.append(line.trim()).append("\n");
            }
        } catch (IOException e) {
            return "error";
        }
        return sb.toString();
    }

    public static String writeWhiteList(){
        //todo
        return null;
    }
}
