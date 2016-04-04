package com.xjh1994.read.util;

import android.content.Context;

import com.xjh1994.read.bean.Article;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public int lastFlag = 0;   //上一次读取的行号
    public long filePointer = 0L;

    public List<Article> getArticleTitle(Context context, String fileName) {
        int titleFlag = 0;  //每次读取10篇文章
        List<Article> articles = new ArrayList<>();

        String path = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/" + fileName;   //data/data目录

        Article article;
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");
            file.seek(filePointer);
            String line;
            while ((line = file.readLine()) != null) {
                filePointer = file.getFilePointer();
                line = new String(line.getBytes("ISO-8859-1"), "UTF-8").trim();
                lastFlag++;
                if (line.contains("Lesson ")) {
                    titleFlag++;
                    if (titleFlag > AMOUNT_ONCE) {
                        break;
                    }

                    article = new Article();
                    article.setPointer(file.getFilePointer());
                    article.setLesson(line);
                    article.setTitle(file.readLine().trim());
                    article.setTitleZh(new String(file.readLine().getBytes("ISO-8859-1"), "UTF-8").trim());
                    articles.add(article);
                }
                filePointer = file.getFilePointer();
            }

            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return articles;

    }

    public int lastInfoFlag = 0;   //上一次读取的行号
    public long fileInfoPointer = 0L;

    public List<Article> getArticleInfo(Context context, String fileName) {
        int titleFlag = 0;  //每次读取10篇文章
        List<Article> articles = new ArrayList<>();

        String path = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/" + fileName;   //data/data目录

        Article article;
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");
            file.seek(fileInfoPointer);
            String line;
            while ((line = new String(file.readLine().getBytes("ISO-8859-1"), "UTF-8")) != null) {
                line = line.trim();
                lastInfoFlag++;
                if (line.contains("Lesson ")) {
                    titleFlag++;
                    if (titleFlag > AMOUNT_ONCE) {
                        break;
                    }

                    article = new Article();
                    article.setLesson(line);
                    article.setTitle(file.readLine().trim());
                    article.setTitleZh(new String(file.readLine().getBytes("ISO-8859-1"), "UTF-8").trim());
                    String content;
                    String listen = "";
                    while ((content = new String(file.readLine().getBytes("ISO-8859-1"), "UTF-8")) != null) {
                        if (content.contains("Lesson ") || content.contains("New words")) {
                            break;
                        }
                        listen += content.trim();
                    }
                    article.setListen(listen);
                    articles.add(article);
                }
            }
            fileInfoPointer = file.getFilePointer();

            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return articles;

    }
}
