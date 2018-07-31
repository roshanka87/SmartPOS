package com.justtide.osapp.util;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.example.admin.smartpos.R;

public class DialogService {
    private static Context context;
    private static AlertDialog progressDialog;

    public static void progressDialog(Context context, boolean show) {
        if (progressDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setTitle("Please wait...");
            builder.setMessage("Checking connection.");
            builder.setIcon(R.drawable.ic_icons8_spinner_24);
            progressDialog = builder.create();
        }
        if (!progressDialog.isShowing() && show) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }
}
