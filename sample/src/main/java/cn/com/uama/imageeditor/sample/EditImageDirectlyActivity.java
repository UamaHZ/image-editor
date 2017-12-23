package cn.com.uama.imageeditor.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import cn.com.uama.imageeditor.EditImageActivity;
import cn.com.uama.imageeditor.EditImageInfo;

/**
 * Created by liwei on 2017/12/23 15:06
 * Email: liwei@uama.com.cn
 * Description: 直接编辑图片示例（不进入 pager 页面）
 */
public class EditImageDirectlyActivity extends AppCompatActivity {

    ImageView imageView;
    String currentPath;
    EditImageInfo currentImageInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image_directly);

        currentPath = "http://b.hiphotos.baidu.com/image/pic/item/902397dda144ad346863814dd9a20cf430ad851f.jpg";
        imageView = findViewById(R.id.imageView);
        showCurrent();
    }

    public void edit(View view) {
        /*
        直接进入编辑界面有两种方式
        1. 将图片路径传入编辑界面
        2. 将图片信息对象（包含之前的编辑信息）传入编辑界面，
        这个对象可以在一次编辑返回之后通过 EditImageActivity.getEditedImageInfo(data) 方法拿到
         */

        if (currentImageInfo != null) {
            // 通过这种方式进入编辑界面，可以对之前的编辑进行撤销
            EditImageActivity.startForResult(this, currentImageInfo, 100);
        } else {
            EditImageActivity.startForResult(this, currentPath, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // 获取编辑过的图片路径
            String editedImagePath = EditImageActivity.getEditedImagePath(data);
            if (!TextUtils.isEmpty(editedImagePath)) {
                currentPath = editedImagePath;
                showCurrent();
            }

            // 获取编辑过的图片信息对象，后面可以把该对象传入编辑界面，将之前的编辑操作进行撤销
            currentImageInfo = EditImageActivity.getEditedImageInfo(data);
        }
    }

    private void showCurrent() {
        Glide.with(this)
                .load(currentPath)
                .into(imageView);
    }
}
