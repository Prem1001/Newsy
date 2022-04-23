package com.experiments.newsy.ui.sources;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.experiments.newsy.R;
import com.experiments.newsy.adapters.SourceAdapter;
import com.experiments.newsy.databinding.FragmentSourceBinding;
import com.experiments.newsy.models.Source;
import com.experiments.newsy.models.Specification;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class SourceFragment extends Fragment implements SourceAdapter.SourceAdapterListener {

    private final SourceAdapter sourceAdapter = new SourceAdapter(null, this);
    private InterstitialAd mInterstitialAd;

    public SourceFragment() {
        // Required empty public constructor
    }

    public static SourceFragment newInstance() {
        return new SourceFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentSourceBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_source, container, false);

        setupViewModel();
        binding.rvSources.setAdapter(sourceAdapter);
        if (getContext() != null) {
            DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            divider.setDrawable(getResources().getDrawable(R.drawable.recycler_view_divider));
            binding.rvSources.addItemDecoration(divider);
        }


        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstital_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        return binding.getRoot();
    }

    private void setupViewModel() {
        SourceViewModel viewModel = ViewModelProviders.of(this).get(SourceViewModel.class);
        Specification specification = new Specification();
        specification.setLanguage(Locale.getDefault().getLanguage());
        specification.setCountry(null);
        viewModel.getSource(specification).observe(getViewLifecycleOwner(), new Observer<List<Source>>() {
            @Override
            public void onChanged(@Nullable List<Source> sources) {
                if (sources != null) {
                    sourceAdapter.setSources(sources);
                }
            }
        });
    }


    @Override
    public void onSourceButtonClicked(Source source) {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    openFulSotry(source);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    openFulSotry(source);
                }
            });
            mInterstitialAd.show();
        }
        else {
            openFulSotry(source);
        }
    }

    private void openFulSotry(Source source) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getContext(), Uri.parse(source.getUrl()));
    }
}
