package com.ozone.movements;

import java.util.ArrayList;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Piece;
import com.ozone.common.Square;
import com.ozone.utility.Converters;

public class BoardUtil {

	public static final String LINE = "|----|----|----|----|----|----|----|----|";
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	
	public final static int WHITE = 1;
	public final static int BLACK = -1;
	
	public final static int MAX_ROW = 8;
	public final static int MAX_COL = 8;

	public static final int KING_POSITION = MAX_COL-1-3;
	
	public static final int SPACE = 0; 

	public static final int PAWN = 100;
	public static final int KING = 2; 
	public static final int KNIGHT = 295; 
	public static final int BISHOP= 300; 
	public static final int ROOK = 500; 
	public static final int QUEEN = 900; 
	
	public static int getInitialPawnRow(int team) {
		return team > SPACE ? 1 : 6;
	}

	public static List<Piece> getAllPieces(Board board){
		return getAllPieces(board.getBoard());
	}
	public static List<Piece> getAllPieces(int[] board){
		int count = 0;
		List<Piece> pieces = new ArrayList<Piece>();
		for(int i=0;i<64;i++){
			if(board[i] != SPACE){
				count++;
				pieces.add(new Piece(board[i], i/8, i%8));
				if(count==32){
					return pieces;
				}
			}
		}
		return pieces;
	}
	
	public static List<Piece> getAllTeamPiecesInPieces(Board board, int team){
		return getAllTeamPiecesInPieces(board.getBoard(), team);
	}
	public static List<Piece> getAllTeamPiecesInPieces(int[] board, int team){
		int count = 0;
		List<Piece> pieces = new ArrayList<Piece>();
		for(int i=0;i<64;i++){
			if(board[i]*team > SPACE){
				count++;
				pieces.add(new Piece(board[i], i/8, i%8));
				if(count==16){
					return pieces;
				}
			}
		}
		return pieces;
	}
	public static List<Piece> getAllTeamPieces(Board board, int team){
		return getAllTeamPieces(board.getBoard(), team);
	}
	public static List<Piece> getAllTeamPieces(int[] board, int team){
		int count = 0;
		List<Piece> pieces = new ArrayList<Piece>();
		for(int i=0;i<64;i++){
			if(board[i]*team > SPACE){
				count++;
				pieces.add(new Piece(board[i], i/8, i%8));
				if(count==16){
					return pieces;
				}
			}
		}
		
		return pieces;
	}

	public static List<int[]> getAllTeamPinPieces(Board board, int team){
		int count = 0;
		List<int[]> pieces = new ArrayList<int[]>();
		if(team>0){
			for(int i=0;i<MAX_ROW;i++){
				for(int j=0;j<MAX_COL;j++){
					if(board.getPiece(i,j) > KNIGHT){
						count++;
						pieces.add(new int[]{board.getPiece(i,j), i, j});
						if(count==5) return pieces;
					}
				}
			}	

		}else{
			for(int i=MAX_ROW-1;i>-1;i--){
				for(int j=0;j<MAX_COL;j++){
					if(board.getPiece(i,j) < -KNIGHT){
						count++;
						pieces.add(new int[]{board.getPiece(i,j), i, j});
						if(count==5) return pieces;
					}
				}
			}	
			
		}
		
		
		return pieces;
	}
	public static List<Piece> getAllTeamHighPiecesInPieces(Board board, int team){
		return getAllTeamHighPiecesInPieces(board.getBoard(), team);
	}
	public static List<Piece> getAllTeamHighPiecesInPieces(int[] board, int team){
		int count = 0;
		List<Piece> pieces = new ArrayList<Piece>();
		for(int i=0;i<64;i++){
			if(board[i]*team > PAWN && board[i]*team != KING){
				count++;
				pieces.add(new Piece(board[i], i/8, i%8));
				if(count==7){
					return pieces;
				}
			}
		}
		return pieces;
	}
	
	public static List<int[]> getAllTeamHighPieces(Board board, int team){
		return getAllTeamHighPieces(board.getBoard(), team);
	}
	public static List<int[]> getAllTeamHighPieces(int[] board, int team){
		int count = 0;
		List<int[]> pieces = new ArrayList<int[]>();
		for(int i=0;i<64;i++){
			if(board[i]*team > PAWN && board[i]*team != KING){
				count++;
				pieces.add(new int[]{board[i], i/8, i%8});
				if(count==7){
					return pieces;
				}
			}
		}
		return pieces;
	}
	
	public static boolean isWhitePiece(int piece) {
		if(piece == SPACE){
			return false;
		}
		return piece == Math.abs(piece);
	}
	
	public static int transformToWhitePiece(int piece) {
		return Math.abs(piece);
	}
	
	public static int transformToBlackPiece(int piece) {
		return -transformToWhitePiece(piece);
	}
	
	public static boolean isPositionOutOfBounds(int yx[]) {
		return isPositionOutOfBounds(yx[0], yx[1]);
	}
	public static boolean isPositionOutOfBounds(int y, int x) {
		return y < 0 || y > 7 || x < 0 || x > 7;
	}
	public static int getBoardStatusWithoutPawn(int[] board) {
		int sum=0;
		List<int[]> whiteHighMaterial = getAllTeamHighPieces(board, WHITE);
		List<int[]> blackHighMaterial = getAllTeamHighPieces(board, BLACK);
		for(int[] piece : whiteHighMaterial)
			sum = sum + (int)Math.round(piece[0]/10.0);
		for(int[] piece : blackHighMaterial)
			sum = sum + (int)Math.round(piece[0]/10.0);
		
		return sum;
	}
	
	public static int getBoardStatus(Board board) {
		return getBoardStatus(board.getBoard());
	}
	public static int getBoardStatus(int[] board) {
		int sum=0;
		List<Piece> whiteMaterial = getAllTeamPieces(board, WHITE); 
		List<Piece> blackMaterial = getAllTeamPieces(board, BLACK); 
		for(Piece piece : whiteMaterial){
			sum = sum + (int)Math.round(piece.getValue()/100.0);
		}
		for(Piece piece : blackMaterial){
			sum = sum + (int)Math.round(piece.getValue()/100.0);
		}
		return sum;
	}
	
	public static void displaySquaresOnBoard(Board board, List<Square> squares) { 
		displaySquaresOnBoard(board, squares, false);
	}
	
	public static void displaySquaresOnBoard(Board board, List<Square> squares, boolean isConsole){
		for(int y=MAX_ROW-1;y>-1;y--){
			System.out.println("\t" + LINE);
			System.out.print((y+1) + "\t| ");
			for(int x=0;x<MAX_COL;x++){
				boolean foundSquare = false;
				for(Square square : squares){
					if(square.getX()==x && square.getY()==y){
						foundSquare = true;
						System.out.print("xX" + " | ");
						break;
					}
				}
				if(!foundSquare){
					System.out.print(printPiece(board.getPiece(y, x), isConsole) + " | ");
				}
			}
			System.out.println();
		}
		System.out.println("\t" + LINE);
		System.out.print("\t");
		for(int n=0;n<MAX_COL;n++){
			System.out.print("  " + (char)(n+65) + "  ");
		}
		System.out.println("\nStatus: " + getBoardStatus(board.getBoard()));
		System.out.println("FEN: " + Converters.boardToFen(board, false));
		System.out.println();
		
	}
		
	public static void displaySquareOnBoard(Board board, Square square){
		displaySquareOnBoard(board, square, false);
	}
	public static void displaySquareOnBoard(Board board, Square square, boolean isConsole){
		for(int y=MAX_ROW-1;y>-1;y--){
			System.out.println("\t" + LINE);
			System.out.print((y+1) + "\t| ");
			for(int x=0;x<MAX_COL;x++){
				if(square.getX()==x && square.getY()==y){
					System.out.print("xX" + " | ");
				}else{
					System.out.print(printPiece(board.getPiece(y, x), isConsole) + " | ");
				}
			}
			System.out.println();
		}
		System.out.println("\t" + LINE);
		System.out.print("\t");
		for(int n=0;n<MAX_COL;n++){
			System.out.print("  " + (char)(n+65) + "  ");
		}
		System.out.println("\nStatus: " + getBoardStatus(board.getBoard()));
		System.out.println();
	}
	
	public static void display(Board board) {
		displayBoard(board);
	}
	public static void displayBoard(Board board) {
		displayBoard(board, false);
	}
	
	public static void displayBoard(Board board, boolean isConsole) {
		for(int y=MAX_ROW-1;y>-1;y--){
			System.out.println("\t" + LINE);
			System.out.print((y+1) + "\t| ");
			for(int x=0;x<MAX_COL;x++){
				System.out.print(printPiece(board.getPiece(y, x), isConsole) + " | ");
			}
			System.out.println();
		}
		System.out.println("\t" + LINE);
		System.out.print("\t");
		for(int n=0;n<MAX_COL;n++){
			System.out.print("  " + (char)(n+65) + "  ");
		}
		System.out.println("\nStatus: " + getBoardStatus(board.getBoard()));
		System.out.println("FEN: " + Converters.boardToFen(board, false));
		System.out.println();
	}
	
	public static void displayBoardReverse(Board board){
		displayBoardReverse(board, false);
	}
	public static void displayBoardReverse(Board board, boolean isConsole){
		for(int y=0;y<MAX_ROW;y++){
			System.out.println("\t" + LINE);
			System.out.print((y+1) + "\t| ");
			for(int x=0;x<MAX_COL;x++){
				System.out.print(printPiece(board.getPiece(y, MAX_ROW-x-1), isConsole) + " | ");
			}
			System.out.println();
		}
		System.out.println("\t" + LINE);
		System.out.print("\t");
		for(int n=0;n<MAX_COL;n++){
			System.out.print("  " + (char)(MAX_COL-n-1+65) + "  ");
		}
		System.out.println("\nStatus: " + getBoardStatus(board.getBoard()));
		System.out.println("FEN: " + Converters.boardToFen(board, true));
		System.out.println();
	}

	public static String printPieceWithPosition(int[] piece){
		return printPiece(piece[0], false) + "@" + Common.convertIntArrayToSquare(piece[1], piece[2]); 
	}
	public static String printPiece(int piece){
		return printPiece(piece, false);
	}
	
	public static String printPiece(int piece, boolean isConsole){
		String pieceTxt = "";
		switch(Math.abs(piece)){
		case(PAWN):
			pieceTxt = "P";
			break;
		case(BISHOP):
			pieceTxt = "B";
			break;
		case(KNIGHT):
			pieceTxt = "N";
			break;
		case(ROOK):
			pieceTxt = "R";
			break;
		case(QUEEN):
			pieceTxt = "Q";
			break;
		case(KING):
			pieceTxt = "K";
			break;
		case (SPACE):
			return "  ";
		}
		if(!isConsole){
			pieceTxt = isWhitePiece(piece) ? "w" + pieceTxt : "k" + pieceTxt;
		}else{
			pieceTxt = (isWhitePiece(piece) ? ANSI_GREEN + " " + pieceTxt : ANSI_RED + " " + pieceTxt) + ANSI_RESET;
		}
		return pieceTxt;
	}

	public static String printFullPiece(int piece){
		String pieceTxt = "";
		switch(Math.abs(piece)){
		case(PAWN):
			pieceTxt = "Pawn";
		break;
		case(BISHOP):
			pieceTxt = "Bishop";
		break;
		case(KNIGHT):
			pieceTxt = "Knight";
		break;
		case(ROOK):
			pieceTxt = "Rook";
		break;
		case(QUEEN):
			pieceTxt = "Queen";
		break;
		case(KING):
			pieceTxt = "King";
		break;
		case (SPACE):
			return "  ";
		}
		pieceTxt = isWhitePiece(piece) ? "white " + pieceTxt : "black " + pieceTxt;
		return pieceTxt;
	}
}