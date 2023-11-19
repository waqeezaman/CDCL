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



    public static void main(String[] args)throws Exception{
            
        
        ReadClauses();

        

       

        
        OutputClauses(Clauses);


        EliminateTautologies();
        //Clauses = RemoveAllDoubleLiterals(Clauses);
        
        // implement pure literal optimisation here

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

        }
        

        return "";
    } 


    private static void UnitPropogate(){

        
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