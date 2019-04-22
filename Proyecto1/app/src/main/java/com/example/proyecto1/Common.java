package com.example.proyecto1;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.example.proyecto1.dialogs.DateDialog;
import com.example.proyecto1.fragments.AsyncTaskFragment;

public class Common extends LanguageActivity  implements AsyncTaskFragment.TaskCallbacks  {

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private AsyncTaskFragment mTaskFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fragmento que contiene la tarea asíncrona
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (AsyncTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // El fragmento solo es null cuando la actividad se crea por primera vez, cuando se rota
        // el fragmento se mantiene
        if (mTaskFragment == null) {
            mTaskFragment = new AsyncTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }

    /**
     * Show a toast in the view
     * @param acrossWindows - if true the toast does not disappear when view changes
     * @param messageId - the message id to show
     */
    public void showToast(Boolean acrossWindows, int messageId){
        int tiempo = Toast.LENGTH_SHORT;
        Context context;
        if (acrossWindows){
            context = getApplicationContext();
        }else{
            context = this;
        }
        Toast aviso = Toast.makeText(context, getResources().getString(messageId), tiempo);
        aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
        aviso.show();
    }

    /**
     * Get the active username
     * @param - the active username (token)
     */
    public String getActiveUsername(){
        SharedPreferences prefs_especiales= getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        return prefs_especiales.getString("activeUsername", null);
    }

    /**
     * Set the active username
     * @param username - the active username
     */
    public void setActiveUsername(String username){
        SharedPreferences prefs_especiales= getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor2= prefs_especiales.edit();
        editor2.putString("activeUsername", username);
        editor2.apply();
    }

    /**
     * Get the fragment containing the asynctask
     * @return - The async task fragment
     */
    public AsyncTaskFragment getmTaskFragment(){
        return mTaskFragment;
    }

    /**
     * Add a self note to the recycler view
     * @param message - the text of the message
     * @param imagePath - the image path
     * @param date - the date of the message
     */
    public void addSelfNoteToRecycler(String message, String imagePath, String date){}

}
