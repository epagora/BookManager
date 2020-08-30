package com.epagora.tsundokumanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class DeleteDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DatabaseAdapter dbAdapter = new DatabaseAdapter(getActivity());
        final Bundle args = Objects.requireNonNull(getArguments());
        final int id = args.getInt("id");
        final String table = args.getString("table");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setTitle(args.getString("itemName"))
                .setMessage(R.string.really_delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dbAdapter.open();
                        dbAdapter.selectDelete(table, id);
                        dbAdapter.close();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create();
    }
}
