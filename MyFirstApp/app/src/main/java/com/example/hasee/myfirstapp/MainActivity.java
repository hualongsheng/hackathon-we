package com.example.hasee.myfirstapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothProfile;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.baidu.aip.ocr.AipOcr;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.example.hasee.myfirstapp.gui.OpenFileDialog;
import com.example.hasee.myfirstapp.gui.CallbackBundle;
import com.example.hasee.myfirstapp.httptools.HttpClientUtils;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {
    String imagePath = null;
    static private int openfileDialogId = 0;

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String API_KEY = "vDZO3GocQcB5BI9QYGhVIBuQ";
    public static final String SECRET_KEY = "ckir1R8epRXlj7uyzCGmVgIoevw1VMpE";
    private AlertDialog.Builder alertDialog;

//    private TextView tvCategory;
//    private TextView tvDate;
//    private TextView tvAmount;



    private void infoPopText(final String result) {
        alertText("", result);
    }
    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e("", "onCreate");

        setContentView(R.layout.activity_main);
        alertDialog = new AlertDialog.Builder(this);
//        imagePath = "/storage/emulated/0/DCIM/Camera/test.jpg";


        requestPermissions(permissions);

        // 设置单击按钮时打开文件对话框
        findViewById(R.id.button_openfile).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                image();
//                showDialog(openfileDialogId);
            }
        });


        // R.string.app_name

    }

    // 用户权限 申请 的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        takePhotoAct.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (int j = 0; j < grantResults.length; j++) {
                    if (grantResults[j] != PackageManager.PERMISSION_GRANTED) {
                        // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                        for (int i = 0; i < permissions.length; i++) {
                            boolean b = this.shouldShowRequestPermissionRationale(permissions[i]);
                            if (!b) {
                                Log.e("onRequestPerm", "onRePermissionsResult ：调到设置应用界面，让用户手动授权");
                                // 用户还是想用我的 APP 的，提示用户去应用设置界面手动开启权限
                                goToAppSetting();
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * @描述 4.4及以上系统使用这个方法处理图片 相册图片返回的不再是真实的Uri,而是分装过的Uri
     * */
    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        imagePath = null;
        Log.e("TAG1", "hello");
//        if (data == null){
//            Log.e("TAG", "hello5");
//        }
        Uri uri = data.getData();
        Log.e("TAG1", "hello2");
        Log.e("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if (Variable.android_providers_media_documents.equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if (Variable.android_providers_downloads_documents.equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if (Variable.content.equalsIgnoreCase(uri.getScheme())) {//equalsIgnoreCase 比较的两个字符串 可以不区分大小写
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if (Variable.file.equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        Log.i("imagePath : ",""+imagePath);
        return imagePath;
    }
    /**
     * @描述 查询图片的真实路径
     * */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = this.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 跳转到当前应用的设置界面
     */
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        this.startActivityForResult(intent, 321);
    }



    /**
     * @描述 先检查权限是否授权，如果授权直接调用，若果没有动态申请权限
     * */
    public void requestPermissions(String[] permissions){
        //申请相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            for (int x=0;x<permissions.length;x++){
                int i = ContextCompat.checkSelfPermission(this, permissions[x]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有授予该权限，就去提示用户请求
                    Log.i("requestPermissions","requestPermissions : 用户去申请权限");
                    ActivityCompat.requestPermissions(this, permissions, 123);
                }
            }
        }
    }

    private void initAccessTokenWithAkSk() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
            }
        }, getApplicationContext(), API_KEY, SECRET_KEY);
    }

    private void image() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        //根据版本号不同使用不同的Action
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        startActivityForResult(intent, 321);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 321 && data != null)
        {

            String path = handleImageOnKitKat(data);
            Log.e("PATH", path);

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("IMGPATH", path);
            intent.putExtras(bundle);
            intent.setClass(this, InfoActivity.class);
            startActivity(intent);



        }

    }










}
