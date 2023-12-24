import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;

public class Main {

    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 768;
    public static final int STUDENT = 0;
    public static final int ADMIN = 1;
    public static final int CHARGER = 2;
    public static final CardLayout cardLayout = new CardLayout(0,0);
    public static final JPanel cardPanel = new JPanel(cardLayout);

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("校园志愿系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(false);
        frame.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
        frame.add(cardPanel);


        Login login = new Login(frame);

        cardPanel.add(login.getMainPanel());
        frame.setVisible(true);

    }
}