import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.*;

public class UserPage {
    private static final int ACCEPT = 1;
    private static final int REJECT = 0;
    private static final int INIT = -1;
    private final SqlConnector sc = SqlConnector.getInstance();
    private ResultSet rs;
    private JPanel userpage;
    private JButton flush;
    private JButton applyForActivity;
    private JPanel buttonPanel;
    private JPanel showPanel;
    private JButton myApplyBtn;
    private JScrollPane activitylist;
    private JLabel myInfoLabel;
    private JButton signBtn;
    private JTable activityTable;
    private JButton backBtn;
    private String stu_id;
    private DefaultTableModel tableModel;
    private int act_id = -1;
    private boolean isActivityList = true;

    public UserPage(JFrame frame, String stu_id) {
        this.stu_id = stu_id;
        setInfo();
        System.out.println("账号：" + stu_id);
        String[] columnName = {"编号", "活动", "人数上限", "已参加人数", "地点", "活动时间", "持续时间", "要求", "状态"};
        tableModel = new DefaultTableModel(columnName, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        getActivityData();

        //刷新信息
        flush.addActionListener(e -> {
            tableModel.setRowCount(0);
            if (isActivityList) {
                getActivityData();
            } else {
                getMyApplyList();
            }
        });

        backBtn.addActionListener(e -> {
            if (!isActivityList) {
                isActivityList = true;
                getActivityData();
            }
        });

        //申请活动
        applyForActivity.addActionListener(e -> {
            if (!isActivityList) {
                JOptionPane.showMessageDialog(null, "请在活动列表下使用", "Warning", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("非法按键");
            }
            if (act_id == -1) {
                JOptionPane.showMessageDialog(null, "请选择活动", "Warning", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("未选择活动");
            }
            try {
                CallableStatement callableStatement = sc.getConnection().prepareCall("{call applyForActivity(?, ?, ?)}");

                // 设置输入参数
                callableStatement.setString(1, stu_id);
                callableStatement.setInt(2, act_id);
                callableStatement.registerOutParameter(3, Types.CHAR); // 注册输出参数

                // 执行存储过程
                callableStatement.execute();

                // 获取输出参数的值
                String applyResult = callableStatement.getString(3);
                JOptionPane.showMessageDialog(frame, applyResult, "Notice", JOptionPane.INFORMATION_MESSAGE);

                callableStatement.close();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "申请失败", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(ex);
            }
        });

        //选中想要申请的活动，获得act_id
        activityTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = activityTable.getSelectedRow();
                if (row != -1) {
                    act_id = Integer.parseInt(activityTable.getValueAt(row, 0).toString());
                    System.out.println("选中序号：" + act_id);

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        //查看我的申请
        myApplyBtn.addActionListener(e -> {
            isActivityList = false;
            getMyApplyList();
        });
        //签到
        signBtn.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (isActivityList) {
                        JOptionPane.showMessageDialog(null, "请在“我的申请”界面中使用该按键", "Error", JOptionPane.ERROR_MESSAGE);
                        throw new RuntimeException("非法按键");
                    }
                    CallableStatement callableStatement = sc.getConnection().prepareCall("{call stuSignUp(?, ?,?)}");
                    // 设置输入参数
                    callableStatement.setString(1, stu_id);
                    callableStatement.setInt(2, act_id);
                    callableStatement.registerOutParameter(3, Types.CHAR);
                    callableStatement.execute();
                    String signResult = callableStatement.getString(3);
                    JOptionPane.showMessageDialog(null, signResult, "申请结果", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }


            }
        });
    }

    /**
     * 设置个人信息的显示
     */
    private void setInfo() {
        String sql = "select * from users where stu_num = {0}".replace("{0}", stu_id);
        try {
            rs = sc.execSql(sql);
            if (rs != null && rs.next()) {
                String showText = String.format("学号：%s  姓名：%s  志愿时长；%s小时  出勤率：%s", rs.getString("stu_num"), rs.getString("realname"), rs.getString("volun_time"), rs.getString("attend_rate") + "%");
                myInfoLabel.setText(showText);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 从数据库读取活动数据并加载到table中
     */
    private void getActivityData() {
        //生成JTable
        tableModel.setRowCount(0);
        activityTable.setModel(tableModel);
        activitylist.setViewportView(activityTable);
        try {
            rs = sc.execSql("select * from activity, place where activity.place_id = place.place_id");
            if (rs != null) {
                while (rs.next()) {
                    String state;
                    switch (rs.getInt("act_state")) {
                        case 0:
                            state = "可用";
                            break;
                        case 1:
                            state = "人满";
                            break;
                        case 2:
                            state = "过期";
                            break;
                        default:
                            state = "未知";
                            break;
                    }
                    Object[] obj = {rs.getString("act_id"), rs.getString("act_name"), rs.getString("participant_num"), rs.getString("join_num"), rs.getString("name"), rs.getString("act_time"), rs.getString("lasting_time"), rs.getString("act_request"), state};
                    tableModel.addRow(obj);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getMyApplyList() {
        String[] columnName = {"编号", "活动", "活动状态", "审核状态", "是否签到"};
        DefaultTableModel applyModel = new DefaultTableModel(columnName, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        activityTable.setModel(applyModel);
        activitylist.setViewportView(activityTable);
        try {
            rs = sc.execSql("select * from apply_list natural join activity where apply_list.stu_num = '{0}'".replace("{0}", stu_id));
            if (rs != null) {
                while (rs.next()) {
                    String apply_state;
                    switch (rs.getInt("apply_state")) {
                        case REJECT:
                            apply_state = "不通过";
                            break;
                        case ACCEPT:
                            apply_state = "通过";
                            break;
                        case INIT:
                            apply_state = "待审核";
                            break;
                        default:
                            apply_state = "未知";
                            break;
                    }
                    String act_state;
                    switch (rs.getInt("act_state")) {
                        case 0:
                            act_state = "可用";
                            break;
                        case 1:
                            act_state = "人满";
                            break;
                        case 2:
                            act_state = "过期";
                            break;
                        default:
                            act_state = "未知";
                            break;
                    }
                    String sign_state = "----";
                    if (apply_state.equals("通过")) {
                        Connection tmpConn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/vol_sys", "root", "lin_dcyy123");
                        Statement tmpStmt = tmpConn.createStatement();
                        String sql = String.format("select present from participating where stu_num = '%s' and act_id = %d", stu_id, rs.getInt("act_id"));
                        System.out.println(sql);
                        ResultSet rs1 = tmpStmt.executeQuery(sql);
                        if(rs1!=null && rs1.next()){
                            switch (rs1.getInt("present")) {
                                case 0:
                                    sign_state = "未签到";
                                    break;
                                case 1:
                                    sign_state = "已签到";
                                    break;
                                default:
                                    sign_state = "未知";
                                    break;
                            }
                        }
                        tmpStmt.close();
                        tmpConn.close();
                    }
                    Object[] obj = {rs.getString("act_id"), rs.getString("act_name"), act_state, apply_state, sign_state};
                    applyModel.addRow(obj);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public JPanel getMainPanel() {
        return userpage;
    }
}

