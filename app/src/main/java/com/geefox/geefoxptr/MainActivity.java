package com.geefox.geefoxptr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.geefox.geefoxptrlib.GeeFoxPullToRefreshFrame;

public class MainActivity extends AppCompatActivity implements GeeFoxPullToRefreshFrame.OnRefreshListener, GeeFoxPullToRefreshFrame.OnLoadMoreListener {
    private GeeFoxPullToRefreshFrame refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refresh = this.findViewById(R.id.refresh);
        refresh.setOnRefreshListener(this);
        refresh.setOnLoadMoreListener(this);
//        refresh.setRefreshBackgroundResource(R.color.colorAccent);
//        refresh.setLoadMoreBackgroundResource(R.color.colorPrimary);
//        RotateRefreshView footerView = new RotateRefreshView(this);
//        footerView.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        footerView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        refresh.setFooterView(footerView);
    }

    @Override
    public void onRefresh() {

        refresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh.refreshComplete();
            }
        }, 3000);

    }

    @Override
    public void onLoadMore() {
        refresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh.loadMoreComplete();
            }
        }, 3000);
    }
}
