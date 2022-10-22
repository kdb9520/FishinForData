public class Helpers {
    public static final int DELETE = 999;

    public static int getOption(int max) {
        return getOption(max, false);
    }

    public static int getOption(int max, boolean deleteOk) {
        while (true) {
            try {
                int input = Integer.parseInt(
                        MainClass.in.nextLine());
                if ((input >= 0 && input <= max) ||
                        (deleteOk && input == DELETE)) {
                    System.out.println();
                    return input;
                }
            } catch (Exception ignored) { }
            System.out.print("Invalid input, try again: ");
        }
    }
}
