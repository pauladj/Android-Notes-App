package com.example.proyecto1.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.proyecto1.R;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MyDB extends SQLiteOpenHelper {

    public MyDB(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table Users
        db.execSQL("CREATE TABLE Users ('username' CHAR(255) PRIMARY KEY NOT NULL, 'password' " +
                "CHAR(255) NOT NULL, 'active' BIT DEFAULT 0)");

        // Create table Tags
        db.execSQL("CREATE TABLE Tags ('id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                " 'name' CHAR(255) NOT NULL, 'username' INTEGER, FOREIGN KEY('username') "+
                " REFERENCES Users('username') ON DELETE CASCADE)");

        // Create table Notes
        db.execSQL("CREATE TABLE Notes ('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'title' CHAR(255) NOT NULL, 'fileContent' CHAR(255) NOT NULL UNIQUE, 'date' " +
                "DATETIME " +
                "NOT NULL DEFAULT CURRENT_TIMESTAMP, 'labelId' INTEGER, 'username' CHAR(255)" +
                ", " +
                "FOREIGN KEY('labelId') REFERENCES Tags('id') ON DELETE SET NULL, " +
                "FOREIGN KEY('username') REFERENCES Users('username') ON DELETE CASCADE)");

        // Insert dummy data
        db.execSQL("INSERT INTO Users VALUES ('admin', '1111', 1)");
        db.execSQL("INSERT INTO Tags(id, name, username) VALUES (1, 'tagPrueba', 'admin')");
        db.execSQL("INSERT INTO Tags(id, name, username) VALUES (2, 'tagPrueba2', 'admin')");
        db.execSQL("INSERT INTO Tags(id, name, username) VALUES (3, 'tagPrueba3', 'admin')");
        db.execSQL("INSERT INTO Notes(fileContent, title, username) VALUES ('nombrefichero.html'," +
                " 'sergsehhhhhhhhhhhhrg r gsdfg ', 'admin')");
        db.execSQL("INSERT INTO Notes(fileContent, labelId, title, username) VALUES " +
                "('nombrefichero2.html', 1," +
                " 'sergserg r gsdfg ', 'admin')");
        db.execSQL("INSERT INTO Notes(fileContent, labelId, title, username) VALUES ('fff', 1, 'klsdfjkldf ksdjfksjdfks jdfksjdfksd fjkdfj f skdf jskjdf df kdfjskld fjkd jkdjf kdfj dfklf', 'admin')");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        // Delete the existing tables
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Tags");
        db.execSQL("DROP TABLE IF EXISTS Notes");

        // Create the tables again
        onCreate(db);
    }

    /**
     * Check if a username exists in database
     * @param username
     * @return True - exists, False - does not exist
     */
    public Boolean checkIfUsernameExists(String username){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT username FROM Users WHERE username='" + username + "'", null);
        boolean exists = false;
        if (c.moveToNext() != false) {
            // there is a user with these data
            exists = true;
        }
        c.close();
        db.close();
        return exists;
    }

    /**
     * Checks if a username exists with that password
     * @param username
     * @param password
     * @return True or False
     */
    public Boolean checkIfUserCanBeLoggedIn(String username, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT username FROM Users WHERE username='" + username + "' AND password='"+password + "'", null);
        boolean exists = false;
        if (c.moveToNext() != false) {
            // there is a user with these data
            exists = true;
        }
        c.close();
        db.close();
        return exists;
    }

    /**
     * Sets the username as active
     * @param username
     */
    public void setUsernameAsActive(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues modification = new ContentValues();
        modification.put("active", 1);
        db.update("Users", modification, "username='" + username + "'", null);
        db.close();
    }

    /**
     * Gets the active username if there's one
     * @return the active username or null
     */
    public String getActiveUsername(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT username FROM Users WHERE active=1", null);

        String username = null;
        if (c.moveToNext() != false) {
            // there is a user with these data
            username = c.getString(0);
        }
        c.close();
        db.close();
        return username;
    }

    /**
     * Get notes data to show on the main screen
     * @param username - of which we have to get the notes
     * @return ids, titles, dates and tags of the notes
     */
    public ArrayList<ArrayList<String>> getNotesDataByUser(String username){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT Notes.id, title, date, name FROM Notes LEFT JOIN Tags ON " +
                "Notes.labelId=Tags.id WHERE Notes.username='" + username + "'", null);

        ArrayList<String> notesIds = new ArrayList<>();
        ArrayList<String> notesTitles = new ArrayList<>();
        ArrayList<String> notesDates = new ArrayList<>();
        ArrayList<String> notesTagsNames = new ArrayList<>();

        while (c.moveToNext()) {
            // there is a user with these data
            String id = c.getString(0);
            String title = c.getString(1);
            String date = c.getString(2);
            String tagName = null;
            if (c.getColumnCount() == 4){
                tagName = c.getString(3);
            }

            notesIds.add(id);
            notesTitles.add(title);
            notesDates.add(date);
            notesTagsNames.add(tagName);
        }
        c.close();
        db.close();

        ArrayList<ArrayList<String>> notesData = new ArrayList<>();
        notesData.add(notesIds);
        notesData.add(notesTitles);
        notesData.add(notesDates);
        notesData.add(notesTagsNames);

        return notesData;
    }

    /**
     * Get a note filename where the content is saved
     * @param noteId
     * @return the filename of the note
     */
    public String getNoteFileName(int noteId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =
                db.rawQuery("SELECT fileContent FROM Notes WHERE id=" + String.valueOf(noteId),
                        null);

        String fileName = null;
        if (c.moveToNext() != false) {
            // there is a note with this data
            fileName = c.getString(0);
        }
        c.close();
        db.close();
        return fileName;
    }

    /**
     * Delete a note by knowing the id
     * @param noteId - the id of the note to delete
     */
    public void deleteANote(int noteId){
        SQLiteDatabase db = getWritableDatabase();
        db.delete("Notes", "id=" + noteId, null);
        db.close();
    }

    /**
     * Get tags by user
     * @param username - of which we have to get the notes
     * @return ids, names of the tags
     */
    public ArrayList<ArrayList<String>> getTagsByUser(String username){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name FROM Tags WHERE username='" + username +
                "'", null);

        ArrayList<String> tagsIds = new ArrayList<>();
        ArrayList<String> tagsNames = new ArrayList<>();

        while (c.moveToNext()) {
            // there is a user with these data
            String id = c.getString(0);
            String tagName = c.getString(1);

            tagsIds.add(id);
            tagsNames.add(tagName);
        }
        c.close();
        db.close();

        ArrayList<ArrayList<String>> data = new ArrayList<>();
        data.add(tagsIds);
        data.add(tagsNames);

        return data;
    }

    /**
     * Add new tag for user
     * @param username - the user that the new tag belongs to
     * @param nameTag - the name of the new tag
     */
    public void addTag(String username, String nameTag){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO Tags ('name', 'username') VALUES ('" + nameTag + "', '"+ username +
                "')");
        db.close();
    }

    /**
     * Insert new note
     * @param title - title of the new note
     * @param fileContent - the filename where the new note content is
     * @param labelId - the label id
     * @param username - the user who has created the note
     */
    public void insertNewNote(String title, String fileContent, int labelId, String username){
        SQLiteDatabase db = this.getWritableDatabase();
        String label = String.valueOf(labelId);
        if (labelId == -1) {
            label = null;
        }
        db.execSQL("INSERT INTO Notes ('title', 'fileContent', 'labelId', 'username') VALUES ('" + title +
                "', '"+ fileContent +"', "+ label +", '"+ username +"')");

        db.close();

    }

    /**
     * Get last inserted note data knowing the filename (it's unique)
     * @return - list with id, title, date, tag
     */
    public ArrayList<String> getLastAddedNoteData(String fileName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =
                db.rawQuery("SELECT Notes.id, title, date, name FROM Notes LEFT JOIN Tags ON " +
                        "Notes.labelId=Tags.id WHERE Notes.fileContent='" + fileName + "'", null);


        ArrayList<String> data = new ArrayList<>();

        if (c.moveToFirst()){
            do {
                String id = c.getString(0);
                String title = c.getString(1);
                String date = c.getString(2);
                String tagName = null;
                if (c.getColumnCount() == 4){
                    tagName = c.getString(3);
                }

                data.add(id);
                data.add(title);
                data.add(date);
                data.add(tagName);
            }while(c.moveToNext());
        }

        c.close();
        db.close();

        return data;
    }
}
