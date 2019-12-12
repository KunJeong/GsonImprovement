package com.mycompany.app;

/**
 * Hello world!
 * https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html
 */
public class App
{
    public static void main(String[] args)
    {
        System.out.println( "Hello World!" );
    }

    static final int INVALID = 0;
    static final int SCALENE = 1;
    static final int EQUALATERAL = 2;
    static final int ISOCELES = 3;

    public static int classifyTriangle(int a, int b, int c) {

        delay();

        // Sort the sides so that a <= b <= c
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }

        if (a > c) {
            int tmp = a;
            a = c;
            c = tmp;
        }

        if (b > c) {
            int tmp = b;
            b = c;
            c = tmp;
        }

        if (a + b <= c) {
            return INVALID;
        } else if (a == b && b == c) {
            return EQUALATERAL;
        } else if (a == b || b == c) {
            return ISOCELES;
        } else {
            return SCALENE;
        }

    }

    private static void delay() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

    }

}
