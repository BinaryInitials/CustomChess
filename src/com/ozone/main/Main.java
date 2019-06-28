package com.ozone.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Common.GameStatus;
import com.ozone.common.Move;
import com.ozone.engine.Engine;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH1;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH3;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH7;
import com.ozone.engine.EngineNewFastFast;
import com.ozone.engine.EngineStrong;
import com.ozone.movements.BoardUtil;
import com.ozone.utility.Converters;

public class Main {
	static Engine engine;
	
	public static void main(String[] args){
		int team = 1;
		boolean isConsole = true;
		if(args != null && args.length > 0){
			
			if(args[0].toLowerCase().equals("fen")){
				Board board = Converters.fenToBoard(args[1]);
				BoardUtil.displayBoard(board, true);
				System.exit(0);
			}
			
			boolean wantsMinMax7 = false;
			boolean wantsMinMax5 = false;
			boolean wantsMinMax3 = false;
			boolean wantsMinMax1 = false;
			boolean wantsHard = false;
			boolean wantsAgainstStockFish = false;
			boolean wantsAgainstDefault = false;
			for(String arg : args){
				if(arg.toLowerCase().matches(".*stockfish.*")) {
					wantsAgainstStockFish = true;
				}
				if(arg.toLowerCase().matches(".*default.*")) {
					wantsAgainstDefault = true;
				}
				if(arg.toLowerCase().matches("^.*(minmax7).*$")){
					wantsMinMax7 = true;
				}
				if(arg.toLowerCase().matches("^.*(minmax5).*$")){
					wantsMinMax5 = true;
				}
				if(arg.toLowerCase().matches("^.*(minmax3).*$")){
					wantsMinMax3 = true;
				}
				if(arg.toLowerCase().matches("^.*(minmax1).*$")){
					wantsMinMax1 = true;
				}
				if(arg.toLowerCase().matches("^.*(hard|strong).*$")){
					wantsHard = true;
				}
				if(arg.toLowerCase().contains("w")){
					team = BoardUtil.WHITE;
				}else if(arg.toLowerCase().contains("b")){
					team = BoardUtil.BLACK;
				}
			}
			
			if(wantsMinMax7){
				System.out.println("Loading Minemax MAXDEPTH=7");
				engine = new EngineMinMaxNoMateDectionMAXDEPTH7(-team);
			}else if(wantsMinMax5){
				System.out.println("Loading Minemax MAXDEPTH=5");
				engine = new EngineMinMaxNoMateDectionMAXDEPTH5(-team);
			}else if(wantsMinMax3){
				System.out.println("Loading Minemax MAXDEPTH=3");
				engine = new EngineMinMaxNoMateDectionMAXDEPTH3(-team);
			}else if(wantsMinMax1){
				System.out.println("Loading Minemax MAXDEPTH=1");
				engine = new EngineMinMaxNoMateDectionMAXDEPTH1(-team);
			}else if(wantsHard){
				System.out.println("Loading hard engine");
				engine = new EngineStrong(-team);
			}else{
				System.out.println("Loading default engine");
				engine = new EngineNewFastFast(-team);
			}
			
			if(wantsAgainstStockFish) {
				if(engine == null) {
					System.out.println("Default engine playing against StockFish");
					engine = new EngineNewFastFast(-team); 
				}
				Engine stockfish = new EngineStrong(team);
				EngineSimulation es = new EngineSimulation();
				Date tic = new Date();
				Board board = new Board();
				board.reset();
				GameStatus gs = es.start(stockfish, engine, false, true, board, true);
				Date toc = new Date();
				int time = (int)((toc.getTime() - tic.getTime()));
				if(gs.equals(GameStatus.BLACK_IS_CHECK_MATE)){
					System.out.println("White wins.");
				}else if(gs.equals(GameStatus.WHITE_IS_CHECK_MATE)){
					System.out.println("Black wins");
				}else {
					System.out.println("Stale mate or some other tie: " + gs.toString());
				}
				System.out.println("Elapsed time: " + time);
				System.exit(1);
			}else if(wantsAgainstDefault) {
				if(engine == null) {
					System.out.println("Default engine playing against StockFish");
					engine = new EngineMinMaxNoMateDetectiongMAXDEPTH3(-team); 
				}
				Engine default = new EngineNewFastFast(team);
				EngineSimulation es = new EngineSimulation();
				Date tic = new Date();
				Board board = new Board();
				board.reset();
				GameStatus gs = es.start(stockfish, engine, false, true, board, true);
				Date toc = new Date();
				int time = (int)((toc.getTime() - tic.getTime()));
				if(gs.equals(GameStatus.BLACK_IS_CHECK_MATE)){
					System.out.println("White wins.");
				}else if(gs.equals(GameStatus.WHITE_IS_CHECK_MATE)){
					System.out.println("Black wins");
				}else {
					System.out.println("Stale mate or some other tie: " + gs.toString());
				}
				System.out.println("Elapsed time: " + time);
				System.exit(1);
			}
			
		}else {
			engine = new EngineMinMaxNoMateDectionMAXDEPTH5(-team);
		}
		start(team, args, isConsole);
	}
	
	private static void start(int humanTeam, String[] args, boolean isConsole){
		List<Move> moveHistory = new ArrayList<Move>();
		Board board = new Board();
		boolean isDeveloperMode = true;
		board.reset();
		if(humanTeam == BoardUtil.BLACK){
			BoardUtil.displayBoardReverse(board, isConsole);
		}else{
			BoardUtil.displayBoard(board, isConsole);
		}
		
		Common.GameStatus gameStatus = GameStatus.ON_GOING;
		int iteration = 0;
		
		if(humanTeam == BoardUtil.WHITE){
			String input = Common.captureHumanMove(isDeveloperMode);
			input = Common.checkForEdit(board, input, isDeveloperMode, isConsole);
			Move humanMove = Common.validateMove(board, Common.convertInputToMove(board, input), BoardUtil.WHITE, isDeveloperMode);
			System.out.println("HUMAN:\t" + humanMove);
			board.movePiece(humanMove);
			BoardUtil.displayBoard(board, isConsole);
			moveHistory.add(humanMove);
		}else{
			Move computerMove = engine.findMove(board, iteration, isConsole, isDeveloperMode, moveHistory);
			System.out.println("MACHIN:\t" + computerMove);
			board.movePiece(computerMove);
			BoardUtil.displayBoardReverse(board, isConsole);
			moveHistory.add(computerMove);
		}
		
		while(gameStatus == GameStatus.ON_GOING){
			/*
			 * Black's turn
			 */
			if(humanTeam == BoardUtil.BLACK){
				String input = Common.captureHumanMove(isDeveloperMode);
				input = Common.checkForEdit(board, input, isDeveloperMode, isConsole);
				Move humanMove = Common.validateMove(board, Common.convertInputToMove(board, input), BoardUtil.BLACK, isDeveloperMode);
				System.out.println("Move #" + iteration + "HUMAN:\t" + humanMove);
				board.movePiece(humanMove);
				BoardUtil.displayBoardReverse(board, isConsole);
				moveHistory.add(humanMove);
			}else{
				Move computerMove = engine.findMove(board, iteration, isConsole, isDeveloperMode, moveHistory);
				if(computerMove != null) {
					System.out.println("Move #" + iteration + "MACHIN:\t" + computerMove);
					board.movePiece(computerMove);
					BoardUtil.displayBoard(board, isConsole);
					moveHistory.add(computerMove);
				}else {
					gameStatus = GameStatus.BLACK_IS_CHECK_MATE;
					break;
				}
			}
			
			/*
			 * Game status update
			 */
			gameStatus = Common.getGameStatus(board, moveHistory, BoardUtil.WHITE, 0);
			if(gameStatus != GameStatus.ON_GOING){
				break;
			}
			/*
			 * White's turn
			 */
			if(humanTeam == BoardUtil.BLACK){
				Move computerMove = engine.findMove(board, iteration, isConsole, isDeveloperMode, moveHistory);
				if(computerMove != null) {
					System.out.println("Move #" + iteration + "MACHIN:\t" + computerMove);
					board.movePiece(computerMove);
					BoardUtil.displayBoardReverse(board, isConsole);
					moveHistory.add(computerMove);
				}else {
					gameStatus = GameStatus.BLACK_IS_CHECK_MATE;
					break;
				}
			}else{
				String input = Common.captureHumanMove(isDeveloperMode);
				input = Common.checkForEdit(board, input, isDeveloperMode, isConsole); 
				Move humanMove = Common.validateMove(board, Common.convertInputToMove(board, input), BoardUtil.WHITE, isDeveloperMode);
				System.out.println("Move #" + iteration + "HUMAN:\t" + humanMove);
				board.movePiece(humanMove);
				BoardUtil.displayBoard(board, isConsole);
				moveHistory.add(humanMove);
			}
			
			/*
			 * Game status update
			 */
			gameStatus = Common.getGameStatus(board, moveHistory, BoardUtil.BLACK, 0);
			iteration++;
		}
		System.out.println(gameStatus.toString().replaceAll("_", " ") + "!");
	}
}