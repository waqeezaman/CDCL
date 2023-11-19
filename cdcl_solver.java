import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class cdcl_solver{

    final String SATISFIABLE = "SATISFIABLE";
    final String UNSATISFIABLE = "UNSATISFIABLE";

    private static int NumVariables;
    private static int NumClauses;

    private static List<HashSet<Integer>> Clauses = new ArrayList< HashSet<Integer>>();
    private static ArrayList<Integer> PartialAssignment = new ArrayList<Integer>();

    // two watch literal structure
    // which stores the literals in the formula and the clauses that they are being watched in 
    private static HashMap<Integer,ArrayList<Integer>> Literal_To_Clause= new HashMap<Integer,ArrayList<Integer>>();

    // two watch literal structure 
    // which stores clauses and the literals in them that are being watched
    private static HashMap<Integer, int[]> Clause_To_Literal= new HashMap<Integer,int[]>();



    public static void main(String[] args)throws Exception{
            
        
        ReadClauses();

        

       

        
        OutputClauses(Clauses);


        EliminateTautologies();
        //Clauses = RemoveAllDoubleLiterals(Clauses);
        
        // implement pure literal optimisation here

        // initialising two watch literal data structure

        // for(int clause=0;clause<Clauses.size();clause++){
        //     Integer[] literals = (Integer[])Clauses.get(clause).toArray();
        //     if (literals.length==1){
        //         Clause_To_Literal.put(clause,new int[]{literals[0],0});
        //         Literal_To_Clause.put(literals[0],)
        //     }
        //     else{
        //     Clause_To_Literal.put(clause,new int[]{literals[0],literals[1]});
        //     }
        // }

        String tree ="B";
        OutputClauses(Clauses);
       // System.out.println(Solve(Clauses,tree));
        
        
    }

    private static String CDCL(String Formula){
        int DecisionLevel = 0;
        HashMap<Integer,Integer> SizeOfModelAtDecisionLevel = new HashMap<Integer,Integer>();



        if ( PartialAssignment.size()< NumVariables){
            SizeOfModelAtDecisionLevel.put(DecisionLevel, PartialAssignment.size());
            DecisionLevel += 1;
           //int decision = Decide();
           UnitPropogate();

        }
        

        return "";
    } 


    private static void UnitPropogate(){
        // append to PartialAssignment
        // append to implication graph
        // change two-watch literal data structure, if a literal assigned false
        // if both two watch literals are unassigned, then we have a contradiction 
        // if only a single two watch literal is unassigned then it is a unit clause

        boolean propogation_complete = false;
        
        while( !propogation_complete){

            // the affected literal is the negation of the last item we added to the propogation stack
            // this is the literal that has just been assigned false
            Integer affected_literal = -PartialAssignment.get(PartialAssignment.size()-1);

            // check the status of every affected clause
            for( Integer clause_index: Literal_To_Clause.get(affected_literal)){

                Integer watch_literal_1 = Clause_To_Literal.get(clause_index)[0];
                Integer watch_literal_2 = Clause_To_Literal.get(clause_index)[1];

                

                // is the affected literal a watch literal
                if( watch_literal_1 == affected_literal ){

                }
                else if( watch_literal_2 == affected_literal){

                }
                // the affected literal is not a watch literal for this clause
                else{
                    System.out.println("Two watch Data Structure Is Inconsistent");
                }


                
            }

        }
    }

    public static  String GetClauseStatus(HashSet clause){
        
        return "";
    }

    // returns the literal in a unit clause if it exists 
    // 0 otherwise
    private static Integer FindUnitClauseLiteral(){

        // the literal that has just been made false is the only one we have 
        // to consider when using two-watch literals
        Integer literal_made_false = PartialAssignment.get(PartialAssignment.size()-1);

        // when a literal is made false
        // the clauses where it is a watched literal 
        // are now either unit clauses
        // or are conflicts 


        return 0;

    }

    public static void EliminateTautologies(){
        List<HashSet<Integer>> oldClauses = new ArrayList< HashSet<Integer>>(Clauses);

        for( HashSet<Integer> clause: oldClauses){
            
            if ( IsTautology(clause) ){
                Clauses.remove(clause);
                
            }
        }

    }

    public static boolean IsTautology(HashSet<Integer> clause){
        for (int var =1; var<=NumVariables;var++){

            if (clause.contains(var) && clause.contains(-var)){
                return true;
            }
        }
        return false;

    }

    public static void OutputClauses(List<HashSet<Integer>> clauses){
        System.out.println("CLAUSES");
        for( HashSet<Integer> clause : clauses){
            System.out.println(clause);
            
        }
        System.out.println("=============================================================================");
    }

    private static void ReadClauses() throws Exception{
        var in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = in.readLine();
            if (line == null) break;
                if (line.charAt(0) =='c'){
                    continue;
                }
                if( line.contains("p cnf")){

                    String clauses_vars = line.replace("p cnf","");
                    clauses_vars = clauses_vars.trim();
                    String clauses = clauses_vars.split(" ")[0];
                    String vars = clauses_vars.split(" ")[1];
                    
                    NumClauses = Integer.parseInt(clauses);
                    NumVariables =Integer.parseInt(vars);

                    break;
                }


                
            
        }

        while (true) {

            String line = in.readLine();

            if (line == null) break;
            
                String[] vars_in_clause = line.split(" ");
                HashSet<Integer> clause = new HashSet<Integer>();

                for(String s: vars_in_clause){
                    if( Integer.parseInt(s)==0){
                        break;
                    }
                    clause.add( Integer.parseInt(s));
                }
                Clauses.add(clause);


                
            
        }
    }

}