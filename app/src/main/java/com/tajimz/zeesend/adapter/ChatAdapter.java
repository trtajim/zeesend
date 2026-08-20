package com.tajimz.zeesend.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tajimz.zeesend.databinding.LayoutChatBinding;
import com.tajimz.zeesend.helper.CONSTANTS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolderChat> {
    Context context;
    JSONArray jsonArray;
    String userId;

    public ChatAdapter(Context context, String userId, JSONArray jsonArray){
        this.context = context;
        this.jsonArray = jsonArray;
        this.userId = userId;
    }

    public class ViewHolderChat extends RecyclerView.ViewHolder {
        LayoutChatBinding binding;

        public ViewHolderChat(LayoutChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LayoutChatBinding binding = LayoutChatBinding.inflate(inflater, parent, false);
        return new ViewHolderChat(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderChat holder, int position) {
        try {
            JSONObject jsonObject = (JSONObject) jsonArray.get(position);
            String message = jsonObject.optString("message", "");
            String senderId = jsonObject.optString(CONSTANTS.sender_id, "");

            if (userId.equals(senderId)) {
                holder.binding.tvMe.setText(message);
                holder.binding.tvHe.setVisibility(GONE);
                holder.binding.tvMe.setVisibility(VISIBLE);
            } else {
                holder.binding.tvHe.setText(message);
                holder.binding.tvMe.setVisibility(GONE);
                holder.binding.tvHe.setVisibility(VISIBLE);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public void updateData(JSONArray newArray) {
        this.jsonArray = newArray;
        notifyDataSetChanged();

    }

    public void addData(JSONArray jsonArray1){
        int start = jsonArray.length();
        for (int i = 0 ; i < jsonArray1.length(); i ++){
            try {
                jsonArray.put(jsonArray1.get(i));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
        notifyItemRangeInserted(start, jsonArray1.length());
    }
}
