package com.example.foodrecipes.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodrecipes.R;

public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView title, publisher, socialScore;
    ImageView imageView;
    OnRecipeListener onRecipeListener;

    public RecipeViewHolder(@NonNull View itemView, OnRecipeListener onRecipeListener) {
        super(itemView);
        this.onRecipeListener = onRecipeListener;
        title = itemView.findViewById(R.id.recipe_title);
        publisher = itemView.findViewById(R.id.recipe_publisher);
        socialScore = itemView.findViewById(R.id.recipe_social_score);
        imageView = itemView.findViewById(R.id.recipe_image);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onRecipeListener.onRecipeClick(getAdapterPosition());
    }
}

