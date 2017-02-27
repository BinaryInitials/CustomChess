package com.ozone.test;

import java.util.ArrayList;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.engine.Engine;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.libraries.LibraryLoader;
import com.ozone.movements.BoardUtil;
import com.ozone.utility.Converters;

public class TestOpening {

	
	@Test
	public void testOpening() {
		Board board = new Board();
		board.reset();
		Engine e = new EngineMinMaxNoMateDectionMAXDEPTH5(1);
		Move move = e.findMove(board);
		System.out.println(move);
	}
	
	@Test
	public void testLibrary() {
		Board board = new Board();
		board.reset();
		System.out.println(Converters.boardToFen(board, true));
		LibraryLoader libraryLoader = new LibraryLoader(BoardUtil.WHITE, true);
		Move move = libraryLoader.findAdvancedOpening(board);
		System.out.println(move);
		System.out.println(Converters.pieceToFenPiece(-BoardUtil.QUEEN));
	}
	
	@Test
	public void testMateDetection() {
		Board board = new Board();
		board.setPiece(-BoardUtil.KING, 0, 0);
		board.setPiece(BoardUtil.KING, 0, 2);
		board.setPiece(BoardUtil.ROOK, 7, 0);
		BoardUtil.display(board);
		System.out.print(Common.getGameStatus(board, new ArrayList<Move>(), -1, 0));
		System.out.print(Common.getGameStatus(board, new ArrayList<Move>(), 1, 0));
	}
}
