package com.example.foodrecipes;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public abstract class BaseActivity extends AppCompatActivity {

    public ProgressBar progressBar;

    @Override
    public void setContentView(int layoutID) {
        ConstraintLayout constraintLayout=(ConstraintLayout)getLayoutInflater().inflate(R.layout.activity_base,null);
        FrameLayout frameLayout = constraintLayout.findViewById(R.id.activity_content);
        progressBar=constraintLayout.findViewById(R.id.progress_bar);
        getLayoutInflater().inflate(layoutID,frameLayout,true);
        super.setContentView(layoutID);
    }

    public void showProgressBar(boolean visibility){
        progressBar.setVisibility(visibility ? View.VISIBLE :View.INVISIBLE);
    }
}

