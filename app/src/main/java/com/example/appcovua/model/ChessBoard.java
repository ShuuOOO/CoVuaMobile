package com.example.appcovua.model;

import com.example.appcovua.R;

public class ChessBoard {
    public static final int EMPTY = 0;
    private final int[][] board = new int[8][8];

    // Vị trí có thể bị bắt en passant
    private int[] enPassantTarget = null;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookAMoved = false; // Trắng - a1
    private boolean whiteRookHMoved = false; // Trắng - h1
    private boolean blackRookAMoved = false; // Đen - a8
    private boolean blackRookHMoved = false; // Đen - h8

    public ChessBoard() {
        setupDefault();
    }

    // Truy cập bảng cờ
    public int[][] getBoard() {
        return board;
    }

    // Truy cập mục tiêu en passant
    public int[] getEnPassantTarget() {
        return enPassantTarget;
    }

    // Kiểm tra vua đã di chuyển chưa (theo màu)
    public boolean hasKingMoved(boolean white) {
        return white ? whiteKingMoved : blackKingMoved;
    }

    // Kiểm tra rook đã di chuyển chưa (trắng/đen + bên trái/phải)
    public boolean hasRookMoved(boolean white, boolean kingside) {
        if (white) {
            return kingside ? whiteRookHMoved : whiteRookAMoved;
        } else {
            return kingside ? blackRookHMoved : blackRookAMoved;
        }
    }

    // Khởi tạo bàn cờ mặc định
    public void setupDefault() {
        // Quân đen
        board[0] = new int[]{
                R.drawable.xe_den,   R.drawable.ma_den,   R.drawable.tuong_den, R.drawable.hau_den,
                R.drawable.vua_den,  R.drawable.tuong_den, R.drawable.ma_den,    R.drawable.xe_den
        };
        for (int i = 0; i < 8; i++) board[1][i] = R.drawable.tot_den;

        // Quân trắng
        board[7] = new int[]{
                R.drawable.xe_trang, R.drawable.ma_trang, R.drawable.tuong_trang, R.drawable.hau_trang,
                R.drawable.vua_trang, R.drawable.tuong_trang, R.drawable.ma_trang, R.drawable.xe_trang
        };
        for (int i = 0; i < 8; i++) board[6][i] = R.drawable.tot_trang;
    }

    // Truy xuất quân tại vị trí cụ thể
    public int getPiece(int row, int col) {
        return board[row][col];
    }

    // Di chuyển không promotion
    public void move(int fr, int fc, int tr, int tc) {
        move(fr, fc, tr, tc, EMPTY);
    }

    // Di chuyển có xử lý promotion + en passant + cập nhật trạng thái castling
    public void move(int fr, int fc, int tr, int tc, int promotionPiece) {
        int moving = board[fr][fc];
        int[] oldEP = enPassantTarget;

        // (1) Thực hiện di chuyển và đặt quân nếu promotion
        board[fr][fc] = EMPTY;
        board[tr][tc] = (promotionPiece != EMPTY ? promotionPiece : moving);

        // (2) Nếu là bắt en passant → xóa quân bị bắt
        if ((moving == R.drawable.tot_trang || moving == R.drawable.tot_den)
                && oldEP != null && tr == oldEP[0] && tc == oldEP[1]) {
            board[fr][tc] = EMPTY;
        }

        // (3) Cập nhật cờ "đã di chuyển" (dùng cho nhập thành)
        if (moving == R.drawable.vua_trang) {
            whiteKingMoved = true;
        } else if (moving == R.drawable.vua_den) {
            blackKingMoved = true;
        } else if (moving == R.drawable.xe_trang) {
            if (fr == 7 && fc == 0) whiteRookAMoved = true;
            if (fr == 7 && fc == 7) whiteRookHMoved = true;
        } else if (moving == R.drawable.xe_den) {
            if (fr == 0 && fc == 0) blackRookAMoved = true;
            if (fr == 0 && fc == 7) blackRookHMoved = true;
        }

        // (4) Nếu là tốt và đi 2 ô → đặt vị trí en passant
        if (moving == R.drawable.tot_trang && fr == 6 && tr == 4) {
            enPassantTarget = new int[]{5, fc};
        } else if (moving == R.drawable.tot_den && fr == 1 && tr == 3) {
            enPassantTarget = new int[]{2, fc};
        } else {
            enPassantTarget = null;
        }
    }
}
