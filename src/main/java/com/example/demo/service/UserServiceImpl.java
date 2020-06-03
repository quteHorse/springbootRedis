package com.example.demo.service;


import com.example.demo.mapper.UserMapper;
import com.example.demo.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    public Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
     private UserMapper userMapper;

    /*
    连接池自动管理，提供了一个高度封装的"RedisTemplate"类
     */
    @Autowired
    private RedisTemplate redisTemplate;

    private static Map<Integer, UserInfo> userMap = new HashMap<>();

    static {
        userMap.put(1, new UserInfo(1, "安崎", 25));
        userMap.put(2, new UserInfo(2, "刘雨昕", 23));
        userMap.put(3, new UserInfo(3, "上官喜爱", 26));
    }
//根据方法的请求参数对其结果进行缓存，和@Cacheable不同的是，它每次都会触发真实方法的调用
    @CachePut(value = "user", key = "#user.id")
    @Override
    public UserInfo save(UserInfo user) {
        userMap.put(user.getId(), user);
        log.info("进入到save方法，当前存储对象：{}", user.toString());
        return user;
    }


    //根据条件对缓存进行清空
    @CacheEvict(value = "user", key = "#id")
    @Override
    public void delete(int id) {
        userMap.remove(id);
        log.info("进入到delete方法，删除成功");
    }
//此注解根据方法的请求参数对其结果进行缓存，当controller开始调用这个方法的时候，直接从缓存里面看看有没有，有的话就不执行这个方法了。
    @Cacheable(value = "user", key = "#id")
    @Override
    public UserInfo get(Integer id) {
        log.info("进入到get方法，当前获取对象：{}", userMap.get(id) == null ? null : userMap.get(id).toString());
        return userMap.get(id);
    }

    public List<UserInfo> queryAll(){
        return userMapper.queryAll();
    }

    /*
    获取用户策略：先从缓存中获取用户，没有则取数据表中的数据，再将数据写入缓存
     */
    @Override
    public UserInfo findUserById(int id){
        String key = "user_"+id;
        //此处的ValueOperations是指简单K-V操作，通过opsForValue()还可以调用到很多其他方法。
        //左右的类型转换是通过一个Editor,类似于工厂模式的实现。把redisTemplate注入到ValueOperations
        ValueOperations<String,UserInfo> operations = redisTemplate.opsForValue();
        //判断是否有key所对应的值，有则返回true,没有则返回false
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey){
            long start = System.currentTimeMillis();
            UserInfo user  = operations.get(key);
            log.info("===========从缓存中获得数据============");
            log.info(user.getName());
            log.info("===================================");
            long end = System.currentTimeMillis();
            log.info("查询redis花费的时间是："+(end-start)+"s");
            return  user;
        }else{
            long start = System.currentTimeMillis();
            UserInfo user = userMapper.findUserById(id);
            log.info("===========从数据表中获得数据============");
            log.info(user.getName());
            log.info("===================================");

            //写入缓存，并设置变量值的过期时间，5小时
            operations.set(key,user,5,TimeUnit.HOURS);
            long end = System.currentTimeMillis();
            log.info("查询mysql花费的时间是："+(end-start)+"s");
            return user;
        }
    }

  /*
    更新用户策略：先更新数据表，成功之后，删除原来的缓存，再更新缓存
     */
    @Override
    public int updateUser(UserInfo userInfo) {
        ValueOperations<String,UserInfo> operations = redisTemplate.opsForValue();
        int result = userMapper.updateUser(userInfo);
        if (result !=0){
            String key = "user_"+userInfo.getId();
            boolean haskey = redisTemplate.hasKey(key);
            if (haskey){
                redisTemplate.delete(key);
                log.info("删除缓存中的key==========>"+key);
            }
            //再将跟新后的数据加入缓存
            UserInfo userNew = userMapper.findUserById(userInfo.getId());
            if (userNew != null){
                operations.set(key,userNew,3,TimeUnit.HOURS);
            }
        }
        return  result;
    }

    @Override
    public int deleteUserById(int id) {
         int result = userMapper.deleteUserById(id);
         String key = "user_"+id;
         if (result!=0){
             boolean haskey = redisTemplate.hasKey(key);
             if (haskey){
                 redisTemplate.delete(key);
                 log.info("删除了缓存中的key"+key);
             }
         }
         return  result;
    }

}
