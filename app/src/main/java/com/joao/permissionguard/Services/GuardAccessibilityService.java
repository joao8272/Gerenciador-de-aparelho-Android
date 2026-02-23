package com.joao.permissionguard.Services;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.joao.permissionguard.UI.LockActivity;

import java.util.List;

public class GuardAccessibilityService extends AccessibilityService {

    private static final long BLOCK_COOLDOWN = 1000;
    private final BroadcastReceiver backReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_FORCE_BACK_BROWSER".equals(intent.getAction())) {
                int backCount = intent.getIntExtra("back_count", 1);
                performBackInBrowser(backCount);
            }
        }
    };
    private long lastBlockTime = 0;
    private String activeBrowserSession = null;
    private final BroadcastReceiver authReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_AUTH_SUCCESS".equals(intent.getAction())) {
                String pkg = intent.getStringExtra("package");
                activeBrowserSession = pkg;
            }
        }
    };
    private String lastWindowClass = null;
    private String lastPackage = null;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_FORCE_BACK_BROWSER");
        filter.addAction("ACTION_AUTH_SUCCESS");

        registerReceiver(backReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(authReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        CharSequence pkgChar = event.getPackageName();
        if (pkgChar == null) return;

        String currentPackage = pkgChar.toString();
        String currentClass = event.getClassName() != null ? event.getClassName().toString() : "";

        SharedPreferences prefs = getSharedPreferences("lock_prefs", MODE_PRIVATE);
        String unlockedApp = prefs.getString("unlocked_app", null);
        String unlockedBrowser = prefs.getString("unlocked_browser", null); // <- Verificação importante

        if (lastPackage != null && !lastPackage.equals(currentPackage)) {
            lastWindowClass = null;
        }
        lastPackage = currentPackage;

        if (currentClass.equals(lastWindowClass)) return;
        lastWindowClass = currentClass;

        if (activeBrowserSession != null && !currentPackage.equals(activeBrowserSession)) {
            activeBrowserSession = null;
            prefs.edit().remove("unlocked_browser").apply();
        }
        if (isLauncher(currentPackage)) {
            activeBrowserSession = null;
            prefs.edit().remove("unlocked_app").remove("unlocked_browser").apply();
            return;
        }
        if (isBlockedApp(currentPackage) && unlockedApp == null) {
            openLockScreen(currentPackage, false);
            return;
        }

        if (unlockedApp != null && !currentPackage.equals(unlockedApp)) {
            prefs.edit()
                    .remove("unlocked_app")
                    .apply();
            unlockedApp = null;
        }
        if (isBrowser(currentPackage)) {
            if ((activeBrowserSession != null && activeBrowserSession.equals(currentPackage))
                    || (unlockedBrowser != null && unlockedBrowser.equals(currentPackage))) {
                return;
            }

            if (isBlockedWebsite(currentPackage)) {
                long now = System.currentTimeMillis();
                if (now - lastBlockTime < BLOCK_COOLDOWN) return;
                lastBlockTime = now;

                openLockScreen(currentPackage, true);

                new Handler().postDelayed(() -> {
                    String unlocked = getSharedPreferences("lock_prefs", MODE_PRIVATE)
                            .getString("unlocked_browser", null);
                    if (unlocked == null || !unlocked.equals(currentPackage)) {
                        Intent backIntent = new Intent("ACTION_FORCE_BACK_BROWSER");
                        backIntent.setPackage(getPackageName());
                        backIntent.putExtra("back_count", 5);
                        sendBroadcast(backIntent);
                    }
                }, 200);
            }
        }
    }

    private boolean isBlockedWebsite(String browserPackage) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        String[] possibleIds = {
                "com.android.chrome:id/url_bar",
                "com.microsoft.emmx:id/url_bar",
                "com.brave.browser:id/url_bar",
                "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
                "com.opera.browser:id/url_field",
                "com.sec.android.app.sbrowser:id/location_bar_edit_text"
        };

        for (String id : possibleIds) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);
            if (nodes == null || nodes.isEmpty()) continue;

            for (AccessibilityNodeInfo node : nodes) {
                CharSequence url = node.getText();
                if (url == null) continue;

                String urlStr = url.toString().toLowerCase().trim();

                if (
                        urlStr.contains("instagram.com") ||
                                urlStr.contains("twitter.com") ||
                                urlStr.contains("x.com") ||
                                urlStr.contains("mercadolivre.com") ||
                                urlStr.contains("ml.com") ||
                                urlStr.contains("amazon.com") ||
                                urlStr.contains("amazon.com.br") ||
                                urlStr.contains("globo.com") ||
                                urlStr.contains("g1.globo.com") ||
                                urlStr.contains("globonews.globo.com") ||
                                urlStr.contains("facebook.com") ||
                                urlStr.contains("fb.com") ||
                                urlStr.contains("olx.com") ||
                                urlStr.contains("olx.com.br") ||
                                urlStr.contains("olx.com.ar") ||
                                urlStr.contains("olx.pt") ||
                                urlStr.contains("olx.ua") ||
                                urlStr.contains("kwai.com") ||
                                urlStr.contains("primevideo.com") ||
                                urlStr.contains("hbomax.com") ||
                                urlStr.contains("globoplay.com") ||
                                urlStr.contains("tinder.com") ||
                                urlStr.contains("tiktok.com") ||
                                urlStr.contains("snapchat.com")
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    private void openLockScreen(String packageName, boolean isBrowser) {
        if (!LockActivity.isLockActivityOpen) {
            Intent i = new Intent(this, LockActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("packageToUnlock", packageName);
            i.putExtra("isBrowser", isBrowser);
            startActivity(i);
        }
    }

    private boolean isBrowser(String pkg) {
        return pkg.equals("com.android.chrome") ||
                pkg.equals("com.sec.android.app.sbrowser") ||
                pkg.equals("com.microsoft.emmx") ||
                pkg.equals("com.brave.browser") ||
                pkg.equals("com.opera.browser") ||
                pkg.equals("com.opera.gx") ||
                pkg.equals("org.mozilla.firefox") ||
                pkg.equals("org.mozilla.focus");
    }

    private boolean isBlockedApp(String pkg) {
        return
                pkg.equals("com.netflix.mediaclient") ||
                        pkg.equals("com.spotify.music") ||
                        pkg.equals("com.zhiliaoapp.musically") ||
                        pkg.equals("com.amazon.mShop.android.shopping") ||
                        pkg.equals("com.amazon.avod.thirdpartyclient") ||
                        pkg.equals("com.amazon.amazonvideo.livingroom") ||
                        pkg.equals("com.amazon.mp3") ||
                        pkg.equals("com.twitter.android") ||
                        pkg.equals("com.mercadolibre") ||
                        pkg.equals("com.instagram.android") ||
                        pkg.equals("com.facebook.katana") ||
                        pkg.equals("com.facebook.lite") ||
                        pkg.equals("com.olxbr") ||
                        pkg.equals("com.globo.globotv") ||
                        pkg.equals("com.globo.g1") ||
                        pkg.equals("com.globo.ge") ||
                        pkg.equals("com.globo.globonews") ||
                        pkg.equals("com.snapchat.android") ||
                        pkg.equals("com.kwai.video") ||
                        pkg.equals("com.hbo.hbonow") ||
                        pkg.equals("br.globo.globoplay") ||
                        pkg.equals("com.tinder");
    }

    private boolean isLauncher(String pkg) {
        return pkg.contains("launcher") ||
                pkg.equals("com.sec.android.app.launcher") ||
                pkg.equals("com.android.launcher");
    }

    private void performBackInBrowser(int count) {
        String currentPkg = lastPackage;
        if (!isBrowser(currentPkg)) return;

        new Thread(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    Thread.sleep(300);
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(backReceiver);
        unregisterReceiver(authReceiver);
    }
}