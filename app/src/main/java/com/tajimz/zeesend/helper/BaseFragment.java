package com.tajimz.zeesend.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.tajimz.zeesend.R;
import com.tajimz.zeesend.SettingsActivity;
import com.tajimz.zeesend.SplashActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class BaseFragment extends Fragment {

    protected String getSharedPref(String keyword) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(CONSTANTS.SHAREDPREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(keyword, null);
    }

    protected void editSharedPref(String keyword, String value) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(CONSTANTS.SHAREDPREF, Context.MODE_PRIVATE);
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
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
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
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                arrayListener.onSuccess(jsonArray);
                endLoading();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                endLoading();

                if (volleyError.networkResponse != null) {
                    int statusCode = volleyError.networkResponse.statusCode;
                    String body = "";
                    try {
                        body = new String(volleyError.networkResponse.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    errorAlert("Server Error\nStatus Code: " + statusCode + "\nResponse: " + body);
                } else {
                    errorAlert("Server Error: " + volleyError.toString());
                }
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    protected void toast(String text){
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
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
            loadingDialog = new AlertDialog.Builder(requireContext())
                    .setView(LayoutInflater.from(requireContext()).inflate(R.layout.alert_loading, null))
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
        new AlertDialog.Builder(requireContext()).setTitle("Error occurred").setMessage(message).setNeutralButton("Understand", null).show();
    }

    public static String generateUniqueId(Context context) {
        String androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        long time = System.currentTimeMillis();

        String base = androidId + time;

        long hash = Math.abs(base.hashCode());

        return String.format("%010d", hash % 1_000_000_0000L);
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

    protected void handleMenu(View view){
        view.setOnClickListener(v->{
            PopupMenu menu = new PopupMenu(getContext(), v);
            menu.getMenuInflater().inflate(R.menu.three_dots, menu.getMenu());
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.settings){

                        startActivity(new Intent(getContext(), SettingsActivity.class));

                    }else if (id == R.id.privacy_policy){
                        openLink("https://google.com");
                    }else if (id == R.id.about_us){
                        toast("hi");
                    }else if (id == R.id.log_out){
                        editSharedPref(CONSTANTS.name, null);
                        Intent intent = new Intent(getContext(), SplashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    return false;
                }
            });
            menu.show();
        });
    }
    protected void openLink(String link){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
    }

    protected void loadImage(String url, ImageView imageView){
        Picasso.get().load(url).placeholder(R.drawable.bydefault).into(imageView);
    }

}
