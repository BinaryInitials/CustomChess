package com.ozone.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.utility.Converters;

public class TestFen {
	
	@Test
	public void testDefaultFen(){
		Board board = new Board();
		board.reset();
		String fen = Converters.boardToFen(board, true);
		assertEquals("The FEN determined should have matched the string provided", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", fen);
	}
}
