package com.course.localization.exactumpositioner;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Button;

/**
 * Created by Pete on 25.11.2015.
 */
public class OptionProgressDialog extends ProgressDialog {
    public OptionProgressDialog(Context context) {
        super(context);
    }

    public OptionProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void show(){
        super.show();
        hideButtons();
    }

    private void showButtons(){
        getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(Button.VISIBLE);
        getButton(ProgressDialog.BUTTON_NEUTRAL).setVisibility(Button.VISIBLE);
    }

    private void hideButtons(){
        getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(Button.INVISIBLE);
        getButton(ProgressDialog.BUTTON_NEUTRAL).setVisibility(Button.INVISIBLE);
    }

    @Override
    public void incrementProgressBy(int num){
        if( getProgress() + num == getMax()){
            showButtons();
        }
        super.incrementProgressBy(num);
    }

    @Override
    public void dismiss(){
        incrementProgressBy(-1 * getProgress());
        hideButtons();
        super.dismiss();
    }
}
