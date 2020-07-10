package com.zh.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReadConfig {
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
                    sb.append(line.trim() + "\n");
            }
        } catch (IOException e) {
            return "error";
        }
        return sb.toString();
    }
}
