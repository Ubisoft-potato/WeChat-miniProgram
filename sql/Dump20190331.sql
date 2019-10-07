CREATE TABLE `user` (
  `id` varchar(50) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `gender` int(2) DEFAULT NULL,
  `province` varchar(45) DEFAULT NULL,
  `city` varchar(45) DEFAULT NULL,
  `country` varchar(45) DEFAULT NULL,
  `openid` varchar(70) DEFAULT NULL,
  `username` varchar(45) DEFAULT NULL,
  `password` varchar(45) DEFAULT NULL,
  `avatarURL` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



CREATE TABLE `allphotos` (
  `photoId` varchar(50) NOT NULL,
  `ownerId` varchar(50) NOT NULL,
  `instruction` varchar(512) DEFAULT NULL,
  `location` varchar(50) DEFAULT NULL,
  `photoURL` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `likeNum` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`photoId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



CREATE TABLE `commentreply` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commentid` varchar(50) NOT NULL COMMENT '被评论的评论id',
  `fromid` varchar(45) NOT NULL,
  `fromname` varchar(45) NOT NULL,
  `content` varchar(512) NOT NULL,
  `createTime` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `photocomment` (
  `id` varchar(45) NOT NULL COMMENT '评论id',
  `photoid` varchar(45) NOT NULL COMMENT '照片id',
  `fromid` varchar(45) NOT NULL COMMENT '评论者id',
  `fromname` varchar(45) NOT NULL COMMENT '评论者昵称',
  `fromURL` varchar(100) NOT NULL COMMENT '评论者头像',
  `content` varchar(512) NOT NULL COMMENT '评论内容',
  `createTime` varchar(45) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论者id';


