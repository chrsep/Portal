package com.directdev.portal.ui.main.account;


import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directdev.portal.R;
import com.directdev.portal.tools.event.UpdateFinishEvent;
import com.directdev.portal.tools.services.UpdateService;
import com.directdev.portal.ui.access.LogoutAuthorization;
import com.directdev.portal.ui.main.account.finance.FinanceActivity;
import com.directdev.portal.ui.main.account.grades.GradesActivity;

import java.io.FileInputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;


public class AccountFragment extends Fragment implements View.OnClickListener {
    private SharedPreferences sPref;
    private SharedPreferences settingsPref;
    private Bitmap bitmap;
    private View view;


    public AccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);
        if (sPref.getString(getString(R.string.resource_gpa), null) != null) {
            TextView name = (TextView) view.findViewById(R.id.account_name);
            TextView major = (TextView) view.findViewById(R.id.major);
            TextView last_update = (TextView) view.findViewById(R.id.last_updated);

            name.setText(sPref.getString(getString(R.string.resource_account_name), ""));
            major.setText(sPref.getString(getString(R.string.resource_major), ""));
            last_update.setText("Last updated: " + sPref.getString(getString(R.string.last_update_pref), ""));
        }

        RelativeLayout gpa = (RelativeLayout) view.findViewById(R.id.photo);
        RelativeLayout finance = (RelativeLayout) view.findViewById(R.id.open_finance);
        RelativeLayout forum = (RelativeLayout) view.findViewById(R.id.open_forum);
        RelativeLayout grades = (RelativeLayout) view.findViewById(R.id.open_grades);
        RelativeLayout logout = (RelativeLayout) view.findViewById(R.id.open_logout);
        RelativeLayout settings = (RelativeLayout) view.findViewById(R.id.open_settings);
        RelativeLayout knownBug = (RelativeLayout) view.findViewById(R.id.open_known_bugs);

        gpa.setOnClickListener(this);
        finance.setOnClickListener(this);
        forum.setOnClickListener(this);
        grades.setOnClickListener(this);
        logout.setOnClickListener(this);
        settings.setOnClickListener(this);
        knownBug.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onResume() {
        ImageView photo = (ImageView) view.findViewById(R.id.profile_image);
        TextView gpa = (TextView) view.findViewById(R.id.gpa_number);
        TextView name = (TextView) view.findViewById(R.id.account_name);
        TextView major = (TextView) view.findViewById(R.id.major);

        if (!settingsPref.getBoolean(getString(R.string.setting_photo), false)) {
            byte[] toDecode = new byte[700000];
            try {
                FileInputStream fis = getActivity().openFileInput(getString(R.string.resource_photo));
                fis.read(toDecode, 0, toDecode.length);
                fis.close();
            } catch (IOException e) {

            }
            bitmap = decodeSampledBitmapFromResource(toDecode, 74, 74);
            if (bitmap != null) {
                photo.setImageBitmap(bitmap);
            }
        } else {
            photo.setImageResource(R.mipmap.ic_launcher);
        }

        if (!settingsPref.getBoolean(getString(R.string.setting_gpa), false)) {
            gpa.setText(sPref.getString(getString(R.string.resource_gpa), ""));
        } else {
            gpa.setText("4.0");
        }
        
        name.setText(sPref.getString(getString(R.string.resource_account_name), ""));
        major.setText(sPref.getString(getString(R.string.resource_major), ""));
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo:
                break;
            case R.id.open_finance:
                Intent financeIntent = new Intent(getActivity(), FinanceActivity.class);
                launchIntent(financeIntent);
                break;
            case R.id.open_forum:
                Toast toast = Toast.makeText(getActivity(), "Forum is under reconsideration", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case R.id.open_grades:
                Intent gradesIntent = new Intent(getActivity(), GradesActivity.class);
                launchIntent(gradesIntent);
                break;
            case R.id.open_settings:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                launchIntent(settingsIntent);
                break;
            case R.id.open_known_bugs:
                try {
                    Intent bugsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/njyqjb"));
                    startActivity(bugsIntent);
                }catch (ActivityNotFoundException e){
                    Toast noBrowser = Toast.makeText(getActivity(), "Failed to open browser", Toast.LENGTH_SHORT);
                    noBrowser.show();
                }
                break;
            case R.id.open_logout:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                getActivity().stopService(new Intent(getActivity(), UpdateService.class));
                                Intent intent = new Intent(getActivity(), LogoutAuthorization.class);
                                startActivity(intent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
                break;
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(UpdateFinishEvent event) {
        TextView last_update = (TextView) getActivity().findViewById(R.id.last_updated);
        last_update.setText("Last updated: " + sPref.getString(getString(R.string.last_update_pref), "2012-12-21 12:12"));

        if (!settingsPref.getBoolean(getString(R.string.setting_gpa), false)) {
            TextView gpa = (TextView) getActivity().findViewById(R.id.gpa_number);
            gpa.setText(sPref.getString(getString(R.string.resource_gpa), ""));
        }

        TextView name = (TextView) getActivity().findViewById(R.id.account_name);
        TextView major = (TextView) getActivity().findViewById(R.id.major);

        name.setText(sPref.getString(getString(R.string.resource_account_name), ""));
        major.setText(sPref.getString(getString(R.string.resource_major), ""));

        if (!settingsPref.getBoolean(getString(R.string.setting_photo), false)) {
            byte[] toDecode = new byte[700000];
            try {
                FileInputStream fis = getActivity().openFileInput(getString(R.string.resource_photo));
                fis.read(toDecode, 0, toDecode.length);
                fis.close();
            } catch (IOException e) {

            }
            ImageView photo = (ImageView) getActivity().findViewById(R.id.profile_image);
            bitmap = decodeSampledBitmapFromResource(toDecode, 74, 74);
            photo.setImageBitmap(bitmap);
        }
    }


    private void launchIntent(Intent intent){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        } else {
            startActivity(intent);
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(byte[] toDecode,
                                                         int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(toDecode, 0, toDecode.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(toDecode, 0, toDecode.length, options);
    }
}
