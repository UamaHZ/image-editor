package cn.com.uama.imageeditor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by liwei on 2018/11/13 15:09
 * Email: liwei@uama.com.cn
 * Description: 运行时权限工具类，方法均为从 AndPermission copy 过来
 */
final class PermissionUtils {
    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    static boolean hasPermission(@NonNull Context context, @NonNull List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(context, permission);
            if (result == PackageManager.PERMISSION_DENIED) return false;

            String op = AppOpsManagerCompat.permissionToOp(permission);
            if (TextUtils.isEmpty(op)) continue;
            result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
            if (result != AppOpsManagerCompat.MODE_ALLOWED) return false;

        }
        return true;
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param activity          {@link Activity}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    static boolean hasAlwaysDeniedPermission(@NonNull Activity activity, @NonNull List<String> deniedPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;

        if (deniedPermissions.size() == 0) return false;

        for (String permission : deniedPermissions) {
            boolean rationale = activity.shouldShowRequestPermissionRationale(permission);
            if (!rationale) return true;
        }
        return false;
    }

    /**
     * 显示跳往权限设置界面对话框
     */
    static void showPermissionSettingDialog(final Activity context) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(R.string.lm_image_editor_permission_title_permission_failed)
                .setMessage(R.string.lm_image_editor_permission_message_permission_failed)
                .setPositiveButton(R.string.lm_image_editor_permission_setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.lm_image_editor_permission_cancel, null)
                .show();
    }
}
