package com.example.proyecto1.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.example.proyecto1.R;
import com.example.proyecto1.utilities.MyDB;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class SingleNoteFragment extends Fragment {

    private WebView noteContent;
    private String textOfNote;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View myFragmentView = inflater.inflate(R.layout.single_note_fragment, parent, false);
        // se han asignado aquí las variables porque si se hace en el método onActivityCreated
        // luego son null, no se ha conseguido determinar el porqué aunque en NotesFragment funciona
        // perfectamente
        noteContent = myFragmentView.findViewById(R.id.noteContent); // assign it to a variable
        noteContent.setBackgroundColor(Color.argb(1, 255, 255, 255)); // make the background transparent
        noteContent.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // the user decides with what application to open the url
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            }
        });
        return myFragmentView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Setup any handles to view objects here
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null){
            if (savedInstanceState.containsKey("textOfNote")){
                textOfNote = savedInstanceState.getString("textOfNote");
            }
        }

    }


    /**
     * Loads a note information and content knowing its id
     * @param noteId - the id of the note
     */
    public void loadNote(int noteId){
        if (noteContent != null){
            MyDB gestorDB = new MyDB(getActivity().getApplicationContext(), "Notes", null, 1);
            // the filename where the content of the note is
            String noteFileName = gestorDB.getNoteFileName(noteId);
            try {
                if (noteFileName == null){
                    throw new FileNotFoundException();
                }
                BufferedReader ficherointerno = new BufferedReader(new InputStreamReader(
                        getActivity().openFileInput(noteFileName)));
                textOfNote = "";
                String line;
                while ((line = ficherointerno.readLine()) != null) {
                    textOfNote += line;
                }
                ficherointerno.close();
            }catch (Exception e){
                textOfNote = getResources().getString(R.string.fileNotFound);
            }
            // se añade el contenido de la nota
            noteContent.loadData(textOfNote,"text/html; charset=UTF-8",
                    null);
        }
    }

    /**
     * Get the note content
     * @return - String with the content
     */
    public String getNoteContent(){
        if (textOfNote != null){
            return textOfNote;
        }else{
            return "Error";
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("textOfNote", textOfNote);
    }
}