package com.faltenreich.diaguard.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.adapter.FoodPagerAdapter;
import com.faltenreich.diaguard.data.entity.Food;
import com.faltenreich.diaguard.ui.activity.EntryActivity;
import com.squareup.picasso.Picasso;

import butterknife.BindView;

/**
 * Created by Faltenreich on 27.09.2016.
 */

public class FoodFragment extends BaseFoodFragment {

    public static final String EXTRA_FOOD_ID = "EXTRA_FOOD_ID";

    @BindView(R.id.food_image) ImageView image;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.food_viewpager) ViewPager viewPager;
    @BindView(R.id.food_tablayout) TabLayout tabLayout;

    public FoodFragment() {
        super(R.layout.fragment_food, R.string.food);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.food, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                return true;
            case R.id.action_eat:
                eatFood();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        Food food = getFood();
        if (food != null) {
            Picasso.with(getContext()).load(food.getImageUrl()).into(image);
            collapsingToolbarLayout.setTitle(food.getName());
            viewPager.setAdapter(new FoodPagerAdapter(getFragmentManager(), food));
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    private void eatFood() {
        Intent intent = new Intent(getActivity(), EntryActivity.class);
        intent.putExtra(EntryActivity.EXTRA_FOOD, getFood().getId());
        startActivity(intent);
    }
}
