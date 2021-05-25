package com.bhavishaymankani.ocrscanner;


import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ImageRetainingFragment extends Fragment {
    private Bitmap selectedImageBitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain the fragment.
        setRetainInstance(true);
    }

    public Bitmap getSelectedImageBitmap() {
        return selectedImageBitmap;
    }

    public void setSelectedImageBitmap(Bitmap selectedImageBitmap) {
        this.selectedImageBitmap = selectedImageBitmap;
    }
}
