package com.ozone.movements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.ozone.common.AbsolutePin;
import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.evaluation.PieceTables;
import com.ozone.mate.PieceFinder;
import com.ozone.movements.BoardUtil;

public class MoveUtil {
	
	public static final Move STALE_MATE = new Move(0,0,0,0,0,0);
	
	public enum Status{
		DEFENDING,
		KING_QUEEN_MATE,
		CHECK_MATE_RECURSION,
		CHECK_MATE_0_YOU,
		CHECK_MATE_1_YOU,
		FORCE_PAWN_MOVEMENT_OR_CAPTURE_TO_AVOID_STALEMATE_THIS_IS_A_PAWN_MOVEMENT_OR_CAPTURE,
		FORCE_PAWN_MOVEMENT_OR_CAPTURE_TO_AVOID_STALEMATE_THIS_IS_NOT_A_PAWN_MOVEMENT_OR_CAPTURE,
		STALE_MATE_DESIRED,
		STALE_MATE_UNDESIRED,
		CHECK_MATE_1_OPP,
		FORCED_MATE_YOU,
		CHECK_MATE_2_OPP,
		MATE_FOUND,
		FORCED_MATE_OPP,
		BAD_OPENING_MOVE_QUEEN_OR_ROOK,
		BAD_OPENING_MOVE_KING,
		GOOD_OPENING_MOVE_KING_SIDE_KNIGHT_BISHOP,
		BAD_OPENING_MOVE_KING_SIDE_KNIGHT_BISHOP_BACK_ROW,
		GOOD_OPENING_MOVE_KING_PAWN,
		AVOID_CASTLE,
		CASTLING,
		KING_MOVEMENT,
		BAD_KNIGHT_MOVE,
		GOOD_MOVES,
		PROTECT_THE_CASTLE,
		STACKED_PAWNS_OPP,
		CAPTURES,
		THREATENED_EVERYWHERE_YOU,
		THREATENED_EVERYWHERE_OPP,
		STACKED_PAWNS_YOU,
		RECAPTURE_FROM_TRADE,
		TRADE_SIMPLIFY_THE_GAME,
		TRADE_PIECES_STACKED_PAWN,
		TRADE_UP_IN_PIECES,
		TRADE_DOWN_IN_PIECES,
		ADDING_THREATS,
		REMOVING_THREATS,
		SURPRISE_CHECKS_OPP,
		CAPTURE_PIECE_FROM_SURPRISE_CHECK,
		CHECKS_OPP,
		SURPRISE_CHECKS_YOU,
		CHECKS_YOU,
		PAWN_PROMOTION,
		PAWN_PROMOTION_LAST_STEP,
		PAWN_PROMOTION_THREAT_SERIOUS,
		PAWN_PROMOTION_PREVENTION_WELL_DONE,
		PAWN_PROMOTION_THREAT,
		PAWN_PROMOTION_OPP,
		PAWN_PROMOTION_PREVENTION,
		PAWN_PROMOTION_PREVENTION_LAST_ROW,
		PAWN_PROMOTION_CAPTURE,
		LOSING_PIECE_DUE_TO_PAWN_PROMOTION_CAPTURE,
		SKEWER_GAIN,
		SKEWER_LOSS,
		PINS_GAIN,
		PINS_THREAT_IS_REAL,
		PINS_LOSS,
		SURPRISE_CAPTURE,
		CURRENT_FORK_GAIN,
		PENALTY_FOR_FORKING_INSTEAD_OF_CAPTURING,
		CURRENT_FORK_LOSS,
		TRADE_WELCOME,
		TRADE_THREAT,
		ENABLING_MOVES,
		OBSTRUCTING_MOVES,
		PROTECTED,
		THIS_MOVE_IS_MAKING_THINGS_WORSE,
		FUTURE_FORK_GAIN,
		IS_CURRENT_MOVING_PIECE_THREATENED_AFTER_MOVE,
		WAS_ONE_OF_HIS_PIECES_THREATENED_AND_THEN_KILLED,
		CAPTURED_PAWN_ABOUT_TO_PROMOTE,
		IS_ONE_OF_MY_PIECES_THREATENED,
		THREAT_OF_ISOLATED_PAWN,
		THREAT_OF_GOOD_PAWN,
		BACK_ROW_PIECES,
		THREATEN_PAWN_ABOUT_TO_PROMOTE,
		WAS_ONE_OF_HIS_PIECES_NOT_THREATENED_BUT_NOW_IS_THREATENED,
	};
	
	public static Board updateBoardSimple(Board board, Move move) {
		Board finalBoard = new Board(board.getBoard());
		finalBoard.movePieceSimple(move);
		return finalBoard;
	}
	
	public static boolean isCheckMate(Board board, int team){
		
		if(!isCheck(board, team)){
			return false;
		}
	
		
		for(Move move : getAllMoveForTeam(board, team)){
			if(isInCheckMoveValid(board, move)){
				return false;
			}
		}

		return true;
	}
	
	public static boolean isCheck(Board board){
		return isCheck(board, BoardUtil.WHITE) || isCheck(board, BoardUtil.BLACK);
	}

	public static boolean isCheck(Board board, int team){
		return isCheck(board, new Piece(PieceFinder.findKing(board, team)));
	}
	public static boolean isCheck(Board board, Piece king){

		int team = king.getValue() > 0 ? 1:-1;
		
		if(team > BoardUtil.SPACE){
			for(int i=0;i<BoardUtil.MAX_ROW;i++){
				for(int j=0;j<BoardUtil.MAX_COL;j++){
					if(board.getPiece(i,j) < BoardUtil.SPACE){
						if(isPieceThreateningOppKing(board, new Piece(board.getPiece(i, j), i, j), king)){
							return true;
						}
					}
				}
			}
		}
			
		if(team < BoardUtil.SPACE){
			for(int i=BoardUtil.MAX_ROW-1;i>-1;i--){
				for(int j=0;j<BoardUtil.MAX_COL;j++){
					if(board.getPiece(i,j) > BoardUtil.SPACE){
						if(isPieceThreateningOppKing(board, new Piece(board.getPiece(i, j), i, j), king)){
							return true;
						}
					}
				}
			}
		}
			
		return false;
	}
	
	public static boolean isThreatenedVeryNaiveApproach(Board board, int[] pieceWithPos){
		return isThreatenedVeryNaiveApproach(board, pieceWithPos[0], pieceWithPos[1], pieceWithPos[2]);
	}
	
	public static List<Move> filterRandomRookMovements(Board board, List<Move> moves, int team){
		List<Move> movesWithoutRandomMoveMovements = new ArrayList<Move>();
		for(Move move : moves){
			if(isRandomRookMovement(board, move, team)){
				continue;
			}
			movesWithoutRandomMoveMovements.add(move);
		}
		return movesWithoutRandomMoveMovements;
	}
	
	public static boolean isRandomRookMovement(Board board, Move move, int team){
		if(Math.abs(move.getPieceMoving()) != BoardUtil.ROOK){
			return false;
		}
		
		if(isPieceThreatenedNew(board, move.getPieceBeforeMove())){
			return false;
		}
		
		if(move.getPieceCaptured() != 0){
			return false;
		}
		
		if(move.getToPos()[0] != (team == BoardUtil.WHITE ? 0 : 7)){
			return false;
		}
		
		if(isFileOpen(move.updateBoard(board), move.getToPos()[1], team)){
			return false;
		}

		return true;
	}
	
	public static boolean isFileOpen(Board board, int xPos, int team) {
		for(int i=(team==1?1:6);i!=(team==1?7:0);i=i+team){
			int pieceOnBoard = board.getPiece(i,xPos);
			if(pieceOnBoard == BoardUtil.SPACE){
				continue;
			}
			if(pieceOnBoard*team > BoardUtil.BISHOP){
				return true;
			}
			if(pieceOnBoard*team > BoardUtil.SPACE){
				return false;
			}
		}
		return true;
	}

	public static int getMoveCountForTeam(Board board, int team){
		return getMoveCountForTeam(board, BoardUtil.getAllTeamPiecesInPieces(board, team));
	}
	public static int getMoveCountForTeam(Board board, List<Piece> pieces){
		int moveCount = 0;
		for(Piece piece : pieces){
			List<Move> moves = findVanillaMovesForPiece(piece);
			for(Move move:moves){
				if(Math.abs(board.getPiece(move.getToPos())) == BoardUtil.KING) continue;
				
				if(isCheck(board, piece.getTeam())){
					moveCount = moveCount + (isInCheckMoveValid(board, move) ? 1:0);
				}else {
					moveCount = moveCount + (isNotInCheckMoveValid(board, move) ? 1:0);
				}
			}
		}
		return moveCount;
	}

	
	public static List<Move> getAllMoveForTeamWithoutSuicides(Board board, int team){
		List<Move> moves = getAllMoveForTeam(board, team);
		List<Move> filteredMoves = new ArrayList<Move>();
		for(Move move : moves){
			Board newBoard = move.updateBoard(board);
			Piece pieceAfterMove = new Piece(move.getPieceMoving(), move.getToPos());
			if(isPieceThreatenedNew(newBoard, pieceAfterMove) && getValue(move.getPieceMoving()) > getValue(move.getPieceCaptured())){
				//This is a suicide.
				//This also could be a sacrifice but at this point the engine isn't smart enough to make these kind of judgements.
				continue;
			}
			filteredMoves.add(move);
		}
		return filteredMoves;
	}
	
	public enum MoveType {
		CASTLES,
		CHECKS,
		PAWNS,
		PROTECTS,
		FREE_CAPTURES,
		LOSING_BATTLES,
		WINNING_BATTLES,
		TRADES,
		THREATS,
		ATTACKS,
		RANDOM,
	}
	
	public static List<Move> getAllMoveForTeamMinMaxSpecialSortingOnlyTheBest(Board board, int team){
		List<Move> castles = new ArrayList<Move>();
		List<Move> moves = new ArrayList<Move>();
		List<Move> checks = new ArrayList<Move>();
		List<Move> pawns = new ArrayList<Move>();
		List<Move> protects = new ArrayList<Move>();
		List<Move> trades = new ArrayList<Move>();
		List<Move> winningBattle = new ArrayList<Move>();
		List<Move> freeCaptures = new ArrayList<Move>();
		List<Move> threats = new ArrayList<Move>();
		List<Move> attacks = new ArrayList<Move>();
		List<Piece> pieces = BoardUtil.getAllTeamPiecesInPieces(board, team);
		HashMap<Move, MoveType> moveTypeMap  = new HashMap<Move, MoveType>();
		
		for(Piece piece : pieces){
			for(Move move : findAllValidMoves(board, piece)){
				Board newBoard = move.updateBoard(board);
				if(piece.getValue()*team == BoardUtil.KING && Math.abs(move.getToPos()[1]-move.getFromPos()[1]) == 2) {
					castles.add(move);
					moveTypeMap.put(move, MoveType.CASTLES);
				}else if(isCheck(newBoard) && !isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
					checks.add(move);
					moveTypeMap.put(move, MoveType.CHECKS);
				}else if(move.getPieceCaptured() != 0){
					if(!isPieceThreatenedNew(newBoard, move.getPieceAfterMove())) {
						freeCaptures.add(move);
						moveTypeMap.put(move, MoveType.FREE_CAPTURES);
					}else {
						int diff = getValue(move.getPieceCaptured()) - getValue(move.getPieceMoving());
						if(diff > 0) {
							winningBattle.add(move);
							moveTypeMap.put(move, MoveType.WINNING_BATTLES);
						}else if(diff == 0){
							trades.add(move);
							moveTypeMap.put(move,  MoveType.TRADES);
						}
					}
				}else if(isPieceThreatenedNew(newBoard, move.getPieceAfterMove())) {
					threats.add(move);
					moveTypeMap.put(move, MoveType.THREATS);
				}else if(findAttackPieces(newBoard, move.getPieceAfterMove()).size() > 0){
					attacks.add(move);
					moveTypeMap.put(move, MoveType.ATTACKS);
				}else if(isPieceThreatenedNew(board, move.getPieceBeforeMove()) && !isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
					protects.add(move);
					moveTypeMap.put(move, MoveType.PROTECTS);
				}else if(piece.getValue()*team == BoardUtil.PAWN && (piece.getyPosition()-3.5)*team > 0) {
					pawns.add(move);
					moveTypeMap.put(move, MoveType.PAWNS);
				}
			}
		}
		Collections.sort(freeCaptures, MoveComparator);
		Collections.sort(trades, MoveComparator);
		Collections.sort(winningBattle, BattleComparator);
		Collections.sort(attacks, MoveComparator);
		Collections.sort(protects, ProtectsComparator);
		Collections.sort(pawns, PawnComparator);
		Collections.sort(checks, MoveComparator);
		
		moves.addAll(castles);
		moves.addAll(checks);
		moves.addAll(freeCaptures);
		moves.addAll(winningBattle);
		moves.addAll(protects);
		moves.addAll(trades);
		moves.addAll(attacks);
		moves.addAll(pawns);
		
		//TODO: Remember to comment this out after testing
//		System.out.println("Remember to comment this out after testing (MoveUtil)");
//		for(Move move : moves) {
//			System.out.println(moveTypeMap.get(move) + "\t" + move);
//		}
		
		return moves;
	}
	
	
	public static List<Move> getAllMoveForTeamMinMaxSpecialSorting(Board board, int team){
		List<Move> castles = new ArrayList<Move>();
		List<Move> moves = new ArrayList<Move>();
		List<Move> checks = new ArrayList<Move>();
		List<Move> pawns = new ArrayList<Move>();
		List<Move> protects = new ArrayList<Move>();
		List<Move> trades = new ArrayList<Move>();
		List<Move> losingBattle = new ArrayList<Move>();
		List<Move> winningBattle = new ArrayList<Move>();
		List<Move> freeCaptures = new ArrayList<Move>();
		List<Move> threats = new ArrayList<Move>();
		List<Move> attacks = new ArrayList<Move>();
		List<Move> random = new ArrayList<Move>();
		List<Piece> pieces = BoardUtil.getAllTeamPiecesInPieces(board, team);
		HashMap<Move, MoveType> moveTypeMap  = new HashMap<Move, MoveType>();
		
		for(Piece piece : pieces){
			for(Move move : findAllValidMoves(board, piece)){
				Board newBoard = move.updateBoard(board);
				if(piece.getValue()*team == BoardUtil.KING && Math.abs(move.getToPos()[1]-move.getFromPos()[1]) == 2) {
					castles.add(move);
					moveTypeMap.put(move, MoveType.CASTLES);
				}else if(isCheck(newBoard) && !isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
					checks.add(move);
					moveTypeMap.put(move, MoveType.CHECKS);
				}else if(move.getPieceCaptured() != 0){
					if(!isPieceThreatenedNew(newBoard, move.getPieceAfterMove())) {
						freeCaptures.add(move);
						moveTypeMap.put(move, MoveType.FREE_CAPTURES);
					}else {
						int diff = getValue(move.getPieceCaptured()) - getValue(move.getPieceMoving());
						if(diff > 0) {
							winningBattle.add(move);
							moveTypeMap.put(move, MoveType.WINNING_BATTLES);
						}else if(diff < 0) {
							losingBattle.add(move);
							moveTypeMap.put(move, MoveType.LOSING_BATTLES);
						}else {
							trades.add(move);
							moveTypeMap.put(move,  MoveType.TRADES);
						}
					}
				}else if(isPieceThreatenedNew(newBoard, move.getPieceAfterMove())) {
					threats.add(move);
					moveTypeMap.put(move, MoveType.THREATS);
				}else if(findAttackPieces(newBoard, move.getPieceAfterMove()).size() > 0){
					attacks.add(move);
					moveTypeMap.put(move, MoveType.ATTACKS);
				}else if(isPieceThreatenedNew(board, move.getPieceBeforeMove()) && !isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
					protects.add(move);
					moveTypeMap.put(move, MoveType.PROTECTS);
				}else if(piece.getValue()*team == BoardUtil.PAWN && (piece.getyPosition()-3.5)*team > 0) {
					pawns.add(move);
					moveTypeMap.put(move, MoveType.PAWNS);
				}else {
					random.add(move);
					moveTypeMap.put(move, MoveType.RANDOM);
				}
			}
		}
		Collections.sort(freeCaptures, MoveComparator);
		Collections.sort(trades, MoveComparator);
		Collections.sort(winningBattle, BattleComparator);
		Collections.sort(losingBattle, BattleComparator);
		Collections.sort(threats, RandomComparator);
		Collections.sort(attacks, MoveComparator);
		Collections.sort(protects, ProtectsComparator);
		Collections.sort(pawns, PawnComparator);
		Collections.sort(checks, MoveComparator);
		Collections.sort(random, RandomComparator);
		
		moves.addAll(castles);
		moves.addAll(checks);
		moves.addAll(freeCaptures);
		moves.addAll(winningBattle);
		moves.addAll(protects);
		moves.addAll(trades);
		moves.addAll(attacks);
		moves.addAll(pawns);
		moves.addAll(random);
		moves.addAll(losingBattle);
		moves.addAll(threats);
		
		
		//TODO: Remember to comment this out after testing
//		System.out.println("Remember to comment this out after testing (MoveUtil)");
//		for(Move move : moves) {
//			System.out.println(moveTypeMap.get(move) + "\t" + move);
//		}
		
		return moves;
	}
	
	public static List<Move> getAllMoveForPieces(Board board, List<Piece> pieces){
		List<Move> moves = new ArrayList<Move>();
		for(Piece piece : pieces) {
			for(Move move : findAllValidMoves(board, piece)){
				moves.add(move);
			}
		}
		return moves;
	}
	
	public static List<Move> getAllGoodMovesForTeam(Board board, int team){
		List<Move> moves = new ArrayList<Move>();
		List<Move> checkMoves = new ArrayList<Move>();
		List<Piece> pieces = BoardUtil.getAllTeamPiecesInPieces(board, team);
		for(Piece piece : pieces){
			for(Move move : findAllValidMoves(board, piece)){
				if(!isBadMove(board, move)){
					if(isCheck(move.updateBoard(board))){
						checkMoves.add(move);
					}else {
						moves.add(move);
					}
				}
			}
		}
		Collections.sort(moves, MoveComparator);
		Collections.sort(checkMoves, MoveComparator);
		checkMoves.addAll(moves);
		return checkMoves;
	}
	
	public static List<Move> getAllMoveForTeam(Board board, int team){
		List<Move> moves = new ArrayList<Move>();
		List<Move> checkMoves = new ArrayList<Move>();
		List<Piece> pieces = BoardUtil.getAllTeamPiecesInPieces(board, team);
		for(Piece piece : pieces){
			for(Move move : findAllValidMoves(board, piece)){
				if(isCheck(move.updateBoard(board))){
					checkMoves.add(move);
				}else {
					moves.add(move);
				}
			}
		}
		Collections.sort(moves, MoveComparator);
		Collections.sort(checkMoves, MoveComparator);
		checkMoves.addAll(moves);
		return checkMoves;
	}

	public static boolean isThreatenedVeryNaiveApproach(Board board, int piece, int y, int x){
		return isThreatenedVeryNaiveApproach(board, piece, y, x, true);
	}
	public static boolean isThreatenedVeryNaiveApproach(Board board, int piece, int y, int x, boolean includeKing){
		int team = Math.abs(piece)/piece;
		List<Piece> hisPieces = BoardUtil.getAllTeamPieces(board, -team);
		for(Piece hisPiece: hisPieces){
			int pieceThreat = hisPiece.getValue();
			int i = hisPiece.getyPosition();
			int j = hisPiece.getxPosition();
			switch(Math.abs(pieceThreat)){
			case(BoardUtil.PAWN):
				if(Math.abs(x-j) == 1 && y==i-piece/Math.abs(piece))	return true;
				if((i==0 && pieceThreat < 0) || (i==7 && pieceThreat > 0)){
					if(isEmptyBetweenPieces(board, BoardUtil.QUEEN, i, j, y, x))	return true;
				}
				break;
			case(BoardUtil.KNIGHT):
				if(Math.abs(x-j)==2 && Math.abs(y-i)==1) return true;
				if(Math.abs(x-j)==1 && Math.abs(y-i)==2) return true;
				break;
			case(BoardUtil.BISHOP):
				if(isEmptyBetweenPieces(board, BoardUtil.BISHOP, i, j, y, x)) return true;
				break;
			case(BoardUtil.ROOK):
				if(isEmptyBetweenPieces(board, BoardUtil.ROOK, i, j, y, x)) return true;
				break;
			case(BoardUtil.QUEEN):
				if(isEmptyBetweenPieces(board, BoardUtil.QUEEN, i, j, y, x)) return true;
				break;
			case(BoardUtil.KING):
				if(includeKing){
					if(Math.abs(x-j) < 2 && Math.abs(y-i) < 2) return true;
					break;
				}
			}
		}
		return false;
	}
	
	public static boolean isEmptyBetweenPieces(Board board, int attackingPiece, int y1, int x1, int y2, int x2) {
		switch(Math.abs(attackingPiece)){
		case(BoardUtil.BISHOP):
			if(!(Math.abs(y2-y1) == Math.abs(x2-x1))){
				return false;
			}else if(x2 == x1){
				return false;
			}else{
				int slope = (y2-y1)/(x2-x1);
				int firstY = y1;
				if(Math.min(x2, x1) != x1){
					firstY = y2;
				}
				for(int i=Math.min(x1, x2)+1;i<Math.max(x1, x2); i++){
					if(isSpaceOccupied(board, (i-Math.min(x1,x2))*slope+firstY, i)){
						return false;
					}
				}
				return true;
			}
		case(BoardUtil.ROOK):
			if(y2 == y1){
				for(int i=Math.min(x1, x2)+1;i<Math.max(x1, x2); i++){
					if(isSpaceOccupied(board, y2, i)){
						return false;
					}
				}
				return true;
			}else if(x2 == x1){
				for(int i=Math.min(y1, y2)+1;i<Math.max(y1, y2); i++){
					if(isSpaceOccupied(board, i, x2)){
						return false;
					}
				}
				return true;
			}
			break;
		case(BoardUtil.QUEEN):
			return (isEmptyBetweenPieces(board, BoardUtil.ROOK, y1, x1, y2, x2) || isEmptyBetweenPieces(board, BoardUtil.BISHOP, y1, x1, y2, x2));
		}
		return false;
	}
	
	public static boolean isSpaceOccupiedByOwnPiece(Board board, int piece, int y, int x) {
		if(!isSpaceOccupied(board, y, x)) return false;
		int team2 = board.getPiece(y,x)/Math.abs(board.getPiece(y,x));
		int team1 = piece/Math.abs(piece); 
		return team1 == team2;
	}
	
	public static boolean isSpaceOccupied(Board board, int y, int x) {
		if(BoardUtil.isPositionOutOfBounds(y, x)) return false;
		if(board.getPiece(y,x) == 0){
			return false;
		}
		return true;
	}
	
	public static List<List<Move>> findVanillaLinesOfMovesForPiece(int[] pieceWithPos){
		List<List<Move>> linesOfMoves = new ArrayList<List<Move>>();
		int piece = pieceWithPos[0];
		if(Math.abs(piece) < BoardUtil.BISHOP){
			return linesOfMoves;
		}
		int y0 = pieceWithPos[1];
		int x0 = pieceWithPos[2];
		switch(Math.abs(piece)){
		case(BoardUtil.BISHOP):
			return generateBishopLines(piece, y0, x0); 
		case(BoardUtil.ROOK):
			return generateRookLines(piece, y0, x0);
		case(BoardUtil.QUEEN):
			linesOfMoves = generateRookLines(piece, y0, x0);
			linesOfMoves.addAll(generateBishopLines(piece, y0, x0));
			return linesOfMoves;
		}
		return linesOfMoves;
	}
	
	public static List<Move> findVanillaMovesForPiece(Piece piece){
		return findVanillaMovesForPiece(piece.toIntArray());
	}
	
	public static List<Move> findVanillaMovesForPiece(int[] pieceWithPos){
		return findVanillaMovesForPiece(pieceWithPos[0], pieceWithPos[1], pieceWithPos[2]);
	}
	public static List<Move> findVanillaMovesForPiece(int piece, int y0, int x0){
		
		if(piece==BoardUtil.SPACE)
			return new ArrayList<Move>();

		switch(Math.abs(piece)){
		case(BoardUtil.BISHOP):
			return generateBishopMovements(piece, y0, x0); 
		case(BoardUtil.KING):
			return generateKingMovements(piece, y0, x0);
		case(BoardUtil.KNIGHT):
			return generateKnightMovements(piece, y0, x0);
		case(BoardUtil.PAWN):
			return generatePawnMovements(piece, y0, x0);
		case(BoardUtil.ROOK):
			return generateRookMovements(piece, y0, x0);
		case(BoardUtil.QUEEN):
			List<Move> moves = generateRookMovements(piece, y0, x0);
			moves.addAll(generateBishopMovements(piece, y0, x0));
			return moves;
		}
		return new ArrayList<Move>();
	}

	/*
	 * This method returns the pieces attacked by piece
	 */
	public static List<Piece> findAttackPieces(Board board, Piece piece){
		List<Move> moves = findAllValidMoves(board, piece);
		List<Piece> attacks = new ArrayList<Piece>();
		for(Move move : moves){
			if(board.getPiece(move.getToPos()) != BoardUtil.SPACE){
				if(Math.abs(board.getPiece(move.getToPos())) > Math.abs(move.getPieceMoving()) || Math.abs(board.getPiece(move.getToPos()))== BoardUtil.KING){
					attacks.add(new Piece(board, move.getToPos()));
				}else{
					Board newBoard = move.updateBoard(board);
					if(!isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
						attacks.add(new Piece(board, move.getToPos()));
					}
				}
			}
		}
		return attacks;
	}

	public static List<Move> generateKingMovements(int piece, int y0, int x0){
		List<Move> moves = new ArrayList<Move>();
		for(int x=x0-1;x<x0+2;x++){
			for(int y=y0-1;y<y0+2;y++){
				if(BoardUtil.isPositionOutOfBounds(y, x) || (x==x0 && y==y0)){
					continue;
				}else{
					moves.add(new Move(piece, BoardUtil.SPACE, y0, x0, y, x));
				}
			}
		}
		//Castling
		if(x0 == BoardUtil.KING_POSITION && ((piece > 0 && y0 == 0) || (piece < 0 && y0 == 7))){
			moves.add(new Move(piece, y0, x0, y0, x0 + 2));
			moves.add(new Move(piece, y0, x0, y0, x0 - 2));
		}
		return moves;
	}
	public static List<Move> generatePawnMovements(int piece, int y0, int x0){
		int team = piece/Math.abs(piece);
		int newPiece = piece;
		if(piece == BoardUtil.PAWN && y0 == 7) newPiece = BoardUtil.QUEEN;
		if(piece == -BoardUtil.PAWN && y0 == 0) newPiece = -BoardUtil.QUEEN;
		List<Move> moves = new ArrayList<Move>();
		moves.add(new Move(newPiece, y0, x0, y0 + team, x0));
		if(piece==BoardUtil.PAWN && y0 == 1) moves.add(new Move(newPiece, y0, x0, y0 + 2, x0));
		if(piece==-BoardUtil.PAWN && y0 == 6) moves.add(new Move(newPiece, y0, x0, y0 - 2, x0));
		if(x0 > 0) moves.add(new Move(newPiece, y0, x0, y0 + team, x0-1));
		if(x0 < 7) moves.add(new Move(newPiece, y0, x0, y0 + team, x0+1));
		
		return moves;
	}
	public static List<Move> generateKnightMovements(int piece, int y0, int x0){
		List<Move> moves = new ArrayList<Move>();
		if(y0+2 < 8 && x0+1<8) moves.add(new Move(piece, y0, x0, y0+2,x0+1));
		if(y0+1 < 8 && x0+2<8) moves.add(new Move(piece, y0, x0, y0+1,x0+2));
		if(y0-1 > -1 && x0+2<8) moves.add(new Move(piece, y0, x0, y0-1,x0+2));
		if(y0-2 > -1 && x0+1<8) moves.add(new Move(piece, y0, x0, y0-2,x0+1));
		if(y0-2 > -1 && x0-1>-1) moves.add(new Move(piece, y0, x0, y0-2,x0-1));
		if(y0-1 > -1 && x0-2>-1) moves.add(new Move(piece, y0, x0, y0-1,x0-2));
		if(y0+1 < 8 && x0-2>-1) moves.add(new Move(piece, y0, x0, y0+1,x0-2));
		if(y0+2 < 8 && x0-1>-1) moves.add(new Move(piece, y0, x0, y0+2,x0-1));
		return moves;
	}
	
	public static List<List<Move>> generateBishopLines(int piece, int y0, int x0){
		List<List<Move>> linesOfMoves = new ArrayList<List<Move>>();
		List<Move> lineOfMoves1 = new ArrayList<Move>();
		List<Move> lineOfMoves2 = new ArrayList<Move>();
		List<Move> lineOfMoves3 = new ArrayList<Move>();
		List<Move> lineOfMoves4 = new ArrayList<Move>();
		
		int x=x0+1;
		int y=y0+1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			lineOfMoves1.add(new Move(piece,y0,x0,y,x));
			x++;
			y++;
		}
		linesOfMoves.add(lineOfMoves1);
		x=x0-1;
		y=y0+1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			lineOfMoves2.add(new Move(piece,y0,x0,y,x));
			x--;
			y++;
		}
		linesOfMoves.add(lineOfMoves2);
		x=x0-1;
		y=y0-1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			lineOfMoves3.add(new Move(piece,y0,x0,y,x));
			x--;
			y--;
		}
		linesOfMoves.add(lineOfMoves3);
		x=x0+1;
		y=y0-1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			lineOfMoves4.add(new Move(piece,y0,x0,y,x));
			x++;
			y--;
		}
		linesOfMoves.add(lineOfMoves4);
		return linesOfMoves;
	}
	public static List<Move> generateBishopMovements(int piece, int y0, int x0){
		List<Move> moves = new ArrayList<Move>();
		int x=x0+1;
		int y=y0+1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			moves.add(new Move(piece,y0,x0,y,x));
			x++;
			y++;
		}
		x=x0-1;
		y=y0+1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			moves.add(new Move(piece,y0,x0,y,x));
			x--;
			y++;
		}
		x=x0-1;
		y=y0-1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			moves.add(new Move(piece,y0,x0,y,x));
			x--;
			y--;
		}
		x=x0+1;
		y=y0-1;
		while(!BoardUtil.isPositionOutOfBounds(y, x)){
			moves.add(new Move(piece,y0,x0,y,x));
			x++;
			y--;
		}
		return moves;
	}
	
	public static List<List<Move>> generateRookLines(int piece, int y0, int x0){
		List<List<Move>> linesOfMoves = new ArrayList<List<Move>>();
		List<Move> lineOfMove1 = new ArrayList<Move>();
		List<Move> lineOfMove2 = new ArrayList<Move>();
		List<Move> lineOfMove3 = new ArrayList<Move>();
		List<Move> lineOfMove4 = new ArrayList<Move>();
		for(int x=x0+1;x<BoardUtil.MAX_COL;x++){
			lineOfMove1.add(new Move(piece, y0, x0, y0, x));
		}
		for(int x=0;x<x0;x++){
			lineOfMove2.add(new Move(piece, y0, x0, y0, x));
		}
		Collections.reverse(lineOfMove2);
		for(int y=y0+1;y<BoardUtil.MAX_ROW;y++){
			lineOfMove3.add(new Move(piece, y0, x0, y, x0));
		}
		for(int y=0;y<y0;y++){
			lineOfMove4.add(new Move(piece, y0, x0, y, x0));
		}
		Collections.reverse(lineOfMove4);
		linesOfMoves.add(lineOfMove1);
		linesOfMoves.add(lineOfMove2);
		linesOfMoves.add(lineOfMove3);
		linesOfMoves.add(lineOfMove4);
		return linesOfMoves;
	}
	public static List<Move> generateRookMovements(int piece, int y0, int x0){
		List<Move> moves = new ArrayList<Move>();
		for(int x=x0+1;x<BoardUtil.MAX_COL;x++){
			moves.add(new Move(piece, y0, x0, y0, x));
		}
		for(int x=0;x<x0;x++){
			moves.add(new Move(piece, y0, x0, y0, x));
		}
		for(int y=y0+1;y<BoardUtil.MAX_ROW;y++){
			moves.add(new Move(piece, y0, x0, y, x0));
		}
		for(int y=0;y<y0;y++){
			moves.add(new Move(piece, y0, x0, y, x0));
		}
		return moves;
	}
	
	public static List<Move> findAllValidMoves(Board board, Piece piece){
		return findAllValidMoves(board, piece.toIntArray());
	}
	public static List<Move> findAllValidMoves(Board board, int[] piece){
		return findAllValidMoves(board, piece[0], piece[1], piece[2]);
	}
	
	public static List<Move> findAllValidMoves(Board board, int piece, int y1, int x1){
		List<Move> validMoves = new ArrayList<Move>();
		boolean includeKingCapture = true;
		List<Move> moves = findVanillaMovesForPiece(new int[]{piece, y1, x1});
		for(Move move:moves){
			if(isMoveValid(board, move, includeKingCapture)){
				move.setPieceCaptured(board.getPiece(move.getToPos()));
				validMoves.add(move);
			}
		}

		return validMoves;
	}
	
	public static boolean isMoveValid(Board board, Move move){
		return isMoveValid(board, move.getPieceMoving(), move.getFromPos()[0], move.getFromPos()[1], move.getToPos()[0], move.getToPos()[1], false);
	}
	public static boolean isMoveValid(Board board, Move move, boolean includeKingCapture){
		return isMoveValid(board, move.getPieceMoving(), move.getFromPos()[0], move.getFromPos()[1], move.getToPos()[0], move.getToPos()[1], includeKingCapture);
	}
	public static boolean isMoveValid(Board board, int piece, int y1, int x1, int y2, int x2, boolean includeKingCapture){
		if(board.getPiece(y1,x1) == BoardUtil.SPACE) return false;
		if(Math.abs(board.getPiece(y2,x2)) == BoardUtil.KING && !includeKingCapture) return false;
		if(x1==x2 && y1==y2) return false;
		if(BoardUtil.isPositionOutOfBounds(y2, x2)) return false;
		
		if(isCheck(board, piece/Math.abs(piece))){
			return isInCheckMoveValid(board, piece, y1, x1, y2, x2);
		}else {
			return isNotInCheckMoveValid(board, piece, y1, x1, y2, x2);
		}
	}

	public static boolean isStaleMate(Board board, int team){
		
		for(Piece piece : BoardUtil.getAllTeamPieces(board, team)) {
			List<Move> moves = findVanillaMovesForPiece(piece);
			for(Move move:moves){
				if(isMoveValid(board, move, false)){
					return false;
				}
			}
		}
		
		if(isCheck(board, team)){
			return false;
		}
		
		return true;
	}
	
	
	public static boolean isStaleMateForAlphaBeta(Board board, List<Move> moves, int team){
		if(moves.size() > 0 || isCheck(board, team)){
			return false;
		}
		return true;
	}
	
	public static int getValueWithSign(Piece piece){
		return getValueWithSign(piece.getValue(), piece.getyPosition(), piece.getxPosition());
	}
	public static int getValueWithSign(int piece, int y, int x){
		int sign = piece > 0 ? 1 : -1;
		switch(Math.abs(piece)){
		case (BoardUtil.PAWN):
			return sign*(100 - ((x==0 || x==7)?15:0) + PieceTables.PAWN_TABLE[sign<0?y:7-y][x]);
		case (BoardUtil.KNIGHT):
			return sign*(300 + PieceTables.KNIGHT_TABLE[sign<0?y:7-y][x]);
		case (BoardUtil.BISHOP):
			return sign*(300 + PieceTables.BISHOP_TABLE[sign<0?y:7-y][x]);
		case (BoardUtil.ROOK):
			return sign*500;
		case (BoardUtil.QUEEN):
			return sign*900;
		case (BoardUtil.KING):
			return sign*(1000000 + PieceTables.KING_TABLE[sign<0?y:7-y][x]);
		}
		return 0;
	}
	
	public static int getValue(Piece piece){
		return getValue(piece.getValue());
	}
	public static int getValue(int piece){
		switch(Math.abs(piece)){
		case (BoardUtil.PAWN):
			return 100;
		case (BoardUtil.KNIGHT):
		case (BoardUtil.BISHOP):
			return 300;
		case (BoardUtil.ROOK):
			return 500;
		case (BoardUtil.QUEEN):
			return 900;
		case (BoardUtil.KING):
			return 20000;
		}
		return 0;
	}
	
	public static boolean isPieceThreatened(Board board, int[] piece) {
		return isPieceThreatenedNew(board, new Piece(piece));
	}
	public static boolean isPieceThreatened(Board board, int piece, int y, int x) {
		return isPieceThreatenedNew(board, new Piece(piece, y, x));
	}
	public static boolean isPieceThreatened(Board board, int piece, int[] yx) {
		return isPieceThreatenedNew(board, new Piece(piece, yx));
	}
	
	
	public static boolean isPieceThreatenedNew(Board board, Piece piece){
		if(isSurpriseCheck(board, piece)) {
			return false;
		}
		List<Piece> threatsForPieceUnfiltered = findThreatsAccurate(board, piece, true);
		/*
		 * Note this method only removes King pins. threats that are pinned to high powered pieces like 
		 * Queens or Rooks aren't filtered out. This is a limitation of the algorithm and must be handled
		 * elsewhere.  
		 */
		List<Piece> threatsForPiece = filterPinnedPieces(board, threatsForPieceUnfiltered);
		//TODO: ^THERE IS A BUG WITH FILTERPINNEDPIECES WHICH LED THE QUEEN TO GIVE HERSELF UP. If the pawn is pinned to the opposite queen the original queen will have no problem moving to the side.
		//2r2r2/pb3pk1/3pp2p/3p1p2/2qP4/1PP1PN2/P1QR1PPP/2R4K b - - 0 1 C4B4.... Terrible. Remove non King pins.
		/*
		 * Case 1: If you have no threats, then obviously the answer is no.
		 */
		if(threatsForPiece.size() == 0){
			return false;
		}
		Collections.sort(threatsForPiece, PieceComparatorForPieces);
		int minThreatenerValue = getValue(threatsForPiece.get(0));
		//TODO: ^ This doesn't work if for example there is a threat from 2 pieces creating a battery where the Queen is in front of the Bishop. This method will automatically assume the bishop will attack first even though the battery forces the queen to go first
		//This was observed with: "r6k/ppp3pp/2n1b3/3q4/8/2P4P/P1Pn1PP1/R2QR1K1 w - - 0 1" on Pawn A2
		int ownValue = getValue(piece); 
		
		/*
		 * Case 2: If the the least threatener is of a lower value than the piece threatened, 
		 * it doesn't matter if "piece" has a defender, it is under threat. 
		 */
		if(minThreatenerValue < ownValue){
			return true;
		}
		
		/*
		 * Case 3: At this point, we know you are threatened by a piece. 
		 * The value of the piece threatening you is of equal or higher 
		 * value than your piece threatened. 
		 * So naturally, at this point, let's see if your piece is defended.
		 * In this case, we return true if you have no defenders.
		 */
		List<Piece> defendersOfPieceUnfiltered = findDefendersForPieceGiven(board, piece);
		/*
		 * Note this method removes all pins. Defenders that are pinned to high powered pieces like 
		 * Queens or Rooks will be filtered out. This is a limitation of the algorithm and must be handled
		 * elsewhere.
		 */
		List<Piece> defendersOfPiece = filterPinnedPieces(board, defendersOfPieceUnfiltered);
		if(defendersOfPiece.size() == 0){
			/*
			 * findDefenders cannot find backward defenders i.e. attacker is between defender and attacked piece. 
			 * This trick determines is the attack is worth while.
			 */
			Move attackingMove = new Move(threatsForPiece.get(0).getValue(), piece.getValue(), threatsForPiece.get(0).getyPosition(), threatsForPiece.get(0).getxPosition(), piece.getyPosition(), piece.getxPosition());
			Board newBoard = attackingMove.updateBoard(board);
			//Recursive call but the idea is that the cycle is broken now.
			Piece pieceThatAttacked = new Piece(attackingMove.getPieceMoving(), attackingMove.getToPos());
			if(!isPieceThreatenedNew(newBoard, pieceThatAttacked)){
				return true;
			}
		}
		
		/*
		 * Case 4: if min attacker is of a greater value than threatened 
		 * piece + min defender then it is not a threat, except for:
		 * 1. The defender is pinned against a higher pieced value.
		 * 2. The capture results in a surprise attack of a higher piece value.
		 */
		if(threatsForPiece.size() == 1 && minThreatenerValue >= ownValue){
			return false;
		}
		/*
		 * 1. Pawn takes pawn. 
		 * 2. knight takes Pawn. 
		 * 3. Knight takes knight. 
		 * 4. queen takes Knight
		 * 
		 * Pawn has one defender: Knight and 2 threats: knight and queen.
		 * ownValue = 1
		 * minThreatenerValue = 3
		 * minDefenderValue = 3
		 * 
		 * case 1: threats.size() > 1 true 
		 * case 2: minThreatenerValue < ownValue false
		 * case 3: defenders.size() > 0 true
		 * case 4: minThreatenerValue >= own + minDefenderValue false
		 * So we need 
		 * case 5: minThreatenerValue + secondMinThreatenerValue 
		 */
		if(defendersOfPiece.size() == 0){
			return true;
		}
		Collections.sort(defendersOfPiece, PieceComparatorForPieces);
		int minDefenderValue = getValue(defendersOfPiece.get(0));
		if(threatsForPiece.size() > 1 && minThreatenerValue >= ownValue + minDefenderValue){
			return false;
		}
		if(threatsForPiece.size() > 1 && defendersOfPiece.size() == 1 && minThreatenerValue < ownValue + minDefenderValue){
			return true;
		}
		
		/*
		 * At this point, we know we have at least 2 defenders AND 2 threats.
		 */
		int secondMinThreatenerValue = getValue(threatsForPiece.get(1));
		
		if(threatsForPiece.size() == 2 && minThreatenerValue + secondMinThreatenerValue >= ownValue + minDefenderValue){
			return false;
		}
		
		if(threatsForPiece.size() == 2 && minThreatenerValue + secondMinThreatenerValue < ownValue + minDefenderValue){
			return true;
		}

		int secondDefenderValue = getValue(defendersOfPiece.get(1));
		if(threatsForPiece.size() > 2 && minThreatenerValue + secondMinThreatenerValue >= ownValue + minDefenderValue + secondDefenderValue){
			return false;
		}
		
		return true;
	}
	
	public static boolean canKingGetToPiece(Board board, Piece piece) {
		int team = piece.getTeam();
		int[] king = PieceFinder.findKing(board, team);
		for(Move move : findAllValidMoves(board, king)){
			if(isCheck(move.updateBoard(board), team) && !isPieceThreatenedNew(move.updateBoard(board), piece)){
				return false;
			}
		}
		return true;
	}
	public static boolean isDefendedByAnythingButTheKing(Board board, int piece, int[] yx) {
		return isDefendedByAnythingButTheKing(board, new int[]{piece, yx[0], yx[1]});
	}
	public static boolean isDefendedByAnythingButTheKing(Board board, int[] piece) {
		Board expBoard = new Board(board.getBoard());
		expBoard.setPiece(-piece[0], piece[1], piece[2]);
		/*
		 * The reason for using Naive approach here is because you are only interested in the pieces that are defending you.
		 * You do not want to include the other pieces from the other piece which is what isThreatened does 
		 */
		return isThreatenedVeryNaiveApproach(expBoard, -piece[0], piece[1], piece[2], false);
	}
	public static boolean isDefended(Board board, int piece, int[] yx) {
		return isDefended(board, piece, yx[0], yx[1]);
	}
	public static boolean isDefended(Board board, int[] piece) {
		return isDefended(board, piece[0], piece[1], piece[2]);
	}
	public static boolean isDefended(Board board, int piece, int y, int x) {
		if(Math.abs(piece) == BoardUtil.PAWN){
			int team = piece>0?1:-1;
			if(x>0 && board.getPiece(y-team,x-1) * team > BoardUtil.SPACE)
				return true;

			if(x<7 && board.getPiece(y-team,x+1) * team > BoardUtil.SPACE)
				return true;
		}
		
		Board expBoard = new Board(board.getBoard());
		expBoard.setPiece(-piece, y, x);
		
		/*
		 * The reason for using Naive approach here is because you are only interested in the pieces that are defending you.
		 * You do not want to include the other pieces from the other piece which is what isThreatened does
		 * ^ This may not work with pawns!! 
		 */
		return isThreatenedVeryNaiveApproach(expBoard, new int[]{-piece, y, x});
	}
	/*
	 * Note: This method cannot determine if a defender is pinned. 
	 */
	public static List<Piece> findDefendersForPieceGiven(Board board, Piece piece){
		int team = piece.getValue() > 0 ? 1 : -1;
		List<Piece> defenders = new ArrayList<Piece>();
		List<Piece> ownPieces = BoardUtil.getAllTeamPiecesInPieces(board, team);
		for(Piece ownPiece : ownPieces){
			if(ownPiece.equals(piece)){
				continue;
			}
			if(canPieceDefendSquare(board, ownPiece, piece.getyPosition(), piece.getxPosition())){
				defenders.add(ownPiece);
			}
		}
		return defenders;
	}
	
	private static boolean canPieceDefendSquare(Board board, Piece piece, int y, int x) {
		int team = piece.getValue() > 0 ? 1 : -1;
		int distance = (y-piece.getyPosition())*(y-piece.getyPosition())+(x-piece.getxPosition())*(x-piece.getxPosition());
		switch(Math.abs(piece.getValue())){
		case BoardUtil.PAWN:
			if(team*(y-piece.getyPosition()) == 1 && Math.abs(x-piece.getxPosition()) == 1){
				return true;
			}
			break;
		case BoardUtil.KNIGHT:
			if(distance == 5){
				return true;
			}
			break;
		case BoardUtil.BISHOP:
			
			if(Math.abs(y-piece.getyPosition()) == Math.abs(x-piece.getxPosition())){
				//TODO: check space position
				int dx = (x-piece.getxPosition()) > 0 ? 1 : -1;
				int dy = (y-piece.getyPosition()) > 0 ? 1 : -1;
				int n = Math.abs(y-piece.getyPosition())-1;
				for(int i=1;i<=n;i++){
					if(board.getPiece(i*dy+piece.getyPosition(),i*dx+piece.getxPosition()) != BoardUtil.SPACE){
						return false;
					}
				}
				return true;
			}
			break;
		case BoardUtil.ROOK:
			if(y == piece.getyPosition()){
				for(int i=Math.min(x, piece.getxPosition()) + 1; i < Math.max(x, piece.getxPosition()); i++){
					if(board.getPiece(y, i) != BoardUtil.SPACE){
						return false;
					}
				}
				return true;
			}
			if(x == piece.getxPosition()){
				for(int i=Math.min(y, piece.getyPosition()) + 1; i < Math.max(y, piece.getyPosition()); i++){
					if(board.getPiece(i, x) != BoardUtil.SPACE){
						return false;
					}
				}
				return true;
			}
			break;
		case BoardUtil.QUEEN:
			return canPieceDefendSquare(board, new Piece(team*BoardUtil.ROOK, piece.getyPosition(), piece.getxPosition()), y, x) ||
				   canPieceDefendSquare(board, new Piece(team*BoardUtil.BISHOP, piece.getyPosition(), piece.getxPosition()), y, x) ;
		case BoardUtil.KING:
			if(distance < 3){
				return true;
			}
			break;
		}
		return false;
	}

	
	public static List<Move> findAllGetMeOutOfCheckMoves(Board board, int team) {
		List<Move> moves = new ArrayList<Move>();
		for(Move rawMove : MoveUtil.getAllMoveForTeam(board, team)){
			if(isInCheckMoveValid(board, rawMove))
				moves.add(rawMove);
		}
		return moves;
	}

	public static boolean isNotInCheckMoveValid(Board board, Move move) {
		return isNotInCheckMoveValid(board, move.getPieceMoving(), move.getFromPos()[0], move.getFromPos()[1], move.getToPos()[0], move.getToPos()[1]);
	}
	public static boolean isNotInCheckMoveValid(Board board, int piece, int y1, int x1, int y2, int x2) {
		return isNotInCheckMoveValid(board, piece, y1, x1, y2, x2, false);
	}
	public static boolean isNotInCheckMoveValid(Board board, int piece, int y1, int x1, int y2, int x2, boolean override) {
		return isNotInCheckMoveValid(board, piece, y1, x1, y2, x2, false, null);
	}
	public static boolean isNotInCheckMoveValid(Board board, int piece, int y1, int x1, int y2, int x2, boolean override, Move lastMove) {
		Board expBoard = new Move(piece, y1, x1, y2, x2).updateBoard(board);
		int team = piece/Math.abs(piece);
		if(isCheck(expBoard, team) && !override){
			return false;
		}
		switch(Math.abs(piece)){
		case BoardUtil.KING:
			if(!isSpaceOccupiedByOwnPiece(board, piece, y2, x2)){
				if(Math.abs(x2-x1) < 2 && Math.abs(y2-y1) < 2 && !isThreatenedVeryNaiveApproach(expBoard, piece, y2, x2)){
					return true;
				}else if(conditionsForCastling(board, team, y1, x1, y2, x2)){
					return true;
				}
			}
			break;
		case BoardUtil.KNIGHT:
			if(!isSpaceOccupiedByOwnPiece(board, piece, y2, x2)){
				if ((Math.abs(x2-x1) == 2 && Math.abs(y2-y1) == 1) || ((Math.abs(y2-y1) == 2 && Math.abs(x2-x1) == 1))){
					return true;
				}
			}
			break;
		case BoardUtil.PAWN:
			if(x1==x2 && !isSpaceOccupied(board, y2, x2)){
				if(y2 == y1 + team){
					//Normal pawn move
					return true;
				}else if(y2 == y1 + 2*team && y1 == BoardUtil.getInitialPawnRow(team) && !isSpaceOccupied(board, y2-team, x2)){
					//Initial pawn move
					return true;
				}
			}else if(!isSpaceOccupiedByOwnPiece(board, piece, y2, x2)) {
				if(isSpaceOccupied(board, y2, x2) && Math.abs(x2-x1) == 1 && y2 == y1 + team){
					//Normal attacking pawn move
					return true;
				}else if(lastMove != null && lastMove.getPieceMoving() == -team*BoardUtil.PAWN && Math.abs(x2-x1) == 1 && y2 == y1 + team && board.getPiece(y1, x2) == -team*BoardUtil.PAWN && lastMove.getToPos()[0] == y1 && lastMove.getToPos()[1] == x2 && y1 == (team>BoardUtil.SPACE?4:3)){
//					//En-passant pawn move
					return true;					
				}
			}
			break;
		case BoardUtil.BISHOP:
		case BoardUtil.ROOK:
		case BoardUtil.QUEEN:
			if(!isSpaceOccupiedByOwnPiece(board, piece, y2, x2)){
//				return 
//						isNotInCheckMoveValid(board, BoardUtil.ROOK*team, y1, x1, y2, x2, true) ||
//						isNotInCheckMoveValid(board, BoardUtil.BISHOP*team, y1, x1, y2, x2, true);
				//^ This does not work because updateBoard does not happen when the proposed piece does not fit the piece in board. 
				//This needs to be handled differently. 
				return isEmptyBetweenPieces(board, Math.abs(piece), y1, x1, y2, x2);
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	public static boolean conditionsForCastling(Board board, int team, Move move) {
		return conditionsForCastling(board, team, move.getFromPos()[0], move.getFromPos()[1], move.getToPos()[0], move.getToPos()[1]);
	}
	
	public static boolean conditionsForCastling(Board board, int team, int y1, int x1, int y2, int x2) {
		if(board.getPiece(y1,x1)*team != BoardUtil.KING){
			return false;
		}
		if(isCheck(board, team)){
			return false;
		}
		if(team == BoardUtil.WHITE && y1 == 0 && x1 == BoardUtil.KING_POSITION && y2 == 0 && x2 == BoardUtil.KING_POSITION + 2 && board.getPiece(0, 7) == BoardUtil.ROOK && board.getPiece(0, 5)==BoardUtil.SPACE && board.getPiece(0,6)==BoardUtil.SPACE){
			int pathClear = findThreatsAccurate(board, new Piece(team, 0, 6)).size() + findThreatsAccurate(board, new Piece(team, 0, 5)).size();
			return pathClear == 0;
		}else if(team == BoardUtil.BLACK && y1 == 7 && x1 == BoardUtil.KING_POSITION && y2 == 7 && x2 == BoardUtil.KING_POSITION + 2 && board.getPiece(7, 7) == -BoardUtil.ROOK && board.getPiece(7, 5)==BoardUtil.SPACE && board.getPiece(7,6)==BoardUtil.SPACE){
			int pathClear = findThreatsAccurate(board, new Piece(team, 7, 6)).size() + findThreatsAccurate(board, new Piece(team, 7, 5)).size();
			return pathClear == 0;
		}else if(team == BoardUtil.WHITE && y1 == 0 && x1 == BoardUtil.KING_POSITION && y2 == 0 && x2 == BoardUtil.KING_POSITION - 2 && board.getPiece(0, 0) == BoardUtil.ROOK && board.getPiece(0, 1)==BoardUtil.SPACE && board.getPiece(0,2)==BoardUtil.SPACE && board.getPiece(0,3)==BoardUtil.SPACE){
			int pathClear = findThreatsAccurate(board, new Piece(team, 0, 1)).size() + findThreatsAccurate(board, new Piece(team, 0, 2)).size() + findThreatsAccurate(board, new Piece(team, 0, 3)).size();
			return pathClear == 0;
		}else if(team == BoardUtil.BLACK && y1 == 7 && x1 == BoardUtil.KING_POSITION && y2 == 7 && x2 == BoardUtil.KING_POSITION - 2 && board.getPiece(7, 0) == -BoardUtil.ROOK && board.getPiece(7, 1)==BoardUtil.SPACE && board.getPiece(7,2)==BoardUtil.SPACE && board.getPiece(7,3)==BoardUtil.SPACE){
			int pathClear = findThreatsAccurate(board, new Piece(team, 7, 1)).size() + findThreatsAccurate(board, new Piece(team, 7, 2)).size() + findThreatsAccurate(board, new Piece(team, 7, 3)).size();
			return pathClear == 0;
		}
		return false;
	}
	
	
	/*
	 * Find all valid moves that do not include king capture for given piece.
	 */
	public static List<Move> findAllValidMovesThatLeadToCheckMate(Board board, Piece piece){
		List<Move> validMoves = new ArrayList<Move>();
		boolean includeKingCapture = false;
		List<Move> moves = findVanillaMovesForPiece(piece);
		for(Move move:moves){
			if(isMoveValid(board, move, includeKingCapture)){
				validMoves.add(move);
			}
		}
		return validMoves;
	}

	public static boolean isPieceThreateningOppKing(Board board, Piece attackingPiece, Piece oppKing){
		int team = attackingPiece.getValue() > BoardUtil.SPACE ? 1 : -1;
		int pieceThreat = team*attackingPiece.getValue();
		
		switch(pieceThreat){
		case(BoardUtil.PAWN):
			if(Math.abs(attackingPiece.getxPosition()-oppKing.getxPosition()) == 1 && attackingPiece.getyPosition()+team == oppKing.getyPosition())
				return true;
			break;
		case(BoardUtil.KNIGHT):
			if(Math.abs(attackingPiece.getyPosition()-oppKing.getyPosition())==2 && Math.abs(attackingPiece.getxPosition()-oppKing.getxPosition())==1)
				return true;
			else if(Math.abs(attackingPiece.getyPosition()-oppKing.getyPosition())==1 && Math.abs(attackingPiece.getxPosition()-oppKing.getxPosition())==2)
				return true;
			break;
		case(BoardUtil.BISHOP):
		case(BoardUtil.ROOK):
		case(BoardUtil.QUEEN):
			if(isEmptyBetweenPieces(board, attackingPiece.getValue(), attackingPiece.getyPosition(), attackingPiece.getxPosition(), oppKing.getyPosition(), oppKing.getxPosition()))
				return true;
		}
		return false;
	}
	
	
	public static List<Piece> findThreatsAccurate(Board board, Piece piece){
		return findThreatsAccurate(board, piece, false);
	}
	public static List<Piece> findThreatsAccurate(Board board, Piece piece, boolean includeBattery){
		int team = piece.getTeam();
		List<Piece> threats = new ArrayList<Piece>();
		List<Piece> hisPieces = BoardUtil.getAllTeamPiecesInPieces(board, -team);
		int x = piece.getxPosition();
		int y = piece.getyPosition();
		for(Piece hisPiece: hisPieces){
			int pieceThreat = hisPiece.getValue();
			int i = hisPiece.getyPosition();
			int j = hisPiece.getxPosition();
			switch(Math.abs(pieceThreat)){
			case(BoardUtil.PAWN):
				if(Math.abs(x-j) == 1 && y==i-team)
					threats.add(new Piece(-team*BoardUtil.PAWN, i,j));
			/*
			 * TODO: En passant, write later
			 */
//					}else if(Math.abs(piece)==BoardUtil.PAWN && Math.abs(x-j) == 1 && i==y+2*team){
//						threats.add(new int[]{-team*BoardUtil.PAWN, i,j});
				else if((i==0 && pieceThreat < 0) || (i==7 && pieceThreat > 0))
					if(isEmptyBetweenPieces(board, BoardUtil.QUEEN, i, j, y, x))
						threats.add(new Piece(-team*BoardUtil.QUEEN, i, j));
				break;
			case(BoardUtil.KNIGHT):
				if(Math.abs(x-j)==2 && Math.abs(y-i)==1)
					threats.add(new Piece(-team*BoardUtil.KNIGHT, i,j));
				else if(Math.abs(x-j)==1 && Math.abs(y-i)==2)
					threats.add(new Piece(-team*BoardUtil.KNIGHT, i,j));
				break;
			case(BoardUtil.BISHOP):
			case(BoardUtil.ROOK):
			case(BoardUtil.QUEEN):
				if(isEmptyBetweenPieces(board, Math.abs(hisPiece.getValue()), i, j, y, x))
					threats.add(new Piece(hisPiece.getValue(), i,j));
				break;
			case(BoardUtil.KING):
				if(Math.abs(x-j) < 2 && Math.abs(y-i) < 2)
					threats.add(new Piece(-team*BoardUtil.KING, i, j));
				break;
			}
		}
		if(includeBattery){
			Collections.sort(threats, PieceComparatorForPieces);
			List<Piece> batteries = new ArrayList<Piece>();
			for(Piece threat : threats){
				if(isBattery(board, threat, piece)){
					batteries.addAll(findBatteriesForPieces(board, threat, piece));
				}
			}
			//TODO: Include an insert algorithm
			threats.addAll(batteries);
		}
		return threats;
	}

	public static boolean isInCheckMoveValid(Board board, Move move) {
		return isInCheckMoveValid(board, move.getPieceMoving(), move.getFromPos()[0], move.getFromPos()[1], move.getToPos()[0], move.getToPos()[1]);
	}
	
	public static boolean isInCheckMoveValid(Board board, int piece, int y1, int x1, int y2, int x2) {
		Move move = new Move(piece, y1, x1, y2, x2);
		Board newBoard = move.updateBoard(board);
		return !isCheck(newBoard, piece/Math.abs(piece)) && isNotInCheckMoveValid(board, piece, y1, x1, y2, x2);
	}
		
	public static List<Piece> filterPinnedPieces(Board board, List<Piece> listOfPieces) {
		List<Piece> piecesPinned = new ArrayList<Piece>();
		for(Piece piece : listOfPieces){
			AbsolutePin pin = AdvancedMoveUtil.getPin(board, piece);
			if(pin != null && Math.abs(pin.getEndPiece()[0]) == BoardUtil.KING){
				continue;
			}
			piecesPinned.add(piece);
		}
		return piecesPinned;
	}
	
	public static List<Piece> findBatteriesForPieces(Board board, Piece attackingPiece, Piece pieceAttacked) {
		List<Piece> batteries = new ArrayList<Piece>();
		
		int attackingTeam = attackingPiece.getTeam();
		int dy = attackingPiece.getyPosition() - pieceAttacked.getyPosition();
		int dx = attackingPiece.getxPosition() - pieceAttacked.getxPosition();
		if(dy != 0) dy = Math.abs(dy)/dy;
		if(dx != 0) dx = Math.abs(dx)/dx;
		
		int yMoving = attackingPiece.getyPosition();
		int xMoving = attackingPiece.getxPosition();
		while(yMoving < 8 && yMoving > -1 && xMoving < 8 && xMoving > -1){
			yMoving = yMoving + dy;
			xMoving = xMoving + dx;
			if(board.getPiece(yMoving, xMoving) * attackingTeam > BoardUtil.KNIGHT){
				if(isBatteryPieceCombinationAndDirectionProperForPiece(attackingPiece, new Piece(board, yMoving,xMoving), dy, dx)){
					batteries.add(new Piece(board, yMoving, xMoving));
				}
			}else if(board.getPiece(yMoving, xMoving) * attackingTeam != BoardUtil.SPACE){
				return batteries;
			}
		}
		
		return batteries;
	}

	public static boolean isBattery(Board board, Piece attackingPiece, Piece pieceAttacked) {
		if(Math.abs(attackingPiece.getValue())<BoardUtil.BISHOP){
			return false;
		}
		int attackingTeam = attackingPiece.getTeam();
		int dy = attackingPiece.getyPosition() - pieceAttacked.getyPosition();
		int dx = attackingPiece.getxPosition() - pieceAttacked.getxPosition();
		if(dy != 0) dy = Math.abs(dy)/dy;
		if(dx != 0) dx = Math.abs(dx)/dx;
		if(dx==0 && dy==0) return false;
		int yMoving = attackingPiece.getyPosition();
		int xMoving = attackingPiece.getxPosition();
		while(yMoving < 8 && yMoving > -1 && xMoving < 8 && xMoving > -1){
			yMoving = yMoving + dy;
			xMoving = xMoving + dx;
			if(board.getPiece(yMoving, xMoving) * attackingTeam > BoardUtil.KNIGHT){
				return isBatteryPieceCombinationAndDirectionProperForPiece(attackingPiece, new Piece(board, yMoving,xMoving), dy, dx);
			}else if(board.getPiece(yMoving, xMoving) * attackingTeam != BoardUtil.SPACE){
				return false;
			}
		}
		return false;
	}


	public static boolean isBatteryPieceCombinationAndDirectionProperForPiece(Piece attackingPiece, Piece batteryPiece, int dy, int dx) {
		if(attackingPiece.getValue()*batteryPiece.getValue() == BoardUtil.BISHOP*BoardUtil.ROOK){
			return false;
		}
		if(attackingPiece.getValue()*batteryPiece.getValue() == BoardUtil.QUEEN*BoardUtil.BISHOP && dy*dx != 0){
			return true;
		}
		if(attackingPiece.getValue()*batteryPiece.getValue() == BoardUtil.QUEEN*BoardUtil.ROOK && dy*dx == 0){
			return true;
		}
		if(attackingPiece.getValue()*batteryPiece.getValue() == BoardUtil.ROOK*BoardUtil.ROOK && dy*dx == 0){
			return true;
		}
		return false;
	}
	
	public static boolean isCapture(Board board, Move move) {
		int movingPiece = move.getPieceMoving();
		int target = board.getPiece(move.getToPos()[0], move.getToPos()[1]);
		return movingPiece * target < 0;
	}
	
	public static boolean isPawnThreateningPromotionAccurate(Board board, Piece pawn) {
		int team = pawn.getTeam();
		if(Math.abs(pawn.getValue()) != BoardUtil.PAWN) return false;
		
		if(team == BoardUtil.WHITE){
			if(pawn.getyPosition() <= 3) return false;
			for(int j = pawn.getyPosition(); j < 8; j++){
				if(board.getPiece(j, pawn.getxPosition()) < BoardUtil.SPACE){
					return false;
				}
			}
			List<Move> pawnMoves = findAllValidMoves(board, pawn);
			for(Move pawnMove : pawnMoves){
				Board newBoard = pawnMove.updateBoard(board);
				if(!isPieceThreatenedNew(newBoard, pawn)){
					return true;
				}
			}
			return false;
		}else{
			if(pawn.getyPosition() >= 4) return false;
			for(int j = pawn.getyPosition(); j > -1; j--){
				if(board.getPiece(j, pawn.getxPosition()) > BoardUtil.SPACE){
					return false;
				}
			}
			List<Move> pawnMoves = findAllValidMoves(board, pawn);
			for(Move pawnMove : pawnMoves){
				Board newBoard = pawnMove.updateBoard(board);
				if(!isPieceThreatenedNew(newBoard, pawn)){
					return true;
				}
			}
			return false;
		}
	}
	
	public static boolean isCoastFreeForPawnAccurate(Board board, Piece pawn) {
		if(pawn.getValue() == -BoardUtil.PAWN){
			for(int i=pawn.getyPosition()-1; i>-1;i--){
				if(board.getPiece(i, pawn.getxPosition()) != BoardUtil.SPACE){
					return false;
				}
			}
			return true;
		}else if(pawn.getValue() == BoardUtil.PAWN){
			for(int i=pawn.getxPosition()+1; i<BoardUtil.MAX_ROW ;i++){
				if(board.getPiece(i, pawn.getxPosition()) != BoardUtil.SPACE){
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static final Comparator<Piece> PieceComparatorForPieces = new Comparator<Piece>() {
		@Override
		public int compare(Piece left, Piece right) {
			return Integer.valueOf(getValue(left)).compareTo(getValue(right));
		}
	};
	
	
	public static boolean isTrade(Board board, Move move) {
		return getValue(move.getPieceCaptured()) == getValue(move.getPieceMoving());
	}
	
	public static boolean isRepeatedThreeTimes(List<Move> moveHistory) {
		if(moveHistory.size() < 10){
			return false;
		}
		int lastIndex = moveHistory.size()-1;
		if(moveHistory.get(lastIndex).equals(moveHistory.get(lastIndex-4)) &&
			moveHistory.get(lastIndex-1).equals(moveHistory.get(lastIndex-5)) &&
			moveHistory.get(lastIndex-2).equals(moveHistory.get(lastIndex-6)) &&
			moveHistory.get(lastIndex-3).equals(moveHistory.get(lastIndex-7)) &&
			moveHistory.get(lastIndex-4).equals(moveHistory.get(lastIndex-8))
				){
			return true;
		}
		return false;
	}

	public static boolean isPawnPromotion(Board board, int[] yx) {
		return isPawnPromotion(board, yx[0], yx[1]);
	}
	public static boolean isPawnPromotion(Board board, int y, int x) {
		int piece = board.getPiece(y,x);
		if(Math.abs(piece) != BoardUtil.PAWN) return false;
		int team = piece/BoardUtil.PAWN;
		if((team == BoardUtil.BLACK && y < 3) || (team == BoardUtil.WHITE && y > 4)) return true;
		return false;
	}

	/*
	 * Is move of member team performing a surprise check. Board is board after the move. 
	 */
	public static boolean isSurpriseCheck(Board board, Move move){
		return isSurpriseCheck(board, move.getPieceAfterMove());
	}
	public static boolean isSurpriseCheck(Board board, Piece piece){
		List<Piece> checks = findThreatsAccurate(board, new Piece(PieceFinder.findKing(board, -piece.getTeam())));
		if(checks.size() > 1){
			return true;
		}
		return checks.size() == 1 && !checks.get(0).equals(piece);
	}
	
	public static boolean isPawnDefender(Board board, Move move){
		int captured = board.getPiece(move.getToPos());
		if(captured == BoardUtil.SPACE) return false;
		List<Piece> defenders = findDefendersForPieceGiven(board, move.getPieceAfterMove());
		return defenders.size() == 1 && Math.abs(defenders.get(0).getValue()) == BoardUtil.PAWN && !isPawnStackedIsolatedOrBlocked(board, defenders.get(0));
	}
	
	public static boolean isPawnStackedIsolatedOrBlocked(Board board, Piece pawn){
		if(Math.abs(pawn.getValue()) != BoardUtil.PAWN){
			return false;
		}
		if(pawn.getValue() > 0 && board.getPiece(pawn.getyPosition()+1, pawn.getxPosition()) != BoardUtil.SPACE){
			return true;
		}else if(pawn.getValue() < 0 && board.getPiece(pawn.getyPosition()-1, pawn.getxPosition()) != BoardUtil.SPACE){
			return true;
		}
		int maxRow = Math.min(8,pawn.getyPosition()+2);
		int minRow = Math.max(0, pawn.getyPosition()-1);
		int maxCol = Math.min(8,pawn.getxPosition()+2);
		int minCol = Math.max(0, pawn.getxPosition()-1);
		for(int i=minRow;i<maxRow;i++){
			for(int j=minCol;j<maxCol;j++){
				if(j!= pawn.getxPosition() && board.getPiece(i,j) == pawn.getValue()){
					return false;
				}
			}
		}
		return true;
	}

	public static boolean couldHaveCapturedInsteadOfFork(Board board, Move move, int potentialGainFromFork) {
		List<Piece> pieces = findAttackPieces(board, move.getPieceBeforeMove());
		for(Piece piece : pieces){
			if(getValue(piece) >= potentialGainFromFork){
				return true;
			}
		}
		return false;
	}
	
	public static final Comparator<Move> PawnComparator = new Comparator<Move>() {
		@Override
		public int compare(Move left, Move right) {
			int capturedPieceLeft = Math.abs(left.getPieceCaptured());
			int capturedPieceRight = Math.abs(right.getPieceCaptured());
			
			if(capturedPieceLeft==capturedPieceRight){
				int team = left.getPieceAfterMove().getTeam();
				int yLeft = left.getPieceAfterMove().getyPosition();
				int yRight = right.getPieceAfterMove().getyPosition();
				return -team*Integer.valueOf(yLeft).compareTo(yRight);
			}
			return Integer.valueOf(capturedPieceRight).compareTo(capturedPieceLeft);
		}
	};
	
	public static final Comparator<Move> ProtectsComparator = new Comparator<Move>() {
		@Override
		public int compare(Move left, Move right) {
			int movingPieceLeft = Math.abs(left.getPieceMoving());
			int movingPieceRight = Math.abs(right.getPieceMoving());
			return Integer.valueOf(movingPieceLeft).compareTo(movingPieceRight);
		}
	};
	
	public static final Comparator<Move> BattleComparator = new Comparator<Move>() {
		@Override
		public int compare(Move left, Move right) {
			int metricLeft = getValue(left.getPieceCaptured()) - getValue(left.getPieceMoving());
			int metricRight = getValue(right.getPieceCaptured()) - getValue(right.getPieceMoving());
			return Integer.valueOf(metricRight).compareTo(metricLeft);
		}
	};
	
	public static final Comparator<Move> MoveComparator = new Comparator<Move>() {
		@Override
		public int compare(Move left, Move right) {
			int capturedPieceLeft = Math.abs(left.getPieceCaptured());
			int capturedPieceRight = Math.abs(right.getPieceCaptured());
			if(capturedPieceLeft==capturedPieceRight){
				int movingPieceLeft = Math.abs(left.getPieceMoving());
				int movingPieceRight = Math.abs(right.getPieceMoving());
				return Integer.valueOf(movingPieceRight).compareTo(movingPieceLeft);
			}
			return Integer.valueOf(capturedPieceRight).compareTo(capturedPieceLeft);
		}
	};

	public static final Comparator<Move> RandomComparator = new Comparator<Move>() {
		@Override
		public int compare(Move left, Move right) {
			return Integer.valueOf(getValue(left.getPieceAfterMove())).compareTo(getValue(right.getPieceAfterMove()));
		}
	};


	public static boolean isCaptureOrPawnMovement(Board board, Move move) {
		int team = move.getPieceMoving() > 0 ? 1 : -1;
		return move.getPieceMoving()*team == BoardUtil.PAWN || move.getPieceCaptured() != BoardUtil.SPACE;
	}

	public static Piece maxPiece(List<Piece> ps) {
		int maxValue = 0;
		Piece maxPiece = null;
		for(Piece p : ps) {
			int currentValue = Math.abs(p.getValue());
			if(currentValue == BoardUtil.KING) {
				continue;
			}else if(currentValue == BoardUtil.QUEEN) {
				return p;
			}else if(currentValue > maxValue){
				maxValue = Math.abs(p.getValue());
				maxPiece = p;
			}
		}
		return maxPiece;
	}
	
	public static List<Move> getFreeCaptures(Board board, int team){
		List<Move> freeCaptures = new ArrayList<Move>();
		int captureScore = 0;
		for(Move move : getAllMoveForTeam(board, team))
			if(move.getPieceCaptured() != 0){
				boolean isPieceThreatenedAfterCapture = isPieceThreatenedNew(move.updateBoard(board), move.getPieceAfterMove());
				if(isPieceThreatenedAfterCapture && Math.abs(move.getPieceMoving()) < Math.abs(move.getPieceCaptured())){
					freeCaptures.add(move);
					captureScore = Math.max(captureScore, Math.abs(move.getPieceCaptured()) - Math.abs(move.getPieceMoving()));
				}else if(!isPieceThreatenedAfterCapture){
					freeCaptures.add(move);
					captureScore = Math.max(captureScore, Math.abs(move.getPieceCaptured()));
				}
			}
		
		List<Move> mostLikelyCaptures = new ArrayList<Move>();
		for(Move move : freeCaptures){
			boolean isPieceThreatenedAfterCapture = isPieceThreatenedNew(move.updateBoard(board), move.getPieceAfterMove());
			if(isPieceThreatenedAfterCapture && Math.abs(move.getPieceCaptured()) - Math.abs(move.getPieceMoving()) == captureScore){
				mostLikelyCaptures.add(move);
			}else if(!isPieceThreatenedAfterCapture && Math.abs(move.getPieceCaptured()) == captureScore){
				mostLikelyCaptures.add(move);
			}
		}
		
		return mostLikelyCaptures;
	}
	
	public static boolean isFreeCapture(Board board, Move move){
		if(move.getPieceCaptured() == 0)
			return false;
		if(isPieceThreatenedNew(move.updateBoard(board), move.getPieceAfterMove()) && Math.abs(move.getPieceCaptured()) > Math.abs(move.getPieceMoving()))
			return true;
		else if(!isPieceThreatenedNew(move.updateBoard(board), move.getPieceAfterMove()))
			return true;
		return false;
	}
	
	public static List<Move> filterBadMoves(Board board, List<Move> moves){
		List<Move> goodMoves = new ArrayList<Move>();
		for(Move move : moves)
			if(!isBadMove(board, move))
				goodMoves.add(move);
		return goodMoves;
	}
	
	public static boolean isBadMove(Board board, Move move){
		return Math.abs(move.getPieceCaptured()) < (isPieceThreatenedNew(move.updateBoard(board), move.getPieceAfterMove()) ? Math.abs(move.getPieceMoving()) : 0);
	}
}