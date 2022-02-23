package com.example.telegrambothungmb.bots;

import java.util.Arrays;

public class Test2Pointer {

    public static void main(String[] args) {
        int[] a = {-4,-1,0,1,3,9};

        int[] c = new int[a.length];

        int i = 0;
        int j = a.length-1;
        int counter = j;

        while(i < j){

            if(sq(a[i]) < sq(a[j])){
                c[counter] = sq(a[j]);
                j--;
            } else {
                c[counter] = sq(a[i]);
                i++;
            }
            counter--;
        }

        System.out.println(Arrays.toString(c));
    }

    static int sq(int a){
        return a*a;
    }
}
