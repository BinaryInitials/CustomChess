package com.ozone.mate;

import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Piece;
import com.ozone.movements.BoardUtil;

public class PieceFinder {
	
	public static Piece findKingPiece(Board board, int team){
		if(team > 0){
			for(int i=0;i<8;i++){
				for(int j=0;j<8;j++){
					if(board.getPiece(i,j) == BoardUtil.KING){
						return new Piece(BoardUtil.KING, i, j);
					}
				}
			}
		}else if(team < 0){
			for(int i=7;i>-1;i--){
				for(int j=0;j<8;j++){
					if(board.getPiece(i,j) == -BoardUtil.KING){
						return new Piece(-BoardUtil.KING, i, j);
					}
				}
			}
		}
		return null;
	}
	
	public static int[] findKing(Board board, int team){
		
		if(team > 0){
			for(int i=0;i<8;i++){
				for(int j=0;j<8;j++){
					if(board.getPiece(i,j) == BoardUtil.KING){
						return new int[]{BoardUtil.KING, i, j};
					}
				}
			}
		}else if(team < 0){
			for(int i=7;i>-1;i--){
				for(int j=0;j<8;j++){
					if(board.getPiece(i,j) == -BoardUtil.KING){
						return new int[]{-BoardUtil.KING, i, j};
					}
				}
			}
		}
		
		return new int[]{};
	}

	public static int[] findQueen(List<int[]> pieces){
		for(int[] piece : pieces){
			if(Math.abs(piece[0]) == BoardUtil.QUEEN){
				return piece;
			}
		}
		return new int[]{};
	}
	public static int[] findQueen(Board board, int team){
		for(int i=0;i<BoardUtil.MAX_ROW;i++){
			for(int j=0;j<BoardUtil.MAX_COL;j++){
				if(board.getPiece(i,j)*team == BoardUtil.QUEEN){
					return new int[]{board.getPiece(i,j), i, j};
				}
			}
		}
		return new int[]{};
	}
}
