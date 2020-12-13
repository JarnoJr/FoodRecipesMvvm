package com.example.foodrecipes.requests;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.foodrecipes.AppExecutors;
import com.example.foodrecipes.models.Recipe;
import com.example.foodrecipes.requests.responses.RecipeResponse;
import com.example.foodrecipes.requests.responses.RecipeSearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

import static com.example.foodrecipes.util.Constants.NETWORK_TIMEOUT;

public class RecipeApiClient {
    private static final String TAG = "RecipeApiClient";

    private static RecipeApiClient instance;
    private MutableLiveData<List<Recipe>> mRecipes;
    private MutableLiveData<Recipe> mRecipe;
    private RetrieveRecipesRunnable mRetrieveRecipesRunnable;
    private RetrieveRecipeRunnable mRetrieveRecipeRunnable;
    private MutableLiveData<Boolean> mRecipeRequestTimeout = new MutableLiveData<>();

    public static RecipeApiClient getInstance() {
        if (instance == null) {
            instance = new RecipeApiClient();
        }
        return instance;
    }

    private RecipeApiClient() {
        mRecipes = new MutableLiveData<>();
        mRecipe = new MutableLiveData<>();
    }

    public MutableLiveData<List<Recipe>> getRecipes() {
        return mRecipes;
    }
    public MutableLiveData<Recipe> getRecipe() {
        return mRecipe;
    }

    public MutableLiveData<Boolean> isRecipeRequestTimeout() {
        return mRecipeRequestTimeout;
    }

    public void searchRecipesApi(String query, int pageNumber) {

        if (mRetrieveRecipesRunnable != null) {
            mRetrieveRecipesRunnable = null;
        }
        mRetrieveRecipesRunnable = new RetrieveRecipesRunnable(query, pageNumber);
        final Future handler = AppExecutors.getInstance().networkIO().submit(mRetrieveRecipesRunnable);


        AppExecutors.getInstance().networkIO().schedule(() -> {
            //let the user know its times out
            mRecipeRequestTimeout.postValue(true);
            handler.cancel(true);
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void searchRecipeById(String recipeId){
        if(mRetrieveRecipeRunnable != null){
            mRetrieveRecipeRunnable = null;
        }
        mRetrieveRecipeRunnable = new RetrieveRecipeRunnable(recipeId);
        final Future handler = AppExecutors.getInstance().networkIO().submit(mRetrieveRecipeRunnable);

        mRecipeRequestTimeout.setValue(false);

        AppExecutors.getInstance().networkIO().schedule((Runnable) () -> {
            //let the user know it's timed out
           handler.cancel(true);
        },NETWORK_TIMEOUT,TimeUnit.MILLISECONDS);
    }


    private class RetrieveRecipesRunnable implements Runnable {
        private String query;
        private int pageNumber;
        boolean cancelRequest;

        public RetrieveRecipesRunnable(String query, int pageNumber) {
            this.query = query;
            this.pageNumber = pageNumber;
            cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response response = getRecipes(query, pageNumber).execute();
                if (cancelRequest) {
                    return;
                }
                if (response.code() == 200) {
                    List<Recipe> list = new ArrayList<>(((RecipeSearchResponse) response.body()).getRecipes());
                    if (pageNumber == 1) {
                        mRecipes.postValue(list);
                    } else {
                        List<Recipe> current = mRecipes.getValue();
                        current.addAll(list);
                        mRecipes.postValue(current);
                    }
                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    mRecipes.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                mRecipes.postValue(null);
            }

        }

        private Call<RecipeSearchResponse> getRecipes(String query, int pageNumber) {
            return ServiceGenerator.getRecipeApi().searchRecipe(
                    query,
                    String.valueOf(pageNumber)
            );
        }

        private void cancelRequest() {
            Log.d(TAG, "cancelRequest: canceling the search");
            cancelRequest = true;
        }
    }

    private class RetrieveRecipeRunnable implements Runnable {
        private String recipeId;
        boolean cancelRequest;

        public RetrieveRecipeRunnable(String recipeId) {
            this.recipeId = recipeId;
            cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response response = getRecipe(recipeId).execute();
                if (cancelRequest) {
                    return;
                }
                if (response.code() == 200) {
                    Recipe recipe = ((RecipeResponse)response.body()).getRecipe();
                    mRecipe.postValue(recipe);
                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    mRecipe.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                mRecipe.postValue(null);
            }

        }

        private Call<RecipeResponse> getRecipe(String recipeId) {
            return ServiceGenerator.getRecipeApi().getRecipe(recipeId);
        }

        private void cancelRequest() {
            Log.d(TAG, "cancelRequest: canceling the search");
            cancelRequest = true;
        }
    }

    public void cancelRequest() {
        if (mRetrieveRecipesRunnable != null) {
            mRetrieveRecipesRunnable.cancelRequest();
        }
        if(mRetrieveRecipeRunnable != null){
            mRetrieveRecipesRunnable.cancelRequest();
        }
    }
}

