package com.example.appcovua.controller;

import com.example.appcovua.R;
import com.example.appcovua.model.ChessBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý logic cờ: valid moves, check, checkmate, castling (nhập thành), en passant (bắt tốt qua đường)
 */
public class GameController {
    private final ChessBoard board;
    private static final int SIZE = 8;

    public GameController(ChessBoard board) {
        this.board = board;
    }

    public int getPieceAt(int row, int col) {
        return board.getPiece(row, col);
    }

    private boolean laQuanTrang(int id) {
        return id == R.drawable.tot_trang || id == R.drawable.xe_trang
                || id == R.drawable.ma_trang  || id == R.drawable.tuong_trang
                || id == R.drawable.hau_trang || id == R.drawable.vua_trang;
    }

    /**
     * Trả về danh sách nước đi hợp lệ (đã loại bỏ nước dẫn đến chiếu)
     * Bao gồm castling và en passant.
     */
    public List<int[]> getLegalMoves(int row, int col) {
        List<int[]> raw = getPseudoLegalMoves(row, col);
        List<int[]> legal = new ArrayList<>();
        boolean whiteTurn = laQuanTrang(board.getPiece(row, col));

        // Lọc các nước đi gây chiếu
        for (int[] mv : raw) {
            int fr = row, fc = col, tr = mv[0], tc = mv[1];
            int captured = board.getPiece(tr, tc);

            board.move(fr, fc, tr, tc);
            boolean inCheck = isInCheck(whiteTurn);
            board.move(tr, tc, fr, fc); // Hoàn nguyên
            board.getBoard()[tr][tc] = captured;

            if (!inCheck) legal.add(mv);
        }

        // Xử lý nhập thành (castling)
        int piece = board.getPiece(row, col);
        if ((piece == R.drawable.vua_trang || piece == R.drawable.vua_den)
                && !isInCheck(whiteTurn) && !board.hasKingMoved(whiteTurn)) {
            int r = row;

            // Kingside (bên phải)
            if (!board.hasRookMoved(whiteTurn, true)
                    && board.getPiece(r, 5) == ChessBoard.EMPTY
                    && board.getPiece(r, 6) == ChessBoard.EMPTY) {
                // Giả lập vua đi qua cột 5 và 6
                board.move(r, 4, r, 5);
                boolean check1 = isInCheck(whiteTurn);
                board.move(r, 5, r, 4);

                board.move(r, 4, r, 6);
                boolean check2 = isInCheck(whiteTurn);
                board.move(r, 6, r, 4);

                if (!check1 && !check2) {
                    legal.add(new int[]{r, 6}); // Vua đi 2 ô bên phải
                }
            }

            // Queenside (bên trái)
            if (!board.hasRookMoved(whiteTurn, false)
                    && board.getPiece(r, 1) == ChessBoard.EMPTY
                    && board.getPiece(r, 2) == ChessBoard.EMPTY
                    && board.getPiece(r, 3) == ChessBoard.EMPTY) {
                board.move(r, 4, r, 3);
                boolean check1 = isInCheck(whiteTurn);
                board.move(r, 3, r, 4);

                board.move(r, 4, r, 2);
                boolean check2 = isInCheck(whiteTurn);
                board.move(r, 2, r, 4);

                if (!check1 && !check2) {
                    legal.add(new int[]{r, 2}); // Vua đi 2 ô bên trái
                }
            }
        }

        return legal;
    }


    /**
     * Sinh pseudo-legal moves (chưa kiểm chiếu)
     * Bao gồm en passant nhưng chưa có castling.
     */
    private List<int[]> getPseudoLegalMoves(int row, int col) {
        List<int[]> moves = new ArrayList<>();
        int piece = board.getPiece(row, col);
        if (piece == ChessBoard.EMPTY) return moves;
        boolean isWhite = laQuanTrang(piece);

        // Pawn
        if (piece == R.drawable.tot_trang || piece == R.drawable.tot_den) {
            int dir = (piece == R.drawable.tot_trang) ? -1 : 1;
            int start = (piece == R.drawable.tot_trang) ? 6 : 1;
            int r1 = row + dir;
            // Đi một bước
            if (inBounds(r1, col) && board.getPiece(r1, col) == ChessBoard.EMPTY) {
                moves.add(new int[]{r1, col});
                // Đi hai bước
                int r2 = row + 2 * dir;
                if (row == start && inBounds(r2, col) && board.getPiece(r2, col) == ChessBoard.EMPTY) {
                    moves.add(new int[]{r2, col});
                }
            }
            // Ăn chéo
            for (int dc = -1; dc <= 1; dc += 2) {
                int c2 = col + dc;
                if (inBounds(r1, c2)) {
                    int target = board.getPiece(r1, c2);
                    if (target != ChessBoard.EMPTY && laQuanTrang(target) != isWhite) {
                        moves.add(new int[]{r1, c2});
                    }
                }
            }
            // En passant (Bắt tốt qua đường)
            int[] ep = board.getEnPassantTarget();
            if (ep != null && row + dir == ep[0] && Math.abs(col - ep[1]) == 1) {
                moves.add(new int[]{ep[0], ep[1]});
            }
        }
        // Knight
        else if (piece == R.drawable.ma_trang || piece == R.drawable.ma_den) {
            int[][] off = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
            for (int[] o : off) {
                int r2 = row + o[0], c2 = col + o[1];
                if (inBounds(r2, c2)) {
                    int t = board.getPiece(r2, c2);
                    if (t == ChessBoard.EMPTY || laQuanTrang(t) != isWhite)
                        moves.add(new int[]{r2, c2});
                }
            }
        }
        // Bishop, Rook, Queen
        else if (piece == R.drawable.tuong_trang || piece == R.drawable.tuong_den
                || piece == R.drawable.xe_trang   || piece == R.drawable.xe_den
                || piece == R.drawable.hau_trang  || piece == R.drawable.hau_den) {
            int[][] dirs;
            if (piece == R.drawable.xe_trang || piece == R.drawable.xe_den)
                dirs = new int[][]{{-1,0},{1,0},{0,-1},{0,1}};
            else if (piece == R.drawable.tuong_trang || piece == R.drawable.tuong_den)
                dirs = new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}};
            else // Queen
                dirs = new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};
            for (int[] d : dirs) {
                int r2 = row + d[0], c2 = col + d[1];
                while (inBounds(r2, c2)) {
                    int t = board.getPiece(r2, c2);
                    if (t == ChessBoard.EMPTY) {
                        moves.add(new int[]{r2, c2});
                    } else {
                        if (laQuanTrang(t) != isWhite) moves.add(new int[]{r2, c2});
                        break;
                    }
                    r2 += d[0]; c2 += d[1];
                }
            }
        }
        // King (1 ô)
        else if (piece == R.drawable.vua_trang || piece == R.drawable.vua_den) {
            int[][] steps = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
            for (int[] s : steps) {
                int r2 = row + s[0], c2 = col + s[1];
                if (inBounds(r2, c2)) {
                    int t = board.getPiece(r2, c2);
                    if (t == ChessBoard.EMPTY || laQuanTrang(t) != isWhite)
                        moves.add(new int[]{r2, c2});
                }
            }
        }
        return moves;
    }

    /**
     * Kiểm tra ô (r,c) có đang bị tấn công bởi bên opposite không
     */
    private boolean isAttacked(int r, int c, boolean ownWhite) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int p = board.getPiece(i, j);
                if (p == ChessBoard.EMPTY || laQuanTrang(p) == ownWhite) continue;
                List<int[]> attacks = getPseudoLegalMoves(i, j);
                for (int[] a : attacks) {
                    if (a[0] == r && a[1] == c) return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra bên whiteTurn có đang bị chiếu không
     */
    public boolean isInCheck(boolean whiteTurn) {
        int kingRes = whiteTurn ? R.drawable.vua_trang : R.drawable.vua_den;
        int kr = -1, kc = -1;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board.getPiece(i, j) == kingRes) {
                    kr = i; kc = j; break;
                }
            }
            if (kr != -1) break;
        }
        if (kr < 0) return false;
        return isAttacked(kr, kc, whiteTurn);
    }

    /**
     * Kiểm tra còn nước đi hợp lệ (không chiếu tự thân)
     */
    public boolean hasAnyLegalMove(boolean whiteTurn) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int p = board.getPiece(r, c);
                if (p == ChessBoard.EMPTY || laQuanTrang(p) != whiteTurn) continue;
                if (!getLegalMoves(r, c).isEmpty()) return true;
            }
        }
        return false;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }
}