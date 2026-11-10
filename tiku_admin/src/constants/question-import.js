const BASE_QUESTION_FIELDS = {
  difficulty: '简单',
  videoUrl: '',
  videoTeacher: '李老师',
  videoDuration: '02:30',
  videoSummary: '本题专属讲解视频，请结合文字解析复盘。',
  tags: [],
  sortOrder: 1,
  state: 1,
}

/** 题目批量导入 JSON 模板（含三种题型真实样例） */
export const QUESTION_IMPORT_TEMPLATE = [
  {
    practiceEntry: 'chapter',
    subject: '数学',
    grade: '初中一年级',
    version: '人教版',
    chapterTitle: '第一章：有理数',
    sectionTitle: '1.1 正数和负数',
    title: '下列各数中是负数的是（  ）',
    questionType: '单选题',
    rightAnswer: 'A',
    analysis: '负数是小于 0 的数，-3 符合题意。',
    options: [
      { label: 'A', value: '-3' },
      { label: 'B', value: '0' },
      { label: 'C', value: '2' },
      { label: 'D', value: '5' },
    ],
    tags: ['有理数', '正负数'],
    ...BASE_QUESTION_FIELDS,
  },
  {
    practiceEntry: 'chapter',
    subject: '数学',
    grade: '初中一年级',
    version: '人教版',
    chapterTitle: '第一章：有理数',
    sectionTitle: '1.1 正数和负数',
    title: '下列各数中，属于负数的有（  ）',
    questionType: '多选题',
    rightAnswer: 'A,C',
    analysis: '-3 和 -1.5 均小于 0，属于负数；0 既不是正数也不是负数；2 是正数。',
    options: [
      { label: 'A', value: '-3' },
      { label: 'B', value: '0' },
      { label: 'C', value: '-1.5' },
      { label: 'D', value: '2' },
    ],
    tags: ['有理数', '多选题'],
    difficulty: '中等',
    ...BASE_QUESTION_FIELDS,
  },
  {
    practiceEntry: 'chapter',
    subject: '数学',
    grade: '初中一年级',
    version: '人教版',
    chapterTitle: '第一章：有理数',
    sectionTitle: '1.1 正数和负数',
    title: '0 是有理数。',
    questionType: '判断题',
    rightAnswer: 'A',
    analysis: '有理数包括整数和分数，0 是整数，因此 0 是有理数，该说法正确。',
    options: [
      { label: 'A', value: '正确' },
      { label: 'B', value: '错误' },
    ],
    tags: ['有理数', '判断题'],
    ...BASE_QUESTION_FIELDS,
  },
]

/** 下载导入模板 JSON */
export function downloadQuestionImportTemplate() {
  const blob = new Blob([JSON.stringify(QUESTION_IMPORT_TEMPLATE, null, 2)], {
    type: 'application/json;charset=utf-8',
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'question-import-template.json'
  link.click()
  URL.revokeObjectURL(url)
}

/** 解析上传的 JSON 导入文件 */
export async function parseQuestionImportFile(file) {
  if (!file) {
    throw new Error('请选择 JSON 文件')
  }
  const text = await file.text()
  let data
  try {
    data = JSON.parse(text)
  } catch {
    throw new Error('JSON 格式无效，请检查文件内容')
  }
  if (!Array.isArray(data)) {
    throw new Error('JSON 须为数组格式，每项为一道题')
  }
  if (data.length === 0) {
    throw new Error('导入列表不能为空')
  }
  return data
}
