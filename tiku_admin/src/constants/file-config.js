/** 文件存储配置（对应 sys_config.param_type = file） */
export const FILE_PARAM_TYPE = 'file'
export const FILE_PARAM_TYPE_DESC = '文件上传'

export const FILE_STORAGE_OPTIONS = [
  { value: 'local', label: '本地存储（开发环境）' },
  { value: 'ali', label: '阿里云 OSS（生产推荐）' },
]

export const FILE_CONFIG_SECTIONS = [
  {
    id: 'storage',
    title: '存储方式',
    items: [
      {
        key: 'file_storage_type',
        label: '存储类型',
        defaultValue: 'ali',
        type: 'select',
        options: FILE_STORAGE_OPTIONS,
        required: true,
      },
      {
        key: 'file_local_upload_path',
        label: '本地上传目录',
        defaultValue: '/upload',
        type: 'text',
        maxLength: 256,
        showWhen: (form) => form.file_storage_type === 'local',
      },
    ],
  },
  {
    id: 'oss',
    title: '阿里云 OSS',
    items: [
      {
        key: 'file_cdn_domain',
        label: 'CDN 访问域名',
        defaultValue: '',
        type: 'text',
        maxLength: 256,
        showWhen: (form) => form.file_storage_type === 'ali',
      },
      {
        key: 'file_endpoint',
        label: 'OSS Endpoint',
        defaultValue: 'https://oss-cn-beijing.aliyuncs.com',
        type: 'text',
        maxLength: 256,
        showWhen: (form) => form.file_storage_type === 'ali',
      },
      {
        key: 'file_bucket',
        label: 'Bucket 名称',
        defaultValue: '',
        type: 'text',
        maxLength: 128,
        showWhen: (form) => form.file_storage_type === 'ali',
      },
      {
        key: 'file_ali_oss_accessKeyId',
        label: 'AccessKey ID',
        defaultValue: '',
        type: 'text',
        maxLength: 128,
        showWhen: (form) => form.file_storage_type === 'ali',
      },
      {
        key: 'file_ali_oss_secretAccessKey',
        label: 'AccessKey Secret',
        defaultValue: '',
        type: 'password',
        maxLength: 128,
        showWhen: (form) => form.file_storage_type === 'ali',
      },
    ],
  },
]

export const FILE_CONFIG_DEFS = FILE_CONFIG_SECTIONS.flatMap((section) => section.items)
