package com.ozone.movements;

import static com.ozone.movements.MoveUtil.PieceComparatorForPieces;
import static com.ozone.movements.MoveUtil.canKingGetToPiece;
import static com.ozone.movements.MoveUtil.findAllValidMoves;
import static com.ozone.movements.MoveUtil.findAllValidMovesThatLeadToCheckMate;
import static com.ozone.movements.MoveUtil.findThreatsAccurate;
import static com.ozone.movements.MoveUtil.findVanillaLinesOfMovesForPiece;
import static com.ozone.movements.MoveUtil.getAllMoveForTeam;
import static com.ozone.movements.MoveUtil.getAllMoveForTeamWithoutSuicides;
import static com.ozone.movements.MoveUtil.getValue;
import static com.ozone.movements.MoveUtil.isCheck;
import static com.ozone.movements.MoveUtil.isCheckMate;
import static com.ozone.movements.MoveUtil.isDefended;
import static com.ozone.movements.MoveUtil.isDefendedByAnythingButTheKing;
import static com.ozone.movements.MoveUtil.isMoveValid;
import static com.ozone.movements.MoveUtil.isPieceThreatenedNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ozone.common.AbsolutePin;
import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.movements.BoardUtil;

public class AdvancedMoveUtil {
	
	public static final List<Move> GOOD_MOVES = Arrays.asList(
			new Move(BoardUtil.PAWN, "e2","e4"),
			new Move(BoardUtil.PAWN, "e2","e3"),
			new Move(BoardUtil.PAWN, "d2","d4"),
			new Move(BoardUtil.PAWN, "d2","d3"),
			new Move(BoardUtil.KNIGHT, "b1","c3"),
			new Move(BoardUtil.KNIGHT, "b1","d2"),
			new Move(BoardUtil.KNIGHT, "g1","f3"),
			new Move(BoardUtil.KNIGHT, "g1","e2"),
			new Move(BoardUtil.BISHOP, "c1","e3"),
			new Move(BoardUtil.BISHOP, "c1","f4"),
			new Move(BoardUtil.BISHOP, "c1","g5"),
			new Move(BoardUtil.BISHOP, "f1","d3"),
			new Move(BoardUtil.BISHOP, "f1","c4"),
			new Move(BoardUtil.BISHOP, "f1","b5"),

			new Move(-BoardUtil.PAWN, "e7","e5"),
			new Move(-BoardUtil.PAWN, "e7","e6"),
			new Move(-BoardUtil.PAWN, "d7","d5"),
			new Move(-BoardUtil.PAWN, "d7","d6"),
			new Move(-BoardUtil.KNIGHT, "b8","c6"),
			new Move(-BoardUtil.KNIGHT, "b8","d7"),
			new Move(-BoardUtil.KNIGHT, "g8","f6"),
			new Move(-BoardUtil.KNIGHT, "g8","e7"),
			new Move(-BoardUtil.BISHOP, "c8","e6"),
			new Move(-BoardUtil.BISHOP, "c8","f5"),
			new Move(-BoardUtil.BISHOP, "c8","g4"),
			new Move(-BoardUtil.BISHOP, "f8","d6"),
			new Move(-BoardUtil.BISHOP, "f8","c5"),
			new Move(-BoardUtil.BISHOP, "f8","b4")
	);

	public static boolean isMovePartOfGoodMoves(Move move) {
		return GOOD_MOVES.contains(move);
	}
	
	public static boolean isPieceForkingAccurate(Board board, Piece piece){
		if(isPieceThreatenedNew(board, piece)){
			return false;
		}
		//At this point we know the piece isn't threatened.... But can it be traded? If it is, this ain't no fork!
		List<Piece> oppThreatsAtTheForkPosition = findThreatsAccurate(board, piece);
		for(Piece oppThreat: oppThreatsAtTheForkPosition){
			if(getValue(oppThreat.getValue()) == getValue(piece)){
				return false;
			}
		}
		
		int team = piece.getTeam();
		//All valid moves from the piece that may or may not be forking.
		List<Move> moves = findAllValidMoves(board, piece);
		List<Piece> forkedPieces = new ArrayList<Piece>();
		int threats = 0;
		for(Move move : moves){
			//Is the place where the piece is moving to occupied by a piece of the opposite team?
			//Note: the reason move.getPieceCaptued() is not used is because the king will never be found there. This logic includes the king as well.
			if(board.getPiece(move.getToPos()[0], move.getToPos()[1]) * team < 0){
				//Is the move were to take place, would the piece be protected? Or is the new piece the King? Or is the new piece of higher value? 
 				
				//newBoard here is the board if the forking piece were to hypothetically capture one of the forked pieces. 
				Board newBoard = move.updateBoard(board);
				if(getValue(board.getPiece(move.getToPos())) > getValue(piece)){
					//It doesn't matter if those pieces are protected, the sacrifice is worth it. 
					forkedPieces.add(new Piece(board, move.getToPos()));
					threats++;
				}else if(Math.abs(board.getPiece(move.getToPos())) == BoardUtil.KING){
					//The King must be defended and thus Forking the King is a great move.
					forkedPieces.add(new Piece(board, move.getToPos()));
					threats++;
				}else if(!isPieceThreatenedNew(newBoard, new Piece(move.getPieceMoving(), move.getToPos()))){
					//For everything else, the pieces must be in the clear.
					forkedPieces.add(new Piece(board, move.getToPos()));
					threats++;
					//TODO: ^ There is an exception to this. There is a way for one of the forking piece to protect the other piece while moving. Make sure that it cannot. 
					//^ To do this, one would need to iterate over all opposite moves and check each combination of iteration that the threat number is always > 1. This is an expensive method. 
					//^ It might be worth making that mistake for the benefit of computational time.
				}
			}
		}
		//Special case: when there are exactly 2 pieces threatened, can one piece protect the other? And if so, will the capture of the other still be an advantage?
		if(threats == 2){
			Piece higherValue = Collections.max(forkedPieces, PieceComparatorForPieces);
			Piece lowerValue = Collections.min(forkedPieces, PieceComparatorForPieces);
			for(Move move : findAllValidMoves(board, higherValue)){
				Board newBoard = move.updateBoard(board);
				if(!isPieceThreatenedNew(newBoard, lowerValue) && !isPieceThreatenedNew(newBoard, move.getPieceAfterMove())){
					return false;
				}
			}
		}
		return (threats>1);
	}
	
	public static int findPotentialRewardFromSkewerAccurate(Board board, Piece piece){
		if(isPieceThreatenedNew(board, piece)){
			return BoardUtil.SPACE;
		}
		int team = piece.getTeam();
		List<List<Move>> linesOfMoves = findVanillaLinesOfMovesForPiece(piece.toIntArray());
		for(List<Move> lineOfMoves: linesOfMoves){
			boolean isFirstPieceFound = false;
			Piece oppKing = null;
			for(Move move : lineOfMoves){
				int pieceOnBoard = board.getPiece(move.getToPos());
				if(pieceOnBoard == BoardUtil.SPACE) continue;
				if(!isFirstPieceFound && pieceOnBoard*team == -BoardUtil.KING){
					oppKing = new Piece(pieceOnBoard, move.getToPos());
					isFirstPieceFound = true;
					/*
					 * If the previous piece was found and that the second piece is of higher value.
					 * The order needs to be taken in consideration.
					 * ^ So this is kinda ugly but I am pretty sure that the getVanillaMove method ensures this, that is, all moves start from the piece.  
					 */
				}else if(isFirstPieceFound && pieceOnBoard*team < -BoardUtil.PAWN){
					if(getValue(pieceOnBoard) > getValue(piece)){
						if(!canOtherPieceLowerInValueGetInLine(board, lineOfMoves, oppKing, piece)){
							return getValue(pieceOnBoard);
						}
					}else if(!isDefendedByAnythingButTheKing(board, pieceOnBoard, move.getToPos()) && !canKingGetToPiece(board, new Piece(pieceOnBoard, move.getToPos()))){
						if(!canOtherPieceLowerInValueGetInLine(board, lineOfMoves, oppKing, piece)){
							return getValue(pieceOnBoard);
						}
					}
					return BoardUtil.SPACE;
				}else if(pieceOnBoard*team != -BoardUtil.KING){
					break;
				}
			}
		}
		return BoardUtil.SPACE;
	}
	private static boolean canOtherPieceLowerInValueGetInLine(Board board, List<Move> lineOfMoves, Piece oppKing, Piece piece) {
		for(Move move : lineOfMoves){
			if(board.getPiece(move.getToPos()) == oppKing.getValue()){
				//Assumes that the line of moves start from the piece
				break;
			}else{
				for(Move oppMove : getAllMoveForTeam(board, -piece.getTeam())){
					if(oppMove.getToPos()[0] == move.getToPos()[0] && oppMove.getToPos()[1] == move.getToPos()[1]){
						Board newNewBoard = move.updateBoard(oppMove.updateBoard(board));
						if(isPieceThreatenedNew(newNewBoard, new Piece(piece.getValue(), move.getToPos()))){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	//TODO: It's not a skewer if the piece taking it has the value and the skewed reward is defended.
	public static int findPotentialRewardFromSkewer(Board board, Piece piece){
		if(isPieceThreatenedNew(board, piece)){
			return BoardUtil.SPACE;
		}
		int team = piece.getTeam();
		List<List<Move>> linesOfMoves = findVanillaLinesOfMovesForPiece(piece.toIntArray());
		for(List<Move> lineOfMoves: linesOfMoves){
			boolean isFirstPieceFound = false;
			for(Move move : lineOfMoves){
				int pieceOnBoard = board.getPiece(move.getToPos());
				if(pieceOnBoard == BoardUtil.SPACE) continue;
				if(!isFirstPieceFound && pieceOnBoard*team == -BoardUtil.KING){
					isFirstPieceFound = true;
				/*
				 * If the previous piece was found and that the second piece is of higher value.
				 * The order needs to be taken in consideration.
				 * ^ So this is kinda ugly but I am pretty sure that the getVanillaMove method ensures this, that is, all moves start from the piece.  
				 */
				}else if(isFirstPieceFound && pieceOnBoard*team < -BoardUtil.PAWN){
					if(Math.abs(pieceOnBoard) > Math.abs(piece.getValue())){
						return Math.abs(pieceOnBoard);
					}else if(!isDefendedByAnythingButTheKing(board, pieceOnBoard, move.getToPos()) && !canKingGetToPiece(board, new Piece(pieceOnBoard, move.getToPos()))){
						return Math.abs(pieceOnBoard);
					}
					return BoardUtil.SPACE;
				}else if(pieceOnBoard*team != -BoardUtil.KING){
					break;
				}
			}
		}
		return BoardUtil.SPACE;
	}
	
	public static boolean isPiecePinned(Board board, Piece piece){
		return isPiecePinned(board, piece.getValue(), piece.getyPosition(), piece.getxPosition());
	}
	public static boolean isPiecePinned(Board board, int piece, int y, int x){
		Piece pinnedPiece = new Piece(piece, y, x);
		int team = piece>0?1:-1;
		boolean foundPinner = false;
		boolean foundHVP = false;
		Piece pinner = null;
		Piece hvp = null;
		//MOVING UP FIRST
		for(int i=y;i<8;i++){
			if(i==y)continue;
			int pieceOnBoard = board.getPiece(i,x);
			if(pieceOnBoard==0) continue;
			if(pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT) {
				hvp = new Piece(pieceOnBoard, i, x);
				foundHVP = true;
				break;
			}
			if(-team*pieceOnBoard > BoardUtil.BISHOP) {
				pinner = new Piece(pieceOnBoard, i, x);
				foundPinner = true;
				break;
			}
			break;
		}
		if(foundPinner || foundHVP){
			//MOVING DOWN NEXT
			for(int i=y;i>=0;i--){
				if(i==y)continue;
				int pieceOnBoard = board.getPiece(i,x);
				if(pieceOnBoard==0) continue;
				if(foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)) {
					hvp = new Piece(pieceOnBoard, i, x);
					foundHVP = true;
					break;
				}
				if(foundHVP && -team*pieceOnBoard > BoardUtil.BISHOP) {
					pinner = new Piece(pieceOnBoard, i, x);
					foundPinner = true;
					break;
				}
				break;
			}
		}
		if(foundPinner && foundHVP) return determineIfPieceCombinationDeterminePin(pinner.getValue(), piece, hvp.getValue(), false, isDefended(board, pinner.getValue(), pinner.getyPosition(), pinner.getxPosition()), canPawnReclaimPinner(board, pinnedPiece, pinner));
		foundPinner = false;
		foundHVP = false;
		//MOVING RIGHT FIRST
		for(int i=x;i<8;i++){
			if(i==x)continue;
			int pieceOnBoard = board.getPiece(y,i);
			if(pieceOnBoard==0) continue;
			if(pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT) {
				hvp = new Piece(pieceOnBoard, y, i);
				foundHVP = true;
				break;
			}
			if(-team*pieceOnBoard > BoardUtil.BISHOP) {
				pinner = new Piece(pieceOnBoard, y, i);
				foundPinner = true;
				break;
			}
			break;
		}
		if(foundPinner || foundHVP){
			//MOVING LEFT NEXT
			for(int i=x;i>=0;i--){
				if(i==x)continue;
				int pieceOnBoard = board.getPiece(y,i);
				if(pieceOnBoard==0) continue;
				if(foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)) {
					hvp = new Piece(pieceOnBoard, y, i);
					foundHVP = true;
					break;
				}
				if(foundHVP && -team*pieceOnBoard > BoardUtil.BISHOP) {
					pinner = new Piece(pieceOnBoard, y, i);
					foundPinner = true;
					break;
				}
				break;
			}
		}
		if(foundPinner && foundHVP) return determineIfPieceCombinationDeterminePin(pinner.getValue(), piece, hvp.getValue(), false, isDefended(board, pinner.getValue(), pinner.getyPosition(), pinner.getxPosition()), canPawnReclaimPinner(board, pinnedPiece, pinner));
		foundPinner = false;
		foundHVP = false;
		int j = x;
		//MOVING UPRIGHT FIRST
		for(int i=y;i<8;i++){
			if(i==y)continue;
			j++;
			if(i>7||j>7) break;
			int pieceOnBoard = board.getPiece(i,j);
			if(pieceOnBoard==0) continue;
			if(pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT){
				hvp = new Piece(pieceOnBoard, i, j);
				foundHVP = true;
				break;
			}
			if(pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP){
				pinner = new Piece(pieceOnBoard, i, j);
				foundPinner = true;
				break;
			}
			break;
		}
		if(foundPinner || foundHVP){
			j=x;
			//MOVING DOWNLEFT NEXT
			for(int i=y;i>=0;i--){
				if(i==y)continue;
				j--;
				if(i>7||j<0) break;
				int pieceOnBoard = board.getPiece(i,j);
				if(pieceOnBoard==0) continue;
				if(foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)){
					hvp = new Piece(pieceOnBoard, i, j);
					foundHVP = true;
					break;
				}
				if(foundHVP && (pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP)){
					pinner = new Piece(pieceOnBoard, i, j);
					foundPinner = true;
					break;
				}
				break;
			}
		}
		if(foundPinner && foundHVP) return determineIfPieceCombinationDeterminePin(pinner.getValue(), piece, hvp.getValue(), true, isDefended(board, pinner.getValue(), pinner.getyPosition(), pinner.getxPosition()), canPawnReclaimPinner(board, pinnedPiece, pinner));
		foundPinner = false;
		foundHVP = false;
		j=x;
		//MOVING UPLEFT FIRST
		for(int i=y;i<8;i++){
			if(i==y)continue;
			j--;
			if(i>7 || j<0)break;
			int pieceOnBoard = board.getPiece(i,j);
			if(pieceOnBoard==0)continue;
			if((pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)){
				hvp = new Piece(pieceOnBoard, i, j);
				foundHVP = true;
				break;
			}
			if(pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP){
				pinner = new Piece(pieceOnBoard, i, j);
				foundPinner = true;
			}
			break;
		}
		if(foundPinner || foundHVP){
			j=x;
			//MOVING DOWNRIGHT NEXT
			for(int i=y;i>=0;i--){
				if(i==y)continue;
				j++;
				if(i<0 || j>7)break;
				int pieceOnBoard = board.getPiece(i,j);
				if(pieceOnBoard==0)continue;
				if((foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT))){
					hvp = new Piece(pieceOnBoard, i, j);
					foundHVP = true;
					break;
				}
				if(foundHVP && (pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP)){
					pinner = new Piece(pieceOnBoard, i, j);
					foundPinner = true;
				}
				break;
			}
		}
		if(foundPinner && foundHVP) return determineIfPieceCombinationDeterminePin(pinner.getValue(), piece, hvp.getValue(), true, isDefended(board, pinner.getValue(), pinner.getyPosition(), pinner.getxPosition()), canPawnReclaimPinner(board, pinnedPiece, pinner));
		return false;
	}
	
	public static AbsolutePin getPin(Board board, Piece pinnedPiece){
		int team = pinnedPiece.getTeam();
		boolean foundPinner = false;
		boolean foundHVP = false;
		int x = pinnedPiece.getxPosition();
		int y = pinnedPiece.getyPosition();
		Piece pinner = null;
		Piece hvp = null;
		//MOVING UP FIRST
		for(int i=y;i<8;i++){
			if(i==y)continue;
			int pieceOnBoard = board.getPiece(i,x);
			if(pieceOnBoard==0) continue;
			if(pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT) {
				hvp = new Piece(pieceOnBoard, i, x);
				foundHVP = true;
				break;
			}
			if(-team*pieceOnBoard > BoardUtil.BISHOP) {
				pinner = new Piece(pieceOnBoard, i, x);
				foundPinner = true;
				break;
			}
			break;
		}
		if(foundPinner || foundHVP){
			//MOVING DOWN NEXT
			for(int i=y;i>=0;i--){
				if(i==y)continue;
				int pieceOnBoard = board.getPiece(i,x);
				if(pieceOnBoard==0) continue;
				if(foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)) {
					hvp = new Piece(pieceOnBoard, i, x);
					foundHVP = true;
					break;
				}
				if(foundHVP && -team*pieceOnBoard > BoardUtil.BISHOP) {
					pinner = new Piece(pieceOnBoard, i, x);
					foundPinner = true;
					break;
				}
				break;
			}
		}
		if(foundPinner && foundHVP && determineIfPieceCombinationDeterminePin(pinner.getValue(), pinnedPiece.getValue(), hvp.getValue(), false, isDefended(board, pinnedPiece.getValue(), y, x), canPawnReclaimPinner(board, pinnedPiece, pinner))) {
			return new AbsolutePin(pinner, pinnedPiece, hvp, false);
		}
		foundPinner = false;
		foundHVP = false;
		//MOVING RIGHT FIRST
		for(int i=x;i<8;i++){
			if(i==x)continue;
			int pieceOnBoard = board.getPiece(y,i);
			if(pieceOnBoard==0) continue;
			if(pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT) {
				hvp = new Piece(pieceOnBoard, y, i);
				foundHVP = true;
				break;
			}
			if(-team*pieceOnBoard > BoardUtil.BISHOP) {
				pinner = new Piece(pieceOnBoard, y, i);
				foundPinner = true;
				break;
			}
			break;
		}
		if(foundPinner || foundHVP){
			//MOVING LEFT NEXT
			for(int i=x;i>=0;i--){
				if(i==x)continue;
				int pieceOnBoard = board.getPiece(y,i);
				if(pieceOnBoard==0) continue;
				if(foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)) {
					hvp = new Piece(pieceOnBoard, y, i);
					foundHVP = true;
					break;
				}
				if(foundHVP && -team*pieceOnBoard > BoardUtil.BISHOP) {
					pinner = new Piece(pieceOnBoard, y, i);
					foundPinner = true;
					break;
				}
				break;
			}
		}
		if(foundPinner && foundHVP && determineIfPieceCombinationDeterminePin(pinner.getValue(), pinnedPiece.getValue(), hvp.getValue(), false, isDefended(board, pinnedPiece.getValue(), y, x), canPawnReclaimPinner(board, pinnedPiece, pinner))) {
			return new AbsolutePin(pinner, pinnedPiece, hvp, false);
		}
		foundPinner = false;
		foundHVP = false;
		int j = x;
		//MOVING UPRIGHT FIRST
		for(int i=y;i<8;i++){
			if(i==y)continue;
			j++;
			if(i>7||j>7) break;
			int pieceOnBoard = board.getPiece(i,j);
			if(pieceOnBoard==0) continue;
			if(pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT){
				hvp = new Piece(pieceOnBoard, i, j);
				foundHVP = true;
				break;
			}
			if(pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP){
				pinner = new Piece(pieceOnBoard, i, j);
				foundPinner = true;
				break;
			}
			break;
		}
		if(foundPinner || foundHVP){
			j=x;
			//MOVING DOWNLEFT NEXT
			for(int i=y;i>=0;i--){
				if(i==y)continue;
				j--;
				if(i>7||j<0) break;
				int pieceOnBoard = board.getPiece(i,j);
				if(pieceOnBoard==0) continue;
				if(foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)){
					hvp = new Piece(pieceOnBoard, i, j);
					foundHVP = true;
					break;
				}
				if(foundHVP && (pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP)){
					pinner = new Piece(pieceOnBoard, i, j);
					foundPinner = true;
					break;
				}
				break;
			}
		}
		if(foundPinner && foundHVP && determineIfPieceCombinationDeterminePin(pinner.getValue(), pinnedPiece.getValue(), hvp.getValue(), true, isDefended(board, pinnedPiece.getValue(), y, x), canPawnReclaimPinner(board, pinnedPiece, pinner))) {
			return new AbsolutePin(pinner, pinnedPiece, hvp, true);
		}
		foundPinner = false;
		foundHVP = false;
		j=x;
		//MOVING UPLEFT FIRST
		for(int i=y;i<8;i++){
			if(i==y)continue;
			j--;
			if(i>7 || j<0)break;
			int pieceOnBoard = board.getPiece(i,j);
			if(pieceOnBoard==0)continue;
			if((pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT)){
				hvp = new Piece(pieceOnBoard, i, j);
				foundHVP = true;
				break;
			}
			if(pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP){
				pinner = new Piece(pieceOnBoard, i, j);
				foundPinner = true;
			}
			break;
		}
		if(foundPinner || foundHVP){
			j=x;
			//MOVING DOWNRIGHT NEXT
			for(int i=y;i>=0;i--){
				if(i==y)continue;
				j++;
				if(i<0 || j>7)break;
				int pieceOnBoard = board.getPiece(i,j);
				if(pieceOnBoard==0)continue;
				if((foundPinner && (pieceOnBoard == team*BoardUtil.KING || pieceOnBoard*team > BoardUtil.KNIGHT))){
					hvp = new Piece(pieceOnBoard, i, j);
					foundHVP = true;
					break;
				}
				if(foundHVP && (pieceOnBoard*team == -BoardUtil.QUEEN || pieceOnBoard*team == -BoardUtil.BISHOP)){
					pinner = new Piece(pieceOnBoard, i, j);
					foundPinner = true;
				}
				break;
			}
		}
		if(foundPinner && foundHVP && determineIfPieceCombinationDeterminePin(pinner.getValue(), pinnedPiece.getValue(), hvp.getValue(), true, isDefended(board, pinner.getValue(), pinner.getyPosition(), pinner.getxPosition()), canPawnReclaimPinner(board, pinnedPiece, pinner))) {
			return new AbsolutePin(pinner, pinnedPiece, hvp, true);
		}
		return null;
	}
	
	public static List<AbsolutePin> findAllPinnedPiecesFromAbsolutePin(Board board, int team){
		List<int[]> thisTeamPiecesThatCanPin = BoardUtil.getAllTeamPinPieces(board, team);
		List<AbsolutePin> pins = new ArrayList<AbsolutePin>();
		for(int[] pinPiece : thisTeamPiecesThatCanPin){
			if(isPieceThreatenedNew(board, new Piece(pinPiece))) continue;
			List<List<Move>> linesOfMoves = findVanillaLinesOfMovesForPiece(pinPiece);
			for(List<Move> lineOfMoves: linesOfMoves){
				boolean isDiag = determineDiags(lineOfMoves);
				boolean isFirstPieceFound = false;
				boolean isSecondPieceFound = false;
				int[] firstPieceFound = new int[3];
				for(Move move : lineOfMoves){
					int pieceOnBoard = board.getPiece(move.getToPos());
					if(pieceOnBoard == BoardUtil.SPACE) continue;
					/*
					 * Store the first piece found on the path
					 */
					if(pieceOnBoard * team < BoardUtil.SPACE && !isFirstPieceFound){
						isFirstPieceFound = true;
						firstPieceFound = new int[]{pieceOnBoard, move.getToPos()[0], move.getToPos()[1]};
						continue;
					}
					/*
					 * A piece was found but there is a piece of your own team in between. No pins can be made from it
					 */
					if(pieceOnBoard * team > BoardUtil.SPACE && !isSecondPieceFound){
						break;
					}
					
					/*
					 * Store the second piece found on the path
					 */
					if(pieceOnBoard * team < BoardUtil.SPACE && isFirstPieceFound && !isSecondPieceFound){
						boolean canPawnReclaimPinner = canPawnReclaimPinner(board, new Piece(firstPieceFound), new Piece(pinPiece));
						if(determineIfPieceCombinationDeterminePin(Math.abs(pinPiece[0]), Math.abs(firstPieceFound[0]), Math.abs(pieceOnBoard), isDiag, isDefended(board, move.getPieceMoving(), move.getFromPos()), canPawnReclaimPinner)){
							isSecondPieceFound = true;
							pins.add(new AbsolutePin(pinPiece, firstPieceFound, new int[]{pieceOnBoard, move.getToPos()[0], move.getToPos()[1]}, isDiag));
						}
						break;
					}
				}
			}
		}
		return pins;
	}
	
	public static boolean determineIfPieceCombinationDeterminePin(int pinningPiece, int pinnedPiece, int lastPiece, boolean isDiag, boolean isProtected, boolean canPawnReclaimPinner){
		pinningPiece = Math.abs(pinningPiece);
		pinnedPiece = Math.abs(pinnedPiece);
		lastPiece = Math.abs(lastPiece);
		if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.PAWN && lastPiece == BoardUtil.ROOK && !canPawnReclaimPinner){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.PAWN && lastPiece == BoardUtil.QUEEN && isDiag && !canPawnReclaimPinner){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.KNIGHT && lastPiece == BoardUtil.ROOK && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.KNIGHT && lastPiece == BoardUtil.QUEEN && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.KNIGHT && lastPiece == BoardUtil.KING && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.ROOK && lastPiece == BoardUtil.ROOK && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.ROOK && lastPiece == BoardUtil.QUEEN && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.ROOK && lastPiece == BoardUtil.KING && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.QUEEN && lastPiece == BoardUtil.ROOK && isProtected && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.BISHOP && pinnedPiece == BoardUtil.QUEEN && lastPiece == BoardUtil.KING && isProtected && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.PAWN && lastPiece == BoardUtil.QUEEN && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.PAWN && lastPiece == BoardUtil.KING && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.KNIGHT && lastPiece == BoardUtil.QUEEN && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.KNIGHT && lastPiece == BoardUtil.KING && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.BISHOP && lastPiece == BoardUtil.BISHOP && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.BISHOP && lastPiece == BoardUtil.QUEEN && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.BISHOP && lastPiece == BoardUtil.KING && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.ROOK && pinnedPiece == BoardUtil.QUEEN && lastPiece == BoardUtil.KING && isProtected && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.QUEEN && pinnedPiece == BoardUtil.PAWN && lastPiece == BoardUtil.ROOK && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.QUEEN && pinnedPiece == BoardUtil.KNIGHT && lastPiece == BoardUtil.KING){
			return true;
		}else if(pinningPiece == BoardUtil.QUEEN && pinnedPiece == BoardUtil.BISHOP && lastPiece == BoardUtil.BISHOP && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.QUEEN && pinnedPiece == BoardUtil.BISHOP && lastPiece == BoardUtil.KING && !isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.QUEEN && pinnedPiece == BoardUtil.ROOK && lastPiece == BoardUtil.ROOK && isDiag){
			return true;
		}else if(pinningPiece == BoardUtil.QUEEN && pinnedPiece == BoardUtil.ROOK && lastPiece == BoardUtil.KING && isDiag){
			return true;
		}
		return false;
	}
	
	
	
	public static int[] findPinnedPieceFromAbsolutePin(Board board, int pinPiece, int[] yx){
		return findPinnedPieceFromAbsolutePin(board, new int[]{pinPiece, yx[0], yx[1]});
	}
	public static int[] findPinnedPieceFromAbsolutePin(Board board, int[] pinPiece){
		int piece = pinPiece[0];
		int team = piece/Math.abs(piece);
		if(piece*team < BoardUtil.BISHOP) return new int[]{0,0,0};
		List<AbsolutePin> pins = new ArrayList<AbsolutePin>();
		if(isPieceThreatenedNew(board, new Piece(pinPiece))) return new int[]{0,0,0};
		List<List<Move>> linesOfMoves = findVanillaLinesOfMovesForPiece(pinPiece);
		for(List<Move> lineOfMoves: linesOfMoves){
			boolean isDiag = determineDiags(lineOfMoves);
			boolean isFirstPieceFound = false;
			boolean isSecondPieceFound = false;
			int[] firstPieceFound = new int[3];
			for(Move move : lineOfMoves){
				int pieceOnBoard = board.getPiece(move.getToPos());
				if(pieceOnBoard == BoardUtil.SPACE) continue;
				/*
				 * Store the first piece found on the path
				 */
				if(pieceOnBoard * team < BoardUtil.SPACE && !isFirstPieceFound){
					isFirstPieceFound = true;
					firstPieceFound = new int[]{pieceOnBoard, move.getToPos()[0], move.getToPos()[1]};
					continue;
				}
				/*
				 * A piece of your own team was found en route. No matter what pieces are after, no pins can be made.
				 * This assumes that movement started from original piece making the pin.
				 */
				if(pieceOnBoard * team > BoardUtil.SPACE){
					break;
				}
				
				/*
				 * Store the second piece found on the path
				 */
				if(pieceOnBoard * team < BoardUtil.SPACE && isFirstPieceFound && !isSecondPieceFound){
					boolean canPawnReclaimPinner = canPawnReclaimPinner(board, new Piece(firstPieceFound), new Piece(pinPiece));
					if(determineIfPieceCombinationDeterminePin(Math.abs(pinPiece[0]), Math.abs(firstPieceFound[0]), Math.abs(pieceOnBoard), isDiag, isDefended(board, pinPiece[0], pinPiece[1], pinPiece[2]), canPawnReclaimPinner)){
						isSecondPieceFound = true;
						pins.add(new AbsolutePin(pinPiece, firstPieceFound, new int[]{pieceOnBoard, move.getToPos()[0], move.getToPos()[1]}, isDiag));
					}
					break;
				}
			}
		}

		int[] bestPin = new int[]{BoardUtil.SPACE, BoardUtil.SPACE, BoardUtil.SPACE};
		for(AbsolutePin pin : pins){
			if(!isAbsolutePinValid(pin)) continue;
			if(Math.abs(pin.getPinnedPiece()[0]) > bestPin[0]){
				bestPin = pin.getPinnedPiece();
			}
		}
		return bestPin;
	}
	
	private static boolean canPawnReclaimPinner(Board board, Piece pawn, Piece pinner){
		if(Math.abs(pawn.getValue()) != BoardUtil.PAWN){
			return false;
		}
		int dy = pawn.getTeam();
		if(board.getPiece(pawn.getyPosition() + dy, pawn.getxPosition()-1) == pinner.getValue() && pinner.getyPosition() == pawn.getyPosition()+dy && pinner.getxPosition() == pawn.getxPosition()-1){
			Move captureOfPinByPawn = new Move(pawn.getValue(), pinner.getValue(), pawn.getyPosition(), pawn.getxPosition(), pinner.getyPosition(), pinner.getxPosition());
			return isMoveValid(board, captureOfPinByPawn);
		}else if(board.getPiece(pawn.getyPosition() + dy, pawn.getxPosition()+1) == pinner.getValue() && pinner.getyPosition() == pawn.getyPosition()+dy && pinner.getxPosition() == pawn.getxPosition()+1){
			Move captureOfPinByPawn = new Move(pawn.getValue(), pinner.getValue(), pawn.getyPosition(), pawn.getxPosition(), pinner.getyPosition(), pinner.getxPosition());
			return isMoveValid(board, captureOfPinByPawn);
		}
		return false;
	}
	
	private static boolean isAbsolutePinValid(AbsolutePin pin) {
		switch(Math.abs(pin.getPinningPiece()[0])){
		case BoardUtil.BISHOP:
			switch(Math.abs(pin.getPinnedPiece()[0])){
			case BoardUtil.QUEEN:
			case BoardUtil.ROOK:
			case BoardUtil.KNIGHT:
				return true;
			}
			return false;
		case BoardUtil.ROOK:
			switch(Math.abs(pin.getPinnedPiece()[0])){
			case BoardUtil.QUEEN:
			case BoardUtil.BISHOP:
			case BoardUtil.KNIGHT:
				return true;
			}
			return false;
		case BoardUtil.QUEEN:
			switch(Math.abs(pin.getPinnedPiece()[0])){
			case BoardUtil.ROOK:
				return pin.isDiag();
			case BoardUtil.BISHOP:
				return !pin.isDiag();
			case BoardUtil.KNIGHT:
				return true;
			}
		}
		return false;
	}
	private static boolean determineDiags(List<Move> lineOfMoves) {
		if(lineOfMoves.size() == 0) return false;
		int dy = Math.abs(lineOfMoves.get(0).getFromPos()[0] - lineOfMoves.get(0).getToPos()[0]);
		int dx = Math.abs(lineOfMoves.get(0).getFromPos()[1] - lineOfMoves.get(0).getToPos()[1]);
		return dy > 0 && dx > 0;
	}
	
	/*
	 * Team is the team that is causing the fork
	 */
	public static List<Piece> findAllForkPieces(Board board, Piece piece){
		List<Piece> allForkedPieces = new ArrayList<Piece>();
		if(isPieceThreatenedNew(board, piece)){
			return allForkedPieces;
		}
		List<Move> moves = findAllValidMoves(board, piece);
		int threats = 0;
		for(Move move : moves){
			if(move.getPieceCaptured() != 0){
				Board newBoard = move.updateBoard(board);
				if(
						(getValue(move.getPieceMoving()) < getValue(move.getPieceCaptured()))
					||  (!isPieceThreatenedNew(newBoard, new Piece(move.getPieceMoving(), move.getToPos())))
					||  (isCheck(newBoard))
						){
					threats++;
					allForkedPieces.add(new Piece(board, move.getToPos()));
				}
			}
		}
		if(threats < 2){
			return new ArrayList<Piece>();
		}
		return allForkedPieces;
	}
	
	public static int findPotentialGainFromForkAccurate(Board board, Piece forkingPiece, List<Piece> forkedPieces) {

		if(forkedPieces.size() < 2){
			return 0;
		}
		
		if(isPieceThreatenedNew(board, forkingPiece)){
			return 0;
		}
		
		//This assumes that the forking team is going again even though they just forked. Not right.
		
		List<Integer> allGainsFromFork = new ArrayList<Integer>();
		for(Piece forkedPiece : forkedPieces){
			if(Math.abs(forkedPiece.getValue()) == BoardUtil.KING){
				allGainsFromFork.add(getValue(BoardUtil.KING));
			}else{
				
				//The forking move, i.e. forking piece moving towards one of the forked pieces
				Move forkMove = new Move(forkingPiece, forkedPiece);
				Board boardAtTheFork = forkMove.updateBoard(board);
				/*
				 * The following line checks to see if by capturing one of the forked pieces, it will result in its own capture.
				 * This is necessary to determine an accurate scoring scheme i.e. free capture versus capture minus loss of piece.
				 * Without this correction, a high level piece would attempt to fork even though the piece is protected.  
				 */
				if(isPieceThreatenedNew(boardAtTheFork, forkMove.getPieceAfterMove())){
					allGainsFromFork.add(getValue(forkedPiece) - getValue(forkingPiece));
				}else{
					allGainsFromFork.add(getValue(forkedPiece));
				}					
				
				/*
				 * TODO: Determine if the one piece being forked can move to an area that would protect the other forked piece.
				 * In case this happens, there is no gain from the fork.
				 */
			}
		}
		Collections.sort(allGainsFromFork);
		/*
		 * The assumption is that the opponent will protect the most valuable scenario (not piece as a piece could be protected and thus not be as valuable as such)
		 * For example:
		 * ROOK vs QUEEN protected and ROOK not protected. Which one is better? ROOK unprotected of course?
		 */
		return allGainsFromFork.get(allGainsFromFork.size()-2);
	}
	
	private static final int MAX_DEPTH = 6;
	
	public static boolean isForcedMate(Board board, int team, boolean useHack){
		return isForcedMate(board, team, 0, useHack);
	}
	public static boolean isForcedMate(Board board, int team, int depth, boolean useHack){
		if(isCheckMate(board, -team))
			return true;
		if(MAX_DEPTH == depth)
			return false;
		if(!isCheck(board, -team))
			return false;

		List<Move> oppMoves = getAllMoveForTeamWithoutSuicides(board, -team);
		
		if(useHack){
			oppMoves = trimSenselessSacrifice(board, oppMoves);
		}
		
		if(oppMoves.size() > 1) {
			List<Move> trimmedMoves2 = trimAllMateInOne(board, oppMoves, -team);
			if(trimmedMoves2.size() == 0){
				return true;
			}else if(trimmedMoves2.size() == 1){
				oppMoves = trimmedMoves2;
			}else{
				return false;
			}
		}else if(oppMoves.size() == 0){
			return true;
		}
		Board newBoard = oppMoves.get(0).updateBoard(board);
		for(Move myMove : getAllMoveForTeam(newBoard, team))
			if(isForcedMate(myMove.updateBoard(newBoard), team, depth+1, useHack))
				return true;
		return false;
	}
	
	private static List<Move> trimAllMateInOne(Board board, List<Move> moves, int team) {
		List<Move> movesSansMi1 = new ArrayList<Move>();
		for(Move move : moves){
			if(!isHisNextMovePuttingMeToCheckMate(move.updateBoard(board), team)){
				movesSansMi1.add(move);
			}
		}
		return movesSansMi1;
	}
	
	public static boolean isMyNextMovePuttingHimToCheckMate(Board board, int team){
		List<Piece> hisPieces = BoardUtil.getAllTeamPieces(board, -team);
		List<Move> allHisMoves = new ArrayList<Move>();
		for(Piece hisPiece : hisPieces){
			allHisMoves.addAll(findAllValidMoves(board, hisPiece));
		}

		//Check for stale mate.
		if(allHisMoves.size() == 0 && !isCheck(board, -team)){
			return false;
		}
		
		for(Move theirMove : allHisMoves){
			Board newBoard = theirMove.updateBoard(board);
			List<Piece> myPieces = BoardUtil.getAllTeamPieces(newBoard, team);
			List<Move> myMoves = new ArrayList<Move>();
			for(Piece myPiece : myPieces){
				myMoves.addAll(findAllValidMoves(newBoard, myPiece));
			}
			
			boolean doesThisMoveLeadToCheckMate = false;
			for(Move myMove : myMoves){
				Board newNewBoard = myMove.updateBoard(newBoard);
				if(isCheckMate(newNewBoard, -team)){
					doesThisMoveLeadToCheckMate = true;
					break;
				}
			}
			
			if(!doesThisMoveLeadToCheckMate){
				/*
				 * We have found one of his move that does not lead to check mate. So it is false that our original move leads to check mate
				 * No need to check his other moves.
				 */
				
				return false;
			}
		}
		/*
		 * We iterated through all his moves and they all lead to check mate.
		 */
		return true;
	}
	
	public static boolean isHisNextMovePuttingMeToCheckMate(Board board, int team){
		for(Move move : getAllMoveForTeam(board, -team)){
			Board newBoard = move.updateBoard(board);
			if(isCheckMate(newBoard, team)){
				return true;
			}
		}
		return false;
	}
		
	/*
	 * Is there a move from the opponent where no matter what my next move is, the opponent will find at least one way to check mate.
	 * In other words, there exists one move in the set of his moves where for all moves in the set of my moves, there exists a move in the set of his moves that leads to check mate. 
	 * In other words, Opponent has 10 moves, Each of these moves leads me to play 3 moves. Move 7 for example, results in all my moves to be checkmated. But if for all of the opponent's 10 moves, I found a way out, then it's false.
	 */
	//TODO:
	public static boolean isHisNextNextMovePuttingMeToCheckMate(Board board, int team){
		List<Piece> allHisPieces = BoardUtil.getAllTeamPieces(board, -team);
		List<Move> allHisMoves = new ArrayList<Move>();
		for(Piece hisPiece : allHisPieces){
			allHisMoves.addAll(findAllValidMovesThatLeadToCheckMate(board, hisPiece));
		}
		int iterator = 0;
		for(Move hisMove : allHisMoves){
			Board newBoard = hisMove.updateBoard(board);
			List<Piece> myPieces = BoardUtil.getAllTeamPieces(newBoard, team);
			List<Move> myMoves = new ArrayList<Move>();
			for(Piece myPiece : myPieces){
				myMoves.addAll(findAllValidMovesThatLeadToCheckMate(newBoard, myPiece));
			}
			if(myMoves.size() == 0 && !isCheck(newBoard, team)){
				continue;
			}
			boolean isHisMoveResultingInCheckMate = true;
			for(Move myMove : myMoves){
				Board newNewBoard = myMove.updateBoard(newBoard);
				List<Piece> allHisNewPieces = BoardUtil.getAllTeamPieces(newNewBoard, -team);
				List<Move> allHisNextMoves = new ArrayList<Move>();
				for(Piece hisNewPiece : allHisNewPieces){
					allHisNextMoves.addAll(findAllValidMovesThatLeadToCheckMate(newNewBoard, hisNewPiece));
				}
				boolean isCheckMate = false;
				for(Move hisNextMove : allHisNextMoves){
					iterator++;
					if(iterator > MATE_ITERATOR_THRESHOLD*3000) {
						return false;
					}
					Board newNewNewBoard = hisNextMove.updateBoard(newNewBoard);
					if(isCheckMate(newNewNewBoard, team)){
						isCheckMate = true;
						break;
					}
				}
				if(!isCheckMate) isHisMoveResultingInCheckMate = false;
			}
			if(isHisMoveResultingInCheckMate) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Number set according to Kermur Sire De Legal vs. St. Brie mate in 1750
	 * ^ Number readjusted according to Mate No. 8 (The Art of Check Mate pg. 119)
	 */
//	private static final int MATE_ITERATOR_THRESHOLD = 360;
	private static final int MATE_ITERATOR_THRESHOLD = 3600;
	

	public static List<Move> trimSenselessSacrifice(Board board, List<Move> hisMovesAfterMyMove) {
		List<Move> moves = new ArrayList<Move>();
		for(Move move : hisMovesAfterMyMove){
			if(!isSacrificeSenseless(board, move)){
				moves.add(move);
			}
		}

		if(moves.size() == 0 && hisMovesAfterMyMove.size() > 0) {
			moves.add(hisMovesAfterMyMove.get(0));
		}
		return moves;
	}
	
	public static boolean isSacrificeSenseless(Board board, Move move){
		int team = move.getPieceMoving() > 0 ? 1 : -1;
		if(!isCheck(board, team)){
			return false;
		}
		return isPieceThreatenedNew(move.updateBoard(board), move.getPieceAfterMove());
	}
}