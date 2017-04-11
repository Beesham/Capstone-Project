package com.beesham.beerac.ui.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.beesham.beerac.R;

/**
 * Created by Beesham on 3/22/2017.
 */

public class AboutPreference extends DialogPreference {

    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AboutPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);

        setLayoutResource(R.layout.about_preference_child);
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

    /**
     * Adds the EditText widget of this preference to the dialog's view.
     *
     * @param dialogView The dialog view.
     */
    protected void onAddTextViewToDialogView(View dialogView, TextView textView) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.edittext_container);
        if (container != null) {
            container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

}
