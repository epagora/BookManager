package com.epagora.tsundokumanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class MainDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int[] items = {R.string.rename,R.string.delete};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(name)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case R.string.rename:

                                break;
                            case R.string.delete:
                                break;
                        }
                    }
                })
                .create();
    }
}
