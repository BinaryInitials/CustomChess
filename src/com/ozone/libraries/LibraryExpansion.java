package com.ozone.libraries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.movements.BoardUtil;
import com.ozone.utility.Converters;


public class LibraryExpansion {
	
	private static final String COMMAND_LINE = "\'|grep bestmove | sed \'s/.*bestmove.\\(.*\\) ponder.*/\\1/g\' | sort | uniq -c | sort -nr";
	private static final String[] FIRST_MOVES = {"E2E4", "D2D4", "B1C3", "G1F3", "E2E3"};
	private static HashMap<String, Set<Move>> openings = new HashMap<String, Set<Move>>();
	
	public static void main(String[] args) throws IOException{
		Writer writer = new OutputStreamWriter(new FileOutputStream("openings.txt"));
		Board originalBoard = new Board();
		originalBoard.reset();
		Set<Move> firstMoves = new HashSet<Move>();  
		for(String firstMove : FIRST_MOVES){
			firstMoves.add(Common.convertInputToMove(originalBoard, firstMove));
		}
		openings.put(Converters.boardToFenSimplified(originalBoard), firstMoves);
		int j=0;
		Date tic = new Date();
		for(String firstMove : FIRST_MOVES){
			Move whiteMove1 = Common.convertInputToMove(originalBoard, firstMove);
			Board boardAfterWhiteMove1 = whiteMove1.updateBoard(originalBoard);
			Set<Move> blackMoves1 = getComputerMoves(boardAfterWhiteMove1, BoardUtil.BLACK);
			addMovesToOpeningLibrary(boardAfterWhiteMove1, blackMoves1);
			for(Move blackMove1 : blackMoves1){
				Board boardAfterBlackMove1 = blackMove1.updateBoard(boardAfterWhiteMove1);
				Set<Move> whiteMoves2 = getComputerMoves(boardAfterBlackMove1, BoardUtil.WHITE);
				addMovesToOpeningLibrary(boardAfterBlackMove1, whiteMoves2);
				for(Move whiteMove2 : whiteMoves2){
					Board boardAfterWhiteMove2 = whiteMove2.updateBoard(boardAfterBlackMove1);
					Set<Move> blackMoves2 = getComputerMoves(boardAfterWhiteMove2, BoardUtil.BLACK);
					addMovesToOpeningLibrary(boardAfterWhiteMove2, blackMoves2);
					for(Move blackMove2 : blackMoves2){
						Board boardAfterBlackMove2 = blackMove2.updateBoard(boardAfterWhiteMove2);
						Set<Move> whiteMoves3 = getComputerMoves(boardAfterBlackMove2, BoardUtil.WHITE);
						addMovesToOpeningLibrary(boardAfterBlackMove2, whiteMoves3);
						for(Move whiteMove3 : whiteMoves3){
							Board boardAfterWhiteMove3 = whiteMove3.updateBoard(boardAfterBlackMove2);
							Set<Move> blackMoves3 = getComputerMoves(boardAfterWhiteMove3, BoardUtil.BLACK);
							addMovesToOpeningLibrary(boardAfterWhiteMove3, blackMoves3);
							for(Move blackMove3 : blackMoves3){
								Board boardAfterBlackMove3 = blackMove3.updateBoard(boardAfterWhiteMove3);
								Set<Move> whiteMoves4 = getComputerMoves(boardAfterBlackMove3, BoardUtil.WHITE);
								addMovesToOpeningLibrary(boardAfterBlackMove3, whiteMoves4);
								for(Move whiteMove4 : whiteMoves4){
									Board boardAfterWhiteMove4 = whiteMove4.updateBoard(boardAfterBlackMove3);
									Set<Move> blackMoves4 = getComputerMoves(boardAfterWhiteMove4, BoardUtil.BLACK);
									addMovesToOpeningLibrary(boardAfterWhiteMove4, blackMoves4);
									for(Move blackMove4 : blackMoves4){
										Board boardAfterBlackMove4 = blackMove4.updateBoard(boardAfterWhiteMove4);
										Set<Move> whiteMoves5 = getComputerMoves(boardAfterBlackMove4, BoardUtil.WHITE);
										addMovesToOpeningLibrary(boardAfterBlackMove4, whiteMoves5);
										for(Move whiteMove5 : whiteMoves5){
											Board boardAfterWhiteMove5 = whiteMove5.updateBoard(boardAfterBlackMove4);
											Set<Move> blackMoves5 = getComputerMoves(boardAfterWhiteMove5, BoardUtil.BLACK);
											addMovesToOpeningLibrary(boardAfterWhiteMove5, blackMoves5);
											j++;
											if(j%100==0){
												System.out.println(j+"\t\"" +
													whiteMove1.getSquare() + "\",\"" + 
													blackMove1.getSquare() + "\",\"" +
													whiteMove2.getSquare() + "\",\"" + 
													blackMove2.getSquare() + "\",\"" +
													whiteMove3.getSquare() + "\",\"" + 
													blackMove3.getSquare() + "\",\"" +
													whiteMove4.getSquare() + "\",\"" + 
													blackMove4.getSquare() + "\",\"" +
													whiteMove5.getSquare() + "\",{" + 
													blackMoves5.toString() + "}"
														);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		System.out.println("Ellapsed time: " + (new Date().getTime()-tic.getTime()) + "ms");
		int i = 0;
		for(String fen : openings.keySet()){
			i++;
			String moves = "###";
			for(Move move : openings.get(fen)){
				moves = moves + "|" + move.getSquare();
			}
			writer.write(fen + "," + moves + "\n");
			System.out.println(i + "\t" + fen + "," + moves);
		}
		writer.close();
	}
	
	private static void addMovesToOpeningLibrary(Board board, Set<Move> moves){
		if(openings.get(board) == null){
			openings.put(Converters.boardToFenSimplified(board), moves);
		}else{
			openings.get(Converters.boardToFenSimplified(board)).addAll(moves);
		}
	}
	
	private static Set<Move> getComputerMoves(Board board, int team){
		String folderLocation = "common/";
		if(new File("src/common/").exists()){ folderLocation = "src/common/";}
		if(new File("ChessEngine").exists()){ folderLocation = "";}
		if(new File("Chess/ChessEngine").exists()){ folderLocation = "Chess/";}
		Process p;
		Set<Move> bestMoves = new HashSet<Move>();
		try{
			p = Runtime.getRuntime().exec(new String[]{"bash","-c", folderLocation + "./ChessEngineLite fen fen \'" + Converters.boardToFen(board, team>0) + COMMAND_LINE});
//			p = Runtime.getRuntime().exec(new String[]{"bash","-c", folderLocation + "./ChessEngine fen \'" + Converters.boardToFen(board, team>0) + COMMAND_LINE});
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line ="";
			while((line=reader.readLine())!=null){
				Move move = Common.convertInputToMove(board, line.replaceAll(".*([a-h][0-9][a-h][0-9]).*$","$1"));
				bestMoves.add(move);
			}
			if(bestMoves.size()>0){
				return bestMoves;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}