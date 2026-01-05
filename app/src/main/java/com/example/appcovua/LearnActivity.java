package com.example.appcovua;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LearnActivity extends AppCompatActivity {

    private ImageView imgPawn, imgKnight, imgBishop, imgRook, imgQueen, imgKing;
    private TextView textPawn, textKnight, textBishop, textRook, textQueen, textKing;
    private MaterialButton btnBackMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // Hiển thị nút back trên ActionBar và tiêu đề
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hướng dẫn chơi Cờ Vua");
        }


        // Binding các view
        imgPawn     = findViewById(R.id.imgPawn);
        textPawn    = findViewById(R.id.textPawn);
        imgKnight   = findViewById(R.id.imgKnight);
        textKnight  = findViewById(R.id.textKnight);
        imgBishop   = findViewById(R.id.imgBishop);
        textBishop  = findViewById(R.id.textBishop);
        imgRook     = findViewById(R.id.imgRook);
        textRook    = findViewById(R.id.textRook);
        imgQueen    = findViewById(R.id.imgQueen);
        textQueen   = findViewById(R.id.textQueen);
        imgKing     = findViewById(R.id.imgKing);
        textKing    = findViewById(R.id.textKing);
        btnBackMenu = findViewById(R.id.btnBackMenu);

        textPawn.setOnClickListener(v -> showDetailDialog(
                "Quân Tốt",
                "• Di chuyển thẳng 1 ô mỗi lượt.\n"
                        + "• Lần đầu đi có thể di chuyển 2 ô.\n"
                        + "• Ăn chéo 1 ô về phía trước."));

        textKnight.setOnClickListener(v -> showDetailDialog(
                "Quân Mã",
                "• Di chuyển theo hình chữ L: 2 ô theo trục + 1 ô vuông góc.\n"
                        + "• Có thể nhảy qua các quân khác."));

        textBishop.setOnClickListener(v -> showDetailDialog(
                "Quân Tượng",
                "• Di chuyển chéo không giới hạn số ô.\n"
                        + "• Chỉ đi trên màu ô ban đầu."));

        textRook.setOnClickListener(v -> showDetailDialog(
                "Quân Xe",
                "• Di chuyển thẳng theo hàng hoặc cột.\n"
                        + "• Tham gia nhập thành với Quân Vua."));

        textQueen.setOnClickListener(v -> showDetailDialog(
                "Quân Hậu",
                "• Kết hợp di chuyển của Quân Xe và Quân Tượng: thẳng hoặc chéo.\n"
                        + "• Không giới hạn số ô."));

        textKing.setOnClickListener(v -> showDetailDialog(
                "Quân Vua",
                "• Di chuyển 1 ô mọi hướng.\n"
                        + "• Có thể nhập thành với Quân Xe nếu điều kiện thỏa mãn."));

        // Xử lý nút Quay lại Main Menu
        btnBackMenu.setOnClickListener(v -> {
            Intent intent = new Intent(LearnActivity.this, MainMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Hiển thị dialog chi tiết cho mỗi quân
     */
    private void showDetailDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Xử lý nút back trên ActionBar: trở về MainMenu
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }
}