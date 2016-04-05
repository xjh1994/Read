package com.xjh1994.read.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xjh1994.read.R;
import com.xjh1994.read.bean.Article;
import com.xjh1994.read.ui.ArticleActivity;

/**
 * Created by xjh1994 on 2016/4/1.
 */
public class ArticleItem implements IAdapterItem<Article> {

    private TextView tv_title;
    private LinearLayout container;

    @Override
    public int getLayoutResId() {
        return R.layout.item_article;
    }

    @Override
    public void bindViews(View root) {
        tv_title = (TextView) root.findViewById(R.id.tv_title);
        container = (LinearLayout) root.findViewById(R.id.container);
    }

    @Override
    public void setViews() {

    }

    @Override
    public void handleData(final Context context, final Article data, int type) {
        tv_title.setText(data.getLesson() + "  " + data.getTitle());
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ArticleActivity.class);
                intent.putExtra(ArticleActivity.LESSON, data.getLesson());
                intent.putExtra("pointer", data.getPointer());
                context.startActivity(intent);
            }
        });
    }
}
