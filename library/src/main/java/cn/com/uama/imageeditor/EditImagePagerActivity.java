package cn.com.uama.imageeditor;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwei on 2017/3/18 11:07
 * Email: liwei@uama.com.cn
 * Description: 图片预览 Pager 界面，有编辑按钮和删除按钮
 */

public class EditImagePagerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_EDIT_IMAGE = 1;

    public static final String IMAGE_PATH_LIST = "image_path_list";
    public static final String INDEX = "index";

    private ArrayList<EditImageInfo> imageInfoList;
    private int currentIndex;

    ViewPager viewPager;
    ImagePagerAdapter adapter;
    boolean hasChanged = false; // 是否有过删除或编辑

    public static void startForResult(Activity activity, List<String> pathList, int index, int requestCode) {
        Intent intent = new Intent(activity, EditImagePagerActivity.class);
        intent.putStringArrayListExtra(EditImagePagerActivity.IMAGE_PATH_LIST, new ArrayList<>(pathList));
        intent.putExtra(EditImagePagerActivity.INDEX, index);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startForResult(Fragment fragment, List<String> pathList, int index, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), EditImagePagerActivity.class);
        intent.putStringArrayListExtra(EditImagePagerActivity.IMAGE_PATH_LIST, new ArrayList<>(pathList));
        intent.putExtra(EditImagePagerActivity.INDEX, index);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 获取编辑过的图片路径列表
     * @param data onActivityResult 中的 data
     */
    public static List<String> getEditedImagePathList(Intent data) {
        if (data != null) {
            return data.getStringArrayListExtra(EditImagePagerActivity.IMAGE_PATH_LIST);
        }
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lm_image_editor_activity_edit_image_pager);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        // 获取传递过来的图片路径列表
        List<String> imagePathList = getIntent().getStringArrayListExtra(IMAGE_PATH_LIST);
        // 如果传递过来的图片路径列表为空，直接返回
        if (imagePathList == null || imagePathList.size() == 0) {
            finish();
            return;
        }
        // 转换为图片信息实体类列表
        imageInfoList = new ArrayList<>();
        for (String path : imagePathList) {
            imageInfoList.add(new EditImageInfo(path));
        }
        currentIndex = getIntent().getIntExtra(INDEX, 0);
        // 如果传递过来的当前 index 不在合理范围内，将其设置为 0
        if (currentIndex < 0 || currentIndex >= imageInfoList.size()) {
            currentIndex = 0;
        }
        adapter = new ImagePagerAdapter(imageInfoList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_edit).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_back) {
            finish();
        } else if (id == R.id.button_edit) {
            edit();
        } else if (id == R.id.button_delete) {
            delete();
        }
    }

    /**
     * 编辑当前图片
     */
    protected void edit() {
        EditImageActivity.startForResult(this, imageInfoList.get(currentIndex), REQUEST_EDIT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_IMAGE && resultCode == RESULT_OK) {
            EditImageInfo editedImageInfo = EditImageActivity.getEditedImageInfo(data);
            if (editedImageInfo != null) {
                // 如果原图路径和已编辑过的路径不一样，说明没有编辑过
                if (!editedImageInfo.getOrgPath().equals(editedImageInfo.getPath())) {
                    hasChanged = true;
                }
                imageInfoList.remove(currentIndex);
                imageInfoList.add(currentIndex, editedImageInfo);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 弹出删除图片提示框
     */
    private void delete() {
        // 先提示一下
        new AlertDialog.Builder(this)
                .setMessage("是否确认删除？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doDelete();
                    }
                })
                .show();
    }

    /**
     * 删除当前图片
     */
    private void doDelete() {
        hasChanged = true;
        // 移除图片路径
        imageInfoList.remove(currentIndex);
        // 如果图片删光了，返回
        if (imageInfoList.size() == 0) {
            finish();
        } else {
            // 刷新 adapter
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void finish() {
        // 图片没有改变过（编辑或删除）就没必要带回了
        if (hasChanged) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(IMAGE_PATH_LIST, getImagePathList());
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    /**
     * 获取图片路径的列表
     */
    private ArrayList<String> getImagePathList() {
        ArrayList<String> imagePathList = new ArrayList<>();
        for (EditImageInfo imageInfo : imageInfoList) {
            imagePathList.add(imageInfo.getPath());
        }
        return imagePathList;
    }

    /**
     * 简单的展示图片的 adapter
     */
    private class ImagePagerAdapter extends PagerAdapter {

        private final List<EditImageInfo> imageInfos;

        ImagePagerAdapter(List<EditImageInfo> imageInfos) {
            this.imageInfos = imageInfos;
        }

        @Override
        public int getCount() {
            return imageInfos.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            String path = imageInfos.get(position).getPath();
            Glide.with(container.getContext())
                    .load(path)
                    .thumbnail(0.1f)
                    .into(photoView);
            container.addView(photoView);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
