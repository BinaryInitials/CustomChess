package com.ozone.libraries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;
import com.ozone.utility.Converters;

public class OpeningGeneratorForBlack {
	
	private static final String RESET_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private static final String COMMAND_LINE = "|grep bestmove | sed \'s/.*bestmove.\\(.*\\) ponder.*/\\1/g\' | sort | uniq -c | sort -nr";
	
	private static final List<String> WHITE_FIRST_MOVES = Arrays.asList("E2E4", "E2E3", "D2D4", "D2D3", "G1F3", "B1C3");
	private static final List<String> BLACK_FIRST_MOVES = Arrays.asList("E7E5", "E7E6", "D7D5", "D7D6", "C7C5", "C7C6", "B8C6", "G8F6");
	
	public static void main(String[] args) {
		
		List<String> stockFishBestOpenings = generateOpenings(1);
		
		//Write all the openings to file
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(new File("blackOpenings.txt")));
			for(String opening : stockFishBestOpenings) {
				buffer.write(opening + "\n");
			}
			buffer.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static List<String> generateOpenings(int depth) {
		List<String> lines = new ArrayList<String>();
		
		//To make the game interesting, add more openings for black's first move
		
		Board board = new Board(RESET_FEN);
		
		for(String whiteFirstMove : WHITE_FIRST_MOVES) {
			Move whiteMove = new Move(board, whiteFirstMove);
			Board boardAfterWhiteHumanMove = whiteMove.updateBoard(board);
			String fen = Converters.boardToFen(boardAfterWhiteHumanMove, BoardUtil.BLACK);
			Date tic = new Date();
			lines.add(fen.replaceAll(" .*", "") + "=" + BLACK_FIRST_MOVES.toString().replaceAll(", ", ","));
			System.out.println(whiteMove + "\t" + fen.replaceAll(" .*", "") + "=" + BLACK_FIRST_MOVES.toString().replaceAll(", ", ","));
			
			for(String blackFirstMove : BLACK_FIRST_MOVES) {
				
				System.out.println("Black first move : " + blackFirstMove);
				
				Move blackMove = new Move(boardAfterWhiteHumanMove, blackFirstMove);
				generateOpenings(blackMove.updateBoard(boardAfterWhiteHumanMove), depth, lines);
			}
			Date toc = new Date();
			System.out.println(fen.replaceAll(" .*", "") + "=" + BLACK_FIRST_MOVES.toString().replaceAll(", ", ",") + "\t" + (toc.getTime()-tic.getTime())/1000 + " seconds");
			
		}
		return lines;
	}
	
	private static void generateOpenings(Board boardAfterBlackStockfishMove, int depth, List<String> lines) {
		
		for(Move whiteMoveHuman : MoveUtil.getAllMoveForTeam(boardAfterBlackStockfishMove,  BoardUtil.WHITE)) {
			Board boardAfterWhiteHumanMove = whiteMoveHuman.updateBoard(boardAfterBlackStockfishMove);
			String fen = Converters.boardToFen(boardAfterWhiteHumanMove, BoardUtil.BLACK);
			List<Move> stockFishMoves = getStockfishMoves(fen);
			lines.add(fen.replaceAll(" .*", "") + "=" + stockFishMoves.toString().replaceAll("[kw][A-Z]: ", "").replaceAll(", ", ","));
			System.out.println(whiteMoveHuman + "\t" + fen.replaceAll(" .*", "") + "=" + stockFishMoves.toString().replaceAll("[kw][A-Z]: ", "").replaceAll(", ", ","));
			if(depth > 0) {
				for(Move stockFishMove : stockFishMoves) {
					generateOpenings(stockFishMove.updateBoard(boardAfterWhiteHumanMove), depth-1, lines);
				}
			}
		}
	}
	
	
	private static List<Move> getStockfishMoves(String fen){
		List<Move> moves = new ArrayList<Move>();
		try {
			Process p = Runtime.getRuntime().exec(new String[]{"bash","-c", "raw/./ChessEngine fen  \'" + fen + "\'" + COMMAND_LINE});
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line ="";
			while((line=reader.readLine())!=null){
				Move move = Common.convertInputToMove(new Board(fen), line.replaceAll(".*([a-h][0-9][a-h][0-9]).*$","$1"));
				moves.add(move);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return moves;
	}

}
