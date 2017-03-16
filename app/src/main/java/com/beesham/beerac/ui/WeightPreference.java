package com.beesham.beerac.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import com.beesham.beerac.R;

/**
 * Created by Beesham on 3/10/2017.
 * Custom dialog preference to allow immediate display of pref changes
 */

public class WeightPreference extends DialogPreference {

    private static final String LOG_TAG = WeightPreference.class.getSimpleName();

    /**
     * The edit text shown in the dialog.
     */
    private EditText mEditText;

    private String mText;
    private boolean mTextSet;

    public WeightPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mEditText = new EditText(context, attrs);

        // Give it an ID so it can be saved/restored
        mEditText.setId(R.id.edit);

        /*
         * The preference framework and view framework both have an 'enabled'
         * attribute. Most likely, the 'enabled' specified in this XML is for
         * the preference framework, but it was also given to the view framework.
         * We reset the enabled state.
         */
        mEditText.setEnabled(true);
    }

    public WeightPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WeightPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);

        setLayoutResource(R.layout.preference_child);

        setDialogLayoutResource(R.layout.preference_dialog_edittext);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public WeightPreference(Context context) {
        this(context, null);
    }

     /**
     * Saves the text to the {SharedPreferences}.
     *
     * @param text The text to save
     */
    public void setText(String text) {
        // Always persist/notify the first time.
        final boolean changed = !TextUtils.equals(mText, text);
        if (changed || !mTextSet) {
            mText = text;
            mTextSet = true;
            persistString(text);
            if(changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    /**
     * Gets the text from the {SharedPreferences}.
     *
     * @return The current preference value.
     */
    public String getText() {
        return mText;
    }

    /**
     * Setup the dialog view
     * Set edittext to value from SharedPrefs because of delay of stock implementation
     * @param view
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = mEditText;

        String text = "";
        if(!TextUtils.isEmpty(PreferenceManager.getDefaultSharedPreferences(view.getContext())
                .getString(view.getContext().getString(R.string.pref_body_weight_key), ""))) {
            text = Double.toString(Math.ceil(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(view.getContext())
                    .getString(view.getContext().getString(R.string.pref_body_weight_key), null))));
        }

        editText.setText(text);

        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    /**
     * Adds the EditText widget of this preference to the dialog's view.
     *
     * @param dialogView The dialog view.
     */
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.edittext_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setText(restoreValue ? getPersistedString(mText) : (String) defaultValue);
    }
    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.text = getText();
        return myState;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.text);
    }

    private static class SavedState extends BaseSavedState {
        String text;

        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }
        public SavedState(Parcelable superState) {
            super(superState);
        }
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
