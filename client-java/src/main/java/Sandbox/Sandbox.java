package Sandbox;

public class Sandbox {
  public static void main(String[] args) {
    System.out.println(
        "You read: \"Clo?ed ?or in?entory\". lbalajla"
            .matches(
                ".*You read: \"[C?][l?][o?][s?][e?][d?] [f?][o?][r?]"
                    + " [i?][n?][v?][e?][n?][t?][o?][r?][y?]\"\\..*"));
  }
}
