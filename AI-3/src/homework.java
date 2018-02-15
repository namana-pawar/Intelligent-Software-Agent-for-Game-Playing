import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class homework {
    static int line = 0, sizeQuery = 0, sizeSentence = 0;
    static String sCurrentLine;
    static ArrayList<String> queries = new ArrayList<>();
    static ArrayList<String> sentences = new ArrayList<>();
    static HashMap<String, ArrayList<String>> predicate = new HashMap<>();
    static HashMap<String, String> groundTerms = new HashMap<>();
    static String currentQuery;
    static boolean first = true;
    static ArrayList<Boolean> result = new ArrayList<>();

    public static void main(String Args[]) throws IOException {
        File file = new File("/Users/namanapawar/Documents/AI/input.txt");
        Scanner s = new Scanner(file);
        while (s.hasNext() && (sCurrentLine = s.nextLine()) != null) {
            line++;
            if (line == 1) {
                sizeQuery = Integer.parseInt(sCurrentLine);
            } else if (line == sizeQuery + 2) {
                sizeSentence = Integer.parseInt(sCurrentLine);
            } else {

                if (sizeSentence == 0) {
                    queries.add(sCurrentLine);
                } else {
                    if (sCurrentLine.indexOf("|") == -1) sCurrentLine = " " + sCurrentLine;
                    sentences.add(sCurrentLine);
                }
            }
        }

        homework h = new homework();
        h.standardize();
        h.createHashMap();
        try {
            h.findMatch();
        }
        catch (StackOverflowError s1){
            result.add(false);
        }
        String output="";
        for(boolean x : result) {
            if(x) output=output+"TRUE\n";
            else output=output+"FALSE\n";
        }
        h.outputToFile(output);
    }
    private void outputToFile(String s) throws IOException {
        File file = new File("/Users/namanapawar/Documents/AI/output.txt");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(s);
        writer.flush();
        writer.close();
    }

    public void standardize() {
        for (int i = 0; i < sentences.size(); i++) {
            String s = sentences.get(i);
            int k = 0;
            String splitSentences[] = s.split("\\s\\|\\s");
            String s3 = "";
            for (int j = 0; j < splitSentences.length; j++) {
                k = splitSentences[j].indexOf('(');
                String var[] = splitSentences[j].substring(k + 1, splitSentences[j].length() - 1).split(",");
                for (int k2 = 0; k2 < var.length; k2++) {
                    if (Character.isLowerCase(var[k2].charAt(0))) {
                        String s1 = var[k2] + i;
                        String stemp=splitSentences[j].substring(splitSentences[j].indexOf("(")+1);
                        stemp = stemp.replace(var[k2], s1);
                        splitSentences[j]=splitSentences[j].substring(0,splitSentences[j].indexOf("(")+1)+stemp;
                    }
                }
                s3 = s3.concat(" " + splitSentences[j] + " |");
            }
            sentences.remove(i);
            s3 = s3.substring(1, s3.length() - 1);
            sentences.add(i, s3);
        }
    }
    public void createHashMap() {
        for (String s : sentences) {
            String sSplit[] = s.split("\\s\\|\\s");
            for (String s1 : sSplit) {
                if (!predicate.containsKey(s1)) {
                    ArrayList<String> p = new ArrayList<>();
                    s1 = s1.substring(0, s1.indexOf("("));
                    s1=s1.trim();
                    if(s1.charAt(0)!='~')s1=" "+s1;
                    for (String s2 : sentences) {
                        s2=" "+s2;
                        if (s2.indexOf(s1) != -1) {
                            p.add(s2);
                        }
                    }
                    predicate.put(s1, p);
                }
            }
        }
    }
    public void findMatch() throws IOException {
        for (String q : queries) {
            first=true;
            q = negate(q);
            currentQuery = q;
            currentQuery=currentQuery.trim();
            String q1=q.substring(0,q.indexOf("("));
            if(findMatchUtil(q))
                result.add(true);
            else
                result.add(false);
        }
    }
    public String negate(String q) {
        q=q.trim();
        if (q.charAt(0) == '~') q = " "+q.substring(1);
        else q = "~" + q;
        return q;
    }
    public boolean findMatchUtil(String q) throws IOException {
        q = q.trim();
        String qSplit[] = q.split("\\s\\|\\s");
        for (int i = 0; i < qSplit.length; i++) {
            if(qSplit[i].equals(currentQuery)&&!first){
                return false;
            }
            if(q.equals("")){
                return true;
            }
            String qSplit1=qSplit[i].substring(0,qSplit[i].indexOf("("));
            qSplit1=negate(qSplit1);
            if(!predicate.containsKey(qSplit1))return false;
            ArrayList<String> sFound = predicate.get(qSplit1);
            Iterator<String> iter = sFound.iterator();
            while (iter.hasNext()) {
                String q1 = "";
                String s = iter.next();
                String keys[] = unify(qSplit[i], s, s.indexOf(qSplit1));
                s = setGroundTerms(s, keys);
                if (s.compareTo("skip") == 0) {
                    continue;
                } else {
                    q1 = setGroundTerms(q, keys);
                    qSplit[i]=setGroundTerms(qSplit[i],keys);
                    q1 = resolve(qSplit[i], s, q1);
                }
                first = false;
                if(findMatchUtil(q1))
                    return true;
            }
        }
        return false;
    }
    public String[] unify(String q,String s,int index){
        groundTerms=new HashMap<>();
        int sIndex=s.indexOf('(',index);
        int eIndex=s.indexOf(')',index);
        String var=s.substring(sIndex+1,eIndex);
        String keys[]=var.split(",");
        getGroundTerms(q,keys);
        return keys;
    }
    public String setGroundTerms(String s,String keys[]){

        String s_temp = new String(s);
        for (int i=0;i<keys.length;i++) {
            if(Character.isUpperCase(groundTerms.get(keys[i]).charAt(0))&&Character.isUpperCase(keys[i].charAt(0))&&!groundTerms.get(keys[i]).equals(keys[i]))
                return "skip";
            else if(Character.isLowerCase(groundTerms.get(keys[i]).charAt(0))&&Character.isLowerCase(keys[i].charAt(0))){
                s_temp = s_temp.replace(keys[i], groundTerms.get(keys[i]));
            }
            else if(Character.isUpperCase(groundTerms.get(keys[i]).charAt(0))&&Character.isLowerCase(keys[i].charAt(0))) {
                s_temp = s_temp.replace(keys[i], groundTerms.get(keys[i]));
            }
            else if (Character.isLowerCase(groundTerms.get(keys[i]).charAt(0))&&Character.isUpperCase(keys[i].charAt(0))) {
                s_temp = s_temp.replace(groundTerms.get(keys[i]),keys[i]);
            }
        }
        return s_temp;
    }
    public void getGroundTerms(String q,String keys[]) {
        int startIndex = q.indexOf("(");
        int endIndex = q.indexOf(")");
        if ((q.indexOf(',')) == -1) {
            groundTerms.put(keys[0], q.substring(startIndex + 1, endIndex));
        } else {
            String var[] = q.substring(startIndex + 1, q.length() - 1).split(",");
            for (int i = 0; i < keys.length; i++) {
                String v = var[i];
                groundTerms.put(keys[i], v);
            }
        }
        for (int i = 0; i < keys.length; i++) {
            System.out.println(keys[i]+":"+groundTerms.get(keys[i]));
        }
    }
    public String resolve(String qSplit,String s,String q){
        s=s.trim();
        String resolved="";
        String negQSplit="";
        if (qSplit.charAt(0) != '~')
            negQSplit = "~" + qSplit.substring(0);
        else negQSplit = qSplit.substring(1);
        String qSplit1[]=q.split("\\s\\|\\s");
        String sSplit[]=s.split("\\s\\|\\s");

        for(String qs:qSplit1) {
            if (!qs.equals(qSplit))
                resolved = resolved + qs + " | ";
        }
        for(String ss:sSplit) {
            if (!ss.equals(negQSplit))
                resolved = resolved + ss + " | ";
        }
        if(resolved.indexOf("|")==1)resolved=resolved.substring(3);
        resolved=resolved.substring(0,resolved.lastIndexOf(')')+1);
        return resolved;
    }

}