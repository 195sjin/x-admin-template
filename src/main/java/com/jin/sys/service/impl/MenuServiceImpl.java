package com.jin.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jin.sys.entity.Menu;
import com.jin.sys.mapper.MenuMapper;
import com.jin.sys.service.IMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    @Override
    public List<Menu> getAllmenu() {
        //查询一级菜单
        List<Menu> menuList = this.list(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, 0));
        setMenuChildren(menuList);
        return menuList;
    }

    private void setMenuChildren(List<Menu> menuList) {
        if (menuList != null){
            for (Menu menu : menuList) {
                List<Menu> list = this.list(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, menu.getMenuId()));
                menu.setChildren(list);
                //递归
                setMenuChildren(list);
            }
        }
    }

    @Override
    public List<Menu> getMenuListByUserId(Integer userId) {
        //一级菜单
        List<Menu> menuList = this.getBaseMapper().getMenuListByUserId(userId,0);

        //子菜单
        setMenuChildrenByUserId(userId, menuList);
        return menuList;
    }

    private void setMenuChildrenByUserId(Integer userId, List<Menu> menuList) {
        if (menuList != null){
            for (Menu menu : menuList) {
                List<Menu> subMenuList = this.getBaseMapper().getMenuListByUserId(userId, menu.getMenuId());
                menu.setChildren(subMenuList);
                setMenuChildrenByUserId(userId,subMenuList);
            }
        }
    }
}
