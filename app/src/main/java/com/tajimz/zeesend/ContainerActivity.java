package com.tajimz.zeesend;

import android.os.Bundle;
import com.tajimz.zeesend.databinding.ActivityContainerBinding;
import com.tajimz.zeesend.helper.BaseActivity;
import com.tajimz.zeesend.helper.CONSTANTS;
import com.tajimz.zeesend.tab.ProfileFragment;

public class ContainerActivity extends BaseActivity {
    ActivityContainerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupEdgeToEdge();
        String status = getIntent().getStringExtra("status");
        if ("profile".equals(status)) handleProfile();
    }



    private void handleProfile(){

        Bundle bundle = new Bundle();
        bundle.putString(CONSTANTS.name, getIntent().getStringExtra(CONSTANTS.name));
        bundle.putString(CONSTANTS.bio, getIntent().getStringExtra(CONSTANTS.bio));
        bundle.putString(CONSTANTS.email, getIntent().getStringExtra(CONSTANTS.email));
        bundle.putString(CONSTANTS.image, getIntent().getStringExtra(CONSTANTS.image));
        bundle.putString(CONSTANTS.username, getIntent().getStringExtra(CONSTANTS.username));
        bundle.putString(CONSTANTS.id, getIntent().getStringExtra(CONSTANTS.id));
        bundle.putString("fcm", getIntent().getStringExtra("fcm"));
        String rawDate = getIntent().getStringExtra(CONSTANTS.createTime);
        bundle.putString(CONSTANTS.createTime, rawDate != null ? cleanDate(rawDate) : "");

        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, profileFragment)
                .commit();


    }

}