import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    /* Do not change code below */
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        var elements = scanner.nextInt();
        var el = new Integer[elements];
        for (int i = 0; i <= elements; i++) {
            el[i] = scanner.nextInt();
        }
        var last = el[el.length];
        for (int i = 0; i <= elements; i++) {
            el[i + 1] = el[i];
        }
        el[0] = last;
        for (int e : el) {
            System.out.print(e);
            System.out.print(" ");
        }
    }
}