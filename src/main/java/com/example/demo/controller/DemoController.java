package com.example.demo.controller;



import com.example.demo.model.UserInfo;
import com.example.demo.service.UserService;
import com.example.demo.service.UserServiceImpl;
import com.example.demo.util.RedisUtil;

import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.OBJ_ADAPTER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.*;

@Controller
public class DemoController {
    private static Logger log = (Logger) LoggerFactory.getLogger(DemoController.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String,Serializable> redisTemplate;
    @Autowired
    private UserService userService;


    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        log.info("进入到方法");
        return "Hello world";
    }

    /**
     * 连接本地的redis服务和查看服务是否运行
     */
    @GetMapping("/redis")
    @ResponseBody
    public void getRedis() {
        //连接本地的Redis服务
        Jedis jedis = new Jedis("localhost");
       log.info("连接本地的Redis服务成功");
        //查看服务是否运行
       log.info("服务正在运行：" + jedis.ping());

    }

    /**
     * Redis Java String (字符串)实例
     */
    @GetMapping("/redisString")
    @ResponseBody
    public void redisStringJava() {
        //连接本地的redis服务
        Jedis jedis = new Jedis("localhost");
       log.info("连接成功");
        //设置redis字符串数据
        jedis.set("runoobkey", "www.runoob.com");
        //获取存储的数据并输出
       log.info("redis存储的字符串是" + jedis.get("runoobkey"));
    }

    /**
     * redis Java List(列表)实例
     */
    @GetMapping("/redisList")
    @ResponseBody
    public void redisListJava() {
        //连接本地的redis服务
        Jedis jedis = new Jedis("localhost");
       log.info("连接成功");
        jedis.lpush("site-list", "Runoob");
        jedis.lpush("site-list", "Google");
        jedis.lpush("site-list", "Taobao");
        //获取存储的数据并输出
        List<String> list = jedis.lrange("site-list", 0, 2);
        for (int i = 0; i < list.size(); i++) {
           log.info("列表项 为：" + list.get(i));
        }
    }

    /**
     * 查询所有的键的名字
     */
    @GetMapping("/redisKey")
    @ResponseBody
    public void redisKeyJava() {
        //连接本地的redis服务
        Jedis jedis = new Jedis("localhost");
       log.info("连接成功");
        Set<String> keys = jedis.keys("*");
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
           log.info(key);
        }
    }

    @GetMapping("/redisAll")
    @ResponseBody
    public void  redisAll(){
        Jedis myJedis = RedisUtil.getJedis();
        RedisUtil.close(myJedis);
    }

    @GetMapping("/redisMap")
    @ResponseBody
    public void redisMap(){
        Jedis jedis = RedisUtil.getJedis();
        Map<String,String> user = new HashMap<
                String,String>();
        user.put("name","mmd");
        user.put("pwd","password");
        jedis.hmset("user",user);
        //取出user中的name,执行结果[mmd]-->注意结果是一个泛型的List
        //第一个参数是存入redis中map对象的key,后面跟的是放入map中的对象的key，后面的key可以跟多个，是可变参数
        List<String> rsmap = jedis.hmget("user","name");
        System.out.println(rsmap);
    }

    @GetMapping("/redisUser")
    @ResponseBody
    public void userTest(){
        redisTemplate.opsForValue().set("userkey", new UserInfo(1,"张三",25));
        UserInfo user =(UserInfo) redisTemplate.opsForValue().get("userkey");
        log.info("当前获取对象：{}",user.toString());
    }
    @GetMapping("/redisUserCache")
    @ResponseBody
    public void testUserCache(){
        redisTemplate.opsForValue().set("userkey",new UserInfo(1,"张三",25));
        UserInfo user = (UserInfo) redisTemplate.opsForValue().get("userkey");
        log.info("当前获取对象：{}",user.toString());
    }

    @GetMapping("/redisAdd")
    @ResponseBody
    public void add(){
        UserInfo user = userService.save(new UserInfo(4,"乃万",25));
        log.info("添加的用户信息：{}",user.toString());
    }

    @GetMapping("/redisDelete")
    @ResponseBody
    public void  delete(){
        userService.delete(1);
    }

    @GetMapping("/redisGetId/{id}")
    @ResponseBody
    public void get(@PathVariable("id") String idStr) throws Exception{
        log.info("获取的id值为"+idStr);
        if(StringUtils.isBlank(idStr)){
            throw new Exception("id为空");
        }
        Integer id = Integer.parseInt(idStr);
        log.info("转化为Integer的值为"+id);
        UserInfo user = userService.get(id);
        log.info("获取的用户信息：{}",user.toString());
    }

       /*
       用于防止发生缓存击穿

       public static  String getData(String key) throws InterruptedException{
           //从redis查询数据
           String result = getDataByKV(key);
           //参数校验
           if (StringUtils.isBlank(result)){
               try{
                   //获得锁
                   if(reenLock.tryLock()){
                       //去数据库查询
                       result = getDataByDB(key);
                       //校验
                       if(StringUtils.isNotBlank(result)){
                           //插进缓存
                           setDataToKV(key,result);
                       }
                   }else{
                       //睡一会儿再拿
                       Thread.sleep(100L);
                       result = getData(key);
                   }
               }catch(Exception e){
                   e.printStackTrace();
               }finally {
                   //释放锁
                   reenLock.unlock();
               }
           }
           return result;
       }*/
@GetMapping("/redisFind/{id}")
    @ResponseBody
    public Map<String ,Object> findUserById(@PathVariable("id") int id){
    log.info("id的值为"+id);
           UserInfo user = userService.findUserById(id);
           Map<String,Object> map = new HashMap<>();
           map.put("id",user.getId());
           map.put("name",user.getName());
           map.put("age",user.getAge());
           return map;
}
@GetMapping("/redisUpdate/{id}")
    @ResponseBody
    public String updateUser(@PathVariable("id")int id){
       UserInfo user = new UserInfo();
       user.setId(id);
       user.setName("吗快快");
       user.setAge(24);
       int result = userService.updateUser(user);
       if (result!=0){
           return "update user success";
       }
       return "fail";

}

@GetMapping("/redisDelete/{id}")
@ResponseBody
    public String deleteUserById(@PathVariable("id")int id){
    log.info("id的值为"+id);
            int  result = userService.deleteUserById(id);
            if (result != 0){
                return "success";
            }
            return "fail";
}
}
