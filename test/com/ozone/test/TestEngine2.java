package com.ozone.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.engine.Engine2;
import com.ozone.movements.BoardUtil;

public class TestEngine2 {
	
	@Test
	public void testChessEngine(){
		Board board = new Board();
		board.reset();
		Move move = Engine2.findMove(board, BoardUtil.WHITE, true);
		assertNotNull("The move should not be null", move);
		assertTrue("The move should be White's piece", move.getPieceMoving() > BoardUtil.SPACE);
	}
}