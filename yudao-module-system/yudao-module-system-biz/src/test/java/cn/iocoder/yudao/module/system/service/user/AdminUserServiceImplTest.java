package cn.iocoder.yudao.module.system.service.user;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.UserPostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.UserPostMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.service.BaseServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * {@link AdminUserServiceImpl} 的单元测试
 */
public class AdminUserServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Mock
    private AdminUserMapper userMapper;

    @Mock
    private UserPostMapper userPostMapper;



    private UserSaveReqVO createReqVO;

    @BeforeEach
    public void setUp() {
        // 初始化通用的Mock方法
        initMocks();
        
        // 准备测试数据
        createReqVO = new UserSaveReqVO();
        createReqVO.setUsername("testUser");
        createReqVO.setPassword("testPassword");
        createReqVO.setNickname("测试用户");
        createReqVO.setEmail("test@example.com");
        createReqVO.setMobile("13800138000");
        createReqVO.setDeptId(1L);
        Set<Long> postIds = new HashSet<>();
        postIds.add(1L);
        postIds.add(2L);
        createReqVO.setPostIds(postIds);
    }

    @Test
    @DisplayName("测试创建用户成功")
    public void testCreateUser_success() {
        // mock 方法
        // 1. mock 密码加密 - 已在基类中处理
        when(passwordEncoder.encode(createReqVO.getPassword())).thenReturn("encodedPassword");
        // 注意：部门和岗位验证已在基类的initMocks()方法中处理

        // 3. mock 用户插入
        when(userMapper.insert(any(AdminUserDO.class))).thenAnswer(invocation -> {
            AdminUserDO userDO = invocation.getArgument(0);
            userDO.setId(1L); // 模拟数据库生成ID
            return 1;
        });

        // 4. mock 岗位关联插入
        when(userPostMapper.insertBatch(any())).thenReturn(true);

        // 执行方法
        Long userId = adminUserService.createUser(createReqVO);

        // 断言
        assertEquals(1L, userId);

        // 验证用户插入的参数正确性
        ArgumentCaptor<AdminUserDO> userCaptor = ArgumentCaptor.forClass(AdminUserDO.class);
        verify(userMapper).insert(userCaptor.capture());
        AdminUserDO userDO = userCaptor.getValue();
        assertEquals(createReqVO.getUsername(), userDO.getUsername());
        assertEquals("encodedPassword", userDO.getPassword());
        assertEquals(createReqVO.getNickname(), userDO.getNickname());
        assertEquals(createReqVO.getEmail(), userDO.getEmail());
        assertEquals(createReqVO.getMobile(), userDO.getMobile());
        assertEquals(createReqVO.getDeptId(), userDO.getDeptId());
        assertEquals(createReqVO.getPostIds(), userDO.getPostIds());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), userDO.getStatus());

        // 验证岗位关联插入
        ArgumentCaptor<List<UserPostDO>> postCaptor = ArgumentCaptor.forClass(List.class);
        verify(userPostMapper).insertBatch(postCaptor.capture());
        List<UserPostDO> postDOs = postCaptor.getValue();
        assertEquals(2, postDOs.size());
        assertTrue(postDOs.stream().allMatch(post -> post.getUserId().equals(1L)));
        Set<Long> capturedPostIds = CollectionUtils.convertSet(postDOs, UserPostDO::getPostId);
        assertEquals(createReqVO.getPostIds(), capturedPostIds);
    }



    @Test
    @DisplayName("测试创建用户 - 无岗位")
    public void testCreateUser_withoutPosts() {
        // 准备测试数据 - 无岗位
        createReqVO.setPostIds(null);

        // mock 方法
        when(passwordEncoder.encode(createReqVO.getPassword())).thenReturn("encodedPassword");

        when(userMapper.insert(any(AdminUserDO.class))).thenAnswer(invocation -> {
            AdminUserDO userDO = invocation.getArgument(0);
            userDO.setId(1L);
            return 1;
        });

        // 执行方法
        Long userId = adminUserService.createUser(createReqVO);

        // 断言
        assertEquals(1L, userId);

        // 验证用户插入
        verify(userMapper).insert(any(AdminUserDO.class));
        
        // 验证没有进行岗位关联插入
        verify(userPostMapper, never()).insertBatch(any());
    }
}
