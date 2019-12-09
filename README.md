#### 免责声明
- 本项目仅进行技术展示,学习交流使用,不得用于商业用途,违者自负；软件资源皆是采集来的，不存储任何资源。
#### 效果展示
- 手机版首页
!(phone-home)[show/20191209092051.png]
- 搜索
!(phone-home)[show/20191209092249.png]
- 播放视频
!(phone-home)[show/20191209092422.png]
#### 其他说明
- app-release-2.apk  搜索功能图片优化，播放列表优化

- ERROR: Unsupported method: AndroidProject.getVariantNames(). 安装sdk 其他版本
- Failed to list versions for com.android.support:appcompat-v7. 安装sdk tools其他仓库
- Cause: connect timed out   可能是代理问题
- Execution failed for task ':library:compileDebugAidl'.  com.android.support:appcompat-v7:26.+' 版本不对应 改成  'com.android.support:appcompat-v7:' + rootProject.supportLibraryVersion