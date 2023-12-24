DELIMITER //

CREATE PROCEDURE createNewUser(
  IN p_username CHAR(10),
  IN p_password VARCHAR(20),
  IN p_realname VARCHAR(20),
  IN p_usertype INT,
  OUT p_message CHAR(255)
)
BEGIN
	if p_usertype = 1 or p_usertype = 2 or p_usertype = 3 then
		INSERT INTO register_data (id, pwd, realname, register_time, user_type)
		VALUES (p_username, p_password, p_realname, now(), p_usertype);
		set p_message = '创建新用户成功';
	else
		set p_message = '创建新用户失败';
	end if;
END //

DELIMITER ;

-- 触发器，实现注册时在对应的表上新增数据
DELIMITER //

CREATE TRIGGER createUserDataAfterInsert
AFTER INSERT ON register_data
FOR EACH ROW
BEGIN
  -- 在 users 表中创建数据
  if new.user_type = 0 then
	  INSERT INTO users (id, realname, stu_num, volun_time, attend_rate)
	  VALUES (NEW.username, NEW.realname, NEW.username, 0, 0);
	elseif new.user_type = 1 then
		insert into admins (id, office_id, realname)
        values(NEW.username,1,NEW.realname); 
    elseif new.user_type = 2 then
		insert into charger (id, office_id, realname)
        values(NEW.username,1,NEW.realname);
  end if;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE createNewActivity(
  IN p_act_name CHAR(10),
  IN p_act_time datetime,
  IN p_realname VARCHAR(20),
  IN p_max_num INT,
  IN p_request TEXT,
  IN p_admin_id SMALLINT,
  IN p_lasting_time INT,
  OUT p_message CHAR(255)
)
BEGIN
	insert into pub_activity(admin_id,pub_time)
    values(p_admin_id, now());
    insert into activity(charger_id, place_id, act_name, participant_num, act_state, act_request, lasting_time, join_num)
    values(50001, 1, p_act_name, p_max_num, 0, p_request, p_lasting_time, 0);  
END //

DELIMITER ;
DELIMITER //

create trigger acceptApply after update of apply_state
on apply_list for each row
begin
    if new.apply_state = true then
    insert into participating(stu_num, act_id)
    values (new.stu_num,new.act_id);
    end if;
end
DELIMITER ;

DELIMITER //

CREATE PROCEDURE getApplyList(
  IN p_act_id SMALLINT
)
BEGIN
	select apply_list.apply_id, apply_list.stu_num, users.realname, attend_rate
    from apply_list, users
    where apply_list.act_id = p_act_id;
END //

DELIMITER ;


DELIMITER //
CREATE TRIGGER updateVolunTime
AFTER UPDATE ON participating
FOR EACH ROW
BEGIN
  DECLARE v_stu_id INT;
  DECLARE v_total_time INT;
  
  -- 获取触发器更新的 stu_id
  SET v_stu_id = NEW.stu_id;
  
  -- 统计 lasting_time 之和
  SELECT SUM(lasting_time) INTO v_total_time
  FROM participating
  WHERE stu_id = v_stu_id;
  
  -- 更新 users 表中对应 stu_id 的 volun_time
  UPDATE users
  SET volun_time = v_total_time
  WHERE stu_id = v_stu_id;
END //

DELIMITER ;

DELIMITER //

CREATE TRIGGER updateAttendanceRate
AFTER UPDATE ON participating
FOR EACH ROW
BEGIN
  DECLARE v_stu_id INT;
  DECLARE v_total_rows INT;
  DECLARE v_attend_count INT;
  DECLARE v_attendance_rate DECIMAL(5, 2);
  
  -- 获取触发器更新的 stu_id
  SET v_stu_id = NEW.stu_id;
  
  -- 统计所有元组的数量
  SELECT COUNT(*) INTO v_total_rows
  FROM participating
  WHERE stu_id = v_stu_id;
  
  -- 统计属性 attend 为 true 的元组的数量
  SELECT COUNT(*) INTO v_attend_count
  FROM participating
  WHERE stu_id = v_stu_id AND present = TRUE;
  
  -- 计算出席率
  IF v_total_rows > 0 THEN
    SET v_attendance_rate = (v_attend_count / v_total_rows) * 100;
  ELSE
    SET v_attendance_rate = 0;
  END IF;
  
  -- 更新 users 表中对应 stu_id 的 attendance_rate
  UPDATE users
  SET attendance_rate = v_attendance_rate
  WHERE stu_id = v_stu_id;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE UpdatePresentStatus(
IN p_stu_id INT, 
IN p_sign_in_time DATETIME
)
BEGIN
  -- 更新 present 属性为 true
  UPDATE participating
  SET present = TRUE
  WHERE stu_id = p_stu_id AND sign_in_time = p_sign_in_time;
END //

DELIMITER ;

