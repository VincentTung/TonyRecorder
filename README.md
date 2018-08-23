## 为什么

![](https://github.com/VincentTung/tonyrecorder/raw/master/gif/cry.png)

Toney是个测试，太多的bug需要他来截图，其中有些bug是我写的，所以他来找我，然后给他写了这个工具，让他快乐的记录bug的一瞬间。

tonnyrecorder提供截屏和录屏功能，方便QA在测试的过程中使用（>= 5.0）。

## 集成 [![Release](https://jitpack.io/v/VincentTung/tonyrecorder.svg)]()

1. 在工程的build.gradle中加入Jitpack仓库：
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. 在app的build.gradle中添加如下代码：
```
dependencies {
	        debugImplementation 'com.github.VincentTung:tonyrecorder:x.y.z'{
                		exclude group: 'com.android.support'
		}
}
```

## 如何使用

 通过晃动手机,调出悬浮操作面板，所以需要「悬浮窗」权限，如果没有开启自动打开设置提示进行开启。
 
 生成图片位置 /Android/data/packagename/cache/picture/
 
 生成mp4位置  /Android/data/packagename/cache/video/
 
  ![](https://github.com/VincentTung/tonyrecorder/raw/master/gif/show.gif)
 
## 致谢
 
 展现和使用方式来自  [pandora](https://github.com/whataa/pandora)；
 
 录屏的功能使用了
 [ScreenRecorder](https://github.com/yrom/ScreenRecorder)；
 
