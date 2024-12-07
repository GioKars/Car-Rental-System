package com.example.car_rent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Ensure there's no action bar title if using a custom toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setupToolbar();

        Button myAccountButton = findViewById(R.id.button_my_account);
        myAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MyAccountActivity.class);
            startActivity(intent);
        });

        Button history = findViewById(R.id.button_history);
        history.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.contactSupportButton).setOnClickListener(v -> {
            // Example: Redirect to a support page or send an email
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@example.com"});
            startActivity(Intent.createChooser(intent, "Contact Support"));
        });

        ImageView userIcon = findViewById(R.id.user_icon);
        userIcon.setOnClickListener(this::showUserMenu);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.dashboardToolbar);
        setSupportActionBar(toolbar);
    }

    private void showUserMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.dashboard_user_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    finish();
                    return true;

                case R.id.action_return_main:
                    startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                    return true;

                default:
                    return false;
            }
        });

        popupMenu.show();
    }
}
