package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TestMapper {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectUser() {
       User user = userMapper.seltUserById(1);
       System.out.println(user);

       user = userMapper.seltUserByUsername("user583574");
       System.out.println(user);

       user = userMapper.seltUserByEmail("3394555@example.com");
       System.out.println(user);
   }

   @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("sy");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("sy@example.com");
        user.setHeaderUrl("http://www.nowcoder.com/avatar/123456.jpg");
        user.setCreateTime(new Date());

        int row = userMapper.insertUser(user);
        System.out.println(row);
        System.out.println(user.getId());
   }

   @Test
    public void testSelectDiscussPost() {
       List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10,0);
       for (DiscussPost discussPost : list) {
           System.out.println(discussPost);
       }
       int rows = discussPostMapper.selectDiscussPostRows(0);
       System.out.println(rows);
   }

   @Test
    public void tsetInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
       loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60*10));
       loginTicketMapper.insertLoginTicket(loginTicket);
   }
    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }
}
