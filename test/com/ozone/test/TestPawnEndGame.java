package com.ozone.test;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Common.GameStatus;
import com.ozone.common.Piece;
import com.ozone.engine.Engine;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH7;
import com.ozone.engine.EngineNewFastFast;
import com.ozone.main.EngineSimulation;
import com.ozone.movements.BoardUtil;

public class TestPawnEndGame {
	
	@Test
	public void testKingTwoPawns(){
		Board board = new Board();
		board.setPiece(new Piece(BoardUtil.KING, 0, 4));
		board.setPiece(new Piece(BoardUtil.PAWN, 1, 2));
		board.setPiece(new Piece(BoardUtil.PAWN, 1, 5));
		board.setPiece(new Piece(-BoardUtil.KING, 7, 4));
		board.setPiece(new Piece(-BoardUtil.PAWN, 6, 0));
		board.setPiece(new Piece(-BoardUtil.PAWN, 6, 7));
		
		Engine eWhite = new EngineNewFastFast(1);
		Engine eBlack = new EngineMinMaxNoMateDectionMAXDEPTH7(-1);
		EngineSimulation es = new EngineSimulation();
		GameStatus gs = es.start(eWhite, eBlack, false, true, board);
		System.out.println(gs);
	}
}
