package com.jin.sys.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jin.common.utils.JwtUtil;
import com.jin.sys.entity.Menu;
import com.jin.sys.entity.User;
import com.jin.sys.entity.UserRole;
import com.jin.sys.mapper.UserMapper;
import com.jin.sys.mapper.UserRoleMapper;
import com.jin.sys.service.IMenuService;
import com.jin.sys.service.IUserRoleService;
import com.jin.sys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zijin
 * @since 2023-02-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private IMenuService menuService;


    /*@Override
    public Map<String, Object> login(User user) {
        //根据用户名和密码查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        wrapper.eq(User::getPassword,user.getPassword());
        User loginUser = this.baseMapper.selectOne(wrapper);

        //结果不为空则查询到用户，则生成token返回给用户来作为唯一标识，并将用户信息存入redis
        if (loginUser != null){
            //生成token，暂时先使用uuid,最好是用jwt
            String key = "user:"+UUID.randomUUID();

            //存入redis
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

            //返回数据
            HashMap<String, Object> data = new HashMap<>();
            data.put("token",key);
            return data;
        }

        return null;
    }*/

    @Override
    public Map<String, Object> login(User user) {
        //根据用户名查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        User loginUser = this.baseMapper.selectOne(wrapper);

        //结果不为空，并且密码和传入的密码匹配，则生成token返回给用户来作为唯一标识，并将用户信息存入redis
        if (loginUser != null && passwordEncoder.matches(user.getPassword(),loginUser.getPassword())){
            //生成token，暂时先使用uuid,最好是用jwt
            //String key = "user:"+UUID.randomUUID();
            //存入redis
            loginUser.setPassword(null);
            //redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

            //创建jwt
            String token = jwtUtil.createToken(loginUser);

            //返回数据
            HashMap<String, Object> data = new HashMap<>();
            data.put("token",token);
            return data;
        }
        return null;
    }

    @Override
    public Map<String, Object> getUserInfo(String token) {

        //根据token获取用户信息
        //Object obj = redisTemplate.opsForValue().get(token);
        User user = null;
        try {
            user = jwtUtil.parseToken(token, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user != null){
            //User loginUser = JSON.parseObject(JSON.toJSONString(obj), User.class);
            Map<String, Object> data = new HashMap<>();
            data.put("name",user.getUsername());
            data.put("avatar",user.getAvatar());

            //角色
            List<String> roleList = this.baseMapper.getRoleNameByUserId(user.getId());
            data.put("roles",roleList);

            //权限列表
            List<Menu> menuList = menuService.getMenuListByUserId(user.getId());
            data.put("menuList",menuList);


            return data;
        }
        return null;

    }

    @Override
    public void logout(String token) {
       // redisTemplate.delete(token);

    }

    @Override
    @Transactional
    public void addUser(User user) {
        //写入用户表
        this.baseMapper.insert(user);
        //写入用户角色表
        List<Integer> roleIdList = user.getRoleIdList();
        if (roleIdList != null){
            for (Integer roleId : roleIdList) {
                userRoleMapper.insert(new UserRole(null, user.getId(), roleId));
            }
        }

    }

    @Override
    @Transactional
    public User getUserById(Integer id) {
        User user = this.baseMapper.selectById(id);

        List<UserRole> userRoleList = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));

        List<Integer> roleIdList = userRoleList.stream().map(userRole -> {
                    return userRole.getRoleId();
                })
                .collect(Collectors.toList());
        user.setRoleIdList(roleIdList);

        return user;
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        //修改用户表的数据
        this.baseMapper.updateById(user);
        //清除原有角色
       userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId,user.getId()));
        //设置新的角色
        List<Integer> roleIdList = user.getRoleIdList();
        if (roleIdList != null){
            for (Integer roleId : roleIdList) {
                userRoleMapper.insert(new UserRole(null,user.getId(),roleId));
            }

        }
    }

    @Override
    @Transactional
    public void deleteUserById(Integer id) {
        this.baseMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId,id));
    }
}
