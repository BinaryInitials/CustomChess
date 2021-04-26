package com.ozone.engine;

import static com.ozone.movements.MoveUtil.getAllMoveForTeamMinMaxSpecialSorting;
import static com.ozone.movements.MoveUtil.getValueWithSign;
import static com.ozone.movements.MoveUtil.isCheckMate;
import static com.ozone.movements.MoveUtil.isStaleMate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.movements.BoardUtil;

public class EngineSingleThreadMinMax {
	public static final int KING_VALUE = 1000000;
	public static final int MAX_DEPTH = 3;
	
	public static final int PSEUDO_INFINITE = KING_VALUE*1000;
	private int team;
	private Move rootMove;

	public EngineSingleThreadMinMax(int team, Move rootMove){
		this.team = team;
		this.rootMove = rootMove;
	}
	
	public HashMap<Move, Integer> findMoves(Board board){
		HashMap<Move, Integer> output = new HashMap<Move, Integer>();
		output.put(rootMove, alphaBetaMinMaxInitializer(board, rootMove));
		return output;
	}
	
	
	public int alphaBetaMinMaxInitializer(Board board, Move rootMove){
		List<Move> moves = new ArrayList<Move>();
		moves.add(rootMove);
		return alphaBetaMinMax(board, team, MAX_DEPTH, -PSEUDO_INFINITE, PSEUDO_INFINITE, 1, rootMove);
	}
	
	public int alphaBetaMinMax(Board board, int team, int depth, int alpha, int beta, int maximizing, Move parentMove){
		
		if(isCheckMate(board, team)){
			return -(PSEUDO_INFINITE - MAX_DEPTH + depth);
		}
		
		if(isCheckMate(board, -team)){
			return PSEUDO_INFINITE - MAX_DEPTH + depth;
		}
		
		if(isStaleMate(board, maximizing*team)) {
			return 0;
		}
		
		if(depth == 0){
			return heuristic(board, team);
		}
		
		int v = -maximizing * (PSEUDO_INFINITE - MAX_DEPTH + depth);
		List<Move> moves;
		if(depth == MAX_DEPTH) {
			moves = new ArrayList<Move>();
			moves.add(rootMove);
		}else {
			moves = getAllMoveForTeamMinMaxSpecialSorting(board, maximizing*team);
		}
		if(moves.isEmpty()) {
			return v;
		}
		if(maximizing>0){
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				if(MAX_DEPTH - depth >= PSEUDO_INFINITE - alpha)
					break;
				v = Math.max(v, alphaBetaMinMax(newBoard, team, depth-1, alpha, beta, -1, null));
				alpha = Math.max(alpha, v);
				if(beta <= alpha)
					break; //Beta cut-off
			}
			return v;
		}else{
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				int bestScore = alphaBetaMinMax(newBoard, team, depth-1, alpha, beta, 1, null);
				v = Math.min(v, bestScore);
				beta = Math.min(beta, v);
				if(beta < alpha)
					break; //Alpha cut-off
			}
			return v;
		}
	}
	public static int heuristic(Board board, int team){
		int sum = 0;
		List<Piece> ps = BoardUtil.getAllPieces(board);
		for(Piece p : ps)
			sum += getValueWithSign(p);
		return team*sum;
	}
}