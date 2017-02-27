package com.ozone.utility;

import com.ozone.common.Board;
import com.ozone.movements.BoardUtil;

public class Converters {
	private static final String WHITE_FEN_SUFFIX = " w KQkq - 0 1";
	private static final String BLACK_FEN_SUFFIX = " b KQkq - 0 1";
	private static final String FEN_SUFFIX = " . KQkq - 0 1";

	public static String boardToFen(Board board, int teamToMove){
		return boardToFen(board, teamToMove == BoardUtil.WHITE);
	}
	
	public static String boardToFenSimplified(Board board){
		return boardToFen(board, true).replaceAll(FEN_SUFFIX + "$", "");
	}
	
	public static String boardToFen(Board board, boolean whiteToMove){
		String fen = "";
		for(int row=0;row<8;row++) {
			String fenRow = "";
			int space=0;
			for(int i=8*row;i<8+8*row;i++) {
				int piece = board.getPiece(i);
				if(piece == 0){
					space++;
				}else if(space > 0){
					fenRow = fenRow + space + pieceToFenPiece(piece);
					space = 0;
				}else{
					fenRow = fenRow + pieceToFenPiece(piece);
				}
			}
			if(space > 0){
				fenRow = fenRow + space;
			}
			fen = fenRow + "/" + fen;
		}
		return fen.substring(0, fen.length()-1) + (whiteToMove?WHITE_FEN_SUFFIX:BLACK_FEN_SUFFIX);
	}
	
	
	
	public static String pieceToFenPiece(int piece){
		if(piece == BoardUtil.SPACE){
			return "";
		}
		char fenChar = ' ';
		int team = piece/Math.abs(piece);
		switch(Math.abs(piece)){
		case BoardUtil.PAWN:
			fenChar = 'P';
			break;
		case BoardUtil.KNIGHT:
			fenChar = 'N';
			break;
		case BoardUtil.BISHOP:
			fenChar = 'B';
			break;
		case BoardUtil.ROOK:
			fenChar = 'R';
			break;
		case BoardUtil.QUEEN:
			fenChar = 'Q';
			break;
		case BoardUtil.KING:
			fenChar = 'K';
			break;
		}
		fenChar = (char)(((int)fenChar)+16*(1-team));
		return fenChar + "";
	}
	
	public static int fenPieceToPiece(char fenPiece){
		if((int)fenPiece < 57){
			return BoardUtil.SPACE;
		}
		int piece = BoardUtil.SPACE;
		char normPiece = fenPiece;
		int team = BoardUtil.WHITE;
		if((int)normPiece > 90){
			normPiece = (char)((int)normPiece - 32);
			team = -team;
		}
		switch(normPiece){
		case 'P':
			piece = BoardUtil.PAWN;
			break;
		case 'N':
			piece = BoardUtil.KNIGHT;
			break;
		case 'B':
			piece = BoardUtil.BISHOP;
			break;
		case 'R':
			piece = BoardUtil.ROOK;
			break;
		case 'Q':
			piece = BoardUtil.QUEEN;
			break;
		case 'K':
			piece = BoardUtil.KING;
			break;
		}
		return piece*team;
	}
	
	public static Board fenToBoard(String fen){
		String fenChopped = fen.replaceAll(FEN_SUFFIX, ""); 
		Board board = new Board();
		String[] rows = fenChopped.split("/");
		for(int i=0;i<BoardUtil.MAX_ROW;i++){
			String row = rows[i];
			int incrementer = 0;
			for(int j=0;j<row.length();j++){
				if(row.substring(j, j+1).matches("^[0-9]$")){
					int numSpaces = Integer.valueOf(row.substring(j, j+1));
					for(int z=0;z<numSpaces;z++){
						board.setPiece(BoardUtil.SPACE, 7-i, incrementer);
						incrementer++;
					}
				}else{
					board.setPiece(fenPieceToPiece(row.charAt(j)), 7-i, incrementer);
					incrementer++;
				}
			}
		}
		return board;
	}
}