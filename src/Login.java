import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    private JPanel LoginPanel;
    private JPanel pwdPanel;
    private JPanel btnPanel;
    private JButton loginBtn;
    private JButton registerBtn;
    private JPanel idPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;


    public Login(JFrame frame) {
        SqlConnector sc = SqlConnector.getInstance();
        Main.cardLayout.last(Main.cardPanel);
        loginBtn.addActionListener(e -> {
            String username = usernameField.getText();
            System.out.println("username: " + username);
            String password = new String(passwordField.getPassword());
            System.out.println("password: " + password);
            String sql = "select pwd, user_type from register_data where user_id = {0}";
            sql = sql.replace("{0}", username);
            try {
                ResultSet rs = sc.execSql(sql);
                if (rs.next()) {
                    System.out.println("pwd from sql: " + rs.getString("pwd"));
                    if (rs.getString("pwd").equals(password)) {
                        int usertype = Integer.parseInt(rs.getString("user_type"));
                        if (usertype == Main.STUDENT) {
                            // 跳转到学生界面
                            JOptionPane.showMessageDialog(frame, "登录学生成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                            UserPage userPage = new UserPage(frame, username);
                            Main.cardPanel.add(userPage.getMainPanel());
                            Main.cardLayout.last(Main.cardPanel);
                        } else if (usertype == Main.ADMIN) {
                            //TODO 跳转到管理员界面
                            JOptionPane.showMessageDialog(frame, "登录管理员成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                            AdminPage userPage = new AdminPage(frame, username);
                            Main.cardPanel.add(userPage.getMainPanel());
                            Main.cardLayout.last(Main.cardPanel);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "用户名或密码错误", "Error", JOptionPane.ERROR_MESSAGE);
                        passwordField.setText("");
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        //注册
        registerBtn.addActionListener(e -> {
            Register rgt = new Register(frame);
            Main.cardPanel.add(rgt.getMainPanel());
            Main.cardLayout.last(Main.cardPanel);
        });
    }


    public JPanel getMainPanel() {
        return LoginPanel;
    }
}
