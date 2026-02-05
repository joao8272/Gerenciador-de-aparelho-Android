package com.joao.permissionguard.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.joao.permissionguard.R;

public class LockActivity extends AppCompatActivity {

    private static final String ADMIN_PASSWORD = "8924";

    public static boolean isLockActivityOpen = false;

    private EditText passwordEdit;
    private Button unlockBtn;
    private String packageToUnlock;

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

        authenticated = false;

        passwordEdit = findViewById(R.id.password);
        unlockBtn = findViewById(R.id.unlock);

        packageToUnlock = getIntent().getStringExtra("packageToUnlock");

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
                            .putString("unlocked_app", packageToUnlock)
                            .apply();
                }

                finish();

            } else {
                Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show();
                passwordEdit.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        authenticated = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!authenticated) {

            getSharedPreferences("lock_prefs", MODE_PRIVATE)
                    .edit()
                    .remove("unlocked_app")
                    .apply();

            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);

            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLockActivityOpen = false;
    }
}
















