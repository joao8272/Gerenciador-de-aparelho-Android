package com.joao.permissionguard.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.joao.permissionguard.R;

public class LockActivity extends AppCompatActivity {

    private static final String ADMIN_PASSWORD = "892472";

    public static boolean isLockActivityOpen = false;

    private EditText passwordEdit;
    private Button unlockBtn;
    private Button cancelBtn;

    private String packageToUnlock;
    private boolean isBrowser;
    private boolean authenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_lock);
        isLockActivityOpen = true;

        passwordEdit = findViewById(R.id.password);
        unlockBtn = findViewById(R.id.unlock);
        cancelBtn = findViewById(R.id.cancel);

        packageToUnlock = getIntent().getStringExtra("packageToUnlock");
        isBrowser = getIntent().getBooleanExtra("isBrowser", false);

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                    }
                });

        unlockBtn.setOnClickListener(v -> {
            String entered = passwordEdit.getText().toString();

            if (entered.equals(ADMIN_PASSWORD)) {
                authenticated = true;

                if (packageToUnlock != null) {
                    getSharedPreferences("lock_prefs", MODE_PRIVATE)
                            .edit()
                            .putString(
                                    isBrowser ? "unlocked_browser" : "unlocked_app",
                                    packageToUnlock
                            )
                            .apply();
                }

                new Handler().postDelayed(this::finish, 200);

            } else {
                Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show();
                passwordEdit.setText("");
            }
        });

        cancelBtn.setOnClickListener(v -> {
            clearUnlock();

            if (isBrowser) {
                Intent backIntent = new Intent("ACTION_FORCE_BACK_BROWSER");
                backIntent.putExtra("back_count", 5);
                sendBroadcast(backIntent);
            }

            finish();
        });
    }

    private void clearUnlock() {
        getSharedPreferences("lock_prefs", MODE_PRIVATE)
                .edit()
                .remove("unlocked_app")
                .remove("unlocked_browser")
                .apply();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!authenticated) {
            clearUnlock();
            if (!isBrowser) goHome();
        }
    }

    private void goHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLockActivityOpen = false;
    }
}
