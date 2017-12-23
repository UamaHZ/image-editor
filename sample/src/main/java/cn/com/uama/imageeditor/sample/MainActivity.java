package cn.com.uama.imageeditor.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.com.uama.imageeditor.EditImagePagerActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PAGER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void pager(View view) {
        List<String> imagePathList = Arrays.asList("http://b.hiphotos.baidu.com/image/pic/item/902397dda144ad346863814dd9a20cf430ad851f.jpg",
                "http://g.hiphotos.baidu.com/image/pic/item/962bd40735fae6cddddeaaab06b30f2443a70f95.jpg",
                "http://c.hiphotos.baidu.com/image/pic/item/0b46f21fbe096b63fc80589a05338744eaf8ac5a.jpg");
        // 进入预览界面
        EditImagePagerActivity.startForResult(this, imagePathList, 0, REQUEST_CODE_PAGER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAGER && resultCode == RESULT_OK) {
            List<String> editedImagePathList = EditImagePagerActivity.getEditedImagePathList(data);
            if (editedImagePathList != null) {
                // 拿到编辑过后的图片路径列表，执行业务逻辑
            }
        }
    }

    public void editDirectly(View view) {
        startActivity(new Intent(this, EditImageDirectlyActivity.class));
    }
}
