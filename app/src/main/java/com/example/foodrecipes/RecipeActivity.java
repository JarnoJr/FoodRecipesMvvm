package com.example.foodrecipes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodrecipes.models.Recipe;
import com.example.foodrecipes.viewmodel.RecipeViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeActivity extends BaseActivity {
    private static final String TAG = "RecipeActivity";

    //UI components
    @BindView(R.id.recipe_image)
    AppCompatImageView mRecipeImage;
    @BindView(R.id.recipe_title)
    TextView mRecipeTitle;
    @BindView(R.id.recipe_social_score)
    TextView mRecipeRank;
    @BindView(R.id.container)
    LinearLayout mRecipeIngredientsContainer;
    @BindView(R.id.parent)
    ScrollView mScrollView;

    private RecipeViewModel mRecipeViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        ButterKnife.bind(this);
        mRecipeViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(RecipeViewModel.class);
        showProgressBar(true);
        getIncomingIntent();
        subscribeObservers();
    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("recipe")) {
            Recipe recipe = getIntent().getParcelableExtra("recipe");
            mRecipeViewModel.searchRecipeById(recipe.getRecipe_id());
        }
    }

    private void subscribeObservers() {
        mRecipeViewModel.getRecipe().observe(this, recipe -> {
            if (recipe != null) {
                if (recipe.getRecipe_id().equals(mRecipeViewModel.getRecipeId())) {
                    setRecipeProperties(recipe);
                    mRecipeViewModel.setDidRetrieveRecipe(true);
                }
            }
        });
        mRecipeViewModel.isRecipeRequestTimedOut().observe(this, isTimedOut -> {
                    if (isTimedOut && !mRecipeViewModel.didRetrieveRecipe()) {
                        Log.d(TAG, "subscribeObservers: timed out");
                        displayErrorScreen("Error Retrieving data.Check network connection.");
                    }
                }
        );
    }

    private void displayErrorScreen(String errorMessage) {
        mRecipeTitle.setText("Error retrieving recipe...");
        mRecipeRank.setText("");
        TextView textView = new TextView(this);
        textView.setTextSize(15);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        if (!errorMessage.equals("")) {
            textView.setText(errorMessage);
        } else {
            textView.setText("Error");
        }

        mRecipeIngredientsContainer.addView(textView);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);
        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(R.drawable.ic_launcher_background)
                .into(mRecipeImage);
        showParent();
        showProgressBar(false);
        mRecipeViewModel.setDidRetrieveRecipe(true);
    }

    private void setRecipeProperties(Recipe recipe) {
        if (recipe != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background);
            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(recipe.getImage_url())
                    .into(mRecipeImage);
            mRecipeTitle.setText(recipe.getTitle());
            mRecipeRank.setText(String.valueOf(Math.round(recipe.getSocial_rank())));

            mRecipeIngredientsContainer.removeAllViews();
            for (String ingredient : recipe.getIngredients()) {
                TextView textView = new TextView(this);
                textView.setText(ingredient);
                textView.setTextSize(15);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                mRecipeIngredientsContainer.addView(textView);
            }
        }
        showParent();
        showProgressBar(false);

    }

    private void showParent() {
        mScrollView.setVisibility(View.VISIBLE);
    }
}

