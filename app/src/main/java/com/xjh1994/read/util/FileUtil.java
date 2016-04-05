package com.xjh1994.read.util;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xjh1994 on 2016/4/4.
 */
public class FileUtil {

    /**
     * 一次读取的文章数目
     */
    public static final int AMOUNT_ONCE = 10;

    /**
     * 将文件保存到/data/data目录
     *
     * @param context
     * @param fileName
     * @throws IOException
     */
    public static void save(Context context, String fileName) {

        String path = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/" + fileName;   //data/data目录

        File file = new File(path);
        if (file.exists())
            return;
        InputStream in = null;  //从assets目录下复制
        try {
            in = context.getAssets().open(fileName);

            FileOutputStream out = new FileOutputStream(file);
            int length = -1;
            byte[] buf = new byte[1024];
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
            out.flush();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取txt并转换为Map键值对
     *
     * @param context
     * @param fileName
     * @return
     */
    public static Map<String, Integer> txt2Map(Context context, String fileName) {
        Map<String, Integer> wordMap = new HashMap<>();
        String tempString = null;
        int tempInt = 0;

        String path = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/" + fileName;   //data/data目录

        Pattern p_char = Pattern.compile("[a-zA-Z]+");
        Pattern p_number = Pattern.compile("\\d+");

        try {
            RandomAccessFile file = new RandomAccessFile(path, "rw");
            String line;
            while ((line = file.readLine()) != null) {
                Matcher matcher = p_char.matcher(line);
                while (matcher.find()) {
                    tempString = "";
                    tempString += (" " + matcher.group());
                }
                matcher = p_number.matcher(line);
                while (matcher.find()) {
                    tempInt = Integer.parseInt(matcher.group());
                }
                wordMap.put(tempString, tempInt);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wordMap;
    }

}
