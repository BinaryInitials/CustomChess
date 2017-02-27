package com.ozone.libraries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Move;
import com.ozone.movements.BoardUtil;
import com.ozone.utility.Converters;

public class LibraryLoader {
	int team;
	List<String[]> whiteMoves = new ArrayList<String[]>();
	List<String[]> blackMoves = new ArrayList<String[]>();
	public static final int MOVE_THRESHOLD = 10;
	HashMap<String, Move> boardMap = new HashMap<String, Move>();
	HashMap<String, List<String>> openingMap = new HashMap<String, List<String>>();
	
	public LibraryLoader(int team){
		this.team = team;
		if(team == BoardUtil.BLACK) loadBlackOpenings();
//		if(team == BoardUtil.WHITE) loadWhiteOpenings();
		initializeBoardMap();
	};
	
	public LibraryLoader(int team, boolean advancedLibrary){
		this.team = team;
		try {
			loadOpenings();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadOpenings() throws IOException {
		String fileName = System.getProperty("user.dir") + "/src/com/ozone/libraries/openings.txt";
		String newLineInFile;
		
		BufferedReader buffer = new BufferedReader(new FileReader(fileName));
		int lineNumber = 0;
		while((newLineInFile = buffer.readLine()) != null){
			lineNumber++;
			String[] moves = newLineInFile.split(",")[1].split("\\|");
			List<String> movesArray = new ArrayList<String>();
			for(int i=1;i<moves.length;i++){
				movesArray.add(moves[i]);
				if(moves[i].contains("#")){
					System.out.println("Problem at " + lineNumber + "\t" + newLineInFile);
				}
			}
			openingMap.put(newLineInFile.split(",")[0], movesArray);
		}
		buffer.close();
	}

	private void loadBlackOpenings() {
		
		java.io.InputStream file = LibraryLoader.class.getResourceAsStream("blackOpeningsConcice.txt");
		System.out.println(System.getProperty("java.class.path"));
		Scanner scanInputFile = new Scanner(file);
		String newLineInFile;
		while(scanInputFile.hasNextLine()){
			newLineInFile = scanInputFile.nextLine();
			blackMoves.add(newLineInFile.split(","));
		}
		scanInputFile.close();
	}

//	private void loadWhiteOpenings() {
//		
//		java.io.InputStream file = LibraryLoader.class.getResourceAsStream("whiteOpenings.txt");
//		
//		Scanner scanInputFile = new Scanner(file);
//		String newLineInFile;
//		while(scanInputFile.hasNextLine()){
//			newLineInFile = scanInputFile.nextLine();
//			System.out.println(newLineInFile);
//			whiteMoves.add(newLineInFile.split(","));
//		}
//		scanInputFile.close();
//	}

	private void initializeBoardMap(){
		List<String[][]> library = new ArrayList<String[][]>();
		if(team>0){
			library.add(Library1.w2Moves);
			library.add(Library1.w3Moves);
			library.add(Library2.w4Moves);
//			library.add(createWhiteOpeners(0));
//			library.add(createWhiteOpeners(1));
//			library.add(createWhiteOpeners(2));
//			library.add(createWhiteOpeners(3));
		}else{
//			library.add(Library1.b1Moves);
//			library.add(Library1.b2Moves);
//			library.add(Library2.b3Moves);
			library.add(createBlackOpeners(0));
			library.add(createBlackOpeners(1));
			library.add(createBlackOpeners(2));
			library.add(createBlackOpeners(3));
		}
		
		for(String[][] moves : library){
			for(String[] txtMoves : moves){
				Board board = new Board();
				board.reset();
				for(int i=0;i<txtMoves.length-1;i++){
					board = Common.convertInputToMove(board, txtMoves[i]).updateBoard(board);
				}
				boardMap.put(Converters.boardToFen(board, this.team>0), Common.convertInputToMove(board, txtMoves[txtMoves.length-1]));
			}	
		}
	}
	
//	private String[][] createWhiteOpeners(int i) {
//		int lastIndex = 2*i+1;
//		Set<String[]> output = new HashSet<String[]>();
//		for(String[] moves : whiteMoves){
//			String[] row = new String[lastIndex];
//			for(int j=0;j<lastIndex;j++){
//				row[j] = moves[j].replaceAll("\"", "");
//			}
//			output.add(row);
//		}
//		String[][] array = new String[output.size()][lastIndex];
//		int j=0;
//		for(String[] row : output){
//			array[j] = row;
//			j++;
//		}
//		return array;
//	}
	
	private String[][] createBlackOpeners(int i) {
		int lastIndex = 2*i+2;
		Set<String[]> output = new HashSet<String[]>();
		for(String[] moves : blackMoves){
			String[] row = new String[lastIndex];
			for(int j=0;j<lastIndex;j++){
				row[j] = moves[j].replaceAll("\"", "");
			}
			output.add(row);
		}
		String[][] array = new String[output.size()][lastIndex];
		int j=0;
		for(String[] row : output){
			array[j] = row;
			j++;
		}
		return array;
	}

	/*
	 * NOTE: There is a problem with findAdvancedOpening where the hash of the board can be found for either white's turn or black's turn.
	 * The issue here is that white might find a hash board with black's turn in it and erroneously return black's move. Which would make it not move. 
	 */
	public Move findAdvancedOpening(Board board){
		if(openingMap.get(Converters.boardToFenSimplified(board)) == null || openingMap.get(Converters.boardToFenSimplified(board)).size() == 0){
			return null;
		}
		List<String> moves = openingMap.get(Converters.boardToFenSimplified(board));
		for(String moveInText : moves){
			Move move = Common.convertInputToMove(board, moveInText);
			if(move.getPieceMoving() > 0 && team < 0){
				return null;
			}else if(move.getPieceMoving() < 0 && team > 0){
				return null;
			}
		}
		return Common.convertInputToMove(board, moves.get((int)Math.floor(Math.random()*moves.size())));
	}
}
