package com.ozone.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;

public class Common {
	private static final String MOVEMENT_PATTERN = "[A-Ha-h][1-8]-?[A-Ha-h][1-8]";
	private static final String SQUARE_PATTERN = "[A-Ha-h][1-8]";
	private static final String PIECE_PATTERN = "[wk][KQRBNP]";
	public enum Difficulty {
		EASY,
		MEDIUM,
		HARD
	};
	public enum GameStatus {
		WHITE_IS_CHECK_MATE,
		BLACK_IS_CHECK_MATE,
		STALE_MATE_THREEFOLD_REP,
		STALE_MATE_NO_CAPTURE_OR_PAWN,
		STALE_MATE_NO_MOVEMENTS,
		STALE_MATE_INSUFFICIENT_MATERIAL,
		ON_GOING,
	};
	public static String readTeamDepiction(){
		BufferedReader reader;
		reader = new BufferedReader(new InputStreamReader(System.in));
		String userInput = "";
		try {
			userInput = reader.readLine();
			if(userInput!= null && userInput.toLowerCase().matches(".*(exit|quit).*")){
				System.out.println("Good bye!");
				System.exit(0);
			}
//			reader.close();
		} catch (IOException e) {
		}
		return userInput;
	}
	
	public static String readSquareEdit(boolean developerMode) {
		BufferedReader reader;
		reader = new BufferedReader(new InputStreamReader(System.in));
		String userInput = "";
		try {
			userInput = reader.readLine();
			if(userInput != null && userInput.toLowerCase().matches(".*(exit|quit).*")){
				System.out.println("Good bye!");
				System.exit(0);
			}
//			reader.close();
		} catch (IOException e) {
		}
		if(userInput.matches(".*" + SQUARE_PATTERN + ".*")){
			return userInput.replaceAll(".*("  + SQUARE_PATTERN + ").*", "$1");
		}else if(developerMode && userInput.toLowerCase().startsWith("edit")){
			return userInput;
		}else{
			return "Error: Please enter a square in algebraic format, e.g. a2";
		}
	}

	public static String readPieceEdit(boolean developerMode) {
		BufferedReader reader;
		reader = new BufferedReader(new InputStreamReader(System.in));
		String userInput = "";
		try {
			userInput = reader.readLine();
			if(userInput != null && userInput.toLowerCase().matches(".*(exit|quit).*")){
				System.out.println("Good bye!");
				System.exit(0);
			}
//			reader.close();
		} catch (IOException e) {
		}
		if(userInput.matches(".*" + PIECE_PATTERN + ".*")){
			return userInput.replaceAll(".*("  + PIECE_PATTERN + ").*", "$1");
		}else if(userInput.matches(".*(space|empty| +).*")){
			return "  ";
		}else{
			return "Error: Please enter a square in algebraic format, e.g. a2";
		}
	}
	public static String readInput(boolean developerMode) {
		System.out.print("Your Move: ");
		BufferedReader reader;
		reader = new BufferedReader(new InputStreamReader(System.in));
		String userInput = "";
		try {
			userInput = reader.readLine();
			if(userInput != null && userInput.toLowerCase().matches(".*(exit|quit).*")){
				System.out.println("Good bye!");
				System.exit(0);
			}
//			reader.close();
		} catch (IOException e) {
		}
		if(userInput.matches(".*" + MOVEMENT_PATTERN + ".*")){
			return userInput.replaceAll(".*("  + MOVEMENT_PATTERN + ").*", "$1");
		}else if(developerMode && userInput.toLowerCase().startsWith("edit")){
			return userInput;
		}else{
			return "Error: Please enter move in algebraic format, e.g. a2-a4 ";
		}
	}
	
	public static int[] convertInputToSquare(String inputSquare){
		int x = (int)(inputSquare.toUpperCase().trim().charAt(0)) - 65;
		int y = Integer.valueOf(inputSquare.toUpperCase().trim().substring(1, 2))-1;
		return new int[]{y, x};
	}
	
	public static String convertIntArrayToSquare(int[] yx){
		int y = yx[0]; 
		int x = yx[1];
		return convertIntArrayToSquare(y,x);
	}
	public static String convertIntArrayToSquare(int y, int x){
		char row = (char)((""+(y+1)).charAt(0));
		char col = (char)(x+65);
		return ""+col + row;
		
	}
	public static Move convertInputToMove(int piece, String inputMove){
		String y1x1 = inputMove.toUpperCase().trim().replaceAll("^([A-H][1-8]).*","$1");
		String y2x2 = inputMove.toUpperCase().trim().replaceAll(".*([A-H][1-8])$","$1");
		int x1 = (int)(y1x1.charAt(0)) - 65;
		int y1 = Integer.valueOf(y1x1.substring(1, 2))-1;
		int x2 = (int)(y2x2.charAt(0)) - 65;
		int y2 = Integer.valueOf(y2x2.substring(1, 2))-1;
		return new Move(piece, y1, x1, y2, x2);		
	}
	public static Move convertInputToMove(Board board, String inputMove){
		String y1x1 = inputMove.toUpperCase().trim().replaceAll("^([A-H][1-8]).*","$1");
		String y2x2 = inputMove.toUpperCase().trim().replaceAll("^[A-H][1-8]([A-H][1-8]).*","$1");
		int x1 = (int)(y1x1.charAt(0)) - 65;
		int y1 = Integer.valueOf(y1x1.substring(1, 2))-1;
		int x2 = (int)(y2x2.charAt(0)) - 65;
		int y2 = Integer.valueOf(y2x2.substring(1, 2))-1;
		return new Move(board.getPiece(y1, x1), y1, x1, y2, x2);
	}
	
	public static int convertPieceTxtToPiece(String pieceTxt){
		int[] pieces = new int[]{BoardUtil.KING, BoardUtil.QUEEN, BoardUtil.ROOK, BoardUtil.BISHOP, BoardUtil.KNIGHT, BoardUtil.PAWN, BoardUtil.SPACE};
		for(int piece : pieces){
			if(BoardUtil.printPiece(piece).equals(pieceTxt)){
				return piece;
			}else if(BoardUtil.printPiece(-piece).equals(pieceTxt)){
				return -piece;
			}
		}
		return BoardUtil.SPACE;
	}
	
	public static String captureHumanMove(boolean developerMode){
		String input = readInput(developerMode);
		while(input.startsWith("Error")){
			System.out.println(input);
			input = Common.readInput(developerMode);
		}
		return input;
	}
	public static Move validateMove(Board board, Move humanMove, int team, boolean developerMode) {
		Move validMove = humanMove;
		boolean isMoveValid = MoveUtil.isMoveValid(board, validMove);
		if(board.getPiece(validMove.getFromPos()[0], validMove.getFromPos()[1]) * team < 0){
			System.out.println("Error: Invalid Move, the piece belongs to the opposite team");
			isMoveValid = false;
		}
		while(!isMoveValid){
			if(BoardUtil.isPositionOutOfBounds(validMove.getFromPos()[0], validMove.getFromPos()[1])){
				System.out.println("Error: Invalid Move, this move leads to a position that is out of bounds");
			}else if(board.getPiece(validMove.getFromPos()[0], validMove.getFromPos()[1]) == BoardUtil.SPACE){
				System.out.println("Error: Invalid Move, there are no pieces found in position provided");
			}else if(MoveUtil.isCheck(validMove.updateBoard(board), team)){
				System.out.println("Error: Invalid Move, the move still puts you in check");
			}else {
				System.out.println("Error: Invalid Move, the piece cannot be moved there");
			}
			String input = Common.readInput(developerMode);
			while(input.startsWith("Error")){
				input = Common.readInput(developerMode);
			}
			validMove = Common.convertInputToMove(board, input);
			isMoveValid = MoveUtil.isMoveValid(board, validMove);
			if(board.getPiece(validMove.getFromPos()[0], validMove.getFromPos()[1]) * team < 0){
				System.out.println("Error: Invalid Move, the piece belongs to the opposite team");
				isMoveValid = false;
			}
		}
		return validMove;
	}

	/*
	 * If it's not your turn to move, it doesn't matter if you can't move, it's not stalemate. 
	 * Your opponent must move first. This is why this method needs to be dependent on team.
	 */
	public static GameStatus getGameStatus(Board board, List<Move> moveHistory, int team, int noPawnOrCaptureMoveCounter) {
		GameStatus gameStatus = GameStatus.ON_GOING;
		if(MoveUtil.isRepeatedThreeTimes(moveHistory)){
			return GameStatus.STALE_MATE_THREEFOLD_REP;
		}else if(noPawnOrCaptureMoveCounter >= 100){
			return GameStatus.STALE_MATE_NO_CAPTURE_OR_PAWN;
		}else if(MoveUtil.isCheckMate(board, 1)){
			gameStatus = GameStatus.WHITE_IS_CHECK_MATE;
		}else if(MoveUtil.isCheckMate(board, -1)){
			gameStatus = GameStatus.BLACK_IS_CHECK_MATE;
		}else if(moveHistory.size() > 0 && moveHistory.get(moveHistory.size()-1).equals(MoveUtil.STALE_MATE)){
			gameStatus = GameStatus.STALE_MATE_NO_MOVEMENTS;
		}else if(MoveUtil.isStaleMate(board, team)){
			gameStatus = GameStatus.STALE_MATE_NO_MOVEMENTS;
		}
		return gameStatus;
	}
	
	public static int[] convertToPieceArray(int piece, String square){
		int[] yx = convertInputToSquare(square);
		return new int[]{piece, yx[0], yx[1]};
	}

	public static String checkForEdit(Board board, String input, boolean isDeveloperMode, boolean isConsole) {
		String edit = input;
		while(edit.toLowerCase().startsWith("edit")){
			System.out.println("Please enter the square that needs edited: ");
			String squareEdit = Common.readSquareEdit(isDeveloperMode);
			int[] square = Common.convertInputToSquare(squareEdit);
			System.out.println("Square detected: " + square[0] + ", " + square[1]);
			
			System.out.println("What would you like the square to be filled with?");
			System.out.println("{wK, kK, wQ, kQ, wR, kR, wB, kB, wN, kN, wP, kP, space}");
			String pieceEdit = Common.readPieceEdit(isDeveloperMode);
			int piece = Common.convertPieceTxtToPiece(pieceEdit);
			System.out.println("Piece detected: " + BoardUtil.printFullPiece(piece));
			
			board.setPiece(piece, square[0], square[1]);
			BoardUtil.displayBoard(board, isConsole);
			System.out.println("Editing completed, please enter a move: ");
			edit = Common.captureHumanMove(isDeveloperMode);
		}
		return edit == null ? input : edit;
	}
}