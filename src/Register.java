import javax.swing.*;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Objects;

public class Register {

    private JPanel registerPanel;
    private JPanel usernamePanel;
    private JPanel passwordPanel;
    private JPanel buttonPanel;
    private JLabel usernameLabel;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JComboBox selectType;
    private JButton regBtn;
    private JPanel namePanel;
    private JTextField nameTextField;
    private JLabel nameLabel;
    public static final int WIDTH = 512;
    public static final int HEIGHT = 768;
    private int userType = -1;

    public Register(JFrame frame) {

        // 选择用户类型
        selectType.addActionListener(e -> {
            String option = selectType.getSelectedItem().toString();
            if (Objects.equals(option, "学生")) {
                userType = Main.STUDENT;
            } else if (Objects.equals(option, "管理员")) {
                userType = Main.ADMIN;
            } else if (Objects.equals(option, "负责人")) {
                userType = Main.CHARGER;
            }
        });

        regBtn.addActionListener(e -> {
            //获取账号
            String username = usernameTextField.getText();
            if (username.length() != 10) {
                JOptionPane.showMessageDialog(frame, "用户名必须为10位", "Notice", JOptionPane.ERROR_MESSAGE);
                throw new IllegalArgumentException();
            }
            System.out.println(username);
            String password = String.valueOf(passwordTextField.getPassword());
            String name = nameTextField.getText();
            SqlConnector sc = SqlConnector.getInstance();
            try {
                CallableStatement callableStatement = sc.getConnection().prepareCall("{call createNewUser(?, ?, ?, ?, ?)}");

                // 设置输入参数
                callableStatement.setString(1, username);
                callableStatement.setString(2, password);
                callableStatement.setString(3, name);
                callableStatement.setInt(4, userType); // 设置用户类型
                callableStatement.registerOutParameter(5, Types.CHAR); // 注册输出参数

                // 执行存储过程
                callableStatement.execute();

                // 获取输出参数的值
                String rgtResult = callableStatement.getString(5);

                if (Objects.equals(rgtResult, "创建新用户失败")) {
                    JOptionPane.showMessageDialog(frame, "注册失败", "Notice", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException("创建新用户失败");
                } else{
                    JOptionPane.showMessageDialog(frame, rgtResult, "Notice", JOptionPane.INFORMATION_MESSAGE);
                }
                callableStatement.close();
                Main.cardLayout.previous(Main.cardPanel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

    }

    public JPanel getMainPanel() {
        return registerPanel;
    }
}
