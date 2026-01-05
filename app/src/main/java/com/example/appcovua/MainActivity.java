package com.example.appcovua;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.example.appcovua.controller.GameController;
import com.example.appcovua.model.ChessBoard;
import com.example.appcovua.model.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private boolean vsAI;
    private static final int SIZE = 8;

    // MVC
    private ChessBoard chessBoard;
    private GameController gameController;

    // Views
    private GridLayout banCo;
    private TextView textLuotChoi, logText;
    private ImageButton btnHint, btnUndo;
    private ScrollView logScroll;

    // State
    private boolean luotTrang = true;
    private int[] viTriDangChon = null;
    private ImageView oCoDangChon = null;
    private final List<ImageView> oCoDangToMau = new ArrayList<>();
    private ImageView oCoHint = null;
    private final Random random = new Random();

    // Undo history
    private final Stack<Move> history = new Stack<>();

    private int cellSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Đọc intent
        vsAI = getIntent().getBooleanExtra("MODE_AI", false);

        setContentView(R.layout.activity_main);

        // 2. Bắt nút back custom
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Quay về menu chính
            finish(); // hoặc launch MainMenuActivity nếu bạn clear stack
        });

        chessBoard = new ChessBoard();
        gameController = new GameController(chessBoard);

        banCo = findViewById(R.id.banCo);
        textLuotChoi = findViewById(R.id.textLuotChoi);
        logText = findViewById(R.id.logText);
        logScroll = findViewById(R.id.logScroll);
        btnHint = findViewById(R.id.btnHint);
        btnUndo = findViewById(R.id.btnUndo);

        btnHint.setOnClickListener(v -> showHint());
        btnUndo.setOnClickListener(v -> undoMove());

        capNhatLuotChoi();

        banCo.post(() -> {
            cellSize = banCo.getWidth() / SIZE;
            banCo.getLayoutParams().height = cellSize * SIZE;
            banCo.requestLayout();
            taoBanCo(cellSize);
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    private void taoBanCo(int size) {
        banCo.removeAllViews();
        banCo.setColumnCount(SIZE);
        banCo.setRowCount(SIZE);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                banCo.addView(taoOCo(r, c, size));
            }
        }
    }

    private ImageView taoOCo(int row, int col, int size) {
        ImageView oCo = new ImageView(this);
        int bg = ((row + col) % 2 == 0)
                ? Color.parseColor("#F0D9B5")
                : Color.parseColor("#B58863");
        oCo.setBackgroundColor(bg);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = size;
        lp.height = size;
        oCo.setLayoutParams(lp);
        oCo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int resId = gameController.getPieceAt(row, col);
        if (resId != 0) oCo.setImageResource(resId);
        oCo.setTag(new int[]{row, col});
        oCo.setOnClickListener(this::chonOCo);
        return oCo;
    }

    private void chonOCo(View view) {
        // 1. Nếu đang ở chế độ AI và chưa tới lượt Trắng (AI đang đi) → bỏ qua
        if (vsAI && !luotTrang) {
            return;
        }

        ImageView oCo = (ImageView) view;
        int[] pos = (int[]) oCo.getTag();
        int r = pos[0], c = pos[1];
        int idQuan = gameController.getPieceAt(r, c);

        // 2. Nếu đã chọn ô ban đầu và người dùng bấm vào ô đích khác ô đó
        if (viTriDangChon != null && oCo != oCoDangChon) {
            int fromR = viTriDangChon[0];
            int fromC = viTriDangChon[1];
            // **Khai báo movingPiece ở đây để dùng cho en passant, castling, promotion**
            int movingPiece = gameController.getPieceAt(fromR, fromC);

            // **Lấy luôn các nước đi đã lọc chiếu tự thân**
            List<int[]> legalMoves = gameController.getLegalMoves(fromR, fromC);


        // 3. Thực thi nước đi nếu trong danh sách hợp lệ
            for (int[] mv : legalMoves) {
                if (mv[0] == r && mv[1] == c) {
                    int captured = chessBoard.getBoard()[r][c];

                    // 3a. Chặn ăn vua → kết thúc ngay
                    if (captured == R.drawable.vua_trang || captured == R.drawable.vua_den) {
                        banCo.setEnabled(false);
                        String winner = luotTrang ? "Trắng" : "Đen";
                        int icon = luotTrang ? R.drawable.vua_trang : R.drawable.vua_den;

                        new MaterialAlertDialogBuilder(this)
                                .setTitle("🎉 " + winner + " thắng!")
                                .setMessage(winner + " đã bắt được vua đối phương. Bạn có muốn chơi lại?")
                                .setIcon(icon)
                                .setPositiveButton("Chơi lại", (dialog, which) -> recreate())
                                .setNegativeButton("Thoát", (dialog, which) -> finishAffinity())
                                .setCancelable(false)
                                .show();

                        return;
                    }


                    // 3b. Xử lý en passant
                    int[] ep = chessBoard.getEnPassantTarget();
                    boolean isEP = ((movingPiece == R.drawable.tot_trang || movingPiece == R.drawable.tot_den)
                            && ep != null && r == ep[0] && c == ep[1]);
                    if (isEP) {
                        // quân bị bắt en passant nằm ở hàng fromR, cột c
                        captured = chessBoard.getBoard()[fromR][c];
                        chessBoard.move(fromR, fromC, r, c);
                        history.push(new Move(fromR, fromC, r, c, captured, true, fromR, c));
                    }
                    // 3c. Xử lý castling
                    else if ((movingPiece == R.drawable.vua_trang || movingPiece == R.drawable.vua_den)
                            && Math.abs(c - fromC) == 2) {
                        int rookFrom = c - fromC > 0 ? 7 : 0;
                        int rookTo   = c - fromC > 0 ? c - 1 : c + 1;
                        chessBoard.move(fromR, fromC, r, c);
                        chessBoard.move(r, rookFrom, r, rookTo);
                        history.push(new Move(fromR, fromC, r, c, ChessBoard.EMPTY, rookFrom, rookTo));
                    }
                    // 3d. Xử lý promotion
                    else if ((movingPiece == R.drawable.tot_trang && r == 0)
                            || (movingPiece == R.drawable.tot_den   && r == 7)) {
                        showPromotionDialog(fromR, fromC, r, c, captured, luotTrang);
                        return;
                    }
                    // 3e. Move bình thường (có thể kèm capture)
                    else {
                        chessBoard.move(fromR, fromC, r, c);
                        history.push(new Move(fromR, fromC, r, c, captured));
                        if (captured != ChessBoard.EMPTY) {
                            String who = luotTrang ? "Trắng" : "Đen";
                            String name = getPieceName(captured);
                            logText.append(who + " ăn " + name + "\n");
                            logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
                        }
                    }

                    // 4. Chuyển lượt
                    luotTrang = !luotTrang;
                    capNhatLuotChoi();
                    clearSelection();
                    taoBanCo(banCo.getWidth() / SIZE);

                    // 5. Kiểm tra endgame
                    checkEndgame();

                    // 6. Nếu chơi với AI và tới lượt Đen → cho AI đánh
                    if (vsAI && !luotTrang) {
                        aiMove();
                    }
                    return;
                }
            }
        }

        // 7. Kiểm tra người chọn đúng quân theo lượt
        if ((luotTrang && !laQuanTrang(idQuan)) || (!luotTrang && laQuanTrang(idQuan))) {
            return;
        }

        // 8. Đánh dấu chọn ô
        clearSelection();
        oCoDangChon = oCo;
        viTriDangChon = pos;
        oCo.setBackgroundColor(Color.parseColor("#FFD700"));
        hienThiNuocDiHopLe(r, c);
    }


    private void aiMove() {
        banCo.postDelayed(() -> {
            // 1. Tập hợp tất cả nước đi cho quân Đen
            List<int[]> allMoves = new ArrayList<>();
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    int id = gameController.getPieceAt(r, c);
                    if (!laQuanTrang(id)) {
                        for (int[] mv : gameController.getLegalMoves(r, c)) {
                            allMoves.add(new int[]{r, c, mv[0], mv[1]});
                        }
                    }
                }
            }
            // 2. Nếu hết nước → để checkEndgame bắt stalemate
            if (allMoves.isEmpty()) {
                checkEndgame();
                return;
            }

            // 3. Chọn ngẫu nhiên và thực thi
            int[] pick = allMoves.get(random.nextInt(allMoves.size()));
            int fr = pick[0], fc = pick[1], tr = pick[2], tc = pick[3];
            int captured = chessBoard.getBoard()[tr][tc];

            // Chặn AI ăn vua Trắng
            if (captured == R.drawable.vua_trang) {
                banCo.setEnabled(false);
                Snackbar.make(banCo,
                                "Đen đã ăn vua Trắng — Đen thắng!",
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("Chơi lại", v -> recreate())
                        .show();
                return;
            }

            chessBoard.move(fr, fc, tr, tc);
            history.push(new Move(fr, fc, tr, tc, captured));
            if (captured != ChessBoard.EMPTY) {
                logText.append("Đen ăn " + getPieceName(captured) + "\n");
                logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
            }

            // 4. Chuyển về lượt Trắng
            luotTrang = true;
            capNhatLuotChoi();
            taoBanCo(banCo.getWidth() / SIZE);
            checkEndgame();
        }, 500);
    }

    private void hienThiNuocDiHopLe(int row, int col) {
        List<int[]> moves = gameController.getLegalMoves(row, col);
        for (int[] mv : moves) {
            int idx = mv[0] * SIZE + mv[1];
            ImageView cell = (ImageView) banCo.getChildAt(idx);
            if (cell != null) {
                cell.setBackgroundColor(Color.parseColor("#66BB6A"));
                oCoDangToMau.add(cell);
            }
        }
    }

    private void showHint() {
        if (viTriDangChon == null) {
            Toast.makeText(this, "Chọn quân trước", Toast.LENGTH_SHORT).show();
            return;
        }
        List<int[]> moves = gameController.getLegalMoves(viTriDangChon[0], viTriDangChon[1]);
        if (moves.isEmpty()) {
            Toast.makeText(this, "Không có nước đi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (oCoHint != null) resetCellBackground(oCoHint);
        int[] h = moves.get(random.nextInt(moves.size()));
        int idx = h[0] * SIZE + h[1];
        oCoHint = (ImageView) banCo.getChildAt(idx);
        if (oCoHint != null) {
            oCoHint.setBackgroundColor(Color.parseColor("#FF9800"));
        }
    }

    private void clearSelection() {
        if (oCoDangChon != null) resetCellBackground(oCoDangChon);
        for (ImageView c : oCoDangToMau) resetCellBackground(c);
        oCoDangToMau.clear();
        if (oCoHint != null) resetCellBackground(oCoHint);
        oCoHint = null;
    }

    private void resetCellBackground(ImageView cell) {
        int[] p = (int[]) cell.getTag();
        int bg = ((p[0] + p[1]) % 2 == 0)
                ? Color.parseColor("#F0D9B5")
                : Color.parseColor("#B58863");
        cell.setBackgroundColor(bg);
    }

    private void capNhatLuotChoi() {
        textLuotChoi.setText(luotTrang ? "Lượt trắng" : "Lượt đen");
    }

    private boolean laQuanTrang(int resId) {
        return resId == R.drawable.tot_trang
                || resId == R.drawable.xe_trang
                || resId == R.drawable.ma_trang
                || resId == R.drawable.tuong_trang
                || resId == R.drawable.hau_trang
                || resId == R.drawable.vua_trang;
    }

    private void undoMove() {
        if (history.isEmpty()) {
            Toast.makeText(this, "Chưa có nước để quay lại", Toast.LENGTH_SHORT).show();
            return;
        }
        // Lấy nước cuối cùng
        Move m = history.pop();

        // Hoàn nguyên nước en passant
        if (m.isEnPassant) {
            chessBoard.move(m.toRow, m.toCol, m.fromRow, m.fromCol);
            chessBoard.getBoard()[m.epCapRow][m.epCapCol] = m.captured;
        }
        // Hoàn nguyên castling
        else if (m.isCastling) {
            chessBoard.move(m.toRow, m.toCol, m.fromRow, m.fromCol);
            chessBoard.move(m.fromRow, m.rookToCol, m.fromRow, m.rookFromCol);
        }
        // Hoàn nguyên promotion
        else if (m.promotionPiece != ChessBoard.EMPTY) {
            // Nếu có quân bị ăn, đặt lại quân bị ăn vào ô đích
            if (m.captured != ChessBoard.EMPTY) {
                chessBoard.getBoard()[m.toRow][m.toCol] = m.captured;
            } else {
                chessBoard.getBoard()[m.toRow][m.toCol] = ChessBoard.EMPTY;
            }
            // Đặt lại pawn về vị trí ban đầu
            int pawn = (m.promotionPiece == R.drawable.hau_trang
                    || m.promotionPiece == R.drawable.xe_trang
                    || m.promotionPiece == R.drawable.tuong_trang
                    || m.promotionPiece == R.drawable.ma_trang)
                    ? R.drawable.tot_trang
                    : R.drawable.tot_den;
            chessBoard.getBoard()[m.fromRow][m.fromCol] = pawn;
        }
        // Hoàn nguyên nước đi bình thường (có thể kèm capture)
        else {
            chessBoard.move(m.toRow, m.toCol, m.fromRow, m.fromCol);
            if (m.captured != ChessBoard.EMPTY) {
                chessBoard.getBoard()[m.toRow][m.toCol] = m.captured;
            }
        }

        // Đảo lượt
        luotTrang = !luotTrang;
        capNhatLuotChoi();
        clearSelection();

        // Chỉ cập nhật lại 2 ô (tối ưu): hoặc
        // nếu chưa refactor thì rebuild toàn bộ với cellSize đã lưu:
        taoBanCo(cellSize);

        // Nếu đang chơi với AI và lượt hiện tại là AI (đen), cho AI đánh tiếp
        if (vsAI && !luotTrang) {
            banCo.postDelayed(this::aiMove, 300);
        }
    }


    private String getPieceName(int resId) {
        if      (resId==R.drawable.tot_trang)   return "Tốt trắng";
        else if (resId==R.drawable.tot_den)     return "Tốt đen";
        else if (resId==R.drawable.xe_trang)    return "Xe trắng";
        else if (resId==R.drawable.xe_den)      return "Xe đen";
        else if (resId==R.drawable.ma_trang)    return "Mã trắng";
        else if (resId==R.drawable.ma_den)      return "Mã đen";
        else if (resId==R.drawable.tuong_trang) return "Tượng trắng";
        else if (resId==R.drawable.tuong_den)   return "Tượng đen";
        else if (resId==R.drawable.hau_trang)   return "Hậu trắng";
        else if (resId==R.drawable.hau_den)     return "Hậu đen";
        else if (resId==R.drawable.vua_trang)   return "Vua trắng";
        else if (resId==R.drawable.vua_den)     return "Vua đen";
        else                                     return "Không xác định";
    }


    private void checkEndgame() {
        // bên tới lượt sẽ đi, cũng là bên có thể đang bị chiếu
        boolean sideToMove = luotTrang;      // true = White tới lượt, false = Black tới lượt
        boolean inCheck   = gameController.isInCheck(sideToMove);
        boolean hasMoves  = gameController.hasAnyLegalMove(sideToMove);


        Log.d("DEBUG-CHESS", "sideToMove=" + (sideToMove?"White":"Black")
                + "  inCheck=" + inCheck
                + "  hasMoves=" + hasMoves);

        // 1. Checkmate (bị chiếu hết)
        if (inCheck && !hasMoves) {
            clearSelection();
            banCo.setEnabled(false);
            String loser = sideToMove ? "Trắng" : "Đen";
            String winner = sideToMove ? "Đen" : "Trắng";
            int icon = loser.equals("Trắng") ? R.drawable.vua_trang : R.drawable.vua_den;

            new MaterialAlertDialogBuilder(this)
                    .setTitle("🏁 " + winner + " thắng!")
                    .setMessage(loser + " bị chiếu hết. Bạn có muốn chơi lại?")
                    .setIcon(icon)
                    .setPositiveButton("Chơi lại", (dialog, which) -> recreate())
                    .setNegativeButton("Thoát", (dialog, which) -> finishAffinity())
                    .setCancelable(false)
                    .show();

            return;
        }

        // 2. Đang bị chiếu nhưng vẫn còn nước đi
        if (inCheck) {
            String who = sideToMove ? "Trắng" : "Đen";
            Snackbar.make(
                            findViewById(android.R.id.content),
                            who + " đang bị chiếu",
                            Snackbar.LENGTH_LONG
                    )
                    .show();
            return;
        }

        // 3. Stalemate (hòa do không còn nước đi và không bị chiếu)
        if (!inCheck && !hasMoves) {
            clearSelection();
            banCo.setEnabled(false);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("🤝 Hòa cờ")
                    .setMessage("Không còn nước đi hợp lệ. Ván cờ kết thúc hòa.")
                    .setIcon(R.drawable.vua_trang)
                    .setPositiveButton("Chơi lại", (dialog, which) -> recreate())
                    .setNegativeButton("Thoát", (dialog, which) -> finishAffinity())
                    .setCancelable(false)
                    .show();
            return;
        }

        // 4. Thường tiếp tục chơi (không làm gì thêm)
    }




    private void showPromotionDialog(int fromR, int fromC, int toR, int toC,
                                     int captured, boolean isWhite) {
        final String[] labels = {"Hậu", "Xe", "Tượng", "Mã"};
        final int[] whitePieces = {
                R.drawable.hau_trang, R.drawable.xe_trang,
                R.drawable.tuong_trang, R.drawable.ma_trang
        };
        final int[] blackPieces = {
                R.drawable.hau_den, R.drawable.xe_den,
                R.drawable.tuong_den, R.drawable.ma_den
        };
        final int[] promoPieces = isWhite ? whitePieces : blackPieces;

        new MaterialAlertDialogBuilder(this)
                .setTitle("♛ Chọn quân để phong cấp")
                .setSingleChoiceItems(labels, -1, (dialog, which) -> {
                    int promo = promoPieces[which];

                    // (1) Thực hiện phong cấp
                    chessBoard.move(fromR, fromC, toR, toC, promo);
                    history.push(new Move(fromR, fromC, toR, toC, captured, promo));

                    // (2) Ghi log nếu có bắt quân
                    if (captured != ChessBoard.EMPTY) {
                        String who = luotTrang ? "Trắng" : "Đen";
                        logText.append(who + " ăn " + getPieceName(captured) + "\n");
                        logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
                    }

                    // (3) Cập nhật giao diện
                    luotTrang = !luotTrang;
                    capNhatLuotChoi();
                    clearSelection();
                    taoBanCo(banCo.getWidth() / SIZE);

                    // (4) Kiểm tra kết thúc
                    checkEndgame();

                    // (5) Nếu chơi với AI và đến lượt AI
                    if (vsAI && !luotTrang) {
                        aiMove();
                    }

                    dialog.dismiss(); // đóng dialog sau chọn
                })
                .setCancelable(false)
                .show();
    }
}