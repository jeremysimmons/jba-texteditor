class Starter {

    public static void startRunnables(Runnable[] runnables) {
        // implement the method
        for (Runnable r : runnables) {
            var t = new Thread(r);
            t.start();
        }
    }
}