package com.jerry.travel.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.Charsets;

import com.google.common.io.Files;

public class FileCommonUtils {

    /**
     * 读取文件
     * @param filePath
     * @param decodeCharset
     * @return
     */
    public static List<String> fileRead(String filePath, Charset decodeCharset) {

        File file = new File(filePath);
        List<String> fileLines=null;
        try {
            fileLines = Files.readLines(file, decodeCharset == null ? decodeCharset : Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLines;

    }
    
    /**
     * 写入文件
     * @param fileName
     * @param fileContent
     */
    public static void fileWrite(String fileName , String fileContent){
        
        
        File file = new File(fileName);
        
        try {
            Files.write(fileContent.getBytes(), file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
    }

}
