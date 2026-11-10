/** 题目讲解视频配置（对应 sys_config.param_type = question_video） */
export const QUESTION_VIDEO_PARAM_TYPE = 'question_video'
export const QUESTION_VIDEO_PARAM_TYPE_DESC = '题目讲解视频'

/** C 端接口 */
export const APP_QUESTION_VIDEO_API = 'GET /app/question/video'

export const QUESTION_VIDEO_CONFIG_SECTIONS = [
  {
    id: 'default',
    title: '默认讲解视频（兜底）',
    items: [
      {
        key: 'app_default_question_video_url',
        label: '默认视频地址',
        defaultValue: 'https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/2minute-demo.mp4',
        type: 'file',
        accept: 'video/*,.mp4,.mov,.m4v',
        placeholder: '题目未单独配置视频时，会员在 C 端看到的默认讲解视频',
        required: true,
      },
      {
        key: 'app_default_question_video_teacher',
        label: '默认讲师',
        defaultValue: 'AI 名师',
        type: 'text',
        maxLength: 64,
      },
      {
        key: 'app_default_question_video_duration',
        label: '默认时长文案',
        defaultValue: '02:00',
        type: 'text',
        maxLength: 16,
        placeholder: '如 02:30',
      },
      {
        key: 'app_default_question_video_summary',
        label: '默认视频简介',
        defaultValue: '逐步拆解解题思路，帮助你掌握同类题型的通法与易错点。',
        type: 'textarea',
        rows: 3,
        maxLength: 512,
      },
    ],
  },
]

export const QUESTION_VIDEO_CONFIG_DEFS = QUESTION_VIDEO_CONFIG_SECTIONS.flatMap((section) => section.items)
