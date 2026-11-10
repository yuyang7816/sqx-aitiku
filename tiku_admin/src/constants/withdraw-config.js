/** 提现规则配置（对应 sys_config.param_type = withdraw） */
export const WITHDRAW_PARAM_TYPE = 'withdraw'
export const WITHDRAW_PARAM_TYPE_DESC = '提现'

export const WITHDRAW_CONFIG_SECTIONS = [
  {
    id: 'withdraw',
    title: '提现规则',
    items: [
      {
        key: 'sys_withdraw_min',
        label: '最低提现金额',
        unit: '元',
        display: 'amount',
        defaultValue: '50',
        type: 'number',
        min: 1,
        max: 99999,
        step: 1,
        precision: 0,
        required: true,
        remark: '用户单次提现不得低于该金额；C 端须为 50 元的整数倍（与步进规则一致）',
      },
      {
        key: 'sys_withdraw_max',
        label: '最高提现金额',
        unit: '元',
        display: 'amount',
        defaultValue: '5000',
        type: 'number',
        min: 1,
        max: 999999,
        step: 1,
        precision: 0,
        required: true,
        remark: '用户单次提现不得超过该金额',
      },
      {
        key: 'sys_withdraw_rate',
        label: '提现手续费率',
        unit: '%',
        display: 'percent',
        defaultValue: '0',
        type: 'number',
        min: 0,
        max: 100,
        step: 0.1,
        precision: 1,
        required: true,
        remark: '按申请金额比例收取；例如填 2 表示收取 2% 手续费，到账金额 = 申请金额 × (1 - 费率)',
      },
    ],
  },
]

export const WITHDRAW_CONFIG_DEFS = WITHDRAW_CONFIG_SECTIONS.flatMap((section) => section.items)
