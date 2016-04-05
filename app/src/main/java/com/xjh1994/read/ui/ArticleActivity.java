package com.xjh1994.read.ui;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.orhanobut.logger.Logger;
import com.xjh1994.read.R;
import com.xjh1994.read.base.BaseActivity;
import com.xjh1994.read.util.FileUtil;
import com.xjh1994.read.view.AlignTextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by xjh1994 on 2016/4/1.
 */
public class ArticleActivity extends BaseActivity {
    /**
     * 文章详细信息
     */

    public static final String LESSON = "lesson";

    private String lesson;
    private RandomAccessFile file;

    private AlignTextView tv_content;
    private ProgressBar progressBar;
    private StringBuilder contentBuilder;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_article);
    }

    @Override
    public void initViews() {
        setBackTitle();

        tv_content = (AlignTextView) findViewById(R.id.tv_content);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {
        lesson = getIntent().getStringExtra(LESSON);
        if (TextUtils.isEmpty(lesson))
            return;

        setTitle(lesson);

        String path = getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/" + ARTICLE_FILE;   //data/data目录
        try {
            file = new RandomAccessFile(path, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                contentBuilder = new StringBuilder();
                try {
                    String line;
                    while ((line = file.readLine()) != null) {
                        line = new String(line.getBytes("ISO-8859-1"), "UTF-8").trim();
                        if (line.contains(lesson)) {
                            String newLine;
                            while ((newLine = file.readLine()) != null) {
                                newLine = new String(newLine.getBytes("ISO-8859-1"), "UTF-8").trim();
                                if (newLine.contains("Lesson ")) {
                                    break;
                                }
                                contentBuilder.append(newLine).append("\n");
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(contentBuilder.toString());
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        tv_content.setText(s);
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_words:
                displayWords();
                break;
            case R.id.action_set_level:
                setLevel();
                break;
            default:
                break;
        }
        return true;
    }

    private void setLevel() {

    }

    /**
     * 高亮文章在单词列表中出现的单词
     */
    private void displayWords() {
        if (TextUtils.isEmpty(contentBuilder.toString())) return;

        SpannableStringBuilder spannableStringBuilder;

        Map<String, Integer> wordMap = FileUtil.txt2Map(this, WORD_FILE);
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            if (contentBuilder.toString().contains(entry.getKey())) {
                Logger.d(entry.getKey());
//                spannableStringBuilder = TextUtil.highlight(entry.getKey(), contentBuilder.toString());
//                tv_content.setText(spannableStringBuilder);
            }
        }
    }

    private void readWords() {
        Map<String, Integer> wordMap = FileUtil.txt2Map(this, WORD_FILE);
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
//            tv_words.append(entry.getKey() + "    " + entry.getValue() + "\n");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
