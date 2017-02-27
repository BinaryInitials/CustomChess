package com.ozone.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Common.GameStatus;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.engine.Engine;
import com.ozone.engine.EngineMinMaxNoMateDectionExperiment;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH3;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.engine.EngineNewFastFast;
import com.ozone.mate.KingQueen;
import com.ozone.movements.AdvancedMoveUtil;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;
import com.ozone.movements.MoveUtil.Status;

public class TestTime {
	
	EngineMinMaxNoMateDectionMAXDEPTH3 e1 = new EngineMinMaxNoMateDectionMAXDEPTH3(1); 
	EngineMinMaxNoMateDectionMAXDEPTH5 e2 = new EngineMinMaxNoMateDectionMAXDEPTH5(-1); 
	
	@Test
	public void ETamInFive() {
		Board board = new Board("rnbq1rk1/pppp1ppp/8/8/2B5/2B5/P4PPP/R2Q1RK1 w - - 0 1");
		Engine e = new EngineMinMaxNoMateDectionExperiment(1);
		Move move = e.findMove(board);
		Move expected = new Move(board, "D1G4");
		assertEquals(expected, move);
	}
	
	@Test
	public void KQK(){
		Board board = new Board();
		board.setPiece(-BoardUtil.KING, "d4");
		board.setPiece(BoardUtil.KING, "d1");
		board.setPiece(BoardUtil.QUEEN, "c1");
		int i=20;
		
		List<Move> moveHistory = new ArrayList<Move>();
		boolean isStaleMate = false;
		Date tic = new Date();
		while(!MoveUtil.isCheckMate(board, BoardUtil.BLACK)){
			Move whiteMove = e1.findMove(board, i, false, true, new ArrayList<Move>());
			moveHistory.add(whiteMove);
			board.movePiece(whiteMove);
			BoardUtil.displayBoard(board);
			if(MoveUtil.isCheckMate(board, BoardUtil.BLACK)){
				break;
			}else if(isRepeatedThreeTimes(moveHistory)){
				isStaleMate = true;
				System.out.println("STALEMATE");
				break;
			}
			Move blackMove = e2.findMove(board, i, false, true, new ArrayList<Move>());
			board.movePiece(blackMove);
			moveHistory.add(blackMove);
			BoardUtil.displayBoard(board);
			System.out.println(i);
			i++;
		}
		long ellapsedTime = (new Date().getTime()-tic.getTime());
		assertFalse("Stalemate", isStaleMate);
		assertTrue("Took too long to solve for mate", ellapsedTime < 10000);
		assertTrue("Took too many iterations to solve for mate", i < 50);
		
	}
	
	@Test
	public void whyIsntTheBishopTakingTheRook(){
		Board board = new Board();
		board.reset();
		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "d7d5"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "e7e6"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.BISHOP, "f8b4"));

		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "d2d4"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "e2e3"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "b2b3"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "c2c4"));

		board.movePiece(Common.convertInputToMove(-BoardUtil.BISHOP, "b4c3"));
		board.movePiece(Common.convertInputToMove(BoardUtil.KING, "e1e2"));
		board.setPiece(BoardUtil.SPACE, "b1");
		board.setPiece(BoardUtil.SPACE, "g8");
		board.setPiece(BoardUtil.SPACE, "c1");
		
		BoardUtil.displayBoard(board);
		Move move = e2.findMove(board, 8, false, true, new ArrayList<Move>());
		System.out.println(move);
		
	}

	@Test
	public void whyIsntTheKnightForking(){
		Board board = new Board();
		board.reset();
		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "d7d5"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "e7e6"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.KNIGHT, "b8b4"));
		
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "d2d4"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "e2e3"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "b2b3"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "c2c4"));
		
		board.movePiece(Common.convertInputToMove(-BoardUtil.BISHOP, "c8f5"));
		board.setPiece(BoardUtil.SPACE, "b1");
		board.setPiece(BoardUtil.SPACE, "g8");
		board.setPiece(BoardUtil.SPACE, "c1");
		
		BoardUtil.displayBoard(board);
		Move move = e2.findMove(board, 8, false, true, new ArrayList<Move>());
		System.out.println(move);
		board.movePiece(move);
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "e1e2"));
		Move move2 = e2.findMove(board, 8, false, true, new ArrayList<Move>());
		System.out.println(move2);
		
	}
	
	@Test
	public void testWeirdOpening2(){
		Board board = new Board();
		board.reset();
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "d2d4"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.KNIGHT, "g8f6"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "c2c4"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "d7d5"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "b2b3"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "e7e6"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "e2e3"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.BISHOP, "f8b4"));
		board.movePiece(Common.convertInputToMove(BoardUtil.BISHOP, "c1d2"));
		BoardUtil.displayBoard(board);
		Move move = e2.findMove(board, 3, false, true, new ArrayList<Move>());
		System.out.println(move);
		assertFalse("The Bishop should have moved", move.updateBoard(board).getPiece(3, 1) == -BoardUtil.BISHOP);
		
	}
	
	@Test
	public void testTradeWhereForkFollows(){
		Board board = new Board();
		board.reset();
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "e2e4"));
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "d2d4"));
		board.movePiece(Common.convertInputToMove(BoardUtil.KNIGHT, "g1f3"));
		board.movePiece(Common.convertInputToMove(BoardUtil.KNIGHT, "b1c3"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "e7e6"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.KNIGHT, "g8f6"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.KNIGHT, "f6d5"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.BISHOP, "f8b4"));
		BoardUtil.display(board);
		Move move = e2.findMove(board, 4, false, true, new ArrayList<Move>());
		board.movePiece(move);
		board.movePiece(Common.convertInputToMove(BoardUtil.PAWN, "b2c3"));
//		BoardUtil.displayBoard(board);
		Move move2 = e2.findMove(board, 4, false, true, new ArrayList<Move>());
		System.out.println(move2);
	}
	
	private boolean isRepeatedThreeTimes(List<Move> moveHistory) {
		if(moveHistory.size() < 8){
			return false;
		}
		int lastIndex = moveHistory.size()-1;
		if(moveHistory.get(lastIndex).equals(moveHistory.get(lastIndex-4)) &&
			moveHistory.get(lastIndex-1).equals(moveHistory.get(lastIndex-5)) &&
			moveHistory.get(lastIndex-2).equals(moveHistory.get(lastIndex-6)) &&
			moveHistory.get(lastIndex-3).equals(moveHistory.get(lastIndex-7))
				){
			return true;
		}
		return false;
	}
	
	@Test
	public void testingKingQueenKingEdgeCase1(){
		Board board = new Board();
		board.setPiece(-BoardUtil.KING, "b6");
		board.setPiece(BoardUtil.KING, "b4");
		board.setPiece(-BoardUtil.QUEEN, "h3");
		BoardUtil.displayBoard(board);
		Move move = KingQueen.findMove(board, BoardUtil.BLACK);
		System.out.println(move);
		assertFalse("The Queen commited suicide", MoveUtil.isThreatenedVeryNaiveApproach(move.updateBoard(board), move.getPieceMoving(), move.getToPos()[0], move.getToPos()[1]));
	}
	
	@Test
	public void testingKingQueenKingEdgeCase2(){
		Board board = new Board();
		board.setPiece(-BoardUtil.KING, "a6");
		board.setPiece(BoardUtil.KING, "b4");
		board.setPiece(BoardUtil.QUEEN, "h7");
		BoardUtil.displayBoard(board);
		Move move = KingQueen.findMove(board, BoardUtil.WHITE);
		System.out.println(move);
		assertEquals("The king should have moved in a different direction", new Move(BoardUtil.KING, 3, 1, 4, 2), move);
	}
	
	
	@Test
	public void testDidNotSeeThatComingCheckMate(){
		Engine engine = new EngineMinMaxNoMateDectionMAXDEPTH5(BoardUtil.BLACK);
//		EfficientEngine engine = new EfficientEngine(BoardUtil.BLACK);
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.ROOK, "e8");
		board.setPiece(-BoardUtil.KING, "g8");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "c7");
		board.setPiece(-BoardUtil.PAWN, "f7");
		board.setPiece(-BoardUtil.PAWN, "h7");
		board.setPiece(-BoardUtil.PAWN, "g6");
		board.setPiece(-BoardUtil.PAWN, "e6");
		board.setPiece(-BoardUtil.KNIGHT, "d7");
		board.setPiece(-BoardUtil.QUEEN, "c3");
		
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.QUEEN, "d1");
		board.setPiece(BoardUtil.ROOK, "f1");
		board.setPiece(BoardUtil.KING, "g1");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.PAWN, "h2");
		board.setPiece(BoardUtil.PAWN, "b3");
		board.setPiece(BoardUtil.BISHOP, "d3");
		board.setPiece(BoardUtil.KNIGHT, "f3");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "e5");
		board.setPiece(BoardUtil.BISHOP, "h4");
		
		board.movePiece(Common.convertInputToMove(board, "d1d2"));
		board.movePiece(Common.convertInputToMove(board, "c3c6"));
		board.movePiece(Common.convertInputToMove(board, "h4f6"));
		board.movePiece(Common.convertInputToMove(board, "d7f6"));
		board.movePiece(Common.convertInputToMove(board, "e5f6"));
//		board.movePiece(Common.convertInputToMove(board, "c6d6"));
//		board.movePiece(Common.convertInputToMove(board, "d2h6"));
		BoardUtil.displayBoard(board);
		System.out.println(BoardUtil.getBoardStatus(board));
		Date tic = new Date();
		Move move = engine.findMove(board, 21, false, true, new ArrayList<Move>());
		System.out.println(move + "\t" + (new Date().getTime()-tic.getTime()) + "ms");
		board.movePiece(move);
		BoardUtil.displayBoard(board);
		System.out.println(BoardUtil.getBoardStatus(board));
	}
	
	@Test
	public void testWhyNoPawnCaptureInsteadRookMovedWhichLeadToCheckMate(){
		
		Engine engine = new EngineMinMaxNoMateDectionMAXDEPTH5(BoardUtil.BLACK);
		
		Board board = new Board();
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.ROOK, "h1");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b3");
		board.setPiece(BoardUtil.PAWN, "c4");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.PAWN, "h2");
		board.setPiece(BoardUtil.PAWN, "f6");
		board.setPiece(BoardUtil.BISHOP, "b1");
		board.setPiece(BoardUtil.BISHOP, "d2");
		board.setPiece(BoardUtil.KNIGHT, "c3");
		board.setPiece(BoardUtil.KNIGHT, "g1");
		board.setPiece(BoardUtil.QUEEN, "d3");
		board.setPiece(BoardUtil.KING, "e1");
		
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "f8");
		board.setPiece(-BoardUtil.PAWN, "a7");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "c7");
		board.setPiece(-BoardUtil.PAWN, "d5");
		board.setPiece(-BoardUtil.PAWN, "e6");
		board.setPiece(-BoardUtil.PAWN, "f7");
		board.setPiece(-BoardUtil.PAWN, "g7");
		board.setPiece(-BoardUtil.PAWN, "h7");
		board.setPiece(-BoardUtil.BISHOP, "b4");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.KNIGHT, "c6");
		board.setPiece(-BoardUtil.QUEEN, "d8");
		board.setPiece(-BoardUtil.KING, "g8");
		
		BoardUtil.displayBoard(board);
		Move blackMove = engine.findMove(board, 20, false, true, new ArrayList<Move>());
		System.out.println("Is the last castle side pawn threatened? " + MoveUtil.isPieceThreatened(board, new int[]{-BoardUtil.PAWN, 6, 7}));
		System.out.println(blackMove);
	}
	
	@Test
	public void testFreePieceVersusLosingAPiece(){
		Engine engine = new EngineMinMaxNoMateDectionMAXDEPTH5(BoardUtil.BLACK);
		
		Board board = new Board();
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.ROOK, "h1");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b3");
		board.setPiece(BoardUtil.PAWN, "c4");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.PAWN, "h2");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "e5");
		board.setPiece(BoardUtil.BISHOP, "b1");
		board.setPiece(BoardUtil.BISHOP, "d2");
		board.setPiece(BoardUtil.KNIGHT, "c3");
		board.setPiece(BoardUtil.KNIGHT, "g1");
		board.setPiece(BoardUtil.QUEEN, "c2");
		board.setPiece(BoardUtil.KING, "e1");
		
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "f8");
		board.setPiece(-BoardUtil.PAWN, "a7");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "c7");
		board.setPiece(-BoardUtil.PAWN, "d5");
		board.setPiece(-BoardUtil.PAWN, "e6");
		board.setPiece(-BoardUtil.PAWN, "f7");
		board.setPiece(-BoardUtil.PAWN, "g7");
		board.setPiece(-BoardUtil.PAWN, "h7");
		board.setPiece(-BoardUtil.BISHOP, "b4");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.KNIGHT, "c6");
		board.setPiece(-BoardUtil.KNIGHT, "f6");
		board.setPiece(-BoardUtil.QUEEN, "d8");
		board.setPiece(-BoardUtil.KING, "g8");
		
		BoardUtil.displayBoard(board);
		Move blackMove = engine.findMove(board, 20, false, true, new ArrayList<Move>());
		//That is the right thing to do: Knight takes free piece. King side knight cannot move due to check mate threat.
		System.out.println(blackMove);
		board.movePiece(blackMove);
		board.movePiece(Common.convertInputToMove(board, "c2d3"));
		blackMove = engine.findMove(board, 20, false, true, new ArrayList<Move>());
		board.movePiece(blackMove);
		
	}
	
	@Test
	public void undetectedForcedMate(){
		Engine engine = new EngineMinMaxNoMateDectionMAXDEPTH5(BoardUtil.BLACK); 
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "e8");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.BISHOP, "d6");
		board.setPiece(-BoardUtil.KING, "g8");
		board.setPiece(-BoardUtil.KNIGHT, "c6");
		board.setPiece(-BoardUtil.QUEEN, "a5");
		board.setPiece(-BoardUtil.PAWN, "a7");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "c5");
		board.setPiece(-BoardUtil.PAWN, "d7");
		board.setPiece(-BoardUtil.PAWN, "f6");
		board.setPiece(-BoardUtil.PAWN, "f7");
		
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.ROOK, "h1");
		board.setPiece(BoardUtil.BISHOP, "b1");
		board.setPiece(BoardUtil.KING, "e1");
		board.setPiece(BoardUtil.QUEEN, "c2");
		board.setPiece(BoardUtil.KNIGHT, "d2");
		board.setPiece(BoardUtil.KNIGHT, "g6");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b2");
		board.setPiece(BoardUtil.PAWN, "c3");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "e3");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		BoardUtil.displayBoard(board);
		board.movePiece(Common.convertInputToMove(-BoardUtil.QUEEN, "a5b5"));
		board.movePiece(Common.convertInputToMove(BoardUtil.KNIGHT, "g6h8"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.ROOK, "e8e7"));
		board.movePiece(Common.convertInputToMove(BoardUtil.QUEEN, "c2h7"));
		board.movePiece(Common.convertInputToMove(-BoardUtil.KING, "g8f8"));
		board.movePiece(Common.convertInputToMove(BoardUtil.KNIGHT, "h8f7"));
//		board.movePiece(Common.convertInputToMove(-BoardUtil.PAWN, "f7g6"));
//		board.movePiece(Common.convertInputToMove(BoardUtil.QUEEN, "c2g6"));
		BoardUtil.displayBoard(board);
		Date tic = new Date();
		Move move = engine.findMove(board, 15, false, true, new ArrayList<Move>());
		System.out.println(move);
		System.out.println((new Date().getTime() - tic.getTime()) + "ms");
		
//		Move move = engine.findMove(board, 15, false, true, new ArrayList<Move>());
//		board.movePiece(move);

		
//		System.out.println(move);
	}
	
	@Test
	public void testCrash(){
		Engine engine = new EngineMinMaxNoMateDectionMAXDEPTH5(BoardUtil.BLACK);
		Board board = new Board();
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.ROOK, "h1");
		board.setPiece(BoardUtil.BISHOP, "c1");
		board.setPiece(BoardUtil.BISHOP, "e2");
		board.setPiece(BoardUtil.KING, "e1");
		board.setPiece(BoardUtil.KNIGHT, "e3");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "c2");
		board.setPiece(BoardUtil.PAWN, "d2");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.PAWN, "h2");
		board.setPiece(BoardUtil.PAWN, "b3");
		board.setPiece(BoardUtil.PAWN, "e4");
		
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "g8");
		board.setPiece(-BoardUtil.BISHOP, "f8");
		board.setPiece(-BoardUtil.PAWN, "a7");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "c5");
		board.setPiece(-BoardUtil.PAWN, "e5");
		board.setPiece(-BoardUtil.PAWN, "f7");
		board.setPiece(-BoardUtil.PAWN, "h5");
		board.setPiece(-BoardUtil.KING, "e7");
		board.setPiece(-BoardUtil.KNIGHT, "d7");
		
		BoardUtil.displayBoard(board);
		
		Move move = engine.findMove(board, 13, false, true, new ArrayList<Move>());
		System.out.println(move);
	}
	
	@Test
	public void testCheckProbabilityWithMateVeryLikely(){
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "e8");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.BISHOP, "d6");
		board.setPiece(-BoardUtil.KING, "g8");
		board.setPiece(-BoardUtil.KNIGHT, "c6");
		board.setPiece(-BoardUtil.QUEEN, "a5");
		board.setPiece(-BoardUtil.PAWN, "a7");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "c5");
		board.setPiece(-BoardUtil.PAWN, "d7");
		board.setPiece(-BoardUtil.PAWN, "f6");
		board.setPiece(-BoardUtil.PAWN, "f7");
		
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.ROOK, "h1");
		board.setPiece(BoardUtil.BISHOP, "b1");
		board.setPiece(BoardUtil.KING, "e1");
		board.setPiece(BoardUtil.QUEEN, "c2");
		board.setPiece(BoardUtil.KNIGHT, "d2");
		board.setPiece(BoardUtil.KNIGHT, "g6");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b2");
		board.setPiece(BoardUtil.PAWN, "c3");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "e3");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		BoardUtil.display(board);
//		Date tic = new Date();
		//TODO: Come up with a way to do this;
//		System.out.println("Mating Probability: " + p + "\tElapsed time: " + (new Date().getTime()-tic.getTime()));
	}
	
	@Test
	public void testArrayIndexOutOfBounds() {
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "f8");
		board.setPiece(-BoardUtil.KNIGHT, "b8");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "d7");
		board.setPiece(-BoardUtil.BISHOP, "e7");
		board.setPiece(-BoardUtil.KING, "g7");
		board.setPiece(-BoardUtil.PAWN, "f6");
		board.setPiece(-BoardUtil.QUEEN, "b5");
		board.setPiece(-BoardUtil.PAWN, "c5");
		board.setPiece(-BoardUtil.PAWN, "a4");
		
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.BISHOP, "b1");
		board.setPiece(BoardUtil.KING, "e1");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b2");
		board.setPiece(BoardUtil.PAWN, "c3");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "e3");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.PAWN, "g6");
		board.setPiece(BoardUtil.ROOK, "h7");
		board.setPiece(BoardUtil.QUEEN, "c2");
		board.setPiece(BoardUtil.KNIGHT, "d2");
		
		BoardUtil.displayBoard(board);
		
		Engine engine = new EngineMinMaxNoMateDectionMAXDEPTH5(BoardUtil.BLACK);
		Move move = engine.findMove(board, 14, false, true, new ArrayList<Move>());
		System.out.println(move);
		GameStatus status = Common.getGameStatus(move.updateBoard(board), new ArrayList<Move>(), 1, 0);
		System.out.println(status);
	}
	
	@Test
	public void testFork() {
		Board board = new Board();
		board.reset();
		board.setPiece(BoardUtil.SPACE, "d1");
		board.movePiece("e1d1");
		board.movePiece("c2c3");
		board.movePiece("d2d4");
		board.setPiece(-BoardUtil.KNIGHT, "c2");
		board.movePiece("f2f4");
		Engine engine = new EngineNewFastFast(BoardUtil.BLACK);
		BoardUtil.displayBoard(board);
		Move move = engine.findMove(board, 12, false, true, new ArrayList<Move>());
		System.out.println(move);
	}
	
	@Test
	public void undetectedMateInTwo(){
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "e8");
		board.setPiece(-BoardUtil.BISHOP,"c8");
		board.setPiece(-BoardUtil.KNIGHT,"c6");
		board.setPiece(-BoardUtil.QUEEN,"b6");
		board.setPiece(-BoardUtil.KING,"g7");
		board.setPiece(-BoardUtil.PAWN,"a6");
		board.setPiece(-BoardUtil.PAWN,"b7");
		board.setPiece(-BoardUtil.PAWN,"c5");
		board.setPiece(-BoardUtil.PAWN,"d7");
		board.setPiece(-BoardUtil.PAWN,"f6");
		
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.ROOK, "h1");
		board.setPiece(BoardUtil.KNIGHT, "b1");
		board.setPiece(BoardUtil.BISHOP, "h7");
		board.setPiece(BoardUtil.QUEEN, "c2");
		board.setPiece(BoardUtil.KING, "e1");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b2");
		board.setPiece(BoardUtil.PAWN, "c3");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "e3");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		
//		board.movePiece(-BoardUtil.KNIGHT, "c6d8");
//		board.movePiece(BoardUtil.QUEEN, "c2g6");
		
		BoardUtil.displayBoard(board);
		
//		Engine engine = new EngineNewFastFast(BoardUtil.BLACK);
		Engine engine = new EngineNewFastFast(BoardUtil.BLACK);
		Move move = engine.findMove(board, 16, false, true, new ArrayList<Move>());
		System.out.println(move);
		System.out.println(AdvancedMoveUtil.isHisNextMovePuttingMeToCheckMate(board, BoardUtil.BLACK));
		System.out.println(AdvancedMoveUtil.isHisNextNextMovePuttingMeToCheckMate(board, BoardUtil.BLACK));
	}
	
	@Test
	public void testWhySoSlow(){
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
	}
	
	
	
	@Test
	public void whatIsPinLoss(){
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "f8");
		board.setPiece(-BoardUtil.KNIGHT, "b8");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.BISHOP, "d6");
		board.setPiece(-BoardUtil.QUEEN, "d8");
		board.setPiece(-BoardUtil.KING, "g8");
		board.setPiece(-BoardUtil.PAWN, "a7");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "c5");
		board.setPiece(-BoardUtil.PAWN, "d7");
		board.setPiece(-BoardUtil.PAWN, "f7");
		board.setPiece(-BoardUtil.PAWN, "f6");
		board.setPiece(-BoardUtil.PAWN, "h6");
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.ROOK, "h1");
		board.setPiece(BoardUtil.KNIGHT, "b1");
		board.setPiece(BoardUtil.KNIGHT, "g1");
		board.setPiece(BoardUtil.QUEEN, "d1");
		board.setPiece(BoardUtil.KING, "e1");
		board.setPiece(BoardUtil.BISHOP, "f1");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b2");
		board.setPiece(BoardUtil.PAWN, "c2");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "e3");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.PAWN, "h5");
		BoardUtil.displayBoard(board);
		EngineNewFastFast engine = new EngineNewFastFast(-1);
		Set<Status> statuses = engine.getStatusesForMove(board, Common.convertInputToMove(-BoardUtil.KNIGHT, "b8c6"), 5, new ArrayList<Move>(), BoardUtil.getAllTeamPiecesInPieces(board, -1));
		System.out.println(statuses);
	}
	
	@Test
	public void mateEstimation(){
		Board board = new Board();
		board.reset();
		board.movePiece("f2f3");
		board.movePiece("e7e5");
		BoardUtil.displayBoard(board);
		Engine engine = new EngineNewFastFast(BoardUtil.WHITE);
		
		Move move = engine.findMove(board, 5, false, true, new ArrayList<Move>());
		System.out.println(move);
	}
	
	@Test
	public void simpleMateInThreeDetection(){
		Engine engine = new EngineNewFastFast(BoardUtil.WHITE);
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.ROOK, "d4");
		board.setPiece(-BoardUtil.KING, "g8");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "d5");
		board.setPiece(-BoardUtil.PAWN, "e6");
		board.setPiece(-BoardUtil.PAWN, "f7");
		board.setPiece(-BoardUtil.PAWN, "g6");
		board.setPiece(-BoardUtil.PAWN, "h5");
		board.setPiece(-BoardUtil.BISHOP, "f6");
		board.setPiece(-BoardUtil.BISHOP, "c4");
		board.setPiece(-BoardUtil.QUEEN, "d2");
		
		board.setPiece(BoardUtil.PAWN, "b2");
		board.setPiece(BoardUtil.PAWN, "a3");
		board.setPiece(BoardUtil.PAWN, "a5");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.PAWN, "h2");
		board.setPiece(BoardUtil.QUEEN, "b4");
		board.setPiece(BoardUtil.ROOK, "c5");
		board.setPiece(BoardUtil.ROOK, "e1");
		board.setPiece(BoardUtil.BISHOP, "f3");
		board.setPiece(BoardUtil.KING, "h1");
		
		BoardUtil.displayBoard(board);
		Date tic = new Date();
		Move move = engine.findMove(board, 37, false, true, new ArrayList<Move>());
		System.out.println(move);
		System.out.println("Ellapsed Time: " + (new Date().getTime()-tic.getTime()));
	}
	
	@Test
	public void didNotCatchTwoMate() {
		Board board = new Board();
		board.setPiece(-BoardUtil.ROOK, "a8");
		board.setPiece(-BoardUtil.BISHOP, "c8");
		board.setPiece(-BoardUtil.ROOK, "e8");
		board.setPiece(-BoardUtil.KING, "g8");
		board.setPiece(-BoardUtil.PAWN, "a7");
		board.setPiece(-BoardUtil.PAWN, "b7");
		board.setPiece(-BoardUtil.PAWN, "d7");
		board.setPiece(-BoardUtil.PAWN, "f7");
		board.setPiece(-BoardUtil.KNIGHT, "c6");
		board.setPiece(-BoardUtil.BISHOP, "d6");
		board.setPiece(-BoardUtil.PAWN, "f6");
		board.setPiece(BoardUtil.BISHOP, "g6");
		board.setPiece(-BoardUtil.QUEEN, "a5");
		board.setPiece(-BoardUtil.PAWN, "c5");
		board.setPiece(BoardUtil.PAWN, "d4");
		board.setPiece(BoardUtil.PAWN, "c3");
		board.setPiece(BoardUtil.PAWN, "e3");
		board.setPiece(BoardUtil.PAWN, "a2");
		board.setPiece(BoardUtil.PAWN, "b2");
		board.setPiece(BoardUtil.QUEEN, "c2");
		board.setPiece(BoardUtil.PAWN, "f2");
		board.setPiece(BoardUtil.PAWN, "g2");
		board.setPiece(BoardUtil.ROOK, "a1");
		board.setPiece(BoardUtil.KNIGHT, "b1");
		board.setPiece(BoardUtil.KING, "e1");
		board.setPiece(BoardUtil.KNIGHT, "g1");
		board.setPiece(BoardUtil.ROOK, "h1");
		BoardUtil.displayBoard(board);
		Engine engine = new EngineNewFastFast(-1);
		Move move = engine.findMove(board, 12, false, true, new ArrayList<Move>());
		System.out.println(move);
	}
	
	
	@Test
	public void testMateDetectionTime(){
		Board board = new Board();
		board.reset();
		board.movePiece("f2f3");
		board.movePiece("e7e5");
		board.movePiece("g2g4");
		BoardUtil.displayBoard(board);
		Date tic = new Date();
		System.out.println(MoveUtil.isCheckMate(board, 1));
		System.out.println("Ellapsed time: " + ((new Date()).getTime()-tic.getTime()));
		Date tic1 = new Date();
		List<Piece> pieces = BoardUtil.getAllTeamPieces(board, -1);
		System.out.println("Ellapsed time: " + ((new Date()).getTime()-tic1.getTime()));
		Date tic2 = new Date();
		List<Move> moves = new ArrayList<Move>();
		for(Piece piece : pieces){
			moves.addAll(MoveUtil.findAllValidMoves(board, piece));
		}
		System.out.println("# of MOVES: " + moves.size());
		for(Move move : moves){
			Board newBoard = move.updateBoard(board);
			if(MoveUtil.isCheckMate(newBoard, 1)) System.out.println(move + "\tCHECK MATE!");
		}
		System.out.println("Ellapsed time: " + ((new Date()).getTime()-tic2.getTime()));
		
	}
	
	@Test
	public void testCapturingRookKillinGBishopOrCapturingFreePawn(){
		Board b = new Board();
		b.setPiece(-BoardUtil.ROOK, "a8");
		b.setPiece(-BoardUtil.BISHOP,"c8");
		b.setPiece(-BoardUtil.QUEEN,"d8");
		b.setPiece(-BoardUtil.ROOK,"f8");
		b.setPiece(-BoardUtil.KING,"g8");
		b.setPiece(-BoardUtil.PAWN,"a7");
		b.setPiece(-BoardUtil.PAWN,"d7");
		b.setPiece(-BoardUtil.PAWN,"f7");
		b.setPiece(-BoardUtil.PAWN,"f6");
		b.setPiece(-BoardUtil.KNIGHT,"g6");
		b.setPiece(-BoardUtil.PAWN,"h6");
		b.setPiece(-BoardUtil.PAWN,"b5");
		b.setPiece(-BoardUtil.BISHOP,"c3");
		
		b.setPiece(BoardUtil.ROOK, "a1");
		b.setPiece(BoardUtil.QUEEN, "d1");
		b.setPiece(BoardUtil.KING, "f1");
		b.setPiece(BoardUtil.ROOK, "h1");
		b.setPiece(BoardUtil.PAWN, "a2");
		b.setPiece(BoardUtil.BISHOP, "c2");
		b.setPiece(BoardUtil.PAWN, "f2");
		b.setPiece(BoardUtil.PAWN, "g2");
		b.setPiece(BoardUtil.PAWN, "e3");
		b.setPiece(BoardUtil.KNIGHT, "f3");
		b.setPiece(BoardUtil.PAWN, "c4");
		b.setPiece(BoardUtil.PAWN, "d4");
		b.setPiece(BoardUtil.PAWN, "h5");
		BoardUtil.displayBoard(b);
		Engine engine = new EngineNewFastFast(-1);
		Move move = engine.findMove(b, 14, false, true, new ArrayList<Move>());
		System.out.println(move);

	}
	
	@Test
	public void testSurpriseMateKnightMove(){
		Board b = new Board("r1b1N1k1/pp3pbp/1q4p1/3p4/4PP2/2N5/PP2QnPP/1RB2BK1 b - - 0 1");
		Engine e = new EngineNewFastFast(BoardUtil.BLACK);
		Move move = e.findMove(b);
		Move expectedMove = new Move(-BoardUtil.KNIGHT, "f2h3");
		assertEquals("The knight move leads to a mate in 2", expectedMove, move);
	}
	
	@Test
	public void testMateIn3AfterQueenSacrifice(){
		Board b = new Board("3RQn2/2r1q1k1/4Bppp/3p3P/3p4/4P1P1/5PK1/8 w - - 0 1");
		BoardUtil.display(b);
		Engine e = new EngineNewFastFast(BoardUtil.WHITE);
		Move move = e.findMove(b);
		Move expectedMove = new Move(BoardUtil.QUEEN, "e8g6");
		assertEquals("The queen move leads to a mate in 3", expectedMove, move);
	}
	
	@Test
	public void testMateIn3BishopMoveSurpriseCheck(){
		Board b = new Board("r5r1/p1q2p1k/1p1R2pB/3pP3/6bQ/2p5/P1P1NPPP/6K1 w - - 0 1");
		Engine e = new EngineNewFastFast(BoardUtil.WHITE);
		Move move = e.findMove(b);
		Move expectedMove = new Move(BoardUtil.BISHOP, "h6f8");
		assertEquals("The bishop move leads to a mate in 3", expectedMove, move);
	}
	
	
	//Not sure how to solve this without minimax
	@Test
	public void testRookCaptureSacrifice(){
		Board board = new Board("8/1p1r2k1/q4npp/3B4/P1PQ4/8/6PP/5R1K b - - 0 1");
		final Move EXPECTED_MOVE = new Move(-BoardUtil.ROOK, "d7d5");
		
		Engine e1 = new EngineNewFastFast(-1);
		Move m1 = e1.findMove(board);
//		Move m2 = e2.findMove(board);
		
//		assertEquals("Sacrifice of the rook should have been predicted by EngineMinMax", EXPECTED_MOVE, m2);
		assertEquals("Sacrifice of the rook should have been predicted by EngineNew", EXPECTED_MOVE, m1);
	}
	
	@Test
	public void testIgnoreSenselessSacrifice(){
		Board board = new Board("r2b4/2RRQP2/7k/8/8/8/6PP/7K b - - 0 1");
		BoardUtil.displayBoard(board);
		Move exp = new Move(board, "a8a1");
		Move redHerring = new Move(board, "a8b8");
		List<Move> allMoves = MoveUtil.getAllMoveForTeam(exp.updateBoard(board), 1);
		List<Move> allMovesSansSenselessSacrifice = AdvancedMoveUtil.trimSenselessSacrifice(exp.updateBoard(board), allMoves);
		assertTrue(allMoves.size() > allMovesSansSenselessSacrifice.size());
		assertTrue(AdvancedMoveUtil.isForcedMate(exp.updateBoard(board), -1, 0, true));
		assertFalse(AdvancedMoveUtil.isForcedMate(redHerring.updateBoard(board), -1, 0, true));
		Move move = e2.findMove(board);
		assertEquals(exp, move);
	}
}