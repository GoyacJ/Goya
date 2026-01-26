# Goya 数据库代码生成器

## 描述
根据表结构生成 Entity、Mapper、Service 等完整代码。

## 使用场景
- 新建表后生成代码
- 快速生成 CRUD
- 生成标准的三层结构

## 使用方法

提供表结构或表名，例如：
- "为 sys_user 表生成代码"
- "生成用户表的 Entity 和 Mapper"

## 生成内容

### 1. Entity
```java
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
```

### 2. Mapper
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    User findByUsername(@Param("username") String username);
}
```

### 3. Service
```java
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {
    public User getByUsername(String username) {
        return baseMapper.findByUsername(username);
    }
}
```

### 4. Controller（可选）
```java
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    
    @GetMapping("/{id}")
    public Response<User> getById(@PathVariable Long id) {
        return Response.ok(userService.getById(id));
    }
}
```

## 规范要求

- 继承 BaseEntity
- 使用 @TableLogic 软删除
- 敏感字段添加 @FieldEncrypt
- 完整的中文注释
- 遵循命名规范

## 参考资料

- MyBatis Plus 文档
- Goya 数据库规范
