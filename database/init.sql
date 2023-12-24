-- 初始化，添加固定的场地和单位

-- 添加场地
insert into place(place_id, name) values(1,'操场');
insert into place(place_id, name) values(2,'礼堂');
insert into place(place_id, name) values(3,'教室');
insert into place(place_id, name) values(4,'食堂');

-- 添加单位
insert into office(office_id, office_name) values(1,'教务处');
insert into office(office_id, office_name) values(2,'计算机学院');
insert into office(office_id, office_name) values(3,'机电学院');

-- 添加测试普通用户
call createNewUser('2021000001', '123456', '同学1', 0, @p_message);
call createNewUser('2021000002', '123456', '同学2', 0, @p_message);

-- 添加测试管理员
call createNewUser('2021010001', '123456', '管理员1', 1, @p_message);
call createNewUser('2021010002', '123456', '管理员2', 1, @p_message);

-- 添加测试负责人
call createNewUser('2021020001', '123456', '负责人1', 2, @p_message);
call createNewUser('2021020002', '123456', '负责人2', 2, @p_message);

-- 添加测试活动
call vol_sys.createNewActivity('活动1', '2023-12-31 09:00:00', 50, '无', 30001, 2, @p_message);