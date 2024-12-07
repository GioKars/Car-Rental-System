package com.example.car_rent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class BanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ban);

        // Toolbar menu handling
        ImageView userIcon = findViewById(R.id.user_icon);
        userIcon.setOnClickListener(this::showUserMenu);

        // Contact support button
        findViewById(R.id.contact_support_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@example.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Banned Account Assistance");
            startActivity(Intent.createChooser(intent, "Contact Support"));
        });
    }

    // Method to show the user menu
    private void showUserMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.ban_activity_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.ban_action_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(BanActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}
