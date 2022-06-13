import java.util.*;

public class Main {
    public static void main(String[] args) {
        // write your code here
        var s = new Scanner(System.in);
        var num = s.nextInt();
        var incomes = new int[num];
        for (int i = 0; i < num; i++) {
            incomes[i] = s.nextInt();
        }
        var taxes = new int[num];
        for (int i = 0; i < num; i++) {
            taxes[i] = s.nextInt();
        }

        var maxTax = 0.0;
        var highest = 0;
        for (int i = 0; i < num; i++) {
            var totalTax = incomes[i] * (taxes[i]/100.0);
            if(totalTax > maxTax) {
                maxTax = totalTax;
                highest = i;
            }
        }
        System.out.println(highest + 1); // 0-based ordinal to 1-based
    }
}