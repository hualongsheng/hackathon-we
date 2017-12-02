package com.example.hasee.myfirstapp;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private List<String> data_list;
    private ArrayAdapter<String> arr_adapter;
    private AlertDialog.Builder alertDialog;
    private ImageView imageView;
    private Button btn;
    ProgressDialog dialog;

    String imagePath = null;
    private Spinner spinner;
    private EditText etDate;
    private EditText etAmount;
    public static final String API_KEY = "hF9iyM34C7d7b4u0vSVOicaL";
    public static final String SECRET_KEY = "j2SvOa4oDA8E8GkyP7MSluhwiQdgXx7s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_activity);

        data_list = new ArrayList<>();

        data_list.add("出差交通费");
        data_list.add("差旅杂费");
        data_list.add("出差住宿费");
        data_list.add("差旅补助补贴");

        imageView = (ImageView) findViewById(R.id.imageView);

        spinner = (Spinner) findViewById(R.id.spinner);

        //适配器
        arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);


        //        etCategory = (EditText) findViewById(R.id.editText);
        etDate = (EditText) findViewById(R.id.editText2);
        etAmount = (EditText) findViewById(R.id.editText3);
        Bundle bundle = this.getIntent().getExtras();
        imagePath =bundle.getString("IMGPATH");
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bm);
        Log.e("TEST", imagePath);

//                imageView.setImageBitmap(bm);
        try
        {
            initAccessTokenWithAkSk();
        } catch (Exception e)
        {
            Log.e("", "Auth failed!");
        }

        dialog = ProgressDialog.show(this, "","识别中，请稍等 …", true, true);
        Thread t = new Thread(new Runnable() {
                   @Override
            public void run() {
                try {
                    RecognizeService.recReceipt(imagePath,
                            new RecognizeService.ServiceListener() {
                                @Override
                                public void onResult(String result) {

                                    Log.e("recReceipt", result);

//                        infoPopText(result);

                                    // 解析 result ==> category, date,  amount
//                                etCategory.setText("出差交通费");
                                    etDate.setText("2017-12-01");
                                    etAmount.setText("100.0");
//
                                    setMoney(result);
                                    setType(result);
                                    Log.e("RES", result);
                                    dialog.dismiss();
                                }

                            });
                 } catch (Exception e) {
                   e.printStackTrace();
                 }}
        });
        t.start();


        btn = (Button) findViewById(R.id.summit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                infoPopText("提交成功");
//                Intent intent = new Intent();
//                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
////                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
////                intent.setData(uri);
//                startActivityForResult(intent, 321);
                AlertDialog.Builder dialog=new AlertDialog.Builder(InfoActivity.this);
                dialog.setTitle("提交").setIcon(android.R.drawable.ic_dialog_info).setMessage("是否提交？").setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();//取消弹出框
                    }
                }).create().show();
            }


        });

    }
    public void setMoney(String jsonResult) {
        try{

            JSONObject dataJson = new JSONObject(jsonResult);
            JSONArray words_result = dataJson.getJSONArray("words_result");
            for (int i = 0; i < words_result.length(); i++) {
                JSONObject result = words_result.getJSONObject(i);
                String words = result.getString("words");
                if (words.equals("金额") ){
                    if (i + 1 < words_result.length()) {
                        String money = "";
                        result = words_result.getJSONObject(i+1);
                        String tmpmoney = result.getString("words");
                        for (int j = 0; j < tmpmoney.length(); j++) {
                            if ((tmpmoney.charAt(j) >= '0' && tmpmoney.charAt(j) <= '9') ) {
                                money += tmpmoney.charAt(j);
                            }
                            else if(tmpmoney.charAt(j) == '.'|| tmpmoney.charAt(j) == '·'){
                                money += '.';
                            }
                        }
                        if(money.indexOf('.')==money.length()-2){
                            money+='0';
                        }
                        Log.e("",money);
                        etAmount.setText(money);
                    }
                } else if (words.contains("金额")) {
                    String money = "";
                    for (int j = 0; j < words.length(); j++) {
                        if ((words.charAt(j) >= '0' && words.charAt(j) <= '9')) {
                            money += words.charAt(j);
                        }
                        else if(words.charAt(j) == '.'|| words.charAt(j) == '·'){
                            money += '.';
                        }
                    }
                    if(money.indexOf('.')==money.length()-2){
                        money+='0';
                    }
                    Log.e("shl",money);
                    etAmount.setText(money);
                }
            }
        }
        catch(Exception e){
            Log.e("e","failed parse json");
        }

    }

    public void setType(String jsonResult) {
        if(jsonResult.contains("上车")||jsonResult.contains("下车")||jsonResult.contains("出租")){
            spinner.setSelection(0);
        }

    }
    private void infoPopText(final String result) {
        alertText("happy", result);
    }
    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                alertDialog.setTitle(title)
//                        .setMessage(message)
//                        .setPositiveButton("确定", null)
//                        .show();
            }
        });
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
        startActivityForResult(intent, 2);
    }


    /**
     * @描述 4.4及以上系统使用这个方法处理图片 相册图片返回的不再是真实的Uri,而是分装过的Uri
     * */
    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        imagePath = null;
        Log.e("TAG1", "hello");
        if (data == null){
            Log.e("TAG", "hello5");
        }
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
}
