package top.crush.mall.api.service.impl;

import org.springframework.stereotype.Service;
import top.crush.mall.api.common.Constants;
import top.crush.mall.api.common.MallException;
import top.crush.mall.api.common.ServiceResultEnum;
import top.crush.mall.api.controller.param.MallUserUpdateParam;
import top.crush.mall.api.entity.MallUser;
import top.crush.mall.api.entity.MallUserToken;
import top.crush.mall.api.mapper.MallUserMapper;
import top.crush.mall.api.mapper.MallUserTokenMapper;
import top.crush.mall.api.service.MallUserService;
import top.crush.mall.api.util.Md5Util;
import top.crush.mall.api.util.NumberUtil;
import top.crush.mall.api.util.SystemUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Crush
 * @date 2021/4/23
 * @dercsiption MallUserServiceImpl
 */
@Service
public class MallUserServiceImpl implements MallUserService {
    @Resource
    private MallUserMapper mallUserMapper;
    @Resource
    private MallUserTokenMapper mallUserTokenMapper;

    @Override
    public String register(String loginName, String password) {
        if (mallUserMapper.selectByLoginName(loginName) != null) {
            return ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        registerUser.setIntroduceSign(Constants.USER_INTRO);
        String passwordMd5 = Md5Util.encode(password, "UTF-8");
        registerUser.setPasswordMd5(passwordMd5);
        if (mallUserMapper.insertSelective(registerUser) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String login(String loginName, String passwordMd5) {
        MallUser user = mallUserMapper.selectByLoginNameAndPasswd(loginName, passwordMd5);
        if (user != null) {
            if (user.getLockedFlag() == 1) {
                return ServiceResultEnum.LOGIN_USER_LOCKED_ERROR.getResult();
            }            //登录后即执行修改token的操作
            String token = getNewToken(System.currentTimeMillis() + "", user.getUserId());
            MallUserToken mallUserToken = mallUserTokenMapper.selectByPrimaryKey(user.getUserId());
            //当前时间
            Date now = new Date();
            //过期时间 48 小时
            Date expireTime = new Date(now.getTime() + 2 * 24 * 3600 * 1000);
            if (mallUserToken == null) {
                mallUserToken = new MallUserToken();
                mallUserToken.setUserId(user.getUserId());
                mallUserToken.setToken(token);
                mallUserToken.setUpdateTime(now);
                mallUserToken.setExpireTime(expireTime);
                //新增一条token数据
                if (mallUserTokenMapper.insertSelective(mallUserToken) > 0) {
                    //新增成功后返回 token
                    return token;
                }
            } else {
                mallUserToken.setToken(token);
                mallUserToken.setUpdateTime(now);
                mallUserToken.setExpireTime(expireTime);
                //更新
                if (mallUserTokenMapper.updateByPrimaryKeySelective(mallUserToken) > 0) {
                    //修改成功后返回 token
                    return token;
                }
            }
        }
        return ServiceResultEnum.LOGIN_ERROR.getResult();
    }

    /**
     * 获取token值     *
     *
     * @param timeStr 时间
     * @param userId  用户id     *
     * @return String
     */
    private String getNewToken(String timeStr, Long userId) {
        String src = timeStr + userId + NumberUtil.genRandomNum(4);
        return SystemUtil.genToken(src);
    }

    @Override
    public Boolean updateUserInfo(MallUserUpdateParam mallUser, Long userId) {
        MallUser user = mallUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            MallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        user.setNickName(mallUser.getNickName());
        user.setPasswordMd5(mallUser.getPasswordMd5());
        user.setIntroduceSign(mallUser.getIntroduceSign());
        return mallUserMapper.updateByPrimaryKeySelective(user) > 0;
    }

    @Override
    public Boolean logout(Long userId) {
        return mallUserTokenMapper.deleteByPrimaryKey(userId) > 0;
    }
}
