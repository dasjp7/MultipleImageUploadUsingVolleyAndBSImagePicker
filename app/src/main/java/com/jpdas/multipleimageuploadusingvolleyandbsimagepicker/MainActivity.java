package com.jpdas.multipleimageuploadusingvolleyandbsimagepicker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.asksira.bsimagepicker.BSImagePicker;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.OnMultiImageSelectedListener, BSImagePicker.ImageLoaderDelegate {


    private Button choosePhotos;
    private RequestManager mGlideRequestManager;
    private ViewGroup mSelectedImagesContainer;
    private String YOURURL ="http://github.dummyurl.com/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGlideRequestManager = Glide.with(this);
        inIt();

        permission();

    }

    private void inIt() {
        mSelectedImagesContainer = (ViewGroup) findViewById(R.id.selected_photos_container);
        choosePhotos = findViewById(R.id.choose_photos);

    }


    private void permission() {
        choosePhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(MainActivity.this)
                        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    BSImagePicker multiSelectionPicker = new BSImagePicker.Builder("com.yourdomain.yourpackage.fileprovider")
                                            .isMultiSelect() //Set this if you want to use multi selection mode.
                                            .setMinimumMultiSelectCount(1) //Default: 1.
                                            .setMaximumMultiSelectCount(4) //Default: Integer.MAX_VALUE (i.e. User can select as many images as he/she wants)
                                            .setMultiSelectBarBgColor(android.R.color.white) //Default: #FFFFFF. You can also set it to a translucent color.
                                            .setMultiSelectTextColor(R.color.primary_text) //Default: #212121(Dark grey). This is the message in the multi-select bottom bar.
                                            .setMultiSelectDoneTextColor(R.color.colorAccent) //Default: #388e3c(Green). This is the color of the "Done" TextView.
                                            .setOverSelectTextColor(R.color.error_text) //Default: #b71c1c. This is the color of the message shown when user tries to select more than maximum select count.
                                            .disableOverSelectionMessage() //You can also decide not to show this over select message.
                                            .build();
                                    multiSelectionPicker.show(getSupportFragmentManager(), "picker");
                                }

                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    showSettingsDialog();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));

        // Set language level to 8 if error shows.

        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);

    }

//You can use below function also for byteArray generation.

    public byte[] getBytes(InputStream inputStream) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        try {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return byteBuffer.toByteArray();
    }


    @Override
    public void loadImage(File imageFile, ImageView ivImage) {
        Glide.with(MainActivity.this).load(imageFile).into(ivImage);
    }

    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {

        mSelectedImagesContainer.removeAllViews();

        mSelectedImagesContainer.setVisibility(View.VISIBLE);

        int wdpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        int htpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());



        for (Uri uri : uriList) {
            View imageHolder = LayoutInflater.from(this).inflate(R.layout.image_item, null);
            ImageView thumbnail = (ImageView) imageHolder.findViewById(R.id.media_image);

            Glide.with(this)
                    .load(uri.toString())
                    .into(thumbnail);

            try {
                // You can update this bitmap to your server
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                //((BitmapDrawable) profilePicImage.getDrawable()).getBitmap();
                counter++;
                savePhoto(bitmap,uri,uriList.size());
                // loading profile image from local cache
            } catch (IOException e) {
                e.printStackTrace();
            }



            mSelectedImagesContainer.addView(imageHolder);


            thumbnail.setLayoutParams(new FrameLayout.LayoutParams(wdpx, htpx));
        }


    }


    @Override
    public void onSingleImageSelected(Uri uri, String tag) {

    }
    int counter=0;
    private void savePhoto(Bitmap bitmap,Uri imageUri,int file_count) {

            final ProgressDialog pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Uploading, please wait..."+counter);
            pd.show();

            //converting image to base64 string
            //bitmap = BitmapFactory.decodeResource(getResources(), R.id.shop_licence_image_1);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            Map<String, String> params = new HashMap<String, String>();

            // Those are my required variables for server

            params.put("file_name", imageUri.getPath());
            params.put("byte_array", imageString);
            params.put("file_count",String.valueOf(file_count));

            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, YOURURL,
                    new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if(pd!=null)
                        pd.dismiss();
                    try {
                        // Handling Errors After Api call.

                        if (response.getString("is_error").equals("1")) {


                        } else {

                            if (response.getInt("is_error") == 0) {

                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
//

                            }

                        }

                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(pd!=null)
                        pd.dismiss();
                    //Handling Different types of volley error .

                    if (error instanceof NoConnectionError)
                    {
                        Toast.makeText(MainActivity.this, "No network connection", Toast.LENGTH_SHORT).show();

                    } else if (error instanceof TimeoutError) {
                        Toast.makeText(MainActivity.this, "Slow network connection", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof AuthFailureError) {

                    } else if (error instanceof ServerError) {
                        Toast.makeText(MainActivity.this, "Something Went Wrong in Server", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(MainActivity.this, "Something Went Wrong in Network", Toast.LENGTH_SHORT).show();

                    } else if (error instanceof ParseError) {

                    }
                }
            }) {
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    Log.d(TAG, "Error in Connecting Volley" + volleyError.getMessage());
                    return super.parseNetworkError(volleyError);
                }


                //Required for Basic authentication if you are using .Otherwise remove this overridden method.

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    // add headers <key,value>
                    String credentials = "user_name"+ ":" + "password";
                    String auth = "Basic "
                            + Base64.encodeToString(credentials.getBytes(),
                            Base64.DEFAULT);
                    headers.put("Authorization", auth);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            requestQueue.add(objectRequest);
        }

    }

