package com.ozone.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.utility.Converters;

public class EngineStrong implements Engine {
	private static final String TAIL = " | head -1";
	private static final String COMMAND_LINE = "\'|grep bestmove | sed \'s/.*bestmove.\\(.*\\) ponder.*/\\1/g\' | sort | uniq -c | sort -nr" + TAIL;
	private static final String COMMAND_LINE2 = "\'| grep \" pv \" | sed \'s/.*pv \\(....\\).*/\\1/g\' | sort | uniq -c | sort -nr" + TAIL;
	private int team;
	
	public EngineStrong(int team){
		this.team = team;
	}
	
	@Override
	public void switchTeam(){
		this.team = -team;
	}
	
	@Override
	public void setTeam(int team){
		this.team = team;
	}
	
	@Override
	public Move findMove(Board board, int iteration, boolean isConsole, boolean isDeveloper, List<Move> moveHistory) {
		String folderLocation = "com/ozone/engine/";
		if(new File("src/com/ozone/engine/ChessEngine").exists()){ folderLocation = "src/com/ozone/engine/";}
		if(new File("ChessEngine").exists()){ folderLocation = "";}
		if(new File("Chess/ChessEngine").exists()){ folderLocation = "Chess/";}
		
		Process p; 
		List<Move> bestMoves = new ArrayList<Move>();
		try{
			if(isDeveloper){
				System.out.println(System.getProperty("user.dir"));
				System.out.println("Location: " + folderLocation);
				System.out.println("Running this command: ");
				System.out.println(folderLocation + "./ChessEngine fen \'" + Converters.boardToFen(board, team > 0) + COMMAND_LINE);	
			}
			
			p = Runtime.getRuntime().exec(new String[]{"bash","-c", folderLocation + "./ChessEngineLite fen fen \'" + Converters.boardToFen(board, team > 0) + COMMAND_LINE});
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line ="";
			while((line=reader.readLine())!=null){
				if(isDeveloper){
					System.out.println(line);
				}
				Move move = Common.convertInputToMove(board, line.replaceAll(".*([a-h][1-8][a-h][1-8]).*$","$1"));
				bestMoves.add(move);
			}
			if(bestMoves.size()>0){
				return bestMoves.get(0);
			}else{
				p = Runtime.getRuntime().exec(new String[]{"bash", "-c", folderLocation + "./ChessEngineLite fen fen \'" + Converters.boardToFen(board, team > 0) + COMMAND_LINE2}); 
				p.waitFor();
				BufferedReader reader2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line2 ="";
				while((line2=reader2.readLine())!=null){
					if(isDeveloper){
						System.out.println(folderLocation + "./ChessEngineLite fen fen \'" + Converters.boardToFen(board, team > 0) + COMMAND_LINE2);
						System.out.println(line2);
					}
					Move move = Common.convertInputToMove(board, line2.replaceAll(".*([a-h][1-8][a-h][1-8]).*$","$1"));
					bestMoves.add(move);
				}
				if(bestMoves.size()>0){
					return bestMoves.get(0);
				}
				System.out.println("This line lead to an empty move: null");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Move findMove(Board board) {
		return findMove(board, 20, false, true, new ArrayList<Move>());
	}
	
	@Override
	public int getTeam() {
		return team;
	}
}