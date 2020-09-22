import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.GraphicsEnvironment;

public class App {

    public static void main(String[] args) throws Exception {

        // Create a .bat file when running .jar, which opens a console
        // for the program to run on
        Console console = System.console();
        if(console == null && !GraphicsEnvironment.isHeadless()) {
            String filename = App.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            try {
                File batch = new File("Launcher.bat");
                if(!batch.exists()){
                    batch.createNewFile();
                    PrintWriter writer = new PrintWriter(batch);
                    writer.println("@echo off");
                    writer.println("java -jar "+filename);
                    writer.println("pause");
                    writer.println("exit");
                    writer.flush();
                    writer.close();
                }
                Runtime.getRuntime().exec("cmd /c start \"\" "+batch.getPath());
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            String anagram = "documenting";
            String uri = "https://gist.githubusercontent.com/calvinmetcalf/084ab003b295ee70c8fc/raw/314abfdc74b50f45f3dbbfa169892eff08f940f2/wordlist.txt";
            String printPermutations = "false";

            System.out.println("\n" + "Anagram pair application");
            System.out.println("\n" + "Usable arguments for the application: ");
            System.out.println("\n" + "   1. anagram word for finding possible word pairings");
            System.out.println("\n" + "   2. (true/false) Generate and print all permutations for chosen anagram word (WARNING: having a long word as anagram can cause massive string array generation or OutOfMemoryError!)");
            System.out.println("\n" + "   3. URI for cross reference word text file");
            System.out.println("\n" + "Example: documenting false https://gist.githubusercontent.com/wordlist.txt (Parameters 2 and 3 are optional)");
            System.out.println("\n\n" + "Type a word to find possible anagram pairings: " + "\n");

            String[] arguments = System.console().readLine().split(" ");
            if (arguments.length >= 3) {
                anagram = arguments[0];
                printPermutations = arguments[1];
                if (printPermutations.contains("true")) {
                    System.out.println("\n" + "Generating permutations.. \n");
                    printMap(createPermutations(anagram));
                }
                uri = arguments[2];
            } else if (arguments.length == 2) {
                anagram = arguments[0];
                String perms = arguments[1].toString();
                if (perms.contains("true")) {
                    System.out.println("\n" + "Generating permutations.. \n");
                    printMap(createPermutations(anagram));
                }  
            } else if (arguments.length == 1) {
                if (!arguments[0].isEmpty()) {
                    anagram = arguments[0];
                } else {
                    System.out.println("No arguments given, running program with default word " + anagram + "\n");
                }
            } 

            System.out.println("\n" + "Using " + anagram + " as chosen word.. \n");

            // Get word map from chosen URI and filter the word results
            // based on anagram word
            HashMap<String, Boolean> wordMap = filterWordList(getTextFile(uri), anagram);
            
            // Get results from combining rest of wordlist together to create
            // two-paired anagrams from the given anagram word
            ArrayList<String> anagramList = getCombinedAnagrams(wordMap, anagram);

            
            // Below are method calls for creating anagram permutations and
            // for cross referencing the results to the given word list 

            //ArrayList<String> permList = createPermutations(anagram);
            //ArrayList<String> foundWordList = findWordsUsingPermutations(permList, wordMap);
            //printMap(foundWordList);
        } 
    }

    // Checks for detailed testing if a chosen word is in a hash map
    private static void isInMap(HashMap<String, Boolean> map, String word) {
        if (map.containsKey(word)) {
            System.out.println("Word found in map: " + word);
        } else {
            System.out.println(word + " not found in map!");
        }
    }

    // Prints out chosen hash map key values
    private static void printMap(HashMap<String, Boolean> map) {
        if (map.size() == 0) {
            return;
        }
        for (String key : map.keySet()) {
            System.out.println(key);
        }
    }

    // Prints out array list values
    private static void printMap(ArrayList<String> printList) {
        if(printList.size() == 0) {
            return;
        }
        for (var permkey : printList) {
            System.out.println(permkey);
        }
    }

    // Creates a GET request with given or default uri and returns the 
    // parsed result of the GET response
    private static HashMap<String, Boolean> getTextFile(String uri) throws Exception {
        System.out.println("Obtaining word list from URI: \n" + uri + "\n");
        try {
            HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String[] result = response.body().split("\n");
        
        for (int i=1;i<result.length;i++) {

            String[] word = result[i].split(" ");
            
            for(int e=0;e<word.length;e++) {
                if (word[e].length() == 0) {
                    continue;
                }
                map.put(word[e], true);
            }
        }
        return map;
        } catch (Exception e) {
            System.out.println("Attempt to get word list from " + uri + " failed! \n");
            System.out.println(e.fillInStackTrace());
        }
        //System.out.println(response.statusCode());
        return null;
    }

    // Cross checks generated anagram permutations with word list
    private static ArrayList<String> findWordsUsingPermutations(ArrayList<String> list, HashMap<String, Boolean> map) {
        ArrayList<String> newList = new ArrayList<String>();

        Iterator<String> iterator = map.keySet().iterator();

        while (iterator.hasNext()) {
            String mapKey = iterator.next();

            for (int i=0;i<list.size()-1;i++) {
                if (list.get(i).contains(mapKey)) {
                    if (newList.contains(mapKey)) {
                        iterator.remove();
                        continue;
                    }

                    newList.add(mapKey);
                    iterator.remove();
                    System.out.println("Found "+mapKey+"!");
                    break;
                }
            }
        }
        System.out.println("Word finding complete! Applicable words found: "+ newList.size());
        return newList;
    }

    // filters given hash map based on the anagram word's characters and character count
    private static HashMap<String, Boolean> filterWordList(HashMap<String, Boolean> list, String anagram) {

        System.out.println("Words in word list before filtering: " + list.size() + "\n");
        // Make a hashmap of the characters and amount of characters in anagramWord
        HashMap<Character, Integer> charMap = new HashMap<Character, Integer>();
        for (int i=0;i<anagram.length();i++) {
            char c = anagram.charAt(i);
            if (charMap.containsKey(c)) {
                charMap.put(c, charMap.get(c)+1);
            } else {
                charMap.put(c, 1);
            }
        }
        int removedWordByChar = 0;
        int removedWordByAmount = 0;

        // Filter words from the list based on wrong characters and amount of identical characters
        Iterator<String> iterator = list.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            for (int e=0;e<key.length();e++) {
                int index = e;
                if (!anagram.contains(Character.toString(key.charAt(e)))) {
                    removedWordByChar++;
                    iterator.remove();
                    //System.out.println("Char in word "+ key + " not found in word " + anagram);
                    break;
                } else if (key.chars().filter(ch -> ch == key.charAt(index)).count() > charMap.get(key.charAt(index))) {
                    removedWordByAmount++;
                    iterator.remove();
                    //System.out.println("Found "+ key + " with too many " +key.charAt(index)+ "-characters!");
                    break;
                }
            }
        }
        System.out.println("Filtered " + removedWordByChar + " words from word list for containing a wrong character. \n");
        System.out.println("Filtered " + removedWordByAmount + " words from word list for containing an additional correct character. \n");
        if (list.size() == 0) {
            System.out.println("No applicable words for anagram pair found after filtering word list. \n");
        } else {
            System.out.println(list.size()+ " applicable words for anagram pair found after filtering word list: " + "\n");
            printMap(list);
        }
        return list;
    }

    // Attempts to combine word list's words together to create the original anagram
    private static ArrayList<String> getCombinedAnagrams(HashMap<String, Boolean> words, String anagram) {

        if (words.size() == 0) {
            System.out.println("No anagram pairs found by using word "+anagram+"!");
            return null;
        }

        ArrayList<String> resultList = new ArrayList<String>();
        HashMap<String, Integer> charMap = new HashMap<String, Integer>();
        
        int unfittingCharCount = 0;
        int unavailableCharCount = 0;
        int additionalCharCount = 0;

        for(var word:words.keySet()) {

            // Clear and refresh value of the anagram hashmap and reduces the first
            // word from the hashmap character base

            // Refresh anagram hashmap
            charMap.clear();
            for (int i=0;i<anagram.length();i++) {
                String c = Character.toString(anagram.charAt(i));
                if (charMap.containsKey(c)) {
                    charMap.put(c, charMap.get(c)+1);
                } else {
                    charMap.put(c, 1);
                }
            }
            int nextSize = 0;

            // Remove first word from anagram hashmap
            for (int i=0;i<word.length();i++) {
                String c = Character.toString(word.charAt(i));
                
                if (charMap.containsKey(c)) {
                    charMap.put(c, charMap.get(c)-1);
                    nextSize++;
                }
            }
            nextSize = anagram.length()-nextSize;

            // Create a nested loop to check for all the word combinations
            for (var nextWord:words.keySet()) {

                // Check if the next word fits in reduced anagram
                if (nextWord.length()==nextSize) {
                    //System.out.println(word + " " + nextWord + " forms correct length!");
                    for (int e=0;e<nextWord.length();e++) {
                        int index = e; // Used for avoiding charAt-error message
                        
                        // Check if nextword contains a char that isn't available in anagram
                        if (!charMap.containsKey(Character.toString(nextWord.charAt(e)))) {
                            unavailableCharCount++;
                            //System.out.println(Character.toString(nextWord.charAt(e)) +" in word "+ nextWord + " not found in reduced anagram \n");
                            break;
                        } 
                        // Check if there's no more of a character left in reduced anagram
                        else if (charMap.get(Character.toString(nextWord.charAt(e)))<=0) {
                            unavailableCharCount++;
                            //System.out.println(Character.toString(nextWord.charAt(e)) + " is already used in first word " + word +"\n");
                            break;
                        } 
                        // Check if potential anagram pair word has too many of one character
                        else if (nextWord.chars().filter(ch -> ch == nextWord.charAt(index)).count() > charMap.get(Character.toString(nextWord.charAt(e)))) {
                            additionalCharCount++;
                            //System.out.println(nextWord + " has too many " +nextWord.charAt(e)+ "-characters! \n");
                            break;
                        }
                        // If all characters from potential word pass through the tests, create two worded anagram from resulting words
                        if (e == nextWord.length()-1)
                        {
                            String result = word + " " + nextWord;
                            //System.out.println("Added " + result + " to results");
                            resultList.add(result);
                            break;
                        }
                    }
                } else {
                    unfittingCharCount++;
                }
            }
        }
        System.out.println("\n" + unfittingCharCount + " word combinations removed from anagram pairs for containing wrong amount of characters. \n");
        System.out.println(unavailableCharCount + " word combinations removed from anagram pairs for having an already allocated character. \n");
        System.out.println(additionalCharCount + " word combinations removed from anagram pairs for containing an additional used character. \n");

        if (resultList.size()>0) {
            System.out.println(resultList.size() + " anagram pairs found: " + "\n");
            printMap(resultList);
        } else {
            System.out.println("No anagram pairs found by using word "+anagram+"!");
        }
        return resultList;
    }
    
    // Below is my own version of a permutation function

    /*private static ArrayList<String> getPermutationsToArray(String perm) {

        ArrayList<String> returnPerms = new ArrayList<String>();
        int length = perm.length();

        if (length > 2) {
            for (int i=0;i<length;i++) {

                // Unselects index character from the word
                // and continues the loop with the rest of the letters
                String startPerm = perm.substring(0, i);
                String endPerm = perm.substring(i+1);
                String nextPerm = startPerm.concat(endPerm);
                
                for (var p : getPermutationsToArray(nextPerm)) {
                    String result = Character.toString(perm.charAt(i));
                    System.out.println(result.concat(p));
                    returnPerms.add(result.concat(p));
                }
            }
        } else if (length == 2) {
            String turnPerm = Character.toString(perm.charAt(1)) + Character.toString(perm.charAt(0));
            ArrayList<String> results = new ArrayList<String>();
            results.add(perm);
            results.add(turnPerm);
            return results;
        }
        else {
            ArrayList<String> results = new ArrayList<String>();
            results.add(perm);
            return results;
        }
        return returnPerms;
    } */

    // Creates all permutations from chosen word
    private static ArrayList<String> createPermutations(String word) {

        if (word.length() == 0) {
            ArrayList<String> empty = new ArrayList<>();
            empty.add("");
            return empty;
         }
        char ch = word.charAt(0);
        String subStr = word.substring(1);
        ArrayList<String> lastCombination = createPermutations(subStr);
        ArrayList<String> newCombination = new ArrayList<>();
        for (String val : lastCombination) {
            for (int i=0;i<=val.length();i++) {
                newCombination.add(val.substring(0,i)+ch+val.substring(i));
            }
        }
        return newCombination;
    }
}
