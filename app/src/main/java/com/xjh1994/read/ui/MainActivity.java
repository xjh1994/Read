package com.xjh1994.read.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.xjh1994.read.R;
import com.xjh1994.read.adapter.ArticleItem;
import com.xjh1994.read.adapter.BaseUltimateAdapter;
import com.xjh1994.read.adapter.IAdapterItem;
import com.xjh1994.read.base.BaseActivity;
import com.xjh1994.read.bean.Article;
import com.xjh1994.read.util.FileUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 首页文章列表
 */
public class MainActivity extends BaseActivity {

    /**
     * 一次读取的文章数目
     */
    public static final int AMOUNT_ONCE = 10;

    private UltimateRecyclerView ultimateRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private BaseUltimateAdapter adapter;
    private List<Article> articleList = new ArrayList<>();

    private RandomAccessFile file;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initViews() {
        ultimateRecyclerView = (UltimateRecyclerView) findViewById(R.id.ultimate_recycler_view);
        ultimateRecyclerView.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager(this);
        ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new BaseUltimateAdapter(this, articleList) {
            @NonNull
            @Override
            public IAdapterItem createItem(Object type) {
                return new ArticleItem();
            }
        };
        ultimateRecyclerView.setAdapter(adapter);
        ultimateRecyclerView.setRefreshing(true);
        ultimateRecyclerView.showEmptyView();
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {
        String path = getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/" + ARTICLE_FILE;   //data/data目录
        try {
            file = new RandomAccessFile(path, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Observable.just(ARTICLE_FILE)
                .map(new Func1<String, String>() {  //第一次将文件保存到/data/data目录
                    @Override
                    public String call(String s) {
                        FileUtil.save(MainActivity.this, ARTICLE_FILE);
                        FileUtil.save(MainActivity.this, WORD_FILE);
                        return null;
                    }
                })
                .map(new Func1<String, List<Article>>() {
                    @Override
                    public List<Article> call(String s) {
                        return getArticleTitle();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<List<Article>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Article> articles) {
                        articleList.addAll(articles);
                        adapter.notifyDataSetChanged();
                        ultimateRecyclerView.hideEmptyView();
                        ultimateRecyclerView.setRefreshing(false);

                        onLoadMore();
                    }
                });
    }

    private void onLoadMore() {
        ultimateRecyclerView.enableLoadmore();
        ultimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                ultimateRecyclerView.setRefreshing(true);
                Observable.just("")
                        .map(new Func1<String, List<Article>>() {
                            @Override
                            public List<Article> call(String s) {
                                return getArticleTitle();
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<Article>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(List<Article> articles) {
                                if (articles.size() > 0) {
                                    articleList.addAll(articles);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    ultimateRecyclerView.disableLoadmore();
                                }
                                ultimateRecyclerView.setRefreshing(false);
                            }
                        });

            }
        });
    }

    /**
     * 获取文章标题
     * @return
     */
    public List<Article> getArticleTitle() {
        int titleFlag = 0;
        List<Article> articles = new ArrayList<>();

        Article article;
        try {
            String line;
            while ((line = file.readLine()) != null) {
                line = new String(line.getBytes("ISO-8859-1"), "UTF-8").trim();
                if (line.contains("Lesson ")) {
                    titleFlag++;
                    if (titleFlag > (AMOUNT_ONCE - 1)) {  //每次读取10篇文章
                        article = new Article();
                        article.setPointer(file.getFilePointer());
                        article.setLesson(line);
                        article.setTitle(file.readLine().trim());
                        article.setTitleZh(new String(file.readLine().getBytes("ISO-8859-1"), "UTF-8").trim());
                        articles.add(article);
                        break;
                    }

                    article = new Article();
                    article.setPointer(file.getFilePointer());
                    article.setLesson(line);
                    article.setTitle(file.readLine().trim());
                    article.setTitleZh(new String(file.readLine().getBytes("ISO-8859-1"), "UTF-8").trim());
                    articles.add(article);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return articles;

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

