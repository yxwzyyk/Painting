package com.yxwzyyk.painting.app;

import android.os.Environment;

/**
 * Created by yyk on 06/10/2016.
 */

public interface ConstUtils {
    //文件保存目录
    String EXTENSION = ".jpg";
    String TEMP_FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/Paint/temp/";
    String FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/Paint/sketchPhoto/";
    String TEMP_FILE_NAME = "temp" + EXTENSION;
}
