package com.beesham.beerac.ui.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beesham.beerac.R;

/**
 * Created by Beesham on 3/22/2017.
 */

public class HowItWorksPreference extends DialogPreference {

    public HowItWorksPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HowItWorksPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HowItWorksPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);

        setLayoutResource(R.layout.about_preference_child);
        setDialogLayoutResource(R.layout.how_it_works_dialog);
        setPositiveButtonText(android.R.string.ok);
    }

    /**
     * Setup the dialog view
     * Set edittext to value from SharedPrefs because of delay of stock implementation
     * @param view
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }
}
