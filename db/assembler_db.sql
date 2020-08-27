/*
Navicat MySQL Data Transfer

Source Server         : assembler_db
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : assembler_db

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2020-08-02 20:30:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `config`
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` char(255) DEFAULT NULL,
  `value` char(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of config
-- ----------------------------
INSERT INTO `config` VALUES ('1', 'output_dir', null);
INSERT INTO `config` VALUES ('2', 'input_dir', null);

-- ----------------------------
-- Table structure for `task`
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `serial` char(255) DEFAULT NULL,
  `name` char(255) DEFAULT NULL,
  `type` char(255) DEFAULT NULL,
  `files` char(255) DEFAULT NULL,
  `path` char(255) DEFAULT NULL,
  `cmd` char(255) DEFAULT NULL,
  `process_id` char(255) DEFAULT NULL,
  `result` char(255) DEFAULT NULL,
  `result_dir` char(255) DEFAULT NULL,
  `submit_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `finish_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `state` int(11) DEFAULT 0,
  `isdelete` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of task
-- ----------------------------
