package com.ozone.engine;

import java.util.Comparator;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;

public interface Engine {
	
	Move findMove(Board board);
	
	Move findMove(Board board, int iteration, boolean isConsole, boolean isDeveloperMode, List<Move> moveHistory);
			
	int getTeam();
	
	void setTeam(int team);
	
	Comparator<Move> CustomComparator = new Comparator<Move>() {
		@Override
		public int compare(Move left, Move right) {
			if(left.getScore() == right.getScore()){
				if(right.getPieceCaptured() == left.getPieceCaptured()){
					return Integer.valueOf(Math.abs(right.getPieceMoving())).compareTo(Math.abs(left.getPieceMoving()));
				}
				return Integer.valueOf(Math.abs(left.getPieceCaptured())).compareTo(Math.abs(right.getPieceCaptured()));
			}
			return Integer.valueOf(left.getScore()).compareTo(right.getScore());
		}
	};
	
	void switchTeam();
}