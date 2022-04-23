package com.experiments.newsy.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.experiments.newsy.BuildConfig;
import com.experiments.newsy.R;
import com.experiments.newsy.data.NewsRepository;
import com.experiments.newsy.databinding.ActivityMainBinding;
import com.experiments.newsy.models.Article;
import com.experiments.newsy.ui.headlines.HeadlinesFragment;
import com.experiments.newsy.ui.news.NewsFragment;
import com.experiments.newsy.ui.news.OptionsBottomSheet;
import com.experiments.newsy.ui.sources.SourceFragment;
import com.experiments.newsy.widget.SavedNewsWidget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OptionsBottomSheet.OptionsBottomSheetListener {
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private ActivityMainBinding binding;
    private HeadlinesFragment headlinesFragment;
    private SourceFragment sourceFragment;
    private NewsFragment newsFragment;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            try {
                Bundle bundle = new Bundle();
                switch (item.getItemId()) {
                    case R.id.navigation_headlines:
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, headlinesFragment)
                                .commit();
                        return true;
                    case R.id.navigation_saved:
                        if (newsFragment == null) {
                            newsFragment = NewsFragment.newInstance(null);
                        }
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, newsFragment)
                                .commit();
                        return true;
                    case R.id.navigation_sources:
                        if (sourceFragment == null) {
                            sourceFragment = SourceFragment.newInstance();
                        }
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, sourceFragment)
                                .commit();
                        return true;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    };
    private Snackbar snackbar;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // Bind data using DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstital_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        if (savedInstanceState == null) {
            // Add a default fragment
            headlinesFragment = HeadlinesFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, headlinesFragment)
                    .commit();
        }


        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        final LiveData<List<Article>> saved = NewsRepository.getInstance(this).getSaved();
        saved.observe(this, new Observer<List<Article>>() {
            @Override
            public void onChanged(@Nullable List<Article> articles) {
                if (articles != null) {
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), SavedNewsWidget.class));
                    if (articles.size() == 0) {
                        SavedNewsWidget.updateNewsWidgets(getApplicationContext(), appWidgetManager, articles, -1, appWidgetIds);
                    } else {
                        SavedNewsWidget.updateNewsWidgets(getApplicationContext(), appWidgetManager, articles, 0, appWidgetIds);
                    }
                }
            }
        });
    }


    @Override
    public void onSaveToggle(String text) {
        if (snackbar == null) {
            snackbar = Snackbar.make(binding.coordinator, "Hello", Snackbar.LENGTH_SHORT);
            final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbar.getView().getLayoutParams();
            params.setMargins(
                    (int) getResources().getDimension(R.dimen.snackbar_margin_vertical),
                    0,
                    (int) getResources().getDimension(R.dimen.snackbar_margin_vertical),
                    (int) getResources().getDimension(R.dimen.snackbar_margin_horizontal)
            );
            snackbar.getView().setLayoutParams(params);
            snackbar.getView().setPadding(
                    (int) getResources().getDimension(R.dimen.snackbar_padding),
                    (int) getResources().getDimension(R.dimen.snackbar_padding),
                    (int) getResources().getDimension(R.dimen.snackbar_padding),
                    (int) getResources().getDimension(R.dimen.snackbar_padding)
            );
        }
        if (snackbar.isShown()) {
            snackbar.dismiss();
        }
        snackbar.setText(text);
        snackbar.show();
    }

    @Override
    public void onOpenInBrowser(Runnable action) {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    action.run();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    action.run();
                }
            });
            mInterstitialAd.show();
        } else {
            action.run();
        }
    }
}
