package com.xjh1994.read.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xjh1994.read.R;
import com.xjh1994.read.base.BaseActivity;
import com.xjh1994.read.util.FileUtil;
import com.xjh1994.read.util.SharedPreferencesUtil;
import com.xjh1994.read.util.TextJustification;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static final String KEY_WORD_LEVEL = "key_word_level";

    public static final String LESSON = "lesson";

    private String lesson;
    private long pointer;
    private RandomAccessFile file;

    private String result;

    private ProgressBar progressBar;
    private StringBuilder contentBuilder;

    private Map<String, Integer> wordMap;

    private TextView tv_content;

    private int width;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_article);
    }

    @Override
    public void initViews() {
        setBackTitle();

        tv_content = (TextView) findViewById(R.id.tv_content);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        width = dm.widthPixels;
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {
        lesson = getIntent().getStringExtra(LESSON);
        pointer = getIntent().getLongExtra("pointer", 0);
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
                    if (pointer != 0)
                        file.seek(pointer);
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
                        int start = s.indexOf("回答以下问题。");
                        int end = s.indexOf("New words");
                        result = s.substring(start, end);
                        result = result.replace("回答以下问题。\n", "");

                        justifiedResult = TextJustification.getJustifiedString(tv_content, tv_content.getWidth(), result);

                        /** 给每个单词添加点击事件 */
                        ssb = new SpannableStringBuilder(justifiedResult);
                        Pattern pattern = Pattern.compile("[a-zA-Z]+");
                        Matcher matcher = pattern.matcher(justifiedResult);
                        while (matcher.find()) {
                            String group = matcher.group();
                            ClickableSpan cs = new MyClickableSpan(group, matcher.start(), matcher.end());
                            ssb.setSpan(cs, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        tv_content.setText(ssb);
                        tv_content.setMovementMethod(LinkMovementMethod.getInstance());

                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private String justifiedResult;
    private SpannableStringBuilder ssb;

    class MyClickableSpan extends ClickableSpan {
        private String word;
        private int start;
        private int end;

        public MyClickableSpan(String word, int start, int end) {
            this.word = word;
            this.start = start;
            this.end = end;
        }

        @Override
        public void onClick(View widget) {
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primaryTextColor)), 0, justifiedResult.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_content.setText(ssb);
        }

        @Override
        public void updateDrawState(TextPaint ds) {

        }
    }

    private CharSequence[] levels = {"0", "1", "2", "3", "4", "5"};

    /**
     * 设置单词显示级别
     */
    private void setLevel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_select_word_level)
                .setItems(levels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferencesUtil.saveData(ArticleActivity.this, KEY_WORD_LEVEL, which);
                        displayWords();
                    }
                })
                .show();
    }

    /**
     * 高亮文章在单词列表中出现的单词
     */
    private void displayWords() {
        if (TextUtils.isEmpty(result)) return;

        if (wordMap == null)
            wordMap = FileUtil.txt2Map(this, WORD_FILE);

        int level = (int) SharedPreferencesUtil.getData(this, KEY_WORD_LEVEL, 0);

        ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primaryTextColor)), 0, justifiedResult.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            String word = entry.getKey() + " ";
            if (justifiedResult.contains(word))
                if (entry.getValue() <= level) {
                    int start = justifiedResult.indexOf(word);
                    if (start < 0) continue;
                    int end = start + entry.getKey().length();
                    ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
        }
        tv_content.setText(ssb);
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
            case android.R.id.home:
                finish();
                break;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (file != null)
                file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
