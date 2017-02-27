package com.ozone.engine;

import static com.ozone.movements.AdvancedMoveUtil.isForcedMate;
import static com.ozone.movements.AdvancedMoveUtil.isMyNextMovePuttingHimToCheckMate;
import static com.ozone.movements.MoveUtil.getAllMoveForTeam;
import static com.ozone.movements.MoveUtil.getAllMoveForTeamMinMaxSpecialSorting;
import static com.ozone.movements.MoveUtil.getAllMoveForTeamWithoutSuicides;
import static com.ozone.movements.MoveUtil.getMoveCountForTeam;
import static com.ozone.movements.MoveUtil.getValue;
import static com.ozone.movements.MoveUtil.getValueWithSign;
import static com.ozone.movements.MoveUtil.isCheck;
import static com.ozone.movements.MoveUtil.isCheckMate;
import static com.ozone.movements.MoveUtil.isPawnStackedIsolatedOrBlocked;
import static com.ozone.movements.MoveUtil.isPieceThreatenedNew;
import static com.ozone.movements.MoveUtil.isStaleMate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.libraries.LibraryLoader;
import com.ozone.mate.KingQueen;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;

public class EngineMinMaxNoMateDectionLevel1200MOBILE implements Engine {
	public static final int KING_VALUE = 1000000;
//	public static int MAX_NODE = 131071;
//	public static int MAX_NODE = 40000;
//	public static int MAX_NODE = 10000000;
//	public static int MAX_NODE = 1024;
//	public static int MAX_NODE = 32767;
//	public static int MAX_NODE = 16383;
//	public static int MAX_NODE = 8191;

	//Optimal for mobile
//	public static int MAX_NODE = 131071;
//	public static int MAX_TOTAL_NODE = 1000000;
//	public static int MAX_DEPTH = 5;
	public static int MAX_TOTAL_NODE = 256;
	public static int MAX_NODE = 32;
	public static int MAX_DEPTH = 3;
	
	//^ Must be of the form 2^n-1 for optimized comparison.
	public static final int PSEUDO_INFINITE = KING_VALUE*1000;
	private LibraryLoader openings = null;
	private double avgNps;
	private int totalNodes;
	private double totalTime;
	private int team;
	private static HashMap<Move, Integer> nodeMap = new HashMap<Move, Integer>();
	private static HashMap<Integer, List<Move>> moveMap = new HashMap<Integer, List<Move>>();
	private boolean isDeveloper = false;
	public EngineMinMaxNoMateDectionLevel1200MOBILE(int team){
		this.team = team;
		this.isDeveloper = false;
		nodeMap = new HashMap<Move, Integer>();
		moveMap = new HashMap<Integer, List<Move>>();
		openings = team > BoardUtil.SPACE ? new LibraryLoader(BoardUtil.WHITE, true) : new LibraryLoader(BoardUtil.BLACK, true);
	}
	
	public void setMaxNode(int maxNode) {
		MAX_NODE = maxNode;
	}

	public void setMaxDepth(int maxDepth) {
		MAX_DEPTH = maxDepth;
	}
	
	public int getMaxDepth() {
		return MAX_DEPTH;
	}
	
	public int getMaxNode() {
		return MAX_NODE;
	}
	
	public int getAverageNPS() {
		return (int)Math.round(avgNps);
	}
	public int getTotalNodes() {
		return totalNodes;
	}
	
	@Override
	public void setTeam(int team){
		this.team = team;
//		MAX_DEPTH = 5;
//		MAX_NODE = 127;
		avgNps = 0;
		totalNodes = 0;
		totalTime = 0;
		nodeMap.clear();
		moveMap.clear();
		openings = team > BoardUtil.SPACE ? new LibraryLoader(BoardUtil.WHITE, true) : new LibraryLoader(BoardUtil.BLACK, true);
	}
	
	@Override
	public void switchTeam(){
		avgNps = 0;
		totalNodes = 0;
		totalTime = 0;
		nodeMap.clear();
		moveMap.clear();
		this.team = -team;
	}
	
	@Override
	public Move findMove(Board board) {
		return findMove(board, 50, true, true, new ArrayList<Move>());
	}

	@Override
	public Move findMove(Board board, int iteration, boolean isConsole, boolean isDeveloperMode, List<Move> moveHistory) {
		this.isDeveloper = isDeveloperMode;
		avgNps = 0;
		totalNodes = 0;
		totalTime = 0;
		nodeMap.clear();
		moveMap.clear();
		if(isCheckMate(board, team)){
			return null;
		}else if(isStaleMate(board, team)){
			return MoveUtil.STALE_MATE;
		}

		if(iteration < LibraryLoader.MOVE_THRESHOLD && openings != null){
			Move opening = openings.findAdvancedOpening(board);
			if(opening != null){
				return opening;
			}
		}

		boolean mateFound = false;
		boolean mateIn2Found = false;
		List<Move> moves = getAllMoveForTeamMinMaxSpecialSorting(board, team);

		for(Move move: moves) {
			Board newBoard = move.updateBoard(board);
			if(isCheckMate(newBoard, -team)) {
				move.setScore(50000000);
				if(isDeveloperMode) {
					System.out.println(move + "\tMATE IN 1");
				}
				mateFound = true;
				break;
			}
			if(!mateIn2Found) {
				if(isMyNextMovePuttingHimToCheckMate(newBoard, team)) {
					if(isDeveloperMode) {
						System.out.println(move + "\tMATE IN 2");
					}
					move.setScore(25000000);
					mateIn2Found = true;
					mateFound = true;
				}else if(isForcedMate(newBoard, team, 0, false)) {
					if(isDeveloperMode) {
						System.out.println(move + "\tMATE IN 3+");
					}
					move.setScore(12500000);
					mateFound = true;
//				}else if(isForcedMate(newBoard, team, 0, true)) {
//					if(isDeveloperMode) {
//						System.out.println(move + "\tMATE IN 3+?");
//					}
//					move.setScore(9000000);
				}
			}
		}
		
		if(mateFound) {
			return Collections.max(moves, CustomComparator);
		}
		
		List<Move> mateMoves = new ArrayList<Move>();
		List<Move> oppMateMoves = new ArrayList<Move>();
		for(Move move : moves) {
			Board newBoard = move.updateBoard(board);
			List<Move> oppMoves = getAllMoveForTeamWithoutSuicides(newBoard, -team);

			if(isCheck(newBoard) && !isPieceThreatenedNew(newBoard, move.getPieceAfterMove())) {
				if(oppMoves.size() > 3) {
					continue;
				}
				int checkScore = 1000000000;
				for(Move oppMove : oppMoves) {
					Board newNewBoard = oppMove.updateBoard(newBoard);
					
					
					int thisCheckScore = keepCheckingUntilMate(newNewBoard, team);
					
					checkScore = Math.min(checkScore, thisCheckScore);
				}
				if(checkScore > 0) {
					if(isDeveloperMode) {
						System.out.println(move + "\tMATE IN " + (3+(1000000-checkScore))/2);
					}
					move.setScore(move.getScore() + checkScore);
					mateMoves.add(move);
				}
			}else {
				/*
				 * Bare in mind that this is without senseless captures. Sometimes senseless captures aren't exactly that when it comes to surprise checks. 
				 */
				int bestOppMoveScore = KING_VALUE;
				for(Move oppMove : oppMoves) {
					Board newNewBoard = oppMove.updateBoard(newBoard); 
					if(isCheck(newNewBoard, team) && !isPieceThreatenedNew(newNewBoard, oppMove.getPieceAfterMove())) {
						List<Move> myMoves = getAllMoveForTeamWithoutSuicides(newNewBoard, team);
						if(myMoves.size() > 2) {
							continue;
						}
						int checkScore = -KING_VALUE;
						
						for(Move myMove : myMoves) {
							Board newNewNewBoard = myMove.updateBoard(newNewBoard);
							
							
							int thisCheckScore = -keepCheckingUntilMate(newNewNewBoard, -team, 3, -1000000000, 100000000, 1, 3);
							
							checkScore = Math.max(checkScore, thisCheckScore);
							if(checkScore >=0) {
								break;
							}
						}
						if(checkScore < 0) {
							if(isDeveloperMode) {
								System.out.println(move + "\tMATE IN " + (3+(1000000+checkScore))/2 + " via " + oppMove);
							}
							bestOppMoveScore = Math.min(checkScore, bestOppMoveScore);
						}
					}
					if(bestOppMoveScore < 0) {
						break;
					}
				}
				move.setScore(move.getScore() + bestOppMoveScore);
				if(bestOppMoveScore < 0) {
					oppMateMoves.add(move);
				}
			}
		}
		
		if(!mateMoves.isEmpty()) {
			return Collections.max(mateMoves, CustomComparator);
		}
		if(!oppMateMoves.isEmpty()) {
			moves.removeAll(oppMateMoves);
			if(moves.isEmpty()) {
				return Collections.max(oppMateMoves, CustomComparator);
			}
		}
		
//		int mateScore = keepCheckingUntilMate(board, team);
//		if(mateScore > 0 && mateMoveMap.get(mateScore) != null){
//			moves = mateMoveMap.get(mateScore);
//			for(Move move : moves){
//				if(isDeveloperMode) {
//					System.out.println(move + "\tMATE IN " + (1+(1000000-mateScore))/2);
//				}
//				move.setScore(move.getScore() + mateScore);
//			}
//			return Collections.max(moves, CustomComparator);
//		}
		
		List<Piece> pieces = BoardUtil.getAllTeamPieces(board, team);
		List<Piece> oppPieces = BoardUtil.getAllTeamPieces(board, -team);
		if(pieces.size()==2 && Math.abs(pieces.get(0).getValue()+pieces.get(1).getValue()) == BoardUtil.QUEEN + BoardUtil.KING && oppPieces.size() == 1){
			return KingQueen.findMove(board, team);
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
				return new Move(0,0,0,0,0);
			}
		}

		List<Move> bestMoves = moveMap.get(score);
		if(bestMoves.size() == 1) {
			return bestMoves.get(0);
		}
		return pickFromBest(board, bestMoves, iteration, moveHistory);
	}

	private Move pickFromBest(Board board, List<Move> bestMoves, int iteration, List<Move> moveHistory) {
		for(Move move : bestMoves){
			Board newBoard = move.updateBoard(board);
			int score = 1000*getValue(move.getPieceCaptured());
			List<Piece> myPiecesAfterMove = BoardUtil.getAllTeamPiecesInPieces(newBoard, team);
			int greatestPieceInDanger = 0;
			for(Piece myPiece : myPiecesAfterMove){
				if(isPieceThreatenedNew(newBoard, myPiece)){
					greatestPieceInDanger = Math.max(greatestPieceInDanger, getValue(myPiece));
				}
			}
			score = score - 1000*greatestPieceInDanger; 
			
			int oppMoves = getMoveCountForTeam(newBoard, BoardUtil.getAllTeamPiecesInPieces(newBoard, -team));
			int myMoves = getMoveCountForTeam(newBoard, BoardUtil.getAllTeamPiecesInPieces(newBoard, team));
			score = score + myMoves-oppMoves;
			if(isCheck(newBoard, -team)){
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
			
			if(move.getPieceMoving()*team == BoardUtil.PAWN && iteration > 40) {
				int prod = 2;
				int n = team > 0 ? move.getToPos()[0]-1 : 6 - move.getToPos()[0];
				for(int i=0;i<n;i++) {
					prod*=2;
				}
				score = score + prod; 
			}
			if(move.getPieceMoving()*team == BoardUtil.PAWN && isPawnStackedIsolatedOrBlocked(board, move.getPieceAfterMove())) {
				score = score - getValueWithSign(move.getPieceAfterMove())/20;
			}
			
			if(isDeveloper) {
				System.out.println(move + "\t" + score);
			}
			move.setScore(move.getScore() + score);
		}
		
		Move bestMove = Collections.max(bestMoves, CustomComparator);
		
		return bestMove;
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


	public int alphaBetaMinMaxInitializer(Board board, List<Move> rootMoves){
		nodeMap.clear();
		moveMap.clear();

		int npmCutoff = MAX_NODE;
//		int npmCutoff = Math.max(MAX_NODE, (1500)/moveCount);
		
		
//		if(isCheck(board, team) || moveCount < 5) {
//			System.out.println("Hack: MAX_DEPTH = 7");
//			MAX_DEPTH = 7;
//		}else {
//			MAX_DEPTH = 5;
//		}
		
		if(isDeveloper) {
			System.out.println("MAX DEPTH:\t" + MAX_DEPTH);
			System.out.println("MAX NODE:\t" + npmCutoff);
		}
		return alphaBetaMinMax(board, team, MAX_DEPTH, -PSEUDO_INFINITE, PSEUDO_INFINITE, 1, null, npmCutoff, MAX_DEPTH, rootMoves);
	}
	
	public int alphaBetaMinMax(Board board, int team, int depth, int alpha, int beta, int maximizing, Move parentMove, int npmCutoff, int MAX_DEPTH, List<Move> rootMoves){
		
		if(isStaleMate(board, maximizing*team)) {
			return 0;
		}
		
		if(depth == 0){
			return heuristic(board, team);
		}
		
		int v = -maximizing * (PSEUDO_INFINITE - MAX_DEPTH + depth);
		List<Move> moves;
		if(depth == MAX_DEPTH) {
			moves = rootMoves;
		}else {
			moves = getAllMoveForTeam(board, maximizing*team);
//			moves = getAllMoveForTeamMinMaxSpecialSorting(board, maximizing*team);
		}
		if(moves.isEmpty()) {
			return v;
		}
		if(maximizing>0){
			Date tic = new Date();
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				if(depth == MAX_DEPTH) {
					parentMove = move;
					tic = new Date();
					nodeMap.put(move, 0);
				}
				
				if(MAX_DEPTH - depth >= PSEUDO_INFINITE - alpha) {
					break;
				}
				
				nodeMap.put(parentMove, nodeMap.get(parentMove)+1);
				int bestScore;
				if(nodeMap.get(parentMove) > npmCutoff){
					bestScore = alphaBetaMinMax(newBoard, team, 0, alpha, beta, -1, parentMove, npmCutoff, MAX_DEPTH, null);
				}else {
					bestScore = alphaBetaMinMax(newBoard, team, depth-1, alpha, beta, -1, parentMove, npmCutoff, MAX_DEPTH, null);
				}
				
				if(depth == MAX_DEPTH) {
					long time = new Date().getTime()-tic.getTime();
					if(time==0) time=1;
					int localNode = nodeMap.get(parentMove);
					if(moveMap.get(bestScore) == null){
						moveMap.put(bestScore, new ArrayList<Move>());
					}
					moveMap.get(bestScore).add(move);
					avgNps = avgNps + (1000*localNode)/((double)moves.size()*time);
					totalNodes = totalNodes + localNode;
					totalTime = totalTime + time;
					if(isDeveloper){
						System.out.println(move + "\t" + bestScore + "\t" + localNode + "\t" + time + "\t" + (1000*localNode)/time + "\tALPHA: " + alpha);
					}
//					if(totalTime > MAX_TOTAL_TIME) {
//						break;
//					}
					if(totalNodes > MAX_TOTAL_NODE) {
						v = Math.max(v, bestScore);
						break;
					}
				}
				
				v = Math.max(v, bestScore);
				alpha = Math.max(alpha, v);
				
				if(beta <= alpha){
					break; //Beta cut-off
				}
			}
			return v;
		}else{
//			moves = filterUnecessaryMoves(board, moves);
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				nodeMap.put(parentMove, nodeMap.get(parentMove)+1);
				int bestScore = alphaBetaMinMax(newBoard, team, depth-1, alpha, beta, 1, parentMove, npmCutoff, MAX_DEPTH, null);
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
	
	@Override
	public int getTeam() {
		return team;
	}
	
	public int keepCheckingUntilMate(Board board, int team){
		return keepCheckingUntilMate(board, team, 7 , -1000000000, 100000000, 1, 7);
		/*
		 * MAX DEPTH of 9 is what is required to detect smothered mate... Not sure how to perfectly optimize this.
		 * One idea was to remove stupid checks where a piece would throw themselves at the hand of the King and be captured next.
		 * Unfortunately that is exactly what is required here. The Queen sacrifices herself to smother the king by the rook which the knight then
		 * uses to check the king. 
		 * One thing that this isn't doing either is checking for repeated checks. I could remove those but without knowing the thread of moves that might be tricky.
		 * Was able to reduce it to 7. This worked because the root move is now in a loop. This allows adding restrictive limitations on the root move only.
		 * So in the case of the smothered mate, pieces can throw themselves at the king as long as it's later in the sequence. 
		 * I still need to filter out repetitive moves though...  
		 */
	}
	public int keepCheckingUntilMate(Board board, int team, int depth, int alpha, int beta, int maximizing, int MAX_DEPTH){
		if(isCheckMate(board, -team)){
			return KING_VALUE - (MAX_DEPTH - depth);
		}
		
		if(isStaleMate(board, team) || depth == 0){
			return 0;
		}
		
		int v = 0;
		if(maximizing>0){
			v = -(KING_VALUE - (MAX_DEPTH - depth));
			
			List<Move> moves = getAllMoveForTeam(board, team);
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				if(!isCheck(newBoard, -team)){
					/*
					 * Basically this is saying that only move forward if the opponent is in check AND that your piece that just moved isn't under attack.
					 * This assumes that the piece that just moved is doing the checking. What if this was a surprise check? Those are some of the most interesting. 
					 * There must be a way to allow for those while filtering for the senseless suicide checks. 
					 * 
					 * If opp's next move can recapture the piece, then it is a senseless check.
					 * I moved that restriction to the root moves. No need to restrict the moves later in the sequence as was demonstrated in the smothered mate.
					 */
					
					continue;
				}
				int bestScore = keepCheckingUntilMate(newBoard, team, depth-1, alpha, beta, -1, MAX_DEPTH);
				
				v = Math.max(v, bestScore);
				alpha = Math.max(alpha, v);
				if(beta < alpha){
					break; //Beta cut-off
				}
			}
			return v;
		}else{
			v = KING_VALUE - (MAX_DEPTH - depth);
			List<Move> moves = getAllMoveForTeamWithoutSuicides(board, -team);
			if(moves.size() > 3) {
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
	
	public static boolean isPieceCombinationSufficientForEndGame(Board board, int team) {
		List<Piece> highPieces = BoardUtil.getAllTeamHighPiecesInPieces(board, team);
		/*
		 * Combination of insufficient pieces:
		 * Empty Set
		 * Knight
		 * Knight + Knight
		 * Knight + Bishop (it's sufficient but it's hard. Convert the pawns if you can)
		 *  
		 */
		int sumPieceValue = 0;
		for(Piece piece : highPieces) {
			sumPieceValue = sumPieceValue + Math.abs(piece.getValue());
		}
		return (sumPieceValue >= BoardUtil.BISHOP*2) || sumPieceValue == BoardUtil.ROOK;
	}
	
	public static boolean shouldIRecruitPawnsForEndGame(Board board, int team) {
		List<Piece> highPieces = BoardUtil.getAllTeamHighPiecesInPieces(board, team);
		int sumPieceValue = 0;
		for(Piece piece : highPieces) {
			sumPieceValue += Math.abs(piece.getValue());
		}
		return sumPieceValue <= BoardUtil.QUEEN + BoardUtil.KNIGHT;
	}
	
	
	public final Comparator<Move> MoveComparator = new Comparator<Move>() {
		@Override
		public int compare(Move left, Move right) {
			int capturedPieceLeft = Math.abs(left.getPieceCaptured());
			int capturedPieceRight = Math.abs(right.getPieceCaptured());
			if(capturedPieceLeft==capturedPieceRight){
				int movingPieceLeft = Math.abs(left.getPieceMoving());
				int movingPieceRight = Math.abs(right.getPieceMoving());
				return Integer.valueOf(movingPieceLeft).compareTo(movingPieceRight);
			}
			return Integer.valueOf(capturedPieceRight).compareTo(capturedPieceLeft);
		}
	};
}