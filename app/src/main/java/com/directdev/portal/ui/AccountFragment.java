package com.directdev.portal.ui;


import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.crashlytics.android.Crashlytics;
import com.directdev.portal.R;
import com.directdev.portal.tools.event.AccountResponseEvent;
import com.directdev.portal.tools.event.DataUpdateEvent;
import com.directdev.portal.tools.event.GpaResponseEvent;
import com.directdev.portal.tools.event.PhotoResponseEvent;
import com.directdev.portal.tools.event.ThereIsNewPhotoEvent;
import com.directdev.portal.tools.fetcher.FetchAccountData;
import com.directdev.portal.ui.access.LogoutAuthorization;
import com.directdev.portal.ui.finance.FinanceActivity;
import com.directdev.portal.ui.grades.GradesActivity;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        EventBus.getDefault().register(this);
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

        gpa.setOnClickListener(this);
        finance.setOnClickListener(this);
        forum.setOnClickListener(this);
        grades.setOnClickListener(this);
        logout.setOnClickListener(this);
        settings.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ImageView photo = (ImageView) view.findViewById(R.id.profile_image);
        TextView gpa = (TextView) view.findViewById(R.id.gpa_number);

        if (!settingsPref.getBoolean(getString(R.string.setting_photo), false)) {
            byte[] toDecode = new byte[700000];
            try {
                FileInputStream fis = getActivity().openFileInput(getString(R.string.resource_photo));
                fis.read(toDecode, 0, toDecode.length);
                fis.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo:
                break;
            case R.id.open_finance:
                Intent financeIntent = new Intent(getActivity(), FinanceActivity.class);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().startActivity(financeIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(financeIntent);
                }
                break;
            case R.id.open_forum:
                Toast toast = Toast.makeText(getActivity(), "Forum is under reconsideration", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case R.id.open_grades:
                Intent gradesIntent = new Intent(getActivity(), GradesActivity.class);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().startActivity(gradesIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(gradesIntent);
                }
                break;
            case R.id.open_logout:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
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
            case R.id.open_settings:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().startActivity(settingsIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(settingsIntent);
                }
                break;
        }
    }

    public void onEvent(ThereIsNewPhotoEvent event) {
        FetchAccountData fetcher = new FetchAccountData(getActivity());
        fetcher.requestPhoto();
    }

    public void onEvent(DataUpdateEvent event) {
        TextView last_update = (TextView) getActivity().findViewById(R.id.last_updated);
        last_update.setText("Last updated: " + sPref.getString(getString(R.string.last_update_pref), "2012-12-21 12:12"));
    }

    public void onEvent(GpaResponseEvent event) {
        if (!settingsPref.getBoolean(getString(R.string.setting_gpa), false)) {
            TextView gpa = (TextView) getActivity().findViewById(R.id.gpa_number);
            gpa.setText(sPref.getString(getString(R.string.resource_gpa), ""));
        }
    }

    public void onEvent(AccountResponseEvent event) {
        TextView name = (TextView) getActivity().findViewById(R.id.account_name);
        TextView major = (TextView) getActivity().findViewById(R.id.major);

        name.setText(sPref.getString(getString(R.string.resource_account_name), ""));
        major.setText(sPref.getString(getString(R.string.resource_major), ""));
    }

    public void onEvent(PhotoResponseEvent event) {
        if (!settingsPref.getBoolean(getString(R.string.setting_photo), false)) {
            byte[] toDecode = new byte[700000];
            try {
                FileInputStream fis = getActivity().openFileInput(getString(R.string.resource_photo));
                fis.read(toDecode, 0, toDecode.length);
                fis.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
            ImageView photo = (ImageView) getActivity().findViewById(R.id.profile_image);
            bitmap = decodeSampledBitmapFromResource(toDecode, 74, 74);
            photo.setImageBitmap(bitmap);
        }
    }
}
