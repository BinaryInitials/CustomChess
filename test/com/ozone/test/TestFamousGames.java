package com.ozone.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.engine.Engine;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.movements.AdvancedMoveUtil;
import com.ozone.movements.BoardUtil;

public class TestFamousGames {
	
	Engine ew = new EngineMinMaxNoMateDectionMAXDEPTH5(1);
	Engine eb = new EngineMinMaxNoMateDectionMAXDEPTH5(-1);
	
	@Test
	public void testingKermurSireDeLegalVsSaintBrie1750Part1(){
		Board board = new Board("rn1qkbnr/ppp2p1p/3p2p1/4p3/2B1P1b1/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 0 1");
		BoardUtil.displayBoard(board);
		//This is not a forced mate. It does give a positional advantage.
		Move exp = new Move(board, "f3e5");
		Move move = ew.findMove(board);
		assertEquals(exp, move);
	}

	@Test
	public void testingKermurSireDeLegalVsSaintBrie1750Part2(){
		Board board = new Board("rn1qkbnr/ppp2p1p/3p2p1/4p3/2B1P1b1/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 0 1");
		board.movePiece(new Move(BoardUtil.KNIGHT, "f3e5"));
		board.movePiece(new Move(board, "g4d1"));
		Move move = ew.findMove(board);
		Move exp = new Move(board, "c4f7");
		assertEquals(move, exp);
	}
	
	@Test
	public void testingBodensMate(){
		Board board = new Board("2kr3r/pp1nqppp/1b2pnb1/8/Q2P1B2/2P2N2/PP1NBPPP/R4RK1 w KQkq - 0 1");
		assertEquals(new Move(board, "a4c6"), ew.findMove(board));
	}
	
	
	@Test
	public void testingDamiano(){
		Board board = new Board("r4rk1/pppqn1p1/2n1b1P1/3p1p2/1b1P4/2NBBN2/PPP2P2/2KRQ2R w KQkq - 0 1");
		Move expected = new Move(BoardUtil.ROOK, "h1h8");
		Date tic = new Date();
		Move move = ew.findMove(board);
		long time = new Date().getTime() - tic.getTime();
		assertTrue("Elapsed time, ms = " + time, time<5000);
		assertEquals(expected, move);
	}
	
	@Test
	public void testingGrecos(){
		Board board = new Board("4rr1k/pppbq1pp/1b1p1n2/8/2BP4/4B3/PPPN1PPR/1K1Q3R w KQkq - 0 1");
		BoardUtil.displayBoard(board);
		assertEquals(ew.findMove(board), Common.convertInputToMove(board, "h2h7"));		
	}
	
	@Test
	public void testingEpauletteMate(){
		Board board = new Board("3rkb1R/pp1n1pp1/1qpp3n/4p3/2BPP3/1QP1BP2/PP3P2/R3K3 w KQkq - 0 1");
		BoardUtil.displayBoard(board);
		assertEquals(ew.findMove(board), Common.convertInputToMove(board, "c4f7"));
	}
	
	@Test
	public void testingTheImmortalGame(){
		Board board = new Board("r1b1k1nr/p2p1ppp/n2B4/1p1NPN1P/6P1/3P1Q2/P1P1K3/q5b1 w KQkq - 0 1");
		BoardUtil.displayBoard(board);
		assertEquals("The Knight should have moved and created a check", Common.convertInputToMove(board, "f5g7"), ew.findMove(board));
	}

	@Test
	public void testingTheImmortalGamePart2(){
		Board board = new Board("r1b1k1nr/p2p1ppp/n2B4/1p1NPN1P/6P1/3P1Q2/P1P1K3/q5b1 w KQkq - 0 1");
		board.movePiece(new Move(board, "f5g7"));
		board.movePiece(new Move(board, "e8d8"));
		Move move = ew.findMove(board);
		Move exp = new Move(board, "f3f6");
		assertEquals(exp, move);
	}
	
	@Test
	public void testingPaulMorphyParis1859SmotheredGame(){
		Board board = new Board("r1k4r/ppp1bq1p/2n1N3/6B1/3p2Q1/8/PPP2PPP/R5K1 w KQkq - 0 1");
		BoardUtil.displayBoard(board);
		Move exp = new Move(board, "e6c5");
		assertTrue(AdvancedMoveUtil.isForcedMate(exp.updateBoard(board), 1, 0, true));
		assertEquals(exp, ew.findMove(board));
	}

	@Test
	public void testingPaulMorphyParis1859SmotheredGamePart2(){
		Board board = new Board("rk5r/ppp1bq1p/2n5/2N3B1/3p2Q1/8/PPP2PPP/R5K1 b KQkq - 0 1");
		assertTrue(AdvancedMoveUtil.isForcedMate(new Move(board, "c5d7").updateBoard(board), 1, 0, false));
		Move move = ew.findMove(board);
		Move exp = new Move(board, "c5d7");
		assertEquals(exp, move);
	}
}