package cn.com.uama.imageeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by liwei on 2017/3/18 11:04
 * Email: liwei@uama.com.cn
 * Description: 编辑图片（画圈、文字）界面
 */

public class EditImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ADD_TEXT = 1;
    private static final int REQUEST_EDIT_TEXT = 2;

    private static final int PERMISSION_REQUEST_STORAGE = 100;

    private static final String IMAGE_INFO_TO_EDIT = "image_info_to_edit";
    private static final String IMAGE_INFO_EDITED = "image_info_edited";
    private static final String IMAGE_PATH_TO_EDIT = "image_path_to_edit";
    private static final String START_FLAG = "start_flag";

    private EditImageView editImageView;
    private TextView circleButton;
    private EditImageInfo toEditImageInfo;

    /**
     * 进入编辑界面
     *
     * @param activity    activity 对象
     * @param imagePath   要编辑的图片路径
     * @param requestCode request code
     */
    public static void startForResult(Activity activity, String imagePath, int requestCode) {
        Intent intent = new Intent(activity, EditImageActivity.class);
        intent.putExtra(IMAGE_PATH_TO_EDIT, imagePath);
        intent.putExtra(START_FLAG, true);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.lm_image_editor_scale_fade_in, 0);
    }

    /**
     * 进入编辑界面
     *
     * @param fragment    fragment 对象
     * @param imagePath   要编辑的图片路径
     * @param requestCode request code
     */
    public static void startForResult(Fragment fragment, String imagePath, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), EditImageActivity.class);
        intent.putExtra(IMAGE_PATH_TO_EDIT, imagePath);
        intent.putExtra(START_FLAG, true);
        fragment.startActivityForResult(intent, requestCode);
        if (fragment.getActivity() != null) {
            fragment.getActivity().overridePendingTransition(R.anim.lm_image_editor_scale_fade_in, 0);
        }
    }

    /**
     * 进入编辑界面
     *
     * @param activity    activity 对象
     * @param imageInfo   要编辑的图片信息对象
     * @param requestCode request code
     */
    public static void startForResult(Activity activity, EditImageInfo imageInfo, int requestCode) {
        Intent intent = new Intent(activity, EditImageActivity.class);
        intent.putExtra(IMAGE_INFO_TO_EDIT, imageInfo);
        intent.putExtra(START_FLAG, true);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.lm_image_editor_scale_fade_in, 0);
    }

    /**
     * 进入编辑界面
     *
     * @param fragment    fragment 对象
     * @param imageInfo   要编辑的图片信息对象
     * @param requestCode request code
     */
    public static void startForResult(Fragment fragment, EditImageInfo imageInfo, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), EditImageActivity.class);
        intent.putExtra(IMAGE_INFO_TO_EDIT, imageInfo);
        intent.putExtra(START_FLAG, true);
        fragment.startActivityForResult(intent, requestCode);
        if (fragment.getActivity() != null) {
            fragment.getActivity().overridePendingTransition(R.anim.lm_image_editor_scale_fade_in, 0);
        }
    }

    /**
     * 获取编辑过的图片信息
     *
     * @param data onActivityResult 中的 data
     */
    public static EditImageInfo getEditedImageInfo(Intent data) {
        if (data != null) {
            return data.getParcelableExtra(IMAGE_INFO_EDITED);
        }
        return null;
    }

    /**
     * 获取直接编辑过的图片路径
     *
     * @param data onActivityResult 中的 data
     */
    public static String getEditedImagePath(Intent data) {
        EditImageInfo editedImageInfo = getEditedImageInfo(data);
        if (editedImageInfo != null) {
            return editedImageInfo.getPath();
        }
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lm_image_editor_activity_edit_image);

        if (!getIntent().getBooleanExtra(START_FLAG, false)) {
            throw new IllegalStateException("请使用 EditImageActivity.startForResult 方法来进入编辑界面！");
        }

        editImageView = (EditImageView) findViewById(R.id.edit_image_view);
        // 获取传递过来的要编辑的图片路径
        String toEditImagePath = getIntent().getStringExtra(IMAGE_PATH_TO_EDIT);
        if (!TextUtils.isEmpty(toEditImagePath)) {
            toEditImageInfo = new EditImageInfo(toEditImagePath);
        } else {
            // 获取传递过来的要编辑的图片相关信息
            toEditImageInfo = getIntent().getParcelableExtra(IMAGE_INFO_TO_EDIT);
            // 如果没有要编辑的图片信息，直接返回
            if (toEditImageInfo == null) {
                finish();
                return;
            }

            // 设置之前编辑的文字（对已编辑过的图片来说）
            editImageView.setTextInfos(toEditImageInfo.getTextInfos());
            // 设置之前编辑的涂鸦（对已编辑过的图片来说）
            editImageView.setPathInfos(toEditImageInfo.getPathInfos());
        }

        // 设置图片上文字的点击事件（及跳转到编辑文字界面）
        editImageView.setTextClickListener(new EditImageView.OnTextClickListener() {
            @Override
            public void onClick(TextView textView) {
                editText(textView);
            }
        });
        // 获取图片的 bitmap 并设置
        Glide.with(this)
                .load(toEditImageInfo.getOrgPath())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        editImageView.setBitmap(resource);
                    }
                });

        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_done).setOnClickListener(this);
        findViewById(R.id.button_revoke).setOnClickListener(this);
        findViewById(R.id.button_text).setOnClickListener(this);

        circleButton = (TextView) findViewById(R.id.button_circle);
        circleButton.setSelected(editImageView.isDrawCircleEnabled());
        circleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_cancel) {
            finish();
        } else if (id == R.id.button_done) {
            // 先检查权限
            AndPermission.with(this)
                    .requestCode(PERMISSION_REQUEST_STORAGE)
                    .permission(Permission.STORAGE)
                    .callback(this)
                    .start();
        } else if (id == R.id.button_revoke) {
            revoke();
        } else if (id == R.id.button_text) {
            addText();
        } else if (id == R.id.button_circle) {
            toggleCircle();
        }
    }

    @PermissionYes(PERMISSION_REQUEST_STORAGE)
    private void onStoragePermissionYes(List<String> grantPermissions) {
        if (AndPermission.hasPermission(this, grantPermissions)) {
            done();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("没有存储权限")
                    .setMessage("请到系统设置中进行授权")
                    .setPositiveButton("知道了", null)
                    .show();
        }
    }

    @PermissionNo(PERMISSION_REQUEST_STORAGE)
    private void onStoragePermissionNo(List<String> deniedPermissions) {
        if (AndPermission.hasPermission(this, deniedPermissions)) {
            done();
        } else if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
            AndPermission.defaultSettingDialog(this).show();
        }
    }

    /**
     * 画圈功能开关
     */
    private void toggleCircle() {
        boolean selected = circleButton.isSelected();
        circleButton.setSelected(!selected);
        editImageView.setDrawCircleEnabled(circleButton.isSelected());
    }

    /**
     * 完成
     * 保存正在编辑的图片，并将保存好的图片路径带回
     */
    private void done() {
        Bitmap bitmap = editImageView.getBitmap();
        if (bitmap == null) {
            // 获取到的 bitmap 为 null 表示没有编辑图片或者之前编辑的信息已全部撤销或删除
            toEditImageInfo.setTextInfos(null);
            toEditImageInfo.setPathInfos(null);
            toEditImageInfo.setPath(toEditImageInfo.getOrgPath());
            Intent intent = new Intent();
            intent.putExtra(IMAGE_INFO_EDITED, toEditImageInfo);
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "当前外部存储不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        File dcimFile = new File(Environment.getExternalStorageDirectory(), "DCIM");
        File scrawlDir = new File(dcimFile, "scrawls");
        boolean hasScrawlDir = scrawlDir.exists() || scrawlDir.mkdirs();
        if (!hasScrawlDir) {
            Toast.makeText(this, "创建文件目录失败", Toast.LENGTH_SHORT).show();
            return;
        }
        // 保存的路径
        File file = new File(scrawlDir, System.currentTimeMillis() + ".jpg");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);

            // 发送广播使刚保存的图片出现在相册中
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);

            // 图片保存成功将相关信息带回到预览界面
            Intent intent = new Intent();
            String editedImagePath = file.getAbsolutePath();
            toEditImageInfo.setTextInfos(editImageView.getTextInfos());
            toEditImageInfo.setPathInfos(editImageView.getPathInfos());
            toEditImageInfo.setPath(editedImagePath);
            intent.putExtra(IMAGE_INFO_EDITED, toEditImageInfo);
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 撤销上一步操作
     */
    private void revoke() {
        editImageView.undo();
    }

    /**
     * 新增文字
     */
    private void addText() {
        Intent intent = new Intent(this, EditTextActivity.class);
        startActivityForResult(intent, REQUEST_ADD_TEXT);
        overridePendingTransition(R.anim.lm_image_editor_edit_text_from_bottom_up, 0);
    }

    // 正在编辑的 TextView
    private TextView inEditTextView;

    /**
     * 编辑文字
     */
    private void editText(TextView textView) {
        inEditTextView = textView;
        Intent intent = new Intent(this, EditTextActivity.class);
        intent.putExtra("text", textView.getText());
        startActivityForResult(intent, REQUEST_EDIT_TEXT);
        overridePendingTransition(R.anim.lm_image_editor_edit_text_from_bottom_up, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_TEXT) { // 新增文字
                String text = data.getStringExtra("text");
                editImageView.addText(text);
            } else if (requestCode == REQUEST_EDIT_TEXT) { // 编辑文字
                String text = data.getStringExtra("text");
                // 如果文字不为空，设置给当前正在编辑的 TextView ，否则移除当前正在编辑的 TextView
                if (!TextUtils.isEmpty(text)) {
                    if (inEditTextView != null) {
                        inEditTextView.setText(text);
                    }
                } else {
                    if (inEditTextView != null) {
                        editImageView.removeView(inEditTextView);
                    }
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.lm_image_editor_scale_fade_out);
    }
}
