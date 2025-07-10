package com.rich.richsynapsehub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rich.richsynapsehub.common.ErrorCode;
import com.rich.richsynapsehub.constant.CommonConstant;
import com.rich.richsynapsehub.constant.UserConstant;
import com.rich.richsynapsehub.exception.BusinessException;
import com.rich.richsynapsehub.mapper.UserMapper;
import com.rich.richsynapsehub.model.dto.user.UserQueryRequest;
import com.rich.richsynapsehub.model.dto.user.UserRegisterRequest;
import com.rich.richsynapsehub.model.dto.user.UserUpdateMyRequest;
import com.rich.richsynapsehub.model.entity.User;
import com.rich.richsynapsehub.enumeration.UserRoleEnum;
import com.rich.richsynapsehub.model.vo.LoginUserVO;
import com.rich.richsynapsehub.model.vo.UserVO;
import com.rich.richsynapsehub.service.UserService;
import com.rich.richsynapsehub.utils.SaTokenLoginDeviceUtils;
import com.rich.richsynapsehub.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.rich.richsynapsehub.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author DuRuiChi
 * @return
 * @create 2025/3/20
 **/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * 加密盐值
     */
    public static final String SALT = "rich";

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userAavatar = userRegisterRequest.getUserAavatar();
        String userProfile = userRegisterRequest.getUserProfile();
        String userName = userRegisterRequest.getUserName();
        String phoneNumber = userRegisterRequest.getPhoneNumber();
        String email = userRegisterRequest.getEmail();
        String grade = userRegisterRequest.getGrade();
        String workExperience = userRegisterRequest.getWorkExperience();
        String expertiseDirection = userRegisterRequest.getExpertiseDirection();

        // 非空校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName, phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(userAavatar)) {
            userAavatar = UserConstant.DEFAULT_USER_PICTURE;
        }
        if (StringUtils.isAnyBlank(userProfile)) {
            userProfile = UserConstant.DEFAULT_USER_PROFILE;
        }
        if (StringUtils.isAnyBlank(email)) {
            email = UserConstant.DEFAULT_USER_MSG;
        }
        if (StringUtils.isAnyBlank(grade)) {
            grade = UserConstant.DEFAULT_USER_MSG;
        }
        if (StringUtils.isAnyBlank(workExperience)) {
            workExperience = UserConstant.DEFAULT_USER_MSG;
        }
        if (StringUtils.isAnyBlank(expertiseDirection)) {
            expertiseDirection = UserConstant.DEFAULT_USER_MSG;
        }
        // 合法校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName, phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (userProfile.length() >= 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户简介过长");
        }
        if (grade.length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请正确输入年级，如：大学二年级");
        }
        if (workExperience.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "工作经验过长");
        }
        if (expertiseDirection.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "擅长方向过长");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (!Validator.isMobile(phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        if (!Validator.isEmail(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        // 注册，防止并发
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 密码加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 操作数据库
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserAvatar(userAavatar);
            user.setUserProfile(userProfile);
            user.setUserName(userName);
            user.setPhoneNumber(phoneNumber);
            user.setEmail(email);
            user.setGrade(grade);
            user.setWorkExperience(workExperience);
            user.setExpertiseDirection(expertiseDirection);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户登录
     * Sa-Token:https://sa-token.cc/doc.html#/use/login-auth
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return com.rich.richsynapsehub.model.vo.LoginUserVO
     * @author DuRuiChi
     * @create 2025/6/3
     **/
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("用户登录失败，userAccount与userPassword不匹配");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        // 弃用 severlet 方式存储登录态
//        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // Sa-Token 框架存储登录态,并指定登录设备类型，实现同端登录互斥
        // TODO 设备类型多样化：PC端、移动端、小程序端
        StpUtil.login(user.getId(), SaTokenLoginDeviceUtils.getUserDevice(request));
        // 将当前用户数据存入 Sa-Token 提供的缓存 Session 中，用于后续操作
        StpUtil.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     * Sa-Token:https://sa-token.cc/doc.html#/use/login-auth
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 从 Sa-Token 提供的方法中获取当前用户 id
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (Objects.isNull(loginId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 由于用户数据变更不频繁，暂不从数据库查询
//        return  this.getById((String) loginId);

        // 返回缓存中的用户数据
        // 通过 Sa - Token 提供的工具类 StpUtil 获取当前用户登陆时存储的 SaSession 本地缓存对象
        // 通过 get 获取 dataMap 属性中存放的当前登录的用户的 User 对象
        return (User) StpUtil.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
    }

    /**
     * 获取当前登录用户（允许未登录）
     * Sa-Token:https://sa-token.cc/doc.html#/use/login-auth
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 从 Sa-Token 提供的方法中获取当前用户 id
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (Objects.isNull(loginId)) {
            return null;
        }

        // 由于用户数据变更不频繁，暂不从数据库查询
//        return  this.getById((String) loginId);

        // 返回缓存中的用户数据
        // 通过 Sa - Token 提供的工具类 StpUtil 获取当前用户登陆时存储的 SaSession 本地缓存对象
        // 通过 get 获取 dataMap 属性中存放的当前登录的用户的 User 对象
        User user = (User) StpUtil.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (user == null || user.getId() == null) {
            return null;
        }
        return user;
    }

    /**
     * 是否为管理员
     * Sa-Token:https://sa-token.cc/doc.html#/use/login-auth
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        // Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        Object userObj = StpUtil.getSession().get(USER_LOGIN_STATE);

        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     * Sa-Token:https://sa-token.cc/doc.html#/use/login-auth
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 检查登录
        StpUtil.checkLogin();
        // 执行注销
        StpUtil.logout();
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        String phoneNumber = userQueryRequest.getPhoneNumber();
        String email = userQueryRequest.getEmail();
        String grade = userQueryRequest.getGrade();
        String workExperience = userQueryRequest.getWorkExperience();
        String expertiseDirection = userQueryRequest.getExpertiseDirection();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StringUtils.isNotBlank(phoneNumber), "phoneNumber", phoneNumber);
        queryWrapper.like(StringUtils.isNotBlank(email), "email", email);
        queryWrapper.like(StringUtils.isNotBlank(grade), "grade", grade);
        queryWrapper.like(StringUtils.isNotBlank(workExperience), "workExperience", workExperience);
        queryWrapper.like(StringUtils.isNotBlank(expertiseDirection), "expertiseDirection", expertiseDirection);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 更新用户信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return boolean
     * @author DuRuiChi
     * @create 2025/6/8
     **/
    @Override
    public boolean updateUserById(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        String userName = userUpdateMyRequest.getUserName();
        String userProfile = userUpdateMyRequest.getUserProfile();
        String userPassword = userUpdateMyRequest.getUserPassword();
        String phoneNumber = userUpdateMyRequest.getPhoneNumber();
        String email = userUpdateMyRequest.getEmail();
        String grade = userUpdateMyRequest.getGrade();
        String workExperience = userUpdateMyRequest.getWorkExperience();
        String expertiseDirection = userUpdateMyRequest.getExpertiseDirection();
        String checkPassword = userUpdateMyRequest.getCheckPassword();

        // 若填写了数据，则进行合法校验
        if (userName != null) {
            if (userName.length() > 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称过长");
            }
        }
        if (userPassword != null) {
            if (checkPassword == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入确认密码");
            }
            if (!userPassword.equals(checkPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
            }
            if (userPassword.length() < 8 || checkPassword.length() < 8) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
            }
        }
        if (userProfile != null) {
            if (userProfile.length() >= 100) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户简介过长");
            }
        }
        if (grade != null) {
            if (grade.length() > 10) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请正确输入年级");
            }
        }
        if (workExperience != null) {
            if (workExperience.length() > 100) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "工作经验过长");
            }
        }
        if (expertiseDirection != null) {
            if (expertiseDirection.length() > 50) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "擅长方向过长");
            }
        }
        if (phoneNumber != null) {
            if (!Validator.isMobile(phoneNumber)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
            }
        }
        if (email != null) {
            if (!Validator.isEmail(email)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
            }
        }
        User loginUser = this.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        return this.updateById(user);
    }
}
