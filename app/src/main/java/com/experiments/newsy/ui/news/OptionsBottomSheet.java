package com.experiments.newsy.ui.news;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.experiments.newsy.R;
import com.experiments.newsy.data.NewsRepository;
import com.experiments.newsy.databinding.FragmentOptionsBottomSheetBinding;
import com.experiments.newsy.ui.MainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class OptionsBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String PARAM_TITLE = "param-title";
    private static final String PARAM_URL = "param-url";
    private static final String PARAM_ID = "param-id";
    private static final String PARAM_SAVED = "param-saved";
    private static OptionsBottomSheet fragment;
    private String title;
    private String url;
    private int id;
    private boolean isSaved;
    private OptionsBottomSheetListener listener;
    private final Handler handler = new Handler();


    public OptionsBottomSheet() {
        // Required empty public constructor
    }

    public static OptionsBottomSheet getInstance(String title, String url, int id, boolean isSaved) {
        fragment = new OptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(PARAM_TITLE, title);
        args.putString(PARAM_URL, url);
        args.putInt(PARAM_ID, id);
        args.putBoolean(PARAM_SAVED, isSaved);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(PARAM_TITLE);
            url = getArguments().getString(PARAM_URL);
            id = getArguments().getInt(PARAM_ID);
            isSaved = getArguments().getBoolean(PARAM_SAVED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentOptionsBottomSheetBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_options_bottom_sheet, container, false);

        if (isSaved) {
            binding.btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_saved_item, 0, 0, 0);
        }
        binding.btnShare.setOnClickListener(this);
        binding.btnOpenInBrowser.setOnClickListener(this);
        binding.btnSave.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        try {
            Intent intent;
            switch (v.getId()) {
                case R.id.btn_open_in_browser:
                    listener.onOpenInBrowser(() -> {
                        fragment.dismiss();
                        openFullStory();
                    });
                    break;
                case R.id.btn_share:
                    String shareText = title + "\n" + url;
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, shareText);
                    intent.setType("text/plain");
                    this.dismiss();
                    startActivity(intent);
                    break;
                case R.id.btn_save:
                    if (isSaved) {
                        NewsRepository.getInstance(getContext()).removeSaved(id);
                        listener.onSaveToggle(getString(R.string.message_item_removed));
                    } else {
                        NewsRepository.getInstance(getContext()).save(id);
                        listener.onSaveToggle(getString(R.string.message_item_saved));
                    }
                    Timber.d("Saved for id  : %s", id);
                    dismiss();
                    break;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    private void openFullStory() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getContext(), Uri.parse(url));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OptionsBottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OptionsBottomSheetListener");
        }
    }

    public interface OptionsBottomSheetListener {
        void onSaveToggle(String text);

        void onOpenInBrowser(Runnable action);
    }
}
