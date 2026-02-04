# 开发指南 | Development Guide

本指南详细介绍 Goya 项目的开发规范、最佳实践和常见开发场景。

## 代码规范

### Java 规范

详见项目 `.cursorrules` 和 `.cursor/rules/` 目录下的规则文件。

#### 命名规范

```java
// ✅ 正确
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}

// ❌ 错误
public class user_service_impl implements I_User_Service {
    private UserRepository user_repository;  // 不要使用下划线
    
    public User get_user_by_id(Long user_id) {  // 方法名不应使用下划线
        return user_repository.findById(user_id).orElse(null);
    }
}
```

#### 注解使用

```java
// ✅ 正确 - 必须使用的注解
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(rollbackFor = Exception.class)
    public void createUser(User user) {
        userRepository.save(user);
        log.info("[Goya] |- User created: {}", user.getId());
    }
}

// ❌ 错误 - 缺少必要注解
public class UserService {
    
    @Autowired  // 应使用构造器注入
    private UserRepository userRepository;
    
    @Transactional  // 应指定 rollbackFor
    public void createUser(User user) {
        userRepository.save(user);
        System.out.println("User created");  // 应使用日志
    }
}
```

#### 注释规范

```java
/**
 * 用户服务实现类
 * <p>
 * 提供用户管理的核心业务逻辑，包括用户创建、查询、更新和删除
 * 
 * <p>使用示例：
 * <pre>{@code
 * UserService userService = new UserServiceImpl(userRepository);
 * User user = userService.getUserById(1L);
 * }</pre>
 *
 * @author goya
 * @since 2025-01-24
 * @see IUserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    
    private final UserRepository userRepository;
    
    /**
     * 根据用户 ID 获取用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息，如果不存在则返回 null
     */
    @Override
    public User getUserById(Long userId) {
        log.debug("[Goya] |- Fetching user by id: {}", userId);
        return userRepository.findById(userId).orElse(null);
    }
}
```

### TypeScript/Vue 规范

#### 组件规范

```vue
<!-- ✅ 正确 - 使用 script setup -->
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

interface Props {
  userId: number
}

const props = defineProps<Props>()
const authStore = useAuthStore()

const user = ref<User | null>(null)
const isLoading = ref(false)

const displayName = computed(() => {
  return user.value?.nickname || user.value?.username || '未知用户'
})

async function fetchUser() {
  isLoading.value = true
  try {
    user.value = await getUserInfoApi(props.userId)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="user-card">
    <div v-if="isLoading">Loading...</div>
    <div v-else>{{ displayName }}</div>
  </div>
</template>
```

#### API 函数命名

```typescript
// ✅ 正确
export async function loginApi(data: LoginRequest): Promise<LoginResponse> {
  return request.post('/api/auth/login', data)
}

export async function getUserInfoApi(userId: number): Promise<User> {
  return request.get(`/api/users/${userId}`)
}

// ❌ 错误
export async function login(data: LoginRequest) {  // 应添加 Api 后缀
  return request.post('/api/auth/login', data)
}
```

## 模块开发

### 创建新组件模块

1. **创建模块目录**：

```bash
cd Goya/component
mkdir -p component-xxx/src/main/java/com/ysmjjsy/goya/component/xxx
mkdir -p component-xxx/src/main/resources/META-INF/spring
```

2. **创建 pom.xml**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.ysmjjsy.goya</groupId>
        <artifactId>component</artifactId>
        <version>${revision}</version>
    </parent>
    
    <artifactId>component-xxx</artifactId>
    <description>XXX 组件</description>
    
    <dependencies>
        <dependency>
            <groupId>com.ysmjjsy.goya</groupId>
            <artifactId>component-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```

3. **创建自动配置类**：

```java
package com.ysmjjsy.goya.component.xxx.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * XXX 自动配置
 *
 * @author goya
 * @since 2025-01-24
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(XxxService.class)
@EnableConfigurationProperties(XxxProperties.class)
public class XxxAutoConfiguration {
    
    public XxxAutoConfiguration() {
        log.debug("[Goya] |- component [xxx] XxxAutoConfiguration auto configure.");
    }
    
    @Bean
    public XxxService xxxService() {
        log.trace("[Goya] |- component [xxx] |- bean [xxxService] register.");
        return new XxxService();
    }
}
```

4. **创建自动配置注册文件**：

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
com.ysmjjsy.goya.component.xxx.configuration.XxxAutoConfiguration
```

5. **更新父 pom.xml**：

```xml
<modules>
    <module>component-xxx</module>
</modules>
```

### SPI 模式开发

定义 SPI 接口供业务实现：

```java
package com.ysmjjsy.goya.component.security.core.service;

/**
 * 用户服务 SPI
 * <p>
 * 业务系统需实现此接口以提供用户查询能力
 *
 * @author goya
 * @since 2025-01-24
 */
public interface IUserService {
    
    /**
     * 根据用户名加载用户
     *
     * @param username 用户名
     * @return 安全用户信息
     */
    SecurityUser loadUserByUsername(String username);
    
    /**
     * 根据手机号加载用户
     *
     * @param mobile 手机号
     * @return 安全用户信息
     */
    SecurityUser loadUserByMobile(String mobile);
}
```

业务系统实现：

```java
@Service
public class UserServiceImpl implements IUserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public SecurityUser loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        return SecurityUser.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(convertAuthorities(user.getRoles()))
            .build();
    }
}
```

## 数据库开发

### MyBatis Plus

```java
// 1. Entity
@Data
@TableName("sys_user")
public class User extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    @FieldEncrypt
    private String mobile;
    
    @TableLogic
    private Boolean deleted;
}

// 2. Mapper
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);
}

// 3. Service
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    public User getByUsername(String username) {
        return baseMapper.findByUsername(username);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void createUser(User user) {
        save(user);
    }
}
```

### Spring Data JPA

```java
// 1. Entity
@Entity
@Table(name = "sys_user")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column
    private String mobile;
}

// 2. Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.mobile = :mobile")
    Optional<User> findByMobile(@Param("mobile") String mobile);
}

// 3. Service
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElse(null);
    }
}
```

## 缓存开发

### 使用注解

```java
@Service
public class UserService {
    
    @Cacheable(key = "'user:' + #id", unless = "#result == null")
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @CachePut(key = "'user:' + #user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @CacheEvict(key = "'user:' + #id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

### 手动操作缓存

```java
@Service
public class UserService {
    
    @Autowired
    private CacheService cacheService;
    
    public User getUser(Long id) {
        // 使用 cacheName + key 的方式
        return cacheService.getOrLoad("user", id, () -> {
            return userRepository.findById(id).orElse(null);
        });
    }
    
    public void updateUser(User user) {
        userRepository.save(user);
        // 更新缓存
        cacheService.put("user", user.getId(), user, Duration.ofMinutes(10));
    }
}
```

## 异步处理

### 使用 @Async

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("goya-async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {
    
    @Async
    public CompletableFuture<Void> sendEmail(String to, String subject, String content) {
        log.info("Sending email to: {}", to);
        // 发送邮件逻辑
        return CompletableFuture.completedFuture(null);
    }
}
```

## 事件驱动

### 发布事件

```java
@Service
public class UserService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Transactional(rollbackFor = Exception.class)
    public void createUser(User user) {
        userRepository.save(user);
        
        // 发布用户创建事件
        eventPublisher.publishEvent(new UserCreatedEvent(this, user));
    }
}
```

### 监听事件

```java
@Component
public class UserEventListener {
    
    @EventListener
    @Async
    public void handleUserCreated(UserCreatedEvent event) {
        User user = event.getUser();
        log.info("User created event received: {}", user.getId());
        
        // 发送欢迎邮件
        // 初始化用户数据
    }
}
```

## 测试

### 单元测试

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @Test
    void testGetUser() {
        // Given
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(mockUser));
        
        // When
        User user = userService.getUser(1L);
        
        // Then
        assertNotNull(user);
        assertEquals("test", user.getUsername());
    }
}
```

### 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0200"))
            .andExpect(jsonPath("$.data.username").value("test"));
    }
}
```

## 日志

### 日志级别

```yaml
logging:
  level:
    root: INFO
    com.ysmjjsy.goya: DEBUG
    org.springframework.security: DEBUG
```

### 日志使用

```java
@Slf4j
@Service
public class UserService {
    
    public User getUser(Long id) {
        log.debug("[Goya] |- Fetching user by id: {}", id);
        
        User user = userRepository.findById(id).orElse(null);
        
        if (user == null) {
            log.warn("[Goya] |- User not found: {}", id);
        } else {
            log.info("[Goya] |- User fetched successfully: {}", user.getUsername());
        }
        
        return user;
    }
}
```

## 下一步

- [部署指南](./deployment.md)
- [架构设计](../architecture/overview.md)
- [模块详解](../architecture/modules.md)
- [AI 助手使用指南](../../.cursor/AI_ASSISTANT_GUIDE.md) - 使用 Cursor AI 开发必读
