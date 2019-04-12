package com.example.proyecto1.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.proyecto1.R;
import com.example.proyecto1.utilities.Data;
import com.example.proyecto1.utilities.MyDB;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

public class SelectTagEditor extends DialogFragment {

    ListenerDelDialogo miListener;

    int selectedTagId = -1; // selected tag id


    public interface ListenerDelDialogo {
        void addTagToPost(int tagId);
        void createNewTag();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey("selectedTagId")){
            // activity restarted
            selectedTagId = savedInstanceState.getInt("selectedTagId");
        }else if (getArguments() != null) {
            // just created the dialog, the user has previously selected a tag
            Bundle bundle = getArguments();
            selectedTagId = bundle.getInt("chosenTagId");
        }

        miListener = (ListenerDelDialogo) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.addTag_title));

        // get the tags names with their ids
        MyDB gestorDB = new MyDB(getActivity().getApplicationContext(), "Notes", null, 1);
        final ArrayList<ArrayList<String>> data =
                gestorDB.getTagsByUser(Data.getMyData().getActiveUsername());

        if (data == null){
            // database error
            int tiempo = Toast.LENGTH_SHORT;
            Toast aviso = Toast.makeText(getActivity(), R.string.databaseError, tiempo);
            aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
            aviso.show();
        }else {
            CharSequence[] tagNames = data.get(1).toArray(new CharSequence[data.get(1).size()]);

            int pos = -1;
            if (selectedTagId != -1) {
                // A tag has been chosen previously, select it again
                pos = data.get(0).indexOf(String.valueOf(selectedTagId));
            }

            builder.setSingleChoiceItems(tagNames, pos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // The user has selected a tag & we save its id
                    int newSelectedTagId = Integer.valueOf(data.get(0).get(which));
                    if (newSelectedTagId == selectedTagId) {
                        ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                        selectedTagId = -1;
                    } else {
                        selectedTagId = newSelectedTagId;
                    }
                }
            });
        }

        String positiveButton = getResources().getString(R.string.insertLink_save);
        String negativeButton = getResources().getString(R.string.insertLink_cancel);
        String addNewTag = getResources().getString(R.string.addTag_newTag);

        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save
                miListener.addTagToPost(selectedTagId);
            }
        });

        builder.setNeutralButton(addNewTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // create new tag
                miListener.createNewTag();
            }
        });

        builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedTagId", selectedTagId);
    }
}
