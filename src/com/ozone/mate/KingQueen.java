package com.ozone.mate;

import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.movements.MoveUtil;

public class KingQueen {
	
	public static Move findMove(Board board, int team){
		int[] whiteKing = PieceFinder.findKing(board, team);
		int[] whiteQueen = PieceFinder.findQueen(board, team);
		int[] blackKing = PieceFinder.findKing(board, -team);
		
		List<Move> queenMoves = MoveUtil.findAllValidMoves(board, whiteQueen);
		List<Move> kingMoves = MoveUtil.findAllValidMoves(board, whiteKing);
		if(isKingLockedInBackRowByKing(blackKing, whiteKing)){
			for(Move queenMove : queenMoves){
				if(MoveUtil.isCheckMate(queenMove.updateBoard(board), -team)){
					return queenMove;
				}
			}
		}
		
		if(isKingLockedInBackRowByQueen(blackKing, whiteQueen)){
			double minDistance = calculateDistance(blackKing, whiteKing);
			if(minDistance < 3.7){
				for(Move queenMove : queenMoves){
					if(MoveUtil.isCheckMate(queenMove.updateBoard(board), -team)){
						return queenMove;
					}
				}
			}
			Move kingMove = null;
			for(Move validMove : kingMoves){
				if(validMove.getToPos()[1]==0 || validMove.getToPos()[0] == 0 || validMove.getToPos()[0] == 7 || validMove.getToPos()[1] == 7){
					continue;
				}
				int[] piece = new int[]{validMove.getPieceMoving(), validMove.getToPos()[0], validMove.getToPos()[1]}; 
				double distance = calculateDistance(piece, blackKing) + 0.01;
				if(distance < minDistance){
					minDistance = distance;
					kingMove = validMove;
				}
			}
			if(kingMove != null){
				return kingMove;
			}
			for(Move queenMove : queenMoves){
				if(MoveUtil.isCheckMate(queenMove.updateBoard(board), -team)){
					return queenMove;
				}
			}
			return kingMoves.get(0);

		}
		if(Math.abs(whiteQueen[1]-blackKing[1]) != 1 && Math.abs(whiteQueen[2]-blackKing[2]) != 1){
			int minArea = calculateArea(blackKing, whiteKing);
			Move move = null;
			for(Move validMove : queenMoves){
				Piece piece = new Piece(validMove.getPieceMoving(), validMove.getToPos()[0], validMove.getToPos()[1]);
				if(MoveUtil.isPieceThreatenedNew(validMove.updateBoard(board), piece) || MoveUtil.isStaleMate(validMove.updateBoard(board), team)){
					continue;
				}
				int area = calculateArea(blackKing, piece.toIntArray());
				if((Math.abs(validMove.getToPos()[0]-blackKing[1])==1 || Math.abs(validMove.getToPos()[1]-blackKing[2])==1) && area < minArea){
					minArea = area;
					move = validMove;
				}
			}
			if(move != null){
				return move;
			}
		}

		double minDistance = calculateDistance(blackKing, whiteKing) + 0.1;
		Move kingMove = null;
		for(Move validMove : kingMoves){
			if(validMove.getToPos()[1]==0 || validMove.getToPos()[0] == 0 || validMove.getToPos()[0] == 7 || validMove.getToPos()[1] == 7){
				continue;
			}
			int[] piece = new int[]{validMove.getPieceMoving(), validMove.getToPos()[0], validMove.getToPos()[1]}; 
			double distance = calculateDistance(piece, blackKing);
			if(distance < minDistance){
				minDistance = distance;
				kingMove = validMove;
			}
		}
		if(kingMove != null){
			return kingMove;
		}
		for(Move move : queenMoves){
			if(MoveUtil.isCheckMate(move.updateBoard(board), -team)){
				return move;
			}
		}
		for(Move move : queenMoves){
			if(!MoveUtil.isThreatenedVeryNaiveApproach(move.updateBoard(board), move.getPieceMoving(), move.getToPos()[0], move.getToPos()[1])){
				if(!MoveUtil.isStaleMate(move.updateBoard(board), -team)){
					return move;
				}
			}
		}
		if(kingMoves.size() == 0){
			return queenMoves.get((int)Math.floor(Math.random()*queenMoves.size()));
		}

		return kingMoves.get((int)Math.floor(Math.random()*kingMoves.size()));

	}
	
	private static boolean isKingLockedInBackRowByQueen(int[] king, int[] queen) {
		if(king[1] == 0 && queen[1] == 1){
			return true;
		}else if(king[1] == 7 && queen[1] == 6){
			return true;
		}else if(king[2] == 0 && queen[2] == 1){
			return true;
		}else if(king[2] == 7 && queen[2] == 6){
			return true;
		}
		return false;
	}

	private static boolean isKingLockedInBackRowByKing(int[] king, int[] king2) {
		if(king[2] == king2[2] && king[1]==7 && king2[1]==5){
			return true;
		}else if(king[2] == king2[2] && king[1]==0 && king2[1]==2){
			return true;
		}else if(king[1] == king2[1] && king[2]==7 && king2[2]==5){
			return true;
		}else if(king[1] == king2[1] && king[2]==0 && king2[2]==2){
			return true;
		}
		return false;
	}

	private static double calculateDistance(int[] piece1, int[] piece2){
		return Math.sqrt((piece1[1]-piece2[1])*(piece1[1]-piece2[1]) + (piece1[2]-piece2[2])*(piece1[2]-piece2[2]));
	}
	private static int calculateArea(int[] king, int[] queen){
		if(king[0] > queen[0]){
			if(king[1] > queen[1]){
				 return (8-queen[1])*(8-queen[2]); 
			}else{
				return queen[1]*(8-queen[2]);
			}
		}else if(king[1] > queen[1]){
			return (8-queen[1])*(queen[2]);
		}else{
			return queen[1]*queen[2];
		}
	}
}