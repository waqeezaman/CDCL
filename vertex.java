import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public  class vertex{


    List<vertex > Parents;
    List<vertex> Children;

    Integer DecisionLevel;

    // if literal is 0, this is a conflict node
    Integer Literal;

    //Integer

    public vertex(Integer literal, Integer dl){
        Literal=literal;
        DecisionLevel=dl;

        Parents = new ArrayList<>();
        Children = new ArrayList<>();
    }

    public void AddParent(vertex parent){
        Parents.add(parent);
    }

    public void AddChild(vertex child){
        Children.add(child);
    }

    public HashSet<vertex> GetRoots(){
        HashSet<vertex> roots= new HashSet<vertex>();
        
        roots=this.FindRoots(roots);

        return roots;
        
    }

    public static HashSet<Integer> GetConflictClause(HashSet<vertex> clause){
        HashSet<Integer> conflictclause= new HashSet<>();

        for(vertex rootnode:clause){
            conflictclause.add(-rootnode.Literal);
        }
        return conflictclause;

    }

    public static Integer GetLowestDecisionLevel(HashSet<vertex> roots){
        Integer minDecisionLevel=99999;
        for(vertex root: roots){
            if( root.DecisionLevel<minDecisionLevel){
                minDecisionLevel=root.DecisionLevel;
            }
        }
        return minDecisionLevel;

    }

    public HashSet<vertex> FindRoots(HashSet<vertex> roots){


        if( this.Parents.size()==0){
            HashSet<vertex> root=new HashSet<vertex>();
            root.add(this);
            return root;
        }else{
            for(vertex p : Parents){
                roots.addAll(p.FindRoots(roots));
            }
            return roots;
        }
    }

} 