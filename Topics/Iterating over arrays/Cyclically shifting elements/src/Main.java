import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // put your code here
        int length = scanner.nextInt();
        var items = new Integer[length];
        for(int i = 0; i < length; i++) {
            items[i] = scanner.nextInt();
        }
        var last = items[items.length - 1];
        var changed = new int[length];
        for (int i = 0; i < length - 1; i++) {
            changed[i + 1] = items[i];
        }
        changed[0] = last;
        for (int i = 0; i < length; i++) {
            System.out.print(changed[i]);
            System.out.print(" ");

        }
    }
}