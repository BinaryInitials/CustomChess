package com.ozone.common;

import static com.ozone.movements.BoardUtil.BISHOP;
import static com.ozone.movements.BoardUtil.KING;
import static com.ozone.movements.BoardUtil.KNIGHT;
import static com.ozone.movements.BoardUtil.MAX_COL;
import static com.ozone.movements.BoardUtil.MAX_ROW;
import static com.ozone.movements.BoardUtil.PAWN;
import static com.ozone.movements.BoardUtil.QUEEN;
import static com.ozone.movements.BoardUtil.ROOK;
import static com.ozone.movements.BoardUtil.SPACE;
import static com.ozone.movements.BoardUtil.isPositionOutOfBounds;

import java.util.Arrays;

import com.ozone.movements.MoveUtil;
import com.ozone.utility.Converters;


public class Board {

	
	private int[] board;
	
	public Board(String fen){
		board = Converters.fenToBoard(fen).getBoard();
	}
	
	public Board() {
		board = new int[64];
	};
	
	public Board(int[] board) {
		this.board = new int[64];
		System.arraycopy(board, 0, this.board, 0, 64);
	}
	
	public void setBoard(int[] board){
		System.arraycopy(board, 0, this.board, 0, 64);
	}
	
	public int[] getBoard(){
		return board;
	}
	
	public int getPiece(String square) {
		return getPiece(Common.convertInputToSquare(square));
	}
	public int getPiece(int[] yx) {
		return getPiece(yx[0], yx[1]);
	}
	public int getPiece(int i) {
		if(isPositionOutOfBounds(i/8,i%8)) return SPACE;
		return board[i];
	}
	public int getPiece(int y, int x) {
		if(isPositionOutOfBounds(y, x)) return SPACE;
		return board[y*8+x];
	}
	
	public boolean movePiece(String square){
		return movePiece(Common.convertInputToMove(this, square));
	}
	
	public boolean movePiece(Move move) {
		return movePiece(move.getPieceMoving(), move.getFromPos()[0], move.getFromPos()[1], move.getToPos()[0], move.getToPos()[1]);
	}

	
	public boolean movePieceSimple(Move move) {
		return movePieceSimple(move.getPieceMoving(), move.getFromPos()[0], move.getFromPos()[1], move.getToPos()[0], move.getToPos()[1]);
	}

	public boolean movePieceSimple(int piece, int y1, int x1, int y2, int x2){
		int team = piece > 0 ? 1 : -1;
		if(getPiece(y1,x1)*team == KING && Math.abs(x1-x2) == 2) {
			setPiece(team*ROOK, y2, (x1+x2)/2);
			if(Math.abs(x2 - 7) < Math.abs(x2-0)){
				setPiece(SPACE, y2, 7);
			}else{
				setPiece(SPACE, y2, 0);
			}
		}
		if((piece == PAWN && y2 == 7) || (piece == -PAWN && y2 == 0)){
			return setPiece((team*QUEEN), y2, x2) && setPiece(0, y1, x1); 
		}
		return setPiece(piece, y2, x2) && setPiece(0, y1, x1);
	}
	
	public boolean movePiece(int piece, int y1, int x1, int y2, int x2){
		if(y1==-1 && x1==-1 && y2==-1 && x2==-1){
			return false;
		}
		if(getPiece(y1, x1) == SPACE){
			return false;
		}
		if(getPiece(y1, x1) != piece){
			return false;
		}
		if(y1==y2 && x1==x2){
			return false;
		}
		if(Math.abs(getPiece(y2,x2)) == KING){
			return false;
		}
		int team = piece/Math.abs(piece);
		if(MoveUtil.conditionsForCastling(new Board(board), team, y1, x1, y2, x2)){
			setPiece(team*ROOK, y2, (x1+x2)/2);
			if(Math.abs(x2 - 7) < Math.abs(x2-0)){
				setPiece(SPACE, y2, 7);
			}else{
				setPiece(SPACE, y2, 0);
			}
		}
		//En Passant
		if(piece == PAWN && y1 == 4 && y2 == 5 && Math.abs(x1-x2) == 1 && board[y1*8+x2] == -PAWN){
			setPiece(SPACE, y1, x2);
		}else if(piece == -PAWN && y1 == 3 && y2 == 2 && Math.abs(x1-x2) == 1 && board[y1*8+x2] == PAWN){
			setPiece(SPACE, y1, x2);
		}

		//Queen promotion
		if((piece == PAWN && y2 == 7) || (piece == -PAWN && y2 == 0)){
			return setPiece((piece*QUEEN)/PAWN, y2, x2) && setPiece(0, y1, x1); 
		}
		return setPiece(piece, y2, x2) && setPiece(0, y1, x1);
	}
	
	public boolean setPiece(Piece piece){
		return setPiece(piece.getValue(), piece.getyPosition(), piece.getxPosition());
	}
	
	public boolean setPiece(int[] piece) {
		return setPiece(piece[0], piece[1], piece[2]);
	}
	public boolean setPiece(int piece, int[] yx) {
		return setPiece(piece, yx[0], yx[1]);
	}
	public boolean setPiece(int piece, int y, int x) {
		if(isPositionOutOfBounds(y, x)) return false;
		board[y*8+x] = piece;
		return true;
	}
	
	public boolean setPiece(int piece, Square square){
		return setPiece(piece, square.getY(), square.getX());
	}
	public boolean setPiece(int piece, String squareInput){
		int[] square = Common.convertInputToSquare(squareInput);
		return setPiece(piece, square[0], square[1]);
	}
	
	public boolean setPiece(String pieceInput, String squareInput){
		int[] square = Common.convertInputToSquare(squareInput);
		int piece = Common.convertPieceTxtToPiece(pieceInput);
		return setPiece(piece, square[0], square[1]);
	}
		
	public void reset() {
		board = new int[MAX_ROW*MAX_COL];
		for(int i=0;i<MAX_COL;i++){
			setPiece(PAWN, 1,i);
			setPiece(-PAWN, 6,i);
		}
		setPiece(ROOK, 0, 0);
		setPiece(ROOK, 0, 7);
		setPiece(-ROOK, 7, 0);
		setPiece(-ROOK, 7, 7);
		
		setPiece(KNIGHT, 0, 1);
		setPiece(KNIGHT, 0, 6);
		setPiece(-KNIGHT, 7, 1);
		setPiece(-KNIGHT, 7, 6);
		
		setPiece(BISHOP, 0, 2);
		setPiece(BISHOP, 0, 5);
		setPiece(-BISHOP, 7, 2);
		setPiece(-BISHOP, 7, 5);

		setPiece(QUEEN, 0, 3);
		setPiece(KING, 0, 4);
		setPiece(-QUEEN, 7, 3);
		setPiece(-KING, 7, 4);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(board);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Board other = (Board) obj;
		if (!Arrays.equals(board, other.board))
			return false;
		return true;
	}
}