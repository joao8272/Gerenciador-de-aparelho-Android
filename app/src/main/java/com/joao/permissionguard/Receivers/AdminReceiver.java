package com.joao.permissionguard.Receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Permission Guard ativado como administrador", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "Permission Guard desativado como administrador", Toast.LENGTH_SHORT).show();
    }
}
