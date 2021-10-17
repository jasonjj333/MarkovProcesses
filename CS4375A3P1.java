import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
public class CS4375A3P1 {
    static ArrayList<Node>[] list;
    static ArrayList<String> choiceList;
    public static void main(String[] args) throws FileNotFoundException{
        //Reading file name through IOStream (needs to be changed)
        // Scanner scan = new Scanner(System.in);
        // System.out.print("Name of training data file: ");
        // String trainFileName = scan.nextLine();

        //reading from terminal
        int counter = 0;
        int numStates = 0;
        int numChoices = 0;
        String trainFileName = "";
        double decay = 0;
        
        for(String s: args) {
            if(counter == 0) {
                numStates = Integer.parseInt(s);
                counter++;
            }
            else if(counter == 1) {
                numChoices = Integer.parseInt(s);
                counter++;
            }
            else if(counter == 2) {
                trainFileName = s;
                counter++;
            }
            else if(counter == 3) {
                decay = Double.parseDouble(s);
                counter++;
            }
            else 
                System.out.println("Too many arguments inputted in Console");

        }
        Scanner file = new Scanner(new File(trainFileName));
        int count = 0;

        //hardcode decay value for now (Change later to user selected decay)
        //decay = .9;
        //hardcode number of choices for now (Change later to user selected) (May not need as code does not need user specification)
        //int numChoices = 2;
        //find how many states there are (Will not need user specification)
        while(file.hasNextLine()) {
            file.nextLine();
            count++;
        }
        
        //initialize array of arrayLists
        list = new ArrayList[count];   //array of arrayLists, where each array is a different class,
        for (int i = 0; i < count; i++) {                   //containing the different values of their respective class
            list[i] = new ArrayList<Node>();
        }
        choiceList = new ArrayList<String>();
        int tempCounter = 0;
        Scanner newFile = new Scanner(new File(trainFileName));
        
        while(newFile.hasNextLine()) {
            String line = newFile.nextLine();
            int spaceChecker = line.indexOf(" ");
            String currentName = line.substring(0, spaceChecker);
            int rewardEndPoint = line.indexOf("(");
            int reward = Integer.parseInt(line.substring(spaceChecker+1,rewardEndPoint-1));
            Node currentNode = new Node(currentName, reward);
            list[tempCounter].add(currentNode);
            line = line.substring(rewardEndPoint+1);
            boolean checker = true;
            while(checker) {
                //System.out.print(line.length() + " ");
                if(line.indexOf(")") == -1)
                    checker = false;
                else {
                    int spaceIndex = line.indexOf(" ");
                    String choice = line.substring(0, spaceIndex);
                    if(!choiceList.contains(choice))
                        choiceList.add(choice);
                    line = line.substring(spaceIndex+1);
                    spaceIndex = line.indexOf(" ");
                    String end = line.substring(0, spaceIndex);
                    line = line.substring(spaceIndex+1);
                    spaceIndex = line.indexOf(")");
                    double prob = Double.parseDouble(line.substring(0,spaceIndex));
                    line = line.substring(spaceIndex);
                    Node branch = new Node(choice, end, prob);
                    list[tempCounter].add(branch);
                    if(line.indexOf("(") != -1) {
                        line = line.substring(line.indexOf("(")+1);
                    }
                    else
                        checker = false;
                    
                }
            }
            tempCounter++;
        }
        
        // //test to see if correctly read and analyzed file
        // for(int i = 0; i < count; i++) {
        //     for(int u = 0; u < list[i].size(); u++) {
        //         System.out.println(list[i].get(u).print());
        //     }
        // }
        // //test to see if choice List contains all possible choices
        // for(int i = 0; i < choiceList.size(); i++ ) {
        //     System.out.print(choiceList.get(i) + " ");
        // }
        // System.out.println();
        
        //Start MDP
        ArrayList<Double> jValues = new ArrayList<Double>();
        ArrayList<Double> newJValues = new ArrayList<Double>();
        ArrayList<String> maxAValues = new ArrayList<String>();
        double maxDecision = -1000.0;
        String maxChoice = "a1";
        //Initialize jValues with J1 values
        for(int i = 0; i < list.length; i++) {
            jValues.add((double)list[i].get(0).reward);
        }
        //Find find first iteration
        System.out.println("After iteration 1:");
        for(int u = 0; u < list.length; u++) {
            //to add each choice available
            for(int z = 0; z < choiceList.size(); z++) {
                double newJValue = calculateJValue(u, choiceList.get(z), jValues);
                if(newJValue > maxDecision) {
                    maxDecision = newJValue;
                    maxChoice = choiceList.get(z);
                }
            }
            System.out.print("(" + list[u].get(0).state + " " + maxChoice + " " + (double) (Math.round(list[u].get(0).reward*10000.0)/10000.0) + ") ");
            maxDecision = -10000.0;
            maxChoice = "a1";
        }
        System.out.println();
        //clear 
        maxDecision = -1000.0;
         maxChoice = "a1";
        //Finding next 19 iterations
        for(int i = 1; i < 20; i ++) {
            //go through each state
            for(int u = 0; u < list.length; u++) {
                //to add each choice available
                for(int z = 0; z < choiceList.size(); z++) {
                    double newJValue = calculateJValue(u, choiceList.get(z), jValues);
                    if(newJValue > maxDecision) {
                        maxDecision = newJValue;
                        maxChoice = choiceList.get(z);
                    }
                }
                maxDecision = ((double)decay * maxDecision) + list[u].get(0).reward;
                newJValues.add(maxDecision);
                maxAValues.add(maxChoice);
                maxDecision = -1000.0;
                maxChoice = "a1";
            }
            System.out.println("After iteration " + (i+1) + ": ");
            for(int temp = 0; temp < list.length; temp++) {
                System.out.print("(" + list[temp].get(0).state + " " + maxAValues.get(temp) + " " + (double) (Math.round(newJValues.get(temp)*10000.0)/10000.0) + ") ");
                jValues.remove(0);
            }
            System.out.println();

            //add new Jvalues into current jValues and clear aValues
            for(int a =0; a < list.length; a++) {
                jValues.add(newJValues.get(a));
                maxAValues.remove(0);
            }
            //clear newJValues
            for(int b =0; b < list.length; b++) {
                newJValues.remove(0);
            }

        }
        
        
    }
    public static double calculateJValue(int stateIndex, String choice, ArrayList<Double> jValues) {
        double sum = 0.0;
        boolean checker = false;
        //go through each probability recorded in the state
        for(int i = 1; i < list[stateIndex].size(); i++) {
            if(list[stateIndex].get(i).choice.equals(choice)) {
                int endIndex = indexOfState(list[stateIndex].get(i).endNode);
                sum+= (list[stateIndex].get(i).probability * jValues.get(endIndex));
                checker = true;
            }
        }
        if(checker)
            return sum;
        return -10000.0;
    }

    public static int indexOfState(String state) {
        for(int i = 0; i < list.length; i++) {
            if(list[i].get(0).state.equals(state))
                return i;
        }
        System.out.println("Could not find state name in list");
        return -1;
    }

}

class Node{
    public String state;
    public String choice;
    public String endNode;
    public int reward;
    public double probability;
    boolean hasReward;
    
    public Node(String start, int rewd) {
        state = start;
        reward = rewd;
        hasReward = true;
    }
    
    public Node(String start, String end, double prob) {
        choice = start;
        endNode = end;
        probability = prob;
        hasReward = false;
    }
    public String print() {
        if(hasReward)
            return (state + " " + reward);
        else
            return (choice + " " + endNode + " " + probability);
    }
}

