package com.joao.permissionguard.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.joao.permissionguard.UI.LockActivity;

public class GuardAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            return;

        CharSequence pkg = event.getPackageName();
        if (pkg == null) return;

        String currentPackage = pkg.toString();

        if (isLauncher(currentPackage)) {
            getSharedPreferences("lock_prefs", MODE_PRIVATE)
                    .edit()
                    .remove("unlocked_app")
                    .apply();
            return;
        }

        String unlockedApp = getSharedPreferences("lock_prefs", MODE_PRIVATE)
                .getString("unlocked_app", null);

        // Mudou de app → remove desbloqueio
        if (unlockedApp != null && !currentPackage.equals(unlockedApp)) {
            getSharedPreferences("lock_prefs", MODE_PRIVATE)
                    .edit()
                    .remove("unlocked_app")
                    .apply();
            unlockedApp = null;
        }

        if (isBlocked(currentPackage) && unlockedApp == null) {

            if (!LockActivity.isLockActivityOpen) {
                Intent i = new Intent(this, LockActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("packageToUnlock", currentPackage);
                startActivity(i);
            }
        }
    }

    private boolean isBlocked(String pkg) {
        return pkg.equals("com.samsung.android.dialer")
                || pkg.equals("com.google.android.youtube");
    }

    private boolean isLauncher(String pkg) {
        return pkg.equals("com.sec.android.app.launcher")   // Samsung
                || pkg.equals("com.android.launcher")
                || pkg.contains("launcher");
    }

    @Override
    public void onInterrupt() {}
}








