package com.tajimz.zeesend.helper;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.tajimz.zeesend.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {


    protected String getSharedPref(String keyword) {
        SharedPreferences sharedPreferences = getSharedPreferences(CONSTANTS.SHAREDPREF, MODE_PRIVATE);
        return sharedPreferences.getString(keyword, null);
    }

    protected void editSharedPref(String keyword, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(CONSTANTS.SHAREDPREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyword, value);
        editor.apply();
    }


    public interface ObjListener{
        void onSuccess(JSONObject result);
    }
    public interface ArrayListener{
        void onSuccess(JSONArray result);
    }

    protected void requestObj(Boolean silent, String url, JSONObject jsonObject, ObjListener objListener){
        if (!silent) startLoading();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                endLoading();
                objListener.onSuccess(jsonObject);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                endLoading();
                errorAlert("Server Error "+volleyError.toString());
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    protected void requestArray(Boolean silent , String url, JSONArray jsonArray, ArrayListener arrayListener){
        if (!silent) startLoading();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                arrayListener.onSuccess(jsonArray);
                endLoading();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                errorAlert("Server Error "+volleyError.toString());
                endLoading();
            }
        });
        requestQueue.add(jsonArrayRequest);


    }

    protected void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected String gettext(EditText editText){
        return editText.getText().toString().trim();
    }

    protected boolean isPass(String string ){
        return string.length() >= 8 && string.length() <=32;
    }






    //setup loading dialog

    private AlertDialog loadingDialog;
    protected void startLoading() {
        if (loadingDialog == null) {
            loadingDialog = new AlertDialog.Builder(this)
                    .setView(LayoutInflater.from(this).inflate(R.layout.alert_loading, null))
                    .setCancelable(false)
                    .create();
            if (loadingDialog.getWindow() != null)
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        loadingDialog.show();
    }

    protected void endLoading() {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
    }

    protected void errorAlert(String message){
        new AlertDialog.Builder(this).setTitle("Error occurred").setMessage(message).setNeutralButton("Understand", null).show();

    }

    protected static String generateUniqueId(Context context) {
        String androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        long time = System.currentTimeMillis();

        String base = androidId + time;

        long hash = Math.abs(base.hashCode());

        return String.format("%010d", hash % 1_000_000_0000L);
    }

    protected String getLastMessageTime(JSONArray jsonArray, String defaultValue){
        String time;
        if (jsonArray.length() == 0){
            return defaultValue;
        }
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);
            time = jsonObject.optString("message_time", defaultValue);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return time;

    }

    protected void putInJsonObj(JSONObject jsonObject, String key, Object value){
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    protected String getStrFromJsonObj(JSONObject jsonObject, String key){

        return jsonObject.optString(key, "");
    }

    protected String cleanDate(String date) {
        // Input format
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // Output format
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

        try {
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date; // fallback to original if parsing fails
        }
    }

    protected String getCurrentDate() {
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        return outputFormat.format(new Date());
    }

    protected void setupEdgeToEdge(){
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected void loadImage(String url, ImageView imageView){
        Picasso.get().load(url).placeholder(R.drawable.bydefault).into(imageView);
    }

    public static void sendToServer(Context context){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                String token = task.getResult();

                SharedPreferences sharedPreferences = context.getSharedPreferences(CONSTANTS.SHAREDPREF, MODE_PRIVATE);
                String id = sharedPreferences.getString(CONSTANTS.id, null);
                if (id == null) return;
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("text", token);
                    jsonObject.put("reason", "fcm");
                    jsonObject.put(CONSTANTS.id, id);
                    Log.d("tustus", jsonObject.toString());

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, CONSTANTS.appUrl+"others/edit.php", jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("fcm", token);
                            editor.apply();
                            Log.d("tustus", jsonObject.toString());

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d("tustus", volleyError.toString());
                        }
                    });
                    requestQueue.add(jsonObjectRequest);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }



            }
        });

    }








}
