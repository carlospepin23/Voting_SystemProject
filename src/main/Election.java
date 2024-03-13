package main;

import interfaces.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
//import java.io.IOException;
import java.io.IOException;

import data_structures.ArrayList;
import main.Candidate;
import main.Ballot;


public class Election {
	private List<Candidate> candidates=new ArrayList<Candidate>();
	private List<Ballot> ballots=new ArrayList<Ballot>();
	private List<ArrayList<Integer>> board=new ArrayList<ArrayList<Integer>>();
	private List<String> eliminated_candidates=new ArrayList<String>();
	List<Candidate> survivors=new ArrayList<Candidate>();
	
	int total_b=0,valid_b=0,blank_b=0,invalid_b=0,elim_v=0,rip_v=0;
	//private List<ArrayList<Ballot>> tournament=new ArrayList<ArrayList<Ballot>>();
	
	/* Constructor that implements the election logic using the files candidates.csv 
	and ballots.csv as input. (Default constructor) */ 
	public Election() {
		//utilize BufferedReader & FileReader to read the csv files containing the data
				BufferedReader candidates_Reader = null,ballots_Reader=null;
				try {
					candidates_Reader = new BufferedReader(new FileReader("inputFiles/candidates.csv"));
					ballots_Reader=new BufferedReader(new FileReader("inputFiles/ballots.csv"));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String c_line,b_line;
				//genera lista de candidatos
			    try {
					while ((c_line = candidates_Reader.readLine()) != null) {
						candidates.add(new Candidate(c_line));
					}
					
					//genera lista de ballots
				    while ((b_line = ballots_Reader.readLine()) != null) {
				    	Ballot temp=new Ballot(b_line, candidates);
				    	total_b++;
				    	if(temp.getBallotType()==0) {
				    		ballots.add(temp);
				    		valid_b++;
				    	}
				    	else if(temp.getBallotType()==1) {
				    		blank_b++;
				    	}
				    	else if(temp.getBallotType()==2) {
				    		invalid_b++;
				    	}
				    	
				    	
				    	
				    }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    
//			   
	}
	/* Constructor that receives the name of the candidate and ballot files and applies 
	the election logic. Note: The files should be found in the input folder. */
	public Election(String candidates_filename, String ballot_filename) {

//		//utilize BufferedReader & FileReader to read the csv files containing the data
//		BufferedReader candidates_Reader=new BufferedReader(new FileReader("inputFiles/"+candidates_filename));
//		BufferedReader ballots_Reader=new BufferedReader(new FileReader("inputFiles/"+ballot_filename));
//

	}
	// returns the name of the winner of the election 
	public String getWinner() {
		reclassification();
		//int x=9;
		
		//if the candidate has more than 50% (lucky_num) of the ballots, the candidate wins
		int lucky_num=valid_b/2;
		boolean found=false;
		
		while(found==false) {
			//printBallotDistribution();
			for(int i=0;i<board.size();i++) {
				ArrayList<Integer>c=board.get(i); //candidato de cada posicion correspondiente
			
				if(rank_Counter(c,1)>lucky_num) {
				//int x=7;
					return candidates.get(i).getName();
				}
			}
			
			eliminate_Lowest_Candidate();
			reclassification();	
		
		}

		return null;
		
		
	} 
	// returns the total amount of ballots submitted
	public int getTotalBallots() {
		return total_b;
	} 
	// returns the total amount of invalid ballots
	public int getTotalInvalidBallots() {
		return invalid_b;

	} 
	// returns the total amount of blank ballots
	public int getTotalBlankBallots() {
		return blank_b;
	}
	// returns the total amount of valid ballots
	public int getTotalValidBallots() {
		return valid_b;
	}
	
	/* List of names for the eliminated candidates with the numbers of 1s they had, 
	must be in order of elimination. Format should be <candidate name>-<number of 1s 
	when eliminated>*/
	public List<String> getEliminatedCandidates() {
		getWinner();
		return eliminated_candidates;
	}
	
	public void reclassification() {
		board.clear();
	    //genera lista de listas [candidato1[1,2,3], candidato2[4,5,6]]
	    for(int i=0;i<candidates.size();i++) { //por cada candidato
	    	ArrayList<Integer>t=new ArrayList<Integer>(); //candidato i
	    	
	    	for(int j=0;j<ballots.size();j++) { //por cada papeleta de los candidatos correspondiente
	    		Integer b=ballots.get(j).getRankByCandidate(candidates.get(i).getId()); //papeleta j
	    		t.add(b);
	    		
	    	}
    		
	    	board.add(t);
	    }
	}
	
	public void eliminate_Lowest_Candidate() {
		int max=valid_b;
		int r=1;
		survivors.clear();
		for(Candidate c:candidates) {
			survivors.add(c);
		}
		
		while(r<max) {
			ArrayList<Integer> ranks_eliminatory=new ArrayList<Integer>(); //esta en orden con los candidatos
			for(int i=0;i<board.size();i++) {
				ArrayList<Integer>c=board.get(i); //candidate
				ranks_eliminatory.add(rank_Counter(c, r));   //c1:4,c2:3,c3:0
			}
			
			Candidate loser=min_Candidate(ranks_eliminatory); //esto obtiene la pos del candidato con menos votos
			//error es que despues de encontrar el empate de los mas bajitos,busca el 2 para todos
			if(loser!=null) { //si hay un min 


				for(Ballot b:ballots) {
					b.eliminate(loser.getId()); //
				}
				
				candidates.remove(loser); // 
				eliminated_candidates.add(loser.getName()+"-"+Integer.toString(rip_v));
				
				break;
			}
			
			r++;
				
		}	
	}
	
	//returns the count of the selected rank for a single candidate
	public int rank_Counter(ArrayList<Integer> candidate_ballots,int rank) { //funciona
		int counter=0;
		for(int b:candidate_ballots) {
			if(b==rank) counter++;
		}
		
		return counter;
	}
	
	//returns position
	public Candidate min_Candidate(ArrayList<Integer> ranks_of_candidates) {		//funciona
		List<Integer> to_remove=new ArrayList<Integer>();
		int min_pos=0;
		for(int i=1;i<ranks_of_candidates.size();i++) {
			if(ranks_of_candidates.get(i)<ranks_of_candidates.get(min_pos)) {
				min_pos=i;
				
			//if theres two equal mins, theres need to be a tie breaker
			}else if (ranks_of_candidates.get(i).equals(ranks_of_candidates.get(min_pos))) {
				
				//desempate		
				for(int r=0;r<ranks_of_candidates.size();r++) {
						
					if(!ranks_of_candidates.get(r).equals(ranks_of_candidates.get(i))) {	
						
						to_remove.add(r);
						
					}
				}
				
				for(int k=to_remove.size()-1;k>=0;k--) {
					board.remove(to_remove.get(k));
					survivors.remove(to_remove.get(k));
				}
					
				return null;
				
			}
		}
		
		rip_v=rank_Counter(board.get(min_pos), 1);
		return survivors.get(min_pos);
	}
	
	/**
	* Prints all the general information about the election as well as a 
	* table with the vote distribution.
	* Meant for helping in the debugging process.
	*/
	public void printBallotDistribution() {
//		System.out.println("Total ballots:" + getTotalBallots());
//		System.out.println("Total blank ballots:" + getTotalBlankBallots());
//		System.out.println("Total invalid ballots:" + getTotalInvalidBallots());
//		System.out.println("Total valid ballots:" + getTotalValidBallots());
	 System.out.println(getEliminatedCandidates());
		for(Candidate c: candidates) {
			System.out.print(c.getName().substring(0, c.getName().indexOf(" ")) + "\t");
			for(Ballot b: ballots) {
				int rank = b.getRankByCandidate(c.getId());
				String tableline = "| " + ((rank != -1) ? rank: " ") + " ";
				System.out.print(tableline); 
			}
			System.out.println("|");
		}
	}

	

	
	
//	public static void main(String[] args) {
//		Election election = new Election();
//			//Election election = new Election("candidates.csv", "ballots.csv");
////			
////			assertAll(
////			
////					() -> assertTrue(election.getEliminatedCandidates().get(0).equals("Lola Mento-1"), "Didn't return correct eliminated candidate and/or count for this position"),
////					() -> assertTrue(election.getEliminatedCandidates().get(1).equals("Juan Lopez-1"), "Didn't return correct eliminated candidate and/or count for this position"),
////					() -> assertTrue(election.getEliminatedCandidates().get(2).equals("Pucho Avellanet-3"), "Didn't return correct eliminated candidate and/or count for this position")
////					);
//			
////		List<String> elims=election.getEliminatedCandidates();
////		for(int i=0;i<election.getEliminatedCandidates().size();i++) {
////			System.out.println(election.getEliminatedCandidates().get(i));
////
////		}
//		System.out.println(election.getWinner());
//		
//		
////		
////		election.printBallotDistribution();
////		ArrayList<Integer>test=new ArrayList<Integer>();
////		test.add(0);
////		test.add(6);
////		test.add(2);
////		test.add(5);
////		test.add(0);
////		System.out.println( election.min_Candidate(test));
////		
////		ArrayList<Integer>test=new ArrayList<Integer>();
////		test.add(1);
////		test.add(4);
////		test.add(3);
////		test.add(1);
////		test.add(1);
////		System.out.println( election.rank_Counter(test, 1));
//		
//		
//		
//
//		
//	}
}
