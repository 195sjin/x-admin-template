package com.jin.sys.service;

import com.jin.sys.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zijin
 * @since 2023-02-25
 */
public interface IMenuService extends IService<Menu> {

    List<Menu> getAllmenu();

    List<Menu> getMenuListByUserId(Integer userId);

}
