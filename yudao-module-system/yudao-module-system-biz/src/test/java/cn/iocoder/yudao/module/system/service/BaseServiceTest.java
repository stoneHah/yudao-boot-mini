package cn.iocoder.yudao.module.system.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.dept.PostService;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Service 层单元测试的基类
 * 
 * 提供常用的 Mock 对象，减少每个测试类的重复代码
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    // ========== 常用的 Mock 对象 ==========
    
    @Mock
    protected DeptService deptService;
    
    @Mock
    protected PostService postService;
    
    @Mock
    protected RoleService roleService;
    
    @Mock
    protected MenuService menuService;
    
    @Mock
    protected PermissionService permissionService;
    
    @Mock
    protected PasswordEncoder passwordEncoder;
    
    // ========== 常用的 Mock 方法 ==========
    
    /**
     * 初始化常用的 Mock 方法
     */
    protected void initMocks() {
        // Mock 部门和岗位验证逻辑
        doNothing().when(deptService).validateDeptList(anyCollection());
        doNothing().when(postService).validatePostList(anyCollection());
        
        // Mock 密码编码
        when(passwordEncoder.encode(any(String.class))).thenAnswer(invocation -> {
            String rawPassword = invocation.getArgument(0);
            return "encoded:" + rawPassword;
        });
        
        // 可以根据需要添加更多常用的 Mock 方法
    }
    
    /**
     * 创建分页结果
     *
     * @param list 数据列表
     * @param total 总记录数
     * @param <T> 数据类型
     * @return 分页结果
     */
    protected <T> PageResult<T> createPageResult(Collection<T> list, long total) {
        return new PageResult<T>(list instanceof List ? (List<T>) list : new ArrayList<>(list), total);
    }
}
