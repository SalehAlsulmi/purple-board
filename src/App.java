import javax.swing.JFrame;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Purple Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1020, 700);
        frame.setLocationRelativeTo(null);

        MainPanel mainPanel = new MainPanel();
        frame.add(mainPanel);

        frame.setVisible(true);
    }
}