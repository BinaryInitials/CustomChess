package com.ozone.main;

import java.util.ArrayList;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Common.GameStatus;
import com.ozone.common.Move;
import com.ozone.common.State;
import com.ozone.engine.Engine;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;
import com.ozone.utility.Converters;

public class EngineSimulation {
	
	static int moves;
	static int score;
	final int LANDSLIDE_THRESHOLD = 11;
	public EngineSimulation(){
		moves = 0;
		score = 0;
	}
	
	public GameStatus start(Engine e1, Engine e2, Board initialBoard){
		if(e1.getTeam() == BoardUtil.WHITE) return start(e1, e2, false, false, initialBoard);
		return start(e2, e1, false, false, initialBoard);
	}
	
	public GameStatus start(Engine e1, Engine e2){
		e1.setTeam(1);
		e2.setTeam(-1);
		return start(e1, e2, false, false, null);
	}
	
	public State findBlunder(Engine e1, Engine e2){
		State state = new State();
		GameStatus status = GameStatus.ON_GOING;
		Board board = new Board();
		board.reset();
		int team = 1;
		List<Move> moveHistory = new ArrayList<Move>();
		int iteration = 0;
		while(!status.name().contains("CHECK_MATE")){
			iteration++;
			if(iteration > 120)return null;
			int boardStatusBeforeWhiteMove = BoardUtil.getBoardStatus(board.getBoard());
			
			Move whiteMove = e1.findMove(board, iteration, false, false, moveHistory);
			board.movePiece(whiteMove);
			moveHistory.add(whiteMove);
			System.out.println(iteration + "\t" + whiteMove + "\t" + BoardUtil.getBoardStatus(board.getBoard()) + "\t" + Converters.boardToFen(board, false));
			
			team = -team;
			status = Common.getGameStatus(board, moveHistory, team, 0);
			if(status == GameStatus.BLACK_IS_CHECK_MATE) {
				System.out.println("Black is Checkmate");
				state.setBoard(board);
				state.setMoveHistory(moveHistory);
				state.setMove(new Move(0,0,0,0,0));
				state.setFes(Converters.boardToFen(board, team));
				return state;
			}
			
			
			Move blackMove = e2.findMove(board, iteration, false, false, moveHistory);
			board.movePiece(blackMove);
			moveHistory.add(blackMove);
			System.out.println(iteration + "\t" + blackMove + "\t" + BoardUtil.getBoardStatus(board.getBoard()) + "\t" + Converters.boardToFen(board, true));
			
			int boardStatusAfterBlackMove = BoardUtil.getBoardStatus(board.getBoard());
			
			team = -team;
			status = Common.getGameStatus(board, moveHistory, team, 0);
			
			if(boardStatusBeforeWhiteMove >= 0 && boardStatusBeforeWhiteMove+1 < boardStatusAfterBlackMove){
				state.setBoard(board);
				state.setMoveHistory(moveHistory);
				state.setMove(blackMove);
				state.setFes(Converters.boardToFen(board, team));
				return state;
			}
			System.out.println("-------------------");
		}
		return null;
	}
	
	public GameStatus start(Engine e1, Engine e2, boolean isDeveloper, boolean showBoard, Board initialBoard){
		return start(e1, e2, isDeveloper, showBoard, initialBoard, false);
	}
	public GameStatus start(Engine e1, Engine e2, boolean isDeveloper, boolean showBoard, Board initialBoard, boolean isConsole){
		
		moves = 0;
		score = 0;
		Board board = new Board();
		if(initialBoard == null){
			board.reset();	
		}else{
			board.setBoard(initialBoard.getBoard());
		}
		
		List<Move> moveHistory = new ArrayList<Move>();
		GameStatus status = GameStatus.ON_GOING;
		
		int i=0;
		int turn = 1;
		int isNoConsecutiveCaptureOrPawnMovement = 0;
		while(isNoConsecutiveCaptureOrPawnMovement < 100){
			
			Move whiteMove = e1.findMove(board, i, false, isDeveloper, moveHistory);
			moveHistory.add(whiteMove);
			
			if(whiteMove == null || whiteMove.getPieceMoving() < 0){
				System.out.println("E1: " + e1.getClass().toString() + "\t" + e1.getTeam());
				System.out.println("E2: " + e2.getClass().toString() + "\t" + e2.getTeam());
				BoardUtil.displayBoard(board, isConsole);
				System.out.println(moveHistory);
				System.err.println("White move is null\nBoard status: " + BoardUtil.getBoardStatus(board.getBoard()) + "\nIterations: " + i);
				System.exit(-1);
			}
			
			if(!MoveUtil.isCaptureOrPawnMovement(board, whiteMove)){
				isNoConsecutiveCaptureOrPawnMovement++;
			}else{
				isNoConsecutiveCaptureOrPawnMovement = 0;
			}
			board.movePiece(whiteMove);
			score = BoardUtil.getBoardStatus(board);
			
			turn = BoardUtil.BLACK;
			status = Common.getGameStatus(board, moveHistory, turn, isNoConsecutiveCaptureOrPawnMovement);

			if(showBoard) {
				BoardUtil.displayBoard(board, isConsole);
				System.out.println("# " + i + "\tWhite move: " + whiteMove);
			}
			if(status != GameStatus.ON_GOING) {
				break;
			}

						
			Move blackMove = e2.findMove(board, i, false, isDeveloper, moveHistory);
			moveHistory.add(blackMove);
			
			if(blackMove == null || blackMove.getPieceMoving() > 0){
				System.out.println("E1: " + e1.getClass().toString() + "\t" + e1.getTeam());
				System.out.println("E2: " + e2.getClass().toString() + "\t" + e2.getTeam());
				BoardUtil.displayBoard(board, isConsole);
				System.out.println(moveHistory);
				System.err.println("Black move is null\nBoard status: " + BoardUtil.getBoardStatus(board) + "\nIterations: " + i);
				System.exit(-1);
			}
			
			if(!MoveUtil.isCaptureOrPawnMovement(board, blackMove)){
				isNoConsecutiveCaptureOrPawnMovement++;
			}else{
				isNoConsecutiveCaptureOrPawnMovement = 0;
			}
			board.movePiece(blackMove);
			score = BoardUtil.getBoardStatus(board);
			turn = BoardUtil.WHITE;
			status = Common.getGameStatus(board, moveHistory, turn, isNoConsecutiveCaptureOrPawnMovement);
			if(showBoard) {
				BoardUtil.displayBoard(board, isConsole);
				System.out.println("# " + i + "\tBlack move: " + blackMove);
			}
			if(status != GameStatus.ON_GOING) {
				break;
			}
			i++;
		}
		moves = i;
		if(isNoConsecutiveCaptureOrPawnMovement >= 100){
//			System.out.println("Stalemate by no pawn movement or capture");
			return GameStatus.STALE_MATE_NO_CAPTURE_OR_PAWN;
		}
		
		//TODO: Delete after testing... High frequency of stalemates with low number of moves. Very dubious
//		if(status.toString().startsWith("STALE_MATE") && moves < 20){
//			System.out.println("*SUSPICIOUS*");
//			System.out.println("ENGINE 1: " + e1.getClass().toString());
//			System.out.println("ENGINE 2: " + e2.getClass().toString());
//			System.out.println("MOVES: " + moves);
//			board.displayBoard();
//			if(turn == -1){
//				for(Move move : moveHistory){
//					System.out.println(move);
//				}
//			}
//			System.exit(-1);
//		}
		
		return status;
	}

	public int getMoves(){
		return moves;
	}

	public int getScore(){
		return score;
	}
}