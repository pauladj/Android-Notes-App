package com.example.proyecto1.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

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

        // Create table Tags
        db.execSQL("CREATE TABLE Tags ('id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                " 'name' CHAR(255) NOT NULL, 'username' INTEGER)");

        // Create table Notes
        db.execSQL("CREATE TABLE Notes ('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'title' CHAR(255) NOT NULL, 'fileContent' CHAR(255) NOT NULL UNIQUE, 'date' " +
                "DATETIME " +
                "NOT NULL DEFAULT (datetime('now','localtime')), 'labelId' INTEGER, 'username' CHAR(255)" +
                ", 'lat' CHAR(255), 'lg' CHAR(255)," +
                "FOREIGN KEY('labelId') REFERENCES Tags('id') ON DELETE SET NULL)");

        // Create table SelfNotes
        db.execSQL("CREATE TABLE SelfNotes ('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'message' CHAR(255), 'imagePath' CHAR(255), 'date' DATETIME " +
                "NOT NULL, 'username' CHAR(255))");


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
     * Gets the active username if there's one
     * @return the active username or null
     */
    public String getActiveUsername(){
        SQLiteDatabase db = null;
        Cursor c = null;
        String username = null;
        try {
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT username FROM Users WHERE active=1", null);

            if (c.moveToNext()) {
                // there is a user with these data
                username = c.getString(0);
            }
        }catch (SQLException e){
            //
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return username;
    }

    /**
     * Get notes data to show on the main screen
     * @param username - of which we have to get the notes
     * @return ids, titles, dates and tags of the notes
     */
    public ArrayList<ArrayList<String>> getNotesDataByUser(String username){
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<ArrayList<String>> notesData = null;

        try {
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT Notes.id, title, date, name FROM Notes LEFT JOIN Tags ON " +
                            "Notes.labelId=Tags.id WHERE Notes.username='" + username + "' ORDER BY date ASC",
                    null);

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
            notesData = new ArrayList<>();
            notesData.add(notesIds);
            notesData.add(notesTitles);
            notesData.add(notesDates);
            notesData.add(notesTagsNames);
        }catch (SQLException e){
            //
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return notesData;
    }

    /**
     * Get notes data to show on the main screen
     * @param username - of which we have to get the notes
     * @return ids, titles, dates and tags of the notes
     */
    public ArrayList<ArrayList<String>> getMapPositionsByUser(String username){
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<ArrayList<String>> notesData = null;

        try {
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT title, lat, lg FROM Notes WHERE username='" + username + "' " +
                            "AND lat IS NOT NULL",
                    null);

            ArrayList<String> notesLat = new ArrayList<>();
            ArrayList<String> notesLg = new ArrayList<>();
            ArrayList<String> notesTitles = new ArrayList<>();

            while (c.moveToNext()) {
                // there is a note with these data
                String title = c.getString(0);
                String lat = c.getString(1);
                String lg = c.getString(2);

                notesLat.add(lat);
                notesTitles.add(title);
                notesLg.add(lg);
            }
            notesData = new ArrayList<>();
            notesData.add(notesTitles);
            notesData.add(notesLat);
            notesData.add(notesLg);
        }catch (SQLException e){
            //
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return notesData;
    }

    /**
     * Get self notes data to show on the screen
     * @param username - of which we have to get the notes
     * @return text, date, image of each note
     */
    public ArrayList<ArrayList<String>> getSelfNotesByUser(String username){
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<ArrayList<String>> notesData = null;

        try {
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT message, imagePath, date FROM" +
                            " SelfNotes WHERE username='" + username + "' ORDER BY " +
                            "date ASC",
                    null);

            ArrayList<String> noteMessages = new ArrayList<>();
            ArrayList<String> noteImages = new ArrayList<>();
            ArrayList<String> noteDates = new ArrayList<>();

            while (c.moveToNext()) {
                // there is a user with these data
                String message = c.getString(0);
                String image = c.getString(1);
                if (message.equals("null")){
                    message = null;
                }

                if (image.isEmpty() || image.equals("null")){
                    image = null;
                }
                String date = c.getString(2);

                noteMessages.add(message);
                noteImages.add(image);
                noteDates.add(date);
            }
            notesData = new ArrayList<>();
            notesData.add(noteMessages);
            notesData.add(noteDates);
            notesData.add(noteImages);
        }catch (SQLException e){
            //
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return notesData;
    }


    /**
     * Add self note
     * @param username - the user who wrote it
     * @param message - the message, this could be null
     * @param imagePath - the image local path, this could be null
     * @param date- the date of the self note
     * @param database - si se quiere utilizar una transacción
     * @return - if the data has been added
     */
    public boolean addSelfNote(String username, String message, String imagePath, String date,
                               SQLiteDatabase database){
        SQLiteDatabase db = null;

        boolean changed;
        try {
            if (database != null){
                // se quiere empezar una transacción
                db = database;
            }else {
                db = this.getWritableDatabase();
            }

            db.execSQL("INSERT INTO SelfNotes ('message', 'imagePath', 'date', 'username') VALUES" +
                    " ('" + message + "', '" + imagePath + "', '" + date + "', '" + username +
                    "')");
            changed = true;
        }catch (SQLException e){
            changed = false;
        }finally {
            if (db != null && database == null){
                // si no es una transacción
                db.close();
            }
        }
        return changed;
    }


    /**
     * Get a note filename where the content is saved
     * @param noteId
     * @return the filename of the note
     */
    public String getNoteFileName(int noteId){
        SQLiteDatabase db = null;
        Cursor c = null;
        String fileName = null;
        try {
            db = this.getReadableDatabase();

            c =
                    db.rawQuery("SELECT fileContent FROM Notes WHERE id=" + String.valueOf(noteId),
                            null);
            if (c.moveToNext()) {
                // there is a note with this data
                fileName = c.getString(0);
            }
        }catch (SQLException e){
            //
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return fileName;
    }

    /**
     * Update note after editing it
     * @param noteId - the id of the note to update
     * @param title - the new title of the note
     * @param chosenTagId - the chosen tag id
     * @return true if the note has been updated
     */
    public boolean updateNote(int noteId, String title, int chosenTagId){
        SQLiteDatabase db = null;
        boolean changed;
        try {
            db = this.getWritableDatabase();
            ContentValues modification = new ContentValues();
            modification.put("title", title);
            modification.put("labelId", chosenTagId);
            db.update("Notes", modification, "id=" + noteId, null);
            changed = true;
        }catch (SQLException e){
            changed = false;
        }finally {
            if (db != null){
                db.close();
            }
        }
        return changed;
    }

    /**
     * Update latitude and longitude fields of a note
     * @param lat - latitud
     * @param lg - longitud
     * @return true if the note has been updated
     */
    public boolean updateLatitudeLongitude(int noteId, String lat,
                                           String lg){
        SQLiteDatabase db = null;
        boolean changed;
        try {
            db = this.getWritableDatabase();
            ContentValues modification = new ContentValues();
            modification.put("lat", lat);
            modification.put("lg", lg);
            db.update("Notes", modification, "id=" + noteId, null);
            changed = true;
        }catch (SQLException e){
            changed = false;
        }finally {
            if (db != null){
                db.close();
            }
        }
        return changed;
    }


    /**
     * Get a note data so the user can see it on the editor
     * @param noteId
     * @return
     */
    public String[] getNoteData(int noteId){
        String[] data = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = this.getReadableDatabase();
            c =
                    db.rawQuery("SELECT title, fileContent, name, Tags.id, date FROM Notes LEFT JOIN " +
                                    "Tags " +
                                    "ON " +
                                    "Notes.labelId=Tags.id WHERE Notes.id=" + String.valueOf(noteId),
                            null);

            if (c.moveToNext()) {
                data = new String[5];
                // there is a note with this data
                data[0] = c.getString(0); // note title
                data[1] = c.getString(1); // note fileContent
                data[2] = null;
                data[3] = null;
                if (c.getColumnCount() == 5){
                    // there's a tag
                    data[2] = c.getString(2); // tag name
                    data[3] = c.getString(3); // tag id
                }
                data[4] = c.getString(4); // date
            }
        }catch (SQLException e){
            data = null;
        }finally{
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return data;
    }

    /**
     * Delete a note by knowing the id
     * @param noteId - the id of the note to delete
     * @return true, the note has been successfully deleted
     */
    public boolean deleteANote(int noteId){
        SQLiteDatabase db = null;
        boolean result;
        try {
            db = getWritableDatabase();
            db.delete("Notes", "id=" + noteId, null);
            result = true;
        }catch (SQLException e){
            result = false;
        }finally {
            if (db != null){
                db.close();
            }
        }
        return result;
    }

    /**
     * Get tags by user
     * @param username - of which we have to get the notes
     * @return ids, names of the tags
     */
    public ArrayList<ArrayList<String>> getTagsByUser(String username){
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<ArrayList<String>> data = new ArrayList<>();

        try {
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT id, name FROM Tags WHERE username='" + username +
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

            data.add(tagsIds);
            data.add(tagsNames);
        }catch(SQLException e){
            data = null;
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return data;
    }

    /**
     * Add new tag for user
     * @param username - the user that the new tag belongs to
     * @param nameTag - the name of the new tag
     * @return true if the tag has been added
     */
    public boolean addTag(String username, String nameTag){
        SQLiteDatabase db = null;
        boolean changed;
        try {
            db = this.getWritableDatabase();

            db.execSQL("INSERT INTO Tags ('name', 'username') VALUES ('" + nameTag + "', '" + username +
                    "')");
            changed = true;
        }catch (SQLException e){
            changed = false;
        }finally {
            if (db != null){
                db.close();
            }
        }
        return changed;
    }

    /**
     * Check if the tag exists
     * @param username - the user
     * @param nameTag - the name of the tag
     * @return - True if the user has this tag, false if he doesn't
     */
    public boolean tagExists(String username, String nameTag){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =
                db.rawQuery("SELECT id FROM Tags WHERE name='"+ nameTag+"' AND username='" + username + "'", null);
        boolean exists = false;
        if (c.moveToNext()){
            // the tag exists
            exists = true;
        }
        db.close();
        return exists;
    }

    /**
     * Remove tag by id
     * @param tagId - the id of the tag to delete
     * @return - list of post id with that tag
     */
    public ArrayList<Integer> removeTag(int tagId){
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<Integer> tagsIds = new ArrayList<>();

        try {
            db = this.getWritableDatabase();
            c = db.rawQuery("SELECT id FROM Notes WHERE labelId=" + tagId, null);

            while (c.moveToNext()) {
                // there is a note with this tag id
                int id = c.getInt(0);
                tagsIds.add(id);
            }
            db.execSQL("DELETE FROM Tags WHERE id=" + tagId);
        }catch (SQLException e){
            tagsIds = null;
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return tagsIds;
    }

    /**
     * Insert new note
     * @param title - title of the new note
     * @param fileContent - the filename where the new note content is
     * @param labelId - the label id
     * @param username - the user who has created the note
     * @return true if it has been inserted
     */
    public boolean insertNewNote(String title, String fileContent, int labelId, String username){
        SQLiteDatabase db = null;
        boolean added;
        try {
            db = this.getWritableDatabase();
            String label = String.valueOf(labelId);
            if (labelId == -1) {
                label = null;
            }
            db.execSQL("INSERT INTO Notes ('title', 'fileContent', 'labelId', 'username') VALUES ('" + title +
                    "', '" + fileContent + "', " + label + ", '" + username + "')");
            added = true;
        }catch (SQLException e){
            added = false;
        }finally {
            if (db != null){
                db.close();
            }
        }
        return added;
    }

    /**
     * Get last inserted note data knowing the filename (it's unique)
     * @return - list with id, title, date, tag
     */
    public ArrayList<String> getLastAddedNoteData(String fileName) {
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<String> data;
        try{
            db = this.getReadableDatabase();
            c =
                    db.rawQuery("SELECT Notes.id, title, date, name FROM Notes LEFT JOIN Tags ON " +
                            "Notes.labelId=Tags.id WHERE Notes.fileContent='" + fileName + "'", null);


            data = new ArrayList<>();

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
        }catch (SQLException e){
            data = null;
        }finally {
            if (c != null) {
                c.close();
            }
            if (db != null){
                db.close();
            }
        }
        return data;
    }
}
