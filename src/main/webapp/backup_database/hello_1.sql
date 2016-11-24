-- MySQL dump 10.13  Distrib 5.6.26, for Linux (x86_64)
--
-- Host: localhost    Database: check_a_db
-- ------------------------------------------------------
-- Server version	5.6.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `agent`
--

DROP TABLE IF EXISTS `agent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agent` (
  `agent_id` varchar(10) NOT NULL COMMENT '代理商唯一id',
  `agent_name` varchar(60) DEFAULT NULL COMMENT '代理商名称',
  `agent_connectperson` varchar(20) DEFAULT NULL COMMENT '代理商对账工作人员id',
  `agent_connectpname` varchar(20) DEFAULT NULL COMMENT '代理商对账工作人员姓名',
  `agent_cpphone` varchar(11) DEFAULT NULL COMMENT '代理商对账工作人员电话',
  `agent_cpemail` varchar(20) DEFAULT NULL COMMENT '代理商工作人员邮箱',
  `isregister` tinyint(1) DEFAULT '0' COMMENT '是否注册，和财务人员绑定',
  PRIMARY KEY (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agent`
--

LOCK TABLES `agent` WRITE;
/*!40000 ALTER TABLE `agent` DISABLE KEYS */;
INSERT INTO `agent` VALUES ('ah0001','安徽代理商','w1234','夏雪花','12345678901','132243@qq.com',1),('gd0001','广东代理商','z1234','张小明','17355704249','15587924@qq.com',1);
/*!40000 ALTER TABLE `agent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assistance`
--

DROP TABLE IF EXISTS `assistance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `assistance` (
  `workId` char(10) NOT NULL COMMENT '相当于用户名',
  `sourceSet` varchar(30) DEFAULT NULL,
  `name` varchar(10) NOT NULL,
  `phone` varchar(11) NOT NULL,
  `email` char(20) NOT NULL,
  `password` varchar(20) NOT NULL,
  `usertype` varchar(2) NOT NULL,
  `username` char(20) DEFAULT NULL,
  `agentid` varchar(10) DEFAULT NULL COMMENT '所属代理商id',
  `flag` int(11) DEFAULT NULL COMMENT '注册标志 0：成功注册 -1',
  PRIMARY KEY (`workId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='????ÿ????????Ա??Ϣ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assistance`
--

LOCK TABLES `assistance` WRITE;
/*!40000 ALTER TABLE `assistance` DISABLE KEYS */;
INSERT INTO `assistance` VALUES ('l1234',NULL,'刘大','13760313545','12345678@qq.com','1234','bm',NULL,'',0),('w1234',NULL,'夏雪花','12345678901','132243@qq.com','1234','bu',NULL,'ah0001',0),('z1234',NULL,'张小明','17355704249','15587924@qq.com','1234','bu',NULL,'gd0001',0);
/*!40000 ALTER TABLE `assistance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bank_input`
--

DROP TABLE IF EXISTS `bank_input`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bank_input` (
  `payer` char(60) DEFAULT NULL COMMENT '付款人',
  `money` double DEFAULT NULL COMMENT '付款金额',
  `pay_way` varchar(30) DEFAULT NULL COMMENT '付款方式',
  `payer_account` char(11) DEFAULT NULL COMMENT '付款帐号',
  `payid` int(11) DEFAULT NULL COMMENT '手机付款记录',
  `contract_num` varchar(30) DEFAULT NULL COMMENT '合同号',
  `status` tinyint(1) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `input_time` varchar(10) DEFAULT NULL COMMENT '到帐日期',
  `payee` varchar(60) DEFAULT NULL COMMENT '收款人',
  `payee_account` varchar(20) DEFAULT NULL COMMENT '收款帐号',
  `isConnect` tinyint(1) DEFAULT NULL COMMENT '是否和付款记录关联',
  `many_contract` varchar(1000) DEFAULT NULL COMMENT '多合同付款',
  `connect_num` int(11) DEFAULT '0' COMMENT '关联合同的结果，0：没有关联',
  `connect_client` varchar(21) DEFAULT NULL COMMENT '关联客户的结果',
  `owner` varchar(10) DEFAULT NULL COMMENT '上传者id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=318 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bank_input`
--

LOCK TABLES `bank_input` WRITE;
/*!40000 ALTER TABLE `bank_input` DISABLE KEYS */;
INSERT INTO `bank_input` VALUES ('小1',3500,'网银转账','A123456',NULL,NULL,0,269,'2016/07/18','安徽工程机械有限公司','0987654321',0,'[{\"contract\":\"38310012232132449\",\"money\":3500}]',1,'','ah0001'),('小1',1600,'柜台电汇','B123456',NULL,NULL,0,270,'2016/07/12','安徽工程机械有限公司','0987654321',0,'[{\"contract\":\"38310012232132449\",\"money\":1600}]',1,'','ah0001'),('小2',3638.38,'现金',NULL,NULL,NULL,0,271,'2016/07/29','安徽工程机械有限公司','789456123',0,'[{\"contract\":\"650032025312\",\"money\":3638.38}]',1,'','ah0001'),('合肥工程有限公司',1000,'网银转账','D123456',NULL,NULL,0,272,'2016/07/29','安徽工程机械有限公司','789456123',0,'[{\"contract\":\"6200300130\",\"money\":1000}]',1,'','ah0001'),('河东工程有限公司',5000,'柜台电汇','E123456',NULL,NULL,0,273,'2016/08/09','安徽工程机械有限公司','789456123',0,'[{\"contract\":\"508162321000465\",\"money\":5000}]',1,'','ah0001'),('阿荣旗远东混凝土有限公司',3500,'网银转账','A123456',NULL,NULL,0,307,'2016/07/18','广东工程机械有限公司','0987654321',0,NULL,0,'阿荣旗远东混凝土有限公司','gd0001'),('安达市建安建筑工程有限公司',1600,'柜台电汇','B123456',NULL,NULL,0,308,'2016/07/12','广东工程机械有限公司','0987654321',0,'[{\"contract\":\"KFZLfxe11-654\",\"money\":1600}]',0,'','gd0001'),('王五',3698.38,'现金',NULL,NULL,NULL,0,309,'2016/07/29','广东工程机械有限公司','789456123',0,NULL,0,'','gd0001'),('安福县名骏商品混凝土有限责任公司',10000,'网银转账','D123456',NULL,NULL,0,310,'2016/07/29','广东工程机械有限公司','789456123',0,NULL,0,'安福县名骏商品混凝土有限责任公司','gd0001'),('临猗县瑞帝斯混凝土有限公司',5000,'柜台电汇','E123456',NULL,NULL,0,311,'2016/08/09','广东工程机械有限公司','789456123',0,'[{\"contract\":\"6500056529319\",\"money\":5000}]',0,'','gd0001'),('邓航',380000,'现金','K78965',NULL,NULL,0,312,'2016/08/09','广东工程机械有限公司','789456123',0,NULL,0,'邓航','gd0001'),('王柯',12000,'网银转账','D654321',NULL,NULL,0,313,'2016/08/18','广东工程机械有限公司','3456789012345',0,NULL,0,'','gd0001'),('湖南精优机械科技有限公司',10000,'柜台电汇','E654321',NULL,NULL,0,314,'2016/08/22','广东工程机械有限公司','3456789012345',0,NULL,0,'','gd0001'),('安徽华诚混凝土有限公司',3000,'柜台电汇','F654321',NULL,NULL,0,315,'2016/08/25','广东工程机械有限公司','3456789012345',0,'[{\"contract\":\"XTM1101108\",\"money\":3000}]',0,'','gd0001'),('杨军',2000,'银行承兑汇票',NULL,NULL,NULL,0,316,'2016/08/25','广东工程机械有限公司','789456123',0,'[{\"contract\":\"38311416001449\",\"money\":2000}]',0,'','gd0001'),('张三',1000,'转账','C1234',NULL,NULL,0,317,'2016/09/10','广东工程机械有限公司','789456123',0,NULL,0,'','gd0001');
/*!40000 ALTER TABLE `bank_input` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bank_input_backup`
--

DROP TABLE IF EXISTS `bank_input_backup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bank_input_backup` (
  `payer` char(60) DEFAULT NULL COMMENT '付款人',
  `money` double DEFAULT NULL COMMENT '付款金额',
  `pay_way` varchar(30) DEFAULT NULL COMMENT '付款方式',
  `payer_account` char(11) DEFAULT NULL COMMENT '付款帐号',
  `payid` int(11) DEFAULT NULL COMMENT '手机付款记录',
  `contract_num` varchar(30) DEFAULT NULL COMMENT '合同号',
  `status` tinyint(1) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `input_time` varchar(10) DEFAULT NULL COMMENT '到帐日期',
  `payee` varchar(60) DEFAULT NULL COMMENT '收款人',
  `payee_account` varchar(20) DEFAULT NULL COMMENT '收款帐号',
  `isConnect` tinyint(1) DEFAULT NULL COMMENT '是否和付款记录关联',
  `many_contract` varchar(1000) DEFAULT NULL COMMENT '多合同付款',
  `connect_num` int(11) DEFAULT '0' COMMENT '关联合同的结果，0：没有关联',
  `connect_client` varchar(21) DEFAULT NULL COMMENT '关联客户的结果',
  `owner` varchar(10) DEFAULT NULL COMMENT '上传者id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1697 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bank_input_backup`
--

LOCK TABLES `bank_input_backup` WRITE;
/*!40000 ALTER TABLE `bank_input_backup` DISABLE KEYS */;
/*!40000 ALTER TABLE `bank_input_backup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caresult_history`
--

DROP TABLE IF EXISTS `caresult_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `caresult_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `caid` varchar(30) NOT NULL COMMENT '对账结果id',
  `cayear` varchar(4) DEFAULT NULL COMMENT '对账年份',
  `camonth` varchar(2) DEFAULT NULL COMMENT '对账月份',
  `url` varchar(50) DEFAULT NULL COMMENT '对账结果链接',
  `owner` varchar(30) DEFAULT NULL COMMENT '代理商id',
  `caresult` char(1) DEFAULT NULL COMMENT '对账结果，F代表完成，D进行',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caresult_history`
--

LOCK TABLES `caresult_history` WRITE;
/*!40000 ALTER TABLE `caresult_history` DISABLE KEYS */;
INSERT INTO `caresult_history` VALUES (20,'2016-09-gd0001','2016','09','/check_Accout/报表中心/2016-09-gd0001_对账结果.xlsx','gd0001','F'),(21,'2016-08-gd0001','2016','08','/check_Accout/报表中心/2016-08-gd0001_对账结果.xlsx','gd0001','F'),(22,'2016-10-gd0001','2016','10','/check_Accout/报表中心/2016-10-gd0001_对账结果.xlsx','gd0001','F'),(23,'2016-10-ah0001','2016','10','/check_Accout/报表中心/2016-10-ah0001_对账结果.xlsx','ah0001','F');
/*!40000 ALTER TABLE `caresult_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `connect_person`
--

DROP TABLE IF EXISTS `connect_person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `connect_person` (
  `username` varchar(12) NOT NULL COMMENT '用户名',
  `phone` varchar(11) DEFAULT NULL,
  `weixin` varchar(20) DEFAULT NULL,
  `company` varchar(50) DEFAULT NULL COMMENT '单位名称（付款人名称）',
  `password` varchar(20) NOT NULL,
  `real_name` varchar(20) DEFAULT NULL COMMENT '真实姓名',
  `register_way` char(1) DEFAULT NULL COMMENT '注册方式，P：个人；C：公司',
  `agent` varchar(10) DEFAULT NULL COMMENT '所属代理商',
  `contract_mes` varchar(1000) DEFAULT NULL COMMENT '合同信息，用于证明为有效用户',
  `companyid` varchar(30) DEFAULT NULL COMMENT '公司标识',
  `flag` int(11) DEFAULT '-1' COMMENT '审核通过标志',
  `email` varchar(20) DEFAULT NULL COMMENT '邮箱',
  `cardid` varchar(22) DEFAULT NULL COMMENT '身份证',
  `score` int(11) DEFAULT '0' COMMENT '积分',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connect_person`
--

LOCK TABLES `connect_person` WRITE;
/*!40000 ALTER TABLE `connect_person` DISABLE KEYS */;
INSERT INTO `connect_person` VALUES ('dengding','1344536364','dengdingweixin','临猗县瑞帝斯混凝土有限公司','1234','邓丁','C','gd0001','6500056529319','33262111086745',0,'254435@qq.com','440789923424',10),('dengfa','135678890','dengfaweixin','安福县名骏商品混凝土有限责任公司','1234','邓发','C','gd0001','50821216000465','61640871-7',0,'45687@qq.com','46754456756',20),('denghang','15581666637',NULL,'邓航','1234','邓航','P','gd0001',NULL,'410183197312122000',0,NULL,'410183197312122000',10),('lisi','13760898789','lisiweixin','李四','1234','李四','P','gd0001','65000552118980','1253',0,'rawfga@qq.com','47977534543',10),('liufei','13760898909','liufeiweixin','安达市建安建筑工程有限公司','1234','刘飞','C','gd0001','KFZLfxe11-654','576932908T9-S',0,'123456@qq.com','48097456789',20),('liusan','13750678987','liusanweixin','汕头建筑有限公司','1234','刘三','C','gd0001','6199901Q124669038','tSTFE122000',0,'fawfer@qq.com','44078119934567',10),('test','13789098745','testweixin','三一测试公司','1234','测试','C','gd0001','43243tyu','44078199789',0,'fafee@qq.com','4407899634',0);
/*!40000 ALTER TABLE `connect_person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `connect_person_view`
--

DROP TABLE IF EXISTS `connect_person_view`;
/*!50001 DROP VIEW IF EXISTS `connect_person_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `connect_person_view` AS SELECT 
 1 AS `username`,
 1 AS `phone`,
 1 AS `weixin`,
 1 AS `company`,
 1 AS `password`,
 1 AS `real_name`,
 1 AS `register_way`,
 1 AS `agent`,
 1 AS `contract_mes`,
 1 AS `companyid`,
 1 AS `flag`,
 1 AS `email`,
 1 AS `cardid`,
 1 AS `score`,
 1 AS `agent_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `cus_secondstore`
--

DROP TABLE IF EXISTS `cus_secondstore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cus_secondstore` (
  `client` char(60) NOT NULL,
  `input` double DEFAULT NULL,
  `update_time` char(10) DEFAULT NULL,
  `contract_mes` varchar(1000) DEFAULT NULL COMMENT '客户的合同信息',
  `contract_num` int(11) DEFAULT NULL COMMENT '合同个数',
  `owner` varchar(10) DEFAULT NULL COMMENT '客户所属代理商',
  `total` int(11) DEFAULT NULL COMMENT '保存总额',
  `accout_mes` varchar(100) DEFAULT NULL COMMENT '客户的合同信息',
  PRIMARY KEY (`client`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='????û????д?????ŵĸ?????¼';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cus_secondstore`
--

LOCK TABLES `cus_secondstore` WRITE;
/*!40000 ALTER TABLE `cus_secondstore` DISABLE KEYS */;
INSERT INTO `cus_secondstore` VALUES ('临猗县瑞帝斯混凝土有限公司',0,NULL,'[{\"contract\":\"6500056529319\",\"debt\":6352278}]',1,'gd0001',NULL,'[\"E123456\"]'),('合肥工程有限公司',0,NULL,'[{\"contract\":\"6200300130\",\"debt\":42000}]',1,'ah0001',NULL,'[\"D123456\"]'),('大连船舶',0,NULL,'[{\"contract\":\"3831143201451\",\"debt\":10000}]',1,'ah0001',NULL,NULL),('安德富',0,NULL,NULL,0,'gd0001',NULL,NULL),('安徽华诚混凝土有限公司',0,NULL,'[{\"contract\":\"XTM1101108\",\"debt\":179787}]',1,'gd0001',NULL,'[\"F654321\"]'),('安徽强大建筑集团有限公司',0,NULL,'[{\"contract\":\"7924130137\",\"debt\":1422554}]',1,'gd0001',NULL,NULL),('安徽高翔新型建材有限公司',0,NULL,NULL,0,'gd0001',NULL,NULL),('安福县名骏商品混凝土有限责任公司',0,NULL,'[{\"contract\":\"50821216000465\",\"debt\":345343},{\"contract\":\"7719091004\",\"debt\":679560},{\"contract\":\"77190916000004\",\"debt\":679560}]',3,'gd0001',NULL,'[\"D123456\"]'),('安达市建安建筑工程有限公司',0,NULL,'[{\"contract\":\"KFZLfxe11-654\",\"debt\":1076565}]',1,'gd0001',NULL,'[\"B123456\"]'),('小1',0,NULL,'[{\"contract\":\"38310012232132449\",\"debt\":49000}]',1,'ah0001',NULL,'[\"A123456\",\"B123456\"]'),('小2',0,NULL,'[{\"contract\":\"650032025312\",\"debt\":5000}]',1,'ah0001',NULL,NULL),('小3',0,NULL,'[{\"contract\":\"650040232529319\",\"debt\":45000}]',1,'ah0001',NULL,NULL),('李四',0,NULL,'[{\"contract\":\"65000552118980\",\"debt\":623232}]',1,'gd0001',NULL,NULL),('杨军',0,NULL,'[{\"contract\":\"38311416001449\",\"debt\":189087}]',1,'gd0001',NULL,NULL),('汕头建筑有限公司',0,NULL,'[{\"contract\":\"6199901Q124669038\",\"debt\":179787}]',1,'gd0001',NULL,NULL),('汕尾建筑有限公司',0,NULL,'[{\"contract\":\"KFZL2011-654\",\"debt\":1076565}]',1,'gd0001',NULL,NULL),('河东工程有限公司',0,NULL,'[{\"contract\":\"508162321000465\",\"debt\":78000}]',1,'ah0001',NULL,'[\"E123456\"]'),('清运混凝土有限公司',0,NULL,'[{\"contract\":\"XTMeele01108\",\"debt\":179787}]',1,'gd0001',NULL,NULL),('茂名建筑集团有限公司',0,NULL,'[{\"contract\":\"79241316000137\",\"debt\":1422554}]',1,'gd0001',NULL,NULL),('邓航',760000,'2016/08/09','[{\"contract\":\"6199901Q1131246\",\"debt\":179787},{\"contract\":\"KFZL1-654\",\"debt\":1076565}]',2,'gd0001',NULL,'[\"K78965\"]'),('阿荣旗远东混凝土有限公司',7000,'2016/07/18','[{\"contract\":\"38311416001451\",\"debt\":200000},{\"contract\":\"620004962950130\",\"debt\":189678}]',2,'gd0001',NULL,'[\"A123456\"]');
/*!40000 ALTER TABLE `cus_secondstore` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_log`
--

DROP TABLE IF EXISTS `op_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_log` (
  `time` varchar(22) DEFAULT NULL,
  `usertype` varchar(15) DEFAULT NULL,
  `username` varchar(30) DEFAULT NULL,
  `content` varchar(30) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `result` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_log`
--

LOCK TABLES `op_log` WRITE;
/*!40000 ALTER TABLE `op_log` DISABLE KEYS */;
INSERT INTO `op_log` VALUES ('2016/10/20_11:32:06','管理员','l1234','注销',1,'成功'),('2016/10/20_11:32:10','代理商财务','z1234','登录',2,'成功'),('2016/10/20_11:32:12','代理商财务','z1234','进入对账流程',3,'成功'),('2016/10/20_11:32:54','代理商财务','z1234','注销',4,'成功'),('2016/10/20_11:32:56','管理员','l1234','登录',5,'成功'),('2016/10/20_13:41:53','代理商财务','z1234','登录',6,'成功'),('2016/10/20_13:42:13','代理商财务','z1234','进入对账流程',7,'成功'),('2016/10/20_13:45:34','管理员','l1234','登录',8,'成功'),('2016/10/20_13:47:25','代理商财务','z1234','登录',9,'成功'),('2016/10/20_13:47:28','代理商财务','z1234','进入对账流程',10,'成功'),('2016/10/20_13:47:32','代理商财务','z1234','进入对账流程',11,'成功'),('2016/10/20_13:47:43','代理商财务','z1234','执行对账',12,'成功'),('2016/10/20_13:47:44','代理商财务','z1234','导出对账结果',13,'成功'),('2016/10/20_13:48:25','代理商财务','z1234','注销',14,'成功'),('2016/10/20_13:48:30','管理员','l1234','登录',15,'成功'),('2016/10/20_14:24:45','代理商财务','z1234','登录',16,'成功'),('2016/10/20_14:24:49','代理商财务','z1234','进入对账流程',17,'成功'),('2016/10/20_14:25:35','代理商财务','z1234','注销',18,'成功'),('2016/10/20_14:53:50','管理员','l1234','注销',19,'成功'),('2016/10/20_14:54:06','代理商财务','z1234','登录',20,'成功'),('2016/10/20_14:54:09','代理商财务','z1234','进入对账流程',21,'成功'),('2016/10/20_14:58:37','代理商财务','z1234','注销',22,'成功'),('2016/10/20_14:58:42','管理员','l1234','登录',23,'成功'),('2016/10/20_15:01:55','客户','test','上传付款信息',24,'成功'),('2016/10/20_15:03:46','管理员','l1234','注销',25,'成功'),('2016/10/20_15:03:48','代理商财务','z1234','登录',26,'成功'),('2016/10/20_15:04:11','代理商财务','z1234','注销',27,'成功'),('2016/10/20_15:16:29','代理商财务','z1234','登录',28,'成功'),('2016/10/20_15:16:33','代理商财务','z1234','进入对账流程',29,'成功'),('2016/10/20_15:17:43','代理商财务','z1234','执行对账',30,'成功'),('2016/10/20_15:17:44','代理商财务','z1234','导出对账结果',31,'成功'),('2016/10/20_15:18:11','代理商财务','z1234','取消并重新对账',32,'成功'),('2016/10/20_15:20:08','代理商财务','z1234','注销',33,'成功'),('2016/10/20_15:20:09','代理商财务','z1234','登录',34,'成功'),('2016/10/20_15:20:13','代理商财务','z1234','进入对账流程',35,'成功'),('2016/10/20_15:20:32','代理商财务','z1234','执行对账',36,'成功'),('2016/10/20_15:20:33','代理商财务','z1234','导出对账结果',37,'成功'),('2016/10/20_15:21:07','代理商财务','z1234','取消并重新对账',38,'成功'),('2016/10/20_15:24:08','代理商财务','z1234','注销',39,'成功'),('2016/10/20_15:25:02','代理商财务','z1234','登录',40,'成功'),('2016/10/20_15:25:07','代理商财务','z1234','进入对账流程',41,'成功'),('2016/10/20_15:35:22','代理商财务','z1234','注销',42,'成功'),('2016/10/20_15:35:23','代理商财务','z1234','登录',43,'成功'),('2016/10/20_15:35:27','代理商财务','z1234','注销',44,'成功'),('2016/10/20_15:35:32','管理员','l1234','登录',45,'成功'),('2016/10/20_15:37:53','管理员','l1234','注销',46,'成功'),('2016/10/20_15:38:11','代理商财务','z1234','登录',47,'成功'),('2016/10/20_15:38:16','代理商财务','z1234','进入对账流程',48,'成功'),('2016/10/20_15:43:30','代理商财务','z1234','注销',49,'成功'),('2016/10/20_15:43:33','管理员','l1234','登录',50,'成功'),('2016/10/20_15:44:18','管理员','l1234','注销',51,'成功'),('2016/10/20_15:44:22','管理员','z1234','登录',52,'失败'),('2016/10/20_15:44:31','代理商财务','z1234','登录',53,'成功'),('2016/10/20_15:44:33','代理商财务','z1234','进入对账流程',54,'成功'),('2016/10/20_16:28:08','管理员','l1234','登录',55,'成功'),('2016/10/20_16:30:47','客户','denghang','上传付款信息',56,'成功'),('2016/10/20_16:31:02','管理员','l1234','注销',57,'成功'),('2016/10/20_16:31:07','代理商财务','z1234','登录',58,'成功'),('2016/10/20_16:32:56','客户','denghang','上传付款信息',59,'成功'),('2016/10/20_16:33:25','客户','dengfa','上传付款信息',60,'成功'),('2016/10/20_16:35:24','代理商财务','z1234','注销',61,'成功'),('2016/10/20_16:35:29','管理员','l1234','登录',62,'成功'),('2016/10/20_16:35:56','管理员','l1234','注销',63,'成功'),('2016/10/20_16:36:01','代理商财务','z1234','登录',64,'成功'),('2016/10/20_16:36:05','代理商财务','z1234','进入对账流程',65,'成功'),('2016/10/20_16:36:29','代理商财务','z1234','导入货款表及出纳表',66,'成功'),('2016/10/20_16:49:40','代理商财务','z1234','执行对账',67,'成功'),('2016/10/20_16:49:41','代理商财务','z1234','导出对账结果',68,'成功'),('2016/10/20_16:56:20','代理商财务','z1234','取消并重新对账',69,'成功'),('2016/10/20_16:56:45','代理商财务','z1234','执行对账',70,'成功'),('2016/10/20_16:56:46','代理商财务','z1234','导出对账结果',71,'成功'),('2016/10/20_16:56:56','代理商财务','z1234','取消并重新对账',72,'成功'),('2016/10/20_16:57:06','代理商财务','z1234','执行对账',73,'成功'),('2016/10/20_16:57:07','代理商财务','z1234','导出对账结果',74,'成功'),('2016/10/20_16:57:26','代理商财务','z1234','取消并重新对账',75,'成功'),('2016/10/20_16:57:33','代理商财务','z1234','执行对账',76,'成功'),('2016/10/20_16:57:34','代理商财务','z1234','导出对账结果',77,'成功'),('2016/10/20_16:59:40','代理商财务','z1234','注销',78,'成功'),('2016/10/20_16:59:48','管理员','l1234','登录',79,'成功'),('2016/10/20_17:03:13','管理员','l1234','注销',80,'成功'),('2016/10/20_17:03:23','代理商财务','w1234','登录',81,'成功'),('2016/10/20_17:03:37','代理商财务','w1234','进入对账流程',82,'成功'),('2016/10/20_17:03:53','代理商财务','w1234','导入货款表及出纳表',83,'成功'),('2016/10/20_17:04:10','代理商财务','w1234','执行对账',84,'成功'),('2016/10/20_17:04:11','代理商财务','w1234','导出对账结果',85,'成功'),('2016/10/20_17:06:48','代理商财务','w1234','注销',86,'成功'),('2016/10/20_17:07:07','管理员','l1234','登录',87,'成功'),('2016/10/20_17:09:15','客户','liufei','上传付款信息',88,'成功'),('2016/10/20_17:09:37','管理员','l1234','注销',89,'成功'),('2016/10/20_17:09:43','代理商财务','z1234','登录',90,'成功'),('2016/10/20_17:13:05','客户','denghang','微信上传付款信息',91,'成功'),('2016/10/20_17:13:07','客户','denghang','上传付款信息',92,'成功'),('2016/10/20_17:13:39','代理商财务','z1234','登录',93,'成功'),('2016/10/20_17:17:22','客户','denghang','上传付款信息',94,'成功'),('2016/10/20_18:33:27','代理商财务','l1234','登录',95,'失败'),('2016/10/20_18:33:31','代理商财务','l1234','登录',96,'失败'),('2016/10/20_18:33:32','代理商财务','l1234','登录',97,'失败'),('2016/10/20_20:16:13','客户',NULL,'微信上传付款信息',98,'失败'),('2016/10/20_20:19:22','客户',NULL,'微信上传付款信息',99,'失败'),('2016/10/20_20:19:37','客户',NULL,'微信上传付款信息',100,'失败'),('2016/10/26_12:27:52','代理商财务','z1234','登录',101,'失败'),('2016/10/26_12:28:00','代理商财务','z1234','登录',102,'成功'),('2016/10/26_12:28:06','代理商财务','z1234','注销',103,'成功'),('2016/10/26_12:28:08','代理商财务','z1234','登录',104,'成功'),('2016/10/26_12:28:11','代理商财务','z1234','进入对账流程',105,'成功'),('2016/10/26_12:28:14','代理商财务','z1234','进入对账流程',106,'成功'),('2016/10/26_12:28:27','代理商财务','z1234','注销',107,'成功'),('2016/10/26_12:28:36','管理员','l1234','登录',108,'成功'),('2016/10/26_12:29:04','管理员','l1234','注销',109,'成功'),('2016/10/26_12:30:54','代理商财务','z1234','登录',110,'成功'),('2016/10/26_12:30:56','代理商财务','z1234','进入对账流程',111,'成功'),('2016/10/26_12:31:08','代理商财务','z1234','导入货款表及出纳表',112,'成功'),('2016/10/26_12:32:04','代理商财务','z1234','执行对账',113,'失败'),('2016/10/26_12:33:35','代理商财务','z1234','注销',114,'成功'),('2016/10/27_10:01:28','代理商财务','z1234','登录',115,'失败'),('2016/10/27_10:01:37','代理商财务','z1234','登录',116,'成功'),('2016/10/27_10:01:41','代理商财务','z1234','进入对账流程',117,'成功'),('2016/10/27_10:01:45','代理商财务','z1234','注销',118,'成功'),('2016/10/27_10:01:47','代理商财务','z1234','登录',119,'成功'),('2016/10/27_10:01:49','代理商财务','z1234','进入对账流程',120,'成功'),('2016/10/27_10:02:44','代理商财务','z1234','导入货款表及出纳表',121,'成功'),('2016/10/27_10:03:06','代理商财务','z1234','执行对账',122,'失败'),('2016/10/27_10:03:20','代理商财务','z1234','注销',123,'成功'),('2016/10/27_10:03:24','管理员','l1234','登录',124,'成功'),('2016/10/27_10:09:01','管理员','l1234','注销',125,'成功'),('2016/10/27_10:09:10','代理商财务','z1234','登录',126,'成功'),('2016/10/27_10:09:12','代理商财务','z1234','进入对账流程',127,'成功'),('2016/10/27_10:09:18','代理商财务','z1234','进入对账流程',128,'成功'),('2016/10/27_10:11:47','代理商财务','z1234','注销',129,'成功'),('2016/10/27_10:11:57','管理员','l1234','登录',130,'成功'),('2016/10/27_10:12:08','管理员','l1234','注销',131,'成功'),('2016/10/27_10:15:18','代理商财务','z1234','登录',132,'成功'),('2016/10/27_10:15:20','代理商财务','z1234','进入对账流程',133,'成功'),('2016/10/27_10:15:28','代理商财务','z1234','注销',134,'成功'),('2016/10/27_10:15:42','管理员','l1234','登录',135,'成功'),('2016/10/27_10:17:38','管理员','l1234','注销',136,'成功'),('2016/10/27_10:17:39','代理商财务','z1234','登录',137,'成功'),('2016/10/27_10:17:43','代理商财务','z1234','注销',138,'成功'),('2016/10/27_10:19:40','代理商财务','z1234','登录',139,'成功'),('2016/10/27_10:19:42','代理商财务','z1234','进入对账流程',140,'成功'),('2016/10/27_10:19:54','代理商财务','z1234','导入货款表及出纳表',141,'成功'),('2016/10/27_10:20:01','代理商财务','z1234','注销',142,'成功'),('2016/10/27_10:20:05','管理员','l1234','登录',143,'成功'),('2016/10/27_10:20:25','管理员','l1234','注销',144,'成功'),('2016/10/27_17:06:35','代理商财务','z1234','登录',145,'成功'),('2016/10/27_17:06:39','代理商财务','z1234','进入对账流程',146,'成功'),('2016/10/27_17:06:42','代理商财务','z1234','注销',147,'成功'),('2016/10/27_17:06:43','代理商财务','z1234','登录',148,'成功'),('2016/10/27_17:06:45','代理商财务','z1234','进入对账流程',149,'成功'),('2016/10/27_17:06:48','代理商财务','z1234','注销',150,'成功'),('2016/10/27_17:06:53','管理员','l1234','登录',151,'成功'),('2016/10/27_17:06:59','管理员','l1234','注销',152,'成功'),('2016/10/28_08:21:51','代理商财务','z1234','登录',153,'成功'),('2016/10/28_08:22:12','代理商财务','z1234','进入对账流程',154,'成功'),('2016/10/28_08:22:28','代理商财务','z1234','注销',155,'成功'),('2016/10/28_12:56:57','管理员','l1234','登录',156,'成功'),('2016/10/28_13:09:07','管理员','l1234','登录',157,'成功'),('2016/10/28_13:09:41','管理员','l1234','注销',158,'成功'),('2016/10/28_13:09:42','管理员','l1234','登录',159,'成功'),('2016/10/29_09:01:53','代理商财务','z1234','登录',160,'成功'),('2016/10/29_11:25:02','代理商财务','z1234','登录',161,'成功'),('2016/10/29_11:30:43','代理商财务','z1234','登录',162,'成功'),('2016/10/29_11:31:32','代理商财务','z1234','进入对账流程',163,'成功'),('2016/10/29_11:31:42','代理商财务','z1234','注销',164,'成功'),('2016/10/29_12:48:11','代理商财务','z1234','登录',165,'成功'),('2016/10/29_12:49:53','代理商财务','z1234','注销',166,'成功'),('2016/10/29_12:49:54','代理商财务','z1234','登录',167,'成功'),('2016/10/29_12:52:06','代理商财务','z1234','注销',168,'成功'),('2016/10/29_12:52:20','代理商财务','z1234','登录',169,'成功'),('2016/10/29_12:54:02','代理商财务','z1234','注销',170,'成功'),('2016/10/30_09:06:09','代理商财务','z1234','登录',171,'失败'),('2016/10/30_14:47:22','代理商财务','z1234','登录',172,'成功'),('2016/10/30_14:48:06','代理商财务','z1234','进入对账流程',173,'成功'),('2016/10/30_14:48:15','代理商财务','z1234','注销',174,'成功'),('2016/10/30_14:48:15','代理商财务','z1234','登录',175,'成功'),('2016/10/30_14:48:17','代理商财务','z1234','进入对账流程',176,'成功'),('2016/10/30_14:48:23','代理商财务','z1234','进入对账流程',177,'成功'),('2016/10/30_14:48:29','代理商财务','z1234','注销',178,'成功'),('2016/10/30_14:48:31','代理商财务','z1234','登录',179,'成功'),('2016/10/30_14:48:32','代理商财务','z1234','进入对账流程',180,'成功'),('2016/10/30_14:48:43','代理商财务','z1234','导入货款表及出纳表',181,'成功'),('2016/10/30_14:49:44','代理商财务','z1234','注销',182,'成功'),('2016/10/30_14:49:46','代理商财务','z1234','登录',183,'成功'),('2016/10/30_14:49:47','代理商财务','z1234','进入对账流程',184,'成功'),('2016/10/30_14:50:03','代理商财务','z1234','进入对账流程',185,'成功'),('2016/10/30_14:50:06','代理商财务','z1234','进入对账流程',186,'成功'),('2016/10/30_14:50:12','代理商财务','z1234','注销',187,'成功'),('2016/10/30_14:50:16','管理员','l1234','登录',188,'成功'),('2016/10/30_14:55:06','管理员','l1234','注销',189,'成功'),('2016/10/30_14:55:10','代理商财务','z1234','登录',190,'成功'),('2016/10/30_14:55:16','代理商财务','z1234','进入对账流程',191,'成功'),('2016/10/30_14:55:20','代理商财务','z1234','进入对账流程',192,'成功'),('2016/10/30_14:55:30','代理商财务','z1234','注销',193,'成功'),('2016/10/30_14:55:31','代理商财务','z1234','登录',194,'成功'),('2016/10/30_14:55:41','代理商财务','z1234','进入对账流程',195,'成功'),('2016/10/30_14:56:17','代理商财务','z1234','执行对账',196,'成功'),('2016/10/30_14:56:18','代理商财务','z1234','导出对账结果',197,'成功'),('2016/10/30_14:56:28','代理商财务','z1234','进入对账流程',198,'成功'),('2016/10/30_14:56:34','代理商财务','z1234','执行对账',199,'成功'),('2016/10/30_14:56:35','代理商财务','z1234','导出对账结果',200,'成功'),('2016/10/30_14:56:44','代理商财务','z1234','注销',201,'成功'),('2016/10/30_15:48:50','代理商财务','z1234','登录',202,'成功'),('2016/10/30_15:49:01','代理商财务','z1234','进入对账流程',203,'成功'),('2016/10/30_15:49:07','代理商财务','z1234','注销',204,'成功'),('2016/10/30_15:49:10','代理商财务','z1234','登录',205,'成功'),('2016/10/30_15:49:13','代理商财务','z1234','注销',206,'成功'),('2016/10/30_15:49:18','管理员','l1234','登录',207,'成功');
/*!40000 ALTER TABLE `op_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ori_order`
--

DROP TABLE IF EXISTS `ori_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ori_order` (
  `order_num` char(30) NOT NULL,
  `input` double DEFAULT NULL COMMENT '本月收入',
  `debt` double DEFAULT NULL,
  `total` double DEFAULT NULL,
  `state` char(15) DEFAULT NULL,
  `update_time` varchar(10) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL,
  `client` varchar(60) DEFAULT NULL,
  `owner` varchar(20) DEFAULT NULL,
  `connect_bank` varchar(100) DEFAULT NULL COMMENT '关联的多条出纳',
  `customid` varchar(30) DEFAULT NULL COMMENT '对账联系人id',
  `cuscompanyid` varchar(30) DEFAULT NULL COMMENT '客户id',
  `product_time` varchar(10) DEFAULT NULL COMMENT '发货时间',
  `owner_product` varchar(60) DEFAULT NULL COMMENT '货款主体',
  `customname` varchar(100) DEFAULT NULL,
  `customphone` varchar(11) DEFAULT NULL,
  `customweixin` varchar(20) DEFAULT NULL,
  `asname` varchar(20) DEFAULT NULL,
  `asphone` varchar(11) DEFAULT NULL,
  `asemail` varchar(20) DEFAULT NULL,
  `province` varchar(20) DEFAULT NULL COMMENT '省份',
  PRIMARY KEY (`order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='?????ϴ???ԭʼ?˵?';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ori_order`
--

LOCK TABLES `ori_order` WRITE;
/*!40000 ALTER TABLE `ori_order` DISABLE KEYS */;
INSERT INTO `ori_order` VALUES ('38310012232132449',5100,40400,49000,NULL,'2016/07/12',NULL,'小1','ah0001','[269,270]',NULL,'412924197812080385','2014/02/27','中发',NULL,NULL,NULL,'夏雪花','12345678901','132243@qq.com','安徽'),('38311416001449',0,189087,189087,NULL,NULL,NULL,'杨军','gd0001',NULL,NULL,'4129812080385','2014/02/27','中发',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('38311416001451',0,200000,200000,NULL,NULL,NULL,'阿荣旗远东混凝土有限公司','gd0001',NULL,NULL,'57667809-2','2014/02/27','中宏',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('3831143201451',0,10000,10000,NULL,NULL,NULL,'大连船舶','ah0001',NULL,NULL,'57667809-2','2014/02/27','中宏',NULL,NULL,NULL,'夏雪花','12345678901','132243@qq.com','安徽'),('508162321000465',5000,73000,78000,NULL,'2016/08/09',NULL,'河东工程有限公司','ah0001','[273]',NULL,'61640871-7','2014/02/27','安徽代理商',NULL,NULL,NULL,'夏雪花','12345678901','132243@qq.com','安徽'),('50821216000465',0,345343,345343,NULL,NULL,NULL,'安福县名骏商品混凝土有限责任公司','gd0001',NULL,NULL,'61640871-7','2014/02/27','广东代理商','邓发','135678890','dengfaweixin','张小明','17355704249','15587924@qq.com','广东'),('6199901Q1131246',0,179787,179787,NULL,NULL,NULL,'邓航','gd0001',NULL,NULL,'410183197312122000','2014/03/26','广东代理商','邓航','15581666637',NULL,'张小明','17355704249','15587924@qq.com','广东'),('6199901Q124669038',0,179787,179787,NULL,NULL,NULL,'汕头建筑有限公司','gd0001',NULL,NULL,'tSTFE122000','2014/03/26','广东代理商','刘三','13750678987','liusanweixin','张小明','17355704249','15587924@qq.com','广东'),('620004962950130',0,189678,189678,NULL,NULL,NULL,'阿荣旗远东混凝土有限公司','gd0001',NULL,NULL,'57667809-2','2014/02/27','康富',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('6200300130',1000,41000,42000,NULL,'2016/07/29',NULL,'合肥工程有限公司','ah0001','[272]',NULL,'34567309-2','2014/02/27','康富',NULL,NULL,NULL,'夏雪花','12345678901','132243@qq.com','安徽'),('65000552118980',0,623232,623232,NULL,NULL,NULL,'李四','gd0001',NULL,NULL,'43098005171529','2014/02/27','中发',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('6500056529319',0,6352278,6352278,NULL,NULL,NULL,'临猗县瑞帝斯混凝土有限公司','gd0001',NULL,NULL,'33262111086745','2014/02/27','汽车金融','邓丁','1344536364','dengdingweixin','张小明','17355704249','15587924@qq.com','广东'),('650032025312',3638.38,1361.62,50000,NULL,'2016/07/29',NULL,'小2','ah0001','[271]',NULL,'430902198005171529','2014/02/27','中发',NULL,NULL,NULL,'夏雪花','12345678901','132243@qq.com','安徽'),('650040232529319',0,45000,45000,NULL,NULL,NULL,'小3','ah0001',NULL,NULL,'332621197001086745','2014/02/27','汽车金融',NULL,NULL,NULL,'夏雪花','12345678901','132243@qq.com','安徽'),('7719091004',0,679560,679560,NULL,NULL,NULL,'安福县名骏商品混凝土有限责任公司','gd0001',NULL,NULL,'61640871-7','2014/02/28','广东代理商','邓发','135678890','dengfaweixin','张小明','17355704249','15587924@qq.com','广东'),('77190916000004',0,679560,679560,NULL,NULL,NULL,'安福县名骏商品混凝土有限责任公司','gd0001',NULL,NULL,'61640871-7','2014/02/28','广东代理商','邓发','135678890','dengfaweixin','张小明','17355704249','15587924@qq.com','广东'),('7924130137',0,1422554,1422554,NULL,NULL,NULL,'安徽强大建筑集团有限公司','gd0001',NULL,NULL,'06066252-4','2014/03/26','广东代理商',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('79241316000137',0,1422554,1422554,NULL,NULL,NULL,'茂名建筑集团有限公司','gd0001',NULL,NULL,'06066252-4','2014/03/26','广东代理商',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('KFZL1-654',0,1076565,1076565,NULL,NULL,NULL,'邓航','gd0001',NULL,NULL,'410183197312122000','2014/03/26','广东代理商','邓航','15581666637',NULL,'张小明','17355704249','15587924@qq.com','广东'),('KFZL2011-654',0,1076565,1076565,NULL,NULL,NULL,'汕尾建筑有限公司','gd0001',NULL,NULL,'4101REAFEW200','2014/03/26','广东代理商',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('KFZLfxe11-654',0,1076565,1076565,NULL,NULL,NULL,'安达市建安建筑工程有限公司','gd0001',NULL,NULL,'576932908T9-S','2014/03/26','广东代理商','刘飞','13760898909','liufeiweixin','张小明','17355704249','15587924@qq.com','广东'),('XTM1101108',0,179787,179787,NULL,NULL,NULL,'安徽华诚混凝土有限公司','gd0001',NULL,NULL,'57697899-4','2014/03/26','广东代理商',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东'),('XTMeele01108',0,179787,179787,NULL,NULL,NULL,'清运混凝土有限公司','gd0001',NULL,NULL,'57697899-4','2014/03/26','广东代理商',NULL,NULL,NULL,'张小明','17355704249','15587924@qq.com','广东');
/*!40000 ALTER TABLE `ori_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ori_order_backup`
--

DROP TABLE IF EXISTS `ori_order_backup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ori_order_backup` (
  `order_num` char(30) NOT NULL,
  `input` double DEFAULT NULL COMMENT '本月收入',
  `debt` double DEFAULT NULL,
  `total` double DEFAULT NULL,
  `state` char(15) DEFAULT NULL,
  `update_time` varchar(10) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL,
  `client` varchar(60) DEFAULT NULL,
  `owner` varchar(20) DEFAULT NULL,
  `connect_bank` varchar(100) DEFAULT NULL COMMENT '关联的多条出纳',
  `customid` varchar(30) DEFAULT NULL COMMENT '对账联系人id',
  `cuscompanyid` varchar(30) DEFAULT NULL COMMENT '客户id',
  `product_time` varchar(10) DEFAULT NULL COMMENT '发货时间',
  `owner_product` varchar(60) DEFAULT NULL COMMENT '货款主体',
  `customname` varchar(60) DEFAULT NULL,
  `customphone` varchar(11) DEFAULT NULL,
  `customweixin` varchar(20) DEFAULT NULL,
  `asname` varchar(20) DEFAULT NULL,
  `asphone` varchar(11) DEFAULT NULL,
  `asemail` varchar(20) DEFAULT NULL,
  `province` varchar(20) DEFAULT NULL COMMENT '省份',
  PRIMARY KEY (`order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='?????ϴ???ԭʼ?˵?';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ori_order_backup`
--

LOCK TABLES `ori_order_backup` WRITE;
/*!40000 ALTER TABLE `ori_order_backup` DISABLE KEYS */;
/*!40000 ALTER TABLE `ori_order_backup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pay_record`
--

DROP TABLE IF EXISTS `pay_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pay_record` (
  `payer` char(30) NOT NULL COMMENT '付款人',
  `pay_money` double NOT NULL COMMENT '付款金额',
  `pay_way` varchar(30) NOT NULL COMMENT '付款方式',
  `pay_account` char(11) DEFAULT NULL COMMENT '付款账户',
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一id',
  `vicePayer` varchar(15) DEFAULT NULL,
  `receiver` varchar(60) DEFAULT NULL COMMENT '款项接受人',
  `connPerson` varchar(15) DEFAULT NULL COMMENT '对账联系人',
  `linkCer` varchar(100) DEFAULT NULL COMMENT '凭证保存路径',
  `bankinput_id` int(11) DEFAULT NULL COMMENT '关联的出纳id',
  `owner` varchar(60) DEFAULT NULL COMMENT '付款信息所属代理',
  `checkResult` char(1) DEFAULT NULL COMMENT '审阅结果',
  `pass` tinyint(1) DEFAULT NULL,
  `isconnect` tinyint(1) DEFAULT NULL COMMENT '是否和出纳关联',
  `many_pay` varchar(1000) DEFAULT NULL COMMENT '付款的合同号及金额',
  `upload_time` varchar(20) DEFAULT NULL COMMENT '上传时间',
  `contract_num` varchar(10) DEFAULT NULL,
  `caid` varchar(30) DEFAULT NULL COMMENT '对账id',
  `freeback` tinyint(1) DEFAULT '0' COMMENT '返利标志',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=854 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pay_record`
--

LOCK TABLES `pay_record` WRITE;
/*!40000 ALTER TABLE `pay_record` DISABLE KEYS */;
INSERT INTO `pay_record` VALUES ('临猗县瑞帝斯混凝土有限公司',5000,'其他','E123456',816,'','广东工程机械有限公司','dengding','/check_Accout/付款记录/gd0001/临猗县瑞帝斯混凝土有限公司/2016-10-19_22_27_34.jpg',NULL,'gd0001','N',0,0,'[{\"contract\":\"6500056529319\",\"money\":5000}]','2016年10月19日22:27:34','','2016-10-gd0001',1),('汕头建筑有限公司',5180,'现金','',839,'','赵七','liusan','/check_Accout/付款记录/gd0001/汕头建筑有限公司/2016-10-20_06_57_41.jpg',0,'gd0001','W',0,0,'[{\"contract\":\"6199901Q124669038\",\"money\":5180}]','2016年10月20日06:57:41','','2016-10-gd0001',1),('李四',3698.38,'现金','',840,'','王五','lisi','/check_Accout/付款记录/gd0001/李四/2016-10-20_07_59_46.jpg',NULL,'gd0001','N',0,0,'[{\"contract\":\"65000552118980\",\"money\":3698.38}]','2016年10月20日07:59:46','','2016-10-gd0001',1),('三一测试公司',1000,'网银转账','D123456',847,'','测试','test','/check_Accout/付款记录/gd0001/三一测试公司/2016-10-20_15_01_55.webp',0,'gd0001','N',0,0,'[{\"contract\":\"5082121600046\",\"money\":300},{\"contract\":\"771909100\",\"money\":300},{\"contract\":\"7719091600000\",\"money\":400}]','2016年10月20日15:01:55','','2016-10-gd0001',0),('邓航',1000,'网银转账','K78965',848,'','张三','denghang','/check_Accout/付款记录/gd0001/邓航/2016-10-20_16_30_47.jpg',NULL,'gd0001','N',0,0,'[{\"contract\":\"6199901Q1131246\",\"money\":500},{\"contract\":\"KFZL1-654\",\"money\":500}]','2016年10月20日16:30:47','','2016-10-gd0001',1),('邓航',1000,'网银转账','K78965',849,'','里斯','denghang','/check_Accout/付款记录/gd0001/邓航/2016-10-20_16_32_56.jpg',0,'gd0001','N',0,0,'[{\"contract\":\"6199901Q1131246\",\"money\":500},{\"contract\":\"KFZL1-654\",\"money\":300}]','2016年10月20日16:32:56','','2016-10-gd0001',0),('安福县名骏商品混凝土有限责任公司',10000,'网银转账','D123456',850,'','广东工程机械有限公司','dengfa','/check_Accout/付款记录/gd0001/安福县名骏商品混凝土有限责任公司/2016-10-20_16_33_25.jpg',NULL,'gd0001','N',0,0,'[{\"contract\":\"50821216000465\",\"money\":4000},{\"contract\":\"7719091004\",\"money\":4000},{\"contract\":\"77190916000004\",\"money\":2000}]','2016年10月20日16:33:25','','2016-10-gd0001',1),('安达市建安建筑工程有限公司',1600,'电汇','B123456',851,'','广东工程有限公司','liufei','/check_Accout/付款记录/gd0001/安达市建安建筑工程有限公司/2016-10-20_17_09_14.jpg',0,'gd0001','N',0,0,'[{\"contract\":\"KFZLfxe11-654\",\"money\":1600}]','2016年10月20日17:09:14','','2016-10-gd0001',0),('邓航',777,'银行承诺汇票','K78965',852,'','Hhh','denghang','/check_Accout/付款记录/gd0001/邓航/2016-10-20_17_13_07.jpg',0,'gd0001','N',0,0,'[{\"contract\":\"6199901Q1131246\",\"money\":888},{\"contract\":\"KFZL1-654\",\"money\":654}]','2016年10月20日17:13:07','','2016-10-gd0001',0),('邓航',200,'网银转账','K78965',853,'','张三','denghang','/check_Accout/付款记录/gd0001/邓航/2016-10-20_17_17_22.jpg',0,'gd0001','N',0,0,'[{\"contract\":\"test\",\"money\":100},{\"contract\":\"6199901Q1131246\",\"money\":100}]','2016年10月20日17:17:22','','2016-10-gd0001',0);
/*!40000 ALTER TABLE `pay_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pay_record_cache`
--

DROP TABLE IF EXISTS `pay_record_cache`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pay_record_cache` (
  `payer` char(30) NOT NULL COMMENT '付款人',
  `pay_money` double NOT NULL COMMENT '付款金额',
  `pay_way` varchar(30) NOT NULL COMMENT '付款方式',
  `pay_account` char(11) DEFAULT NULL COMMENT '付款账户',
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一id',
  `vicePayer` varchar(15) DEFAULT NULL,
  `receiver` varchar(60) DEFAULT NULL COMMENT '款项接受人',
  `connPerson` varchar(15) DEFAULT NULL COMMENT '对账联系人',
  `linkCer` varchar(100) DEFAULT NULL COMMENT '凭证保存路径',
  `bankinput_id` int(11) DEFAULT NULL COMMENT '关联的出纳id',
  `owner` varchar(60) DEFAULT NULL COMMENT '付款信息所属代理',
  `checkResult` char(1) DEFAULT NULL COMMENT '审阅结果',
  `pass` tinyint(1) DEFAULT NULL,
  `isconnect` tinyint(1) DEFAULT NULL COMMENT '是否和出纳关联',
  `many_pay` varchar(1000) DEFAULT NULL COMMENT '付款的合同号及金额',
  `upload_time` varchar(20) DEFAULT NULL COMMENT '上传时间',
  `contract_num` varchar(10) DEFAULT NULL,
  `caid` varchar(30) DEFAULT NULL COMMENT '对账id',
  `freeback` tinyint(1) DEFAULT '0' COMMENT '返利标志',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=847 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pay_record_cache`
--

LOCK TABLES `pay_record_cache` WRITE;
/*!40000 ALTER TABLE `pay_record_cache` DISABLE KEYS */;
INSERT INTO `pay_record_cache` VALUES ('邓航',12,'网银转账','K78965',844,NULL,'吧','denghang','/check_Accout/付款记录/ah0001/邓航/2016-10-20_17_13_04.jpg',NULL,'ah0001',NULL,0,0,'[{\"contract\":\"6199901Q1131246\",\"money\":1},{\"contract\":\"KFZL1-654\",\"money\":11}]','2016年10月20日17:13:04',NULL,'2016-10-ah0001',NULL);
/*!40000 ALTER TABLE `pay_record_cache` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pay_record_history`
--

DROP TABLE IF EXISTS `pay_record_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pay_record_history` (
  `payer` char(30) NOT NULL COMMENT '付款人',
  `pay_money` double NOT NULL COMMENT '付款金额',
  `pay_way` varchar(30) NOT NULL COMMENT '付款方式',
  `pay_account` char(11) DEFAULT NULL COMMENT '付款账户',
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一id',
  `vicePayer` varchar(15) DEFAULT NULL,
  `receiver` varchar(60) DEFAULT NULL COMMENT '款项接受人',
  `connPerson` varchar(15) DEFAULT NULL COMMENT '对账联系人',
  `linkCer` varchar(100) DEFAULT NULL COMMENT '凭证保存路径',
  `bankinput_id` int(11) DEFAULT NULL COMMENT '关联的出纳id',
  `owner` varchar(60) DEFAULT NULL COMMENT '付款信息所属代理',
  `checkResult` char(1) DEFAULT NULL COMMENT '审阅结果',
  `pass` tinyint(1) DEFAULT NULL,
  `isconnect` tinyint(1) DEFAULT NULL COMMENT '是否和出纳关联',
  `many_pay` varchar(1000) DEFAULT NULL COMMENT '付款的合同号及金额',
  `upload_time` varchar(20) DEFAULT NULL COMMENT '上传时间',
  `contract_num` varchar(10) DEFAULT NULL,
  `caid` varchar(30) DEFAULT NULL COMMENT '对账id,本次对账的标志',
  `freeback` tinyint(1) DEFAULT '0' COMMENT '返利标志',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pay_record_history`
--

LOCK TABLES `pay_record_history` WRITE;
/*!40000 ALTER TABLE `pay_record_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `pay_record_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `pay_record_view`
--

DROP TABLE IF EXISTS `pay_record_view`;
/*!50001 DROP VIEW IF EXISTS `pay_record_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `pay_record_view` AS SELECT 
 1 AS `payer`,
 1 AS `pay_money`,
 1 AS `pay_way`,
 1 AS `pay_account`,
 1 AS `id`,
 1 AS `vicePayer`,
 1 AS `receiver`,
 1 AS `connPerson`,
 1 AS `linkCer`,
 1 AS `bankinput_id`,
 1 AS `owner`,
 1 AS `checkResult`,
 1 AS `pass`,
 1 AS `isconnect`,
 1 AS `many_pay`,
 1 AS `upload_time`,
 1 AS `contract_num`,
 1 AS `caid`,
 1 AS `freeback`,
 1 AS `agent_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `connect_person_view`
--

/*!50001 DROP VIEW IF EXISTS `connect_person_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `connect_person_view` AS select `connect_person`.`username` AS `username`,`connect_person`.`phone` AS `phone`,`connect_person`.`weixin` AS `weixin`,`connect_person`.`company` AS `company`,`connect_person`.`password` AS `password`,`connect_person`.`real_name` AS `real_name`,`connect_person`.`register_way` AS `register_way`,`connect_person`.`agent` AS `agent`,`connect_person`.`contract_mes` AS `contract_mes`,`connect_person`.`companyid` AS `companyid`,`connect_person`.`flag` AS `flag`,`connect_person`.`email` AS `email`,`connect_person`.`cardid` AS `cardid`,`connect_person`.`score` AS `score`,`agent`.`agent_name` AS `agent_name` from (`connect_person` join `agent`) where (`connect_person`.`agent` = `agent`.`agent_id`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `pay_record_view`
--

/*!50001 DROP VIEW IF EXISTS `pay_record_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `pay_record_view` AS select `pay_record`.`payer` AS `payer`,`pay_record`.`pay_money` AS `pay_money`,`pay_record`.`pay_way` AS `pay_way`,`pay_record`.`pay_account` AS `pay_account`,`pay_record`.`id` AS `id`,`pay_record`.`vicePayer` AS `vicePayer`,`pay_record`.`receiver` AS `receiver`,`pay_record`.`connPerson` AS `connPerson`,`pay_record`.`linkCer` AS `linkCer`,`pay_record`.`bankinput_id` AS `bankinput_id`,`pay_record`.`owner` AS `owner`,`pay_record`.`checkResult` AS `checkResult`,`pay_record`.`pass` AS `pass`,`pay_record`.`isconnect` AS `isconnect`,`pay_record`.`many_pay` AS `many_pay`,`pay_record`.`upload_time` AS `upload_time`,`pay_record`.`contract_num` AS `contract_num`,`pay_record`.`caid` AS `caid`,`pay_record`.`freeback` AS `freeback`,`agent`.`agent_name` AS `agent_name` from (`pay_record` left join `agent` on((`pay_record`.`owner` = `agent`.`agent_id`))) union all select `pay_record_cache`.`payer` AS `payer`,`pay_record_cache`.`pay_money` AS `pay_money`,`pay_record_cache`.`pay_way` AS `pay_way`,`pay_record_cache`.`pay_account` AS `pay_account`,`pay_record_cache`.`id` AS `id`,`pay_record_cache`.`vicePayer` AS `vicePayer`,`pay_record_cache`.`receiver` AS `receiver`,`pay_record_cache`.`connPerson` AS `connPerson`,`pay_record_cache`.`linkCer` AS `linkCer`,`pay_record_cache`.`bankinput_id` AS `bankinput_id`,`pay_record_cache`.`owner` AS `owner`,`pay_record_cache`.`checkResult` AS `checkResult`,`pay_record_cache`.`pass` AS `pass`,`pay_record_cache`.`isconnect` AS `isconnect`,`pay_record_cache`.`many_pay` AS `many_pay`,`pay_record_cache`.`upload_time` AS `upload_time`,`pay_record_cache`.`contract_num` AS `contract_num`,`pay_record_cache`.`caid` AS `caid`,`pay_record_cache`.`freeback` AS `freeback`,`agent`.`agent_name` AS `agent_name` from (`pay_record_cache` left join `agent` on((`pay_record_cache`.`owner` = `agent`.`agent_id`))) union all select `pay_record_history`.`payer` AS `payer`,`pay_record_history`.`pay_money` AS `pay_money`,`pay_record_history`.`pay_way` AS `pay_way`,`pay_record_history`.`pay_account` AS `pay_account`,`pay_record_history`.`id` AS `id`,`pay_record_history`.`vicePayer` AS `vicePayer`,`pay_record_history`.`receiver` AS `receiver`,`pay_record_history`.`connPerson` AS `connPerson`,`pay_record_history`.`linkCer` AS `linkCer`,`pay_record_history`.`bankinput_id` AS `bankinput_id`,`pay_record_history`.`owner` AS `owner`,`pay_record_history`.`checkResult` AS `checkResult`,`pay_record_history`.`pass` AS `pass`,`pay_record_history`.`isconnect` AS `isconnect`,`pay_record_history`.`many_pay` AS `many_pay`,`pay_record_history`.`upload_time` AS `upload_time`,`pay_record_history`.`contract_num` AS `contract_num`,`pay_record_history`.`caid` AS `caid`,`pay_record_history`.`freeback` AS `freeback`,`agent`.`agent_name` AS `agent_name` from (`pay_record_history` left join `agent` on((`pay_record_history`.`owner` = `agent`.`agent_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-10-30 16:59:45
