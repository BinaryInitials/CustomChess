package com.ozone.engine;

import static com.ozone.movements.MoveUtil.getAllMoveForTeam;
import static com.ozone.movements.MoveUtil.getAllMoveForTeamMinMaxSpecialSorting;
import static com.ozone.movements.MoveUtil.getValue;
import static com.ozone.movements.MoveUtil.getValueWithSign;
import static com.ozone.movements.MoveUtil.isPieceThreatenedNew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.libraries.LibraryLoader;
import com.ozone.movements.AdvancedMoveUtil;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;

public class EngineMinMaxNoMateDectionLevel1000MOBILE_REALCOPY implements Engine {

	public static final int KING_VALUE = 1000000;
	public static int MAX_DEPTH = 1;
	public static final int PSEUDO_INFINITE = KING_VALUE * 1000;
	private LibraryLoader openings = null;
	private int team;
	private static HashMap<Move, Integer> nodeMap = new HashMap<Move, Integer>();
	private static HashMap<Integer, List<Move>> moveMap = new HashMap<Integer, List<Move>>();
	private static HashMap<Integer, List<Move>> mateMoveMap = new HashMap<Integer, List<Move>>();
	private static boolean developer;
	
	public EngineMinMaxNoMateDectionLevel1000MOBILE_REALCOPY(int team) {
		this.team = team;
		nodeMap = new HashMap<Move, Integer>();
		moveMap = new HashMap<Integer, List<Move>>();
		mateMoveMap = new HashMap<Integer, List<Move>>();
		openings = new LibraryLoader(team);
	}

	@Override
	public void setTeam(int team) {
		this.team = team;
		nodeMap.clear();
		moveMap.clear();
		openings = new LibraryLoader(team);
	}

	@Override
	public int getTeam() {
		return team;
	}

	@Override
	public Move findMove(Board board, int iteration, boolean isDeveloper, boolean isConsole, List<Move> moveHistory) {
		developer = isDeveloper;
		nodeMap.clear();
		moveMap.clear();
		mateMoveMap.clear();
		if(MoveUtil.isCheckMate(board, team)){
			return null;
		}else if(MoveUtil.isStaleMate(board, team)){
			return new Move(0,0,0,0,0);
		}

		if(iteration < LibraryLoader.MOVE_THRESHOLD && openings != null){
			Move opening = openings.findAdvancedOpening(board);
			if(opening != null){
				return opening;
			}
		}
		int boardStatus = team * BoardUtil.getBoardStatus(board);

		boolean mateFound = false;
		boolean mateIn2Found = false;
		List<Move> moves = getAllMoveForTeamMinMaxSpecialSorting(board, team);

		for(Move move: moves) {
			Board newBoard = move.updateBoard(board);
			if(MoveUtil.isCheckMate(newBoard, -team)) {
				move.setScore(50000000);
				System.out.println(move + "\tMATE IN 1");
				mateFound = true;
				break;
			}
			if(!mateIn2Found && (boardStatus > 5 || MoveUtil.isCheck(newBoard, -team))) {
				if(AdvancedMoveUtil.isMyNextMovePuttingHimToCheckMate(move.updateBoard(board), team)) {
					System.out.println(move + "\tMATE IN 2");
					move.setScore(25000000);
					mateIn2Found = true;
					mateFound = true;
				}else if(AdvancedMoveUtil.isForcedMate(move.updateBoard(board), team, 0, false)) {
					System.out.println(move + "\tMATE IN 3+");
					move.setScore(12500000);
					mateFound = true;
				}
			}
		}
		
		if(mateFound) {
			return Collections.max(moves, CustomComparator);
		}

		if(boardStatus > 5) {
			int score = keepCheckingUntilMate(board, team);
			if(score > 0 && mateMoveMap.get(score) != null){
				moves = mateMoveMap.get(score);
				for(Move move : moves){
					System.out.println(move + "\tMATE IN PERPETUAL CHECK (4+)");
					move.setScore(score);
				}
				return Collections.max(moves, CustomComparator);
			}
		}

		int n = moveHistory.size() - 1;
		List<Move> rootMoves = moves;
		if(n > 8) {
			boolean threeFoldRepetitionPotential = true;
			for(int i=n;i > n-4;i--) {
				if(!moveHistory.get(i).equals(moveHistory.get(i-4))) {
					threeFoldRepetitionPotential = false;
					break;
				}
			}

			if(threeFoldRepetitionPotential && rootMoves.size() > 0) {
				rootMoves.remove(moveHistory.get(n-3));
			}
		}


		int score = alphaBetaMinMaxInitializer(board, rootMoves);


		if(moveMap.get(score) == null){
			if(moves.size() > 0){
				return moves.get(0);
			}else{
				return MoveUtil.STALE_MATE;
			}
		}

		List<Move> bestMoves = moveMap.get(score);
		if(bestMoves.size() == 1){
			return bestMoves.get(0);
		}
		return pickFromBest(board, bestMoves, iteration, moveHistory);
	}

	public int alphaBetaMinMaxInitializer(Board board, List<Move> rootMoves){
		nodeMap.clear();
		moveMap.clear();
		return alphaBetaMinMax(board, team, MAX_DEPTH, -PSEUDO_INFINITE, PSEUDO_INFINITE, 1, null, MAX_DEPTH, rootMoves);
	}

	public int alphaBetaMinMax(Board board, int team, int depth, int alpha, int beta, int maximizing, Move parentMove, int MAX_DEPTH, List<Move> rootMoves){
		if(isStaleMateMinMax(board, maximizing * team)){
			return 0;
		}else if (depth == 0){
			return heuristic(board, team);
		}

		int v = -maximizing * (PSEUDO_INFINITE - MAX_DEPTH + depth);
		List<Move> moves;
		if(depth == MAX_DEPTH) {
			moves = rootMoves;
		}else {
			moves = getAllMoveForTeam(board, maximizing*team);
		}
		if(moves.isEmpty()) {
			return v;
		}

		if(maximizing>0){
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				if(depth == MAX_DEPTH) {
					parentMove = move;
					nodeMap.put(move, 0);
				}

				if(MAX_DEPTH - depth >= PSEUDO_INFINITE - alpha) {
					break;
				}

				nodeMap.put(parentMove, nodeMap.get(parentMove)+1);
				int bestScore;

				bestScore = alphaBetaMinMax(newBoard, team, depth-1, alpha, beta, -1, parentMove, MAX_DEPTH, null);

				if(depth == MAX_DEPTH) {
					if(moveMap.get(bestScore) == null){
						moveMap.put(bestScore, new ArrayList<Move>());
					}
					if(developer){
						System.out.println(move + "\t" + bestScore + "\t" + "\tALPHA: " + alpha);
					}
					moveMap.get(bestScore).add(move);
				}

				v = Math.max(v, bestScore);
				alpha = Math.max(alpha, v);

				if(beta <= alpha){
					break; //Beta cut-off
				}
			}
			return v;
		}else{
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				nodeMap.put(parentMove, nodeMap.get(parentMove)+1);
				int bestScore = alphaBetaMinMax(newBoard, team, depth-1, alpha, beta, 1, parentMove, MAX_DEPTH, null);
				v = Math.min(v, bestScore);
				beta = Math.min(beta, v);

				if(beta < alpha){
					break; //Alpha cut-off
				}
			}
			return v;
		}
	}

	public static int heuristic(Board board, int team){
		int sum = 0;
		int worsePieceThreatened = 0;
		List<Piece> ps = BoardUtil.getAllPieces(board);
		for(Piece p : ps){
			sum = sum + getValueWithSign(p);
			if(p.getValue()*team >0 && isPieceThreatenedNew(board, p)){
				worsePieceThreatened = Math.max(worsePieceThreatened, getValue(p));
			}
		}

		return team*sum - worsePieceThreatened;
	}

	public int keepCheckingUntilMate(Board board, int team){
		mateMoveMap.clear();
		int maxDepth = 4;
		int depth = 4;
		return keepCheckingUntilMate(board, team, depth, -1000000000, 100000000, 1, maxDepth);
	}

	public int keepCheckingUntilMate(Board board, int team, int depth, int alpha, int beta, int maximizing, int MAX_DEPTH){
		if(MoveUtil.isCheckMate(board, -team)){
			return KING_VALUE - (MAX_DEPTH - depth);
		}
		if(depth == 0){
			return 0;
		}

		int v;
		if(maximizing>0){
			v = -1000000000;
			List<Move> moves = MoveUtil.getAllMoveForTeam(board, team);

			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				if(!MoveUtil.isCheck(newBoard, -team) || MoveUtil.isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
					/*
					 * Basically this is saying that only move forward if the opponent is in check AND that your piece that just moved isn't under attack.
					 * This assumes that the piece that just moved is doing the checking. What if this was a surprise check? Those are some of the most interesting.
					 * There must be a way to allow for those while filtering for the senseless suicide checks.
					 *
					 * If opp's next move can recapture the piece, then it is a senseless check.
					 */

					continue;
				}
				int bestScore = keepCheckingUntilMate(newBoard, team, depth-1, alpha, beta, -1, MAX_DEPTH);
				if (depth == MAX_DEPTH){
					if(mateMoveMap.get(bestScore) == null){
						mateMoveMap.put(bestScore, new ArrayList<Move>());
					}
					mateMoveMap.get(bestScore).add(move);
				}
				v = Math.max(v, bestScore);
				alpha = Math.max(alpha, v);
				if(beta < alpha){
					break; //Beta cut-off
				}
			}
			return v;
		}else{
			v = 1000000000;
			List<Move> moves = MoveUtil.getAllMoveForTeam(board, -team);
			if(moves.size() > 3){
				return 0;
			}
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				int bestScore = keepCheckingUntilMate(newBoard, team, depth-1, alpha, beta, 1, MAX_DEPTH);
				v = Math.min(v, bestScore);
				beta = Math.min(beta, v);
				if(beta < alpha){
					break; //Alpha cut-off
				}
			}
			return v;
		}
	}

	private Move pickFromBest(Board board, List<Move> bestMoves, int iteration, List<Move> moveHistory) {
		for(Move move : bestMoves){
			Board newBoard = move.updateBoard(board);
			int score = 1000*getValue(move.getPieceCaptured());
			List<Piece> myPiecesAfterMove = BoardUtil.getAllTeamPiecesInPieces(newBoard, team);
			int greatestPieceInDanger = 0;
			for(Piece myPiece : myPiecesAfterMove){
				if(MoveUtil.isPieceThreatenedNew(newBoard, myPiece)){
					greatestPieceInDanger = Math.max(greatestPieceInDanger, getValue(myPiece));
				}
			}
			score = score - 1000*greatestPieceInDanger;

			int oppMoves = MoveUtil.getMoveCountForTeam(newBoard, BoardUtil.getAllTeamPiecesInPieces(newBoard, -team));
			int myMoves = MoveUtil.getMoveCountForTeam(newBoard, BoardUtil.getAllTeamPiecesInPieces(newBoard, team));
			score = score + myMoves-oppMoves;
			if(MoveUtil.isCheck(newBoard, -team)){
				score = score + 10;
			}

			boolean hasJustCastled = false;
			if(isCastling(move)) {
				score = score + 20; //Reward castle
				hasJustCastled = true;
			}

			if(hasCastled(moveHistory) && !hasJustCastled && Math.abs(move.getPieceMoving()) == BoardUtil.ROOK || Math.abs(move.getPieceMoving()) == BoardUtil.KING) {
				score = score - 20; //Penalize movement that can ruin castle
			}

			if(team*move.getPieceMoving() == BoardUtil.KNIGHT && isKnightOnEdge(move)) {
				score = score - 10; //Penalize edge movement for knight;
			}

			if(iteration < 30 && hasSamePieceMovedBefore(moveHistory, move)) {
				score = score - 10; //Penalize same piece movement;
			}
			move.setScore(score);
		}

		return Collections.max(bestMoves, CustomComparator);
	}

	private boolean hasSamePieceMovedBefore(List<Move> moveHistory, Move move) {
		return moveHistory.size() > 2 && moveHistory.get(moveHistory.size()-2).getPieceAfterMove().equals(move.getPieceBeforeMove());
	}

	private boolean isKnightOnEdge(Move move) {
		int y = move.getToPos()[0];
		int x = move.getToPos()[1];

		return y==0 || y==7 || x==0 || x==7;
	}

	private boolean isCastling(Move move) {
		return Math.abs(move.getPieceMoving()) == BoardUtil.KING && Math.abs(move.getToPos()[1]-move.getFromPos()[1]) == 2;
	}

	private boolean hasCastled(List<Move> moveHistory) {
		int n = moveHistory.size() - 2;
		//^ The last move in the history belongs to the other team.
		if(n < 7) {
			return false;
		}
		for(int i=n; i > 6; i=i-2) {
			if(isCastling(moveHistory.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Move findMove(Board board) {
		return findMove(board, 50, true, true, new ArrayList<Move>());
	}

	@Override
	public void switchTeam() {
		// TODO Auto-generated method stub
		
	}
	
	public static boolean isStaleMateMinMax(Board board, int team) {

		List<Piece> ownMaterial = BoardUtil.getAllTeamPieces(board, team);

		for(Piece piece : ownMaterial){
			List<Move> moves = MoveUtil.findVanillaMovesForPiece(piece);
			for (Move move:moves){
				if(MoveUtil.isMoveValid(board, move, false)){
					return false;
				}
			}
		}

		return !MoveUtil.isCheck(board, team);
	}
}