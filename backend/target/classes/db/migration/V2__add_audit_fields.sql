-- 添加审核状态字段和单位字段
ALTER TABLE user ADD COLUMN audit_status INT DEFAULT 0 COMMENT '审核状态（0:待审核，1:审核通过，2:审核拒绝）';
ALTER TABLE user ADD COLUMN organization VARCHAR(200) COMMENT '单位名称（二级管理员）';
