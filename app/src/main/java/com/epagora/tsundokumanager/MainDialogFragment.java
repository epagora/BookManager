package com.epagora.tsundokumanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class MainDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = Objects.requireNonNull(getArguments());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setTitle(args.getString("name"))
                .setItems(R.array.main_dialog_list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (getResources().obtainTypedArray(R.array.main_dialog_list).getResourceId(i, -1)) {
                            case R.string.rename:
                                DialogFragment editDialog = new EditDialogFragment();
                                editDialog.setArguments(args);
                                editDialog.show(getFragmentManager(), "dialog_edit");
                                break;
                            case R.string.delete:
                                DialogFragment deleteDialog = new DeleteDialogFragment();
                                deleteDialog.setArguments(args);
                                deleteDialog.show(getFragmentManager(), "dialog_delete");
                                break;
                            default:
                        }
                    }
                })
                .create();
    }
}
