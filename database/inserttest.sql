-- insert into register_data(user_id,realname,pwd,user_type,register_time)
-- values('2021000001','小明','123456',0,now())

-- call createNewUser('2021010001', '123456', '管理员1', 1, @p_message);
-- select @p_message;

-- select * from users;
-- call createNewUser('2021030002', '123456', '负责人1', 2, @p_message);
-- select @p_message;

-- insert into apply_list(stu_num, admin_id, act_id, apply_state)
-- values('2021000001', 30001, 1001, 0);

-- update apply_list
-- set apply_state = 1
-- where apply_id = 1

-- set sql_safe_updates=0; 

-- update register_data
-- set pwd = '123456'
-- where user_id = '2021000012'

-- select * from apply_list, users where apply_list.stu_num = users.stu_num
-- select * from apply_list, users where apply_list.stu_num = users.stu_num and act_id = 1001

-- update activity
-- set participant_num = 10
-- where act_id = 1003

-- select act_time, lasting_time, date_add(act_time, interval lasting_time hour) as v_end_time
--   from activity
--   where act_id = 1001;


-- set @testtime = '2023-11-12 20:00:00';
-- set @cur_time = now();
-- select @cur_time>@testtime;
