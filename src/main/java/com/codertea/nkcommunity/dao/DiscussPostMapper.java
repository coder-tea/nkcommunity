package com.codertea.nkcommunity.dao;

import com.codertea.nkcommunity.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // 拼接动态SQL，传入userId时查询当前用户发表的帖子，否则查询所有帖子
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                         @Param("offset") int offset, @Param("limit") int limit);

    // 查询帖子总数 @Param注解用于给参数取别名，当这个方法中只有一个参数时并且这个参数在mapper.xml文件中被使用在<if>标签中时，就必须要使用@Param注解来给这个参数取个别名
    int selectDiscussPostRows(@Param("userId") int userId);

    // 新增帖子
    int insertDiscussPost(DiscussPost discussPost);

    // 查询帖子详情
    DiscussPost selectDiscussPostById(int id);

    // 修改帖子的评论数量
    int updateCommentCount(int id, int commentCount);
}
