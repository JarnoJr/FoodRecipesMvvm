package com.example.foodrecipes.util;

import android.util.Log;

import com.example.foodrecipes.models.Recipe;

import java.util.List;

public class Testing {

    public static void printRecipes(List<Recipe> recipes,String tag){
        for (Recipe recipe : recipes) {
            Log.d(tag, "printRecipes: "+recipe.getTitle());
        }
    }
}

