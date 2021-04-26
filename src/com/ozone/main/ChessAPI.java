package com.ozone.main;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.engine.EngineMinMaxNoMateDectionAPI;
import com.ozone.engine.EngineMinMaxNoMateDectionAPI_NoOpening;

public class ChessAPI {

	public static void main(String[] args) {
		String fen = args[0];
		int iterations = Integer.valueOf(args[5]);
		Board board = new Board(fen);
		if(iterations > 5) {
			EngineMinMaxNoMateDectionAPI_NoOpening engine = new EngineMinMaxNoMateDectionAPI_NoOpening(-1);
			Move move = engine.findMove(board);
			System.out.println(move.getSquare());
		}else {
			EngineMinMaxNoMateDectionAPI engine = new EngineMinMaxNoMateDectionAPI(-1);
			Move move = engine.findMove(board);
			System.out.println(move.getSquare());
		}
	}

}
