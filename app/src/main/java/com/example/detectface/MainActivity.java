package com.example.detectface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.detectface.retrofit.APIUtils;
import com.example.detectface.retrofit.DataClient;
import com.mindorks.paracamera.Camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    Camera camera;
    Bitmap bitmap;
    String realpath;
    private static final int GALLERY = 999;
    Button btnchoose;
    ImageView ivStudent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Build the camera
        camera = new Camera.Builder()
                .resetToCorrectOrientation(true)// it will rotate the camera bitmap to the correct orientation from meta data
                .setTakePhotoRequestCode(1)
                .setDirectory("pics")
                .setName("ali_" + System.currentTimeMillis())
                .setImageFormat(Camera.IMAGE_JPEG)
                .setCompression(75)
                .setImageHeight(1000)// it will try to achieve this height as close as possible maintaining the aspect ratio;
                .build(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        anhxa();
        action();
    }

    private void action() {
        btnchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Sơn tít dz", Toast.LENGTH_SHORT).show();
                showDialog();
            }
        });
        ivStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void anhxa() {
        btnchoose = (Button) findViewById(R.id.btn_ok);
        ivStudent = (ImageView) findViewById(R.id.iv_student);
    }
    private void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);
        TextView tvTakephoto = dialog.findViewById(R.id.tv_takephoto);
        TextView tvFromgallery = dialog.findViewById(R.id.tv_gallery);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tvTakephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                try {
                    camera.takePicture();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        });
        tvFromgallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                choosePhotoFromGallary();
            }
        });

        dialog.show();
    }
    private void choosePhotoFromGallary() {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("test","On result Cammera");
        if(requestCode == Camera.REQUEST_TAKE_PHOTO){
            bitmap = camera.getCameraBitmap();
            if(bitmap != null) {
                ivStudent.setImageBitmap(bitmap);
            }else{
                Toast.makeText(this.getApplicationContext(),"Picture not taken!",Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode==GALLERY){
            Uri url =  data.getData();
            realpath = getRealPathFromURI(url);
            Log.d("test",realpath);
            File file = new File(realpath);
            String file_path = file.getAbsolutePath();
            Log.d("tests",file.getAbsolutePath());
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image",file_path,requestBody);

            DataClient dataClient = APIUtils.getData();
            Call<String> callback = dataClient.UploadPhoto(body);
            callback.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.d("test","success");
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.d("test","Fail");
                    Log.d("error",t.getMessage());
                }
            });




            //            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(),url);
//                ivStudent.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(url);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if(bitmap!= null){
                    ivStudent.setImageBitmap(bitmap);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }
    private String getRealPathFromURI(Uri contentURI)
    {
        String result = null;

        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);

        if (cursor == null)
        { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        }
        else
        {
            if(cursor.moveToFirst())
            {
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            }
            cursor.close();
        }
        return result;
    }
}
