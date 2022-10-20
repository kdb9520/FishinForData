public class Helpers {
    public static int getOption(int max) {
        while (true) {
            try {
                int input = Integer.parseInt(
                        MainClass.in.nextLine());
                if (input >= 0 && input <= max) {
                    System.out.println();
                    return input;
                }
            } catch (Exception ignored) { }
            System.out.print("Invalid input, try again: ");
        }
    }
}
