package com.ozone.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.mate.PieceFinder;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;
import com.ozone.utility.Converters;

public class PuzzleGeneration {
	
	public static void main(String[] args){
		int mateInX = 3;
		generatePuzzle(mateInX);
		
	}

	public static void generatePuzzle(int mateInX){
		String fen = "";
		if(mateInX==1){
			fen = generatePuzzleMateInOne();
		}else if(mateInX==2){
			fen = generatePuzzleMateInTwo();
		}else if(mateInX==3){
			fen = generatePuzzleMateInThree();
		}
		Board board = Converters.fenToBoard(fen);
		int team = fen.contains(" w ") ? 1:-1;
		Move move = findSolution(board, team, mateInX);
		System.out.println("Solutions: " + move);
	}
	
	public static Move findSolution(Board board, int team, int mateInX) {
		if(mateInX == 1){
			for(Move move : MoveUtil.getAllMoveForTeam(board, team)){
				if(MoveUtil.isCheckMate(move.updateBoard(board), -team)){
					return move;
				}
			}
		}else if(mateInX == 2){
			for(Move move : MoveUtil.getAllMoveForTeam(board, team)){
				Board newBoard = move.updateBoard(board);
				boolean doAllHisMoveLeadToMate = true;
				List<Move> oppMoves = MoveUtil.getAllMoveForTeam(newBoard, -team);
				
				//Removing moves that lead to stalemate
				if(oppMoves.size()==0) continue;
				
				for(Move oppMove : oppMoves){
					Board newNewBoard = oppMove.updateBoard(newBoard);
					boolean doesOneOfMyMoveLeadToMate = false;
					for(Move move2 : MoveUtil.getAllMoveForTeam(newNewBoard, team)){
						if(MoveUtil.isCheckMate(move2.updateBoard(newNewBoard), -team)){
							doesOneOfMyMoveLeadToMate = true;
							break;
						}
					}
					if(!doesOneOfMyMoveLeadToMate){
						doAllHisMoveLeadToMate = false;
						break;
					}
				}
				if(doAllHisMoveLeadToMate){
					return move;
				}
			}
		}
		return null;
	}
	
	
	public static String generatePuzzleMateInOne() {
		Board board = new Board();
		int whoMatesInOne = 0;
		while(whoMatesInOne == 0){
			board = generateRandomBoard();
			whoMatesInOne = isNextMoveMate(board);
		}
		return Converters.boardToFen(board, whoMatesInOne==1);
	}
	public static String generatePuzzleMateInTwo() {
		Board board = new Board();
		int whoMatesInOne = 0;
		int whoMatesInTwo = 0;
//		while(board != null){
		while(whoMatesInTwo == 0){
			board = generateRandomBoard();
			whoMatesInOne = isNextMoveMate(board);
			whoMatesInTwo = isNextNextMoveMate(board);
			if(whoMatesInOne != 0){
				whoMatesInTwo = 0;
			}
		}
		return Converters.boardToFen(board, whoMatesInTwo==1);
	}

	public static String generatePuzzleMateInThree() {
		Board board = new Board();
		int whoMatesInOne = 0;
		int whoMatesInTwo = 0;
		int whoMatesInThree = 0;
		while(whoMatesInThree == 0){
			board = generateRandomBoard();
			whoMatesInOne = isNextMoveMate(board);
			whoMatesInTwo = isNextNextMoveMate(board);
			whoMatesInThree = isNextNextNextMoveMate(board);
			if(whoMatesInOne != 0 || whoMatesInTwo != 0){
				whoMatesInThree = 0;
			}
		}
		return Converters.boardToFen(board, whoMatesInThree==1);
	}

	private static Board generateRandomBoard(){
		Board board = new Board();
		List<String> filledPositions = new ArrayList<String>();
		List<Integer> allPieces = fillPieces();
		
		Collections.shuffle(allPieces);
		Collections.shuffle(allPieces);
		Collections.shuffle(allPieces);
		Collections.shuffle(allPieces);
		
		
		int[] yxWhite = new int[]{generateRandomChessPosition(), generateRandomChessPosition()};
		if(yxWhite[0] == 7) yxWhite[0] = 6;
		board.setPiece(BoardUtil.KING, yxWhite);
		int[] yxBlack = yxWhite;
		while(areKingsNeighbors(yxBlack, yxWhite)||isWhiteKingAboveBlackKing(yxWhite, yxBlack)){
			yxBlack = new int[]{generateRandomChessPosition(), generateRandomChessPosition()};
		}
		board.setPiece(-BoardUtil.KING, yxBlack);
		filledPositions.add(Common.convertIntArrayToSquare(yxWhite));
		filledPositions.add(Common.convertIntArrayToSquare(yxBlack));
		
		int pieces = (int)Math.floor(3+Math.random()*7);
		for(int i=0;i<pieces;i++){
			int[] newPiece = pieceGenerator(board, filledPositions, allPieces.get(i));
			if(newPiece == null) break;
			board.setPiece(newPiece);
			filledPositions.add(Common.convertIntArrayToSquare(new int[]{newPiece[1], newPiece[2]}));
		}
//		board.displayBoard();
		return board;
	}
	
	private static List<Integer> fillPieces() {
		List<Integer> pieces = new ArrayList<Integer>();
		int team = 1;
		for(int j=0;j<2;j++){
			for(int i=0;i<8;i++){
				pieces.add(team*BoardUtil.PAWN);
			}
			team=-1*team;
		}
		for(int j=0;j<2;j++){
			for(int i=0;i<2;i++){
				pieces.add(team*BoardUtil.BISHOP);
				pieces.add(team*BoardUtil.KNIGHT);
				pieces.add(team*BoardUtil.ROOK);
			}
			pieces.add(team*BoardUtil.QUEEN);
			team=-1*team;
		}
		
		return pieces;
	}

	public static int[] pieceGenerator(Board board, List<String> filledPositions, int pieceType){
		int team = pieceType>0?1:-1;
		boolean isPawn = team*pieceType == BoardUtil.PAWN;
		int[] piecePos = new int[]{generateRandomChessPosition(isPawn), generateRandomChessPosition()};
		Board newBoard = new Board(board.getBoard());
		newBoard.setPiece(pieceType, piecePos);
		int attempts = 0;
		while(filledPositions.contains(Common.convertIntArrayToSquare(piecePos)) || (PieceFinder.findKing(newBoard, -team).length > 0 && MoveUtil.isCheck(newBoard, -team))){
			newBoard.setPiece(BoardUtil.SPACE, piecePos);
			piecePos = new int[]{generateRandomChessPosition(isPawn), generateRandomChessPosition()};
			newBoard.setPiece(pieceType, piecePos);
			attempts++;
			if(attempts>10) return null;
		}
		return new int[]{pieceType, piecePos[0], piecePos[1]};
	}
	
	public static boolean isWhiteKingAboveBlackKing(int[] yxWhite, int[] yxBlack) {
		return yxWhite[0] >= yxBlack[0];
	}
	
	public static boolean areKingsNeighbors(int[] p1, int[] p2){
		return (p1[0]-p2[0])*(p1[0]-p2[0]) + (p1[1]-p2[1])*(p1[1]-p2[1]) < 3;  
	}
	
	private static int generateRandomChessPosition() {
		return generateRandomChessPosition(false);
	}
	private static int generateRandomChessPosition(boolean isPawn) {
		if(isPawn){
			return (int)Math.floor(Math.random()*3.99999)+2;
		}
		return (int)Math.floor(Math.random()*7.99999);
	}
	
	public static int isNextNextNextMoveMate(Board board){
		int team = 1;
		for(int i=0;i<2;i++){
			
			for(Move myMove1 : MoveUtil.getAllMoveForTeam(board, team)){
				Board newBoard = myMove1.updateBoard(board);
				
				List<Move> oppMoves = MoveUtil.getAllMoveForTeam(newBoard, -team);
				if(oppMoves.size()==0) continue;
				if(MoveUtil.isCheckMate(newBoard, -team)) break;
				
				boolean doAllOppMoveTurnIntoMate = true;
				for(Move oppMove1: oppMoves){
					Board newNewBoard = oppMove1.updateBoard(newBoard);
					boolean doesOneMyMove2LeadToAnyOppMove2ToMate = false;
					for(Move myMove2 : MoveUtil.getAllMoveForTeam(newNewBoard, team)){
						
						List<Move> oppMoves2 = MoveUtil.getAllMoveForTeam(myMove2.updateBoard(newNewBoard), -team);
						if(oppMoves2.size()==0) continue;
						if(MoveUtil.isCheckMate(newNewBoard, -team)) break;
						
						boolean doAllOppMove2TurnIntoMate = false;
						for(Move oppMove2 : oppMoves2){
							Board newNewNewBoard = oppMove2.updateBoard(newNewBoard);
							boolean isMateInThree = false;
							for(Move myMove3 : MoveUtil.getAllMoveForTeam(newNewNewBoard, team)){
								if(MoveUtil.isCheckMate(myMove3.updateBoard(newNewNewBoard), -team)){
									isMateInThree = true;
									break;
								}
							}
							if(!isMateInThree){
								doAllOppMove2TurnIntoMate = false;
								break;
							}
						}
						if(doAllOppMove2TurnIntoMate){
							doesOneMyMove2LeadToAnyOppMove2ToMate = true;
						}
					}
					
					if(!doesOneMyMove2LeadToAnyOppMove2ToMate){
						doAllOppMoveTurnIntoMate = false;
						break;
					}
				}
				if(doAllOppMoveTurnIntoMate){
					System.out.println(myMove1);
					return team;
				}
				
			}
			team=-1*team;
		}
		return 0;
	}
	
	public static int isNextNextMoveMate(Board board){
		int team = 1;
		for(int i=0;i<2;i++){
			for(Move myMove1 : MoveUtil.getAllMoveForTeam(board, team)){
				Board newBoard = myMove1.updateBoard(board);
				
				List<Move> oppMoves = MoveUtil.getAllMoveForTeam(newBoard, -team);
				if(oppMoves.size()==0) continue;
				if(MoveUtil.isCheckMate(newBoard, -team)) break;
				
				boolean doAllOppMoveTurnIntoMate = true;
				for(Move oppMove1: oppMoves){
					Board newNewBoard = oppMove1.updateBoard(newBoard);
					boolean isMateInTwo = false;
					for(Move myMove2 : MoveUtil.getAllMoveForTeam(newNewBoard, team)){
						if(MoveUtil.isCheckMate(myMove2.updateBoard(newNewBoard), -team)){
							isMateInTwo = true;
							break;
						}
					}
					if(!isMateInTwo){
						doAllOppMoveTurnIntoMate = false;
						break;
					}
				}
				if(doAllOppMoveTurnIntoMate){
					System.out.println(myMove1);
					return team;
				}
				
			}
			team=-1*team;
		}
		return 0;
	}
	
	public static int isNextMoveMate(Board board){
		int team = 1;
		for(int i=0;i<2;i++){
			List<Move> moves = MoveUtil.getAllMoveForTeam(board, team);
			for(Move move : moves){
				Board newBoard = move.updateBoard(board);
				if(MoveUtil.isCheckMate(newBoard, -team)){
					return team;
				}
			}
			team=-1*team;
		}
		return 0;
	}
}