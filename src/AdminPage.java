import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class AdminPage {
    private final SqlConnector sc = SqlConnector.getInstance();
    private ResultSet rs;
    private JPanel adminPanel;
    private JButton createActivityBtn;
    private JButton checkingBtn;
    private JTable adminTable;
    private JScrollPane adminScrollPane;
    private JLabel infoLabel;
    private JButton backBtn;
    private JButton acceptBtn;
    private JButton rejectBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton flushBtn;
    private DefaultTableModel tableModel;
    private int act_id = -1;
    private final int admin_id;
    private String selectedCol = null;
    private boolean isChecking = false;
    private int stu_num = -1;

    public AdminPage(JFrame frame, String username) {


        System.out.println("账号：" + username);
        admin_id = getAdminID(username);
        System.out.println("编号：" + admin_id);
        setInfo(username);


        //生成JTable
        String[] columnName = {"编号", "活动", "人数上限", "已参加人数", "地点", "活动时间", "持续时间", "状态"};
        tableModel = new DefaultTableModel(columnName, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        adminTable.setModel(tableModel);
        adminScrollPane.setViewportView(adminTable);
        getActivityData();
        //创建新活动
        createActivityBtn.addActionListener(e -> createNewActivity());

        adminTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = adminTable.getSelectedRow();
                int col = adminTable.getSelectedColumn();
                if (!isChecking) {
                    if (row != -1) {
                        act_id = Integer.parseInt(adminTable.getValueAt(row, 0).toString());
                        System.out.println("选中序号：" + act_id);
                    }
                    switch (col) {
                        case 1:
                            selectedCol = "act_name";
                            break;
                        case 2:
                            selectedCol = "participant_num";
                            break;
                        case 4:
                            selectedCol = "place_id";
                            break;
                        case 5:
                            selectedCol = "act_time";
                            break;
                        case 6:
                            selectedCol = "lasting_time";
                            break;
                        default:
                            selectedCol = null;
                            break;
                    }
                } else {
                    if (row != -1) {
                        stu_num = Integer.parseInt(adminTable.getValueAt(row, 0).toString());
                    }
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
        //点击活动并进行审核
        checkingBtn.addActionListener(e -> {
            if (isChecking) {
                JOptionPane.showMessageDialog(null, "请在活动管理模式下使用该按键", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("使用非法按键");
            }
            if (act_id == -1) {
                JOptionPane.showMessageDialog(frame, "请选择活动", "Notice", JOptionPane.INFORMATION_MESSAGE);
                throw new RuntimeException("未选择活动");
            }
            getApplyListData();
            isChecking = true;
        });
        //从审核列表返回活动列表
        backBtn.addActionListener(e -> {
            if (!isChecking) {
                JOptionPane.showMessageDialog(null, "请在审核模式下使用该按键", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("使用非法按键");
            }
            isChecking = false;
            act_id = -1;
            adminTable.setModel(tableModel);
            adminScrollPane.setViewportView(adminTable);
            getActivityData();
        });
        //刷新列表
        flushBtn.addActionListener(e -> {
            setInfo(username);
            if (!isChecking) {
                act_id = -1;
                getActivityData();
            } else {
                stu_num = -1;
                getApplyListData();
            }

        });
        //更新活动信息
        updateBtn.addActionListener(e -> {
            if (isChecking) {
                JOptionPane.showMessageDialog(null, "请在活动管理模式下使用该按键", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("使用非法按键");
            }
            updateActivity();
        });

        acceptBtn.addActionListener(e -> {
            if (!isChecking) {
                JOptionPane.showMessageDialog(null, "请在审核模式下使用该按键", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("使用非法按键");
            }
            int confirm = JOptionPane.showConfirmDialog(null, "通过该申请？", "确认", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    CallableStatement callableStatement = sc.getConnection().prepareCall("update apply_list set apply_state = 1 where stu_num = ? and act_id = ?");
                    callableStatement.setInt(1, stu_num);
                    callableStatement.setInt(2, act_id);
                    callableStatement.execute();
                    callableStatement.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(null, "已通过该申请", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        rejectBtn.addActionListener(e -> {
            if (!isChecking) {
                JOptionPane.showMessageDialog(null, "请在审核模式下使用该按键", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("使用非法按键");
            }
            int confirm = JOptionPane.showConfirmDialog(null, "拒绝该申请？", "确认", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    CallableStatement callableStatement = sc.getConnection().prepareCall("update apply_list set apply_state = 0 where stu_num = ? and act_id = ?");
                    callableStatement.setInt(1, stu_num);
                    callableStatement.setInt(2, act_id);
                    callableStatement.execute();
                    callableStatement.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(null, "已拒绝该申请", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            if (isChecking) {
                JOptionPane.showMessageDialog(null, "请在活动管理模式下使用该按键", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("使用非法按键");
            }
            int confirm = JOptionPane.showConfirmDialog(null, "是否删除该活动？", "确认", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    CallableStatement callableStatement = sc.getConnection().prepareCall("update activity set act_state = 2 where act_id = ?");
                    callableStatement.setInt(1, act_id);
                    callableStatement.execute();
                    callableStatement.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * 根据账号获取管理员编号
     *
     * @param username 账号
     */
    private int getAdminID(String username) {
        String sql = "select admin_id from register_data, admins where register_data.realname = admins.realname and user_id = {0}".replace("{0}", username);
        try {
            rs = sc.execSql(sql);
            if (rs != null && rs.next()) {
                return rs.getInt("admin_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }


    /**
     * 设置个人信息的显示
     *
     * @param username 账号
     */
    private void setInfo(String username) {
        String sql = "select * from admins, office where admins.office_id = office.office_id and admin_id = {0}".replace("{0}", String.valueOf(admin_id));
        try {
            rs = sc.execSql(sql);
            if (rs != null && rs.next()) {
                String showText = String.format("编号：%d  账号：%s  姓名：%s 单位：%s", admin_id, username, rs.getString("realname"), rs.getString("office_name"));
                infoLabel.setText(showText);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 从数据库读取活动数据并加载到table中
     */
    private void getActivityData() {
        tableModel.setRowCount(0);
        try {
            rs = sc.execSql("select * from pub_activity, activity, place where activity.act_id = pub_activity.act_id and activity.place_id = place.place_id and pub_activity.admin_id = {0}".replace("{0}", String.valueOf(admin_id)));
            if (rs != null) {
                while (rs.next()) {
                    String state;
                    switch (Integer.parseInt(rs.getString("act_state"))) {
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
                    Object[] obj = {rs.getString("act_id"), rs.getString("act_name"), rs.getString("participant_num"), rs.getString("join_num"), rs.getString("name"), rs.getString("act_time"), rs.getString("lasting_time"), state};
                    tableModel.addRow(obj);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getApplyListData() {
        try {
            System.out.println("act_id = " + act_id);
            rs = sc.execSql("select * from apply_list, users where apply_list.stu_num = users.stu_num and act_id = {0}".replace("{0}", String.valueOf(act_id)));
            String[] checkColumn = {"学号", "姓名", "出勤率", "状态"};
            DefaultTableModel checkModel = new DefaultTableModel(checkColumn, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            adminTable.setModel(checkModel);
            adminScrollPane.setViewportView(adminTable);

            if (rs != null) {
                while (rs.next()) {
                    String state;
                    switch (rs.getInt("apply_state")) {
                        case -1:
                            state = "待审核";
                            break;
                        case 0:
                            state = "拒绝";
                            break;
                        case 1:
                            state = "通过";
                            break;
                        default:
                            state = "未知";
                            break;
                    }
                    Object[] obj = {rs.getString("stu_num"), rs.getString("realname"), rs.getString("attend_rate") + "%", state};
                    checkModel.addRow(obj);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 创建新活动
     */
    private void createNewActivity() {
        String act_name = JOptionPane.showInputDialog("请输入活动名称：");
        String act_time = JOptionPane.showInputDialog("请输入活动时间：YYYY-MM-DD HH:MM:SS");
        String max_num = JOptionPane.showInputDialog("请输入最大人数：");
        String request = JOptionPane.showInputDialog("请输入活动要求：");
        String lasting_time = JOptionPane.showInputDialog("请输入持续时间（小时）：");
        String[] options = {"操场", "礼堂", "教室", "食堂"};

        // 显示 JOptionPane 输入对话框
        String selectedOption = (String) JOptionPane.showInputDialog(null,
                "请选择活动地点：",
                "选择地点",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]); // 设置默认选项

        int confirm = JOptionPane.showConfirmDialog(null, String.format("---活动信息---\n活动名称：%s\n活动时间：%s\n最大人数：%s\n活动要求：%s\n持续时间：%sh\n活动地点：%s", act_name, act_time, max_num, request, lasting_time, selectedOption));
        if (confirm == JOptionPane.YES_OPTION) {

            int place_id = 0;
            // 根据用户的选择执行不同的操作
            if (selectedOption != null) {
                switch (selectedOption) {
                    case "操场":
                        place_id = 1;
                        break;
                    case "礼堂":
                        place_id = 2;
                        break;
                    case "教室":
                        place_id = 3;
                        break;
                    case "食堂":
                        place_id = 4;
                        break;
                }
            }
            try {

                CallableStatement callableStatement = sc.getConnection().prepareCall("{call createNewActivity(?, ?, ?, ?, ?, ?, ?, ?)}");

                // 设置输入参数
                callableStatement.setString(1, act_name);
                callableStatement.setString(2, act_time);
                callableStatement.setInt(3, Integer.parseInt(max_num));
                callableStatement.setString(4, request);
                callableStatement.setInt(5, admin_id);
                callableStatement.setInt(6, Integer.parseInt(lasting_time));
                callableStatement.setInt(7, place_id);
                callableStatement.registerOutParameter(8, Types.CHAR);

                // 执行存储过程
                callableStatement.execute();

                // 获取输出参数的值
                String createResult = callableStatement.getString(8);

                if (Objects.equals(createResult, "创建新活动成功")) {
                    JOptionPane.showMessageDialog(null, createResult, "Notice", JOptionPane.INFORMATION_MESSAGE);

                } else {
                    JOptionPane.showMessageDialog(null, "创建新活动失败", "Notice", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException("创建新用户失败");
                }
                callableStatement.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 更新活动
     */
    private void updateActivity() {
        if (selectedCol == null) {
            JOptionPane.showMessageDialog(null, "请重新选择要修改的信息", "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("该信息不可修改");
        }
        String updateInfo = "";
        if (!selectedCol.equals("place_id")) {
            updateInfo = JOptionPane.showInputDialog(null, "请输入{0}修改后的信息：".replace("{0}", selectedCol));
        } else {
            String[] options = {"操场", "礼堂", "教室", "食堂"};

            // 显示 JOptionPane 输入对话框
            String selectedOption = (String) JOptionPane.showInputDialog(null,
                    "请选择一个地点：",
                    "选择地点",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]); // 设置默认选项
            String getPlaceID = "select place_id from place where name = '{0}'";
            // 根据用户的选择执行不同的操作
            if (selectedOption != null) {
                switch (selectedOption) {
                    case "操场":
                        // 执行操场相关操作
                        getPlaceID = getPlaceID.replace("{0}", "操场");
                        break;
                    case "礼堂":
                        // 执行礼堂相关操作
                        getPlaceID = getPlaceID.replace("{0}", "礼堂");
                        break;
                    case "教室":
                        // 执行教室相关操作
                        getPlaceID = getPlaceID.replace("{0}", "教室");
                        break;
                    case "食堂":
                        // 执行食堂相关操作
                        getPlaceID = getPlaceID.replace("{0}", "食堂");
                        break;
                }
            }
            try {
                rs = sc.execSql(getPlaceID);
                if (rs != null && rs.next()) {
                    updateInfo = rs.getString("place_id");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        String sql = String.format("update activity set %s = '%s' where act_id = %d", selectedCol, updateInfo, act_id);

        System.out.println(sql);
        try {
            CallableStatement callableStatement = sc.getConnection().prepareCall(sql);
            callableStatement.execute();
            callableStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        JOptionPane.showMessageDialog(null, "修改活动信息成功", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public JPanel getMainPanel() {
        return adminPanel;
    }
}
