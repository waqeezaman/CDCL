import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;


public class cdcl_solver{

    final static String SATISFIABLE = "SATISFIABLE";
    final static String UNSATISFIABLE = "UNSATISFIABLE";
    final static String CONFLICT ="CONFLICT";
    final static String NOCONFLICT="NOCONFLICT";
    final static String UNIT ="UNIT";
    


    private static int NumVariables;

    private static List<HashSet<Integer>> Clauses = new ArrayList< HashSet<Integer>>();
    private static ArrayList<Integer> PartialAssignment = new ArrayList<Integer>();

    // two watch literal structure
    // which stores the literals in the formula and the clauses that they are being watched in 
    private static HashMap<Integer,ArrayList<Integer>> Literal_To_Clause= new HashMap<Integer,ArrayList<Integer>>();

    // two watch literal structure 
    // which stores clauses and the literals in them that are being watched
    private static HashMap<Integer, int[]> Clause_To_Literal= new HashMap<Integer,int[]>();


    private static Queue<Integer> InitialUnits = new ArrayDeque<Integer>();


    private static graph ImplicationGraph = new graph();

    public static void main(String[] args)throws Exception{
            
        
        ReadClauses();


        EliminateTautologies();

        
        // implement pure literal optimisation here

        // initialising two watch literal data structure
        for(int literal=-NumVariables; literal<=NumVariables;literal++){
            Literal_To_Clause.put(literal,new ArrayList<Integer>());
        }

        for(int clause=0;clause<Clauses.size();clause++){
        //for(int clause=Clauses.size()-1;clause>=0;clause--){

            Integer[] literals= new Integer[Clauses.get(clause).size()];

            Clauses.get(clause).toArray(literals);


            //should just remove unit clauses at this stage, and propogate them 
            if (literals.length==1){
                if(!InitialUnits.contains(literals[0])){
                    InitialUnits.add(literals[0]);
                    
                }
                //Clauses.remove(clause);
                Clause_To_Literal.put(clause,new int[]{literals[0],literals[0]});
                Literal_To_Clause.get(literals[0]).add(clause);
            }
            else{
                Clause_To_Literal.put(clause,new int[]{literals[0],literals[1]});
                Literal_To_Clause.get(literals[0]).add(clause);
                Literal_To_Clause.get(literals[1]).add(clause);


            }
        }
   

       System.out.println(CDCL());
      

    }

    private static String CDCL(){


        int DecisionLevel = 0;
        HashMap<Integer,Integer> SizeOfModelAtDecisionLevel = new HashMap<Integer,Integer>();

        String status = UnitPropogate(InitialUnits);
   
        do{

            while(status == "CONFLICT"){
                if(DecisionLevel==0){
                    return UNSATISFIABLE;
                }
                // analyse conflict
               
                Integer last_decision_index= SizeOfModelAtDecisionLevel.get(DecisionLevel);
                Integer last_decision = PartialAssignment.get(last_decision_index);

                Queue<Integer> units_to_propogate = new ArrayDeque<>();
                units_to_propogate.add(-last_decision);

                DecisionLevel-=1;
              
                PartialAssignment =  new ArrayList<>(PartialAssignment.subList(0,last_decision_index ));
              
                
                status = UnitPropogate(units_to_propogate);
                
            }
          
            if ( PartialAssignment.size()< NumVariables){

                DecisionLevel += 1;
                SizeOfModelAtDecisionLevel.put(DecisionLevel, PartialAssignment.size());

                Integer decision = Decide();

                // add decision to implication graph 
                ImplicationGraph.Graph.add(new vertex(decision,DecisionLevel));

                status=UnitPropogate(new ArrayDeque<Integer>(Arrays.asList(decision)));

            }
          
        }while( PartialAssignment.size()!=NumVariables || status=="CONFLICT");

        return SATISFIABLE;
    } 

    // propogates units until no unit clauses are left 
    // or a conflict is reached 
    // returns conflict if found
    private static String UnitPropogate(Queue<Integer> literals_to_propogate){

        // append to PartialAssignment
        // append to implication graph
        // change two-watch literal data structure, if a literal assigned false
        // if both two watch literals are unassigned, then we have a contradiction 
        // if only a single two watch literal is unassigned then it is a unit clause
  
  
        
        while( !literals_to_propogate.isEmpty()){


            Integer literal_to_propogate = literals_to_propogate.remove();
            Integer affected_literal = -literal_to_propogate;
         
            ArrayList<Integer> watched_clauses=new ArrayList<Integer>(Literal_To_Clause.get(affected_literal));

            AddToPartialAssignment(literal_to_propogate);
            
            // the affected literal is the negation of the last item we added to the propogation stack
            // this is the literal that has just been assigned false

            


            // check the status of every clause where the affected literal is 
            // a watch literal 
            for( Integer clause_index: watched_clauses){

                Integer watch_literal_1 = Clause_To_Literal.get(clause_index)[0];
                Integer watch_literal_2 = Clause_To_Literal.get(clause_index)[1];

                
               


                String clause_status = GetClauseStatus(watch_literal_1, watch_literal_2);

                if (clause_status==NOCONFLICT){
                    continue;
                }
                else if (clause_status==CONFLICT){
                    System.out.println("Conflict Found: "+ clause_index);
                    return "CONFLICT";
                }
                else{
                    // unit clause found
                    if(!PartialAssignment.contains(-watch_literal_1)  && 
                        !PartialAssignment.contains(watch_literal_1) && 
                        !literals_to_propogate.contains(watch_literal_1))
                        {
                            literals_to_propogate.add(watch_literal_1);
                        }

                    else if ( !PartialAssignment.contains(-watch_literal_2) &&
                                !PartialAssignment.contains(watch_literal_2)  &&
                                !literals_to_propogate.contains(watch_literal_2))
                                {
                            
                                    literals_to_propogate.add(watch_literal_2);
                        
                                }
    
                }

         

                


                
            }

        }
        return "NO CONFLICT";
    }

    //needs improving
    private static int Decide(){
        Random rand = new Random();
        while(true){
            int random_literal =  rand.nextInt(2*NumVariables+1)-NumVariables;
            //System.out.println("rand lit "+ random_literal);
            if( random_literal!=0 && !PartialAssignment.contains(random_literal) 
                && !PartialAssignment.contains(-random_literal)){
                    return random_literal;
                }
        }
        
    }

    // adds a literal to the partial assignment 
    // and updates the two-watch literal data structures
    private static void AddToPartialAssignment(Integer literal){

        PartialAssignment.add(literal);
        


       

        // this is the literal that has been made false
        Integer affected_literal = -literal;

        UpdateWatchedLiterals(affected_literal);
        


    }

    private static void UpdateWatchedLiterals(Integer affected_literal ){
        List<Integer> affected_clauses= new ArrayList<Integer>(Literal_To_Clause.get(affected_literal));

        for( Integer clause_index: affected_clauses){

            Integer watch_literal_1 = Clause_To_Literal.get(clause_index)[0];
            Integer watch_literal_2 = Clause_To_Literal.get(clause_index)[1];

            Integer old_watch_literal_1 = watch_literal_1;
            Integer old_watch_literal_2 = watch_literal_2;

            // if the watched literal has just been made false or
            // if the watched literal does not have a value then try to switch it
            if( watch_literal_1==0 || watch_literal_1==affected_literal){
                watch_literal_1 = SwitchWatchLiteral(watch_literal_1, watch_literal_2, Clauses.get(clause_index));
            }
            if(watch_literal_2==0 || watch_literal_2==affected_literal){
                watch_literal_2 = SwitchWatchLiteral(watch_literal_2, watch_literal_1, Clauses.get(clause_index));
            }


            // if watch literals have changed
            // update data structure
            if( watch_literal_1!= old_watch_literal_1){
                Literal_To_Clause.get(old_watch_literal_1).remove(clause_index);
                Literal_To_Clause.get(watch_literal_1).add(clause_index);
                Clause_To_Literal.get(clause_index)[0] = watch_literal_1;
            }

            if( watch_literal_2!= old_watch_literal_2){
                Literal_To_Clause.get(old_watch_literal_2).remove(clause_index);
                Literal_To_Clause.get(watch_literal_2).add(clause_index);
                Clause_To_Literal.get(clause_index)[1] = watch_literal_2;
            }
        }

    } 

    // gets the status of a clause {satisfied, conflict, unit}
    // based on the watch literals
    private static String GetClauseStatus(Integer w1,Integer w2){
        if( PartialAssignment.contains(-w1) && PartialAssignment.contains(-w2)){
            return CONFLICT;
        }
        else if(!PartialAssignment.contains(-w1) && !PartialAssignment.contains(-w2)){
            return NOCONFLICT;
        }
        else{
            return UNIT;
        }
    }

    // switches the literal being watched to an undefined or true literal
    // returns the same value if no other valid watch literal exists 
    private static int SwitchWatchLiteral(Integer current_watch_literal,Integer other_watch_literal,HashSet<Integer> clause){
        for(int literal: clause){
            if ( literal!=other_watch_literal && !PartialAssignment.contains(-literal) && literal!=current_watch_literal){
                return literal;
            }
        }
        return current_watch_literal;
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
                    String clauses = clauses_vars.split(" ")[1];
                    String vars = clauses_vars.split(" ")[0];
                    
                    NumVariables =Integer.parseInt(vars);

                    break;
                }


                
            
        }

        while (true) {

            String line = in.readLine();

            if (line == null) break;
            
                String[] vars_in_clause = line.split(" ");
                System.out.println(line);
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

    private static void OutputWatchedLiterals(){
        for(int c=0; c<Clauses.size();c++){
            System.out.println("Clause: " +c);

            for( int literal: Clause_To_Literal.get(c)){
                System.out.print(literal + " || ");
            }
            System.out.println();

        }

    }

}