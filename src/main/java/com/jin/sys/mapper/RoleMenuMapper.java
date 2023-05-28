package com.jin.sys.mapper;

import com.jin.sys.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zijin
 * @since 2023-02-25
 */
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
        List<Integer> getMenuIdListByRoleId(Integer id);
}
