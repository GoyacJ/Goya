# Goya API 设计器

## 描述
辅助设计和生成符合 RESTful 规范的 API。

## 使用场景
- 设计新的 API
- 生成 Controller 代码
- 生成 API 文档
- 设计请求/响应结构

## 使用方法

描述 API 需求，例如：
- "设计用户管理的 REST API"
- "创建订单查询接口"

## 生成内容

### 1. Controller
```java
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public Response<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Response.ok(UserConverter.INSTANCE.toVO(user));
    }
    
    @PostMapping
    public Response<UserVO> create(@RequestBody @Valid UserCreateDTO dto) {
        User user = userService.create(dto);
        return Response.ok(UserConverter.INSTANCE.toVO(user));
    }
}
```

### 2. DTO
- UserCreateDTO
- UserUpdateDTO
- UserQueryDTO

### 3. VO
- UserVO

### 4. Converter
```java
@Mapper(componentModel = "spring")
public interface UserConverter {
    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);
    
    UserVO toVO(User user);
    User toEntity(UserCreateDTO dto);
}
```

## RESTful 规范

- GET - 查询
- POST - 创建
- PUT - 更新
- DELETE - 删除
- 统一响应格式（Response）
- 错误码规范

## 参考资料

- RESTful API 设计规范
- Goya API 规范
