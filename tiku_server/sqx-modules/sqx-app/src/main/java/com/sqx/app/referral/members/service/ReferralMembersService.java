package com.sqx.app.referral.members.service;

import com.sqx.app.referral.members.domain.vo.AppReferralMemberVO;

import java.util.List;

/**
 * 推广下级成员列表
 */
public interface ReferralMembersService {

    /**
     * 查询当前邀请人的直接下级成员（按注册时间倒序）
     *
     * @param userId 当前登录用户 ID
     * @return 下级成员列表
     */
    List<AppReferralMemberVO> listMembers(Long userId);

    /**
     * 按层级查询邀请下级（1=直接下级，2=间接下级）
     *
     * @param userId         当前登录用户 ID
     * @param level          层级：1 或 2
     * @param withCommission 是否附带分佣金额
     */
    List<AppReferralMemberVO> listMembers(Long userId, int level, boolean withCommission);
}
