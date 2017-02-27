package com.ozone.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.movements.MoveUtil;

public class EngineRandom implements Engine {
	int team;
	public EngineRandom(int team) {
		this.team = team;
	}
	
	@Override
	public void switchTeam(){
		this.team = -team;
	}
	
	@Override
	public void setTeam(int team){
		this.team = team;
	}
	
	@Override
	public Move findMove(Board board, int iteration, boolean isConsole, boolean isDeveloperMode, List<Move> moveHistory){

		if(MoveUtil.isCheckMate(board, team)){
			return null;
		}
		List<Move> moves = MoveUtil.getAllMoveForTeam(board, team);
		if(moves.size() == 0) {
			return MoveUtil.STALE_MATE;
		}
		Collections.shuffle(moves);
		return moves.get(0);
	}
	
	@Override
	public Move findMove(Board board) {
		return findMove(board, 20, false, true, new ArrayList<Move>());
	}
	
	@Override
	public int getTeam() {
		return team;
	}
}