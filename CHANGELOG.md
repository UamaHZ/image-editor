# Change Log

## Version 0.2 *(2018-11-13)*

* 升级 AGP 版本到 3.2.1，升级 gradle wrapper 版本 到 4.6
* 升级 compileSdkVersion 和 targetSdkVersion 到 28
* 去除编辑图片和编辑文字界面在清单文件中的 `android:screenOrientation="portrait"` 设置，因为 Android O 及之后主题中包含 `<item name="android:windowIsTranslucent">true</item>` 的同时设置 `android:screenOrientation="portrait"` 会崩溃
* 给所有资源文件加上前缀 lm_image_editor_
* 去除依赖第三方动态权限处理库 AndPermission，防止和项目中的依赖版本发生冲突

## Version 0.1 *(2017-12-23)*
Initial release.