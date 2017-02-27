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

public class Engine2 {
	private static final String COMMAND_LINE = "\'|grep bestmove | sed \'s/.*bestmove.\\(.*\\) ponder.*/\\1/g\' | sort | uniq -c | sort -nr"; 
	public static Move findMove(Board board, int team, boolean isDeveloper){
		String folderLocation = "common/";
		if(new File("src/common/ChessEngine").exists()){ folderLocation = "src/common/";}
		if(new File("ChessEngine").exists()){ folderLocation = "";}
		if(new File("Chess/ChessEngine").exists()){ folderLocation = "Chess/";}
		
		Process p; 
		List<Move> bestMoves = new ArrayList<Move>();
		try{
			if(isDeveloper){
				System.out.println(System.getProperty("user.dir"));
				System.out.println("Location: " + folderLocation);
				System.out.println("Running this command: ");
				System.out.println(folderLocation + "./ChessEngine fen  \'" + Converters.boardToFen(board, team > 0) + COMMAND_LINE);	
			}
			
			p = Runtime.getRuntime().exec(new String[]{"bash","-c", folderLocation + "./ChessEngine fen  \'" + Converters.boardToFen(board, team > 0) + COMMAND_LINE});
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line ="";
			while((line=reader.readLine())!=null){
				if(isDeveloper){
					System.out.println(line);
				}
				Move move = Common.convertInputToMove(board, line.replaceAll(".*([a-h][0-9][a-h][0-9]).*$","$1"));
				bestMoves.add(move);
			}
			if(bestMoves.size()>0){
				return bestMoves.get(0);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}