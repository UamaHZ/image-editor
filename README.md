# Image Editor
[![](https://jitpack.io/v/UamaHZ/image-editor.svg)](https://jitpack.io/#UamaHZ/image-editor)

图片编辑库，可以进入图片列表的预览界面再选择编辑或删除，或者直接进入单张图片的编辑。

## 添加依赖
首先需要在项目的 `build.gradle` 文件中配置 [https://jitpack.io](https://jitpack.io) 的 maven 库：
```
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```
其次在需要引用该的 module 的 `build.gradle` 中添加依赖：
```
compile 'com.github.UamaHZ:image-editor:{version}'
```
{version} 参见顶部的 badge 。

## 用法
## 进入图片列表预览界面
有两个方法可供调用
```
EditImagePagerActivity#startForResult(Activity activity, List<String> pathList, int index, int requestCode)
EditImagePagerActivity#startForResult(Fragment fragment, List<String> pathList, int index, int requestCode)
```

编辑完成之后，在对应 `Activity` 或者 `Fragment` 的 `onActivityResult(int requestCode, int resultCode, Intent data)` 方法中
调用 `EditImagePagerActivity.getEditedImagePathList(data)` 即可获取到编辑过后的图片路径列表，**注意要对拿到的列表对象进行判空，如果为 null 表示没有编辑** 。
```
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
```

## 直接进入单张图片的编辑界面
有四个方法可供调用

```
EditImageActivity.startForResult(Activity activity, String imagePath, int requestCode)

EditImageActivity.startForResult(Fragment fragment, String imagePath, int requestCode)

EditImageActivity.startForResult(Activity activity, EditImageInfo imageInfo, int requestCode)

EditImageActivity.startForResult(Fragment fragment, EditImageInfo imageInfo, int requestCode)
```

前两个方法是传入要编辑的图片路径，后两个方法是传入要编辑的图片信息对象。

编辑完成之后，在对应 `Activity` 或者 `Fragment` 的 `onActivityResult(int requestCode, int resultCode, Intent data)` 方法中
调用 `EditImageActivity.getEditedImagePath(Intent data)` 即可获取到编辑过后的图片路径，调用 `EditImageActivity.getEditedImageInfo(Intent data)`
 即可获取到编辑过后的图片信息对象，这个对象可以在下次编辑时传入编辑界面，从而可以撤销之前的编辑。**注意对拿到的图片路径或图片信息对象进行判空。**