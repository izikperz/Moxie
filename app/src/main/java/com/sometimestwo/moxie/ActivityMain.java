package com.sometimestwo.moxie;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/*
        TODOS:
            - double tap to upvote
            - view pager
            - exoplayer releases
            - crash after token expires
            settings options:
            - browse mode (no comments, upvoting , etc) for lurkers
            - hide progress bar on exoplayer
            -NSFW icon
            - play gif icon
            - When large previewer fails to load, loads previously successful image instead
            -remove item from recycler
            - progress bar time out when load fails

            layout issues:
            - centering very tall image in large hover
            - can open drawer layout while large hover previewing
            - exo player not reloading when tabbing back into app
            - create truncated title for long titles. To be useful in previewers
            - Go to Subreddit edit text box is too short (height wise) for small screen

            names:
            Unfold

            Explore blue color code: 33B5E5
 */
public class ActivityMain extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private SharedPreferences prefs_settings;
    private SharedPreferences.Editor prefs_settings_editor;
    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.drawer_layout);
        setContentView(R.layout.activity_main);

        /* Set default settings/preferences if never initialized.
         *
         * This is more of a precautionary mesaure to ensure we don't find null preferences later.
         */
        prefs_settings = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        prefs_settings_editor =
                this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE)
                        .edit();

        // Hover previewer size - defaults to small
        if (prefs_settings.getString(Constants.SETTINGS_PREVIEW_SIZE, null) == null) {
            prefs_settings_editor.putString(Constants.SETTINGS_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_LARGE);
        }


        // Allow NSFW
        if (prefs_settings.getBoolean(Constants.SETTINGS_HIDE_NSFW, true) == true) {
            prefs_settings_editor.putBoolean(Constants.SETTINGS_HIDE_NSFW, true);
        }

        // Hide NSFW thumbs
        if (prefs_settings.getBoolean(Constants.SETTINGS_HIDE_NSFW_THUMBS, false) == false) {
            prefs_settings_editor.putBoolean(Constants.SETTINGS_HIDE_NSFW_THUMBS, false);
        }

        // Allow hover previewer - default to yes
        if (prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_HOVER_PREVIEW, true) == true) {
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_HOVER_PREVIEW, true);
        }

        // Allow tap-to-close big display
        if (prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false) == false) {
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
        }

        // Allow domain icons - defaults to false
        if (prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_DOMAIN_ICON, false) == false) {
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_DOMAIN_ICON, false);
        }

        // Allow filetype icons - defaults to false
        if (prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_FILETYPE_ICON, false) == false) {
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_FILETYPE_ICON, false);
        }

        // Show NSFW icon on NSFW submissions
        if (prefs_settings.getBoolean(Constants.SETTINGS_SHOW_NSFW_ICON, false) == false) {
            prefs_settings_editor.putBoolean(Constants.SETTINGS_SHOW_NSFW_ICON, false);
        }

        prefs_settings_editor.commit();

        new FetchRedditUser(this).execute();
    }


    private  boolean checkPermissions() {
      /*  int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p: permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }*/
        return true;
    }

    @Override
    protected void onResume() {
        // Example of how to check for internet. Not using now as we are
        // leaving SubmissionsDataSource to handle 404 page
/*        new Utils.InternetCheck(internet -> {
            boolean a = internet;
            System.out.println(a);
        });*/
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class FetchRedditUser extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<ActivityMain> activity;

        public FetchRedditUser(ActivityMain activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // Load most recently logged in user
            String mostRecentUser = prefs_settings.getString(Constants.MOST_RECENT_USER, Constants.USERNAME_USERLESS);

            if (!Constants.USERNAME_USERLESS.equalsIgnoreCase(mostRecentUser)) {
                App.getAccountHelper().switchToUser(mostRecentUser);
            }
            else {
                App.getAccountHelper().switchToUserless();
            }

            // TODO CRASH HERE ON TAB BACK IN:
            // Caused by: java.lang.IllegalStateException: No unexpired OAuthData or refresh token available for user '<userless>'
            // check authentication stuff and redo if necessary
            // https://mattbdean.gitbooks.io/jraw/content/v/v1.1.0/oauth2.html


            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            Activity activity = this.activity.get();
            if (activity != null) {
                if(checkPermissions()) {
                    activity.startActivity(new Intent(activity, ActivityHome.class));
                }
            }
        }
    }
}
