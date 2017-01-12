package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;

/**
 * Created by ntonani on 1/9/17.
 */

public class DetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Add detail fragment to fragment stack
        if(savedInstanceState==null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(getString(R.string.stock_symbol_variable),getIntent().getData());

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_detail, detailFragment,DetailFragment.class.getSimpleName())
                    .commit();
        }

    }
}
