package cn.larry.paxos;

public class Demo {



    public static void main(String[] args) {
        String s1 = "22";
        String s2 = s1;
        s2 = s2.replaceFirst("2","1");
        System.out.println(s1);
        System.out.println(s2);
        
    }
}
