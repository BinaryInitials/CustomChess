package com.ozone.engine;

import static com.ozone.movements.AdvancedMoveUtil.findAllForkPieces;
import static com.ozone.movements.AdvancedMoveUtil.findAllPinnedPiecesFromAbsolutePin;
import static com.ozone.movements.AdvancedMoveUtil.findPinnedPieceFromAbsolutePin;
import static com.ozone.movements.AdvancedMoveUtil.findPotentialGainFromForkAccurate;
import static com.ozone.movements.AdvancedMoveUtil.findPotentialRewardFromSkewer;
import static com.ozone.movements.AdvancedMoveUtil.findPotentialRewardFromSkewerAccurate;
import static com.ozone.movements.AdvancedMoveUtil.getPin;
import static com.ozone.movements.AdvancedMoveUtil.isForcedMate;
import static com.ozone.movements.AdvancedMoveUtil.isHisNextMovePuttingMeToCheckMate;
import static com.ozone.movements.AdvancedMoveUtil.isHisNextNextMovePuttingMeToCheckMate;
import static com.ozone.movements.AdvancedMoveUtil.isMovePartOfGoodMoves;
import static com.ozone.movements.AdvancedMoveUtil.isMyNextMovePuttingHimToCheckMate;
import static com.ozone.movements.AdvancedMoveUtil.isPieceForkingAccurate;
import static com.ozone.movements.AdvancedMoveUtil.isPiecePinned;
import static com.ozone.movements.MoveUtil.MoveComparator;
import static com.ozone.movements.MoveUtil.PieceComparatorForPieces;
import static com.ozone.movements.MoveUtil.conditionsForCastling;
import static com.ozone.movements.MoveUtil.couldHaveCapturedInsteadOfFork;
import static com.ozone.movements.MoveUtil.filterRandomRookMovements;
import static com.ozone.movements.MoveUtil.findAllGetMeOutOfCheckMoves;
import static com.ozone.movements.MoveUtil.findAllValidMoves;
import static com.ozone.movements.MoveUtil.findAllValidMovesThatLeadToCheckMate;
import static com.ozone.movements.MoveUtil.findAttackPieces;
import static com.ozone.movements.MoveUtil.findDefendersForPieceGiven;
import static com.ozone.movements.MoveUtil.findThreatsAccurate;
import static com.ozone.movements.MoveUtil.getAllMoveForTeam;
import static com.ozone.movements.MoveUtil.getAllMoveForTeamWithoutSuicides;
import static com.ozone.movements.MoveUtil.getMoveCountForTeam;
import static com.ozone.movements.MoveUtil.getValue;
import static com.ozone.movements.MoveUtil.isCheck;
import static com.ozone.movements.MoveUtil.isCheckMate;
import static com.ozone.movements.MoveUtil.isCoastFreeForPawnAccurate;
import static com.ozone.movements.MoveUtil.isMoveValid;
import static com.ozone.movements.MoveUtil.isPawnDefender;
import static com.ozone.movements.MoveUtil.isPawnPromotion;
import static com.ozone.movements.MoveUtil.isPawnStackedIsolatedOrBlocked;
import static com.ozone.movements.MoveUtil.isPawnThreateningPromotionAccurate;
import static com.ozone.movements.MoveUtil.isPieceThreatenedNew;
import static com.ozone.movements.MoveUtil.isRepeatedThreeTimes;
import static com.ozone.movements.MoveUtil.isStaleMate;
import static com.ozone.movements.MoveUtil.isSurpriseCheck;
import static com.ozone.movements.MoveUtil.isThreatenedVeryNaiveApproach;
import static com.ozone.movements.MoveUtil.isTrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ozone.common.AbsolutePin;
import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.libraries.LibraryLoader;
import com.ozone.mate.KingQueen;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;
import com.ozone.movements.MoveUtil.Status;

public class EngineNewFastFast implements Engine{
	private final int MATE_FOUND = 1000; 
	private int score = 0;
	private boolean mateFound = false;
	private boolean isDeveloper = false;
	private LibraryLoader openings = null;
	private int team;
	private int iterationSinceLastCaptureOrPawnMovement;
	private HashMap<Integer, List<Move>> mateMoveMap;
	
	public EngineNewFastFast(int team) {
		this.team = team;
		this.mateFound = false;
		this.score = 0;
		this.mateMoveMap = new HashMap<Integer,List<Move>>();
		this.iterationSinceLastCaptureOrPawnMovement = 0;
		//The new openings are faster to load
		this.openings = team > BoardUtil.SPACE ? new LibraryLoader(BoardUtil.WHITE, true) : new LibraryLoader(BoardUtil.BLACK, true);
		//The old openings seem stronger
//		openings = team > BoardUtil.SPACE ? new LibraryLoader(BoardUtil.WHITE) : new LibraryLoader(BoardUtil.BLACK);
	}
	
	@Override
	public void setTeam(int team){
		this.team = team;
		this.mateFound = false;
		this.score = 0;
		this.mateMoveMap.clear();
		this.iterationSinceLastCaptureOrPawnMovement = 0;
		this.openings = team > BoardUtil.SPACE ? new LibraryLoader(BoardUtil.WHITE, true) : new LibraryLoader(BoardUtil.BLACK, true);
	}
	
	@Override
	public void switchTeam(){
		this.team = -team;
		this.mateFound = false;
		this.score = 0;
		this.mateMoveMap.clear();
		this.iterationSinceLastCaptureOrPawnMovement = 0;
		this.openings = team > BoardUtil.SPACE ? new LibraryLoader(BoardUtil.WHITE, true) : new LibraryLoader(BoardUtil.BLACK, true);
	}
	
	@Override
	public Move findMove(Board board, int iteration, boolean isConsole, boolean isDeveloperMode, List<Move> moveHistory){
		this.isDeveloper = isDeveloperMode;
		if(iteration < LibraryLoader.MOVE_THRESHOLD && openings != null){
			Move opening = openings.findAdvancedOpening(board);
			if(opening != null){
				return opening;
			}
		}
		
		if(isCheckMate(board, team)){
			return null;
		}
		
		
		
		List<Move> bestMoves = findMoves(board, iteration, isConsole, isDeveloperMode, moveHistory);
		
		if(bestMoves == null || bestMoves.size() == 0){
			return MoveUtil.STALE_MATE;
		}
		
		Collections.shuffle(bestMoves);
		Move contender = bestMoves.get(0);
		if(contender.getPieceMoving()*team == BoardUtil.PAWN || contender.getPieceCaptured() != 0){
			iterationSinceLastCaptureOrPawnMovement = 0;
		}else{
			iterationSinceLastCaptureOrPawnMovement++;
		}
		return contender;
	}
	
	
	private List<Move> findMoves(Board board, int iteration, boolean isConsole, boolean isDeveloperMode, List<Move> moveHistory){
		mateMoveMap.clear();
		List<Move> allMoves = new ArrayList<Move>();
		
		List<Piece> allMyPieces = BoardUtil.getAllTeamPiecesInPieces(board, team);
		
		if(isCheck(board, team)){
			List<Move> moves = findAllGetMeOutOfCheckMoves(board, team);
			Collections.sort(moves, MoveComparator);
			mateFound = false;
			for(Move move : moves){
				if(isCheckMate(move.updateBoard(board), -team)){
					move.setScore(50000000);
					move.addStatus(Status.CHECK_MATE_0_YOU);
					allMoves.add(move);
					break;
				}
				move.setStatuses(getStatusesForMove(board, move, iteration, moveHistory, allMyPieces));
				move.setScore(score);
				allMoves.add(move);
			}
		}else{
			List<Piece> pieces = BoardUtil.getAllTeamPieces(board, team);
			List<Piece> oppPieces = BoardUtil.getAllTeamPieces(board, -team);
			if(pieces.size()==2 && Math.abs(pieces.get(0).getValue()+pieces.get(1).getValue()) == BoardUtil.QUEEN + BoardUtil.KING && oppPieces.size() == 1){
				Move move = KingQueen.findMove(board, team);
				move.addStatus(Status.KING_QUEEN_MATE);
				move.setScore(10000);
				allMoves.add(move);
			}else{
				List<Move> moves = getAllMoveForTeam(board, team);
				
				if(pieces.size() < 7 && oppPieces.size() < 7){
					mateMoveMap.clear();
					int score = keepCheckingUntilMate(board, team);
					if(score > 0 && mateMoveMap.get(score) != null){
						moves = mateMoveMap.get(score);
						for(Move move : moves){
							move.addStatus(Status.CHECK_MATE_RECURSION);
							move.setScore(10000000);
						}
						return moves;
					}
				}
				
				mateFound = false;
				
				for(Move move : moves){
					if(isCheckMate(move.updateBoard(board), -team)){
						move.setScore(50000000);
						move.addStatus(Status.CHECK_MATE_0_YOU);
						allMoves.add(move);
						break;
					}else{
						move.addAllStatuses(getStatusesForMove(board, move, iteration, moveHistory, allMyPieces));
						move.setScore(move.getScore() + score);
						allMoves.add(move);
					}
				}
			}
		}
		if(allMoves.size() == 0){
			return new ArrayList<Move>();
		}
		Collections.sort(allMoves, CustomComparator);

		if(isDeveloperMode){
			for(Move move : allMoves){
				System.out.println("Score: " + move.getScore() +"\t " + move + "\t" + move.getStatuses().toString());
			}
		}
		
		List<Move> bestMoves = new ArrayList<Move>();
		int key = allMoves.get(allMoves.size()-1).getScore();
		for(Move move : allMoves){
			if(move.getScore() == key){
				bestMoves.add(move);
			}
		}
		
		return bestMoves;
	}

	public Set<Status> getStatusesForMove(Board board, Move move){
		return getStatusesForMove(board, move, 20, new ArrayList<Move>(), BoardUtil.getAllTeamPiecesInPieces(board, team));
	}
	public Set<Status> getStatusesForMove(Board board, Move move, int iteration, List<Move> moveHistory, List<Piece> allMyPieces){

		Set<Status> statuses = new HashSet<Status>();
		if(move.getStatuses() != null && move.getStatuses().contains(Status.CHECK_MATE_1_YOU)){
			return statuses;
		}
		int piece = move.getPieceMoving();
		Board newBoard = move.updateBoard(board);
		List<Piece> allHisPieces = BoardUtil.getAllTeamPiecesInPieces(newBoard, -team);
		List<Move> oppMoves = filterRandomRookMovements(newBoard, getAllMoveForTeamWithoutSuicides(newBoard, -team), -team);
		if(oppMoves.size() == 0){
			oppMoves = filterRandomRookMovements(newBoard, getAllMoveForTeam(newBoard, -team), -team);
		}
		
		score = 0;
		List<Move> moveHistoryPlus = new ArrayList<Move>();
		moveHistoryPlus.addAll(moveHistory);
		moveHistoryPlus.add(move);
		boolean isMoveRepeat = isRepeatedThreeTimes(moveHistoryPlus);
		
		int boardStatus = team*BoardUtil.getBoardStatus(board); 
		
		if(boardStatus > -4 && iterationSinceLastCaptureOrPawnMovement > 40){
			if(move.getPieceMoving()*team ==  BoardUtil.PAWN || move.getPieceCaptured() != 0){
				score = score + 10000;
				statuses.add(Status.FORCE_PAWN_MOVEMENT_OR_CAPTURE_TO_AVOID_STALEMATE_THIS_IS_A_PAWN_MOVEMENT_OR_CAPTURE);
			}else{
				score = score - 10000;
				statuses.add(Status.FORCE_PAWN_MOVEMENT_OR_CAPTURE_TO_AVOID_STALEMATE_THIS_IS_NOT_A_PAWN_MOVEMENT_OR_CAPTURE);
			}
		}else if(boardStatus < -3 && iterationSinceLastCaptureOrPawnMovement > 40){
			if(move.getPieceMoving()*team ==  BoardUtil.PAWN || move.getPieceCaptured() != 0){
				score = score - 10000;
				statuses.add(Status.STALE_MATE_DESIRED);
			}else{
				score = score + 10000;
				statuses.add(Status.FORCE_PAWN_MOVEMENT_OR_CAPTURE_TO_AVOID_STALEMATE_THIS_IS_NOT_A_PAWN_MOVEMENT_OR_CAPTURE);
			}
		}else if((allMyPieces.size() == 1 || boardStatus < -5) && (isStaleMate(newBoard, -team) || isMoveRepeat)){
			score = 100000000;
			statuses.add(Status.STALE_MATE_DESIRED);
			return statuses;
		}else if(isStaleMate(newBoard, -team) || isMoveRepeat){
			score = -100000000; 
			statuses.add(Status.STALE_MATE_UNDESIRED);
			return statuses;
		}else if(isMyNextMovePuttingHimToCheckMate(newBoard, team)){
			score = 20000000;
			statuses.add(Status.CHECK_MATE_1_YOU);
			mateFound = true;
			return statuses;
		}else if(isHisNextMovePuttingMeToCheckMate(newBoard, team)){
			score = -20000000;
			statuses.add(Status.CHECK_MATE_1_OPP);
			return statuses;
		}else if(isForcedMate(newBoard, team, 0, false)){
			score = 12500000;
			statuses.add(Status.FORCED_MATE_YOU);
			mateFound = true;
			return statuses;
		}else if(getMoveCountForTeam(newBoard, allMyPieces) < 20 && oppMoves.size() < 15){
			//Advanced mate
			if(isHisNextNextMovePuttingMeToCheckMate(newBoard, team)){
				score = -10000000;
				statuses.add(Status.CHECK_MATE_2_OPP);
				return statuses;
			}
		}

		if(mateFound) {
			statuses.add(Status.MATE_FOUND);
			return  statuses;
		}
		
		for(Move oppMove : oppMoves){
			Board newNewBoard = oppMove.updateBoard(newBoard);
			if(isCheck(newNewBoard, team)){
				if(isForcedMate(newNewBoard, -team, true)){
					score = -12500000;
					statuses.add(Status.FORCED_MATE_OPP);
					return statuses;
				}
			}
		}
	
		
		Piece pieceBeforeMove = new Piece(board, move.getFromPos());
		Piece pieceAfterMove = new Piece(newBoard, move.getToPos());
		List<Piece> oppThreats = findThreatsAccurate(newBoard, pieceAfterMove);
		
		if(iteration < 7){
			if(piece*team > BoardUtil.BISHOP && !isPieceThreatenedNew(board, pieceBeforeMove)){
				//This line of logic is necessary because there are cases where a piece needs to be captured
				if(move.getPieceCaptured() == 0){
					statuses.add(Status.BAD_OPENING_MOVE_QUEEN_OR_ROOK);
					score = score - 13000;	
				}
			}else if(piece*team == BoardUtil.KING && !isCheck(board, team) && (Math.abs(move.getFromPos()[1]-move.getToPos()[1]) == 1 || Math.abs(move.getFromPos()[0]-move.getToPos()[0])>0)){
				statuses.add(Status.BAD_OPENING_MOVE_KING);
				score = score - 10000;
			}else if(piece*team > BoardUtil.PAWN && piece*team < BoardUtil.ROOK && move.getFromPos()[1] > 4){
				if((move.getFromPos()[0] == 0 && piece > 0) || (move.getFromPos()[0] == 7 && piece < 0)){
					statuses.add(Status.GOOD_OPENING_MOVE_KING_SIDE_KNIGHT_BISHOP);
					score = score + 25;
				}else if((move.getToPos()[0] == 0 && piece > 0) || (move.getToPos()[0] == 7 && piece < 0)){
					statuses.add(Status.BAD_OPENING_MOVE_KING_SIDE_KNIGHT_BISHOP_BACK_ROW);
					score = score - 300;
				}
			}else if(piece*team == BoardUtil.PAWN && move.getFromPos()[1] == BoardUtil.KING_POSITION){
				if((piece > 0 && move.getFromPos()[0] == 1)||(piece < 0 && move.getFromPos()[0] == 6)){
					statuses.add(Status.GOOD_OPENING_MOVE_KING_PAWN);
					score = score + 700;
				}
			}
		}
	
		if(conditionsForCastling(board, team, move)){
			if(move.getToPos()[1] > BoardUtil.KING_POSITION){
				int piece1= board.getPiece(move.getToPos()[0]+team,7);
				int piece2= board.getPiece(move.getToPos()[0]+team,6);
				if(piece1*team > BoardUtil.SPACE && isThreatenedVeryNaiveApproach(newBoard, piece1, move.getToPos()[0]+team,7)){
//					score = score - 100;
					score = score + 209;
					statuses.add(Status.AVOID_CASTLE);
				}else if(piece2*team > BoardUtil.SPACE && isThreatenedVeryNaiveApproach(newBoard, piece2, move.getToPos()[0]+team,6)){
//					score = score - 100;
					score = score + 209;
					statuses.add(Status.AVOID_CASTLE);
				}else{
					statuses.add(Status.CASTLING);
					score = score + 209;
				}
			}else{
				statuses.add(Status.CASTLING);
				score = score + 209;
			}
		}else if(iteration < 10 && Math.abs(move.getPieceMoving())==BoardUtil.KING){
			statuses.add(Status.KING_MOVEMENT);
			score = score - 100;			
		}
		
		if(Math.abs(move.getPieceMoving()) == BoardUtil.KNIGHT && !isCheck(newBoard, piece) && 
				!isPieceThreatenedNew(board, pieceBeforeMove) &&
				(
				move.getToPos()[0] == 0 ||
				move.getToPos()[0] == 7 ||
				move.getToPos()[1] == 0 ||
				move.getToPos()[1] == 7
				)){
			score = score - BoardUtil.KNIGHT/2;
			statuses.add(Status.BAD_KNIGHT_MOVE);
		}
		
		if(iteration < 10 && isMovePartOfGoodMoves(move) && !isPieceThreatenedNew(newBoard, pieceAfterMove)){
			statuses.add(Status.GOOD_MOVES);
			score = score + 100;
		}
		
		//Pawn movement to allow king mobility and prevent back row mate
		if(team > 0 && move.equals(new Move(BoardUtil.PAWN, "h2h3")) && board.getPiece(3, 7) == -BoardUtil.PAWN && board.getPiece(2,6) == BoardUtil.PAWN){
			statuses.add(Status.PROTECT_THE_CASTLE);
			score = score + BoardUtil.PAWN;
		}else if(team < 0 && move.equals(new Move(-BoardUtil.PAWN, "h7h6")) && board.getPiece(4, 7) == BoardUtil.PAWN && board.getPiece(6,6) == -BoardUtil.PAWN){
			statuses.add(Status.PROTECT_THE_CASTLE);
			score = score + BoardUtil.PAWN;
		}
		
		if(team < 0 && move.equals(Common.convertInputToMove(-BoardUtil.PAWN, "g7g6"))){
			int[] lastPawn = new int[]{board.getPiece(6,7), 6,7}; 
			if(lastPawn[0]*team==BoardUtil.PAWN && isThreatenedVeryNaiveApproach(board, lastPawn)){
				statuses.add(Status.PROTECT_THE_CASTLE);
				score = score + 3*BoardUtil.PAWN;
			}
		}else if(team > 0 && move.equals(Common.convertInputToMove(BoardUtil.PAWN, "g2g3"))){
			int[] lastPawn = new int[]{board.getPiece(1,7), 1,7}; 
			if(lastPawn[0]*team==BoardUtil.PAWN && isThreatenedVeryNaiveApproach(board, lastPawn)){
				statuses.add(Status.PROTECT_THE_CASTLE);
				score = score + 3*BoardUtil.PAWN;
			}
		}
		
		if(move.getPieceMoving() == team*BoardUtil.BISHOP && -team*board.getPiece(move.getToPos()) == BoardUtil.KNIGHT){
			List<Piece> defenders = findDefendersForPieceGiven(newBoard, move.getPieceAfterMove());
			if(defenders.size() > 0){
				boolean isDefendedByPawns = true;
				for(Piece defender : defenders){
					if(Math.abs(defender.getValue()) != BoardUtil.PAWN){
						isDefendedByPawns = false;
						break;
					}
				}
				if(isDefendedByPawns){
					statuses.add(Status.STACKED_PAWNS_OPP);
					score = score + 20;
				}
			}else{
				//Free piece.
				statuses.add(Status.CAPTURES);
				score = score + 20*getValue(move.getPieceCaptured());
			}
		}
		
		/*
		 * Determines whether you can threaten a piece everywhere. 
		 */
		
		List<Piece> oppHighPieces = BoardUtil.getAllTeamHighPiecesInPieces(newBoard, -team);
		List<Integer> piecesAlwaysThreatened = new ArrayList<Integer>();
		for(Piece oppHighPiece : oppHighPieces){
			
			if(!isPieceThreatenedNew(newBoard, oppHighPiece) || isPieceThreatenedNew(board, oppHighPiece) || isPieceThreatenedNew(newBoard, pieceAfterMove)){
				continue;
			}
			List<Move> oppHighPieceMoves = findAllValidMoves(newBoard, oppHighPiece);
			boolean isPieceAlwaysThreatened = oppHighPieceMoves.size() > 0;
			for(Move oppHighPieceMove : oppHighPieceMoves){
				Board newNewBoard = oppHighPieceMove.updateBoard(newBoard);
				if(!isPieceThreatenedNew(newNewBoard, oppHighPieceMove.getPieceAfterMove()) || 
						getValue(oppHighPieceMove.getPieceCaptured()) >= getValue(oppHighPieceMove.getPieceMoving())){
					isPieceAlwaysThreatened = false;
					break;
				}
			}
			if(isPieceAlwaysThreatened){
				piecesAlwaysThreatened.add(oppHighPiece.getValue());
			}
		}
		int bestPieceAlwaysThreatened = 0;
		if(piecesAlwaysThreatened.size() > 0){
			bestPieceAlwaysThreatened  = Collections.max(piecesAlwaysThreatened);
			score = score + 18 * getValue(bestPieceAlwaysThreatened);
			statuses.add(Status.THREATENED_EVERYWHERE_YOU);
		}
		
		/*
		 * Determines whether opponent can corner piece into impasse and thus threaten everywhere
		 */
		if(!isPieceThreatenedNew(newBoard, pieceAfterMove) && pieceAfterMove.getValue()*team > BoardUtil.PAWN){
			//^If you are currently threatened after the move, opp will capture and not try to trap you.
			//Let's not worry about pawns. It gets confusing.
			
			for(Move oppMove : getAllMoveForTeam(newBoard, -team)){
				Board newNewBoard = oppMove.updateBoard(newBoard);
				if(
						newNewBoard.getPiece(pieceAfterMove.getyPosition(), pieceAfterMove.getxPosition()) == pieceAfterMove.getValue()
						//^ ensures the piece is still there.
						&& isPieceThreatenedNew(newNewBoard, pieceAfterMove) 
						//^ ensures that oppMove is creating a threat.
						&& !isPieceThreatenedNew(newNewBoard, new Piece(oppMove.getPieceMoving(), oppMove.getToPos()))){
						//^ ensures that oppMove was not threatened before the move.... Why is that necessary?. What needs to happen is ensuring it's not threatened after the move.
					int threat = 0;
					List<Move> pieceMoves = findAllValidMoves(newNewBoard, pieceAfterMove);
					for(Move pieceMove : pieceMoves){
						Board newBoardAfterOppMoveAndAfterPieceMove = pieceMove.updateBoard(newNewBoard);
						if(isPieceThreatenedNew(newBoardAfterOppMoveAndAfterPieceMove, new Piece(pieceMove.getPieceMoving(), pieceMove.getToPos())) && getValue(pieceMove.getPieceCaptured()) < getValue(pieceMove.getPieceMoving())){
							threat++;
						}
					}
					if(threat == pieceMoves.size()){
						score = score - 20*(getValue(pieceAfterMove.getValue())+2);
						statuses.add(Status.THREATENED_EVERYWHERE_OPP);
						break;
					}
				}
			}
		}
		
		
		if(Math.abs(move.getPieceMoving()) == BoardUtil.PAWN && isPawnStackedIsolatedOrBlocked(newBoard, move.getPieceAfterMove())){
			if(team *(3.5-pieceAfterMove.getyPosition()) > 0){
				score = score - BoardUtil.PAWN/2;
				statuses.add(Status.STACKED_PAWNS_YOU);	
			}
		}

		if(isTrade(board, move) && isPieceThreatenedNew(newBoard, pieceAfterMove)){
			
			if(oppThreats.size() == 1){
				Piece oppPiece = oppThreats.get(0);
				Move capture = new Move(oppPiece, pieceAfterMove);
				Board boardAfterCapture = capture.updateBoard(newBoard);
				List<Piece> myRecapture = findThreatsAccurate(boardAfterCapture, capture.getPieceAfterMove());
				if(myRecapture.size() > 0){
					//Not a trade. The recapture makes it worse while.
					statuses.add(Status.RECAPTURE_FROM_TRADE);
					score = score + 18*getValue(oppPiece);
				}
			}

			if(allMyPieces.size() > 10 && BoardUtil.getAllTeamHighPiecesInPieces(board, -team).size() > 10){
				score = score + 300;
				statuses.add(Status.TRADE_SIMPLIFY_THE_GAME);
			}
			
			if(isPawnDefender(board, move)){
				statuses.add(Status.TRADE_PIECES_STACKED_PAWN);
				score = score + 300;
			}
			if(boardStatus > -1){
				statuses.add(Status.TRADE_UP_IN_PIECES);
				score = score + 10*boardStatus + 100;
			}else{
				if(Math.abs(move.getPieceCaptured()) > BoardUtil.BISHOP){
					statuses.add(Status.TRADE_DOWN_IN_PIECES);
					score = score - 10*boardStatus;	
				}
			}
		}
		
		/*
		 * Adding threats
		 * Bug where once a capture is made, the threat is gone and counts against user. Need to not count this against player if there is a capture
		 */
		if(board.getPiece(move.getToPos())*team == 0){
			int beforeThreats = 0;
			int afterThreats = 0;
			for(Piece myPieceBeforeMove : allMyPieces){
				List<Piece> threatsMadeByPiece = findAttackPieces(board, myPieceBeforeMove);
				if(threatsMadeByPiece.size() > 0){
					beforeThreats = beforeThreats + Collections.max(threatsMadeByPiece, PieceComparatorForPieces).getValue();
				}
			}
			for(Piece myPieceAfterMove : BoardUtil.getAllTeamPiecesInPieces(newBoard, team)){
				List<Piece> threatsMadeByPiece = findAttackPieces(board, myPieceAfterMove);
				if(threatsMadeByPiece.size() > 0){
					afterThreats = afterThreats + Collections.max(threatsMadeByPiece, PieceComparatorForPieces).getValue();
				}
			}
			if(afterThreats > beforeThreats){
				score = score + 2*(afterThreats - beforeThreats);
				statuses.add(Status.ADDING_THREATS);
			}else if(afterThreats < beforeThreats){
				score = score + 2*(afterThreats - beforeThreats);
				statuses.add(Status.REMOVING_THREATS);			
			}
		}
		
		List<Move> hisNextMoves = getAllMoveForTeam(newBoard, -team);
		for(Move hisNextMove : hisNextMoves){
			Board hisNewBoard = hisNextMove.updateBoard(newBoard);
			if(isCheck(hisNewBoard, team)){
				if(!isPieceThreatenedNew(hisNewBoard, hisNextMove.getPieceAfterMove())){
					statuses.add(Status.CHECKS_OPP);
				}
				if(isSurpriseCheck(hisNewBoard, hisNextMove)){
					statuses.add(Status.SURPRISE_CHECKS_OPP);
					score = score - 100;
					List<Piece> attacks = findAttackPieces(hisNewBoard, hisNextMove.getPieceAfterMove());
					if(attacks.size() > 0){
						Piece maxPiece = Collections.max(attacks, PieceComparatorForPieces);
						score = score - 20*getValue(maxPiece);
						statuses.add(Status.CAPTURE_PIECE_FROM_SURPRISE_CHECK);
					}
				}
			}
		}
		if(statuses.contains(Status.CHECKS_OPP)){
			score = score - 20;
		}
		boolean isSurpriseCheck = false;
		if(isCheck(newBoard, -team)){
			
			isSurpriseCheck = isSurpriseCheck(newBoard, move);

			if(isSurpriseCheck){
				statuses.add(Status.SURPRISE_CHECKS_YOU);
				score = score + 100;
				List<Piece> attacks = findAttackPieces(newBoard, pieceAfterMove);
				if(attacks.size() > 0){
					Piece maxPiece = Collections.max(attacks, PieceComparatorForPieces);
					score = score + 20*getValue(maxPiece.getValue());
				}
			}
		}
		
		/*
		 * Is the other team in check and is my piece threatened on the way
		 */
		if(isCheck(newBoard, -team) && !isPieceThreatenedNew(newBoard, pieceAfterMove)){
			statuses.add(Status.CHECKS_YOU);
			score = score + 10;
			List<Piece> hisPieces = BoardUtil.getAllTeamPieces(newBoard, -team);
			List<Move> hisMoves = new ArrayList<Move>();
			for(Piece hisPiece : hisPieces){
				hisMoves.addAll(findAllValidMovesThatLeadToCheckMate(newBoard, hisPiece));
			}
			if(hisMoves.size() == 1){
				score = score + (hisMoves.size() == 1 ? 500 : 0);
				Board newNewBoard = hisMoves.get(0).updateBoard(newBoard);
				List<Integer> rewards = new ArrayList<Integer>();
				for(Move myNewMove : getAllMoveForTeam(newNewBoard, team)){
					if(isPieceThreatenedNew(myNewMove.updateBoard(newNewBoard), myNewMove.getPieceAfterMove())){
						rewards.add(getValue(myNewMove.getPieceCaptured()) - getValue(myNewMove.getPieceMoving()));
					}else{
						rewards.add(getValue(myNewMove.getPieceCaptured()));
					}
				}
				score = score + 18*Collections.max(rewards);
			}
		}
		
		/*
		 * Is the path clear for a pawn to get promoted
		 */
		if(iteration > 30 && Math.abs(piece) == BoardUtil.PAWN && isCoastFreeForPawnAccurate(board, pieceBeforeMove) && (isSurpriseCheck || !isThreatenedVeryNaiveApproach(newBoard, piece, move.getToPos()[0], move.getToPos()[1]))){
			statuses.add(Status.PAWN_PROMOTION);
			if(team == BoardUtil.BLACK){
				score = score + (6-move.getToPos()[0])*(6-move.getToPos()[0])*BoardUtil.ROOK;
				statuses.add(Status.PAWN_PROMOTION_LAST_STEP);
			}else if(team == BoardUtil.WHITE){
				score = score + (move.getToPos()[0]-1)*(move.getToPos()[0]-1)*BoardUtil.ROOK;
				statuses.add(Status.PAWN_PROMOTION_LAST_STEP);
			}
		}
		
		int oppBackRow = (team > 0 ? 0 : 7);
		for(Piece hisPiece : allHisPieces){
			if(Math.abs(hisPiece.getValue()) == BoardUtil.PAWN){
				if(isPawnThreateningPromotionAccurate(newBoard, hisPiece)){
					boolean isCoastClear = true;
					
					for(int i=hisPiece.getyPosition();i!=oppBackRow;i=i-team){
						Move pawnMove = new Move(hisPiece.getValue(), hisPiece.getyPosition(), hisPiece.getxPosition(), hisPiece.getyPosition()-team, hisPiece.getxPosition());
						if(isPieceThreatenedNew(pawnMove.updateBoard(newBoard), new Piece(pawnMove.getPieceMoving(), pawnMove.getToPos()))){
							isCoastClear = false; 
							break;
						}
					}
					if(isCoastClear){
						statuses.add(Status.PAWN_PROMOTION_THREAT);
						score = score - getValue(BoardUtil.QUEEN)+10;
					}
					
					
					if((team==-1 && hisPiece.getyPosition()==6) || (team == 1 && hisPiece.getyPosition() == 1)){
						Board promotionBoard = new Board(newBoard.getBoard());
						promotionBoard.setPiece(BoardUtil.SPACE, hisPiece.getyPosition(), hisPiece.getxPosition());
						Board promotionBoard1 = new Board(promotionBoard.getBoard());
						promotionBoard1.setPiece(-team*BoardUtil.QUEEN, oppBackRow, hisPiece.getxPosition());
						if(!isPieceThreatenedNew(promotionBoard1, new Piece(-team*BoardUtil.QUEEN, oppBackRow, hisPiece.getxPosition()))){
							statuses.add(Status.PAWN_PROMOTION_OPP);
							score = score - getValue(BoardUtil.QUEEN)*2;		
						}else if(hisPiece.getxPosition()>0 && promotionBoard.getPiece(oppBackRow, hisPiece.getxPosition()-1)*team > 0){
							Board promotionBoard2 = new Board(promotionBoard.getBoard());
							promotionBoard2.setPiece(-team*BoardUtil.QUEEN, oppBackRow, hisPiece.getxPosition()-1);
							if(!isPieceThreatenedNew(promotionBoard2, new Piece(-team*BoardUtil.QUEEN, oppBackRow, hisPiece.getxPosition()-1))){
								statuses.add(Status.PAWN_PROMOTION_OPP);
								score = score - getValue(BoardUtil.QUEEN)*2;
							}
						}else if(hisPiece.getyPosition()<7 && promotionBoard.getPiece(oppBackRow, hisPiece.getxPosition()+1)*team > 0){
							Board promotionBoard3 = new Board(promotionBoard.getBoard());
							promotionBoard3.setPiece(-team*BoardUtil.QUEEN, oppBackRow, hisPiece.getxPosition()+1);
							if(!isPieceThreatenedNew(promotionBoard3, new Piece(-team*BoardUtil.QUEEN, oppBackRow, hisPiece.getxPosition()+1))){
								statuses.add(Status.PAWN_PROMOTION_OPP);
								score = score - getValue(BoardUtil.QUEEN*2);
							}
						}
					}
				}
				if(isPawnThreateningPromotionAccurate(board, hisPiece) && isCoastFreeForPawnAccurate(board, hisPiece) && !isCoastFreeForPawnAccurate(newBoard, hisPiece) && newBoard.getPiece(hisPiece.getyPosition(), hisPiece.getxPosition()) == hisPiece.getValue()){
					score = score + BoardUtil.QUEEN/2;
					statuses.add(Status.PAWN_PROMOTION_PREVENTION);
				}
				
				if(isPawnThreateningPromotionAccurate(board, hisPiece) && !isPawnThreateningPromotionAccurate(newBoard, hisPiece)){
					if(board.getPiece(oppBackRow, hisPiece.getxPosition()) == BoardUtil.SPACE && newBoard.getPiece(oppBackRow, hisPiece.getxPosition()) * team > BoardUtil.SPACE && !isPieceThreatenedNew(newBoard, pieceAfterMove)){
						statuses.add(Status.PAWN_PROMOTION_PREVENTION);
						score = score + BoardUtil.QUEEN/2;
						if(hisPiece.getyPosition() == Math.abs(oppBackRow - 1)){
							statuses.add(Status.PAWN_PROMOTION_PREVENTION_LAST_ROW);
							score = score + 20;
						}
						if(isPieceThreatenedNew(newBoard, hisPiece)){
							statuses.add(Status.PAWN_PROMOTION_THREAT);
							score = score + 2*BoardUtil.QUEEN;
						}
					}else if(newBoard.getPiece(hisPiece.getyPosition(), hisPiece.getxPosition()) != -team*BoardUtil.PAWN){
						statuses.add(Status.PAWN_PROMOTION_CAPTURE);
						score = score + 3*BoardUtil.QUEEN;
						if(isPieceThreatenedNew(newBoard, pieceAfterMove)){
							statuses.add(Status.LOSING_PIECE_DUE_TO_PAWN_PROMOTION_CAPTURE);
							score = score - 3*getValue(piece);
						}
					}
				}
			}
		}
		
		
		/*
		 * Finds skewers
		 */
		
		int potentialGainFromSkewer = findPotentialRewardFromSkewerAccurate(newBoard, pieceAfterMove);
		
		if(potentialGainFromSkewer > 0){
			score = score + 20*potentialGainFromSkewer;
			statuses.add(Status.SKEWER_GAIN);
		}
		int worseLossFromSkewer = 0;
		for(Move hisMove : getAllMoveForTeam(newBoard, -team)){
			int hisSkewer = findPotentialRewardFromSkewer(hisMove.updateBoard(newBoard), hisMove.getPieceAfterMove());
			if(hisSkewer > 0 && worseLossFromSkewer < hisSkewer){
				worseLossFromSkewer = hisSkewer;
			}
		}
		if(worseLossFromSkewer > 0){
			score = score - 2*worseLossFromSkewer;
			statuses.add(Status.SKEWER_LOSS);
		}
		
		//Look for pinned pieces will be threatened after this. 
		int worsePinnedPieceThreatenedAfterMove = 0;
		for(Move oppMove : oppMoves){
			Board boardAfterOppMove = oppMove.updateBoard(newBoard);
			if(isPieceThreatenedNew(boardAfterOppMove, oppMove.getPieceAfterMove())){
				continue;
			}
			for(Piece myPieceAfter2Tries : BoardUtil.getAllTeamPiecesInPieces(boardAfterOppMove, team)){
				if(isPieceThreatenedNew(boardAfterOppMove, myPieceAfter2Tries) && isPiecePinned(boardAfterOppMove, myPieceAfter2Tries)){
					if(worsePinnedPieceThreatenedAfterMove < getValue(myPieceAfter2Tries)){
						worsePinnedPieceThreatenedAfterMove = getValue(myPieceAfter2Tries);
					}
				}	
			}
		}
		
		if(worsePinnedPieceThreatenedAfterMove > 0){
			score = score - worsePinnedPieceThreatenedAfterMove;
			statuses.add(Status.PINS_THREAT_IS_REAL);
		}
		
		/*
		 * Finds pins
		 */
		
		int[] previousPin = findPinnedPieceFromAbsolutePin(board, move.getPieceMoving(), move.getFromPos());
		int[] pin = findPinnedPieceFromAbsolutePin(newBoard, move.getPieceMoving(), move.getToPos());
		
		if(previousPin[0] == BoardUtil.SPACE && pin[0] != BoardUtil.SPACE){
			score = score + getValue(pin[0]);
			statuses.add(Status.PINS_GAIN);
		}

		List<AbsolutePin> currentPinSituationOtherTeam = findAllPinnedPiecesFromAbsolutePin(newBoard, -team);
		int worsePin = 0;
		
		for(AbsolutePin currentPin : currentPinSituationOtherTeam){
			if(getValue(currentPin.getPinnedPiece()[0]) > worsePin){
				worsePin = getValue(currentPin.getPinnedPiece()[0]);
			}
		}

		if(!statuses.contains(Status.PINS_THREAT_IS_REAL) && worsePin > 0){
			statuses.add(Status.PINS_LOSS);
			score = score - 5*worsePin;
		}
		
		//Surprise threats...
		if(oppThreats.size() == 1){
			Move oppCaptureMyPiece = new Move(oppThreats.get(0), move.getPieceAfterMove());
			if(isMoveValid(newBoard, oppCaptureMyPiece)){
				Board boardAfterOppCapturesMyPiece = new Move(oppThreats.get(0), move.getPieceAfterMove()).updateBoard(newBoard);
				if(isPiecePinned(newBoard, oppThreats.get(0))){
					AbsolutePin apin = getPin(newBoard, oppThreats.get(0));
					Piece hvp = new Piece(apin.getEndPiece());
					if(isPieceThreatenedNew(boardAfterOppCapturesMyPiece, hvp) && !isPieceThreatenedNew(board, hvp)){
						List<Piece> hvpThreats = findThreatsAccurate(boardAfterOppCapturesMyPiece, hvp);
						int scoreForSurpriseCapture = getValue(hvp) - getValue(move.getPieceMoving()) + getValue(move.getPieceCaptured());
						Move mostLikelyHVPCapture = new Move(Collections.min(hvpThreats, PieceComparatorForPieces), hvp);
						if(isPieceThreatenedNew(mostLikelyHVPCapture.updateBoard(boardAfterOppCapturesMyPiece), mostLikelyHVPCapture.getPieceAfterMove())){
							scoreForSurpriseCapture = scoreForSurpriseCapture - getValue(mostLikelyHVPCapture.getPieceMoving());
						}
						score = score + 18*scoreForSurpriseCapture;
						statuses.add(Status.SURPRISE_CAPTURE);
					}
				}
			}
		}
		
		//Does this move lead to a fork of a much better reward
		/*
		 * For now, only consider sacrifices that lead to future fork. Reason being, future forks depend on what the opponent does. 
		 * There is no reason to guarantee move will lead to future fork
		 */
		int worsePieceThreatened = 0;
		for(Piece myPiece : allMyPieces){
			if(isPieceThreatenedNew(board, myPiece)){
				worsePieceThreatened = Math.max(worsePieceThreatened, getValue(myPiece));
			}
		}
		
		if(isPieceThreatenedNew(newBoard, pieceAfterMove) && oppThreats.size() == 1 && piece*team > BoardUtil.PAWN){
			int potentialGainFromFutureForkAfterSacrifice = -1000000;
			//Assume capture
			
			//Assume capture is done by lower piece
//			Piece minOppThreat = Collections.min(oppThreats, PieceComparatorForPieces);
			Piece onlyThreat = oppThreats.get(0);
//			Move oppCapture = new Move(minOppThreat, pieceAfterMove);
			Move oppCapture = new Move(onlyThreat, pieceAfterMove);
			Board newBoardAfterOppCapture = oppCapture.updateBoard(newBoard);
			for(Piece myPieceAfterCapture : BoardUtil.getAllTeamHighPiecesInPieces(newBoardAfterOppCapture, team)){
				List<Move> movesBeforeTheForkByThePiece = findAllValidMoves(newBoardAfterOppCapture, myPieceAfterCapture);
				for(Move moveBeforeTheForkByThePiece : movesBeforeTheForkByThePiece){
					Board forkingBoard = moveBeforeTheForkByThePiece.updateBoard(newBoardAfterOppCapture);
					Piece myPieceAtTheFork = new Piece(myPieceAfterCapture.getValue(), moveBeforeTheForkByThePiece.getToPos());
					if(isPieceForkingAccurate(forkingBoard, myPieceAtTheFork)){
						List<Piece> forkedPieces = findAttackPieces(forkingBoard, myPieceAtTheFork);
						if(forkedPieces.size() > 0){
							int thisPotentialGainFromFutureForkAfterSacrifice = findPotentialGainFromForkAccurate(forkingBoard, myPieceAtTheFork, forkedPieces) - getValue(pieceAfterMove) + getValue(move.getPieceCaptured()) + getValue(moveBeforeTheForkByThePiece.getPieceCaptured());
							
							Piece higherValue = Collections.max(forkedPieces, PieceComparatorForPieces);
							Piece lowerValue = Collections.min(forkedPieces, PieceComparatorForPieces);
							for(Move higherValueMove : findAllValidMoves(forkingBoard, higherValue)){
								Board boardAfterHighPieceDefendsLowPiece = higherValueMove.updateBoard(forkingBoard);
								Move captureFromTheFork = new Move(moveBeforeTheForkByThePiece.getPieceAfterMove(), lowerValue);
								Board boardAfterForkedCapturesLowPiece = captureFromTheFork.updateBoard(boardAfterHighPieceDefendsLowPiece); 
								if(
										isPieceThreatenedNew(boardAfterForkedCapturesLowPiece, captureFromTheFork.getPieceAfterMove()) && 
										!isPieceThreatenedNew(boardAfterHighPieceDefendsLowPiece, higherValueMove.getPieceAfterMove())
										){
									/*
									 * The logic above is fairly complicated but in general, it determines whether the piece that is forking can capture the lowest of the 2 forks without being threatened. If it is threatened, the reward must be taken in consideration since we've already sacrificed a piece to get here. 
									 */
									
									thisPotentialGainFromFutureForkAfterSacrifice = thisPotentialGainFromFutureForkAfterSacrifice - getValue(moveBeforeTheForkByThePiece.getPieceAfterMove());
									break;
								}
							}
							potentialGainFromFutureForkAfterSacrifice = Math.max(potentialGainFromFutureForkAfterSacrifice, thisPotentialGainFromFutureForkAfterSacrifice);
						}
						//TODO: NOTICE THAT findPotentialGainFromForkAccurate leaves the gain in score format.
					}
				}
			}
			if(potentialGainFromFutureForkAfterSacrifice > 0){
				score = score + 15 * potentialGainFromFutureForkAfterSacrifice + getValue(piece)/10; //<--- The higher the sacrifice, the greater the reward.
				statuses.add(Status.FUTURE_FORK_GAIN);
			}
		}
		
		int potentialGainFromFork = 0;
		
		if(isPieceForkingAccurate(newBoard, pieceAfterMove)){
			List<Piece> attacks = findAttackPieces(newBoard, new Piece(piece, move.getToPos()[0], move.getToPos()[1]));
			statuses.add(Status.CURRENT_FORK_GAIN);
			potentialGainFromFork = findPotentialGainFromForkAccurate(newBoard, new Piece(newBoard, move.getToPos()), attacks);
			if(couldHaveCapturedInsteadOfFork(board, move, potentialGainFromFork)){
				score = score - 5*potentialGainFromFork;
				statuses.add(Status.PENALTY_FOR_FORKING_INSTEAD_OF_CAPTURING);
			}
		}
		
		int potentialLossFromFork = 0;
		Set<Integer> allPotentialLossesFromFork = new HashSet<Integer>();
		for(Move otherTeamMove : oppMoves){
			Board newBoardWithOtherTeamMove = otherTeamMove.updateBoard(newBoard);
			if(isPieceForkingAccurate(newBoardWithOtherTeamMove, new Piece(otherTeamMove.getPieceMoving(), otherTeamMove.getToPos()))){
				List<Piece> allForkedPieces = findAllForkPieces(newBoardWithOtherTeamMove, new Piece(otherTeamMove.getPieceMoving(), otherTeamMove.getToPos()));
				int loss = findPotentialGainFromForkAccurate(newBoardWithOtherTeamMove, new Piece(otherTeamMove.getPieceMoving(), otherTeamMove.getToPos()), allForkedPieces);
				
				if(loss > 0){
					allPotentialLossesFromFork.add(loss);
					statuses.add(Status.CURRENT_FORK_LOSS);
				}
			}
		}
		/*
		 * NOTE: Fork score is added twice as threat is accounting for score. Example pawn forks knight and queen. 
		 * Point given for fork on knight assuming queen escapes.
		 * Point also given to threat given to queen and knight.   
		 */
		if(allPotentialLossesFromFork.size() > 0){
			potentialLossFromFork = Collections.max(allPotentialLossesFromFork);
		}else{
			potentialLossFromFork = 0;
		}
//		score = score - 190*(potentialLossFromFork > BoardUtil.PAWN ? potentialLossFromFork : 0) + 180*potentialGainFromFork + 160*potentialGainFromFutureFork;
//		score = score - 150*(potentialLossFromFork > BoardUtil.PAWN ? potentialLossFromFork : 0) + 70*potentialGainFromFork + 20*potentialGainFromFutureFork;
		score = score - 18*(potentialLossFromFork == BoardUtil.PAWN ? 10 : potentialLossFromFork) + 19*potentialGainFromFork;
//		score = score - 150*potentialLossFromFork + 100*potentialGainFromFork;
		
		if(isPieceThreatenedNew(board, pieceBeforeMove)  && 
		  !isPieceThreatenedNew(newBoard, pieceAfterMove)
				){
			
			if(oppThreats.size() > 0){
				Piece maxThreat = oppThreats.get(0);
				if(getValue(maxThreat) == getValue(piece)){
					/*
					 * The motivation behind labeling a potential trade as a threat if in case a piece can be captured by multiple attackers, capturing with a lower piece.
					 * NOTE: This might not work in case the lower piece can also be traded.   
					 */
					if(boardStatus > 0){
						statuses.add(Status.TRADE_WELCOME);
						score = score + getValue(piece);
					}else{
						statuses.add(Status.TRADE_THREAT);
						score = score - getValue(piece);
					}
				}else{
					statuses.add(Status.PROTECTED);
					score = score + getValue(piece);
				}
			}
		}
		
		int movesBeforeMove = getAllMoveForTeam(board, team).size() - getAllMoveForTeam(board, -team).size();
		int movesAfterMove = getAllMoveForTeam(newBoard, team).size() - getAllMoveForTeam(newBoard, -team).size();
		if(movesAfterMove > movesBeforeMove){
			score = score + (movesAfterMove - movesBeforeMove);
			statuses.add(Status.ENABLING_MOVES);
		}else if(movesBeforeMove > movesAfterMove){
			score = score + (movesAfterMove - movesBeforeMove);
			statuses.add(Status.OBSTRUCTING_MOVES);
		}
		
		List<Integer> piecesInDangerBeforeMove = new ArrayList<Integer>();
		for(Piece myPiece : BoardUtil.getAllTeamPiecesInPieces(newBoard, team)){
			if(isPieceThreatenedNew(newBoard, myPiece)){
				piecesInDangerBeforeMove.add(getValue(myPiece));
			}
		}
		List<Integer> piecesInDanger = new ArrayList<Integer>();
		for(Piece myPieceAfterMove : BoardUtil.getAllTeamPiecesInPieces(newBoard, team)){
			if(isPieceThreatenedNew(newBoard, myPieceAfterMove)){
				piecesInDanger.add(getValue(myPieceAfterMove));
			}
		}
		if(piecesInDangerBeforeMove.size()==0){
			piecesInDangerBeforeMove.add(0);
		}
		if(piecesInDanger.size()==0) {
			piecesInDanger.add(0);
		}
		
		int maxDangerBefore = Collections.max(piecesInDangerBeforeMove);
		int maxDangerAfter = Collections.max(piecesInDanger);
		score = score - (maxDangerAfter - maxDangerBefore);
		if(maxDangerBefore > maxDangerAfter){
			score = score + 10;
			statuses.add(Status.PROTECTED);
		}else if(maxDangerAfter > maxDangerBefore){
			statuses.add(Status.THIS_MOVE_IS_MAKING_THINGS_WORSE);
			score = score - 10;
		}
		
		
		if(!statuses.contains(Status.FUTURE_FORK_GAIN)){
			//Are any of my pieces in danger before and after the move?
			List<Integer> listOfPiecesThreatened = new ArrayList<Integer>();
			for(Piece myPiece: allMyPieces){
				if(myPiece.getValue() == move.getPieceMoving() && myPiece.getyPosition() == move.getFromPos()[0] && myPiece.getxPosition() == move.getFromPos()[1]){
					if(isPieceThreatenedNew(newBoard, pieceAfterMove)){
						statuses.add(Status.IS_CURRENT_MOVING_PIECE_THREATENED_AFTER_MOVE);
						//Further penalize stupid movement. We do not condone sacrifices here. Special logic must figure this out.
						listOfPiecesThreatened.add(20*getValue(myPiece));
					}
					if(move.getPieceCaptured() != BoardUtil.SPACE){
						statuses.add(Status.WAS_ONE_OF_HIS_PIECES_THREATENED_AND_THEN_KILLED);
						score = score + 20*getValue(move.getPieceCaptured()) - getValue(myPiece)/10;
						if(isPawnPromotion(board, move.getToPos())){
							score = score + 1000;
							statuses.add(Status.CAPTURED_PAWN_ABOUT_TO_PROMOTE);
						}
					}
				}else{
					if(isPieceThreatenedNew(newBoard, myPiece)){
						statuses.add(Status.IS_ONE_OF_MY_PIECES_THREATENED);
						if(Math.abs(myPiece.getValue())==BoardUtil.PAWN){
							if(isPawnStackedIsolatedOrBlocked(newBoard, myPiece)){
								statuses.add(Status.THREAT_OF_ISOLATED_PAWN);
								listOfPiecesThreatened.add(20*getValue(myPiece) - getValue(myPiece)/10 -3);
							}else{
								statuses.add(Status.THREAT_OF_GOOD_PAWN);
								listOfPiecesThreatened.add(20*getValue(myPiece) - getValue(myPiece)/10 +3);
							}
						}else{
							listOfPiecesThreatened.add(20*getValue(myPiece) - getValue(myPiece)/10);
						}
					}
				}
			}
			if(listOfPiecesThreatened.size()>0){
				int worseThreat = Collections.max(listOfPiecesThreatened);
				if(potentialLossFromFork < worseThreat){
					score = score - (worseThreat-potentialLossFromFork);
				}
			}
		}
		
		//Count the number of high pieces in the back row. This is to help promote the movement of pieces
		int backRowPrevious = 0;
		int backRowInt = team==BoardUtil.WHITE?0:7;
		for(int[] highPiece : BoardUtil.getAllTeamHighPieces(board, team)){
			if(highPiece[1] == backRowInt){
				backRowPrevious = backRowPrevious + Math.abs(highPiece[0]);
			}
		}
		
		int backRow = 0;
		for(int[] highPiece : BoardUtil.getAllTeamHighPieces(newBoard, team)){
			if(highPiece[1] == backRowInt){
				backRow = backRow + Math.abs(highPiece[0]);
			}
		}

		if(iteration < 10){
			backRow = backRow - 2*BoardUtil.ROOK - BoardUtil.QUEEN/2;
			backRowPrevious = backRow - 2 * BoardUtil.ROOK - BoardUtil.QUEEN/2;
			if((backRow-backRowPrevious) >= 0 && backRow > 0){
				score = score - backRow/2;
				statuses.add(Status.BACK_ROW_PIECES);
			}
		}
		
		//TODO: REMOVE THIS BLOCK OF CODE. IT DOES NOTHING GOOD.
		//Is this a move that is threatening one of his pieces? While not putting itself in a threatening position?
		List<Integer> hisPiecesThreatened = new ArrayList<Integer>();  
		for(Piece hisPiece : allHisPieces){
			
			//You cannot use the isPieceThreatened method since it violates the assumption where the team has no longer a move to make. 
			//In this case, the opponent does have a move to make.
			
			if(
					!isPieceThreatenedNew(board, hisPiece) && isPieceThreatenedNew(newBoard, hisPiece) && 
					(!isPieceThreatenedNew(newBoard, pieceAfterMove))
					){
				
				/*
				 * TODO: REMOVE THIS MONSTROSITY!!!! THIS TALLIES ALL OF THE THREATS MADE. RIDICULOUS! PLUS THIS IS ALREADY HANDLED BY "ADDING_THREATS"
				 * THIS IS ESSENTIALLY INFLATING THREATENING PIECES OVER ANYTHING ELSE.
				 */
				if(!isSurpriseCheck && Math.abs(hisPiece.getValue()) != BoardUtil.KING){
					hisPiecesThreatened.add(getValue(hisPiece)*(isSurpriseCheck?2:1));
				}
				if(isPawnPromotion(board, move.getToPos())){
					score = score + 1000 + (isSurpriseCheck?1000:0);
					statuses.add(Status.THREATEN_PAWN_ABOUT_TO_PROMOTE);
				}
			}
		}
		if(hisPiecesThreatened.size() > 0){
			statuses.add(Status.WAS_ONE_OF_HIS_PIECES_NOT_THREATENED_BUT_NOW_IS_THREATENED);
			score = score + Collections.max(hisPiecesThreatened);
		}
		return statuses;
	}
	
	public int keepCheckingUntilMate(Board board, int team){
		mateMoveMap.clear();
		return keepCheckingUntilMate(board, team, 7, -1000000, 1000000, 1, 7);
	}
	public int keepCheckingUntilMate(Board board, int team, int depth, int alpha, int beta, int maximizing, int MAX_DEPTH){
		if(isCheckMate(board, -team)){
			return MATE_FOUND - (MAX_DEPTH - depth);
		}
		if(depth == 0){
			return 0;
		}
		
		int v = 0;
		if(maximizing>0){
			v = -1000000000;
			List<Move> moves = getAllMoveForTeam(board, team);
			
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				if(!isCheck(newBoard, -team) || isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
					continue;
				}
				int bestScore = keepCheckingUntilMate(newBoard, team, depth-1, alpha, beta, -1, MAX_DEPTH);
				if(depth == MAX_DEPTH){
					if(mateMoveMap.get(bestScore) == null){
						mateMoveMap.put(bestScore, new ArrayList<Move>());
					}
					mateMoveMap.get(bestScore).add(move);
					if(isDeveloper){
						System.out.println(move + "\t" + bestScore);
					}
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
			List<Move> moves = getAllMoveForTeam(board, -team);
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
	
	
	@Override
	public Move findMove(Board board) {
		return findMove(board, 50, false, true, new ArrayList<Move>());
	}
	
	@Override
	public int getTeam() {
		return team;
	}
	
	public int getScore() {
		return score;
	}
}