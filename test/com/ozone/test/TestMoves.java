package com.ozone.test;

import java.util.List;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;

public class TestMoves {
	
	@Test
	public void testPawnCapture(){
		String board = "r1bq1rk1/pppp1ppp/8/n2Np3/1bB3n1/2P2N2/PP3PPP/R1BQK2R b KQkq - 0 1";
//		EngineMinMaxNoMateDectionMAXDEPTH5 e = new EngineMinMaxNoMateDectionMAXDEPTH5(1);
		List<Move> moves = MoveUtil.findAllValidMoves(new Board(board), BoardUtil.PAWN, 2, 2); 
		System.out.println(moves);
	}

}
