package com.epagora.tsundokumanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class MyDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int[] items = {R.string.rename,R.string.delete};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    }
}
