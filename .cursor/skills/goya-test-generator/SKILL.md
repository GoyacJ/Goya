# Goya 测试用例生成器

## 描述
自动生成单元测试和集成测试代码。

## 使用场景
- 为新代码生成测试
- 提升测试覆盖率
- 生成测试数据

## 使用方法

提供要测试的类或方法，例如：
- "为 UserService 生成测试用例"
- "生成 UserController 的集成测试"

## 生成内容

### 1. 单元测试
```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserMapper userMapper;
    
    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void testGetById_Success() {
        // Given
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("test");
        
        when(userMapper.selectById(userId))
            .thenReturn(mockUser);
        
        // When
        User result = userService.getById(userId);
        
        // Then
        assertNotNull(result);
        assertEquals("test", result.getUsername());
    }
}
```

### 2. 集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("查询用户 - 成功")
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0200"));
    }
}
```

### 3. 测试数据

生成测试所需的 Mock 数据

## 测试覆盖

自动生成以下测试：
- 正常流程
- 边界条件
- 异常情况
- 空值处理

## 规范要求

- 使用 AAA 模式
- 添加 @DisplayName
- Mock 外部依赖
- 断言完整

## 参考资料

- JUnit 5 文档
- Mockito 文档
- Goya 测试规范
