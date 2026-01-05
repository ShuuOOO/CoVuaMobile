package com.example.appcovua;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        // Nút Chơi với máy
        findViewById(R.id.btn_play_ai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(true);
            }
        });

        // Nút Chơi 2 người
        findViewById(R.id.btn_play_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(false);
            }
        });

        // Nút Learn
        findViewById(R.id.btn_learn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLearn();
            }
        });

        MaterialButton btnExit = findViewById(R.id.btn_exit);

        btnExit.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn thoát?")
                    .setPositiveButton("Thoát", (dialog, which) -> finishAffinity())
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

    }

    private void startGame(boolean vsAI) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("MODE_AI", vsAI);
        startActivity(intent);
    }

    private void launchLearn() {
        Intent intent = new Intent(this, LearnActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Không gọi super.onBackPressed() vì không muốn quay lại màn hình trước
        // Đây là activity đầu tiên → kết thúc app
        finish();
    }
}
