/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2023/11/1 16:53:18                           */
/*==============================================================*/

/*==============================================================*/
/* Table: activity                                              */
/*==============================================================*/
create table activity
(
   act_id               smallint not null auto_increment  comment '',
   charger_id           smallint not null  comment '',
   place_id             smallint not null  comment '',
   act_name             varchar(255)  comment '',
   act_time             datetime  comment '',
   participant_num      int  comment '',
   act_state            int  comment '',
   act_request          text  comment '',
   lasting_time         int  comment '',
   join_num             int  comment '',
   primary key (act_id)
)
auto_increment = 1001;

/*==============================================================*/
/* Table: admins                                                */
/*==============================================================*/
create table admins
(
   admin_id             smallint not null auto_increment  comment '',
   office_id            smallint not null  comment '',
   realname             varchar(255)  comment '',
   primary key (admin_id)
)
auto_increment = 30001;

/*==============================================================*/
/* Table: apply_list                                            */
/*==============================================================*/
create table apply_list
(
   apply_id             smallint not null auto_increment  comment '',
   stu_num              char(10) not null  comment '',
   admin_id             smallint  comment '',
   act_id               smallint not null  comment '',
   apply_state          bool  comment '',
   primary key (apply_id)
)
auto_increment = 1;

/*==============================================================*/
/* Index: only_stu_id                                           */
/*==============================================================*/
create unique index only_stu_id on apply_list
(
   stu_num,
   act_id
);

/*==============================================================*/
/* Table: charger                                               */
/*==============================================================*/
create table charger
(
   realname             varchar(255)  comment '',
   charger_id           smallint not null auto_increment  comment '',
   office_id            smallint not null  comment '',
   primary key (charger_id)
)
auto_increment = 31001;

/*==============================================================*/
/* Table: office                                                */
/*==============================================================*/
create table office
(
   office_id            smallint not null auto_increment  comment '',
   office_name          varchar(5)  comment '',
   primary key (office_id)
);

/*==============================================================*/
/* Table: participating                                         */
/*==============================================================*/
create table participating
(
   stu_num              char(10) not null  comment '',
   act_id               smallint not null  comment '',
   present              bool  comment '',
   sign_in_time         datetime  comment '',
   primary key (stu_num, act_id)
);

/*==============================================================*/
/* Table: place                                                 */
/*==============================================================*/
create table place
(
   name                 varchar(6)  comment '',
   place_id             smallint not null  comment '',
   primary key (place_id)
);

alter table place comment '//TODO 要判断两个活动的时间是否重叠';

/*==============================================================*/
/* Table: pub_activity                                          */
/*==============================================================*/
create table pub_activity
(
   pub_id               smallint not null auto_increment  comment '',
   admin_id             smallint not null  comment '',
   act_id               smallint  comment '',
   pub_time             datetime not null  comment '',
   primary key (pub_id)
)
auto_increment = 1001;

/*==============================================================*/
/* Table: register_data                                         */
/*==============================================================*/
create table register_data
(
   user_id              char(10) not null  comment '',
   realname             varchar(255)  comment '',
   pwd                  varchar(20) not null  comment '',
   user_type            smallint not null  comment '',
   register_time        datetime not null  comment '',
   primary key (user_id)
);

/*==============================================================*/
/* Index: get_data_by_id                                        */
/*==============================================================*/
create unique index get_data_by_id on register_data
(
   user_id,
   pwd
);

/*==============================================================*/
/* Table: users                                                 */
/*==============================================================*/
create table users
(
   stu_num              char(10) not null  comment '',
   realname             varchar(255)  comment '',
   volun_time           int  comment '',
   attend_rate          float  comment '',
   primary key (stu_num)
);

/*==============================================================*/
/* View: getValidActivity                                       */
/*==============================================================*/
create VIEW  getValidActivity
 as
select act_name, act_time, lasting_time
from activity
where act_state = 0;

alter table activity add constraint FK_ACTIVITY_ACTIVITY__PLACE foreign key (place_id)
      references place (place_id) on delete restrict on update restrict;

alter table activity add constraint FK_ACTIVITY_CHARGER_C_CHARGER foreign key (charger_id)
      references charger (charger_id) on delete restrict on update restrict;

alter table admins add constraint FK_ADMINS_ADMIN_OFF_OFFICE foreign key (office_id)
      references office (office_id) on delete restrict on update restrict;

alter table apply_list add constraint FK_APPLY_LI_ADMIN_APP_ADMINS foreign key (admin_id)
      references admins (admin_id) on delete restrict on update restrict;

alter table apply_list add constraint FK_APPLY_LI_APPLY_FOR_ACTIVITY foreign key (act_id)
      references activity (act_id) on delete restrict on update restrict;

alter table apply_list add constraint FK_APPLY_LI_USER_APPL_USERS foreign key (stu_num)
      references users (stu_num) on delete restrict on update restrict;

alter table charger add constraint FK_CHARGER_CHARGER_O_OFFICE foreign key (office_id)
      references office (office_id) on delete restrict on update restrict;

alter table participating add constraint FK_PARTICIP_PARTICIPA_ACTIVITY foreign key (act_id)
      references activity (act_id) on delete restrict on update restrict;

alter table participating add constraint FK_PARTICIP_PARTICIPA_USERS foreign key (stu_num)
      references users (stu_num) on delete restrict on update restrict;

alter table pub_activity add constraint FK_PUB_ACTI_ADMIN_PUB_ADMINS foreign key (admin_id)
      references admins (admin_id) on delete restrict on update restrict;

alter table pub_activity add constraint FK_PUB_ACTI_PUB_ACTIV_ACTIVITY foreign key (act_id)
      references activity (act_id) on delete restrict on update restrict;

delimiter //

create procedure createNewActivity(IN p_act_name CHAR(255),IN p_act_time datetime,IN p_max_num INT,IN p_request TEXT,IN p_admin_id SMALLINT,IN p_lasting_time INT,OUT p_message CHAR(255))
BEGIN
    insert into activity(charger_id, place_id, act_name, act_time, participant_num, act_state, act_request, lasting_time, join_num)
        values(31001, 1, p_act_name, act_time, p_max_num, 0, p_request, p_lasting_time, 0);  
	insert into pub_activity(admin_id,pub_time)
        values(p_admin_id, now());
    set p_message = '创建新活动成功';
    
END//


create procedure createNewUser(IN p_userid CHAR(10),IN p_password VARCHAR(20),IN p_realname VARCHAR(20),IN p_usertype INT,OUT p_message CHAR(255))
BEGIN
	if p_usertype = 0 or p_usertype = 1 or p_usertype = 2 then
		INSERT INTO register_data (user_id, pwd, realname, register_time, user_type)
		VALUES (p_userid, p_password, p_realname, now(), p_usertype);
		set p_message = '创建新用户成功';
	else
		set p_message = '创建新用户失败';
	end if;
END//


create procedure getApplyList(IN p_act_id SMALLINT)
BEGIN
	select apply_list.apply_id, apply_list.stu_num, users.realname, attend_rate
    from apply_list, users
    where apply_list.act_id = p_act_id;
END//


create procedure stuSignUp(IN p_stu_id INT,IN p_act_id INT,OUT p_message CHAR(255))
BEGIN
  -- 更新 present 属性为 true
  UPDATE participating
  SET present = TRUE, sign_in_time = now()
  WHERE stu_num = p_stu_id
  AND act_id = p_act_id;
  SET p_message = '签到成功';
END//


create trigger acceptApply after update
on apply_list for each row
begin
    if new.apply_state = true then
        insert into participating(stu_num, act_id)
        values (new.stu_num,new.act_id);
        update activity
        set activity.join_num = activity.join_num + 1
        where activity.act_id = new.act_id;
    end if;
end//


create trigger updateVolunTime after update
on participating for each row
begin
  DECLARE v_stu_id INT;
  DECLARE v_total_time INT;
  
  SET v_stu_id = NEW.stu_num;
  
  SELECT SUM(lasting_time) INTO v_total_time
  FROM participating
  WHERE stu_num = v_stu_id;
  
  UPDATE users
  SET volun_time = v_total_time
  WHERE stu_num = v_stu_id;
end//


create trigger updateAttendRate after update
on participating for each row
begin
DECLARE v_stu_id INT;
  DECLARE v_total_rows INT;
  DECLARE v_attend_count INT;
  DECLARE v_attendance_rate DECIMAL(5, 2);
  
  -- 获取触发器更新的 stu_id
  SET v_stu_id = NEW.stu_num;
  
  -- 统计所有元组的数量
  SELECT COUNT(*) INTO v_total_rows
  FROM participating
  WHERE stu_num = v_stu_id;
  
  -- 统计属性 attend 为 true 的元组的数量
  SELECT COUNT(*) INTO v_attend_count
  FROM participating
  WHERE stu_num = v_stu_id AND present = TRUE;
  
  -- 计算出席率
  IF v_total_rows > 0 THEN
    SET v_attendance_rate = (v_attend_count / v_total_rows) * 100;
  ELSE
    SET v_attendance_rate = 0;
  END IF;
  
  -- 更新 users 表中对应 stu_id 的 attendance_rate
  UPDATE users
  SET attendance_rate = v_attendance_rate
  WHERE stu_num = v_stu_id;
end//


create trigger createByType after insert
on register_data for each row
begin
  -- 在 users 表中创建数据
  if new.user_type = 0 then
	  INSERT INTO users (realname, stu_num, volun_time, attend_rate)
	  VALUES (NEW.realname, NEW.user_id, 0, 0);
	elseif new.user_type = 1 then
		insert into admins (office_id, realname)
        values(1,NEW.realname); 
    elseif new.user_type = 2 then
		insert into charger (office_id, realname)
        values(1,NEW.realname);
  end if;
end//

