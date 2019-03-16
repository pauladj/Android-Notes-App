package com.example.proyecto1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto1.dialogs.SelectTagEditor;
import com.example.proyecto1.dialogs.ConfimExit;
import com.example.proyecto1.dialogs.DeleteTextStyles;
import com.example.proyecto1.dialogs.InsertLinkEditor;
import com.example.proyecto1.dialogs.NewTag;
import com.example.proyecto1.utilities.Data;
import com.example.proyecto1.utilities.MainToolbar;
import com.example.proyecto1.utilities.MyDB;
import com.example.proyecto1.utilities.SpanStyleHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;

public class NoteEditorActivity extends MainToolbar implements DeleteTextStyles.ListenerDelDialogo, InsertLinkEditor.ListenerDelDialogo, SelectTagEditor.ListenerDelDialogo, NewTag.ListenerDelDialogo {

    int noteId = -1; // If we are editing an existing note

    int chosenTagId = -1; // the selected tag by the user
    String chosenTagName = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Change the topbar options
        getMenuInflater().inflate(R.menu.editor_toolbar, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor_activity);
        // load top toolbar
        loadToolbar();
        showBackButtonOption();
        // add listeners
        final EditText noteBody = findViewById(R.id.noteBody);
        noteBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    findViewById(R.id.boldText).setEnabled(false);
                    findViewById(R.id.italicText).setEnabled(false);
                    findViewById(R.id.linkText).setEnabled(false);
                    findViewById(R.id.formatText).setEnabled(false);
                }else{
                    // The user can only use these buttons in the note body
                    findViewById(R.id.boldText).setEnabled(true);
                    findViewById(R.id.italicText).setEnabled(true);
                    findViewById(R.id.linkText).setEnabled(true);
                    findViewById(R.id.formatText).setEnabled(true);
                }
            }
        });

        /***
         * Extraído de Stack Overflow
         * Pregunta: https://stackoverflow.com/questions/24428808/how-to-scroll-the-edittext-inside-the-scrollview/
         * Autor: https://stackoverflow.com/users/1761003/mave%c5%88%e3%83%84
         **/
        noteBody.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (noteBody.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });

        // if it's an existing note load its content
        Bundle extras = getIntent().getExtras();

        if (extras != null){
            if (extras.containsKey("noteId")){
                noteId = extras.getInt("noteId");
                loadExistingNoteContent();
            }
        }

    }

    /**
     * If we are editing an existing note we load the data into the editor
     */
    private void loadExistingNoteContent(){
        if (noteId != -1){
            MyDB gestorDB = new MyDB(getApplicationContext(), "Notes", null, 1);
            String[] noteData = gestorDB.getNoteData(noteId);
            if (noteData != null){
                // there's no errors, load data
                TextView title = findViewById(R.id.titleInput);
                title.setText(noteData[0]); // set title

                if (noteData[2] != null){
                    // there's a tag, set the tag
                    addTagToPost(Integer.valueOf(noteData[3]), noteData[2]);
                }

                // set the body
                String text = "";
                try {
                    BufferedReader ficherointerno = new BufferedReader(new InputStreamReader(
                            openFileInput(noteData[1])));
                    String line;
                    while ((line = ficherointerno.readLine()) != null) {
                        text += line;
                    }
                    ficherointerno.close();
                } catch (IOException e) {
                    text = getResources().getString(R.string.fileNotFound);
                }
                SpannableString string = new SpannableString(Html.fromHtml(text));
                TextView noteBody = findViewById(R.id.noteBody);
                noteBody.setText(string);
            }else{
                // database error
                int tiempo = Toast.LENGTH_SHORT;
                Toast aviso = Toast.makeText(this, R.string.databaseError, tiempo);
                aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
                aviso.show();
            }

        }
    }


    /**
     * Check if the user has selected note body text
     * @return true if selected, false if no text has been selected
     */
    private boolean checkIfSelection(){
        EditText noteBody = findViewById(R.id.noteBody);
        if (noteBody.getSelectionEnd() != noteBody.getSelectionStart()){
            return true;
        }else{
            // toast with error
            int tiempo = Toast.LENGTH_SHORT;
            Toast aviso = Toast.makeText(getApplicationContext(), R.string.failBoldText,
                    tiempo);
            aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
            aviso.show();
            return false;
        }
    }

    /**
     * If the user wants bold font
     * @param v
     */
    public void boldClicked(View v){
        boolean selection = checkIfSelection();
        if (selection){ // Si el usuario ha seleccionado algún texto
            EditText noteBody = findViewById(R.id.noteBody);
            int end = noteBody.getSelectionEnd();
            SpanStyleHelper spanStyleHelper = new SpanStyleHelper(noteBody);
            noteBody.setText(
                    spanStyleHelper.toggleBoldSelectedText()
            );
            noteBody.setSelection(end);
        }
    }

    /**
     * If the user wants italic font
     * @param v
     */
    public void italicClicked(View v){
        boolean selection = checkIfSelection();
        if (selection) { // Si el usuario ha seleccionado algún texto
            EditText noteBody = findViewById(R.id.noteBody);
            int end = noteBody.getSelectionEnd();

            SpanStyleHelper spanStyleHelper = new SpanStyleHelper(noteBody);
            noteBody.setText(
                    spanStyleHelper.toggleItalicSelectedText()
            );
            noteBody.setSelection(end);
        }
    }

    /**
     * The user wants to insert a link
     * @param v
     */
    public void insertUrl(View v){
        // Show the dialog to so the user inputs the info
        DialogFragment confirmationDialog = new InsertLinkEditor();
        confirmationDialog.show(getSupportFragmentManager(), "insertLinkEditor");
    }

    /**
     * Append the url to the text content
     */
    public void yesInsertUrl(View textToShow, View inputLink){
        EditText textBody = findViewById(R.id.noteBody);
        int posIni = textBody.getSelectionStart(); // obtener posición del cursor en el texto

        // obtener texto con estilos
        SpannableStringBuilder spannable = new SpannableStringBuilder(textBody.getText());
        // crear cadena html con link
        String link =
                "<a href='" + ((TextView) inputLink).getText().toString() + "'>" + ((TextView) textToShow).getText().toString() +
                "</a>";
        // convertir html a texto spannable (estilos)
        SpannableStringBuilder formatedLink = (SpannableStringBuilder) Html.fromHtml(link);

        spannable.insert(posIni, formatedLink); // insert the link in the text
        textBody.setMovementMethod(LinkMovementMethod.getInstance()); // los links con clickables
        textBody.setText(spannable);
        textBody.setSelection(posIni); // dejar el cursor en el mismo sitio
    }


    /**
     * The user wants to remove all the styles
     * @param v
     */
    public void formatText(View v){
        // Show the dialog to confirm
        DialogFragment confirmationDialog = new DeleteTextStyles();
        confirmationDialog.show(getSupportFragmentManager(), "deleteTextStyles");
    }

    /**
     * The user confirms he wants to delete the text styles
     */
    public void yesDeleteTextStyles(){
        EditText textBody = findViewById(R.id.noteBody);

        SpannableStringBuilder spannable = new SpannableStringBuilder(textBody.getText());

        // get all the spans attached to the SpannedString
        Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);

        for (Object span : spans) {
            if (span instanceof CharacterStyle && !span.toString().contains("URL"))
                spannable.removeSpan(span);
        }
        textBody.setText(spannable);
    }


    /**
     * The user wants to add a tag to the post
     * @param v - the element clicked
     */
    public void addTag(View v){
        // Show the dialog to select one
        DialogFragment dialog = new SelectTagEditor();
        Bundle bl = new Bundle();
        // la etiqueta elegida actual
        bl.putInt("chosenTagId", chosenTagId);
        bl.putString("chosenTagName", chosenTagName);
        dialog.setArguments(bl);
        dialog.show(getSupportFragmentManager(), "addTagEditor");
    }

    /**
     * The user has selected a tag to add to the post
     * @param tagId - the id of the selected tag
     */
    public void addTagToPost(int tagId, String tagName){
        chosenTagId = tagId;
        chosenTagName = tagName;
        TextView a = findViewById(R.id.assignedTag);
        if (tagId == -1){
            // nothing selected, tag name not shown
            a.setVisibility(View.INVISIBLE);
        }else{
            // a tag has been selected, show it
            a.setVisibility(View.VISIBLE);
            a.setVisibility(View.VISIBLE);
            a.setText(tagName);
        }
    }



    /**
     * The user wants to save an edited note
     */
    @Override
    public void saveNote() {
        EditText titleElement = findViewById(R.id.titleInput);
        String title = titleElement.getText().toString();

        if (title.trim().isEmpty()){
            // The title is empty
            int tiempo = Toast.LENGTH_SHORT;
            Toast aviso = Toast.makeText(getApplicationContext(),
                    R.string.failSavingNote_titleEmpty, tiempo);
            aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
            aviso.show();
            return; // Exit method
        }

        EditText bodyElement = findViewById(R.id.noteBody);

        if (bodyElement.getText().toString().trim().isEmpty()){
            // The note body is empty
            int tiempo = Toast.LENGTH_SHORT;
            Toast aviso = Toast.makeText(getApplicationContext(), R.string.failSavingNote_bodyEmpty,
                    tiempo);
            aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
            aviso.show();
            return; // Exit method
        }

        // obtener el texto con estilos
        SpannableString spannable = new SpannableString(bodyElement.getText());

        // texto con estilos a html
        String htmlContent = Html.toHtml(spannable);
        htmlContent = htmlContent.replace("<u>", "") // delete the tags added by the conversor
                                .replace("</u>", "")
                                .replace(" dir=\"ltr\"", "");
        try {
            if (noteId == -1){
                // new note
                // generate random unique filename
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fileName = timeStamp + "_" + ".html";

                OutputStreamWriter fichero = new OutputStreamWriter(openFileOutput(fileName,
                        Context.MODE_PRIVATE));
                fichero.write(htmlContent);
                fichero.close();

                MyDB gestorDB = new MyDB(this, "Notes", null, 1);
                boolean inserted = gestorDB.insertNewNote(title, fileName, chosenTagId,
                        Data.getMyData().getActiveUsername());
                ArrayList<String> noteData = gestorDB.getLastAddedNoteData(fileName);

                if (inserted == false || noteData == null){
                    // database error
                    int tiempo = Toast.LENGTH_SHORT;
                    Toast aviso = Toast.makeText(this, R.string.databaseError, tiempo);
                    aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
                    aviso.show();
                    return; // exit method
                }
                int idNote = Integer.valueOf(noteData.get(0));

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean notifications = prefs.getBoolean("notifications", false);
                if (notifications){
                    // The user wants to be notified
                    // notify with intent
                    NotificationManager elManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this,
                            "newNote");

                    // configure it
                    // max 25 title characters to show
                    String titleToShow = title.substring(0, Math.min(title.length(), 25)) + "...";

                    SharedPreferences prefs_especiales= getSharedPreferences("preferencias_especiales",
                            Context.MODE_PRIVATE);

                    // the initial value is 2 if it's the first time
                    int id = prefs_especiales.getInt("id", 1) + 1;

                    SharedPreferences.Editor editor2= prefs_especiales.edit();
                    // the initial value is 2, but we have to add one to this
                    editor2.putInt("id",id);
                    editor2.apply();

                    // if the user clicks on "read" the note will open
                    Intent i = new Intent(this, SingleNoteActivity.class);
                    i.putExtra("id", id);
                    i.putExtra("noteId", idNote);

                    // limpiamos el stack porque si la abrimos por ejemplo cuando estamos editando
                    // una nota fastidiamos el flow de las actividades, se utiliza el
                    // taskstackbuilder para crear el stack de la actividad
                    PendingIntent intentEnNot =
                            TaskStackBuilder.create(this)
                                    // add all of DetailsActivity's parents to the stack,
                                    // followed by DetailsActivity itself
                                    .addParentStack(SingleNoteActivity.class)
                                    .addNextIntent(i)
                                    .getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);
                                    // el código de petición es diferente para que los intents no
                                    // se sobreescriban y funcionen

                    elBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle(getResources().getString(R.string.notifications_newNote_title))
                            .setContentText(titleToShow)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .addAction(android.R.drawable.ic_menu_view,
                                    getResources().getString(R.string.notifications_newNote_seeNote),
                                    intentEnNot);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel elCanal = new NotificationChannel("newNote",
                                "newNote",
                                NotificationManager.IMPORTANCE_HIGH);
                        elCanal.setDescription("newNote");
                        elCanal.enableLights(true);
                        elCanal.setLightColor(Color.BLUE);
                        elManager.createNotificationChannel(elCanal);
                    }

                    elManager.notify(id, elBuilder.build()); // start notification
                }

                // return
                Intent intent = new Intent();
                intent.putExtra("fileName", fileName);
                setResult(RESULT_OK, intent);
                finish();
            }else{
                // editing existing note
                MyDB gestorDB = new MyDB(getApplicationContext(), "Notes", null, 1);
                String fileName = gestorDB.getNoteFileName(noteId);
                if (fileName != null){
                    OutputStreamWriter fichero = new OutputStreamWriter(openFileOutput(fileName,
                            Context.MODE_PRIVATE));
                    fichero.write(htmlContent);
                    fichero.flush();
                    fichero.close();

                    boolean updated = gestorDB.updateNote(noteId, title, chosenTagId); // update note
                    // in the database
                    if (updated) {
                        // Note updated
                        Intent i = new Intent();
                        i.putExtra("noteId", noteId);
                        setResult(RESULT_OK, i);
                        finish();
                    }else{
                        // database error
                        int tiempo = Toast.LENGTH_SHORT;
                        Toast aviso = Toast.makeText(this
                                , R.string.databaseError, tiempo);
                        aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
                        aviso.show();
                    }
                }else {
                    // database error
                    int tiempo = Toast.LENGTH_SHORT;
                    Toast aviso = Toast.makeText(this
                            , R.string.databaseError, tiempo);
                    aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
                    aviso.show();
                }

            }

        } catch (IOException e){
            // show toast if error saving the file
            int tiempo = Toast.LENGTH_SHORT;
            Toast aviso = Toast.makeText(getApplicationContext(), R.string.failSavingNote, tiempo);
            aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
            aviso.show();
        }
    }


    @Override
    public void onBackPressed() {
        // If the back button is pressed the user has to confirm they want to exit
        DialogFragment confirmationDialog = new ConfimExit();
        confirmationDialog.show(getSupportFragmentManager(), "goBack");
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        chosenTagId = savedInstanceState.getInt("chosenTagId");
        chosenTagName = savedInstanceState.getString("chosenTagName");
        addTagToPost(chosenTagId, chosenTagName); // show the tag if there's one like before
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("chosenTagId", chosenTagId);
        outState.putString("chosenTagName", chosenTagName);
        outState.putInt("noteId", noteId);
    }
}
