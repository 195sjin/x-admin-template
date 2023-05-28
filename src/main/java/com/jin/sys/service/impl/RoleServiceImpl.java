package com.jin.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jin.sys.entity.Role;
import com.jin.sys.entity.RoleMenu;
import com.jin.sys.mapper.RoleMapper;
import com.jin.sys.mapper.RoleMenuMapper;
import com.jin.sys.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zijin
 * @since 2023-02-25
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Override
    @Transactional
    public void addRole(Role role) {
        //插入角色表
        this.baseMapper.insert(role);

        //插入角色菜单关系表
        if (role.getMenuIdList() != null){
            for (Integer menuId : role.getMenuIdList()) {
                roleMenuMapper.insert(new RoleMenu(null, role.getRoleId(), menuId));
            }
        }
    }

    @Override
    public Role getRoleById(Integer id) {
        Role role = this.getById(id);
        List<Integer> menuIdList= roleMenuMapper.getMenuIdListByRoleId(id);
        role.setMenuIdList(menuIdList);
        return role;
    }

    @Override
    @Transactional
    public void updateRole(Role role) {
        //修改角色表
        this.baseMapper.updateById(role);
        //删除原有权限
        Integer roleId = role.getRoleId();
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId,roleId));
        //新增角色菜单关系表
        List<Integer> menuIdList = role.getMenuIdList();
        if (menuIdList != null){
            for (Integer menuId : menuIdList) {
                roleMenuMapper.insert(new RoleMenu(null,roleId,menuId));
            }
        }
    }

    @Override
    @Transactional
    public void deleteRoleById(Integer id) {
        //删除角色表
        this.baseMapper.deleteById(id);
        //删除原有权限
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId,id));
    }
}
