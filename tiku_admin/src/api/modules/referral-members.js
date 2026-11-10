import { getAppUserInfo, getAppUserPage } from '@/api/modules/app-user'

/**
 * 查询邀请人的直接下级成员（按 superior_invitation_code 匹配）
 */
export async function getReferralMembersPage(params = {}) {
  const {
    inviterPhone,
    inviterUserName,
    memberPhone,
    memberUserName,
    payStatus,
    page = 1,
    limit = 10,
  } = params

  if (!inviterPhone && !inviterUserName) {
    return { inviter: null, list: [], total: 0 }
  }

  const inviterPage = await getAppUserPage({
    page: 1,
    limit: 1,
    phone: inviterPhone || undefined,
    username: inviterUserName || undefined,
  })
  const inviter = inviterPage.list?.[0]
  if (!inviter?.invitationCode) {
    return { inviter, list: [], total: 0 }
  }

  const memberPage = await getAppUserPage({
    page,
    limit,
    phone: memberPhone || undefined,
    username: memberUserName || undefined,
    superiorInvitationCode: inviter.invitationCode,
    isVip: payStatus === 0 || payStatus === 1 ? payStatus : undefined,
  })

  return {
    inviter,
    list: memberPage.list || [],
    total: memberPage.total,
  }
}

export function getReferralInviterInfo(userId) {
  return getAppUserInfo(userId)
}
