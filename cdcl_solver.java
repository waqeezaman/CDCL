import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;

public class cdcl_solver{

    final static String SATISFIABLE = "SATISFIABLE";
    final static String UNSATISFIABLE = "UNSATISFIABLE";

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


    private static Queue<Integer> InitialUnits = new ArrayDeque<Integer>();

    public static void main(String[] args)throws Exception{
            
        
        ReadClauses();

        

       

        


        EliminateTautologies();
        OutputClauses(Clauses);

        
        // implement pure literal optimisation here

        // initialising two watch literal data structure

        for(int literal=-NumVariables; literal<=NumVariables;literal++){
            Literal_To_Clause.put(literal,new ArrayList<Integer>());
        }

        for(int clause=0;clause<Clauses.size();clause++){

            Integer[] literals= new Integer[Clauses.get(clause).size()];

            Clauses.get(clause).toArray(literals);

            if (literals.length==1){
                if(!InitialUnits.contains(literals[0])){
                    InitialUnits.add(literals[0]);
                    
                }
                Clause_To_Literal.put(clause,new int[]{literals[0],0});
                Literal_To_Clause.get(literals[0]).add(clause);
            }
            else{
                Clause_To_Literal.put(clause,new int[]{literals[0],literals[1]});
                Literal_To_Clause.get(literals[0]).add(clause);
                Literal_To_Clause.get(literals[1]).add(clause);


            }
        }

        String tree ="B";

       //System.out.println(Clause_To_Literal.toString());
       System.out.println("Initial Units: "+ InitialUnits.toString());

       System.out.println(CDCL());
      

    }

    private static String CDCL(){
        int DecisionLevel = 0;
        HashMap<Integer,Integer> SizeOfModelAtDecisionLevel = new HashMap<Integer,Integer>();

        String status = UnitPropogate(InitialUnits);
        System.out.println(status);

        System.out.println(PartialAssignment.size());
        System.out.println(PartialAssignment);
        System.out.println(NumVariables);
        
        do{
            //System.out.println("PARTIAL "+ PartialAssignment.toString());
            while(status == "CONFLICT"){
                if(DecisionLevel==0){
                    return UNSATISFIABLE;
                }
                // analyse conflict
                int lastdecisionlevel = SizeOfModelAtDecisionLevel.get(DecisionLevel-1);
                DecisionLevel-=1;
                //System.out.println("Before Trim "+ PartialAssignment.size());
                PartialAssignment =  new ArrayList<>(PartialAssignment.subList(0,lastdecisionlevel ));
                //System.out.println("After Trim "+ PartialAssignment.size());

                status = UnitPropogate(null);
                
            }
            System.out.println("Partial Assignment size "+ PartialAssignment.size());
            if ( PartialAssignment.size()< NumVariables){
                SizeOfModelAtDecisionLevel.put(DecisionLevel, PartialAssignment.size());
                DecisionLevel += 1;

                int decision = Decide();
                //System.out.println("Decide level "+ DecisionLevel );
                //AddToPartialAssignment(decision);
                UnitPropogate(new ArrayDeque<Integer>(Arrays.asList(decision)));

            }
        
        }while( PartialAssignment.size()!=NumVariables || status=="CONFLICT");

        return SATISFIABLE;
    } 

    // propogates units until no unit clauses are left 
    // or a conflict is reached 
    // returns conflict if found
    private static String UnitPropogate(Queue<Integer> units_to_propogate){

        // append to PartialAssignment
        // append to implication graph
        // change two-watch literal data structure, if a literal assigned false
        // if both two watch literals are unassigned, then we have a contradiction 
        // if only a single two watch literal is unassigned then it is a unit clause
        Queue<Integer>  literals_to_propogate;

        if(units_to_propogate==null){
            literals_to_propogate = new ArrayDeque<Integer>();

            // initial literal to propogate is the one that was just added to the partial assignment
            literals_to_propogate.add(PartialAssignment.get(PartialAssignment.size()-1));
        }else{
            literals_to_propogate=units_to_propogate;
        }
        
        
        while( !literals_to_propogate.isEmpty()){


            Integer literal_to_propogate = literals_to_propogate.remove();
            Integer affected_literal = -literal_to_propogate;
            // System.out.println("Affected Literal: "+ affected_literal);
            // System.out.println("Before adding unit: ");
            // OutputWatchedLiterals();
            ArrayList<Integer> watched_clauses=new ArrayList<Integer>(Literal_To_Clause.get(affected_literal));
            AddToPartialAssignment(literal_to_propogate);
            // System.out.println("After adding unit: ");
            // OutputWatchedLiterals();
            // the affected literal is the negation of the last item we added to the propogation stack
            // this is the literal that has just been assigned false

            


            // check the status of every clause where the affected literal is 
            // a watch literal 
            for( Integer clause_index: watched_clauses){//Literal_To_Clause.get(affected_literal)){

                Integer watch_literal_1 = Clause_To_Literal.get(clause_index)[0];
                Integer watch_literal_2 = Clause_To_Literal.get(clause_index)[1];

                
                System.out.println("w1  "+ watch_literal_1 +" :   w2  "+ watch_literal_2);

               // the clause is not a unit clause, and is not a conflict
                if( watch_literal_1 !=0 && watch_literal_2!=0){
                    continue;
                }

                // we have reached a conflict
                if( watch_literal_1==0 && watch_literal_2 ==0 ) {
                    return "CONFLICT";
                }

                
                // we have a unit clause
                // we need to add the unit to our queue in order to propogate it

                if(watch_literal_1!=0){
                    if(!PartialAssignment.contains(watch_literal_1)){
                        literals_to_propogate.add(watch_literal_1);
                    }
                }
                else if ( watch_literal_2!=0){
                    if(!PartialAssignment.contains(watch_literal_2)){
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
        System.out.println("PA SIZE " + PartialAssignment.size());


        // update two-watch literal data structure 

        // this is the literal that has been made false
        Integer affected_literal = -literal;

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
        //OutputWatchedLiterals();

    }

    // switches the literal being watched to an undefined or true literal
    // returns 0 if no other valid watch literal exists 
    private static int SwitchWatchLiteral(Integer current_watch_literal,Integer other_watch_literal,HashSet<Integer> clause){
        for(int literal: clause){
            if ( literal!=other_watch_literal && !PartialAssignment.contains(-literal) && literal!=current_watch_literal){
                return literal;
            }
        }
        return 0;
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
                NumClauses-=1;
                
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
                    
                    NumClauses = Integer.parseInt(clauses);
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