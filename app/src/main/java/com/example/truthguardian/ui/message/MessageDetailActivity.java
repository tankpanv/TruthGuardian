package com.example.truthguardian.ui.message;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.graphics.PorterDuff;
import com.example.truthguardian.R;

public class MessageDetailActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_TIME = "extra_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);  // 显示返回按钮
            actionBar.setTitle("消息详情");  // 设置标题
            
            // 设置Toolbar上的所有图标为黑色（包括返回按钮）
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.getNavigationIcon().setColorFilter(
                ContextCompat.getColor(this, android.R.color.black), 
                PorterDuff.Mode.SRC_ATOP
            );
        }

        // 获取传递的数据
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        String time = getIntent().getStringExtra(EXTRA_TIME);

        // 设置UI
        TextView titleView = findViewById(R.id.message_detail_title);
        TextView contentView = findViewById(R.id.message_detail_content);
        TextView timeView = findViewById(R.id.message_detail_time);

        titleView.setText(title);
        contentView.setText(content);
        timeView.setText(time);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();  // 处理返回按钮点击事件
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 