package com.example.appcovua.model;

import com.example.appcovua.model.ChessBoard;

/**
 * Lưu thông tin một nước đi, bao gồm:
 * - Vị trí xuất phát
 * - Vị trí đích
 * - Quân bị bắt
 * - Promotion piece (nếu có)
 * - Castling info (nếu có)
 * - En passant info (nếu có)
 */
public class Move {
    public final int fromRow, fromCol;
    public final int toRow, toCol;
    public final int captured;
    public final int promotionPiece;
    public final boolean isCastling;
    public final int rookFromCol, rookToCol;
    public final boolean isEnPassant;
    public final int epCapRow, epCapCol;

    // Nước đi bình thường
    public Move(int fr, int fc, int tr, int tc, int cap) {
        this(fr, fc, tr, tc, cap, ChessBoard.EMPTY, false, -1, -1,false,-1,-1);
    }

    // Promotion
    public Move(int fr, int fc, int tr, int tc, int cap, int promo) {
        this(fr, fc, tr, tc, cap, promo, false, -1, -1,false,-1,-1);
    }

    // Castling
    public Move(int fr, int fc, int tr, int tc, int cap,
                int rookFc, int rookTc) {
        this(fr, fc, tr, tc, cap, ChessBoard.EMPTY, true, rookFc, rookTc,false,-1,-1);
    }

    // En passant
    public Move(int fr, int fc, int tr, int tc, int cap,
                boolean isEP, int epRow, int epCol) {
        this(fr, fc, tr, tc, cap, ChessBoard.EMPTY, false, -1, -1,isEP,epRow,epCol);
    }

    private Move(int fr, int fc, int tr, int tc,
                 int cap, int promo,
                 boolean castling, int rookFc, int rookTc,
                 boolean enPassant, int epRow, int epCol) {
        this.fromRow = fr;
        this.fromCol = fc;
        this.toRow   = tr;
        this.toCol   = tc;
        this.captured = cap;
        this.promotionPiece = promo;
        this.isCastling = castling;
        this.rookFromCol = rookFc;
        this.rookToCol   = rookTc;
        this.isEnPassant = enPassant;
        this.epCapRow = epRow;
        this.epCapCol = epCol;
    }
}
