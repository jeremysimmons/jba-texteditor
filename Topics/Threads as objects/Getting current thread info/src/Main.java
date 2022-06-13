class Info {

    public static void printCurrentThreadInfo() {
        // get the thread and print its info
        var ct = Thread.currentThread();
        System.out.format("name: %s%n", ct.getName());
        System.out.format("priority: %d%n", ct.getPriority());

    }
}